package com.DinoCorp.Lost_In_Woods.ai.dto;

// One achievement as sent to the frontend: metadata + whether this player has unlocked it.
// Used both for the modal list (all 24) and the per-beat "newly unlocked" toast payload.
public record AchievementView(
        String id,
        String section,        // enum section name (SCORE / SURVIVOR / SINS / INVENTORY / FATE)
        String sectionLabel,   // human label, e.g. "Score Milestones"
        String title,
        String description,
        boolean unlocked
) {}
