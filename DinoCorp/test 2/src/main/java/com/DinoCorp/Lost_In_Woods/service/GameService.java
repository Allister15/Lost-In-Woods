package com.DinoCorp.Lost_In_Woods.service;

import com.DinoCorp.Lost_In_Woods.ai.dto.StoryResponse;
import com.DinoCorp.Lost_In_Woods.dto.LeaderboardEntry;
import com.DinoCorp.Lost_In_Woods.dto.ResumeResponse;
import com.DinoCorp.Lost_In_Woods.model.GameSession;
import com.DinoCorp.Lost_In_Woods.repository.GameSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Player/run bookkeeping + the persistent auto-save layer. Creates sessions (with a
// resume token), snapshots the renderable beat after every turn, restores a run on
// reload, and serves the global leaderboard. The story itself lives in StoryService.
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameSessionRepository sessionRepository;

    // Inline (not constructor-injected) so @RequiredArgsConstructor leaves it out.
    private final ObjectMapper json = new ObjectMapper();

    // ─── Session creation ─────────────────────────────────────────────────────

    // Back-compat: a plain named/guest session (guest iff the name is "Guest").
    public GameSession createSession(String playerName) {
        return createSession(playerName, "Guest".equalsIgnoreCase(playerName), null);
    }

    // Create and persist a fresh run with a resume token + identity. The id is
    // generated on save; the resume token is the opaque handle stored in the cookie.
    public GameSession createSession(String playerName, boolean isGuest, UUID guestToken) {
        return sessionRepository.save(GameSession.builder()
                .playerName(playerName)
                .guest(isGuest)
                .guestToken(guestToken)
                .resumeToken(UUID.randomUUID())
                .updatedAt(Instant.now())
                .build());
    }

    // ─── Auto-save: snapshot the latest beat after every turn ──────────────────

    // Persist the renderable state of the beat just sent to the client. hp/score/
    // gameOver/finalScore are already maintained by StoryService; this adds the
    // narrative/choices/items/traits snapshot needed to re-render on resume.
    public void saveBeat(Long sessionId, StoryResponse beat) {
        if (sessionId == null || beat == null) return;
        GameSession s = sessionRepository.findById(sessionId).orElse(null);
        if (s == null) return;

        s.setLastBeatJson(writeJsonOrNull(beat));
        s.setItemsJson(writeJsonOrNull(beat.items()));
        s.setTraitsJson(writeJsonOrNull(beat.traits()));

        int events = Math.max(0, beat.eventsSurvived());
        s.setCurrentChapter(events / 4 + 1);
        s.setCurrentTurnInChapter(events % 4 + 1);

        // Mirror live vitals (defensive — StoryService already set these).
        s.setCurrentHealth(beat.hp());
        s.setCurrentScore(beat.score());
        s.setUpdatedAt(Instant.now());
        sessionRepository.save(s);
    }

    // ─── Resume: restore the last saved beat for a cookie token ────────────────

    public Optional<ResumeResponse> resume(UUID resumeToken) {
        if (resumeToken == null) return Optional.empty();
        return sessionRepository.findByResumeToken(resumeToken).flatMap(s -> {
            if (s.getLastBeatJson() == null || s.getLastBeatJson().isBlank()) {
                return Optional.empty();   // session exists but no beat saved yet
            }
            try {
                StoryResponse beat = json.readValue(s.getLastBeatJson(), StoryResponse.class);
                return Optional.of(new ResumeResponse(
                        s.getId(), s.getPlayerName(), Boolean.TRUE.equals(s.getGuest()), !s.isGameOver(), beat));
            } catch (Exception e) {
                return Optional.empty();   // corrupt snapshot — treat as no resume
            }
        });
    }

    // ─── Leaderboard: global Top 10 — every player ranked by final score ───────

    public List<LeaderboardEntry> getLeaderboard() {
        // Global high-score board: ALL finished runs ranked by final score (the query
        // caps at 10). No per-player collapsing — one player can hold several spots.
        return sessionRepository.findTop10ByGameOverTrueOrderByFinalScoreDesc().stream()
                .map(s -> new LeaderboardEntry(displayName(s), s.getEventsSurvived(), s.getFinalScore()))
                .toList();
    }

    private String displayName(GameSession s) {
        String name = s.getPlayerName();
        return (name == null || name.isBlank()) ? "Guest" : name;
    }

    private String writeJsonOrNull(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }
}
