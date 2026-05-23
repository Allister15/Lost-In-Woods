package com.LostInWoods.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Choice Entity
 * Represents a player choice with outcomes and scene transitions.
 * Single Responsibility: Encapsulates choice data and persistence.
 */
@Entity
@Table(name = "choices")
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private Scene scene;

    @NotBlank(message = "Choice text cannot be blank")
    @Size(min = 1, max = 500, message = "Choice text must be between 1 and 500 characters")
    @Column(nullable = false)
    private String choiceText;

    @NotBlank(message = "Outcome description cannot be blank")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String outcomeDescription;

    @NotNull(message = "Health change cannot be null")
    @Min(value = -100, message = "Health change cannot be less than -100")
    @Max(value = 100, message = "Health change cannot be more than 100")
    @Column(nullable = false)
    private Integer healthChange;

    @NotNull(message = "Next scene ID cannot be null")
    @Column(nullable = false)
    private Long nextSceneId;

    @NotNull(message = "Display order cannot be null")
    @Min(value = 0, message = "Display order cannot be negative")
    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(nullable = false)
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
        if (displayOrder == null) {
            displayOrder = 0;
        }
        if (healthChange == null) {
            healthChange = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Scene getScene() { return scene; }
    public void setScene(Scene scene) { this.scene = scene; }

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

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Scene scene;
        private String choiceText;
        private String outcomeDescription;
        private Integer healthChange;
        private Long nextSceneId;
        private Integer displayOrder;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder scene(Scene scene) {
            this.scene = scene;
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

        public Builder createdAt(java.time.LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(java.time.LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Choice build() {
            Choice choice = new Choice();
            choice.id = this.id;
            choice.scene = this.scene;
            choice.choiceText = this.choiceText;
            choice.outcomeDescription = this.outcomeDescription;
            choice.healthChange = this.healthChange;
            choice.nextSceneId = this.nextSceneId;
            choice.displayOrder = this.displayOrder;
            choice.createdAt = this.createdAt;
            choice.updatedAt = this.updatedAt;
            return choice;
        }
    }
}
