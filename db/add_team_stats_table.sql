-- ═══════════════════════════════════════════════════════════════════════════════
-- ADD TEAM_STATS TABLE TO DATABASE
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE team_stats (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    
    -- Team Players
    player1 VARCHAR(50),
    player2 VARCHAR(50),
    
    -- Match Statistics
    matches_played INT DEFAULT 0,
    matches_won INT DEFAULT 0,
    sets_won INT DEFAULT 0,
    
    -- Point Statistics
    highs_won INT DEFAULT 0,
    lows_won INT DEFAULT 0,
    jacks_won INT DEFAULT 0,
    games_won INT DEFAULT 0,
    
    -- Streaks
    current_streak INT DEFAULT 0,
    longest_streak INT DEFAULT 0,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_team_stats_name ON team_stats(name);
CREATE INDEX idx_team_stats_last_played ON team_stats(last_played DESC);

-- Optional: Add some sample teams (will be created automatically when games are played)
-- No need to insert anything here - teams will be created dynamically

SELECT 'Team stats table created successfully!' as status;
