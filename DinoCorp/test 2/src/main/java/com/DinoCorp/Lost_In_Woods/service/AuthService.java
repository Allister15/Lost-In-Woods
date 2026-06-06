package com.DinoCorp.Lost_In_Woods.service;

import com.DinoCorp.Lost_In_Woods.model.User;
import com.DinoCorp.Lost_In_Woods.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

// Account management (TASK 2). Username-only registration + login.
//
// Atomic uniqueness: we check existsById(canonical) first for a clean error, but the real
// guarantee is the PRIMARY KEY on users.username (canonical = lowercased). Two concurrent
// registrations of the same name race to insert the same PK; the loser's insert throws
// DataIntegrityViolationException, which we convert to the same 409 "Username already taken".
//
// SECURITY NOTE: per spec there is exactly one field (username) and no password, so this is
// an identity claim, not authentication — anyone can "log in" as any existing username.
// If real account security is needed, add a password/passphrase or OAuth here.
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_USERNAME_LEN = 40;

    private final UserRepository userRepository;

    // Create a brand-new account; 409 if the name is taken (case-insensitively).
    @Transactional
    public User register(String rawUsername) {
        String display = validated(rawUsername);
        String canonical = display.toLowerCase(Locale.ROOT);
        if (userRepository.existsById(canonical)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        try {
            return userRepository.save(User.builder()
                    .username(canonical)
                    .displayName(display)
                    .sessionId(UUID.randomUUID())     // permanent, cryptographically-random account token
                    .createdAt(Instant.now())
                    .build());
        } catch (DataIntegrityViolationException raceLost) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
    }

    // Look up an existing account; 404 if it doesn't exist.
    public User login(String rawUsername) {
        String canonical = validated(rawUsername).toLowerCase(Locale.ROOT);
        return userRepository.findById(canonical)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found"));
    }

    private String validated(String raw) {
        String s = (raw == null) ? "" : raw.trim();
        if (s.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a username");
        }
        if (s.length() > MAX_USERNAME_LEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username too long (max " + MAX_USERNAME_LEN + ")");
        }
        return s;
    }
}
