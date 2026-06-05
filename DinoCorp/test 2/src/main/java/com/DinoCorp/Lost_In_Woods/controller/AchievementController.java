package com.DinoCorp.Lost_In_Woods.controller;

import com.DinoCorp.Lost_In_Woods.ai.dto.AchievementView;
import com.DinoCorp.Lost_In_Woods.model.GameSession;
import com.DinoCorp.Lost_In_Woods.repository.GameSessionRepository;
import com.DinoCorp.Lost_In_Woods.service.AchievementTrackerService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Read API for the achievements modal. Returns all 24 with this player's unlocked flags.
// The owner is resolved from the explicit ?sessionId (during a run) or the HttpOnly
// "litw_resume" cookie (on the splash) — same identity the toasts unlock against.
@RestController
@RequestMapping("/api/achievements")
@CrossOrigin(origins = "*")
public class AchievementController {

    private final AchievementTrackerService tracker;
    private final GameSessionRepository sessionRepo;

    public AchievementController(AchievementTrackerService tracker, GameSessionRepository sessionRepo) {
        this.tracker = tracker;
        this.sessionRepo = sessionRepo;
    }

    @GetMapping
    public List<AchievementView> list(@RequestParam(name = "sessionId", required = false) Long sessionId,
                                      HttpServletRequest request) {
        if (sessionId != null) {
            return tracker.listForSession(sessionId);
        }
        UUID resumeToken = readUuidCookie(request, "litw_resume");
        if (resumeToken != null) {
            GameSession s = sessionRepo.findByResumeToken(resumeToken).orElse(null);
            if (s != null) return tracker.listFor(tracker.ownerKey(s));
        }
        return tracker.listFor(null);   // no identity yet -> everything locked
    }

    private UUID readUuidCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                try { return UUID.fromString(c.getValue()); }
                catch (IllegalArgumentException e) { return null; }
            }
        }
        return null;
    }
}
