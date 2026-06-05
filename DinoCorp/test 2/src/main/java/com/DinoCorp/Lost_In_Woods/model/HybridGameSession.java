package com.DinoCorp.Lost_In_Woods.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// One prefilled playthrough's runtime cursor. The 80 authored beats live in
// prefilled_story_node; this lightweight row is the only thing the False-Branching
// funnel mutates per turn (it just does current_node_index + 1).
@Entity
@Table(name = "hybrid_game_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HybridGameSession {

    // Caller-supplied session id (UUID). Natural key — also the FK every node points to.
    @Id
    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    // Linear cursor into prefilled_story_node.node_index (1..80). The funnel = +1.
    @Builder.Default
    @Column(name = "current_node_index", nullable = false)
    private int currentNodeIndex = 1;

    @Builder.Default
    @Column(nullable = false)
    private int hp = 100;

    @Builder.Default
    @Column(nullable = false)
    private int score = 0;

    // Live inventory mirror of the current node — the source the ghost-item guard reads.
    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "active_items", length = 1000, nullable = false)
    @Builder.Default
    private List<String> activeItems = new ArrayList<>();

    // Hidden "karma" accrued from the choices the player actually clicks. Because the
    // path itself is fixed, this is what lets the four Chapter-20 endings still reflect
    // the run: selectEnding() reads it when the cursor reaches the terminal node.
    @Builder.Default
    @Column(name = "runtime_score", nullable = false)
    private int runtimeScore = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean finished = false;
}
