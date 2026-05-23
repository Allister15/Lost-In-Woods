package com.LostInWoods.app.service;

import com.LostInWoods.app.dto.ChoiceResponse;
import com.LostInWoods.app.dto.SceneResponse;
import com.LostInWoods.app.entity.Choice;
import com.LostInWoods.app.entity.Scene;
import com.LostInWoods.app.exception.ResourceNotFoundException;
import com.LostInWoods.app.repository.ChoiceRepository;
import com.LostInWoods.app.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for scene-related operations
 * Single Responsibility: Handles scene and narrative business logic
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SceneService {

    private final SceneRepository sceneRepository;
    private final ChoiceRepository choiceRepository;

    /**
     * Gets a scene by ID with all its choices
     */
    public SceneResponse getSceneById(Long sceneId) {
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Scene with ID %d not found", sceneId)
                ));
        return convertToResponse(scene);
    }

    /**
     * Gets a scene entity by ID (internal use)
     */
    public Scene getSceneEntityById(Long sceneId) {
        return sceneRepository.findById(sceneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Scene with ID %d not found", sceneId)
                ));
    }

    /**
     * Gets all available choices for a scene
     */
    public List<ChoiceResponse> getChoicesBySceneId(Long sceneId) {
        // Verify scene exists
        getSceneEntityById(sceneId);
        
        return choiceRepository.findBySceneIdOrderByDisplayOrder(sceneId)
                .stream()
                .map(this::convertChoiceToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets a specific choice by ID
     */
    public Choice getChoiceEntityById(Long choiceId) {
        return choiceRepository.findById(choiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Choice with ID %d not found", choiceId)
                ));
    }

    /**
     * Converts Scene entity to SceneResponse DTO
     */
    private SceneResponse convertToResponse(Scene scene) {
        List<ChoiceResponse> choices = scene.getChoices().stream()
                .map(this::convertChoiceToResponse)
                .collect(Collectors.toList());

        return SceneResponse.builder()
                .id(scene.getId())
                .title(scene.getTitle())
                .description(scene.getDescription())
                .isVictory(scene.getIsVictory())
                .isGameOver(scene.getIsGameOver())
                .isTerminalScene(scene.isTerminalScene())
                .choices(choices)
                .build();
    }

    /**
     * Converts Choice entity to ChoiceResponse DTO
     */
    private ChoiceResponse convertChoiceToResponse(Choice choice) {
        return ChoiceResponse.builder()
                .id(choice.getId())
                .choiceText(choice.getChoiceText())
                .outcomeDescription(choice.getOutcomeDescription())
                .healthChange(choice.getHealthChange())
                .nextSceneId(choice.getNextSceneId())
                .displayOrder(choice.getDisplayOrder())
                .build();
    }
}
