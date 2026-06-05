package com.DinoCorp.Lost_In_Woods.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

// One unlocked achievement (or a hidden progress marker, id prefixed with "_") for a
// player identity. owner_key is the persistent identity: "guest:<uuid>" for guests,
// "name:<lowercased>" for named players — the same key the leaderboard dedupes on.
@Entity
@Table(
        name = "user_achievements",
        uniqueConstraints = @UniqueConstraint(name = "uq_owner_achievement", columnNames = {"owner_key", "achievement_id"}),
        indexes = @Index(name = "idx_ua_owner", columnList = "owner_key")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_key", length = 80, nullable = false)
    private String ownerKey;

    @Column(name = "achievement_id", length = 48, nullable = false)
    private String achievementId;

    @Column(name = "unlocked_at")
    private Instant unlockedAt;
}
