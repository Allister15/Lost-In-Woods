package com.LostInWoods.app.dto;

import jakarta.validation.constraints.*;

/**
 * ChoiceOutcomeResponse DTO
 * Represents the outcome of a player's choice in API responses.
 * Single Responsibility: Outcome data exposure with updated game state.
 */
public class ChoiceOutcomeResponse {

    @NotBlank(message = "Outcome description cannot be blank")
    private String outcomeDescription;

    @NotNull(message = "Health changed by value cannot be null")
    private Integer healthChangedBy;

    @NotNull(message = "New health cannot be null")
    @Min(value = 0, message = "Health cannot be negative")
    @Max(value = 100, message = "Health cannot exceed 100")
    private Integer newHealth;

    @NotNull(message = "Next scene ID cannot be null")
    private Long nextSceneId;

    @NotNull(message = "Player alive flag cannot be null")
    private Boolean playerAlive;

    @NotNull(message = "Updated player information cannot be null")
    private PlayerResponse updatedPlayer;

    // Constructors
    public ChoiceOutcomeResponse() {}

    public ChoiceOutcomeResponse(String outcomeDescription, Integer healthChangedBy, Integer newHealth, Long nextSceneId, Boolean playerAlive, PlayerResponse updatedPlayer) {
        this.outcomeDescription = outcomeDescription;
        this.healthChangedBy = healthChangedBy;
        this.newHealth = newHealth;
        this.nextSceneId = nextSceneId;
        this.playerAlive = playerAlive;
        this.updatedPlayer = updatedPlayer;
    }

    // Getters and Setters
    public String getOutcomeDescription() { return outcomeDescription; }
    public void setOutcomeDescription(String outcomeDescription) { this.outcomeDescription = outcomeDescription; }

    public Integer getHealthChangedBy() { return healthChangedBy; }
    public void setHealthChangedBy(Integer healthChangedBy) { this.healthChangedBy = healthChangedBy; }

    public Integer getNewHealth() { return newHealth; }
    public void setNewHealth(Integer newHealth) { this.newHealth = newHealth; }

    public Long getNextSceneId() { return nextSceneId; }
    public void setNextSceneId(Long nextSceneId) { this.nextSceneId = nextSceneId; }

    public Boolean getPlayerAlive() { return playerAlive; }
    public boolean isPlayerAlive() { return Boolean.TRUE.equals(playerAlive); }
    public void setPlayerAlive(Boolean playerAlive) { this.playerAlive = playerAlive; }

    public PlayerResponse getUpdatedPlayer() { return updatedPlayer; }
    public void setUpdatedPlayer(PlayerResponse updatedPlayer) { this.updatedPlayer = updatedPlayer; }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String outcomeDescription;
        private Integer healthChangedBy;
        private Integer newHealth;
        private Long nextSceneId;
        private Boolean playerAlive;
        private PlayerResponse updatedPlayer;

        public Builder outcomeDescription(String outcomeDescription) {
            this.outcomeDescription = outcomeDescription;
            return this;
        }

        public Builder healthChangedBy(Integer healthChangedBy) {
            this.healthChangedBy = healthChangedBy;
            return this;
        }

        public Builder newHealth(Integer newHealth) {
            this.newHealth = newHealth;
            return this;
        }

        public Builder nextSceneId(Long nextSceneId) {
            this.nextSceneId = nextSceneId;
            return this;
        }

        public Builder playerAlive(Boolean playerAlive) {
            this.playerAlive = playerAlive;
            return this;
        }

        public Builder updatedPlayer(PlayerResponse updatedPlayer) {
            this.updatedPlayer = updatedPlayer;
            return this;
        }

        public ChoiceOutcomeResponse build() {
            return new ChoiceOutcomeResponse(this.outcomeDescription, this.healthChangedBy, this.newHealth, this.nextSceneId, this.playerAlive, this.updatedPlayer);
        }
    }
}
