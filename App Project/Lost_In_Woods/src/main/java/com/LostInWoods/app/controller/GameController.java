package com.LostInWoods.app.controller;

import com.LostInWoods.app.dto.ChoiceActionRequest;
import com.LostInWoods.app.dto.ChoiceOutcomeResponse;
import com.LostInWoods.app.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for core game logic endpoints
 * Single Responsibility: Handle HTTP requests for game operations
 */
@RestController
@RequestMapping("/api/game")

@CrossOrigin(origins = "*", maxAge = 3600)
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Make a choice in the game
     * POST /api/game/choice
     */
    @PostMapping("/choice")
    public ResponseEntity<ChoiceOutcomeResponse> makeChoice(@Valid @RequestBody ChoiceActionRequest request) {
        ChoiceOutcomeResponse response = gameService.processChoice(request.getPlayerId(), request.getChoiceId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get current game state
     * GET /api/game/state/{playerId}
     */
    @GetMapping("/state/{playerId}")
    public ResponseEntity<GameService.GameStateResponse> getGameState(@PathVariable Long playerId) {
        GameService.GameStateResponse response = gameService.getGameState(playerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Load saved game progress
     * POST /api/game/load/{playerId}
     */
    @PostMapping("/load/{playerId}")
    public ResponseEntity<GameService.GameStateResponse> loadGame(@PathVariable Long playerId) {
        gameService.loadGameProgress(playerId);
        GameService.GameStateResponse response = gameService.getGameState(playerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Save game progress
     * POST /api/game/save/{playerId}
     */
    @PostMapping("/save/{playerId}")
    public ResponseEntity<Void> saveGame(@PathVariable Long playerId) {
        // Game progress is automatically saved when choices are made
        // This endpoint is for manual saves or explicit save requests
        GameService.GameStateResponse state = gameService.getGameState(playerId);
        gameService.saveGameProgress(playerId, state.getCurrentScene().getId(), state.getPlayer().getCurrentHealth());
        return ResponseEntity.ok().build();
    }
}
