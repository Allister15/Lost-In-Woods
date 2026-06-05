package com.DinoCorp.Lost_In_Woods.service;

import com.DinoCorp.Lost_In_Woods.ai.dto.StoryBeatPayload;
import com.DinoCorp.Lost_In_Woods.ai.engine.ChapterAnchors;
import com.DinoCorp.Lost_In_Woods.ai.engine.ChapterAnchors.AnchorBeat;
import com.DinoCorp.Lost_In_Woods.ai.engine.StoryGuardrails;
import com.DinoCorp.Lost_In_Woods.ai.engine.StoryGuardrails.CorruptionTracker;
import com.DinoCorp.Lost_In_Woods.exception.ResourceNotFoundException;
import com.DinoCorp.Lost_In_Woods.model.GameSession;
import com.DinoCorp.Lost_In_Woods.model.HybridGameSession;
import com.DinoCorp.Lost_In_Woods.model.PrefilledStoryNode;
import com.DinoCorp.Lost_In_Woods.repository.GameSessionRepository;
import com.DinoCorp.Lost_In_Woods.repository.HybridGameSessionRepository;
import com.DinoCorp.Lost_In_Woods.repository.PrefilledStoryNodeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

// The Hybrid Story Engine orchestrator (TASK 2).
//
//   initializeGameSession  — create the session, build ONLY chapter 1 (so the player starts
//                            in < 2s), then kick off the rolling async pipeline.
//   processPlayerTurn      — the O(1) runtime turn: +1 index, metrics, item validity, one
//                            indexed seek, and a rolling-buffer prefetch for future chapters.
//
// "False Branching": the four choices are cosmetic — any click funnels to current_node_index
// + 1. The one place a click matters is the ending: each adds to a hidden karma tally that
// selects which of the four Chapter-20 terminal beats is finalized into node 80.
@Service
@RequiredArgsConstructor
public class GameStateService {

    private static final Logger log = LoggerFactory.getLogger(GameStateService.class);

    public static final int CHAPTERS = 20;
    public static final int TURNS_PER_CHAPTER = 4;
    public static final int TOTAL_NODES = CHAPTERS * TURNS_PER_CHAPTER; // 80
    public static final int FIRST_INDEX = 1;
    public static final int TERMINAL_INDEX = TOTAL_NODES;              // 80 (1-based)

    // Every run begins holding the survival knife (matches the milestone inventory).
    private static final List<String> STARTING_INVENTORY = List.of("Survival Knife");

    // Karma thresholds for the four endings (escape is the rarest/hardest reward).
    private static final int KARMA_ESCAPE = 200;
    private static final int KARMA_TRANSFORMATION = 150;
    private static final int KARMA_LOST = 90;

    private final HybridGameSessionRepository sessionRepo;
    private final PrefilledStoryNodeRepository nodeRepo;
    private final GameSessionRepository leaderboardRepo;     // existing leaderboard entity
    private final StoryGeneratorAsyncWorker worker;

    public record GameStartResult(String sessionId, StoryBeatPayload beat) {}

    // Boot-time guardrail: prove the hardcoded anchor script obeys the one-way
    // base -> mid_demon -> full_demon arc for every Sin BEFORE serving any traffic. Because
    // chapters are generated lazily on different threads, this static validation is what
    // enforces the SINS state machine across the whole 20-chapter script.
    @PostConstruct
    void validateAnchorCorruptionArcs() {
        CorruptionTracker tracker = new StoryGuardrails().newCorruptionTracker();
        for (AnchorBeat a : ChapterAnchors.CHAPTER_ANCHORS) {
            tracker.register(a.npc(), a.stance());
        }
        // Adam tops out at full_demon for the climax (chapter 20 endings).
        tracker.register(ChapterAnchors.ENDING_DEATH.npc(), ChapterAnchors.ENDING_DEATH.stance());
        log.info("Anchor corruption arcs validated (base -> mid_demon -> full_demon).");
    }

    // ---------------------------------------------------------------------------------
    // TASK 2.1 — initialize: build chapter 1 synchronously, launch the pipeline.
    // Intentionally NOT @Transactional: each save commits in order (session, then chapter
    // 1) so the async prefetch only ever observes committed state.
    // ---------------------------------------------------------------------------------
    public GameStartResult initializeGameSession(String playerName) {
        String sessionId = UUID.randomUUID().toString();
        List<String> inventory = new ArrayList<>(STARTING_INVENTORY);

        HybridGameSession session = HybridGameSession.builder()
                .sessionId(sessionId)
                .playerName(blankToGuest(playerName))
                .currentNodeIndex(FIRST_INDEX)
                .hp(100).score(0)
                .activeItems(new ArrayList<>(inventory))
                .runtimeScore(0)
                .finished(false)
                .build();
        sessionRepo.save(session);                                   // FK target exists first

        // Blocking, but cheap: chapter 1 = 3 fillers (< ~2s, or instant offline) + 1 anchor.
        worker.generateChapterIfNeeded(sessionId, 1, inventory);

        PrefilledStoryNode opening = requireNode(sessionId, FIRST_INDEX);
        session.setHp(opening.getHp());
        session.setScore(opening.getScore());
        session.setActiveItems(new ArrayList<>(opening.getItems()));
        sessionRepo.save(session);

        // Rolling pipeline: quietly pre-generate chapters 2 and 3 on a virtual thread.
        worker.triggerBackgroundGeneration(sessionId, 1, inventory);

        return new GameStartResult(sessionId, toPayload(opening));
    }

    // ---------------------------------------------------------------------------------
    // TASK 2.3 — runtime turn. O(1): +1 index, one indexed seek, instant payload, then a
    // non-blocking rolling-buffer top-up. A synchronous safety valve covers buffer underrun.
    // ---------------------------------------------------------------------------------
    public StoryBeatPayload processPlayerTurn(UUID sessionId, int chosenOptionIndex) {
        String sid = sessionId.toString();
        HybridGameSession session = sessionRepo.findById(sid)
                .orElseThrow(() -> new ResourceNotFoundException("Hybrid session not found: " + sid));

        // Already finished / at the terminal node: replay the ending beat.
        if (session.isFinished() || session.getCurrentNodeIndex() >= TERMINAL_INDEX) {
            ensureChapter(sid, CHAPTERS, session.getActiveItems());
            return toPayload(requireNode(sid, TERMINAL_INDEX));
        }

        // False Branching: the click is cosmetic — it only feeds the hidden ending karma.
        int clamped = Math.max(0, Math.min(TURNS_PER_CHAPTER - 1, chosenOptionIndex));
        session.setRuntimeScore(session.getRuntimeScore() + clamped);

        int nextIndex = session.getCurrentNodeIndex() + 1;      // the funnel
        int nextChapter = chapterOf(nextIndex);

        // Safety valve: if the rolling buffer underran, generate this chapter on demand now.
        ensureChapter(sid, nextChapter, session.getActiveItems());
        PrefilledStoryNode next = requireNode(sid, nextIndex);

        // Terminal node: finalize WHICH ending from accrued karma (rewrites node 80 in place).
        if (nextIndex == TERMINAL_INDEX) {
            next = finalizeEnding(session, next);
        }

        session.setCurrentNodeIndex(nextIndex);
        session.setHp(next.getHp());
        session.setScore(next.getScore());
        session.setActiveItems(new ArrayList<>(next.getItems()));   // item validity tracks the beat

        boolean ended = !"continue".equals(next.getOutcome());
        if (ended) {
            session.setFinished(true);
            writeLeaderboard(session, next);
        }
        sessionRepo.save(session);

        // Keep the buffer rolling for the chapters the player is about to reach.
        if (!ended) {
            worker.triggerBackgroundGeneration(sid, nextChapter, session.getActiveItems());
        }
        return toPayload(next);
    }

    // ---- ending finalization --------------------------------------------------------

    // Pick the terminal ending from accrued karma, rewrite node 80 IN PLACE (keeps the
    // footprint at exactly 80 rows) and re-assert the guardrails on the swapped content.
    private PrefilledStoryNode finalizeEnding(HybridGameSession session, PrefilledStoryNode terminal) {
        AnchorBeat ending = selectEnding(session.getRuntimeScore());
        terminal.setLocation(ending.location());
        terminal.setNpc(ending.npc());
        terminal.setStance(ending.stance());
        terminal.setSurvivorStance(ending.survivorStance());
        terminal.setNarrative(ending.narrative());
        terminal.setChoices(List.of());                  // endings present no choices
        terminal.setHp(ending.hp());
        terminal.setScore(ending.score());
        terminal.setItems(new ArrayList<>(ending.items()));
        terminal.setOutcome(ending.outcome());
        terminal.setEnding(ending.ending());

        StoryGuardrails g = new StoryGuardrails();
        g.assertTwoSentences(terminal.getNarrative());
        assertAdamFullDemon(terminal);
        nodeRepo.save(terminal);
        return terminal;
    }

    private AnchorBeat selectEnding(int karma) {
        if (karma >= KARMA_ESCAPE) return ChapterAnchors.ENDING_ESCAPE;
        if (karma >= KARMA_TRANSFORMATION) return ChapterAnchors.ENDING_TRANSFORMATION;
        if (karma >= KARMA_LOST) return ChapterAnchors.ENDING_LOST;
        return ChapterAnchors.ENDING_DEATH;
    }

    // Guardrail: Adam must headline every ending in his Stage-3 full_demon form.
    private void assertAdamFullDemon(PrefilledStoryNode terminal) {
        boolean ok = "adam".equals(terminal.getNpc()) && "full_demon".equals(terminal.getStance());
        String lower = terminal.getNarrative().toLowerCase(Locale.ROOT);
        ok = ok || lower.contains("adam") || lower.contains("deity") || lower.contains("deified");
        if (!ok) {
            throw new IllegalStateException("Ending guardrail violated: Adam (full_demon) must be present on the terminal beat.");
        }
    }

    // ---- pipeline helpers + integration ---------------------------------------------

    // Synchronous safety valve: guarantee a chapter's 4 nodes exist before the player needs
    // them. No-op (a cheap COUNT) once the rolling buffer has already produced the chapter.
    private void ensureChapter(String sessionId, int chapter, List<String> inventory) {
        if (nodeRepo.countBySessionIdAndChapterNumber(sessionId, chapter) < TURNS_PER_CHAPTER) {
            log.debug("Buffer underrun: generating chapter {} on demand for session {}.", chapter, sessionId);
            worker.generateChapterIfNeeded(sessionId, chapter, inventory);
        }
    }

    // Bridge a finished prefilled run into the existing leaderboard (game_sessions).
    private void writeLeaderboard(HybridGameSession session, PrefilledStoryNode terminal) {
        GameSession record = GameSession.builder()
                .playerName(session.getPlayerName())
                .currentHealth(terminal.getHp())
                .currentScore(terminal.getScore())
                .eventsSurvived(TOTAL_NODES)
                .gameOver(true)
                .ending(terminal.getEnding())
                .finalScore(terminal.getScore() + terminal.getHp())
                .build();
        leaderboardRepo.save(record);
    }

    // 1-based chapter for a node index: nodes 1-4 -> ch1, ... node 80 -> ch20.
    private int chapterOf(int nodeIndex) {
        return ((nodeIndex - 1) / TURNS_PER_CHAPTER) + 1;
    }

    private PrefilledStoryNode requireNode(String sessionId, int nodeIndex) {
        return nodeRepo.findBySessionIdAndNodeIndex(sessionId, nodeIndex)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Story node " + nodeIndex + " missing for session " + sessionId));
    }

    private StoryBeatPayload toPayload(PrefilledStoryNode n) {
        return new StoryBeatPayload(
                n.getLocation(), n.getNpc(), n.getStance(), n.getSurvivorStance(),
                n.getNarrative(), n.getChoices(), n.getHp(), n.getScore(),
                n.getItems(), n.getOutcome(), n.getEnding());
    }

    private String blankToGuest(String playerName) {
        return (playerName == null || playerName.isBlank()) ? "Guest" : playerName.trim();
    }
}
