package com.DinoCorp.Lost_In_Woods.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

// The escape-safe beat payload the Hybrid Story Engine returns to the frontend.
// Key order mirrors the GameMaster schema (streaming-friendly). It is serialized ONLY
// through Jackson (Spring's response writer / the JPA converters) so any quote, newline
// or backslash inside the narrative is always escaped — this is the structural defence
// against the "JSON Breakage Bug". Never hand-concatenate this object.
//
// NOTE: ending is intentionally serialized even when null (-> "ending":null) so the
// frontend's Jackson parser always finds the required fallback field.
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
        "location", "npc", "stance", "survivor_stance", "narrative",
        "choices", "hp", "score", "items", "outcome", "ending"
})
public record StoryBeatPayload(
        String location,
        String npc,
        String stance,
        @JsonProperty("survivor_stance") String survivorStance,
        String narrative,
        List<Choice> choices,
        int hp,
        int score,
        List<String> items,
        String outcome,
        String ending
) {
    // One on-screen option. Renders as {"text":"..."} — matches the anchor payloads.
    // (There are always 4 of these on a live beat, and 0 on an ending beat.)
    public record Choice(String text) {}
}
