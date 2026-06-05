package com.DinoCorp.Lost_In_Woods.ai.engine;

import com.DinoCorp.Lost_In_Woods.ai.dto.StoryBeatPayload.Choice;
import com.DinoCorp.Lost_In_Woods.ai.engine.ChapterAnchors.AnchorBeat;
import com.DinoCorp.Lost_In_Woods.ai.engine.StoryGuardrails.CorruptionTracker;
import com.DinoCorp.Lost_In_Woods.service.StoryGeneratorAsyncWorker;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

// Pure-logic guardrail checks — no Spring context, no database. Proves the authored
// anchor data + filler templates satisfy every GAME ENGINE GUARDRAIL, so
// generateInitialTree can never throw on this content at runtime.
class HybridEngineLogicTest {

    private final StoryGuardrails guardrails = new StoryGuardrails();
    private final FillerNarrativeFactory filler = new FillerNarrativeFactory();

    @Test
    void everyAnchorAndEndingIsExactlyTwoSentences() {
        for (AnchorBeat a : ChapterAnchors.CHAPTER_ANCHORS) {
            assertEquals(2, guardrails.sentenceCount(a.narrative()), "anchor: " + a.narrative());
        }
        for (AnchorBeat e : List.of(ChapterAnchors.ENDING_DEATH, ChapterAnchors.ENDING_TRANSFORMATION,
                ChapterAnchors.ENDING_LOST, ChapterAnchors.ENDING_ESCAPE)) {
            assertEquals(2, guardrails.sentenceCount(e.narrative()), "ending: " + e.narrative());
        }
    }

    @Test
    void everyFillerBeatIsExactlyTwoSentences() {
        for (String loc : List.of("dense_forest", "clearing", "swamp", "cliff", "stream", "pond", "cave")) {
            for (int turn = 1; turn <= 3; turn++) {
                assertEquals(2, guardrails.sentenceCount(filler.build(loc, turn).narrative()),
                        loc + " turn " + turn);
            }
        }
    }

    @Test
    void corruptionArcsAreSequentialAndOneWay() {
        CorruptionTracker tracker = guardrails.newCorruptionTracker();
        for (AnchorBeat a : ChapterAnchors.CHAPTER_ANCHORS) {
            assertDoesNotThrow(() -> tracker.register(a.npc(), a.stance()), "npc " + a.npc() + " / " + a.stance());
        }
        // Adam tops out at full_demon for the climax; the seven mid_demon arcs all landed.
        assertDoesNotThrow(() -> tracker.register("adam", "full_demon"));
        assertEquals(3, tracker.currentStage("adam"));
        assertEquals(2, tracker.currentStage("nagi"));
        assertEquals(2, tracker.currentStage("banner"));
    }

    @Test
    void skippingDirectlyToFullDemonIsRejected() {
        CorruptionTracker tracker = guardrails.newCorruptionTracker();
        assertThrows(IllegalStateException.class, () -> tracker.register("vincent", "full_demon"));
    }

    @Test
    void ghostItemChoiceIsSwappedOut() {
        // Chapter 12 offers "Use small hand mirror defensively", but the stored inventory
        // is only the Survival Knife -> the mirror option must be replaced, knife kept.
        AnchorBeat ch12 = ChapterAnchors.CHAPTER_ANCHORS.get(11);
        List<Choice> safe = guardrails.sanitizeChoices(ch12.choiceTexts(), ch12.items());
        assertEquals(4, safe.size());
        assertTrue(safe.stream().noneMatch(c -> c.text().toLowerCase(Locale.ROOT).contains("mirror")),
                "ghost-item mirror choice should have been swapped");
        assertTrue(safe.stream().anyMatch(c -> c.text().toLowerCase(Locale.ROOT).contains("knife")),
                "held knife choice should survive");
    }

    @Test
    void nodeIndexMappingIsOneBasedAndContiguous() {
        assertEquals(1, StoryGeneratorAsyncWorker.nodeIndex(1, 1));   // first beat
        assertEquals(4, StoryGeneratorAsyncWorker.nodeIndex(1, 4));   // chapter 1 anchor
        assertEquals(19, StoryGeneratorAsyncWorker.nodeIndex(5, 3));  // chapter 5, turn 3
        assertEquals(80, StoryGeneratorAsyncWorker.nodeIndex(20, 4)); // terminal node
    }

    @Test
    void fillerRampsLandOnTheAnchorValues() {
        // Turn 4 of the interpolation formula must equal the chapter's authored anchor hp/score.
        for (int chapter = 1; chapter <= 19; chapter++) {
            AnchorBeat anchor = ChapterAnchors.CHAPTER_ANCHORS.get(chapter - 1);
            assertEquals(anchor.hp(), ChapterAnchors.rampHp(chapter, 4), "hp rail ch" + chapter);
            assertEquals(anchor.score(), ChapterAnchors.rampScore(chapter, 4), "score rail ch" + chapter);
        }
    }

    @Test
    void chapter20AnchorSlotIsTheEndingPlaceholder() {
        assertSame(ChapterAnchors.ENDING_DEATH, ChapterAnchors.anchorForChapter(20));
        assertEquals("dense_forest", ChapterAnchors.anchorForChapter(1).location());
    }

    @Test
    void adamReachesFullDemonOnlyAfterMidDemon() {
        CorruptionTracker tracker = guardrails.newCorruptionTracker();
        for (AnchorBeat a : ChapterAnchors.CHAPTER_ANCHORS) {
            tracker.register(a.npc(), a.stance());                 // includes adam mid_demon at ch18
        }
        assertDoesNotThrow(() -> tracker.register(
                ChapterAnchors.ENDING_DEATH.npc(), ChapterAnchors.ENDING_DEATH.stance()));
        assertEquals(3, tracker.currentStage("adam"));
    }
}
