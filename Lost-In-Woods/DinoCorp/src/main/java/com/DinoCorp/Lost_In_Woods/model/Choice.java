package com.DinoCorp.Lost_In_Woods.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false, length = 255)
    private String meta;

    // Flattening outcome directly inside choice to avoid unnecessary database tables (KISS)
    private int hpModifier;
    private int scoreModifier;
    private String conferredTrait;
    private boolean isGoodOutcome;

    @Column(columnDefinition = "TEXT")
    private String systemLog;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String narrative;
}