package com.LostInWoods.app.service;

import com.LostInWoods.app.dto.ChoiceOutcomeResponse;
import com.LostInWoods.app.dto.PlayerResponse;
import com.LostInWoods.app.entity.Choice;
import com.LostInWoods.app.entity.GameProgress;
import com.LostInWoods.app.entity.Player;
import com.LostInWoods.app.entity.Scene;
import com.LostInWoods.app.exception.InvalidGameStateException;
import com.LostInWoods.app.exception.ResourceNotFoundException;
import com.LostInWoods.app.repository.GameProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for core game logic and orchestration
 * Single Responsibility: Handles game mechanics and state transitions
 * Uses Strategy pattern through service delegation
 */
@Service
@Transactional
@RequiredArgsConstructor
public class GameService {

    private final PlayerService playerService;
    private final SceneService sceneService;
    private final GameProgressRepository gameProgressRepository;

    /**
     * Process a player's choice and update game state
     * Orchestrates: apply health change, move to next scene, save progress
     */
    public ChoiceOutcomeResponse processChoice(Long playerId, Long choiceId) {
        // Validate game state
        Player player = playerService.getPlayerEntityById(playerId);
        if (!player.isAlive()) {
            throw new InvalidGameStateException("Cannot make a choice; player is dead");
        }

        // Get the choice
        Choice choice = sceneService.getChoiceEntityById(choiceId);
        
        // Validate choice belongs to current player scene
        if (!choice.getScene().getId().equals(player.getCurrentSceneId())) {
            throw new InvalidGameStateException(
                    "Choice is not available in the current scene"
            );
        }

        // Apply health change
        int oldHealth = player.getCurrentHealth();
        playerService.updatePlayerHealth(playerId, choice.getHealthChange());

        // Refresh player to get updated health
        player = playerService.getPlayerEntityById(playerId);

        // Move to next scene
        playerService.updatePlayerScene(playerId, choice.getNextSceneId());

        // Save game progress
        saveGameProgress(playerId, choice.getNextSceneId(), player.getCurrentHealth());

        // Refresh player data
        PlayerResponse updatedPlayer = playerService.getPlayerById(playerId);

        // Build response
        return ChoiceOutcomeResponse.builder()
                .outcomeDescription(choice.getOutcomeDescription())
                .healthChangedBy(choice.getHealthChange())
                .newHealth(player.getCurrentHealth())
                .nextSceneId(choice.getNextSceneId())
                .playerAlive(player.isAlive())
                .updatedPlayer(updatedPlayer)
                .build();
    }

    /**
     * Load the last saved game progress for a player
     */
    public void loadGameProgress(Long playerId) {
        GameProgress progress = gameProgressRepository.findFirstByPlayerIdOrderByLastSavedAtDesc(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No saved game found for player %d", playerId)
                ));

        Player player = playerService.getPlayerEntityById(playerId);
        player.setCurrentSceneId(progress.getCurrentSceneId());
        player.setCurrentHealth(progress.getCurrentHealth());
        playerService.updatePlayerHealth(playerId, 0); // Force save
    }

    /**
     * Save current game progress
     */
    public void saveGameProgress(Long playerId, Long currentSceneId, Integer currentHealth) {
        GameProgress progress = GameProgress.builder()
                .playerId(playerId)
                .currentSceneId(currentSceneId)
                .currentHealth(currentHealth)
                .build();

        gameProgressRepository.save(progress);
    }

    /**
     * Get current game state summary
     */
    public GameStateResponse getGameState(Long playerId) {
        Player player = playerService.getPlayerEntityById(playerId);
        Scene currentScene = sceneService.getSceneEntityById(player.getCurrentSceneId());

        return GameStateResponse.builder()
                .player(playerService.getPlayerById(playerId))
                .currentScene(sceneService.getSceneById(player.getCurrentSceneId()))
                .isGameOver(!player.isAlive() || currentScene.getIsGameOver())
                .isVictory(currentScene.getIsVictory())
                .build();
    }

    /**
     * Response DTO for game state
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class GameStateResponse {
        private PlayerResponse player;
        private com.LostInWoods.app.dto.SceneResponse currentScene;
        private boolean isGameOver;
        private boolean isVictory;
    }
}
