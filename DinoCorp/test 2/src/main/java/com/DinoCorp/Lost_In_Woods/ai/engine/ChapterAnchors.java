package com.DinoCorp.Lost_In_Woods.ai.engine;

import java.util.List;

// TASK 3 — the 20 fixed milestone anchors (turn 4 of every chapter) plus the four
// Chapter-20 terminal endings. These are the canonical, hand-authored story beats.
// The engine stitches three dynamically-built filler turns in front of each anchor.
//
// Invariants every entry already satisfies (and StoryGuardrails re-checks at build time):
//   * narrative is EXACTLY two sentences, on a single line;
//   * the Sins' stances obey base(mask) -> mid_demon -> full_demon, never skipping/back;
//   * choices read as plausible actions and never assume gear not in `items`.
public final class ChapterAnchors {

    private ChapterAnchors() {}

    public record AnchorBeat(
            String location,
            String npc,
            String stance,
            String survivorStance,
            String narrative,
            List<String> choiceTexts,
            int hp,
            int score,
            List<String> items,
            String outcome,
            String ending
    ) {}

    private static final List<String> KNIFE = List.of("Survival Knife");
    private static final List<String> NONE = List.of();

    // Chapters 1..19 — exactly one fixed anchor each (index 0 == chapter 1).
    public static final List<AnchorBeat> CHAPTER_ANCHORS = List.of(

            // Chapter 1: Cold Awakening
            new AnchorBeat("dense_forest", "", "base", "base",
                    "The freezing dampness of the forest floor wakes you as the scent of rotted pine leaves suffocates the midnight air. Twigs snap in the dense blackness ahead, forcing your hand to tighten around your rugged outdoor gear.",
                    List.of("Follow the faint animal trail", "Crawl blindly through thick briars", "Check utility vest in the dark", "Listen closely to the crunching"),
                    100, 10, KNIFE, "continue", null),

            // Chapter 2: Echoes in the Dark
            new AnchorBeat("clearing", "", "base", "scanning",
                    "The suffocating tree line gives way to a wide, moonlit clearing choked with head-high white weeds. An unnatural, synchronized clicking sound echoes from the grass, spinning around you in a tightening circle.",
                    List.of("Sprint toward the open opposite edge", "Drop low and hide in weeds", "Ignite a dry grass torch", "Whistle loudly to challenge the shape"),
                    100, 50, KNIFE, "continue", null),

            // Chapter 3: The Smiling Master
            new AnchorBeat("clearing", "adam", "relaxed", "base",
                    "An unblemished, glowing mist parts to reveal a warm silhouette standing gracefully by a crumbling stone well. His confident voice cuts through the freezing wind, offering a clean hand and a flawless path to safety.",
                    List.of("Accept his confident guidance", "Back away toward the dark trees", "Inquire about the forest exit", "Keep your utility knife ready"),
                    100, 100, KNIFE, "continue", null),

            // Chapter 4: The Sluggish Bog
            new AnchorBeat("swamp", "nagi", "drowsy", "base",
                    "Stagnant, foul-smelling black water pools around your ankles as the low branches droop under heavy grey moss. A slouched figure rests languidly on a rotting log ahead, barely lifting his eyes to glance at your struggle.",
                    List.of("Wade deeper into the muck", "Demand directions from the figure", "Search the reeds for dry ground", "Rest on the root nearby"),
                    95, 150, KNIFE, "continue", null),

            // Chapter 5: Hollow Glances
            new AnchorBeat("cliff", "yuri", "brooding", "base",
                    "The loose shale crumbles beneath your steps as a sheer, wind-blasted cliff drops off into a bottomless black canyon. A gaunt, staring figure crouches precariously on the precipice, glaring fixedly at the survival gear attached to your belt.",
                    List.of("Scramble up the wet rockface", "Confront the crouched stalker", "Hide behind a jagged boulder", "Drop an item to distract"),
                    90, 200, KNIFE, "continue", null),

            // Chapter 6: The Trapped Humanoid
            new AnchorBeat("pond", "transformed_man", "base", "observing",
                    "Slick, black water laps against a shore littered with bleached animal skeletons and weeping willow roots. A pitiable creature covered in thick, gnarled bark thrashes violently on the bank, groaning as roots bore into its ankles.",
                    List.of("Throw a stone to distract", "Offer a ration out of pity", "Strike the bark-man immediately", "Retreat silently into the brush"),
                    85, 250, KNIFE, "continue", null),

            // Chapter 7: The Trade Glade
            new AnchorBeat("dense_forest", "felicia", "kind", "base",
                    "The dense forest canopy knits together so tightly that the moonlight shrinks to small, silver needles on the dirt. An approachable woman steps out from a hollow trunk, gesturing toward a bulging sack with a warm, transactional smile.",
                    List.of("Inspect her shoulder bag", "Refuse and explore deeper trees", "Trade a signature item away", "Attack her and grab the bag"),
                    85, 300, KNIFE, "continue", null),

            // Chapter 8: The Sprout of Sloth
            new AnchorBeat("cave", "nagi", "mid_demon", "scared",
                    "The low cave ceiling drips freezing moisture onto a floor coated in patches of thick, glowing mold. Nagi's flesh begins sagging unnaturally as bright fungal growths burst through his skin, his posture shifting into a terrifying, deadweight lurch.",
                    List.of("Squeeze past his sagging form", "Drive a blade into his chest", "Sprint deeper into cave darkness", "Throw a torch at his face"),
                    80, 350, KNIFE, "continue", null),

            // Chapter 9: Blue Mist Warnings
            new AnchorBeat("clearing", "ghost", "base", "listening",
                    "A freezing, localized wind sweeps through the open clearing, causing the dry ferns to rustle and die instantly. A semi-transparent young man wrapped in a blue spectral aura emerges from the mist, weeping tears of pure frost.",
                    List.of("Listen closely to the ghost", "Drive a weapon through mist", "Flee backward into the brush", "Quietly pray for its soul"),
                    75, 410, KNIFE, "continue", null),

            // Chapter 10: The Feeding Pit
            new AnchorBeat("dense_forest", "sammuel", "gorging", "base",
                    "The overpowering smell of hot iron and iron-rot hangs heavy beneath the low-hanging brambles of this thicket. A round-faced man sits in a pit of chewed roots, frantically shoving handfuls of mud and raw flesh into his maw.",
                    List.of("Surrender your remaining food strips", "Slash at his protruding throat", "Squeeze past through dense thorns", "Offer him a smooth stone"),
                    70, 470, KNIFE, "continue", null),

            // Chapter 11: Envy Stripped Bare
            new AnchorBeat("cliff", "yuri", "mid_demon", "cornered",
                    "The narrow rock ledge slicked with freezing mountain rain leaves absolutely no room for an easy retreat. Yuri's skin turns a hollow, corpse-like gray as his finger joints snap outwards into long, jagged skeletal claws that scrape the stone.",
                    List.of("Leap across the narrow chasm", "Drive your knife into his jaw", "Bluff him with a loud scream", "Drop your pack and run back"),
                    65, 530, KNIFE, "continue", null),

            // Chapter 12: The Black Willow
            new AnchorBeat("pond", "vincent", "predator", "base",
                    "The reflective surface of the black pond lies completely motionless, mirroring a sky devoid of any stars. A charismatic figure stands knee-deep in the still water, his intense, fixed stare locking directly onto your racing pulse.",
                    List.of("Sprint across the rushing stream", "Drive your knife into his chest", "Demand he step out of path", "Use small hand mirror defensively"),
                    60, 590, KNIFE, "continue", null),

            // Chapter 13: The Axe of Wrath
            new AnchorBeat("stream", "banner", "rage", "reacting",
                    "Rushing stream water thunders over sharp rocks, drowning out the sudden, heavy crash of fracturing underbrush. A broad-shouldered man with a permanent scowl springs across the bank, hoisting a heavy woodcutter's axe directly toward your head.",
                    List.of("Duck beneath the heavy swing", "Parry the axe with your knife", "Throw dirt into his flashing eyes", "Leap backward down the ridge"),
                    55, 650, KNIFE, "continue", null),

            // Chapter 14: Gluttony Expands
            new AnchorBeat("swamp", "sammuel", "mid_demon", "overwhelmed",
                    "Thick, bubbling gases rupture from the ooze as the surrounding cypress trees lean inward at a suffocating angle. Sammuel's frame expands exponentially, his human mouth tearing wide open past his ears to reveal concentric rings of wet teeth.",
                    List.of("Feed him your survival knife", "Dive sideways into foul water", "Scramble up a rotting cypress", "Scream for the Keeper's aid"),
                    45, 710, KNIFE, "continue", null),

            // Chapter 15: The Golden Seduction
            new AnchorBeat("dense_forest", "felicia", "mid_demon", "base",
                    "A sharp metallic clinking rings out as the pine needles beneath your feet turn a cold, lustrous gold color. Felicia's skin hardens into a polished metallic sheen while extra, jointed limbs emerge from her spine, reaching eagerly for your gear.",
                    List.of("Strike at her newly formed limbs", "Slam your pack into her golden face", "Dive through the thorns to escape", "Offer a shiny metal tool"),
                    40, 770, KNIFE, "continue", null),

            // Chapter 16: The Flannel Tear
            new AnchorBeat("stream", "banner", "mid_demon", "attacking",
                    "The clear stream water turns a dark, muddy crimson as the air grows thick with the smell of freshly split wood. Banner's flesh splits apart to reveal winding, thorned briars erupting from his muscles, his frame expanding into a massive engine of rage.",
                    List.of("Roll through the shallow rapids", "Drive your knife into the open wood", "Scale the slippery moss boulder", "Use a tool to block the blow"),
                    35, 830, KNIFE, "continue", null),

            // Chapter 17: Coiling Shorelines
            new AnchorBeat("pond", "vincent", "mid_demon", "base",
                    "The black pond water starts to churn violently as pale, fleshy tentacles lurch upward from the dark depths. Vincent's skin turns a translucent, inhuman white, his beautiful features freezing into a cruel mask as the coils slide across the mud.",
                    List.of("Hack through the approaching coils", "Sprint into the dark reeds", "Throw a heavy stone at his face", "Bluff with your survival light"),
                    30, 890, KNIFE, "continue", null),

            // Chapter 18: Halo of the False God
            new AnchorBeat("cave", "adam", "mid_demon", "base",
                    "The wet cavern walls mirror a sudden, blinding golden luminescence that pierces through the pitch-black tunnels. Adam's jacket ruptures as massive golden wings and razor-sharp halo structures erupt from his back, his flawless smile commanding complete submission.",
                    List.of("Lunge blindly at his beautiful face", "Drape yourself over the cliff edge", "Flee backward into the cavern mouth", "Drop to your knees in surrender"),
                    25, 950, KNIFE, "continue", null),

            // Chapter 19: The Iron Gates
            new AnchorBeat("dense_forest", "keeper", "base", "base",
                    "Winding, ancient roots form a massive, natural wall across the forest path, blocking all forward escape. A hooded figure wrapped in tattered black robes stands before the barrier, his thin, root-covered hand holding a twisted staff.",
                    List.of("Force your way through the roots", "Demand the hooded figure step aside", "Offer your most defining possession", "Slink back into the waiting shadows"),
                    20, 1000, KNIFE, "continue", null)
    );

    // -------------------------------------------------------------------------------
    // Chapter 20 — the four terminal endings. Exactly one becomes node 79 at runtime,
    // chosen by accrued karma (GameStateService.selectEnding). Adam headlines every one
    // in his Stage-3 full-demon form: as on-scene npc for escape, and in the narrative
    // for the fate-NPC endings (ghost / transformed_man / keeper).
    // -------------------------------------------------------------------------------

    // Per the spec, ADAM headlines all four endings in his Stage-3 full_demon form.
    // (npc/stance are identical; the narrative + outcome are what differ.)

    // Ending A — The Blood Sacrifice (death). Default / lowest karma.
    public static final AnchorBeat ENDING_DEATH = new AnchorBeat(
            "clearing", "adam", "full_demon", "base",
            "Adam ascends into an angelic-demonic deity with massive feathered wings and crown-like horns that pierce the sky. His perfect, terrifying frame blindingly ignites as his obsidian appendages reduce your physical form to a lingering, wandering ghost.",
            NONE, 0, 1200, NONE, "death", "Claimed by Pride");

    // Ending B — The Wooden Shell (transformation).
    public static final AnchorBeat ENDING_TRANSFORMATION = new AnchorBeat(
            "dense_forest", "adam", "full_demon", "base",
            "Adam hovers majestically above the glade as his feathered wings slice the wind, his crown-like horns glowing with terrible power. Thick, heavy bark rapidly encases your chest and limbs, anchoring your root-feet permanently into the cursed dirt as a transformed man.",
            NONE, 0, 1500, NONE, "transformation", "The Forest Claims Its Own");

    // Ending C — The Hooded Sentence (lost).
    public static final AnchorBeat ENDING_LOST = new AnchorBeat(
            "dense_forest", "adam", "full_demon", "base",
            "The angelic-demonic deity looks down with a mocking smile as your memories dissolve into the ancient, whispering pine needles. Your trembling fingers slowly pull the tattered black hood over your face, accepting your endless cycle as the new Keeper of the woods.",
            NONE, 0, 1400, NONE, "lost", "The Wardenship");

    // Ending D — Breaking the Mirror (escape). Rarest reward, highest karma.
    public static final AnchorBeat ENDING_ESCAPE = new AnchorBeat(
            "clearing", "adam", "full_demon", "base",
            "Adam's perfect, deified frame shrieks in absolute disbelief as your desperate strike shatters his flawless, porcelain-like golden face. The dense mist parts completely, revealing a clean, starlit country road leading far away from the screaming treeline.",
            NONE, 15, 2500, KNIFE, "escape", "Dawn Light");

    // -------------------------------------------------------------------------------
    // hp/score rails — single source of truth is the anchor table above.
    // ANCHOR_HP/ANCHOR_SCORE[i] is the value the survivor holds AT chapter (i+1)'s
    // anchor. The chapter-20 slot is a pre-climax placeholder; the real terminal hp/score
    // come from whichever ending is finalized at runtime.
    // -------------------------------------------------------------------------------
    public static final int[] ANCHOR_HP = new int[20];
    public static final int[] ANCHOR_SCORE = new int[20];
    static {
        for (int i = 0; i < CHAPTER_ANCHORS.size(); i++) {   // chapters 1..19
            ANCHOR_HP[i] = CHAPTER_ANCHORS.get(i).hp();
            ANCHOR_SCORE[i] = CHAPTER_ANCHORS.get(i).score();
        }
        ANCHOR_HP[19] = 18;        // chapter 20 pre-climax placeholder
        ANCHOR_SCORE[19] = 1100;
    }

    // The fixed milestone for a chapter (1..19). Chapter 20 uses the ending set chosen at
    // runtime, so slot 20 returns the DEATH placeholder until finalizeEnding() rewrites it.
    public static AnchorBeat anchorForChapter(int chapter) {
        if (chapter >= 1 && chapter <= 19) return CHAPTER_ANCHORS.get(chapter - 1);
        return ENDING_DEATH;
    }

    // Linear interpolation of hp/score across a chapter's three filler turns (1..3),
    // ramping from the previous chapter's anchor value toward this chapter's.
    public static int rampHp(int chapter, int turn) {
        int idx = chapter - 1;
        int prev = (chapter == 1) ? 100 : ANCHOR_HP[idx - 1];
        return prev + (ANCHOR_HP[idx] - prev) * turn / 4;
    }

    public static int rampScore(int chapter, int turn) {
        int idx = chapter - 1;
        int prev = (chapter == 1) ? 0 : ANCHOR_SCORE[idx - 1];
        return prev + (ANCHOR_SCORE[idx] - prev) * turn / 4;
    }
}
