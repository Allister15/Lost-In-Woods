package com.LostInWoods.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for player creation
 * Follows Data Transfer Object pattern to decouple API from entities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerCreateRequest {

    @NotBlank(message = "Player name is required")
    private String name;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotBlank(message = "Gender is required")
    private String gender;
}
