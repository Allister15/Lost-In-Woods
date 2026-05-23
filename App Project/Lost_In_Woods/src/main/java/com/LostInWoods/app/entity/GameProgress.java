package com.LostInWoods.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * GameProgress Entity
 * Represents a saved game state for a player.
 * Single Responsibility: Encapsulates game state persistence and save management.
 */
@Entity
@Table(name = "game_progress", indexes = {
    @Index(name = "idx_player_id", columnList = "player_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Player ID cannot be null")
    @Column(nullable = false)
    private Long playerId;

    @NotNull(message = "Current scene ID cannot be null")
    @Column(nullable = false)
    private Long currentSceneId;

    @NotNull(message = "Current health cannot be null")
    @Column(nullable = false)
    private Integer currentHealth;

    /**
     * Timestamp when the game was last saved
     */
    @Column(nullable = false)
    private LocalDateTime lastSavedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastSavedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastSavedAt = LocalDateTime.now();
    }

    /**
     * Check if the player was alive when progress was saved
     */
    public boolean wasAliveAtLastSave() {
        return currentHealth > 0;
    }
}
