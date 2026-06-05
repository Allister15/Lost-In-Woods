package com.DinoCorp.Lost_In_Woods.ai.engine;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

// Builds the three dynamic filler turns (1, 2, and the turn-3 "setup") that lead into
// each chapter's fixed anchor. Deterministic and fully offline — it runs once during
// generateInitialTree, never on a player's turn, so it adds zero runtime latency.
//
// Every line it emits is EXACTLY two sentences, on a single line, and deliberately
// item-agnostic so it can never trip the ghost-item guard. The turn-3 "setup" escalates
// and foreshadows that the anchor's threat is near, without naming the NPC (so filler
// keeps npc="" / stance="base" and stays clear of the corruption state machine).
//
// Swap-in point: to use a true LLM at load time instead of these templates, replace the
// body of build() with a single batched AI call — the rest of the engine is unaffected.
@Component
public class FillerNarrativeFactory {

    public record FillerBeat(String location, String narrative, List<String> choiceTexts, String survivorStance) {}

    // Two ambience lines per location, used for turns 1 and 2.
    private static final Map<String, List<String>> AMBIENCE = Map.of(
            "dense_forest", List.of(
                    "The black pines crowd so close their wet needles rake across your face. Something heavy shifts in the brush ahead, then falls still as if it is listening back.",
                    "Cold bark crumbles to rot under your groping hands as you feel between the trunks. A branch creaks somewhere above, bending under a weight you cannot see."),
            "clearing", List.of(
                    "Pale weeds hiss against your legs as the open ground swallows every sound you make. The moon hangs swollen and wrong, throwing long shadows that do not match the trees.",
                    "Frost crackles underfoot where the grass thins to bare silver dirt. A wind with no source circles you slowly, carrying the faint sweetness of something dead."),
            "swamp", List.of(
                    "Black water sucks at your boots with every dragging step through the reeds. Gas bubbles up from the muck and bursts, releasing a stench of rot and old meat.",
                    "Grey moss drips cold threads down the back of your neck as the ground turns to soup. Something long and low slides beneath the surface, leaving a slow ripple toward you."),
            "cliff", List.of(
                    "Loose shale skitters from under your boots and vanishes into the dark below. The wind shoves at your back, eager to push you off the narrow, crumbling ledge.",
                    "Freezing rain slicks the bare rock until every handhold feels like wet glass. Far down the canyon a sound rises that is almost, but not quite, a human voice."),
            "stream", List.of(
                    "Icy water roars over jagged stones, drowning out everything but its own hungry rush. The far bank keeps shifting in the dark, never quite where you last saw it.",
                    "Your soaked boots go numb in the rushing shallows as the current drags at your knees. Something upstream dams the water for a heartbeat, then lets it crash forward again."),
            "pond", List.of(
                    "The black water lies flat and dead, reflecting a sky with no stars at all. Pale roots break the surface near your feet, twitching though there is no wind.",
                    "A skin of cold mist hangs over the still pond, hiding whatever waits beneath. Each ripple you make spreads out and returns wrong, against the pull of the shore."),
            "cave", List.of(
                    "The dark closes over you like cold water as the cave swallows the last moonlight. Your own breath echoes back doubled, as if something just ahead is breathing too.",
                    "Freezing damp drips from unseen stone and runs down your collar in thin lines. The floor tilts down into a blackness that hums faintly, deep and patient.")
    );

    // Turn-3 "setup": escalate and foreshadow that the chapter's anchor threat is near.
    private static final Map<String, String> SETUP = Map.of(
            "dense_forest", "The trees ahead lean inward, funneling you toward a deeper hollow you did not choose. A shape waits there in the gloom, patient and far too still to be a tree.",
            "clearing", "The open ground narrows toward a single pale figure standing where the weeds part. Its head turns to follow you before you have made a sound, and the cold deepens.",
            "swamp", "The reeds thin around a rotting log where the water has gone utterly silent. Something rests on that log ahead, lifting its head as your struggling steps draw near.",
            "cliff", "The ledge pinches to a thread of stone above the black, bottomless drop. A crouched silhouette waits at the edge, staring fixedly at the gear strapped to your belt.",
            "stream", "The bank ahead is churned and broken, the underbrush snapped as if something burst through. A heavy shape rises from the far side, and the smell of split wood rolls over you.",
            "pond", "The mist parts around a figure standing knee-deep in the motionless black water. Its eyes find yours and lock there, and your pulse hammers loud enough to give you away.",
            "cave", "The tunnel opens into a low chamber lit by a sick, glowing mold. Something is slumped against the far wall, and it begins, slowly, to unfold toward you."
    );

    private static final String DEFAULT_LOCATION = "dense_forest";

    // Item-agnostic actions — safe on every beat regardless of inventory.
    private static final List<String> FILLER_CHOICES = List.of(
            "Press deeper into the dark",
            "Stop and listen closely",
            "Search the ground nearby",
            "Move quickly and quietly");

    public FillerBeat build(String location, int turn) {
        String loc = AMBIENCE.containsKey(location) ? location : DEFAULT_LOCATION;
        String narrative;
        if (turn >= 3) {
            narrative = SETUP.getOrDefault(loc, SETUP.get(DEFAULT_LOCATION));
        } else {
            List<String> ambience = AMBIENCE.get(loc);
            narrative = ambience.get((turn - 1) % ambience.size());
        }
        return new FillerBeat(loc, narrative, FILLER_CHOICES, "base");
    }
}
