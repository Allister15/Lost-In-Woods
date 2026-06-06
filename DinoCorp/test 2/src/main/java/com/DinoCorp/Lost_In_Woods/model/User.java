package com.DinoCorp.Lost_In_Woods.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

// A permanent player account. The username (canonical, lowercased) is the PRIMARY KEY,
// which gives case-insensitive uniqueness for free; display_name keeps the original case.
// session_id is the permanent, cryptographically-random account token bound to the name.
//
// Identity model: a User is the ACCOUNT. Each playthrough is a separate GameSession (Long
// id) linked to the account by player_name == display_name — so achievements and the
// leaderboard already aggregate under the username.
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(length = 40)
    private String username;          // canonical (trimmed + lowercased) — the PK

    @Column(name = "display_name", length = 40, nullable = false)
    private String displayName;       // exactly as the player typed it

    @Column(name = "session_id", unique = true, nullable = false)
    private UUID sessionId;           // permanent account token (UUID)

    @Column(name = "created_at")
    private Instant createdAt;
}
