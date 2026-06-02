package com.DinoCorp.Lost_In_Woods.dto;

import lombok.Data;
import java.util.List;

@Data
public class GameResponse {
    private Long sessionId;
    private int hp;
    private int score;
    private int sceneIndex;
    private boolean gameOver;
    private String chapter;
    private String sceneDescription;
    private String hint;
    private String entityAvatar;
    private String entityName;
    private String entityQuote;
    private String svgPrompt;
    private List<String> choices;
    private List<String> traits;
    private List<String> history;
    private String endingTitle;
    private String endingVerdict;
}
