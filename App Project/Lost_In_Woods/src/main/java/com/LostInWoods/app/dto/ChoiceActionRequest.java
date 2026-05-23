package com.LostInWoods.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for making a choice in the game
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceActionRequest {

    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotNull(message = "Choice ID is required")
    private Long choiceId;
}
