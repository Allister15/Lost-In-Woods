package com.LostInWoods.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Scene Entity
 * Represents a story scene in the game with title, description, and available choices.
 * Single Responsibility: Encapsulates scene data and game narrative structure.
 */
@Entity
@Table(name = "scenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Scene title cannot be blank")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Scene description cannot be blank")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Indicates if this is a victory scene (game ending)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isVictory = false;

    /**
     * Indicates if this is a game over scene (defeat)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isGameOver = false;

    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Choice> choices = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Checks if this scene is a terminal scene (victory or game over)
     */
    public boolean isTerminalScene() {
        return isVictory || isGameOver;
    }
}
