package com.DinoCorp.Lost_In_Woods.controller;

import com.DinoCorp.Lost_In_Woods.dto.*;
import com.DinoCorp.Lost_In_Woods.model.*;
import com.DinoCorp.Lost_In_Woods.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public ResponseEntity<GameResponse> startSession() {
        GameSession entity = gameService.createSession();
        return ResponseEntity.ok(gameService.mapToResponse(entity));
    }

    @GetMapping("/scenario/{sessionId}")
    public ResponseEntity<Scenario> fetchScenario(@PathVariable Long sessionId) {
        return ResponseEntity.ok(gameService.getScenarioForSession(sessionId));
    }

    @PostMapping("/choose")
    public ResponseEntity<GameResponse> postChoice(@RequestBody ChoiceRequest req) {
        GameSession updated = gameService.executeChoice(req.getSessionId(), req.getChoiceIndex());
        return ResponseEntity.ok(gameService.mapToResponse(updated));
    }

    @GetMapping("/illustration")
    public ResponseEntity<Map<String, String>> fetchIllustration(@RequestParam String prompt) {
        String svgOutput = gameService.fetchDynamicSvg(prompt);
        return ResponseEntity.ok(Map.of("svg", svgOutput != null ? svgOutput : "FALLBACK"));
    }
}