package com.LostInWoods.app.controller;

import com.LostInWoods.app.dto.PlayerCreateRequest;
import com.LostInWoods.app.dto.PlayerResponse;
import com.LostInWoods.app.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for player-related endpoints
 * Single Responsibility: Handle HTTP requests for player operations
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PlayerController {

    private final PlayerService playerService;

    /**
     * Create a new player
     * POST /api/players
     */
    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody PlayerCreateRequest request) {
        PlayerResponse response = playerService.createPlayer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get player by ID
     * GET /api/players/{playerId}
     */
    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable Long playerId) {
        PlayerResponse response = playerService.getPlayerById(playerId);
        return ResponseEntity.ok(response);
    }
}
