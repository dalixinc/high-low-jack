-- Migration: add new award stat columns to players table
-- Run against both local and Railway databases

ALTER TABLE players ADD COLUMN IF NOT EXISTS sweeps_won      INTEGER NOT NULL DEFAULT 0;
ALTER TABLE players ADD COLUMN IF NOT EXISTS close_set_wins  INTEGER NOT NULL DEFAULT 0;
ALTER TABLE players ADD COLUMN IF NOT EXISTS failed_from_10  INTEGER NOT NULL DEFAULT 0;
