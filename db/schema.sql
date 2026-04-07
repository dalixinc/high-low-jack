-- ═══════════════════════════════════════════════════════════════════════════════
-- HIGH LOW JACK - POSTGRESQL DATABASE SCHEMA
-- ═══════════════════════════════════════════════════════════════════════════════
-- Version: 1.0
-- Author: Dale & Primus
-- Purpose: Persist player stats, matches, achievements, and personality
-- ═══════════════════════════════════════════════════════════════════════════════

-- Drop existing tables (for clean reinstall)
DROP TABLE IF EXISTS personality_quips CASCADE;
DROP TABLE IF EXISTS achievements CASCADE;
DROP TABLE IF EXISTS round_details CASCADE;
DROP TABLE IF EXISTS set_details CASCADE;
DROP TABLE IF EXISTS match_participants CASCADE;
DROP TABLE IF EXISTS matches CASCADE;
DROP TABLE IF EXISTS players CASCADE;

-- ═══════════════════════════════════════════════════════════════════════════════
-- PLAYERS TABLE - Core player profiles and lifetime statistics
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE players (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    
    -- Lifetime Statistics
    total_matches_played INT DEFAULT 0,
    total_matches_won INT DEFAULT 0,
    total_sets_played INT DEFAULT 0,
    total_sets_won INT DEFAULT 0,
    total_rounds_played INT DEFAULT 0,
    
    -- Point Category Totals
    highs_won INT DEFAULT 0,
    lows_won INT DEFAULT 0,
    jacks_won INT DEFAULT 0,
    games_won INT DEFAULT 0,
    total_points INT DEFAULT 0,
    
    -- Win Streaks
    current_win_streak INT DEFAULT 0,
    longest_win_streak INT DEFAULT 0,
    
    -- Style & Personality
    favorite_suit VARCHAR(10),          -- Most pitched suit
    signature_move VARCHAR(100),        -- e.g., "The Deuce Cutter"
    total_twos_cut INT DEFAULT 0,       -- Special stat for Preezbob!
    total_ace_spades_played INT DEFAULT 0,  -- The Ace of Africa!
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_name CHECK (LENGTH(name) >= 2)
);

-- Indexes for common queries
CREATE INDEX idx_players_name ON players(name);
CREATE INDEX idx_players_last_played ON players(last_played DESC);

-- ═══════════════════════════════════════════════════════════════════════════════
-- MATCHES TABLE - Match-level data
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE matches (
    id SERIAL PRIMARY KEY,
    
    -- Match Configuration
    match_type VARCHAR(20) NOT NULL,    -- SINGLE_SET, BEST_OF_THREE, etc.
    is_team_mode BOOLEAN NOT NULL,
    
    -- Winner Information
    winner_name VARCHAR(100) NOT NULL,  -- Player or Team name
    final_score VARCHAR(20),            -- e.g., "2-1" for sets
    
    -- Team Information (if team mode)
    team1_name VARCHAR(50),
    team1_player1 VARCHAR(50),
    team1_player2 VARCHAR(50),
    team2_name VARCHAR(50),
    team2_player1 VARCHAR(50),
    team2_player2 VARCHAR(50),
    
    -- Timing
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_minutes INT,
    
    -- Constraints
    CONSTRAINT valid_match_type CHECK (match_type IN ('SINGLE_SET', 'BEST_OF_THREE', 'BEST_OF_FIVE', 'BEST_OF_SEVEN'))
);

-- Indexes
CREATE INDEX idx_matches_completed ON matches(completed_at DESC);
CREATE INDEX idx_matches_winner ON matches(winner_name);

-- ═══════════════════════════════════════════════════════════════════════════════
-- MATCH_PARTICIPANTS TABLE - Links players to matches
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE match_participants (
    id SERIAL PRIMARY KEY,
    match_id INT NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    player_name VARCHAR(50) NOT NULL,
    team_name VARCHAR(50),              -- NULL for individual mode
    is_winner BOOLEAN NOT NULL,
    sets_won INT DEFAULT 0,
    total_points INT DEFAULT 0,
    
    -- Foreign key
    CONSTRAINT fk_participant_player FOREIGN KEY (player_name) REFERENCES players(name) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_participants_match ON match_participants(match_id);
CREATE INDEX idx_participants_player ON match_participants(player_name);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SET_DETAILS TABLE - Set-level statistics
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE set_details (
    id SERIAL PRIMARY KEY,
    match_id INT NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    set_number INT NOT NULL,
    
    -- Winner Information
    winner_name VARCHAR(100) NOT NULL,  -- Player or Team name
    winning_point VARCHAR(10) NOT NULL, -- High/Low/Jack/Game
    was_tiebreaker BOOLEAN DEFAULT FALSE,
    
    -- Final Scores (JSON for flexibility)
    final_scores JSONB NOT NULL,       -- {"Player1": 11, "Player2": 8}
    
    -- Timing
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_set_number CHECK (set_number >= 1),
    CONSTRAINT valid_winning_point CHECK (winning_point IN ('High', 'Low', 'Jack', 'Game'))
);

-- Indexes
CREATE INDEX idx_sets_match ON set_details(match_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- ROUND_DETAILS TABLE - Round-by-round data (optional, for deep analytics)
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE round_details (
    id SERIAL PRIMARY KEY,
    set_id INT NOT NULL REFERENCES set_details(id) ON DELETE CASCADE,
    round_number INT NOT NULL,
    
    -- Trump and Pitcher
    trump_suit VARCHAR(10) NOT NULL,
    pitcher_name VARCHAR(50) NOT NULL,
    cut_card VARCHAR(10),               -- e.g., "TWO♠"
    
    -- Point Winners
    high_winner VARCHAR(50),
    high_card VARCHAR(10),
    low_winner VARCHAR(50),
    low_card VARCHAR(10),
    jack_winner VARCHAR(50),
    game_winner VARCHAR(50),
    game_points INT,
    
    -- Scores After Round (JSON)
    scores_after JSONB NOT NULL,
    
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_round_number CHECK (round_number >= 1),
    CONSTRAINT valid_trump CHECK (trump_suit IN ('SPADES', 'HEARTS', 'DIAMONDS', 'CLUBS'))
);

-- Indexes
CREATE INDEX idx_rounds_set ON round_details(set_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- ACHIEVEMENTS TABLE - Player achievements and medals
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE achievements (
    id SERIAL PRIMARY KEY,
    player_name VARCHAR(50) NOT NULL,
    
    -- Achievement Details
    achievement_code VARCHAR(50) NOT NULL,      -- e.g., 'ACE_OF_AFRICA'
    achievement_name VARCHAR(100) NOT NULL,     -- e.g., 'The Ace of Africa'
    description TEXT,
    
    -- Context
    match_id INT REFERENCES matches(id) ON DELETE SET NULL,
    set_id INT REFERENCES set_details(id) ON DELETE SET NULL,
    round_id INT REFERENCES round_details(id) ON DELETE SET NULL,
    
    -- Timing
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Metadata
    rarity VARCHAR(20) DEFAULT 'COMMON',        -- COMMON, RARE, EPIC, LEGENDARY
    
    -- Foreign key
    CONSTRAINT fk_achievement_player FOREIGN KEY (player_name) REFERENCES players(name) ON DELETE CASCADE,
    CONSTRAINT valid_rarity CHECK (rarity IN ('COMMON', 'RARE', 'EPIC', 'LEGENDARY'))
);

-- Indexes
CREATE INDEX idx_achievements_player ON achievements(player_name);
CREATE INDEX idx_achievements_code ON achievements(achievement_code);
CREATE INDEX idx_achievements_unlocked ON achievements(unlocked_at DESC);

-- ═══════════════════════════════════════════════════════════════════════════════
-- PERSONALITY_QUIPS TABLE - Contextual commentary database
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE personality_quips (
    id SERIAL PRIMARY KEY,
    
    -- Target Player (NULL = generic quip)
    player_name VARCHAR(50),
    
    -- Trigger Context
    trigger_context VARCHAR(100) NOT NULL,      -- e.g., 'CUT_TWO_LOSING', 'PLAY_ACE_SPADES'
    
    -- The Quip
    quip_text TEXT NOT NULL,
    
    -- Categorization
    category VARCHAR(50),                       -- 'CELEBRATION', 'TAUNT', 'ENCOURAGEMENT', etc.
    tone VARCHAR(20) DEFAULT 'NEUTRAL',         -- 'POSITIVE', 'NEGATIVE', 'NEUTRAL'
    
    -- Usage Tracking
    times_used INT DEFAULT 0,
    last_used TIMESTAMP,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Constraints
    CONSTRAINT valid_tone CHECK (tone IN ('POSITIVE', 'NEGATIVE', 'NEUTRAL'))
);

-- Indexes
CREATE INDEX idx_quips_player ON personality_quips(player_name);
CREATE INDEX idx_quips_context ON personality_quips(trigger_context);
CREATE INDEX idx_quips_category ON personality_quips(category);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - Initial Players
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO players (name, signature_move) VALUES
    ('Dale', 'The Strategist'),
    ('Primus', 'The Learner'),
    ('Preezbob', 'The Deuce Cutter'),
    ('Kreep', 'The Shadow'),
    ('Ewok', 'The Teacher'),
    ('The Ballie', 'The Wisdom Keeper');

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - Preezbob's Signature Quips
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO personality_quips (player_name, trigger_context, quip_text, category, tone) VALUES
    ('Preezbob', 'CUT_TWO_LOSING', 'Classic Preezbob! Cutting twos when behind!', 'SIGNATURE', 'NEUTRAL'),
    ('Preezbob', 'CUT_TWO_WINNING', 'Preezbob on FIRE! Even his twos are winners!', 'CELEBRATION', 'POSITIVE'),
    ('Preezbob', 'PLAY_ACE_SPADES', 'THE ACE OF AFRICA STRIKES!', 'SIGNATURE', 'POSITIVE'),
    ('Preezbob', 'WIN_WITH_ACE_SPADES', 'Preezbob''s Revenge! The Ace of Africa seals the victory!', 'EPIC', 'POSITIVE'),
    (NULL, 'TIEBREAKER_WIN', 'Down to the WIRE! Precedence rules FTW!', 'CELEBRATION', 'POSITIVE'),
    (NULL, 'COMEBACK_WIN', 'THE COMEBACK IS REAL! Never give up!', 'EPIC', 'POSITIVE'),
    (NULL, 'PERFECT_SWEEP', 'FLAWLESS VICTORY! Not even a contest!', 'CELEBRATION', 'POSITIVE');

-- ═══════════════════════════════════════════════════════════════════════════════
-- VIEWS - Useful Statistics Views
-- ═══════════════════════════════════════════════════════════════════════════════

-- Player Leaderboard
CREATE VIEW v_player_leaderboard AS
SELECT 
    name,
    total_matches_won,
    total_matches_played,
    CASE 
        WHEN total_matches_played > 0 
        THEN ROUND((total_matches_won::NUMERIC / total_matches_played) * 100, 1)
        ELSE 0 
    END as win_percentage,
    total_points,
    highs_won,
    lows_won,
    jacks_won,
    games_won,
    current_win_streak,
    longest_win_streak
FROM players
ORDER BY total_matches_won DESC, win_percentage DESC;

-- Recent Matches
CREATE VIEW v_recent_matches AS
SELECT 
    m.id,
    m.match_type,
    m.is_team_mode,
    m.winner_name,
    m.final_score,
    m.completed_at,
    m.duration_minutes
FROM matches m
ORDER BY m.completed_at DESC
LIMIT 50;

-- Achievement Showcase
CREATE VIEW v_achievement_showcase AS
SELECT 
    a.player_name,
    COUNT(*) as total_achievements,
    COUNT(CASE WHEN a.rarity = 'LEGENDARY' THEN 1 END) as legendary_count,
    COUNT(CASE WHEN a.rarity = 'EPIC' THEN 1 END) as epic_count,
    MAX(a.unlocked_at) as last_unlocked
FROM achievements a
GROUP BY a.player_name
ORDER BY total_achievements DESC;

-- ═══════════════════════════════════════════════════════════════════════════════
-- END OF SCHEMA
-- ═══════════════════════════════════════════════════════════════════════════════

-- Grant permissions (adjust username as needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO highlowjack_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO highlowjack_user;
