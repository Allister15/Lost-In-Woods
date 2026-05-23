package com.LostInWoods.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Player Entity
 * Represents a player profile in the Lost-in-Woods game.
 * Single Responsibility: Encapsulates player data and persistence.
 */
@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currentHealth == null) {
            currentHealth = 100; // Initial health
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the player is alive (health > 0)
     */
    public boolean isAlive() {
        return currentHealth > 0;
    }

    /**
     * Applies damage to the player
     */
    public void takeDamage(int damage) {
        this.currentHealth = Math.max(0, this.currentHealth - damage);
    }

    /**
     * Heals the player
     */
    public void heal(int healAmount) {
        this.currentHealth = Math.min(100, this.currentHealth + healAmount);
    }
}
