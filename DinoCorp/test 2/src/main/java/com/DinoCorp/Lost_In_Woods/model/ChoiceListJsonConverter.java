package com.DinoCorp.Lost_In_Woods.model;

import com.DinoCorp.Lost_In_Woods.ai.dto.StoryBeatPayload.Choice;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

// Persists the four on-screen choices as a JSON array of {"text":...} objects.
// Serialized with Jackson so any quote in choice text is escaped on the way into the
// choices_json column (database-layer "JSON Breakage Bug" fix).
@Converter
public class ChoiceListJsonConverter implements AttributeConverter<List<Choice>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<Choice>> TYPE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<Choice> attribute) {
        try {
            return MAPPER.writeValueAsString(attribute == null ? List.of() : attribute);
        } catch (Exception e) {
            return "[]";
        }
    }

    @Override
    public List<Choice> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return new ArrayList<>();
        try {
            return MAPPER.readValue(dbData, TYPE);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
