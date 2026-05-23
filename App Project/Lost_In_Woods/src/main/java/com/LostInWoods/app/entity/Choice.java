package com.LostInWoods.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Choice Entity
 * Represents a player choice within a scene that leads to outcomes and other scenes.
 * Single Responsibility: Encapsulates choice data and choice-to-consequence mapping.
 */
@Entity
@Table(name = "choices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Choice text cannot be blank")
    @Column(nullable = false)
    private String choiceText;

    @NotBlank(message = "Outcome description cannot be blank")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String outcomeDescription;

    /**
     * Health change when this choice is selected
     * Negative values = damage, Positive values = healing
     */
    @NotNull(message = "Health change cannot be null")
    @Column(nullable = false)
    private Integer healthChange;

    /**
     * The next scene ID when this choice is selected
     */
    @NotNull(message = "Next scene ID cannot be null")
    @Column(nullable = false)
    private Long nextSceneId;

    /**
     * The scene this choice belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private Scene scene;

    /**
     * Order of choices within a scene (for proper display)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Validates that health change is within reasonable bounds
     */
    public boolean isValidHealthChange() {
        return healthChange >= -100 && healthChange <= 100;
    }
}
