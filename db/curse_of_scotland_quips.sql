-- ═══════════════════════════════════════════════════════════════════════════════
-- CURSE OF SCOTLAND QUIPS  (9 of Diamonds)
-- trigger_context = 'CURSE_OF_SCOTLAND'
-- Run this against both local and Railway Postgres.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO personality_quips (player_name, trigger_context, quip_text, category, tone) VALUES

    -- Generic quips (fire for any player)
    (NULL, 'CURSE_OF_SCOTLAND', 'The Curse of Scotland! Legend has it this card sealed Scotland''s fate at Culloden...', 'LORE', 'NEUTRAL'),
    (NULL, 'CURSE_OF_SCOTLAND', '♦️ The Nine of Diamonds — Scotland''s curse rides again!', 'LORE', 'NEUTRAL'),
    (NULL, 'CURSE_OF_SCOTLAND', 'Beware the Nine of Diamonds... some say it brings ruin to those who hold it!', 'LORE', 'NEUTRAL'),
    (NULL, 'CURSE_OF_SCOTLAND', 'The Curse is loose! Who dares play the Nine of Diamonds?!', 'DRAMATIC', 'NEUTRAL'),
    (NULL, 'CURSE_OF_SCOTLAND', 'That''s the Curse of Scotland! Did you know it was found on the Duke of Cumberland''s orders at Culloden?', 'LORE', 'NEUTRAL'),
    (NULL, 'CURSE_OF_SCOTLAND', 'The infamous Nine of Diamonds... handle with care!', 'LORE', 'NEUTRAL'),
    (NULL, 'CURSE_OF_SCOTLAND', 'Scotland trembles at the sight of that card!', 'DRAMATIC', 'NEUTRAL'),
    (NULL, 'CURSE_OF_SCOTLAND', 'Aye, there she is — the cursed nine!', 'LORE', 'NEUTRAL'),

    -- Player-specific quips
    ('Dale',     'CURSE_OF_SCOTLAND', 'Classic Dale — bringing out the Curse of Scotland with surgical precision!', 'SIGNATURE', 'NEUTRAL'),
    ('Primus',   'CURSE_OF_SCOTLAND', 'Primus plays the Curse! Chaos is his ally tonight!', 'SIGNATURE', 'NEUTRAL'),
    ('Preezbob', 'CURSE_OF_SCOTLAND', 'Preezbob unleashes the Curse of Scotland! Is anything safe?!', 'SIGNATURE', 'NEUTRAL'),
    ('Kreep',    'CURSE_OF_SCOTLAND', 'From the shadows, Kreep produces the Nine of Diamonds... of course.', 'SIGNATURE', 'NEUTRAL');
