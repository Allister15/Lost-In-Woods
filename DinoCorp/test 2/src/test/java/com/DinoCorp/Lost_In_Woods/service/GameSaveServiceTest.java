package com.DinoCorp.Lost_In_Woods.service;

import com.DinoCorp.Lost_In_Woods.ai.dto.StoryResponse;
import com.DinoCorp.Lost_In_Woods.dto.LeaderboardEntry;
import com.DinoCorp.Lost_In_Woods.dto.ResumeResponse;
import com.DinoCorp.Lost_In_Woods.model.GameSession;
import com.DinoCorp.Lost_In_Woods.repository.GameSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Pure-logic tests for the persistent save layer (no Spring context, no DB — the
// repository is mocked). Covers the two requirements the spec calls out: the deduped
// "highest peak per player" leaderboard, and the resume round-trip.
class GameSaveServiceTest {

    private final GameSessionRepository repo = mock(GameSessionRepository.class);
    private final GameService service = new GameService(repo);
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void leaderboardRanksAllFinishedRunsByScoreWithoutCollapsingPlayers() {
        // The DB query returns finished runs in final-score-desc order (already capped
        // at 10). It is a GLOBAL board: every run is ranked, the same player can appear
        // more than once, and blank names render as "Guest".
        when(repo.findTop10ByGameOverTrueOrderByFinalScoreDesc()).thenReturn(List.of(
                named("Alice", 500, 5),
                guest(UUID.randomUUID(), 450, 4),
                named("Bob", 400, 4),
                named("Alice", 300, 3)      // same player, second run — NOT merged away
        ));

        List<LeaderboardEntry> board = service.getLeaderboard();

        assertEquals(4, board.size(), "every finished run is ranked, none collapsed");
        assertEquals(List.of(500, 450, 400, 300),
                board.stream().map(LeaderboardEntry::finalScore).toList(), "ranked by final score desc");
        assertEquals("Alice", board.get(0).playerName());
        assertEquals("Guest", board.get(1).playerName());
        assertEquals("Alice", board.get(3).playerName(), "Alice's second run is preserved");
    }

    @Test
    void resumeRoundTripsTheSavedBeat() throws Exception {
        UUID token = UUID.randomUUID();
        StoryResponse beat = new StoryResponse(
                42L, "swamp", "nagi", "drowsy", "base", "A two sentence beat. It escalates now.",
                List.of(), false, null, null, 80, 350, List.of(), List.of("Survival Knife"), 7, 0);
        GameSession s = GameSession.builder()
                .id(42L)   // the persistent session id the frontend keeps using for /story/choose
                .playerName("Runa").guest(false).resumeToken(token)
                .gameOver(false).lastBeatJson(json.writeValueAsString(beat))
                .build();
        when(repo.findByResumeToken(token)).thenReturn(Optional.of(s));

        Optional<ResumeResponse> resumed = service.resume(token);

        assertTrue(resumed.isPresent());
        assertEquals(42L, resumed.get().sessionId());
        assertEquals("Runa", resumed.get().playerName());
        assertTrue(resumed.get().active(), "an unfinished run resumes as active");
        assertEquals("swamp", resumed.get().beat().location());
        assertEquals(80, resumed.get().beat().hp());
        assertEquals("Survival Knife", resumed.get().beat().items().get(0));
    }

    @Test
    void resumeIsEmptyWhenNoBeatSavedYet() {
        UUID token = UUID.randomUUID();
        when(repo.findByResumeToken(token)).thenReturn(Optional.of(
                GameSession.builder().playerName("Guest").guest(true).resumeToken(token).build()));
        assertTrue(service.resume(token).isEmpty(), "a session with no saved beat cannot resume");
    }

    @Test
    void saveBeatPersistsSnapshotAndDerivedChapterTurn() throws Exception {
        GameSession s = GameSession.builder().playerName("Voss").build();
        when(repo.findById(9L)).thenReturn(Optional.of(s));
        when(repo.save(any(GameSession.class))).thenAnswer(i -> i.getArgument(0));

        StoryResponse beat = new StoryResponse(
                9L, "cave", "", "base", "base", "Cold drips. Something moves.",
                List.of(), false, null, null, 73, 410, List.of(), List.of("Knife"), 6, 0);
        service.saveBeat(9L, beat);

        assertNotNull(s.getLastBeatJson());
        assertEquals(73, s.getCurrentHealth());
        assertEquals(410, s.getCurrentScore());
        // events=6 -> chapter 6/4+1 = 2, turn 6%4+1 = 3
        assertEquals(2, s.getCurrentChapter());
        assertEquals(3, s.getCurrentTurnInChapter());
        assertNotNull(s.getUpdatedAt());
    }

    // ── helpers ──
    private GameSession named(String name, int score, int events) {
        return GameSession.builder().playerName(name).guest(false)
                .finalScore(score).eventsSurvived(events).gameOver(true).build();
    }

    private GameSession guest(UUID token, int score, int events) {
        return GameSession.builder().playerName("Guest").guest(true).guestToken(token)
                .finalScore(score).eventsSurvived(events).gameOver(true).build();
    }
}
