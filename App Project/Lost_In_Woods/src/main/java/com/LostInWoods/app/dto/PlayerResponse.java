package com.LostInWoods.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for player information
 * Exposes only necessary player data to the client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerResponse {

    private Long id;
    private String name;
    private Integer age;
    private String gender;
    private Integer currentHealth;
    private Long currentSceneId;
    private boolean isAlive;
}
