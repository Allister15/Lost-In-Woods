package com.LostInWoods.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a player choice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceResponse {

    private Long id;
    private String choiceText;
    private String outcomeDescription;
    private Integer healthChange;
    private Long nextSceneId;
    private Integer displayOrder;
}
