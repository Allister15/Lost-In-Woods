package com.LostInWoods.app.dto;

import jakarta.validation.constraints.*;

/**
 * ChoiceActionRequest DTO
 * Represents a request to make a choice during gameplay.
 * Single Responsibility: Input validation and data transfer for choice actions.
 */
public class ChoiceActionRequest {

    @NotNull(message = "Player ID cannot be null")
    @Positive(message = "Player ID must be positive")
    private Long playerId;

    @NotNull(message = "Choice ID cannot be null")
    @Positive(message = "Choice ID must be positive")
    private Long choiceId;

    // Constructors
    public ChoiceActionRequest() {}

    public ChoiceActionRequest(Long playerId, Long choiceId) {
        this.playerId = playerId;
        this.choiceId = choiceId;
    }

    // Getters and Setters
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public Long getChoiceId() { return choiceId; }
    public void setChoiceId(Long choiceId) { this.choiceId = choiceId; }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long playerId;
        private Long choiceId;

        public Builder playerId(Long playerId) {
            this.playerId = playerId;
            return this;
        }

        public Builder choiceId(Long choiceId) {
            this.choiceId = choiceId;
            return this;
        }

        public ChoiceActionRequest build() {
            return new ChoiceActionRequest(this.playerId, this.choiceId);
        }
    }
}
