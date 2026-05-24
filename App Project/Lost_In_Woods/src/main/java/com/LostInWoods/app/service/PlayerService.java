package com.LostInWoods.app.service;

import com.LostInWoods.app.dto.PlayerCreateRequest;
import com.LostInWoods.app.dto.PlayerResponse;
import com.LostInWoods.app.entity.Player;
import com.LostInWoods.app.exception.ResourceNotFoundException;
import com.LostInWoods.app.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for player-related operations
 * Single Responsibility: Handles player business logic
 * Uses Dependency Injection to decouple from repositories
 */
@Service
@Transactional

public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Creates a new player
     */
    public PlayerResponse createPlayer(PlayerCreateRequest request) {
        Player newPlayer = Player.builder()
                .name(request.getName())
                .age(request.getAge())
                .gender(request.getGender())
                .currentHealth(100) // Initial health
                .currentSceneId(1L) // Assumes scene with ID 1 is the starting scene
                .build();

        Player savedPlayer = playerRepository.save(newPlayer);
        return convertToResponse(savedPlayer);
    }

    /**
     * Gets a player by ID
     */
    public PlayerResponse getPlayerById(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Player with ID %d not found", playerId)
                ));
        return convertToResponse(player);
    }

    /**
     * Gets a player entity by ID (internal use)
     */
    public Player getPlayerEntityById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Player with ID %d not found", playerId)
                ));
    }

    /**
     * Updates player's current scene
     */
    public void updatePlayerScene(Long playerId, Long sceneId) {
        Player player = getPlayerEntityById(playerId);
        player.setCurrentSceneId(sceneId);
        playerRepository.save(player);
    }

    /**
     * Updates player's health
     */
    public void updatePlayerHealth(Long playerId, int healthChange) {
        Player player = getPlayerEntityById(playerId);
        if (healthChange < 0) {
            player.takeDamage(Math.abs(healthChange));
        } else if (healthChange > 0) {
            player.heal(healthChange);
        }
        playerRepository.save(player);
    }

    /**
     * Converts Player entity to PlayerResponse DTO
     */
    private PlayerResponse convertToResponse(Player player) {
        return PlayerResponse.builder()
                .id(player.getId())
                .name(player.getName())
                .age(player.getAge())
                .gender(player.getGender())
                .currentHealth(player.getCurrentHealth())
                .currentSceneId(player.getCurrentSceneId())
                .isAlive(player.isAlive())
                .build();
    }
}
