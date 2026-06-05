package com.DinoCorp.Lost_In_Woods.controller;

import com.DinoCorp.Lost_In_Woods.dto.AuthResponse;
import com.DinoCorp.Lost_In_Woods.dto.RegisterRequest;
import com.DinoCorp.Lost_In_Woods.model.GameSession;
import com.DinoCorp.Lost_In_Woods.service.GameService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    // Resume handle for the CURRENT run; read by GET /api/game/resume on reload.
    static final String RESUME_COOKIE = "litw_resume";
    // Persistent anonymous identity for a guest browser, across runs.
    static final String GUEST_COOKIE = "litw_guest";

    private static final Duration RESUME_TTL = Duration.ofDays(30);
    private static final Duration GUEST_TTL = Duration.ofDays(365);

    private final GameService gameService;

    public AuthController(GameService gameService) {
        this.gameService = gameService;
    }

    // Guest: reuse the browser's persistent guest identity if present (so scores group
    // across runs), else mint one. Sets the resume + guest cookies for auto-save.
    @PostMapping("/guest")
    public ResponseEntity<AuthResponse> playAsGuest(HttpServletRequest req, HttpServletResponse resp) {
        UUID guestToken = readUuidCookie(req, GUEST_COOKIE).orElseGet(UUID::randomUUID);
        GameSession session = gameService.createSession("Guest", true, guestToken);
        writeCookie(resp, req, RESUME_COOKIE, session.getResumeToken().toString(), RESUME_TTL);
        writeCookie(resp, req, GUEST_COOKIE, guestToken.toString(), GUEST_TTL);
        return ResponseEntity.ok(new AuthResponse(session.getId(), session.getPlayerName()));
    }

    // Login: create a named session and set the resume cookie for auto-save.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody RegisterRequest request,
                                              HttpServletRequest req, HttpServletResponse resp) {
        String name = request.getUsername();
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        GameSession session = gameService.createSession(name.trim(), false, null);
        writeCookie(resp, req, RESUME_COOKIE, session.getResumeToken().toString(), RESUME_TTL);
        return ResponseEntity.ok(new AuthResponse(session.getId(), session.getPlayerName()));
    }

    // ─── cookie helpers ───────────────────────────────────────────────────────

    // HttpOnly so JS can't read it (XSS-safe); SameSite=Lax + Secure-when-https so it
    // rides along on the same-origin /resume request after a refresh.
    private void writeCookie(HttpServletResponse resp, HttpServletRequest req, String name, String value, Duration ttl) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .path("/")
                .maxAge(ttl)
                .sameSite("Lax")
                .secure(req.isSecure())
                .build();
        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private Optional<UUID> readUuidCookie(HttpServletRequest req, String name) {
        return readCookie(req, name).flatMap(v -> {
            try { return Optional.of(UUID.fromString(v)); }
            catch (IllegalArgumentException e) { return Optional.empty(); }
        });
    }

    private Optional<String> readCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return Optional.empty();
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                return Optional.of(c.getValue());
            }
        }
        return Optional.empty();
    }
}
