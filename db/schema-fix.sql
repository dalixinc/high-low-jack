DROP TABLE IF EXISTS players CASCADE;

CREATE TABLE players (
    id BIGSERIAL PRIMARY KEY,  -- Changed from SERIAL
    name VARCHAR(50) UNIQUE NOT NULL,
    total_matches_played INT DEFAULT 0,
    total_matches_won INT DEFAULT 0,
    total_sets_played INT DEFAULT 0,
    total_sets_won INT DEFAULT 0,
    total_rounds_played INT DEFAULT 0,
    highs_won INT DEFAULT 0,
    lows_won INT DEFAULT 0,
    jacks_won INT DEFAULT 0,
    games_won INT DEFAULT 0,
    total_points INT DEFAULT 0,
    current_win_streak INT DEFAULT 0,
    longest_win_streak INT DEFAULT 0,
    favorite_suit VARCHAR(10),
    signature_move VARCHAR(100),
    total_twos_cut INT DEFAULT 0,
    total_ace_spades_played INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_name CHECK (LENGTH(name) >= 2)
);

CREATE INDEX idx_players_name ON players(name);
CREATE INDEX idx_players_last_played ON players(last_played DESC);

INSERT INTO players (name, signature_move) VALUES
    ('Dale', 'The Strategist'),
    ('Primus', 'The Learner'),
    ('Preezbob', 'The Deuce Cutter'),
    ('Kreep', 'The Shadow'),
    ('Ewok', 'The Teacher'),
    ('The Ballie', 'The Wisdom Keeper');