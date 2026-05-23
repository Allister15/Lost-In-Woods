package com.LostInWoods.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * GameProgress Entity
 * Represents saved game state for a player.
 * Single Responsibility: Encapsulates game progress data and persistence.
 */
@Entity
@Table(name = "game_progress")
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
    @Min(value = 0, message = "Health cannot be negative")
    @Max(value = 100, message = "Health cannot exceed 100")
    @Column(nullable = false)
    private Integer currentHealth;

    @Column(nullable = false)
    private java.time.LocalDateTime lastSavedAt;

    @Column(nullable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        lastSavedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastSavedAt = java.time.LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public Long getCurrentSceneId() { return currentSceneId; }
    public void setCurrentSceneId(Long currentSceneId) { this.currentSceneId = currentSceneId; }

    public Integer getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(Integer currentHealth) { this.currentHealth = currentHealth; }

    public java.time.LocalDateTime getLastSavedAt() { return lastSavedAt; }
    public void setLastSavedAt(java.time.LocalDateTime lastSavedAt) { this.lastSavedAt = lastSavedAt; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long playerId;
        private Long currentSceneId;
        private Integer currentHealth;
        private java.time.LocalDateTime lastSavedAt;
        private java.time.LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder playerId(Long playerId) {
            this.playerId = playerId;
            return this;
        }

        public Builder currentSceneId(Long currentSceneId) {
            this.currentSceneId = currentSceneId;
            return this;
        }

        public Builder currentHealth(Integer currentHealth) {
            this.currentHealth = currentHealth;
            return this;
        }

        public Builder lastSavedAt(java.time.LocalDateTime lastSavedAt) {
            this.lastSavedAt = lastSavedAt;
            return this;
        }

        public Builder createdAt(java.time.LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GameProgress build() {
            GameProgress progress = new GameProgress();
            progress.id = this.id;
            progress.playerId = this.playerId;
            progress.currentSceneId = this.currentSceneId;
            progress.currentHealth = this.currentHealth;
            progress.lastSavedAt = this.lastSavedAt;
            progress.createdAt = this.createdAt;
            return progress;
        }
    }
}
