package com.LostInWoods.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * Scene Entity
 * Represents a narrative scene in the Lost-in-Woods game.
 * Single Responsibility: Encapsulates scene data and persistence.
 */
@Entity
@Table(name = "scenes")
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Scene title cannot be blank")
    @Size(min = 1, max = 255, message = "Scene title must be between 1 and 255 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Scene description cannot be blank")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotNull(message = "Victory flag cannot be null")
    @Column(nullable = false)
    private Boolean isVictory;

    @NotNull(message = "Game over flag cannot be null")
    @Column(nullable = false)
    private Boolean isGameOver;

    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Choice> choices;

    @Column(nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(nullable = false)
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
        if (isVictory == null) {
            isVictory = false;
        }
        if (isGameOver == null) {
            isGameOver = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsVictory() { return isVictory; }
    public void setIsVictory(Boolean isVictory) { this.isVictory = isVictory; }

    public Boolean getIsGameOver() { return isGameOver; }
    public void setIsGameOver(Boolean isGameOver) { this.isGameOver = isGameOver; }

    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business logic methods
    public boolean isTerminalScene() {
        return isVictory || isGameOver;
    }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private Boolean isVictory;
        private Boolean isGameOver;
        private List<Choice> choices;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder isVictory(Boolean isVictory) {
            this.isVictory = isVictory;
            return this;
        }

        public Builder isGameOver(Boolean isGameOver) {
            this.isGameOver = isGameOver;
            return this;
        }

        public Builder choices(List<Choice> choices) {
            this.choices = choices;
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

        public Scene build() {
            Scene scene = new Scene();
            scene.id = this.id;
            scene.title = this.title;
            scene.description = this.description;
            scene.isVictory = this.isVictory;
            scene.isGameOver = this.isGameOver;
            scene.choices = this.choices;
            scene.createdAt = this.createdAt;
            scene.updatedAt = this.updatedAt;
            return scene;
        }
    }
}
