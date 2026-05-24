package com.LostInWoods.app.controller;

import com.LostInWoods.app.dto.ChoiceResponse;
import com.LostInWoods.app.dto.SceneResponse;
import com.LostInWoods.app.service.SceneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for scene-related endpoints
 * Single Responsibility: Handle HTTP requests for scene operations
 */
@RestController
@RequestMapping("/api/scenes")

@CrossOrigin(origins = "*", maxAge = 3600)
public class SceneController {

    private final SceneService sceneService;

    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    /**
     * Get scene by ID with all choices
     * GET /api/scenes/{sceneId}
     */
    @GetMapping("/{sceneId}")
    public ResponseEntity<SceneResponse> getScene(@PathVariable Long sceneId) {
        SceneResponse response = sceneService.getSceneById(sceneId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all choices for a scene
     * GET /api/scenes/{sceneId}/choices
     */
    @GetMapping("/{sceneId}/choices")
    public ResponseEntity<List<ChoiceResponse>> getChoices(@PathVariable Long sceneId) {
        List<ChoiceResponse> choices = sceneService.getChoicesBySceneId(sceneId);
        return ResponseEntity.ok(choices);
    }
}
