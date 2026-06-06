package com.DinoCorp.Lost_In_Woods.controller;

import com.DinoCorp.Lost_In_Woods.dto.AuthResponse;
import com.DinoCorp.Lost_In_Woods.dto.RegisterRequest;
import com.DinoCorp.Lost_In_Woods.model.GameSession;
import com.DinoCorp.Lost_In_Woods.model.User;
import com.DinoCorp.Lost_In_Woods.service.AuthService;
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
    private final AuthService authService;

    public AuthController(GameService gameService, AuthService authService) {
        this.gameService = gameService;
        this.authService = authService;
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

    // Register: create a NEW unique account, then start a run session for it.
    // -> 409 "Username already taken" if it exists; 400 if blank/too long.
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request,
                                                 HttpServletRequest req, HttpServletResponse resp) {
        User user = authService.register(request.getUsername());   // ResponseStatusException -> 409/400
        return startAccountRun(user, req, resp);
    }

    // Login: verify an EXISTING account, then start a run session for it.
    // -> 404 "No account found" if the username isn't registered.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody RegisterRequest request,
                                              HttpServletRequest req, HttpServletResponse resp) {
        User user = authService.login(request.getUsername());      // ResponseStatusException -> 404/400
        return startAccountRun(user, req, resp);
    }

    // Mint a fresh non-guest run bound to the account (player_name = display name) and set the
    // resume cookie. The run's Long id drives the game loop; the account UUID is the permanent
    // identity that achievements + leaderboard aggregate under (via the username).
    private ResponseEntity<AuthResponse> startAccountRun(User user, HttpServletRequest req, HttpServletResponse resp) {
        GameSession session = gameService.createSession(user.getDisplayName(), false, null);
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
