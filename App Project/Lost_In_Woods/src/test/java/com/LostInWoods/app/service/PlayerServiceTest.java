package com.LostInWoods.app.service;

import com.LostInWoods.app.dto.PlayerCreateRequest;
import com.LostInWoods.app.dto.PlayerResponse;
import com.LostInWoods.app.entity.Player;
import com.LostInWoods.app.exception.ResourceNotFoundException;
import com.LostInWoods.app.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlayerService
 * Tests business logic and exception handling
 */
@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player testPlayer;
    private PlayerCreateRequest createRequest;

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

        createRequest = PlayerCreateRequest.builder()
                .name("New Player")
                .age(30)
                .gender("Female")
                .build();
    }

    @Test
    void testCreatePlayer_Success() {
        // Arrange
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // Act
        PlayerResponse response = playerService.createPlayer(createRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Player", response.getName());
        assertEquals(100, response.getCurrentHealth());
        assertTrue(response.isAlive());
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void testGetPlayerById_Success() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));

        // Act
        PlayerResponse response = playerService.getPlayerById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Player", response.getName());
    }

    @Test
    void testGetPlayerById_PlayerNotFound() {
        // Arrange
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> playerService.getPlayerById(999L));
    }

    @Test
    void testUpdatePlayerHealth_TakeDamage() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // Act
        playerService.updatePlayerHealth(1L, -30);

        // Assert
        assertEquals(70, testPlayer.getCurrentHealth());
    }

    @Test
    void testUpdatePlayerHealth_Heal() {
        // Arrange
        testPlayer.setCurrentHealth(50);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // Act
        playerService.updatePlayerHealth(1L, 30);

        // Assert
        assertEquals(80, testPlayer.getCurrentHealth());
    }

    @Test
    void testUpdatePlayerScene() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // Act
        playerService.updatePlayerScene(1L, 2L);

        // Assert
        assertEquals(2L, testPlayer.getCurrentSceneId());
    }

    @Test
    void testPlayerIsAlive() {
        // Arrange
        testPlayer.setCurrentHealth(1);

        // Act & Assert
        assertTrue(testPlayer.isAlive());
    }

    @Test
    void testPlayerIsDead() {
        // Arrange
        testPlayer.setCurrentHealth(0);

        // Act & Assert
        assertFalse(testPlayer.isAlive());
    }

    @Test
    void testHealthCannotExceed100() {
        // Arrange
        testPlayer.setCurrentHealth(90);

        // Act
        testPlayer.heal(50);

        // Assert
        assertEquals(100, testPlayer.getCurrentHealth());
    }

    @Test
    void testHealthCannotBeLessThan0() {
        // Arrange
        testPlayer.setCurrentHealth(10);

        // Act
        testPlayer.takeDamage(50);

        // Assert
        assertEquals(0, testPlayer.getCurrentHealth());
    }
}
