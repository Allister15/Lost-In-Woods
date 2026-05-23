package com.LostInWoods.app.service;

import com.LostInWoods.app.dto.ChoiceResponse;
import com.LostInWoods.app.dto.SceneResponse;
import com.LostInWoods.app.entity.Choice;
import com.LostInWoods.app.entity.Scene;
import com.LostInWoods.app.exception.ResourceNotFoundException;
import com.LostInWoods.app.repository.ChoiceRepository;
import com.LostInWoods.app.repository.SceneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SceneService
 * Tests scene retrieval and choice management
 */
@ExtendWith(MockitoExtension.class)
class SceneServiceTest {

    @Mock
    private SceneRepository sceneRepository;

    @Mock
    private ChoiceRepository choiceRepository;

    @InjectMocks
    private SceneService sceneService;

    private Scene testScene;
    private List<Choice> testChoices;

    @BeforeEach
    void setUp() {
        testScene = Scene.builder()
                .id(1L)
                .title("Dark Forest")
                .description("You wake up in a dark forest...")
                .isVictory(false)
                .isGameOver(false)
                .choices(new ArrayList<>())
                .build();

        testChoices = new ArrayList<>();
        Choice choice1 = Choice.builder()
                .id(1L)
                .choiceText("Go left")
                .outcomeDescription("You go left")
                .healthChange(-10)
                .nextSceneId(2L)
                .displayOrder(0)
                .scene(testScene)
                .build();

        Choice choice2 = Choice.builder()
                .id(2L)
                .choiceText("Go right")
                .outcomeDescription("You go right")
                .healthChange(0)
                .nextSceneId(3L)
                .displayOrder(1)
                .scene(testScene)
                .build();

        testChoices.add(choice1);
        testChoices.add(choice2);
        testScene.setChoices(testChoices);
    }

    @Test
    void testGetSceneById_Success() {
        // Arrange
        when(sceneRepository.findById(1L)).thenReturn(Optional.of(testScene));

        // Act
        SceneResponse response = sceneService.getSceneById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Dark Forest", response.getTitle());
        assertFalse(response.isVictory());
        assertFalse(response.isGameOver());
    }

    @Test
    void testGetSceneById_NotFound() {
        // Arrange
        when(sceneRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> sceneService.getSceneById(999L));
    }

    @Test
    void testGetChoicesBySceneId_Success() {
        // Arrange
        when(sceneRepository.findById(1L)).thenReturn(Optional.of(testScene));
        when(choiceRepository.findBySceneIdOrderByDisplayOrder(1L)).thenReturn(testChoices);

        // Act
        List<ChoiceResponse> responses = sceneService.getChoicesBySceneId(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Go left", responses.get(0).getChoiceText());
        assertEquals("Go right", responses.get(1).getChoiceText());
    }

    @Test
    void testIsTerminalScene_VictoryScene() {
        // Arrange
        testScene.setIsVictory(true);

        // Act & Assert
        assertTrue(testScene.isTerminalScene());
    }

    @Test
    void testIsTerminalScene_GameOverScene() {
        // Arrange
        testScene.setIsGameOver(true);

        // Act & Assert
        assertTrue(testScene.isTerminalScene());
    }

    @Test
    void testIsTerminalScene_NormalScene() {
        // Act & Assert
        assertFalse(testScene.isTerminalScene());
    }

    @Test
    void testChoiceHealthChangeValidation() {
        // Arrange
        Choice choice = testChoices.get(0);

        // Act & Assert
        assertTrue(choice.isValidHealthChange());
    }

    @Test
    void testChoiceHealthChangeInvalid() {
        // Arrange
        Choice invalidChoice = Choice.builder()
                .id(3L)
                .choiceText("Invalid")
                .outcomeDescription("Invalid")
                .healthChange(150) // Out of range
                .nextSceneId(4L)
                .displayOrder(0)
                .build();

        // Act & Assert
        assertFalse(invalidChoice.isValidHealthChange());
    }
}
