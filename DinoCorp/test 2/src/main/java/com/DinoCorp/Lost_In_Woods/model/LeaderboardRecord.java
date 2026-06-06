package com.DinoCorp.Lost_In_Woods.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

// The global leaderboard, indexed by username (canonical, == users PK). Holds each
// registered account's HIGHEST historical score; upserted when a registered run ends.
@Entity
@Table(name = "leaderboard")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardRecord {

    @Id
    @Column(length = 40)
    private String username;            // canonical username (FK -> users.username)

    @Column(name = "display_name", length = 40)
    private String displayName;

    @Builder.Default
    @Column(name = "best_score")
    private int bestScore = 0;

    @Builder.Default
    @Column(name = "events_survived")
    private int eventsSurvived = 0;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
