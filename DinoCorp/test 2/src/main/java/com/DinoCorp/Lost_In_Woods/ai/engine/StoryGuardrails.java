package com.DinoCorp.Lost_In_Woods.ai.engine;

import com.DinoCorp.Lost_In_Woods.ai.dto.StoryBeatPayload.Choice;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// The GAME ENGINE GUARDRAILS, enforced in code at tree-build time so a bad beat can
// never reach the database (fail fast during generateInitialTree rather than crash the
// frontend parser mid-run). Three guards live here:
//   1. assertTwoSentences  — the narrative length rule.
//   2. sanitizeChoices     — the "Ghost Item Dependency Bug" guard.
//   3. CorruptionTracker   — the one-way base -> mid_demon -> full_demon state machine.
@Component
public class StoryGuardrails {

    // ---- 1. Two-sentence narrative rule ---------------------------------------------

    // Counts sentence terminators, collapsing runs (so "..." counts once, not three).
    public int sentenceCount(String narrative) {
        if (narrative == null) return 0;
        int count = 0;
        boolean inTerminator = false;
        for (int i = 0; i < narrative.length(); i++) {
            char c = narrative.charAt(i);
            boolean term = (c == '.' || c == '!' || c == '?');
            if (term && !inTerminator) count++;
            inTerminator = term;
        }
        return count;
    }

    public void assertTwoSentences(String narrative) {
        int n = sentenceCount(narrative);
        if (n != 2) {
            throw new IllegalStateException(
                    "Narrative guardrail violated: expected EXACTLY 2 sentences but found " + n + " -> " + narrative);
        }
    }

    // ---- 2. Ghost-item guard --------------------------------------------------------
    // Maps an item word a choice might mention -> the token that must appear in the
    // current inventory for that choice to be legal. Generic words ("tool", "stone")
    // are intentionally absent so the guard never over-fires on non-gear language.
    private static final Map<String, String> ITEM_KEYWORDS = new LinkedHashMap<>();
    static {
        ITEM_KEYWORDS.put("mirror", "mirror");
        ITEM_KEYWORDS.put("bow", "bow");
        ITEM_KEYWORDS.put("arrow", "arrow");
        ITEM_KEYWORDS.put("quiver", "arrow");
        ITEM_KEYWORDS.put("bandage", "bandage");
        ITEM_KEYWORDS.put("compass", "compass");
        ITEM_KEYWORDS.put("crowbar", "crowbar");
        ITEM_KEYWORDS.put("flint", "flint");
        ITEM_KEYWORDS.put("gourd", "gourd");
        ITEM_KEYWORDS.put("canteen", "canteen");
        ITEM_KEYWORDS.put("staff", "staff");
    }

    // Safe, item-agnostic replacements for any choice that references missing gear.
    private static final List<String> SAFE_FALLBACKS = List.of(
            "Hold your ground and watch",
            "Back away into the dark",
            "Steady your breathing and wait",
            "Press on without looking back");

    // Returns Choice objects for the given texts, swapping any option that references
    // gear the player isn't holding for a safe action. Defeats the Ghost Item Dependency
    // Bug — e.g. a "use the hand mirror" choice when no mirror is in `inventory`.
    public List<Choice> sanitizeChoices(List<String> choiceTexts, List<String> inventory) {
        List<String> invLower = new ArrayList<>();
        if (inventory != null) {
            for (String s : inventory) invLower.add(s.toLowerCase(Locale.ROOT));
        }
        List<Choice> out = new ArrayList<>();
        int fallbackCursor = 0;
        for (String text : choiceTexts) {
            if (referencesMissingItem(text, invLower)) {
                out.add(new Choice(SAFE_FALLBACKS.get(fallbackCursor % SAFE_FALLBACKS.size())));
                fallbackCursor++;
            } else {
                out.add(new Choice(text));
            }
        }
        return out;
    }

    private boolean referencesMissingItem(String text, List<String> invLower) {
        String t = text.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> e : ITEM_KEYWORDS.entrySet()) {
            if (t.contains(e.getKey())) {
                String requiredToken = e.getValue();
                boolean held = invLower.stream().anyMatch(item -> item.contains(requiredToken));
                if (!held) return true;
            }
        }
        return false;
    }

    // ---- 3. Corruption state machine (base -> mid_demon -> full_demon) ---------------
    public static final Set<String> SINS = Set.of("adam", "banner", "yuri", "felicia", "nagi", "vincent", "sammuel");

    // Stage ordinal for a (sin, stance) pair. Any non-demon "mask" stance is stage 1;
    // non-sin NPCs (ghost/keeper/transformed_man) and "" are untracked (0).
    public static int stageOf(String npc, String stance) {
        if ("full_demon".equals(stance)) return 3;
        if ("mid_demon".equals(stance)) return 2;
        return SINS.contains(npc) ? 1 : 0;
    }

    public CorruptionTracker newCorruptionTracker() {
        return new CorruptionTracker();
    }

    // Per-generation tracker enforcing a strictly sequential, one-way arc for each sin:
    // a stance may hold or advance one stage, but never regress and never skip a stage.
    public static final class CorruptionTracker {
        private final Map<String, Integer> maxStage = new HashMap<>();

        public void register(String npc, String stance) {
            if (!SINS.contains(npc)) return;
            int stage = stageOf(npc, stance);
            int prev = maxStage.getOrDefault(npc, 0);
            if (stage < prev) {
                throw new IllegalStateException(
                        "Corruption guardrail violated: " + npc + " regressed (" + prev + " -> " + stage + ")");
            }
            if (stage > prev + 1) {
                throw new IllegalStateException(
                        "Corruption guardrail violated: " + npc + " skipped a stage (" + prev + " -> " + stage + ")");
            }
            maxStage.put(npc, Math.max(prev, stage));
        }

        public int currentStage(String npc) {
            return maxStage.getOrDefault(npc, 0);
        }
    }
}
