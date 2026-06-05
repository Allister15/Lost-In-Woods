package com.DinoCorp.Lost_In_Woods.service;

import com.DinoCorp.Lost_In_Woods.ai.dto.StoryBeatPayload.Choice;
import com.DinoCorp.Lost_In_Woods.ai.engine.ChapterAnchors;
import com.DinoCorp.Lost_In_Woods.ai.engine.ChapterAnchors.AnchorBeat;
import com.DinoCorp.Lost_In_Woods.ai.engine.FillerNarrativeFactory;
import com.DinoCorp.Lost_In_Woods.ai.engine.FillerNarrativeFactory.FillerBeat;
import com.DinoCorp.Lost_In_Woods.ai.engine.StoryGuardrails;
import com.DinoCorp.Lost_In_Woods.ai.provider.AIProvider;
import com.DinoCorp.Lost_In_Woods.ai.provider.AIProvider.ChatMessagePayload;
import com.DinoCorp.Lost_In_Woods.ai.provider.AIProvider.ChatRequest;
import com.DinoCorp.Lost_In_Woods.config.AsyncConfig;
import com.DinoCorp.Lost_In_Woods.model.PrefilledStoryNode;
import com.DinoCorp.Lost_In_Woods.repository.PrefilledStoryNodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// The Rolling Async Background Pipeline (TASK 2.2). Generates a chapter's three filler
// beats via the LLM, stitches them in front of the chapter's hardcoded milestone anchor,
// and persists all four turns. Two entry points:
//
//   triggerBackgroundGeneration(...)  — @Async, fire-and-forget; keeps a 2-chapter buffer
//                                       ahead of the player so LLM latency hides behind play.
//   generateChapterIfNeeded(...)      — synchronous, idempotent, concurrency-guarded; also
//                                       called directly for the blocking Chapter-1 build and
//                                       as the on-demand safety valve if the buffer underruns.
//
// Resilience: if the LLM is unavailable (no key / rate-limited / malformed reply) the
// deterministic FillerNarrativeFactory supplies the beat, so the game never stalls.
@Service
public class StoryGeneratorAsyncWorker {

    private static final Logger log = LoggerFactory.getLogger(StoryGeneratorAsyncWorker.class);

    public static final int TURNS_PER_CHAPTER = 4;
    public static final int FILLERS_PER_CHAPTER = 3;
    public static final int LAST_CHAPTER = 20;
    private static final int BUFFER_AHEAD = 2;   // keep N chapters generated beyond the current one

    private final AIProvider aiProvider;
    private final FillerNarrativeFactory fillerFactory;
    private final StoryGuardrails guardrails;
    private final PrefilledStoryNodeRepository nodeRepo;
    private final ObjectMapper json = new ObjectMapper();

    // Per-(session,chapter) generation claims, so two pipeline triggers never build the
    // same chapter twice. Single-instance guard; the DB's UNIQUE(session_id,node_index)
    // is the cross-instance backstop.
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    public StoryGeneratorAsyncWorker(AIProvider aiProvider, FillerNarrativeFactory fillerFactory,
                                     StoryGuardrails guardrails, PrefilledStoryNodeRepository nodeRepo) {
        this.aiProvider = aiProvider;
        this.fillerFactory = fillerFactory;
        this.guardrails = guardrails;
        this.nodeRepo = nodeRepo;
    }

    // Non-blocking: ensure the next BUFFER_AHEAD chapters exist. Runs on a virtual thread
    // so a slow LLM call never touches the request thread. Exceptions are swallowed and
    // logged — correctness is guaranteed by the synchronous safety valve in the service.
    @Async(AsyncConfig.STORY_GEN_EXECUTOR)
    public void triggerBackgroundGeneration(String sessionId, int currentChapter, List<String> inventory) {
        for (int chapter = currentChapter + 1; chapter <= currentChapter + BUFFER_AHEAD && chapter <= LAST_CHAPTER; chapter++) {
            try {
                generateChapterIfNeeded(sessionId, chapter, inventory);
            } catch (Exception e) {
                log.warn("Background generation failed for session {} chapter {}: {}", sessionId, chapter, e.toString());
            }
        }
    }

    // Idempotent + concurrency-safe. Builds turns 1-3 (LLM filler, with fallback) and
    // turn 4 (hardcoded anchor), then persists all four in a single atomic saveAll.
    public void generateChapterIfNeeded(String sessionId, int chapter, List<String> inventory) {
        if (chapter < 1 || chapter > LAST_CHAPTER) return;
        if (chapterComplete(sessionId, chapter)) return;

        String claim = sessionId + "#" + chapter;
        if (!inFlight.add(claim)) return;                 // another thread is already on it
        try {
            if (chapterComplete(sessionId, chapter)) return;   // re-check after claiming

            AnchorBeat anchor = ChapterAnchors.anchorForChapter(chapter);
            List<String> inv = (inventory == null) ? List.of() : inventory;

            List<FillerDraft> fillers = generateFillers(chapter, anchor, inv);

            List<PrefilledStoryNode> nodes = new ArrayList<>(TURNS_PER_CHAPTER);
            for (int turn = 1; turn <= FILLERS_PER_CHAPTER; turn++) {
                nodes.add(buildFillerNode(sessionId, chapter, turn, anchor.location(), fillers.get(turn - 1), inv));
            }
            nodes.add(buildAnchorNode(sessionId, chapter, anchor, inv));

            nodeRepo.saveAll(nodes);                       // atomic: all 4 turns or none
            log.debug("Generated chapter {} for session {}", chapter, sessionId);
        } catch (org.springframework.dao.DataIntegrityViolationException dup) {
            // Lost a race to another node-build (UNIQUE constraint). The winner's rows
            // stand; nothing to do.
            log.debug("Chapter {} for session {} already generated concurrently.", chapter, sessionId);
        } finally {
            inFlight.remove(claim);
        }
    }

    private boolean chapterComplete(String sessionId, int chapter) {
        return nodeRepo.countBySessionIdAndChapterNumber(sessionId, chapter) >= TURNS_PER_CHAPTER;
    }

    // ---- filler generation (LLM with deterministic fallback) ------------------------

    private record FillerDraft(String narrative, List<String> choiceTexts) {}

    private List<FillerDraft> generateFillers(int chapter, AnchorBeat anchor, List<String> inventory) {
        List<FillerDraft> parsed = tryLlmFillers(chapter, anchor, inventory);

        // Guarantee exactly 3 guardrail-valid fillers; substitute the offline factory for
        // any slot the model missed, blanked, or wrote at the wrong length.
        List<FillerDraft> out = new ArrayList<>(FILLERS_PER_CHAPTER);
        for (int turn = 1; turn <= FILLERS_PER_CHAPTER; turn++) {
            FillerDraft d = (parsed != null && parsed.size() >= turn) ? parsed.get(turn - 1) : null;
            if (!isValidDraft(d)) {
                FillerBeat fb = fillerFactory.build(anchor.location(), turn);
                d = new FillerDraft(fb.narrative(), fb.choiceTexts());
            }
            out.add(d);
        }
        return out;
    }

    private boolean isValidDraft(FillerDraft d) {
        return d != null
                && d.narrative() != null
                && guardrails.sentenceCount(d.narrative()) == 2
                && d.choiceTexts() != null
                && d.choiceTexts().size() >= TURNS_PER_CHAPTER;
    }

    // One LLM call produces all three fillers for the chapter. Returns null on any
    // failure (no key, rate-limit, network, malformed JSON) — the caller falls back.
    private List<FillerDraft> tryLlmFillers(int chapter, AnchorBeat anchor, List<String> inventory) {
        try {
            ChatRequest request = new ChatRequest(List.of(
                    new ChatMessagePayload("system", FILLER_SYSTEM_PROMPT),
                    new ChatMessagePayload("user", buildFillerUserPrompt(chapter, anchor, inventory))));
            String raw = aiProvider.ask(request).message();

            JsonNode root = json.readTree(extractJson(raw));
            JsonNode arr = root.has("fillers") ? root.get("fillers") : root;
            if (arr == null || !arr.isArray()) return null;

            List<FillerDraft> drafts = new ArrayList<>();
            for (JsonNode f : arr) {
                String narrative = f.path("narrative").asText("").trim();
                List<String> choices = new ArrayList<>();
                JsonNode choiceArr = f.get("choices");
                if (choiceArr != null && choiceArr.isArray()) {
                    for (JsonNode c : choiceArr) {
                        String text = c.isObject() ? c.path("text").asText("") : c.asText("");
                        if (!text.isBlank()) choices.add(text.trim());
                    }
                }
                drafts.add(new FillerDraft(narrative, choices));
            }
            return drafts;
        } catch (Exception e) {
            // AIUnavailableException (offline/dev) lands here too — expected, fall back.
            log.debug("LLM filler generation unavailable for chapter {} ({}); using offline factory.", chapter, e.toString());
            return null;
        }
    }

    // ---- node assembly --------------------------------------------------------------

    private PrefilledStoryNode buildFillerNode(String sessionId, int chapter, int turn, String location,
                                               FillerDraft draft, List<String> inventory) {
        guardrails.assertTwoSentences(draft.narrative());
        List<Choice> choices = guardrails.sanitizeChoices(draft.choiceTexts(), inventory);   // ghost-item guard
        return PrefilledStoryNode.builder()
                .sessionId(sessionId).nodeIndex(nodeIndex(chapter, turn)).chapterNumber(chapter).turnNumber(turn)
                .location(location).npc("").stance("base").survivorStance("base")
                .narrative(draft.narrative()).choices(choices)
                .hp(ChapterAnchors.rampHp(chapter, turn)).score(ChapterAnchors.rampScore(chapter, turn))
                .items(new ArrayList<>(inventory))
                .outcome("continue").ending(null)
                .build();
    }

    private PrefilledStoryNode buildAnchorNode(String sessionId, int chapter, AnchorBeat a, List<String> inventory) {
        guardrails.assertTwoSentences(a.narrative());
        // Choices are sanitized against the authored anchor inventory so milestone gear
        // references stay consistent regardless of the live session inventory.
        List<Choice> choices = guardrails.sanitizeChoices(a.choiceTexts(), a.items());
        return PrefilledStoryNode.builder()
                .sessionId(sessionId).nodeIndex(nodeIndex(chapter, TURNS_PER_CHAPTER)).chapterNumber(chapter).turnNumber(TURNS_PER_CHAPTER)
                .location(a.location()).npc(a.npc()).stance(a.stance()).survivorStance(a.survivorStance())
                .narrative(a.narrative()).choices(choices)
                .hp(a.hp()).score(a.score()).items(new ArrayList<>(a.items()))
                .outcome(a.outcome()).ending(a.ending())
                .build();
    }

    // 1-based linear index: chapter 1 turn 1 -> 1 ... chapter 20 turn 4 -> 80.
    public static int nodeIndex(int chapter, int turn) {
        return (chapter - 1) * TURNS_PER_CHAPTER + turn;
    }

    // ---- LLM prompt + JSON helpers --------------------------------------------------

    private static final String FILLER_SYSTEM_PROMPT = """
            You write SHORT connective horror beats for the survival game "Lost in the Woods".
            Output ONLY raw JSON (NO markdown, no code fences), EXACTLY this shape with EXACTLY 3 fillers:
            {"fillers":[{"narrative":"...","choices":["...","...","...","..."]},{"narrative":"...","choices":["...","...","...","..."]},{"narrative":"...","choices":["...","...","...","..."]}]}
            HARD RULES:
            - Each "narrative" is EXACTLY two sentences on ONE line, no line breaks. Sentence 1 = sensory dread (cold, rot, wet, wrong sounds); sentence 2 = an escalating physical threat.
            - Each filler has EXACTLY 4 choices, each a SHORT action phrase (3-7 words).
            - Only reference gear the player is HOLDING (inventory is provided). If inventory is empty, offer item-free actions only.
            - The three fillers escalate and lead TOWARD the chapter's fixed milestone (provided) but must NOT depict or resolve it, and must NOT name its character.
            - Plain text only inside strings; escape any double-quote you use.""";

    private String buildFillerUserPrompt(int chapter, AnchorBeat anchor, List<String> inventory) {
        String inv = inventory.isEmpty() ? "(none)" : String.join(", ", inventory);
        return "Chapter " + chapter + " — location: " + anchor.location() + ". Player inventory: " + inv + ".\n"
                + "The 3 fillers must build dread toward (but NOT show) this upcoming milestone:\n\""
                + anchor.narrative() + "\"\nReturn the JSON now.";
    }

    // Tolerate models that wrap JSON in markdown fences or add stray prose around it.
    private String extractJson(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            if (nl >= 0) s = s.substring(nl + 1);
            if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        }
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        return (start >= 0 && end > start) ? s.substring(start, end + 1) : s;
    }
}
