package com.LostInWoods.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO after a player makes a choice
 * Includes the outcome and updated player state
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceOutcomeResponse {

    private String outcomeDescription;
    private Integer healthChangedBy;
    private Integer newHealth;
    private Long nextSceneId;
    private boolean playerAlive;
    private PlayerResponse updatedPlayer;
}
