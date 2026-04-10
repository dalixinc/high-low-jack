--
-- PostgreSQL database dump
--

\restrict mob9dbuLVZs4vcrVVtfsNlbCUzDimytVwnxhIzhcvW8gCo9ZiL6joPRlVL2LS7Z

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: personality_quips; Type: TABLE DATA; Schema: public; Owner: highlowjack_user
--

INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (2, NULL, 'TIEBREAKER_WIN', 'Tied at 11? Time for the tiebreaker showdown!', 'CELEBRATION', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.155308', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (7, NULL, 'DOMINATING_WIN', 'Absolutely crushing it!', 'CELEBRATION', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.155308', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (8, NULL, 'DOMINATING_WIN', 'No mercy shown!', 'CELEBRATION', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.155308', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (18, NULL, 'COMEBACK_FROM_ZERO', 'THE COMEBACK IS REAL! Never give up!', 'EPIC', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.158675', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (19, NULL, 'COMEBACK_FROM_ZERO', 'From 0-10 to victory - legendary!', 'EPIC', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.158675', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (20, NULL, 'COMEBACK_FROM_ZERO', 'They counted you out, but you came back!', 'EPIC', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.158675', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (21, 'Dale', 'MATCH_WINNER', 'Strategy prevails - The Strategist claims victory!', 'CELEBRATION', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.159307', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (22, 'Dale', 'DOMINATING_WIN', 'Calculated. Precise. Victorious.', 'CELEBRATION', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.159307', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (23, 'Dale', 'CLOSE_WIN', 'Every move calculated - even the close ones!', 'CELEBRATION', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.159307', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (24, 'Primus', 'MATCH_WINNER', 'The student becomes the master!', 'CELEBRATION', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.159935', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (25, 'Primus', 'MATCH_WINNER', 'Learning pays off - victory is mine!', 'CELEBRATION', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.159935', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (26, 'Primus', 'FIRST_SET_WIN', 'First set down - momentum building!', 'CELEBRATION', 'POSITIVE', 0, NULL, '2026-04-05 16:13:31.159935', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (27, 'Kreep', 'MATCH_WINNER', 'From the shadows emerges... victory.', 'CELEBRATION', 'NEUTRAL', 0, NULL, '2026-04-05 16:13:31.160587', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (28, 'Kreep', 'DOMINATING_WIN', 'Silent. Deadly. Dominant.', 'CELEBRATION', 'NEUTRAL', 0, NULL, '2026-04-05 16:13:31.160587', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (29, 'Kreep', 'CLOSE_WIN', 'The Shadow strikes when least expected.', 'CELEBRATION', 'NEUTRAL', 0, NULL, '2026-04-05 16:13:31.160587', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (38, 'Preezbob', 'WIN_WITH_ACE_SPADES', 'Preezbob''s Revenge! The Ace of Africa seals the victory!', 'EPIC', 'POSITIVE', 0, NULL, '2026-04-05 16:17:18.212936', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (39, 'Preezbob', 'WIN_WITH_ACE_SPADES', 'Victory belongs to the Ace of Africa!', 'EPIC', 'POSITIVE', 0, NULL, '2026-04-05 16:17:18.212936', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (37, 'Preezbob', 'PLAY_ACE_SPADES', 'Preezbob unleashes the legendary Ace of Spades!', 'EPIC', 'POSITIVE', 9, '2026-04-07 23:11:27.18239', '2026-04-05 16:17:18.212936', true, true);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (30, 'Preezbob', 'CUT_TWO_LOSING', 'Classic Preezbob! Cutting twos when behind!', 'SIGNATURE', 'NEUTRAL', 0, NULL, '2026-04-05 16:17:18.212936', true, true);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (13, NULL, 'PERFECT_MATCH', 'UNDEFEATED! Not a single set lost!', 'EPIC', 'POSITIVE', 4, '2026-04-07 23:44:27.52387', '2026-04-05 16:13:31.157978', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (32, 'Preezbob', 'CUT_TWO_LOSING', 'Down but not out - Preezbob''s two says "I''m still here!"', 'SIGNATURE', 'POSITIVE', 0, NULL, '2026-04-05 16:17:18.212936', true, true);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (33, 'Preezbob', 'CUT_TWO_WINNING', 'Preezbob on FIRE! Even his twos are winners!', 'CELEBRATION', 'POSITIVE', 0, NULL, '2026-04-05 16:17:18.212936', true, true);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (34, 'Preezbob', 'CUT_TWO_WINNING', 'The Deuce Cutter doesn''t need high cards - a two will do!', 'CELEBRATION', 'POSITIVE', 0, NULL, '2026-04-05 16:17:18.212936', true, true);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (6, NULL, 'SWEEP_ALL_FOUR', 'Total domination - not even a contest!', 'CELEBRATION', 'POSITIVE', 6, '2026-04-07 23:12:07.727961', '2026-04-05 16:13:31.155308', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (16, NULL, 'MATCH_WINNER', 'Match won - glory earned!', 'CELEBRATION', 'POSITIVE', 6, '2026-04-07 23:44:27.526847', '2026-04-05 16:13:31.157978', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (1, NULL, 'TIEBREAKER_WIN', 'Down to the WIRE! Precedence rules FTW!', 'CELEBRATION', 'POSITIVE', 1, '2026-04-05 17:30:29.589002', '2026-04-05 16:13:31.155308', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (14, NULL, 'PERFECT_MATCH', 'Flawless victory - a perfect match!', 'EPIC', 'POSITIVE', 6, '2026-04-07 00:03:39.243167', '2026-04-05 16:13:31.157978', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (15, NULL, 'MATCH_WINNER', 'The champion emerges victorious!', 'CELEBRATION', 'POSITIVE', 3, '2026-04-07 23:27:43.44056', '2026-04-05 16:13:31.157978', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (31, 'Preezbob', 'CUT_TWO_LOSING', 'The Deuce Cutter strikes again - even from the depths of defeat!', 'SIGNATURE', 'NEUTRAL', 1, '2026-04-05 17:35:06.635602', '2026-04-05 16:17:18.212936', true, true);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (9, NULL, 'DOMINATING_WIN', 'That wasn''t even close!', 'CELEBRATION', 'POSITIVE', 2, '2026-04-05 20:43:17.321798', '2026-04-05 16:13:31.155308', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (17, NULL, 'MATCH_WINNER', 'And that''s game, set, and match!', 'CELEBRATION', 'POSITIVE', 2, '2026-04-07 16:09:52.491208', '2026-04-05 16:13:31.157978', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (35, 'Preezbob', 'PLAY_ACE_SPADES', 'THE ACE OF AFRICA STRIKES!', 'SIGNATURE', 'POSITIVE', 13, '2026-04-07 23:36:43.618463', '2026-04-05 16:17:18.212936', true, true);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (3, NULL, 'TIEBREAKER_WIN', 'High-Low-Jack-Game - precedence decides!', 'CELEBRATION', 'NEUTRAL', 3, '2026-04-05 19:11:09.088413', '2026-04-05 16:13:31.155308', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (36, 'Preezbob', 'PLAY_ACE_SPADES', '? Behold the Ace of Africa! ?', 'EPIC', 'POSITIVE', 6, '2026-04-07 15:47:25.234994', '2026-04-05 16:17:18.212936', true, true);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (12, NULL, 'CLOSE_WIN', 'Too close for comfort - but a win is a win!', 'CELEBRATION', 'POSITIVE', 4, '2026-04-07 23:44:22.71812', '2026-04-05 16:13:31.155308', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (5, NULL, 'SWEEP_ALL_FOUR', 'A clean sweep - High, Low, Jack, and Game!', 'CELEBRATION', 'POSITIVE', 5, '2026-04-07 23:10:16.001837', '2026-04-05 16:13:31.155308', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (11, NULL, 'CLOSE_WIN', 'What a nail-biter!', 'CELEBRATION', 'POSITIVE', 5, '2026-04-07 15:49:23.536596', '2026-04-05 16:13:31.155308', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (4, NULL, 'SWEEP_ALL_FOUR', 'FLAWLESS VICTORY! All four points!', 'CELEBRATION', 'POSITIVE', 9, '2026-04-07 23:44:27.50354', '2026-04-05 16:13:31.155308', true, false);
INSERT INTO public.personality_quips (id, player_name, trigger_context, quip_text, category, tone, times_used, last_used, created_at, is_active, is_realtime) VALUES (10, NULL, 'CLOSE_WIN', 'Victory snatched from the jaws of defeat!', 'CELEBRATION', 'POSITIVE', 5, '2026-04-07 23:44:27.520895', '2026-04-05 16:13:31.155308', true, false);


--
-- Name: personality_quips_id_seq; Type: SEQUENCE SET; Schema: public; Owner: highlowjack_user
--

SELECT pg_catalog.setval('public.personality_quips_id_seq', 39, true);


--
-- PostgreSQL database dump complete
--

\unrestrict mob9dbuLVZs4vcrVVtfsNlbCUzDimytVwnxhIzhcvW8gCo9ZiL6joPRlVL2LS7Z

