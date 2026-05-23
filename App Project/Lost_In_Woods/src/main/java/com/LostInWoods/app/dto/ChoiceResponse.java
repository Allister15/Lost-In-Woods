package com.LostInWoods.app.dto;

import jakarta.validation.constraints.*;

/**
 * ChoiceResponse DTO
 * Represents a choice option in API responses.
 * Single Responsibility: Choice data exposure with outcome information.
 */
public class ChoiceResponse {

    @NotNull(message = "Choice ID cannot be null")
    private Long id;

    @NotBlank(message = "Choice text cannot be blank")
    private String choiceText;

    @NotBlank(message = "Outcome description cannot be blank")
    private String outcomeDescription;

    @NotNull(message = "Health change cannot be null")
    private Integer healthChange;

    @NotNull(message = "Next scene ID cannot be null")
    private Long nextSceneId;

    @NotNull(message = "Display order cannot be null")
    @Min(value = 0, message = "Display order cannot be negative")
    private Integer displayOrder;

    // Constructors
    public ChoiceResponse() {}

    public ChoiceResponse(Long id, String choiceText, String outcomeDescription, Integer healthChange, Long nextSceneId, Integer displayOrder) {
        this.id = id;
        this.choiceText = choiceText;
        this.outcomeDescription = outcomeDescription;
        this.healthChange = healthChange;
        this.nextSceneId = nextSceneId;
        this.displayOrder = displayOrder;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChoiceText() { return choiceText; }
    public void setChoiceText(String choiceText) { this.choiceText = choiceText; }

    public String getOutcomeDescription() { return outcomeDescription; }
    public void setOutcomeDescription(String outcomeDescription) { this.outcomeDescription = outcomeDescription; }

    public Integer getHealthChange() { return healthChange; }
    public void setHealthChange(Integer healthChange) { this.healthChange = healthChange; }

    public Long getNextSceneId() { return nextSceneId; }
    public void setNextSceneId(Long nextSceneId) { this.nextSceneId = nextSceneId; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String choiceText;
        private String outcomeDescription;
        private Integer healthChange;
        private Long nextSceneId;
        private Integer displayOrder;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder choiceText(String choiceText) {
            this.choiceText = choiceText;
            return this;
        }

        public Builder outcomeDescription(String outcomeDescription) {
            this.outcomeDescription = outcomeDescription;
            return this;
        }

        public Builder healthChange(Integer healthChange) {
            this.healthChange = healthChange;
            return this;
        }

        public Builder nextSceneId(Long nextSceneId) {
            this.nextSceneId = nextSceneId;
            return this;
        }

        public Builder displayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public ChoiceResponse build() {
            return new ChoiceResponse(this.id, this.choiceText, this.outcomeDescription, this.healthChange, this.nextSceneId, this.displayOrder);
        }
    }
}
