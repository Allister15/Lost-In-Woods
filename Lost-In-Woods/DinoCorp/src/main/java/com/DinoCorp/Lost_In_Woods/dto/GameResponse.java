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
    private String sceneDescription;
    private String hint;
    private List<String> choices;
    private List<String> traits;
    private List<String> history;
    private String endingTitle;
    private String endingVerdict;
}