package com.LostInWoods.app.dto;

import jakarta.validation.constraints.*;

/**
 * PlayerCreateRequest DTO
 * Represents a request to create a new player.
 * Single Responsibility: Input validation and data transfer for player creation.
 */
public class PlayerCreateRequest {

    @NotBlank(message = "Player name cannot be blank")
    @Size(min = 1, max = 100, message = "Player name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Age cannot be null")
    @Min(value = 13, message = "Age must be at least 13")
    @Max(value = 120, message = "Age must be at most 120")
    private Integer age;

    @NotBlank(message = "Gender cannot be blank")
    private String gender;

    // Constructors
    public PlayerCreateRequest() {}

    public PlayerCreateRequest(String name, Integer age, String gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Integer age;
        private String gender;

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

        public PlayerCreateRequest build() {
            return new PlayerCreateRequest(this.name, this.age, this.gender);
        }
    }
}
