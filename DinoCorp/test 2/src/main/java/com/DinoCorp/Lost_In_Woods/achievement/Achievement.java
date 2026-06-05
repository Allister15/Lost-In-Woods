package com.DinoCorp.Lost_In_Woods.achievement;

// The 24 achievements, grouped into the 5 designated sections. Each carries its display
// title and the short criteria text shown in the modal + toast. The enum NAME (lowercased)
// is the stable id used in the DB (user_achievements.achievement_id) and the frontend.
public enum Achievement {

    // ── SECTION A: SCORE MILESTONES ──
    FOREST_WANDERER(Section.SCORE, "Forest Wanderer", "Accumulate 500 points surviving the early-game terrors."),
    DREAD_NAVIGATOR(Section.SCORE, "Dread Navigator", "Reach a score of 1,000 points."),
    SIN_SHAKER(Section.SCORE, "Sin Shaker", "Surpass 2,000 points, outmaneuvering multiple Sins."),
    LEGEND_OF_THE_TREELINE(Section.SCORE, "Legend of the Treeline", "Achieve a monumental score of 3,000 points."),

    // ── SECTION B: SURVIVOR CHARACTER MILESTONES ──
    THE_PRAGMATIC_PATH(Section.SURVIVOR, "The Pragmatic Path", "Survive 30 beats as Runa the Drifter."),
    APEX_PREDATOR(Section.SURVIVOR, "Apex Predator", "Face a threat holding the Compound Bow & Quiver as Kane."),
    RESILIENT_INNOCENCE(Section.SURVIVOR, "Resilient Innocence", "Reach Chapter 15 as Esme without HP dropping below 40."),
    ANCIENT_RITUALS(Section.SURVIVOR, "Ancient Rituals", "Trigger 5 distinct survivor stances unique to Voss."),
    MILITARY_DISCIPLINE(Section.SURVIVOR, "Military Discipline", "Face a Sin while holding Old Morrow's Crowbar."),
    RALLYING_THE_YOUTH(Section.SURVIVOR, "Rallying the Youth", "Surpass a score of 500 in a single run as Pip."),

    // ── SECTION C: SIN & CORRUPTION ENCOUNTERS ──
    UNMASKING_PRIDE(Section.SINS, "Unmasking Pride", "Meet Adam in his human form and take no damage."),
    FLANNEL_AND_FURY(Section.SINS, "Flannel and Fury", "Survive Banner's attacking-stance assault."),
    GORGING_ON_SHADOW(Section.SINS, "Gorging on Shadow", "Lose all your food rations to Sammuel."),
    HOARDERS_FATE(Section.SINS, "Hoarder's Fate", "Trade an item away to Felicia for safe passage."),
    GLIMPSE_BEYOND_THE_VEIL(Section.SINS, "Glimpse Beyond the Veil", "Witness a Sin warp into its mid-demon phase."),

    // ── SECTION D: INVENTORY & SURVIVAL MASTERY ──
    FIRST_AID(Section.INVENTORY, "First Aid", "Consume a set of Bandages to restore your HP."),
    SCARCITY_MANAGEMENT(Section.INVENTORY, "Scarcity Management", "Fire your last arrow or drain your last canteen."),
    CURIOSITYS_BURDEN(Section.INVENTORY, "Curiosity's Burden", "Fill all 6 inventory slots at once."),
    TACTICAL_TOOLING(Section.INVENTORY, "Tactical Tooling", "Use a mundane item to slip past a hidden danger."),

    // ── SECTION E: FATE & TERMINAL ENDINGS ──
    FORESHADOWED_CHANGE(Section.FATE, "Foreshadowed Change", "Trigger the Transformation ending with 3+ bad traits."),
    THE_NEXT_WARDEN(Section.FATE, "The Next Warden", "Trigger the Lost ending and don the Keeper's hood."),
    LINGERING_ECHO(Section.FATE, "Lingering Echo", "Fall to 0 HP and become a wandering Ghost."),
    BREAKING_THE_MIRROR(Section.FATE, "Breaking the Mirror", "Achieve the exceptionally rare Escape ending."),
    DEEP_WOODS_HISTORIAN(Section.FATE, "Deep Woods Historian", "Encounter the Ghost, Dwarf, Transformed Man and Keeper.");

    public enum Section {
        SCORE("Score Milestones"),
        SURVIVOR("Survivor Milestones"),
        SINS("Sin & Corruption"),
        INVENTORY("Inventory & Survival"),
        FATE("Fate & Endings");

        public final String label;
        Section(String label) { this.label = label; }
    }

    public final Section section;
    public final String title;
    public final String description;

    Achievement(Section section, String title, String description) {
        this.section = section;
        this.title = title;
        this.description = description;
    }

    // Stable lowercase id, e.g. FOREST_WANDERER -> "forest_wanderer".
    public String id() {
        return name().toLowerCase();
    }
}
