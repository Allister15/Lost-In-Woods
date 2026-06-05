package com.DinoCorp.Lost_In_Woods.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

// A single endless playthrough. The AI generates the story and reports the
// player's health, score, traits, and inventory each beat; this record holds the
// latest values plus the final score, for the leaderboard.
@Entity
@Table(name = "game_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Name entered on the splash ("Guest" for guests).
    private String playerName;

    // Latest health/score reported by the AI for this run.
    @Builder.Default
    private int currentHealth = 100;
    @Builder.Default
    private int currentScore = 0;

    // How many events (beats) the player has survived this run.
    @Builder.Default
    private int eventsSurvived = 0;

    // True once the run ends (death at 0 HP).
    @Builder.Default
    private boolean gameOver = false;

    // The death title reached (null until the run ends).
    private String ending;

    // finalScore = currentScore + currentHealth + traitPoints, computed at death.
    @Builder.Default
    private int finalScore = 0;

    // ─── AUTO-SAVE / RESUME (persistent session) ──────────────────────────────
    // Opaque, hard-to-guess resume handle. Delivered to the browser as the
    // HttpOnly "litw_resume" cookie; GET /api/game/resume looks the session up by
    // this (never by the sequential id, which would be enumerable).
    @Column(name = "resume_token", unique = true)
    private UUID resumeToken;

    // True for "Play as Guest" runs (player_name == "Guest"). Wrapper type (not
    // primitive) so legacy rows created before this column existed load their NULL as
    // null instead of throwing "Null value assigned to a property of primitive type".
    @Builder.Default
    @Column(name = "is_guest")
    private Boolean guest = false;

    // Persistent anonymous identity for a guest browser (the "litw_guest" cookie),
    // so a returning guest's runs/scores group together. Null for named users.
    @Column(name = "guest_token")
    private UUID guestToken;

    // Soft progress grouping for the endless live engine (every 4 beats = a
    // "chapter"); the hybrid prefilled engine sets these precisely. Wrapper types so
    // legacy rows with NULL in these added columns load cleanly.
    @Builder.Default
    @Column(name = "current_chapter")
    private Integer currentChapter = 1;
    @Builder.Default
    @Column(name = "current_turn_in_chapter")
    private Integer currentTurnInChapter = 1;

    // Serialized snapshots of the last beat, for resume rendering + queryability.
    @Column(name = "items_json", length = 1000)
    private String itemsJson;
    @Column(name = "traits_json", length = 1000)
    private String traitsJson;

    // The verbatim last engine beat (full StoryResponse JSON). Resume deserializes
    // this straight back so the reloaded page renders identically.
    @Column(name = "last_beat_json", length = 8000)
    private String lastBeatJson;

    // Last autosave time — drives guest expiry / pruning of stale runs.
    @Column(name = "updated_at")
    private Instant updatedAt;

    // The chosen survivor's preset id + display name, persisted so a resumed run reloads
    // the correct character portrait/name alongside the rest of the saved session.
    @Column(name = "survivor_id", length = 32)
    private String survivorId;
    @Column(name = "survivor_name", length = 64)
    private String survivorName;
}
