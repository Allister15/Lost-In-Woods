package com.DinoCorp.Lost_In_Woods.controller;

import com.DinoCorp.Lost_In_Woods.dto.LeaderboardEntry;
import com.DinoCorp.Lost_In_Woods.dto.ResumeResponse;
import com.DinoCorp.Lost_In_Woods.service.GameService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard() {
        return ResponseEntity.ok(gameService.getLeaderboard());
    }

    // Called by the page on load: read the HttpOnly resume cookie and return the last
    // saved beat so the run continues across a refresh. 204 when there's nothing to
    // resume (no cookie / unknown token / no saved beat) — the page then starts fresh.
    @GetMapping("/resume")
    public ResponseEntity<ResumeResponse> resume(HttpServletRequest request) {
        return readUuidCookie(request, AuthController.RESUME_COOKIE)
                .flatMap(gameService::resume)
                .<ResponseEntity<ResumeResponse>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private Optional<UUID> readUuidCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return Optional.empty();
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                try { return Optional.of(UUID.fromString(c.getValue())); }
                catch (IllegalArgumentException e) { return Optional.empty(); }
            }
        }
        return Optional.empty();
    }
}
