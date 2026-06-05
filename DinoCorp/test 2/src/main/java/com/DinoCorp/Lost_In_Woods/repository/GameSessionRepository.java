package com.DinoCorp.Lost_In_Woods.repository;
import com.DinoCorp.Lost_In_Woods.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    // Global leaderboard: all finished runs, highest final score first (capped at 10).
    List<GameSession> findTop10ByGameOverTrueOrderByFinalScoreDesc();

    // Auto-save resume: look up the active session by its opaque cookie token.
    Optional<GameSession> findByResumeToken(UUID resumeToken);
}
