package com.DinoCorp.Lost_In_Woods.service;

import com.DinoCorp.Lost_In_Woods.dto.GameResponse;
import com.DinoCorp.Lost_In_Woods.exception.ResourceNotFoundException;
import com.DinoCorp.Lost_In_Woods.model.*;
import com.DinoCorp.Lost_In_Woods.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class GameService {

    private final GameSessionRepository sessionRepository;
    private final ScenarioRepository scenarioRepository;
    private final RestTemplate restTemplate;

    @Value("${anthropic.api.key:MOCK_KEY}")
    private String apiKey;

    public GameService(GameSessionRepository sessionRepo, ScenarioRepository scenarioRepo, RestTemplate rt) {
        this.sessionRepository = sessionRepo;
        this.scenarioRepository = scenarioRepo;
        this.restTemplate = rt;
    }

    public GameSession createSession() {
        return sessionRepository.save(new GameSession());
    }

    public Scenario getScenarioForSession(Long sessionId) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session footprint not found."));
        return scenarioRepository.findById((long) session.getCurrentSceneIndex())
                .orElseThrow(() -> new ResourceNotFoundException("Scenario stage definition missing."));
    }

    public GameSession executeChoice(Long sessionId, int choiceIndex) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session record absent."));

        if (session.isGameOver()) {
            return session;
        }

        Scenario scenario = scenarioRepository.findById((long) session.getCurrentSceneIndex())
                .orElseThrow(() -> new ResourceNotFoundException("Scenario missing."));

        if (choiceIndex < 0 || choiceIndex >= scenario.getChoices().size()) {
            throw new IllegalArgumentException("Choice target indexing violation.");
        }

        Choice chosen = scenario.getChoices().get(choiceIndex);
        session.applyOutcome(chosen);

        // Advance progress metrics
        if (!session.isGameOver()) {
            int nextIndex = session.getCurrentSceneIndex() + 1;
            long totalCount = scenarioRepository.count();
            if (nextIndex >= totalCount) {
                session.setGameOver(true);
            } else {
                session.setCurrentSceneIndex(nextIndex);
            }
        }

        return sessionRepository.save(session);
    }

    public String fetchDynamicSvg(String prompt) {
        if ("MOCK_KEY".equals(apiKey)) {
            return null; // Return null to signal client to execute hardcoded fallback rendering logic safely
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> body = new HashMap<>();
            body.put("model", "claude-3-5-sonnet-20241022");
            body.put("max_tokens", 1200);

            Map<String, String> msgMap = new HashMap<>();
            msgMap.put("role", "user");
            msgMap.put("content", "Generate valid RAW SVG code only matching this description: " + prompt + ". Do not wrap in markdown.");
            body.put("messages", Collections.singletonList(msgMap));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity("https://api.anthropic.com/v1/messages", entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
                return content.get(0).get("text").toString();
            }
        } catch (Exception ex) {
            System.err.println("AI fetch error processing safely downstream: " + ex.getMessage());
        }
        return null;
    }

    public GameResponse mapToResponse(GameSession entity) {
        GameResponse dto = new GameResponse();
        dto.setSessionId(entity.getId());
        dto.setHp(entity.getCurrentHealth());
        dto.setScore(entity.getCurrentScore());
        dto.setSceneIndex(entity.getCurrentSceneIndex());
        dto.setGameOver(entity.isGameOver());
        dto.setTraits(entity.getDiscoveredTraits());
        dto.setHistory(entity.getNarrativeHistory());

        Scenario scenario = scenarioRepository.findById((long) entity.getCurrentSceneIndex())
                .orElse(null);
        if (scenario != null) {
            dto.setSceneDescription(scenario.getSceneDescription());
            dto.setHint(scenario.getHint());
            dto.setChoices(scenario.getChoices().stream().map(Choice::getText).toList());
        } else {
            dto.setSceneDescription("The forest falls silent.");
            dto.setHint("No more scenes are available.");
            dto.setChoices(Collections.emptyList());
        }

        if (entity.isGameOver()) {
            long total = scenarioRepository.count();
            boolean finished = entity.getCurrentSceneIndex() >= total || entity.getCurrentHealth() > 0;
            if (!finished || entity.getCurrentHealth() <= 0) {
                dto.setEndingTitle("💀 You Were Claimed");
                dto.setEndingVerdict("The forest kept you. Your health dropped to zero and the trees closed in forever.");
            } else if (entity.getCurrentHealth() >= 65) {
                dto.setEndingTitle("🎉 Master Survivalist");
                dto.setEndingVerdict("You read every threat, trusted your instincts, and walked out unbroken.");
            } else {
                dto.setEndingTitle("🌿 Scarred but Free");
                dto.setEndingVerdict("You made mistakes. You paid for them. But you got out safely.");
            }
        }
        return dto;
    }
}