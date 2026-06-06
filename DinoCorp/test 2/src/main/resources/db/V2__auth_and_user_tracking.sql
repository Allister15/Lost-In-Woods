-- =====================================================================================
-- V2__auth_and_user_tracking.sql  —  permanent accounts, leaderboard, achievement mapping.
--
-- NOTE ON MIGRATIONS: this project currently runs with spring.jpa.hibernate.ddl-auto=update
-- (Hibernate creates/updates tables from the @Entity classes) and does NOT have Flyway wired.
-- This file is the canonical, reviewable schema. To adopt Flyway: add flyway-core, place this
-- under src/main/resources/db/migration, baseline the existing schema as V1, and switch
-- ddl-auto to "validate". As-is, ddl-auto creates `users` and `leaderboard` from the entities.
-- =====================================================================================

-- ── Accounts ─────────────────────────────────────────────────────────────────────────
-- username is the canonical (lowercased) PRIMARY KEY -> case-insensitive uniqueness.
CREATE TABLE IF NOT EXISTS users (
    username      VARCHAR(40)  PRIMARY KEY,
    display_name  VARCHAR(40)  NOT NULL,
    session_id    UUID         NOT NULL UNIQUE,     -- permanent account token
    created_at    TIMESTAMP    DEFAULT now()
);

-- ── Global leaderboard, indexed by username ──────────────────────────────────────────
-- One row per account: their highest historical score. Upserted when a registered run ends.
CREATE TABLE IF NOT EXISTS leaderboard (
    username        VARCHAR(40) PRIMARY KEY,
    display_name    VARCHAR(40),
    best_score      INTEGER     NOT NULL DEFAULT 0,
    events_survived INTEGER     NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP   DEFAULT now(),
    CONSTRAINT fk_leaderboard_user
        FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ── Achievement mapping ──────────────────────────────────────────────────────────────
-- user_achievements.owner_key already namespaces identity: "name:<username>" for accounts
-- and "guest:<token>" for anonymous guests (so it cannot be a hard FK to users — guests have
-- no account row). For REGISTERED owners we add a CASCADE so deleting an account removes its
-- achievements. (Run only after the column/format is confirmed in your data.)
--
--   ALTER TABLE user_achievements
--     ADD COLUMN IF NOT EXISTS username VARCHAR(40)
--       GENERATED ALWAYS AS (CASE WHEN owner_key LIKE 'name:%' THEN substring(owner_key from 6) END) STORED;
--   ALTER TABLE user_achievements
--     ADD CONSTRAINT fk_ua_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;
--
-- Application-level cleanup is the portable alternative (delete user_achievements WHERE
-- owner_key = 'name:' || <username> when an account is removed).

-- Helpful indexes.
CREATE INDEX IF NOT EXISTS idx_leaderboard_score ON leaderboard (best_score DESC);
CREATE INDEX IF NOT EXISTS idx_users_session     ON users (session_id);
