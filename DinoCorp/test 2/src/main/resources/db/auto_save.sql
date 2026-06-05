-- =====================================================================================
-- Persistent Auto-Save / Resume — PostgreSQL reference DDL.
--
-- JPA ddl-auto=update also derives this from the GameSession entity; this file documents
-- the canonical shape and the leaderboard-relevant constraints. The auto-save columns
-- EXTEND the existing finished-run ledger (game_sessions) rather than adding a 1:1 table,
-- because StoryService already persists this row every beat.
-- =====================================================================================

-- Existing columns (id, player_name, current_health, current_score, events_survived,
-- game_over, ending, final_score) are unchanged. New auto-save columns:

ALTER TABLE game_sessions ADD COLUMN IF NOT EXISTS resume_token            UUID;
ALTER TABLE game_sessions ADD COLUMN IF NOT EXISTS is_guest                BOOLEAN      NOT NULL DEFAULT FALSE;
ALTER TABLE game_sessions ADD COLUMN IF NOT EXISTS guest_token             UUID;
ALTER TABLE game_sessions ADD COLUMN IF NOT EXISTS current_chapter         INTEGER      NOT NULL DEFAULT 1;
ALTER TABLE game_sessions ADD COLUMN IF NOT EXISTS current_turn_in_chapter INTEGER      NOT NULL DEFAULT 1;
ALTER TABLE game_sessions ADD COLUMN IF NOT EXISTS items_json              VARCHAR(1000);
ALTER TABLE game_sessions ADD COLUMN IF NOT EXISTS traits_json             VARCHAR(1000);
ALTER TABLE game_sessions ADD COLUMN IF NOT EXISTS last_beat_json          VARCHAR(8000);  -- verbatim StoryResponse
ALTER TABLE game_sessions ADD COLUMN IF NOT EXISTS updated_at              TIMESTAMP;

-- Resume lookup MUST be by the opaque token (never the sequential id, which is
-- enumerable). Unique so one token resolves to exactly one run; indexed for the
-- GET /api/game/resume hot path.
ALTER TABLE game_sessions
    ADD CONSTRAINT uq_game_sessions_resume_token UNIQUE (resume_token);

-- Leaderboard read path: finished runs, score-desc. The service scans this window and
-- keeps each player's single highest-peak row (guests keyed by guest_token, named users
-- by lower(player_name)) to dedupe before returning the Top 10.
CREATE INDEX IF NOT EXISTS idx_game_sessions_finished
    ON game_sessions (game_over, final_score DESC);

-- Group runs by their persistent identity for dedupe / "all my runs" lookups, and to
-- support guest-score expiry pruning by inactivity.
CREATE INDEX IF NOT EXISTS idx_game_sessions_guest_token ON game_sessions (guest_token);
CREATE INDEX IF NOT EXISTS idx_game_sessions_updated_at  ON game_sessions (updated_at);

-- Reference: the deduped Top-10 expressed purely in SQL (the service does the same in
-- Java for portability). DISTINCT ON keeps the highest-scoring row per dedupe key.
--
--   SELECT player_name, events_survived, final_score
--   FROM (
--       SELECT DISTINCT ON (CASE WHEN is_guest THEN COALESCE(guest_token::text, 'session:' || id)
--                                ELSE 'name:' || lower(player_name) END)
--              player_name, events_survived, final_score
--       FROM game_sessions
--       WHERE game_over = TRUE
--       ORDER BY 1, final_score DESC
--   ) peaks
--   ORDER BY final_score DESC
--   LIMIT 10;
--
-- Guest-score expiry (run periodically): drop stale, unfinished guest runs so the board
-- and table stay clean; authenticated runs are never pruned.
--   DELETE FROM game_sessions
--   WHERE is_guest = TRUE AND game_over = FALSE AND updated_at < now() - INTERVAL '30 days';
