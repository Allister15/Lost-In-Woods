package com.DinoCorp.Lost_In_Woods.dto;

import com.DinoCorp.Lost_In_Woods.ai.dto.StoryResponse;

// Payload for GET /api/game/resume. Carries the identity the reloaded page needs plus
// the verbatim last beat so the UI can re-render exactly where the player left off.
//   active == true  -> the run is still in progress (keep playing from `beat`)
//   active == false -> the run had already ended (show the ending / offer a new run)
public record ResumeResponse(
        Long sessionId,
        String playerName,
        boolean isGuest,
        boolean active,
        StoryResponse beat
) {}
