package com.DinoCorp.Lost_In_Woods.dto;
import lombok.Data;

@Data
public class ChoiceRequest {
    private Long sessionId;
    private int choiceIndex;
}