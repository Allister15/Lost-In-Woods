package com.LostInWoods.app.service;

import com.LostInWoods.app.dto.ChoiceOutcomeResponse;
import com.LostInWoods.app.entity.Choice;
import com.LostInWoods.app.entity.Player;
import com.LostInWoods.app.entity.Scene;
import com.LostInWoods.app.exception.InvalidGameStateException;
import com.LostInWoods.app.repository.GameProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GameService
 * Tests core game logic and state transitions
 */
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private SceneService sceneService;

    @Mock
    private GameProgressRepository gameProgressRepository;

    @InjectMocks
    private GameService gameService;

    private Player testPlayer;
    private Scene testScene;
    private Choice testChoice;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
                .id(1L)
                .name("Test Player")
                .age(25)
                .gender("Male")
                .currentHealth(100)
                .currentSceneId(1L)
                .build();

        testScene = Scene.builder()
                .id(1L)
                .title("Dark Forest")
                .description("You are in a dark forest")
                .isVictory(false)
                .isGameOver(false)
                .choices(new ArrayList<>())
                .build();

        testChoice = Choice.builder()
                .id(1L)
                .choiceText("Go left")
                .outcomeDescription("You went left and encountered a bear")
                .healthChange(-20)
                .nextSceneId(2L)
                .displayOrder(0)
                .scene(testScene)
                .build();

        testScene.setChoices(new ArrayList<>());
        testScene.getChoices().add(testChoice);
    }

    @Test
    void testProcessChoice_Success() {
        // Arrange
        when(playerService.getPlayerEntityById(1L)).thenReturn(testPlayer);
        when(sceneService.getChoiceEntityById(1L)).thenReturn(testChoice);
        when(playerService.getPlayerById(1L)).thenReturn(
                com.LostInWoods.app.dto.PlayerResponse.builder()
                        .id(1L)
                        .name("Test Player")
                        .currentHealth(80)
                        .isAlive(true)
                        .build()
        );

        // Act
        ChoiceOutcomeResponse response = gameService.processChoice(1L, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("You went left and encountered a bear", response.getOutcomeDescription());
        assertEquals(-20, response.getHealthChangedBy());
        assertEquals(2L, response.getNextSceneId());
        assertTrue(response.isPlayerAlive());

        verify(playerService, times(2)).getPlayerEntityById(1L);
        verify(playerService, times(1)).updatePlayerHealth(1L, -20);
        verify(playerService, times(1)).updatePlayerScene(1L, 2L);
        verify(gameProgressRepository, times(1)).save(any());
    }

    @Test
    void testProcessChoice_PlayerDead() {
        // Arrange
        testPlayer.setCurrentHealth(0);
        when(playerService.getPlayerEntityById(1L)).thenReturn(testPlayer);

        // Act & Assert
        assertThrows(InvalidGameStateException.class, () -> gameService.processChoice(1L, 1L));
    }

    @Test
    void testProcessChoice_InvalidScene() {
        // Arrange
        Scene differentScene = Scene.builder()
                .id(2L)
                .title("Forest Cave")
                .description("A cave in the forest")
                .build();

        testChoice.setScene(differentScene);

        when(playerService.getPlayerEntityById(1L)).thenReturn(testPlayer);
        when(sceneService.getChoiceEntityById(1L)).thenReturn(testChoice);

        // Act & Assert
        assertThrows(InvalidGameStateException.class, () -> gameService.processChoice(1L, 1L));
    }

    @Test
    void testGetGameState() {
        // Arrange
        when(playerService.getPlayerEntityById(1L)).thenReturn(testPlayer);
        when(sceneService.getSceneEntityById(1L)).thenReturn(testScene);
        when(playerService.getPlayerById(1L)).thenReturn(
                com.LostInWoods.app.dto.PlayerResponse.builder()
                        .id(1L)
                        .name("Test Player")
                        .currentHealth(100)
                        .isAlive(true)
                        .build()
        );
        when(sceneService.getSceneById(1L)).thenReturn(
                com.LostInWoods.app.dto.SceneResponse.builder()
                        .id(1L)
                        .title("Dark Forest")
                        .isVictory(false)
                        .isGameOver(false)
                        .isTerminalScene(false)
                        .build()
        );

        // Act
        GameService.GameStateResponse response = gameService.getGameState(1L);

        // Assert
        assertNotNull(response);
        assertFalse(response.isGameOver());
        assertFalse(response.isVictory());
        assertEquals(1L, response.getPlayer().getId());
    }

    @Test
    void testGetGameState_PlayerDead() {
        // Arrange
        testPlayer.setCurrentHealth(0);
        when(playerService.getPlayerEntityById(1L)).thenReturn(testPlayer);
        when(sceneService.getSceneEntityById(1L)).thenReturn(testScene);
        when(playerService.getPlayerById(1L)).thenReturn(
                com.LostInWoods.app.dto.PlayerResponse.builder()
                        .id(1L)
                        .name("Test Player")
                        .currentHealth(0)
                        .isAlive(false)
                        .build()
        );
        when(sceneService.getSceneById(1L)).thenReturn(
                com.LostInWoods.app.dto.SceneResponse.builder()
                        .id(1L)
                        .title("Dark Forest")
                        .isGameOver(false)
                        .build()
        );

        // Act
        GameService.GameStateResponse response = gameService.getGameState(1L);

        // Assert
        assertTrue(response.isGameOver());
    }

    @Test
    void testHealthBoundaryConditions() {
        // Test health cannot exceed 100
        testPlayer.heal(150);
        assertEquals(100, testPlayer.getCurrentHealth());

        // Test health cannot go below 0
        testPlayer.takeDamage(150);
        assertEquals(0, testPlayer.getCurrentHealth());
    }
}
