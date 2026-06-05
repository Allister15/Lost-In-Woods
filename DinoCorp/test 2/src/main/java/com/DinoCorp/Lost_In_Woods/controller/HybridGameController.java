package com.DinoCorp.Lost_In_Woods.controller;

import com.DinoCorp.Lost_In_Woods.ai.dto.StoryBeatPayload;
import com.DinoCorp.Lost_In_Woods.service.GameStateService;
import com.DinoCorp.Lost_In_Woods.service.GameStateService.GameStartResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

// Thin HTTP surface for the prefilled Hybrid Story Engine. Lives under its own path so
// it sits alongside (not on top of) the existing live-AI story endpoints.
@RestController
@RequestMapping("/api/game/hybrid")
@CrossOrigin(origins = "*")
public class HybridGameController {

    private final GameStateService engine;

    public HybridGameController(GameStateService engine) {
        this.engine = engine;
    }

    public record StartRequest(String playerName) {}

    // Character-select / initial load: build only chapter 1 (fast), launch the rolling
    // async pipeline, and return the opening beat + the new session id.
    @PostMapping("/start")
    public ResponseEntity<GameStartResult> start(@RequestBody(required = false) StartRequest body) {
        String playerName = (body == null) ? null : body.playerName();
        return ResponseEntity.ok(engine.initializeGameSession(playerName));
    }

    // A player's turn: funnel forward exactly one node and return the next beat instantly.
    // `option` is the clicked choice index (0..3) — recorded for ending karma only.
    @PostMapping("/{sessionId}/choose")
    public ResponseEntity<StoryBeatPayload> choose(@PathVariable String sessionId,
                                                   @RequestParam(name = "option", defaultValue = "0") int option) {
        return ResponseEntity.ok(engine.processPlayerTurn(parseSessionId(sessionId), option));
    }

    private UUID parseSessionId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed session id: " + raw);
        }
    }
}
