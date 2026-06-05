package com.DinoCorp.Lost_In_Woods.model;

import com.DinoCorp.Lost_In_Woods.ai.dto.StoryBeatPayload.Choice;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// One prefilled beat. Exactly 80 of these exist per session (20 chapters x 4 turns).
// Everything is authored up-front at session creation; nothing is generated on a
// player's turn. Runtime reads are an O(1) seek on the (session_id, node_index)
// unique index — that is the False-Branching funnel target.
@Entity
@Table(
        name = "prefilled_story_node",
        uniqueConstraints = @UniqueConstraint(name = "uq_session_node", columnNames = {"session_id", "node_index"}),
        indexes = @Index(name = "idx_node_session", columnList = "session_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrefilledStoryNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", length = 64, nullable = false)
    private String sessionId;

    @Column(name = "node_index", nullable = false)
    private int nodeIndex;        // 1..80 — the linear cursor / funnel target

    @Column(name = "chapter_number", nullable = false)
    private int chapterNumber;    // 1..20

    @Column(name = "turn_number", nullable = false)
    private int turnNumber;       // 1..4 (4 == fixed milestone anchor)

    @Column(nullable = false, length = 32)
    private String location;

    @Builder.Default
    @Column(nullable = false, length = 32)
    private String npc = "";

    @Builder.Default
    @Column(nullable = false, length = 24)
    private String stance = "base";

    @Builder.Default
    @Column(name = "survivor_stance", nullable = false, length = 24)
    private String survivorStance = "base";

    // EXACTLY two sentences, single line. Length headroom for the longest authored beat.
    @Column(nullable = false, length = 1000)
    private String narrative;

    // 4 choices while alive, 0 on an ending. Stored as Jackson-escaped JSON (choices_json).
    @Convert(converter = ChoiceListJsonConverter.class)
    @Column(name = "choices_json", length = 2000, nullable = false)
    @Builder.Default
    private List<Choice> choices = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private int hp = 100;

    @Builder.Default
    @Column(nullable = false)
    private int score = 0;

    // The inventory snapshot for this beat. The ghost-item guard validates choices
    // against this list so no option ever references gear the player isn't holding.
    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "items_json", length = 1000, nullable = false)
    @Builder.Default
    private List<String> items = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false, length = 16)
    private String outcome = "continue";   // continue | death | escape | transformation | lost | secret

    @Column(length = 64)
    private String ending;                  // null unless outcome != continue
}
