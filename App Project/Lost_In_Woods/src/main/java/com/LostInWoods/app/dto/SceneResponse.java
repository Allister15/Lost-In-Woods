package com.LostInWoods.app.dto;

import jakarta.validation.constraints.*;

import java.util.List;

/**
 * SceneResponse DTO
 * Represents scene data with available choices in API responses.
 * Single Responsibility: Scene data exposure with narrative and choice information.
 */
public class SceneResponse {

    @NotNull(message = "Scene ID cannot be null")
    private Long id;

    @NotBlank(message = "Scene title cannot be blank")
    private String title;

    @NotBlank(message = "Scene description cannot be blank")
    private String description;

    @NotNull(message = "Victory flag cannot be null")
    private Boolean isVictory;

    @NotNull(message = "Game over flag cannot be null")
    private Boolean isGameOver;

    @NotNull(message = "Terminal scene flag cannot be null")
    private Boolean isTerminalScene;

    @NotNull(message = "Choices cannot be null")
    private List<ChoiceResponse> choices;

    // Constructors
    public SceneResponse() {}

    public SceneResponse(Long id, String title, String description, Boolean isVictory, Boolean isGameOver, Boolean isTerminalScene, List<ChoiceResponse> choices) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isVictory = isVictory;
        this.isGameOver = isGameOver;
        this.isTerminalScene = isTerminalScene;
        this.choices = choices;
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

    public Boolean getIsTerminalScene() { return isTerminalScene; }
    public void setIsTerminalScene(Boolean isTerminalScene) { this.isTerminalScene = isTerminalScene; }

    public List<ChoiceResponse> getChoices() { return choices; }
    public void setChoices(List<ChoiceResponse> choices) { this.choices = choices; }

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
        private Boolean isTerminalScene;
        private List<ChoiceResponse> choices;

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

        public Builder isTerminalScene(Boolean isTerminalScene) {
            this.isTerminalScene = isTerminalScene;
            return this;
        }

        public Builder choices(List<ChoiceResponse> choices) {
            this.choices = choices;
            return this;
        }

        public SceneResponse build() {
            return new SceneResponse(this.id, this.title, this.description, this.isVictory, this.isGameOver, this.isTerminalScene, this.choices);
        }
    }
}
