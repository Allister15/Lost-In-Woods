-- =====================================================================================
-- Hybrid Story Engine — PostgreSQL schema (TASK 1, reference / migration).
--
-- JPA ddl-auto=update also derives these tables from the @Entity classes
-- (HybridGameSession, PrefilledStoryNode); this file is the canonical, reviewable shape
-- and documents the integrity constraints that structurally defeat the two classic bugs:
--   * "JSON Breakage Bug"        -> JSON columns are only ever written via Jackson
--                                   (StringListJsonConverter / ChoiceListJsonConverter),
--                                   so quotes inside narrative/choices/items are escaped.
--   * "Ghost Item Dependency Bug"-> items_json IS the inventory; choices are validated
--                                   against it at build time (StoryGuardrails).
-- =====================================================================================

-- One prefilled playthrough's runtime cursor (lightweight; the funnel mutates only this).
CREATE TABLE IF NOT EXISTS hybrid_game_session (
    session_id          VARCHAR(64)  PRIMARY KEY,            -- caller UUID; FK target for nodes
    player_name         VARCHAR(255) NOT NULL,
    current_node_index  INTEGER      NOT NULL DEFAULT 1,     -- 1..80 linear cursor
    hp                  INTEGER      NOT NULL DEFAULT 100,
    score               INTEGER      NOT NULL DEFAULT 0,
    active_items        VARCHAR(1000) NOT NULL DEFAULT '[]', -- JSON array, Jackson-serialized
    runtime_score       INTEGER      NOT NULL DEFAULT 0,     -- hidden karma -> picks the ending
    finished            BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_hgs_node_index CHECK (current_node_index BETWEEN 1 AND 80),
    CONSTRAINT chk_hgs_hp         CHECK (hp BETWEEN 0 AND 100)
);

-- The 80 authored beats per session (20 chapters x 4 turns). Fully built up-front.
CREATE TABLE IF NOT EXISTS prefilled_story_node (
    id               BIGSERIAL    PRIMARY KEY,
    session_id       VARCHAR(64)  NOT NULL
                        REFERENCES hybrid_game_session(session_id) ON DELETE CASCADE,
    node_index       INTEGER      NOT NULL,                  -- 1..80 (the funnel target)
    chapter_number   INTEGER      NOT NULL,                  -- 1..20
    turn_number      INTEGER      NOT NULL,                  -- 1..4 (4 == fixed anchor)
    location         VARCHAR(32)  NOT NULL,
    npc              VARCHAR(32)  NOT NULL DEFAULT '',
    stance           VARCHAR(24)  NOT NULL DEFAULT 'base',
    survivor_stance  VARCHAR(24)  NOT NULL DEFAULT 'base',
    narrative        VARCHAR(1000) NOT NULL,                 -- EXACTLY two sentences, one line
    choices_json     VARCHAR(2000) NOT NULL DEFAULT '[]',    -- [{"text":...}]; Jackson-escaped
    hp               INTEGER      NOT NULL DEFAULT 100,
    score            INTEGER      NOT NULL DEFAULT 0,
    items_json       VARCHAR(1000) NOT NULL DEFAULT '[]',    -- the inventory the guard reads
    outcome          VARCHAR(16)  NOT NULL DEFAULT 'continue',
    ending           VARCHAR(64),                            -- null unless outcome != continue

    -- The unique (session_id, node_index) index IS the O(1) funnel seek.
    CONSTRAINT uq_session_node UNIQUE (session_id, node_index),
    CONSTRAINT chk_psn_index   CHECK (node_index BETWEEN 1 AND 80),
    CONSTRAINT chk_psn_chapter CHECK (chapter_number BETWEEN 1 AND 20),
    CONSTRAINT chk_psn_turn    CHECK (turn_number BETWEEN 1 AND 4),
    CONSTRAINT chk_psn_outcome CHECK (outcome IN ('continue','death','escape','transformation','lost','secret')),
    CONSTRAINT chk_psn_ending  CHECK (
        (outcome = 'continue' AND ending IS NULL) OR (outcome <> 'continue')
    )
);

CREATE INDEX IF NOT EXISTS idx_node_session ON prefilled_story_node(session_id);
