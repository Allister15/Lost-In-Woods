package com.DinoCorp.Lost_In_Woods.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scenarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario {
    @Id
    private Long id; // Explicit indices matching game sequence (0-6)

    @Column(length = 100)
    private String chapter;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String sceneDescription;

    private String hint;

    // Entity avatar metadata
    private String entityAvatar;
    private String entityName;
    private String entityQuote;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String svgPrompt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "scenario_id")
    @Builder.Default
    private List<Choice> choices = new ArrayList<>();
}