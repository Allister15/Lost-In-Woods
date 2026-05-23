package com.LostInWoods.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Response DTO for a game scene
 * Includes all choices available in the scene
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SceneResponse {

    private Long id;
    private String title;
    private String description;
    private boolean isVictory;
    private boolean isGameOver;
    private boolean isTerminalScene;
    private List<ChoiceResponse> choices;
}
