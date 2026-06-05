package com.DinoCorp.Lost_In_Woods.service;

import com.DinoCorp.Lost_In_Woods.achievement.Achievement;
import com.DinoCorp.Lost_In_Woods.ai.dto.AchievementView;
import com.DinoCorp.Lost_In_Woods.model.GameSession;
import com.DinoCorp.Lost_In_Woods.model.UserAchievement;
import com.DinoCorp.Lost_In_Woods.repository.GameSessionRepository;
import com.DinoCorp.Lost_In_Woods.repository.UserAchievementRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// TASK 1 — the backend state evaluator. evaluate(...) is called for every narrative beat;
// it checks the 24 milestone rules against the live session + beat, persists any newly
// unlocked ones to user_achievements (keyed by player identity), and returns them so the
// caller can hand them to the frontend toast. listFor(owner) backs the modal.
//
// NOTE on criteria: a few specs ("repel a high-danger threat", "resolve a chaotic event")
// aren't directly observable, so each is mapped to a concrete, deterministic condition over
// the data we actually have. The interpretation is noted inline next to each rule.
@Service
public class AchievementTrackerService {

    private static final Set<String> SINS = Set.of("adam", "banner", "yuri", "felicia", "nagi", "vincent", "sammuel");
    private static final Set<String> SECONDARY_NPCS = Set.of("ghost", "dwarf", "transformed_man", "keeper");
    private static final String NPC_PROGRESS_PREFIX = "_npc_";   // hidden progress markers (not real achievements)

    private final UserAchievementRepository repo;
    private final GameSessionRepository sessionRepo;

    // Per-run accumulator for the stateful rules (distinct stances, deltas, min hp, etc.).
    private final Map<Long, Acc> accumulators = new ConcurrentHashMap<>();

    public AchievementTrackerService(UserAchievementRepository repo, GameSessionRepository sessionRepo) {
        this.repo = repo;
        this.sessionRepo = sessionRepo;
    }

    // Immutable view of the beat that just resolved (built by StoryService).
    public record BeatSnapshot(String npc, String stance, String survivorStance,
                               int hp, int score, List<String> items, int badTraitCount, String endingType) {}

    // ── evaluation ──────────────────────────────────────────────────────────────
    public List<AchievementView> evaluate(GameSession session, BeatSnapshot b) {
        if (session == null || b == null) return List.of();
        String owner = ownerKey(session);
        Acc acc = accumulators.computeIfAbsent(session.getId(), k -> new Acc());

        // update run accumulators BEFORE checking rules
        acc.minHp = Math.min(acc.minHp, b.hp());
        if ("voss".equals(session.getSurvivorId()) && isActionStance(b.survivorStance())) {
            acc.vossStances.add(b.survivorStance().toLowerCase(Locale.ROOT));
        }
        if (hasFood(b.items())) acc.hadFood = true;
        recordNpcProgress(owner, b.npc());            // for Deep Woods Historian

        Set<String> already = unlockedIds(owner);
        List<AchievementView> newlyUnlocked = new ArrayList<>();
        for (Achievement a : Achievement.values()) {
            if (already.contains(a.id())) continue;
            if (satisfies(a, session, b, acc, owner) && persist(owner, a.id())) {
                newlyUnlocked.add(view(a, true));
            }
        }

        acc.prevItems = (b.items() == null) ? List.of() : new ArrayList<>(b.items());
        acc.prevHp = b.hp();
        return newlyUnlocked;
    }

    private boolean satisfies(Achievement a, GameSession s, BeatSnapshot b, Acc acc, String owner) {
        String sv = orEmpty(s.getSurvivorId());
        String npc = orEmpty(b.npc()).toLowerCase(Locale.ROOT);
        String stance = orEmpty(b.stance()).toLowerCase(Locale.ROOT);
        String ending = orEmpty(b.endingType()).toLowerCase(Locale.ROOT);
        int chapter = s.getEventsSurvived() / 4 + 1;           // soft chapter grouping (live engine is endless)
        boolean noDamage = b.hp() >= acc.prevHp;
        boolean lostAnItem = acc.prevItems != null && b.items() != null && b.items().size() < acc.prevItems.size();

        return switch (a) {
            // A — score thresholds
            case FOREST_WANDERER         -> b.score() >= 500;
            case DREAD_NAVIGATOR         -> b.score() >= 1000;
            case SIN_SHAKER              -> b.score() >= 2000;
            case LEGEND_OF_THE_TREELINE  -> b.score() >= 3000;

            // B — survivor specific
            case THE_PRAGMATIC_PATH      -> sv.equals("runa") && s.getEventsSurvived() >= 30;
            case APEX_PREDATOR           -> sv.equals("kane") && has(b.items(), "bow") && has(b.items(), "quiver") && SINS.contains(npc);
            case RESILIENT_INNOCENCE     -> sv.equals("esme") && chapter >= 15 && acc.minHp >= 40;
            case ANCIENT_RITUALS         -> sv.equals("voss") && acc.vossStances.size() >= 5;
            case MILITARY_DISCIPLINE     -> sv.equals("morrow") && has(b.items(), "crowbar") && SINS.contains(npc);
            case RALLYING_THE_YOUTH      -> sv.equals("pip") && b.score() > 500;

            // C — sin encounters
            case UNMASKING_PRIDE         -> npc.equals("adam") && isHumanStance(stance) && noDamage;
            case FLANNEL_AND_FURY        -> npc.equals("banner") && stance.equals("attacking") && b.hp() > 0;
            case GORGING_ON_SHADOW       -> npc.equals("sammuel") && acc.hadFood && !hasFood(b.items());
            case HOARDERS_FATE           -> npc.equals("felicia") && lostAnItem;
            case GLIMPSE_BEYOND_THE_VEIL -> stance.equals("mid_demon") && SINS.contains(npc);

            // D — inventory mastery
            case FIRST_AID               -> had(acc, "bandage") && !has(b.items(), "bandage") && b.hp() > acc.prevHp;
            case SCARCITY_MANAGEMENT     -> (had(acc, "quiver") && !has(b.items(), "quiver"))
                                          || (had(acc, "canteen") && !has(b.items(), "canteen"))
                                          || (had(acc, "gourd") && !has(b.items(), "gourd"));
            case CURIOSITYS_BURDEN       -> b.items() != null && b.items().size() >= 6;
            case TACTICAL_TOOLING        -> hasMundane(b.items()) && noDamage && !npc.isEmpty();

            // E — fate / endings
            case FORESHADOWED_CHANGE     -> ending.equals("transformation") && b.badTraitCount() >= 3;
            case THE_NEXT_WARDEN         -> ending.equals("lost");
            case LINGERING_ECHO          -> ending.equals("death");
            case BREAKING_THE_MIRROR     -> ending.equals("escape");
            case DEEP_WOODS_HISTORIAN    -> allSecondaryNpcsSeen(owner);
        };
    }

    // ── modal listing ───────────────────────────────────────────────────────────
    // All 24, with unlocked flags for the given identity (owner derived from a session).
    public List<AchievementView> listForSession(Long sessionId) {
        GameSession s = (sessionId == null) ? null : sessionRepo.findById(sessionId).orElse(null);
        return listFor(s == null ? null : ownerKey(s));
    }

    public List<AchievementView> listFor(String owner) {
        Set<String> unlocked = (owner == null) ? Set.of() : unlockedIds(owner);
        List<AchievementView> out = new ArrayList<>(Achievement.values().length);
        for (Achievement a : Achievement.values()) {
            out.add(view(a, unlocked.contains(a.id())));
        }
        return out;
    }

    public String ownerKey(GameSession s) {
        if (Boolean.TRUE.equals(s.getGuest())) {
            return s.getGuestToken() != null ? "guest:" + s.getGuestToken() : "session:" + s.getId();
        }
        String name = s.getPlayerName();
        return "name:" + (name == null ? "" : name.trim().toLowerCase(Locale.ROOT));
    }

    // ── persistence helpers ──────────────────────────────────────────────────────
    private Set<String> unlockedIds(String owner) {
        Set<String> ids = new HashSet<>();
        for (UserAchievement ua : repo.findByOwnerKey(owner)) {
            if (!ua.getAchievementId().startsWith(NPC_PROGRESS_PREFIX)) ids.add(ua.getAchievementId());
        }
        return ids;
    }

    // Insert if absent; returns true if THIS call created the row (i.e. newly unlocked).
    private boolean persist(String owner, String achievementId) {
        if (repo.existsByOwnerKeyAndAchievementId(owner, achievementId)) return false;
        try {
            repo.save(UserAchievement.builder()
                    .ownerKey(owner).achievementId(achievementId).unlockedAt(Instant.now()).build());
            return true;
        } catch (DataIntegrityViolationException dup) {
            return false;   // lost a race; the row stands
        }
    }

    private void recordNpcProgress(String owner, String npc) {
        String key = orEmpty(npc).toLowerCase(Locale.ROOT);
        if (SECONDARY_NPCS.contains(key)) persist(owner, NPC_PROGRESS_PREFIX + key);
    }

    private boolean allSecondaryNpcsSeen(String owner) {
        Set<String> seen = new HashSet<>();
        for (UserAchievement ua : repo.findByOwnerKey(owner)) {
            if (ua.getAchievementId().startsWith(NPC_PROGRESS_PREFIX)) {
                seen.add(ua.getAchievementId().substring(NPC_PROGRESS_PREFIX.length()));
            }
        }
        return seen.containsAll(SECONDARY_NPCS);
    }

    // ── small predicates ────────────────────────────────────────────────────────
    private AchievementView view(Achievement a, boolean unlocked) {
        return new AchievementView(a.id(), a.section.name(), a.section.label, a.title, a.description, unlocked);
    }

    private boolean has(List<String> items, String keyword) {
        if (items == null) return false;
        for (String i : items) if (i != null && i.toLowerCase(Locale.ROOT).contains(keyword)) return true;
        return false;
    }

    private boolean had(Acc acc, String keyword) {
        return has(acc.prevItems, keyword);
    }

    private boolean hasFood(List<String> items) {
        if (items == null) return false;
        for (String i : items) {
            String s = (i == null) ? "" : i.toLowerCase(Locale.ROOT);
            if (s.contains("meat") || s.contains("berr") || s.contains("apple") || s.contains("grape")
                    || s.contains("fruit") || s.contains("ration") || s.contains("food")) return true;
        }
        return false;
    }

    private boolean hasMundane(List<String> items) {
        if (items == null) return false;
        for (String i : items) {
            String s = (i == null) ? "" : i.toLowerCase(Locale.ROOT);
            if (s.contains("stone") || s.contains("candle") || s.contains("rope") || s.contains("cup")
                    || s.contains("twine") || s.contains("branch") || s.contains("whistle") || s.contains("locket")) return true;
        }
        return false;
    }

    // Human "mask" stances (not the demon phases) — for Unmasking Pride.
    private boolean isHumanStance(String stance) {
        return !stance.equals("mid_demon") && !stance.equals("full_demon");
    }

    private boolean isActionStance(String survivorStance) {
        String s = orEmpty(survivorStance).toLowerCase(Locale.ROOT);
        return !s.isEmpty() && !s.equals("base");
    }

    private String orEmpty(String s) { return s == null ? "" : s; }

    // Clear a finished run's accumulator (called when a run ends, to free memory).
    public void clearRun(Long sessionId) {
        if (sessionId != null) accumulators.remove(sessionId);
    }

    private static final class Acc {
        List<String> prevItems = List.of();
        int prevHp = 100;
        int minHp = 100;
        boolean hadFood = false;
        final Set<String> vossStances = new HashSet<>();
    }
}
