package com.DinoCorp.Lost_In_Woods.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private int currentHealth = 80;
    @Builder.Default
    private int currentScore = 0;
    @Builder.Default
    private int currentSceneIndex = 0;
    @Builder.Default
    private boolean isGameOver = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_traits", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "trait")
    @Builder.Default
    private List<String> discoveredTraits = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_history", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "story_beat", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> narrativeHistory = new ArrayList<>();

    public void applyOutcome(Choice choice) {
        this.currentHealth = Math.max(0, Math.min(100, this.currentHealth + choice.getHpModifier()));
        this.currentScore += choice.getScoreModifier();

        if (choice.getConferredTrait() != null && !choice.getConferredTrait().isBlank()) {
            if (!this.discoveredTraits.contains(choice.getConferredTrait())) {
                this.discoveredTraits.add(choice.getConferredTrait());
            }
        }

        String cleanChapter = "Chapter " + (this.currentSceneIndex + 1);
        String beatLog = String.format("%s|%b|%s", cleanChapter, choice.isGoodOutcome(), choice.getNarrative());
        this.narrativeHistory.add(beatLog);

        if (this.currentHealth <= 0) {
            this.isGameOver = true;
        }
    }
}
