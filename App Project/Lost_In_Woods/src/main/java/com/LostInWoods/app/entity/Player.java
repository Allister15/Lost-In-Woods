package com.LostInWoods.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Player Entity
 * Represents a player profile in the Lost-in-Woods game.
 * Single Responsibility: Encapsulates player data and persistence.
 */
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Player name cannot be blank")
    @Size(min = 1, max = 100, message = "Player name must be between 1 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Age cannot be null")
    @Min(value = 13, message = "Age must be at least 13")
    @Max(value = 120, message = "Age must be at most 120")
    @Column(nullable = false)
    private Integer age;

    @NotBlank(message = "Gender cannot be blank")
    @Column(nullable = false)
    private String gender;

    @NotNull(message = "Current health cannot be null")
    @Min(value = 0, message = "Health cannot be negative")
    @Max(value = 100, message = "Health cannot exceed 100")
    @Column(nullable = false)
    private Integer currentHealth;

    @NotNull(message = "Current scene ID cannot be null")
    @Column(nullable = false)
    private Long currentSceneId;

    @Column(nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(nullable = false)
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
        if (currentHealth == null) {
            currentHealth = 100;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
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

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business logic methods
    public boolean isAlive() {
        return currentHealth > 0;
    }

    public void takeDamage(int damage) {
        this.currentHealth = Math.max(0, this.currentHealth - damage);
    }

    public void heal(int healAmount) {
        this.currentHealth = Math.min(100, this.currentHealth + healAmount);
    }
}
