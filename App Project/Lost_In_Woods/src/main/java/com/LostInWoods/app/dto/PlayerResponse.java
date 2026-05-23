package com.LostInWoods.app.dto;

import jakarta.validation.constraints.*;

/**
 * PlayerResponse DTO
 * Represents player data to be returned in API responses.
 * Single Responsibility: Safe player data exposure without exposing sensitive details.
 */
public class PlayerResponse {

    @NotNull(message = "Player ID cannot be null")
    private Long id;

    @NotBlank(message = "Player name cannot be blank")
    private String name;

    @NotNull(message = "Age cannot be null")
    private Integer age;

    @NotBlank(message = "Gender cannot be blank")
    private String gender;

    @NotNull(message = "Current health cannot be null")
    @Min(value = 0, message = "Health cannot be negative")
    @Max(value = 100, message = "Health cannot exceed 100")
    private Integer currentHealth;

    @NotNull(message = "Current scene ID cannot be null")
    private Long currentSceneId;

    @NotNull(message = "Alive flag cannot be null")
    private Boolean isAlive;

    // Constructors
    public PlayerResponse() {}

    public PlayerResponse(Long id, String name, Integer age, String gender, Integer currentHealth, Long currentSceneId, Boolean isAlive) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.currentHealth = currentHealth;
        this.currentSceneId = currentSceneId;
        this.isAlive = isAlive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(Integer currentHealth) { this.currentHealth = currentHealth; }

    public Long getCurrentSceneId() { return currentSceneId; }
    public void setCurrentSceneId(Long currentSceneId) { this.currentSceneId = currentSceneId; }

    public Boolean getIsAlive() { return isAlive; }
    public void setIsAlive(Boolean isAlive) { this.isAlive = isAlive; }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private Integer age;
        private String gender;
        private Integer currentHealth;
        private Long currentSceneId;
        private Boolean isAlive;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder age(Integer age) {
            this.age = age;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder currentHealth(Integer currentHealth) {
            this.currentHealth = currentHealth;
            return this;
        }

        public Builder currentSceneId(Long currentSceneId) {
            this.currentSceneId = currentSceneId;
            return this;
        }

        public Builder isAlive(Boolean isAlive) {
            this.isAlive = isAlive;
            return this;
        }

        public PlayerResponse build() {
            return new PlayerResponse(this.id, this.name, this.age, this.gender, this.currentHealth, this.currentSceneId, this.isAlive);
        }
    }
}
