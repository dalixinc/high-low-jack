-- ═══════════════════════════════════════════════════════════════════════════════
-- PERSONALITY QUIPS - SEED DATA
-- ═══════════════════════════════════════════════════════════════════════════════

-- Clear existing quips (optional)
-- DELETE FROM personality_quips;

-- ═══════════════════════════════════════════════════════════════════════════════
-- PREEZBOB'S SIGNATURE QUIPS
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO personality_quips (player_name, trigger_context, quip_text, category, tone) VALUES
    ('Preezbob', 'CUT_TWO_LOSING', 'Classic Preezbob! Cutting twos when behind!', 'SIGNATURE', 'NEUTRAL'),
    ('Preezbob', 'CUT_TWO_LOSING', 'The Deuce Cutter strikes again - even from the depths of defeat!', 'SIGNATURE', 'NEUTRAL'),
    ('Preezbob', 'CUT_TWO_LOSING', 'Down but not out - Preezbob''s two says "I''m still here!"', 'SIGNATURE', 'POSITIVE'),
    
    ('Preezbob', 'CUT_TWO_WINNING', 'Preezbob on FIRE! Even his twos are winners!', 'CELEBRATION', 'POSITIVE'),
    ('Preezbob', 'CUT_TWO_WINNING', 'The Deuce Cutter doesn''t need high cards - a two will do!', 'CELEBRATION', 'POSITIVE'),
    
    ('Preezbob', 'PLAY_ACE_SPADES', 'THE ACE OF AFRICA STRIKES!', 'SIGNATURE', 'POSITIVE'),
    ('Preezbob', 'PLAY_ACE_SPADES', '♠️ Behold the Ace of Africa! ♠️', 'EPIC', 'POSITIVE'),
    ('Preezbob', 'PLAY_ACE_SPADES', 'Preezbob unleashes the legendary Ace of Spades!', 'EPIC', 'POSITIVE'),
    
    ('Preezbob', 'WIN_WITH_ACE_SPADES', 'Preezbob''s Revenge! The Ace of Africa seals the victory!', 'EPIC', 'POSITIVE'),
    ('Preezbob', 'WIN_WITH_ACE_SPADES', 'Victory belongs to the Ace of Africa!', 'EPIC', 'POSITIVE');

-- ═══════════════════════════════════════════════════════════════════════════════
-- GENERIC ROUND QUIPS
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO personality_quips (player_name, trigger_context, quip_text, category, tone) VALUES
    (NULL, 'TIEBREAKER_WIN', 'Down to the WIRE! Precedence rules FTW!', 'CELEBRATION', 'POSITIVE'),
    (NULL, 'TIEBREAKER_WIN', 'Tied at 11? Time for the tiebreaker showdown!', 'CELEBRATION', 'POSITIVE'),
    (NULL, 'TIEBREAKER_WIN', 'High-Low-Jack-Game - precedence decides!', 'CELEBRATION', 'NEUTRAL'),
    
    (NULL, 'SWEEP_ALL_FOUR', 'FLAWLESS VICTORY! All four points!', 'CELEBRATION', 'POSITIVE'),
    (NULL, 'SWEEP_ALL_FOUR', 'A clean sweep - High, Low, Jack, and Game!', 'CELEBRATION', 'POSITIVE'),
    (NULL, 'SWEEP_ALL_FOUR', 'Total domination - not even a contest!', 'CELEBRATION', 'POSITIVE'),
    
    (NULL, 'DOMINATING_WIN', 'Absolutely crushing it!', 'CELEBRATION', 'POSITIVE'),
    (NULL, 'DOMINATING_WIN', 'No mercy shown!', 'CELEBRATION', 'POSITIVE'),
    (NULL, 'DOMINATING_WIN', 'That wasn''t even close!', 'CELEBRATION', 'POSITIVE'),
    
    (NULL, 'CLOSE_WIN', 'Victory snatched from the jaws of defeat!', 'CELEBRATION', 'POSITIVE'),
    (NULL, 'CLOSE_WIN', 'What a nail-biter!', 'CELEBRATION', 'POSITIVE'),
    (NULL, 'CLOSE_WIN', 'Too close for comfort - but a win is a win!', 'CELEBRATION', 'POSITIVE');

-- ═══════════════════════════════════════════════════════════════════════════════
-- MATCH QUIPS
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO personality_quips (player_name, trigger_context, quip_text, category, tone) VALUES
    (NULL, 'PERFECT_MATCH', 'UNDEFEATED! Not a single set lost!', 'EPIC', 'POSITIVE'),
    (NULL, 'PERFECT_MATCH', 'Flawless victory - a perfect match!', 'EPIC', 'POSITIVE'),
    
    (NULL, 'MATCH_WINNER', 'The champion emerges victorious!', 'CELEBRATION', 'POSITIVE'),
    (NULL, 'MATCH_WINNER', 'Match won - glory earned!', 'CELEBRATION', 'POSITIVE'),
    (NULL, 'MATCH_WINNER', 'And that''s game, set, and match!', 'CELEBRATION', 'POSITIVE');

-- ═══════════════════════════════════════════════════════════════════════════════
-- COMEBACK QUIPS
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO personality_quips (player_name, trigger_context, quip_text, category, tone) VALUES
    (NULL, 'COMEBACK_FROM_ZERO', 'THE COMEBACK IS REAL! Never give up!', 'EPIC', 'POSITIVE'),
    (NULL, 'COMEBACK_FROM_ZERO', 'From 0-10 to victory - legendary!', 'EPIC', 'POSITIVE'),
    (NULL, 'COMEBACK_FROM_ZERO', 'They counted you out, but you came back!', 'EPIC', 'POSITIVE');

-- ═══════════════════════════════════════════════════════════════════════════════
-- DALE'S STRATEGIC QUIPS
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO personality_quips (player_name, trigger_context, quip_text, category, tone) VALUES
    ('Dale', 'MATCH_WINNER', 'Strategy prevails - The Strategist claims victory!', 'CELEBRATION', 'POSITIVE'),
    ('Dale', 'DOMINATING_WIN', 'Calculated. Precise. Victorious.', 'CELEBRATION', 'POSITIVE'),
    ('Dale', 'CLOSE_WIN', 'Every move calculated - even the close ones!', 'CELEBRATION', 'POSITIVE');

-- ═══════════════════════════════════════════════════════════════════════════════
-- PRIMUS LEARNING QUIPS
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO personality_quips (player_name, trigger_context, quip_text, category, tone) VALUES
    ('Primus', 'MATCH_WINNER', 'The student becomes the master!', 'CELEBRATION', 'POSITIVE'),
    ('Primus', 'MATCH_WINNER', 'Learning pays off - victory is mine!', 'CELEBRATION', 'POSITIVE'),
    ('Primus', 'FIRST_SET_WIN', 'First set down - momentum building!', 'CELEBRATION', 'POSITIVE');

-- ═══════════════════════════════════════════════════════════════════════════════
-- KREEP'S SHADOW QUIPS
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO personality_quips (player_name, trigger_context, quip_text, category, tone) VALUES
    ('Kreep', 'MATCH_WINNER', 'From the shadows emerges... victory.', 'CELEBRATION', 'NEUTRAL'),
    ('Kreep', 'DOMINATING_WIN', 'Silent. Deadly. Dominant.', 'CELEBRATION', 'NEUTRAL'),
    ('Kreep', 'CLOSE_WIN', 'The Shadow strikes when least expected.', 'CELEBRATION', 'NEUTRAL');

-- Verify insertion
SELECT COUNT(*) as total_quips FROM personality_quips;
SELECT player_name, trigger_context, COUNT(*) as count 
FROM personality_quips 
GROUP BY player_name, trigger_context 
ORDER BY player_name, trigger_context;
