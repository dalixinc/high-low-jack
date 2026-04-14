package com.dalegames.highlowjack.web;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dalegames.highlowjack.SimpleAI;
import com.dalegames.highlowjack.engine.GameEngine;
import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.ChatMessage;
import com.dalegames.highlowjack.model.Deck;
import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.GameSetup;
import com.dalegames.highlowjack.model.Hand;
import com.dalegames.highlowjack.model.Match;
import com.dalegames.highlowjack.model.MatchResult;
import com.dalegames.highlowjack.model.PlayerInfo;
import com.dalegames.highlowjack.model.RoundResult;
import com.dalegames.highlowjack.model.SetResult;
import com.dalegames.highlowjack.model.Team;
import com.dalegames.highlowjack.model.Trick;
import com.dalegames.highlowjack.model.WrapUpInfo;
import com.dalegames.highlowjack.persistence.entity.Player;
import com.dalegames.highlowjack.persistence.entity.TeamStats;
import com.dalegames.highlowjack.persistence.service.HeadToHeadService;
import com.dalegames.highlowjack.persistence.service.PlayerService;
import com.dalegames.highlowjack.persistence.service.TeamStatsService;
import com.dalegames.highlowjack.service.QuipDetector;
import com.dalegames.highlowjack.service.RealtimeQuipDetector;

import jakarta.servlet.http.HttpSession;

/**
 * Web controller for High Low Jack card game.
 * 
 * @author Dale &amp; Primus
 * @version 8.14 - Head-to-head feature
 */
@Controller
@RequestMapping("/highlowjack")
public class HighLowJackController {
	
    @Value("${app.version:unknown}")
    private String appVersion;

    private static final int MAX_CHAT_LENGTH = 40;

    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private TeamStatsService teamStatsService;

    @Autowired
    private HeadToHeadService headToHeadService;

    @Autowired
    private QuipDetector quipDetector;
    
    @Autowired
    private RealtimeQuipDetector realtimeQuipDetector;
    
    @GetMapping
    public String showGame(Model model, HttpSession session) {
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        
        // No setup exists - redirect to setup screen
        if (setup == null) {
            return "redirect:/highlowjack/setup";
        }
        
        Game game = (Game) session.getAttribute("hlj_game");

        if (game == null) {
            return "redirect:/highlowjack/setup";
        }

        // If game hasn't started yet, go through cut ceremony first
        if (game.getState() == Game.GameState.NOT_STARTED) {
            return "redirect:/highlowjack/cut";
        }

        // Match is over — send all players to the winner page
        if (game.getState() == Game.GameState.MATCH_COMPLETE) {
            return "redirect:/highlowjack/match-winner";
        }

        // Set is over — send all players to the set winner page
        if (game.getState() == Game.GameState.SET_COMPLETE) {
            return "redirect:/highlowjack/set-winner";
        }

        // Check if we should show final trick before scoring
        Boolean showFinalTrick = (Boolean) session.getAttribute("hlj_showFinalTrick");

        // ── Stale-flag guard (multiplayer race condition) ────────────────────
        // In multiplayer the controller can advance past ROUND_COMPLETE before
        // this session's second request arrives, which means the removal at
        // line 110 below never runs and hlj_showFinalTrick persists into the
        // next round.  If the game is no longer ROUND_COMPLETE, the flag is
        // stale — clear it now so it never blocks completedTrick clearing.
        if (Boolean.TRUE.equals(showFinalTrick)
                && game.getState() != Game.GameState.ROUND_COMPLETE) {
            session.removeAttribute("hlj_showFinalTrick");
            showFinalTrick = null;
        }

        if (game.getState() == Game.GameState.ROUND_COMPLETE) {
            if (showFinalTrick != null && showFinalTrick) {
                // Flag is set - show the final trick, then let JavaScript redirect
                System.out.println("✅ SHOWING FINAL TRICK - JavaScript will redirect in 2 seconds");
                session.removeAttribute("hlj_showFinalTrick");
                model.addAttribute("showFinalTrick", true);
                // DON'T clear the completed trick - we want to see it!
                // DON'T return - continue to render the view below
                
                // ═══════════════════════════════════════════════════════════════
                // TEST: FORCE QUIPS TO DISPLAY ON GAME PAGE
                // ═══════════════════════════════════════════════════════════════
//                List<String> testQuips = new ArrayList<>();
//                testQuips.add("🔥 TEST QUIP - If you see this, the display works!");
//                testQuips.add("⚡ THE ACE OF AFRICA STRIKES!");
//                model.addAttribute("eventQuips", testQuips);
//                System.out.println("🧪 FORCED TEST QUIPS IN showGame(): " + testQuips);
                
            } else {
                // First time seeing ROUND_COMPLETE - set flag and show final trick
                System.out.println("🎯 ROUND COMPLETE - Setting showFinalTrick flag");
                session.setAttribute("hlj_showFinalTrick", true);
                session.setAttribute("hlj_game", game);
                System.out.println("🎯 Redirecting to /highlowjack to show final trick");
                return "redirect:/highlowjack";
            }
        }
        
        // Clear completed trick ONLY if NOT showing final trick
        Boolean shouldClearTrick = (Boolean) session.getAttribute("hlj_clearTrick");
        if (Boolean.TRUE.equals(shouldClearTrick) && 
            !(showFinalTrick != null && showFinalTrick)) {
            System.out.println("🧹 Clearing completed trick");
            game.clearCompletedTrick();
            session.removeAttribute("hlj_clearTrick");
        }
        
        Trick completedTrick = game.getCompletedTrick();
        
        if (completedTrick != null) {
            session.setAttribute("hlj_clearTrick", true);

            // ═══════════════════════════════════════════════════════════════
            // REALTIME QUIPS: Store in shared game so ALL humans see them
            // ═══════════════════════════════════════════════════════════════
            try {
                List<String> eventQuips = realtimeQuipDetector.checkRealtimeEvents(game);
                if (!eventQuips.isEmpty()) {
                    game.setPendingRealtimeQuips(eventQuips);  // shared with all sessions
                    System.out.println("⚡ REALTIME EVENT QUIPS: " + eventQuips);
                }
                game.clearRecentEvents();
            } catch (Exception e) {
                System.err.println("❌ Error checking realtime quips: " + e.getMessage());
            }
        } 
        
        
        
        else if (game.getState() == Game.GameState.IN_PROGRESS &&
                 !isCurrentPlayerHuman(game, setup)) {

            // ── Issue 2 fix: check wrap-up BEFORE playing AI card ───────────
            // When most/all players are AI the completed trick is cleared and AI
            // plays immediately, leaving no window for checkWrapUpLocked (which
            // guards against currentTrick.size() > 0). Pause here if a wrap-up
            // is already available so the controller can see the panel.
            boolean wrapUpPending = false;
            if (!game.isWrapUpDeclined()) {
                try {
                    wrapUpPending = GameEngine.checkWrapUpLocked(game) != null;
                } catch (Exception ignored) {}
            }

            if (!wrapUpPending) {
                playAITurn(game);

                completedTrick = game.getCompletedTrick();
                if (completedTrick != null) {
                    session.setAttribute("hlj_clearTrick", true);

                    // ── Issue 1 fix: check realtime events when trick completes via AI
                    // The top-level completedTrick block (above) is bypassed when the
                    // trick completes inside playAITurn, so we mirror the check here.
                    try {
                        List<String> eventQuips = realtimeQuipDetector.checkRealtimeEvents(game);
                        if (!eventQuips.isEmpty()) {
                            game.setPendingRealtimeQuips(eventQuips);
                            System.out.println("⚡ REALTIME EVENT QUIPS (AI trick): " + eventQuips);
                        }
                        game.clearRecentEvents();
                    } catch (Exception e) {
                        System.err.println("❌ Error checking realtime quips (AI trick): " + e.getMessage());
                    }
                }

                if (game.getState() == Game.GameState.ROUND_COMPLETE) {
                    System.out.println("🎯 AI completed round - setting showFinalTrick flag");
                    session.setAttribute("hlj_showFinalTrick", true);
                    session.setAttribute("hlj_game", game);
                    return "redirect:/highlowjack";
                }
            }

            session.setAttribute("hlj_game", game);
        }
        
        String humanPlayer = (String) session.getAttribute("hlj_playerName");
        if (humanPlayer == null) {
            humanPlayer = getHumanPlayerName(setup);
        }
        List<Card> validCards = calculateValidCards(game, humanPlayer);
        boolean isAITurn = !isCurrentPlayerHuman(game, setup);
        boolean isMultiplayer = session.getAttribute("hlj_playerName") != null;
        boolean isMyTurn = game.getCurrentPlayer() != null && game.getCurrentPlayer().equals(humanPlayer);
        
        Card.Suit leadSuit = null;
        if (game.getCurrentTrick() != null && game.getCurrentTrick().size() > 0) {
            leadSuit = game.getCurrentTrick().getLeadSuit();
        }
        
        Map<String, String> pointStatus = GameEngine.getCurrentPointStatus(game);
        
        model.addAttribute("game", game);
        model.addAttribute("setup", setup);
        model.addAttribute("humanPlayer", humanPlayer);
        model.addAttribute("isController", setup.isController(humanPlayer));
        model.addAttribute("isAITurn", isAITurn);
        model.addAttribute("isMultiplayer", isMultiplayer);
        model.addAttribute("chatMaxLength", MAX_CHAT_LENGTH);
        model.addAttribute("chatLocked", game.isChatLocked());
        // Human player names for the targeted-message recipient picker
        List<String> humanPlayerNames = setup.getPlayers().stream()
                .filter(p -> p.getType() == PlayerInfo.PlayerType.HUMAN)
                .map(PlayerInfo::getName)
                .collect(Collectors.toList());
        model.addAttribute("humanPlayerNames", humanPlayerNames);
        model.addAttribute("isMyTurn", isMyTurn);
        model.addAttribute("completedTrick", completedTrick);
        model.addAttribute("validCards", validCards);
        model.addAttribute("leadSuit", leadSuit);
        model.addAttribute("pointStatus", pointStatus);

        // PHASE 5: Add pitcher name
        String pitcherName = game.getPitcherName();
        model.addAttribute("pitcherName", pitcherName);
        
        // PHASE 5: Calculate tricks won per player
        Map<String, Integer> tricksWon = new HashMap<>();
        for (String player : game.getPlayerNames()) {
            int tricks = 0;
            for (Trick trick : game.getTricks()) {
                if (trick.getWinner().equals(player)) {
                    tricks++;
                }
            }
            tricksWon.put(player, tricks);
        }
        model.addAttribute("tricksWon", tricksWon);

        // Round number comes from the shared Game object — accurate for all players in multiplayer
        model.addAttribute("roundNumber", game.getRoundNumber());
        model.addAttribute("setsWon", game.getSetsWon());

        // Realtime quips: read from shared game so all humans (not just the card-player) see them
        List<String> pendingQuips = game.getAndMaybeExpireRealtimeQuips();
        if (pendingQuips != null && !pendingQuips.isEmpty()) {
            model.addAttribute("eventQuips", pendingQuips);
        }

        // Trump tracker: compute which trump ranks have been played
        model.addAttribute("trumpTrackerEnabled", game.isTrumpTrackerEnabled());
        if (game.isTrumpTrackerEnabled() && game.getTrumpSuit() != null) {
            model.addAttribute("trumpSuit", game.getTrumpSuit());
            model.addAttribute("playedTrumpRanks", computePlayedTrumpRanks(game));
            model.addAttribute("playedTrumpInfo", computePlayedTrumpInfo(game));
        }

        // Early wrap-up: check if High/Low/Jack are locked and a set winner is guaranteed.
        // Suppressed for this round if the controller already declined.
        // NOTE: We intentionally do NOT gate this on completedTrick==null — the panel
        // should also appear while the just-completed trick is still being displayed,
        // so fast-clicking controllers don't miss the window (especially in multiplayer).
        // checkWrapUpLocked() has its own guard that returns null when a trick is mid-flight
        // (currentTrick.size() > 0), so it is safe to call here unconditionally.
        WrapUpInfo wrapUp = null;
        if (game.getState() == Game.GameState.IN_PROGRESS && !game.isWrapUpDeclined()) {
            try {
                wrapUp = GameEngine.checkWrapUpLocked(game);
            } catch (Exception e) {
                System.err.println("❌ Error checking wrap-up: " + e.getMessage());
            }
        }
        model.addAttribute("wrapUpInfo", wrapUp);
        model.addAttribute("wrapUpRequested", game.isWrapUpRequested());

        return "highlowjack/game";
    }

    /** Returns the set of trump-card rank names (e.g. "ACE", "TEN") already on the table or in won tricks. */
    private java.util.Set<String> computePlayedTrumpRanks(Game game) {
        java.util.Set<String> played = new java.util.LinkedHashSet<>();
        Card.Suit trump = game.getTrumpSuit();
        if (trump == null) return played;
        // Completed tricks
        for (Trick trick : game.getTricks()) {
            for (var play : trick.getPlays()) {
                if (play.card.getSuit() == trump) {
                    played.add(play.card.getRank().name());
                }
            }
        }
        // Current trick (cards already on the table this round)
        if (game.getCurrentTrick() != null) {
            for (var play : game.getCurrentTrick().getPlays()) {
                if (play.card.getSuit() == trump) {
                    played.add(play.card.getRank().name());
                }
            }
        }
        return played;
    }

    /** Returns a map of trump rank name → "Played by [playerName]" for tooltip display. */
    private java.util.Map<String, String> computePlayedTrumpInfo(Game game) {
        java.util.Map<String, String> info = new java.util.LinkedHashMap<>();
        Card.Suit trump = game.getTrumpSuit();
        if (trump == null) return info;
        // Completed tricks
        for (Trick trick : game.getTricks()) {
            for (var play : trick.getPlays()) {
                if (play.card.getSuit() == trump) {
                    info.put(play.card.getRank().name(), "Played by " + play.playerName);
                }
            }
        }
        // Current trick
        if (game.getCurrentTrick() != null) {
            for (var play : game.getCurrentTrick().getPlays()) {
                if (play.card.getSuit() == trump) {
                    info.put(play.card.getRank().name(), "Played by " + play.playerName);
                }
            }
        }
        return info;
    }
    
    @GetMapping("/setup")
    public String showSetup(Model model) {
        model.addAttribute("appVersion", appVersion);
        model.addAttribute("buzzMessages", generateBuzzMessages());
        return "highlowjack/setup";
    }

    /**
     * Generates a list of fun, stat-driven buzz messages for the setup screen.
     * Falls back to generic welcome messages when there is no history yet.
     */
    private List<String> generateBuzzMessages() {
        List<String> msgs = new ArrayList<>();
        try {
            List<com.dalegames.highlowjack.persistence.entity.Player> all =
                    playerService.getAllPlayers();
            List<com.dalegames.highlowjack.persistence.entity.Player> players =
                    all.stream().filter(p -> !p.isTeam()).collect(Collectors.toList());

            for (com.dalegames.highlowjack.persistence.entity.Player p : players) {
                int streak = p.getCurrentWinStreak();
                if (streak >= 6)
                    msgs.add("🔥🔥🔥 " + p.getName() + " is ABSOLUTELY ON FIRE — " + streak + " wins in a row!");
                else if (streak >= 4)
                    msgs.add("🔥🔥 " + p.getName() + " is cooking with gas — " + streak + "-match win streak!");
                else if (streak >= 2)
                    msgs.add("🔥 " + p.getName() + " is on a roll — " + streak + " wins in a row!");

                if (p.getTotalTwosCut() >= 4)
                    msgs.add("✂️ " + p.getName() + " keeps cutting twos — " + p.getTotalTwosCut() + " times and counting!");
                else if (p.getTotalTwosCut() >= 2)
                    msgs.add("✂️ " + p.getName() + " is on a two-cutting binge — " + p.getTotalTwosCut() + " twos cut!");

                if (p.getTotalAcesCut() >= 3)
                    msgs.add("🃏 " + p.getName() + " keeps cutting Aces — lucky star or dark art?");

                if (p.getTotalMatchesPlayed() >= 5 && p.getWinPercentage() >= 70)
                    msgs.add("👑 " + p.getName() + " dominates at "
                            + String.format("%.0f", p.getWinPercentage()) + "% — who can stop them?");

                if (p.getSweepsWon() >= 3)
                    msgs.add("🧹 " + p.getName() + " has swept the board " + p.getSweepsWon() + " times — ruthless!");

                if (p.getCloseSetWins() >= 3)
                    msgs.add("🎯 " + p.getName() + " is ice-cold in the clutch — " + p.getCloseSetWins() + " close-set wins!");

                if (p.getFailedFrom10() >= 3)
                    msgs.add("😰 " + p.getName() + " keeps blowing leads from 10... will today be different?");

                if (p.getTotalAceSpadesPlayed() >= 3)
                    msgs.add("♠️ " + p.getName() + " loves dropping the Ace of Spades — " + p.getTotalAceSpadesPlayed() + " times!");
            }

            // Single "leader" shout-out if not already mentioned
            players.stream()
                    .filter(p -> p.getTotalMatchesPlayed() >= 5)
                    .max(Comparator.comparingInt(
                            com.dalegames.highlowjack.persistence.entity.Player::getTotalMatchesWon))
                    .ifPresent(p -> {
                        boolean alreadyCovered = msgs.stream().anyMatch(m -> m.contains(p.getName()));
                        if (!alreadyCovered)
                            msgs.add("👑 " + p.getName() + " leads the all-time table with "
                                    + p.getTotalMatchesWon() + " match wins!");
                    });

            // Iron Man
            players.stream()
                    .filter(p -> p.getTotalMatchesPlayed() >= 10)
                    .max(Comparator.comparingInt(
                            com.dalegames.highlowjack.persistence.entity.Player::getTotalMatchesPlayed))
                    .ifPresent(p -> msgs.add("💪 " + p.getName() + " — Iron Man! "
                            + p.getTotalMatchesPlayed() + " matches played and still standing!"));

        } catch (Exception e) {
            // DB not available or empty — fall through to defaults below
        }

        if (msgs.isEmpty()) {
            msgs.add("🃏 New game, new glory — who will rise to the top?");
            msgs.add("⚡ High Low Jack — where legends are made!");
            msgs.add("♠️ Shuffle up and deal — let the cards decide!");
        }
        return msgs;
    }
    
    @PostMapping("/setup")
    public String processSetup(
            @RequestParam(required = false) String gameMode,
            // Individual mode parameters
            @RequestParam(required = false) String player1Name,
            @RequestParam(required = false) String player2Name,
            @RequestParam(required = false) String player3Name,
            @RequestParam(required = false) String player4Name,
            @RequestParam(required = false) PlayerInfo.PlayerType player1Type,
            @RequestParam(required = false) PlayerInfo.PlayerType player2Type,
            @RequestParam(required = false) PlayerInfo.PlayerType player3Type,
            @RequestParam(required = false) PlayerInfo.PlayerType player4Type,
            // Team mode parameters
            @RequestParam(required = false) String player1NameTeam,
            @RequestParam(required = false) String player2NameTeam,
            @RequestParam(required = false) String player3NameTeam,
            @RequestParam(required = false) String player4NameTeam,
            @RequestParam(required = false) PlayerInfo.PlayerType player1TypeTeam,
            @RequestParam(required = false) PlayerInfo.PlayerType player2TypeTeam,
            @RequestParam(required = false) PlayerInfo.PlayerType player3TypeTeam,
            @RequestParam(required = false) PlayerInfo.PlayerType player4TypeTeam,
            // Team name parameters
            @RequestParam(required = false) String team1Name,
            @RequestParam(required = false) String team2Name,
            @RequestParam GameSetup.MatchType matchType,
            @RequestParam(required = false, defaultValue = "false") boolean trumpTrackerEnabled,
            HttpSession session) {
        
        // Determine which mode and use appropriate parameters
        boolean isTeamMode = "TEAM".equals(gameMode);
        
        String p1Name, p2Name, p3Name, p4Name;
        PlayerInfo.PlayerType p1Type, p2Type, p3Type, p4Type;
        
        if (isTeamMode) {
            // Use team mode parameters
            p1Name = player1NameTeam;
            p2Name = player2NameTeam;
            p3Name = player3NameTeam;
            p4Name = player4NameTeam;
            p1Type = player1TypeTeam;
            p2Type = player2TypeTeam;
            p3Type = player3TypeTeam;
            p4Type = player4TypeTeam;
        } else {
            // Use individual mode parameters
            p1Name = player1Name;
            p2Name = player2Name;
            p3Name = player3Name;
            p4Name = player4Name;
            p1Type = player1Type;
            p2Type = player2Type;
            p3Type = player3Type;
            p4Type = player4Type;
        }
        
        // Create player info list (same for both modes)
        List<PlayerInfo> players = new ArrayList<>();
        players.add(new PlayerInfo(p1Name, p1Type, true));  // Player 1 is controller
        players.add(new PlayerInfo(p2Name, p2Type, false));
        players.add(new PlayerInfo(p3Name, p3Type, false));
        players.add(new PlayerInfo(p4Name, p4Type, false));
        
        // Create game setup based on mode
        GameSetup setup;
        if (isTeamMode) {
            // Use custom team names or defaults
            String t1Name = (team1Name != null && !team1Name.trim().isEmpty())
                            ? team1Name.trim() : "North-South";
            String t2Name = (team2Name != null && !team2Name.trim().isEmpty())
                            ? team2Name.trim() : "East-West";

            System.out.println("🏆 Creating team mode with custom names: " + t1Name + " vs " + t2Name);
            setup = GameSetup.createTeam(players, matchType, t1Name, t2Name);
        } else {
            setup = GameSetup.createIndividual(players, matchType);
        }
        setup.setTrumpTrackerEnabled(trumpTrackerEnabled);
        
        // Store in session and clear any existing game
        session.setAttribute("hlj_setup", setup);
        session.removeAttribute("hlj_game");
        session.removeAttribute("hlj_clearTrick");

        // Create match tracker
        Match match = new Match(matchType);
        session.setAttribute("hlj_match", match);

        // Create game (NOT_STARTED — cut ceremony will deal the cards)
        Game game = new Game(setup);
        session.setAttribute("hlj_game", game);

        return "redirect:/highlowjack/cut";
    }
    
    // ── Early wrap-up endpoints ──────────────────────────────────────────────

    /**
     * Controller claims the set early. Applies only the locked points (High/Low/Jack),
     * marks the round as scored, and redirects to the scoring page.
     */
    @PostMapping("/claim-set")
    public String claimSet(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        if (game == null || setup == null) return "redirect:/highlowjack/setup";
        if (game.getState() != Game.GameState.IN_PROGRESS) return "redirect:/highlowjack";

        // Validate controller
        String humanPlayer = (String) session.getAttribute("hlj_playerName");
        if (humanPlayer == null) humanPlayer = getHumanPlayerName(setup);
        if (!setup.isController(humanPlayer)) return "redirect:/highlowjack";

        // Re-check wrap-up is still valid
        WrapUpInfo wrapUp = GameEngine.checkWrapUpLocked(game);
        if (wrapUp == null) return "redirect:/highlowjack"; // No longer valid

        // Apply locked points (capped at 11 per entity)
        Map<String, String> lockedResults = new HashMap<>();
        applyLockedPoint(game, wrapUp.getHighWinner(), "High", lockedResults);
        applyLockedPoint(game, wrapUp.getLowWinner(),  "Low",  lockedResults);
        applyLockedPoint(game, wrapUp.getJackWinner(), "Jack", lockedResults);

        // Mark scoring done (guard against double-scoring in scoring page)
        game.setLastRoundScores(lockedResults);
        game.setRoundScoresApplied(true);
        game.setWrapUpRequested(false);
        game.setState(Game.GameState.ROUND_COMPLETE);
        session.setAttribute("hlj_game", game);

        System.out.println("✂️ Early wrap-up claimed by " + humanPlayer + ": " + lockedResults);
        return "redirect:/highlowjack/scoring";
    }

    /** Apply one locked point for an entity, capped so the score doesn't exceed 11. */
    private void applyLockedPoint(Game game, String entity, String category,
                                   Map<String, String> results) {
        if (entity == null) return;
        results.put(category, entity);   // Always record winner for display
        if (game.getScore(entity) < 11) {
            game.addScore(entity, 1);
        }
    }

    /** Non-controller: flag a wrap-up request visible to the controller. */
    @PostMapping("/request-wrap-up")
    @ResponseBody
    public Map<String, Object> requestWrapUp(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        if (game == null || game.getState() != Game.GameState.IN_PROGRESS)
            return Map.of("ok", false);
        game.setWrapUpRequested(true);
        return Map.of("ok", true);
    }

    /** Controller: decline wrap-up for this round; panel hidden until next round. */
    @PostMapping("/decline-wrap-up")
    @ResponseBody
    public Map<String, Object> declineWrapUp(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        if (game == null) return Map.of("ok", false);
        game.setWrapUpDeclined(true);
        game.setWrapUpRequested(false);
        return Map.of("ok", true);
    }

    /** Controller-only: toggle the trump tracker on/off mid-game. */
    @PostMapping("/toggle-trump-tracker")
    @ResponseBody
    public Map<String, Object> toggleTrumpTracker(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        if (game == null) return Map.of("ok", false);
        game.setTrumpTrackerEnabled(!game.isTrumpTrackerEnabled());
        return Map.of("ok", true, "enabled", game.isTrumpTrackerEnabled());
    }

    @PostMapping("/play")
    public String playCard(@RequestParam int cardIndex, HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        
        if (game != null && setup != null && game.getState() == Game.GameState.IN_PROGRESS) {
            
            // Get THIS session's player name
            String humanPlayer = (String) session.getAttribute("hlj_playerName");
            if (humanPlayer == null) {
                humanPlayer = getHumanPlayerName(setup);
            }
            
            // Check if it's this player's turn
            if (!game.getCurrentPlayer().equals(humanPlayer)) {
                // Not this player's turn - just refresh
                return "redirect:/highlowjack";
            }
            
            // It's their turn - get their hand
            Hand hand = game.getHand(humanPlayer);
            
            if (hand != null && cardIndex >= 0 && cardIndex < hand.getCards().size()) {
                Card card = hand.getCards().get(cardIndex);
                
                if (GameEngine.isValidPlay(game, humanPlayer, card)) {
                    game.playCard(card);
                    session.setAttribute("hlj_game", game);
                }
            }
        }
        
        return "redirect:/highlowjack";
    }
    
    @PostMapping("/new")
    public String newGame(HttpSession session) {
        // Clear game but keep setup - redirect to setup screen
        session.removeAttribute("hlj_game");
        session.removeAttribute("hlj_clearTrick");
        return "redirect:/highlowjack/setup";
    }

    /**
     * Rematch: reuse the existing GameSetup to start a brand-new game immediately.
     */
    @PostMapping("/rematch")
    public String rematch(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        if (setup == null) {
            return "redirect:/highlowjack/setup";
        }

        // Create the new match
        Match match = new Match(setup.getMatchType());

        if (game != null) {
            // Reset the EXISTING game object in place — preserves the shared reference
            // that all multiplayer sessions hold, so every session automatically sees the reset.
            game.resetForRematch(match);
        } else {
            // Fallback for single-player if game somehow disappeared
            game = new Game(setup);
            game.setCurrentMatch(match);
            session.setAttribute("hlj_game", game);
        }

        // Clear per-round/per-set session remnants from this session
        session.removeAttribute("hlj_roundResult");
        session.removeAttribute("hlj_roundNumber");
        session.removeAttribute("hlj_setResult");
        session.removeAttribute("hlj_matchResult");
        session.setAttribute("hlj_match", match);

        System.out.println("🔄 Rematch started");
        return "redirect:/highlowjack/cut";
    }
    
    @PostMapping("/sort-hand")
    public String sortHand(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        
        if (game != null && setup != null) {
            String humanPlayer = (String) session.getAttribute("hlj_playerName");
            if (humanPlayer == null) humanPlayer = getHumanPlayerName(setup);
            Hand hand = game.getHand(humanPlayer);
            hand.sort();
            session.setAttribute("hlj_game", game);
        }
        
        return "redirect:/highlowjack";
    }
    
    @GetMapping("/scoring")
    public String showScoring(Model model, HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        
        if (game == null || setup == null) {
            return "redirect:/highlowjack/setup";
        }
        
        if (game.getState() != Game.GameState.ROUND_COMPLETE) {
            return "redirect:/highlowjack";
        }
        
        RoundResult results = GameEngine.calculateRoundResults(game);
        session.setAttribute("hlj_roundResult", results);  // Store for continueGame()
        
        // Check for set winner using tiebreaker logic
        // TEAM MODE FIX: Calculate team scores or player scores depending on mode
        Map<String, Integer> scoresBefore = new HashMap<>();
        
        if (game.isTeamMode()) {
            // Team mode: calculate team scores before this round
            for (Team team : game.getTeams()) {
                String teamName = team.getName();
                int currentScore = game.getScore(teamName);
                int roundPoints = 0;
                
                // Count points awarded to this team this round
                for (String category : new String[]{"High", "Low", "Jack", "Game"}) {
                    String winner = results.getRoundPointWinner(category);
                    if (winner != null) {
                        // Check if winner is team name OR a player on this team
                        if (teamName.equals(winner) || team.hasPlayer(winner)) {
                            roundPoints++;
                        }
                    }
                }
                scoresBefore.put(teamName, currentScore - roundPoints);
            }
        } else {
            // Individual mode: calculate player scores before this round
            for (String player : game.getPlayerNames()) {
                int currentScore = game.getScore(player);
                int roundPoints = 0;
                for (String category : new String[]{"High", "Low", "Jack", "Game"}) {
                    if (player.equals(results.getRoundPointWinner(category))) {
                        roundPoints++;
                    }
                }
                scoresBefore.put(player, currentScore - roundPoints);
            }
        }
        
        SetResult setResult = SetResult.determineWinner(scoresBefore, results.getRoundPointWinners());

	     // ═══════════════════════════════════════════════════════════════
	     // PERSONALITY: Set completion quips
	     // ═══════════════════════════════════════════════════════════════
	     try {
	         List<String> setQuips = quipDetector.checkSetQuips(game, setResult);
	         if (!setQuips.isEmpty()) {
	             model.addAttribute("setQuips", setQuips);
	             System.out.println("🎭 SET QUIPS: " + setQuips);
	         }
	     } catch (Exception e) {
	         System.err.println("❌ Error checking set quips: " + e.getMessage());
	     }

     // ... rest of the code continues
        
     // DEBUG: Print set result in showScoring
        boolean doADebug = true;
        if (game.isTeamMode() && doADebug) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║  SHOW SCORING - SET RESULT CHECK    ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.println("CURRENT SCORES:");
            for (Team team : game.getTeams()) {
                System.out.println("  " + team.getName() + ": " + game.getScore(team.getName()));
            }
            System.out.println("\nSCORES BEFORE THIS ROUND:");
            for (Map.Entry<String, Integer> entry : scoresBefore.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("\nROUND POINT WINNERS:");
            for (String category : new String[]{"High", "Low", "Jack", "Game"}) {
                String winner = results.getRoundPointWinner(category);
                System.out.println("  " + category + ": " + (winner != null ? winner : "none"));
            }
            System.out.println("\nSET RESULT:");
            if (setResult != null) {
                System.out.println("  ✅ WINNER: " + setResult.getWinner());
                System.out.println("  WINNING POINT: " + setResult.getWinningPoint());
                System.out.println("  WAS TIEBREAKER: " + setResult.wasTiebreaker());
            } else {
                System.out.println("  ❌ NO WINNER YET");
            }
            System.out.println("════════════════════════════════════════\n");
        }

        
        String scoringHumanPlayer = (String) session.getAttribute("hlj_playerName");
        boolean isController;
        if (scoringHumanPlayer == null) {
            isController = true; // local game — always controller
        } else {
            isController = setup.getPlayers().stream()
                .anyMatch(p -> p.getName().equals(scoringHumanPlayer) && p.isController());
        }

        // PHASE 5: Add pitcher name
        model.addAttribute("pitcherName", game.getPitcherName());
        
        // PHASE 5: Calculate tricks won
        Map<String, Integer> tricksWon = new HashMap<>();
        for (String player : game.getPlayerNames()) {
            int tricks = 0;
            for (Trick trick : game.getTricks()) {
                if (trick.getWinner().equals(player)) {
                    tricks++;
                }
            }
            tricksWon.put(player, tricks);
        }
        model.addAttribute("tricksWon", tricksWon);
        
        model.addAttribute("game", game);
        model.addAttribute("setup", setup);
        model.addAttribute("results", results);
        model.addAttribute("setResult", setResult);
        model.addAttribute("isController", isController);
        model.addAttribute("playerNames", game.getPlayerNames());
        model.addAttribute("currentSetNumber", game.getCurrentSetNumber());
        model.addAttribute("setsWon", game.getSetsWon());
        model.addAttribute("winningScore", 11);
        
        session.setAttribute("hlj_game", game);

        // ── Trick log: pre-process all 7 tricks for the summary display ──────────
        List<Map<String, Object>> trickSummary = new ArrayList<>();
        Card.Suit trumpSuit = game.getTrumpSuit();
        Card highCard = (trumpSuit != null) ? GameEngine.findHighTrumpCard(game.getTricks(), trumpSuit) : null;
        Card lowCard  = (trumpSuit != null) ? GameEngine.findLowTrumpCard (game.getTricks(), trumpSuit) : null;

        int trickNum = 1;
        for (Trick trick : game.getTricks()) {
            Map<String, Object> td = new HashMap<>();
            String trickWinner = trick.getWinner();
            td.put("number", trickNum++);
            td.put("winner", trickWinner);
            if (game.isTeamMode()) {
                try { td.put("winnerTeam", game.getTeamForPlayer(trickWinner).getName()); }
                catch (Exception ignored) {}
            }
            boolean tHasJack = false, tHasHigh = false, tHasLow = false;
            List<Map<String, Object>> plays = new ArrayList<>();
            for (Trick.CardPlay play : trick.getPlays()) {
                Map<String, Object> pd = new HashMap<>();
                pd.put("player", play.playerName);
                pd.put("cardImage", CardImageHelper.getCardImage(play.card));
                boolean isTrump = trumpSuit != null && play.card.getSuit() == trumpSuit;
                boolean isJack  = isTrump && play.card.getRank() == Card.Rank.JACK;
                boolean isHigh  = isTrump && highCard != null && play.card.getRank() == highCard.getRank();
                boolean isLow   = isTrump && lowCard  != null && play.card.getRank() == lowCard.getRank();
                boolean isWin   = play.playerName.equals(trickWinner);
                pd.put("isTrump", isTrump);
                pd.put("isJack",  isJack);
                pd.put("isHigh",  isHigh);
                pd.put("isLow",   isLow);
                pd.put("isWinner", isWin);
                pd.put("label",   play.card.getRank().name() + play.card.getSuit().getSymbol());
                if (isJack) tHasJack = true;
                if (isHigh) tHasHigh = true;
                if (isLow)  tHasLow  = true;
                plays.add(pd);
            }
            td.put("plays", plays);
            td.put("hasJack", tHasJack);
            td.put("hasHigh", tHasHigh);
            td.put("hasLow",  tHasLow);
            trickSummary.add(td);
        }
        model.addAttribute("trickSummary", trickSummary);

        // ── Round points this round per entity (for the bold result banner) ──────
        Map<String, Integer> roundPointsEarned = new java.util.LinkedHashMap<>();
        for (String category : new String[]{"High", "Low", "Jack", "Game"}) {
            String w = results.getRoundPointWinner(category);
            if (w != null) roundPointsEarned.merge(w, 1, Integer::sum);
        }
        // Ensure every scoring entity appears (with 0 if needed) for the banner
        if (game.isTeamMode()) {
            for (Team t : game.getTeams()) {
                roundPointsEarned.putIfAbsent(t.getName(), 0);
            }
        } else {
            for (String pn : game.getPlayerNames()) {
                roundPointsEarned.putIfAbsent(pn, 0);
            }
        }
        model.addAttribute("roundPointsEarned", roundPointsEarned);

        return "highlowjack/scoring";
    }
    
    /**
     * Shows player statistics and leaderboard.
     */
    @GetMapping("/stats")
    public String showStats(Model model) {
        try {
            // Get PLAYER stats only (excluding teams)
            List<Player> players = playerService.getPlayerLeaderboard();
            
            // Calculate summary stats
            int totalMatches = playerService.getTotalMatches();
            
            int totalPoints = players.stream()
                .mapToInt(p -> p.getHighsWon() + p.getLowsWon() + 
                              p.getJacksWon() + p.getGamesWon())
                .sum();
            
            // Get team stats
            List<TeamStats> teams = teamStatsService.getLeaderboard();

            // Awards: Hall of Fame / Hall of Shame + four new medals
            Player topAceCutter = players.stream()
                .filter(p -> p.getTotalAcesCut() > 0)
                .max(java.util.Comparator.comparingInt(Player::getTotalAcesCut))
                .orElse(null);
            Player topTwoCutter = players.stream()
                .filter(p -> p.getTotalTwosCut() > 0)
                .max(java.util.Comparator.comparingInt(Player::getTotalTwosCut))
                .orElse(null);
            Player topSweeper = players.stream()
                .filter(p -> p.getSweepsWon() > 0)
                .max(java.util.Comparator.comparingInt(Player::getSweepsWon))
                .orElse(null);
            Player topCloser = players.stream()
                .filter(p -> p.getCloseSetWins() > 0)
                .max(java.util.Comparator.comparingInt(Player::getCloseSetWins))
                .orElse(null);
            Player topChoker = players.stream()
                .filter(p -> p.getFailedFrom10() > 0)
                .max(java.util.Comparator.comparingInt(Player::getFailedFrom10))
                .orElse(null);
            Player ironMan = players.stream()
                .filter(p -> !p.isTeam() && p.getTotalMatchesPlayed() > 0)
                .max(java.util.Comparator.comparingInt(Player::getTotalMatchesPlayed))
                .orElse(null);

            // Head-to-head grid
            List<String> playerNames = players.stream()
                .map(Player::getName)
                .collect(java.util.stream.Collectors.toList());
            List<List<com.dalegames.highlowjack.model.H2HCell>> h2hGrid = headToHeadService.buildGrid(playerNames);

            model.addAttribute("players", players);
            model.addAttribute("teams", teams);
            model.addAttribute("totalMatches", totalMatches);
            model.addAttribute("totalPoints", totalPoints);
            model.addAttribute("topAceCutter", topAceCutter);
            model.addAttribute("topTwoCutter", topTwoCutter);
            model.addAttribute("topSweeper", topSweeper);
            model.addAttribute("topCloser", topCloser);
            model.addAttribute("topChoker", topChoker);
            model.addAttribute("ironMan", ironMan);
            model.addAttribute("h2hGrid", h2hGrid);
            model.addAttribute("playerNames", playerNames);
            
            return "highlowjack/stats";
        } catch (Exception e) {
            System.err.println("❌ Error loading stats: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("players", new ArrayList<>());
            model.addAttribute("teams", new ArrayList<>());
            model.addAttribute("totalMatches", 0);
            model.addAttribute("totalPoints", 0);
            model.addAttribute("topAceCutter", null);
            model.addAttribute("topTwoCutter", null);
            model.addAttribute("topSweeper", null);
            model.addAttribute("topCloser", null);
            model.addAttribute("topChoker", null);
            model.addAttribute("ironMan", null);
            model.addAttribute("h2hGrid", new java.util.LinkedHashMap<>());
            model.addAttribute("playerNames", new ArrayList<>());
            return "highlowjack/stats";
        }
    }
    
    /**
     * Polling endpoint for multiplayer turn detection.
     * Returns the current player and game state so clients can detect when it's their turn.
     */
    @GetMapping("/poll")
    @ResponseBody
    public Map<String, Object> pollGameState(
            @RequestParam(defaultValue = "0") int lastChat,
            HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        String humanPlayer = (String) session.getAttribute("hlj_playerName");
        Map<String, Object> response = new HashMap<>();
        if (game != null) {
            response.put("currentPlayer", game.getCurrentPlayer());
            response.put("gameState", game.getState().name());
            // Extra state for real-time card-play detection
            int trickSize = (game.getCurrentTrick() != null) ? game.getCurrentTrick().size() : 0;
            response.put("currentTrickSize", trickSize);
            response.put("completedTrickCount", game.getTricks().size());
            // Cut ceremony state (for cross-session coordination)
            response.put("cutCardsDrawn", game.getCutCard1() != null && !game.isCutTied());
            response.put("cut1Revealed", game.isCutPlayer1Revealed());
            response.put("cut2Revealed", game.isCutPlayer2Revealed());
            // Early wrap-up state
            boolean wrapAvail = false;
            if (!game.isWrapUpDeclined()) {
                try {
                    wrapAvail = GameEngine.checkWrapUpLocked(game) != null;
                } catch (Exception ignored) {}
            }
            response.put("wrapUpAvailable", wrapAvail);
            response.put("wrapUpRequested", game.isWrapUpRequested());
            // Chat — return new messages visible to this player
            List<ChatMessage> newMsgs = game.getChatSince(lastChat);
            List<Map<String, Object>> chatPayload = new ArrayList<>();
            final String hp = humanPlayer;
            for (ChatMessage msg : newMsgs) {
                // Show: broadcasts, messages sent by me, messages targeted at me
                boolean visible = msg.getRecipient() == null
                        || (hp != null && hp.equals(msg.getRecipient()))
                        || (hp != null && hp.equals(msg.getPlayerName()));
                if (visible) chatPayload.add(msg.toMap());
            }
            response.put("chatMessages", chatPayload);
            response.put("chatVersion", game.getChatVersion());
            response.put("chatLocked", game.isChatLocked());
        }
        response.put("humanPlayer", humanPlayer);
        return response;
    }

    @PostMapping("/chat")
    @ResponseBody
    public Map<String, Object> sendChatMessage(
            @RequestParam String text,
            @RequestParam(defaultValue = "TEXT") String type,
            @RequestParam(defaultValue = "ALL") String target,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Game game = (Game) session.getAttribute("hlj_game");
        String playerName = (String) session.getAttribute("hlj_playerName");
        if (game == null || playerName == null) {
            response.put("ok", false);
            response.put("error", "No active multiplayer game");
            return response;
        }
        if (game.isChatLocked()) {
            response.put("ok", false);
            response.put("error", "Chat is locked");
            return response;
        }
        if (text == null || text.isBlank()) {
            response.put("ok", false);
            response.put("error", "Empty message");
            return response;
        }
        ChatMessage.Type msgType;
        try {
            msgType = ChatMessage.Type.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            msgType = ChatMessage.Type.TEXT;
        }
        String trimmed = text.trim();
        if (msgType == ChatMessage.Type.TEXT && trimmed.length() > MAX_CHAT_LENGTH) {
            trimmed = trimmed.substring(0, MAX_CHAT_LENGTH);
        }
        String recipient = "ALL".equalsIgnoreCase(target) ? null : target;
        game.addChatMessage(new ChatMessage(playerName, trimmed, msgType, recipient));
        response.put("ok", true);
        response.put("chatVersion", game.getChatVersion());
        return response;
    }

    @PostMapping("/chat/lock")
    @ResponseBody
    public Map<String, Object> toggleChatLock(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        String playerName = (String) session.getAttribute("hlj_playerName");
        if (playerName == null) playerName = getHumanPlayerName(setup);
        if (game == null || setup == null || !setup.isController(playerName)) {
            response.put("ok", false);
            response.put("error", "Not authorised");
            return response;
        }
        game.setChatLocked(!game.isChatLocked());
        response.put("ok", true);
        response.put("chatLocked", game.isChatLocked());
        return response;
    }

    @PostMapping("/continue")
    public String continueGame(HttpSession session, Model model) {
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        RoundResult results = (RoundResult) session.getAttribute("hlj_roundResult");
        Match match = (Match) session.getAttribute("hlj_match");  // NEW: Get match
        
        if (game != null && setup != null && results != null && match != null) {
            // Get scores BEFORE the round (subtract the points just awarded)
            // TEAM MODE FIX: Use team scores for team mode
            Map<String, Integer> scoresBefore = new HashMap<>();
            
            if (game.isTeamMode()) {
                // Team mode: calculate team scores before this round
                for (Team team : game.getTeams()) {
                    String teamName = team.getName();
                    int currentScore = game.getScore(teamName);
                    int roundPoints = 0;
                    
                    // Count points awarded to this team this round
                    for (String category : new String[]{"High", "Low", "Jack", "Game"}) {
                        String winner = results.getRoundPointWinner(category);
                        if (winner != null) {
                            // Check if winner is team name OR a player on this team
                            if (teamName.equals(winner) || team.hasPlayer(winner)) {
                                roundPoints++;
                            }
                        }
                    }
                    scoresBefore.put(teamName, currentScore - roundPoints);
                }
                
                // DEBUG: Print team scores
                System.out.println("═══ TEAM SCORES CHECK (continueGame) ═══");
                for (Team team : game.getTeams()) {
                    String teamName = team.getName();
                    int currentScore = game.getScore(teamName);
                    System.out.println(teamName + " current: " + currentScore);
                }
                
                System.out.println("\n═══ SCORES BEFORE THIS ROUND ═══");
                for (Map.Entry<String, Integer> entry : scoresBefore.entrySet()) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
                
                System.out.println("\n═══ ROUND POINT WINNERS ═══");
                
                // ═══════════════════════════════════════════════════════════════
                // PERSONALITY: Check for quips
                // ═══════════════════════════════════════════════════════════════
                try {
                    List<String> quips = quipDetector.checkRoundQuips(game, results);
                    if (!quips.isEmpty()) {
                        model.addAttribute("quips", quips);
                        System.out.println("🎭 QUIPS: " + quips);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error checking quips: " + e.getMessage());
                }
                

                
                // ═══════════════════════════════════════════════════════════════
                // PERSONALITY: Check for real-time event quips
                // ═══════════════════════════════════════════════════════════════
                try {
                	List<String> eventQuips = realtimeQuipDetector.checkRealtimeEvents(game);
                    if (!eventQuips.isEmpty()) {
                        model.addAttribute("eventQuips", eventQuips);
                        System.out.println("🎭 EVENT QUIPS: " + eventQuips);
                    }
                    
                    // Clear events after checking
                    game.clearRecentEvents();
                } catch (Exception e) {
                    System.err.println("❌ Error checking event quips: " + e.getMessage());
                }
                
                for (String category : new String[]{"High", "Low", "Jack", "Game"}) {
                    String winner = results.getRoundPointWinner(category);
                    System.out.println(category + ": " + (winner != null ? winner : "none"));
                }
                
                // Track individual point wins in database
                try {
                    for (String category : new String[]{"High", "Low", "Jack", "Game"}) {
                        String winner = results.getRoundPointWinner(category);
                        if (winner != null) {
                            // Get actual player name (not team name)
                            String playerName = game.isTeamMode() ? 
                                results.getRoundPointPlayer(category) : winner;
                            
                            if (playerName != null) {
                                playerService.recordPoint(playerName, category);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error tracking round points: " + e.getMessage());
                }
                
            } else {
                // Individual mode: calculate player scores before this round
                for (String player : game.getPlayerNames()) {
                    int currentScore = game.getScore(player);
                    int roundPoints = 0;
                    for (String category : new String[]{"High", "Low", "Jack", "Game"}) {
                        if (player.equals(results.getRoundPointWinner(category))) {
                            roundPoints++;
                        }
                    }
                    scoresBefore.put(player, currentScore - roundPoints);
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // AWARDS: The Sweeper — all four points won by same entity
            // ═══════════════════════════════════════════════════════════════
            try {
                String[] sweepCats = {"High", "Low", "Jack", "Game"};
                String sweepWinner = results.getRoundPointWinner("High");
                boolean isSweep = sweepWinner != null;
                for (String cat : sweepCats) {
                    String w = results.getRoundPointWinner(cat);
                    if (w == null || !w.equals(sweepWinner)) { isSweep = false; break; }
                }
                if (isSweep) {
                    if (game.isTeamMode()) {
                        for (Team team : game.getTeams()) {
                            if (team.getName().equals(sweepWinner)) {
                                for (String pn : team.getPlayerNames()) {
                                    playerService.recordSweep(pn);
                                }
                                break;
                            }
                        }
                    } else {
                        playerService.recordSweep(sweepWinner);
                    }
                    System.out.println("🧹 SWEEP recorded for: " + sweepWinner);
                }
            } catch (Exception e) {
                System.err.println("❌ Error recording sweep: " + e.getMessage());
            }

            SetResult setResult = SetResult.determineWinner(scoresBefore, results.getRoundPointWinners());
            
            // ═══════════════════════════════════════════════════════════════
            // PERSONALITY: Set completion quips
            // ═══════════════════════════════════════════════════════════════
            try {
                List<String> setQuips = quipDetector.checkSetQuips(game, setResult);
                if (!setQuips.isEmpty()) {
                    model.addAttribute("setQuips", setQuips);
                    System.out.println("🎭 SET QUIPS: " + setQuips);
                }
            } catch (Exception e) {
                System.err.println("❌ Error checking set quips: " + e.getMessage());
            }
            
            // DEBUG: Print result
            System.out.println("\n═══ SET RESULT ═══");
            if (setResult != null) {
                System.out.println("WINNER: " + setResult.getWinner());
                System.out.println("WINNING POINT: " + setResult.getWinningPoint());
                System.out.println("WAS TIEBREAKER: " + setResult.wasTiebreaker());
                System.out.println("FINAL SCORES: " + setResult.getFinalScores());
            } else {
                System.out.println("NO WINNER YET");
            }
            System.out.println("═════════════════════════\n");
            
            if (setResult != null) {
                // ═══════════════════════════════════════════════════════════════
                // SET WINNER! Record in Match and check for match winner
                // ═══════════════════════════════════════════════════════════════
                
                boolean matchWon = match.recordSetWin(setResult);
                session.setAttribute("hlj_match", match);  // Update match in session

                // Update game's own setsWon so game.html scoreboard reflects the new count
                game.recordSetWin(setResult.getWinner());

                // ═══════════════════════════════════════════════════════════════
                // AWARDS: The Closer + The Choker — based on loser's final score
                // ═══════════════════════════════════════════════════════════════
                try {
                    Map<String, Integer> finalScores = setResult.getFinalScores();
                    String setWinner = setResult.getWinner();
                    for (Map.Entry<String, Integer> entry : finalScores.entrySet()) {
                        String entity = entry.getKey();
                        int score = entry.getValue();
                        if (!entity.equals(setWinner)) {
                            // This is the loser
                            if (score >= 9) {
                                // Closer: winner won a tight set
                                if (game.isTeamMode()) {
                                    for (Team team : game.getTeams()) {
                                        if (team.getName().equals(setWinner)) {
                                            for (String pn : team.getPlayerNames()) {
                                                playerService.recordCloseSetWin(pn);
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    playerService.recordCloseSetWin(setWinner);
                                }
                                System.out.println("🎯 CLOSE SET WIN recorded for: " + setWinner + " (loser had " + score + ")");
                            }
                            if (score >= 10) {
                                // Choker: loser had 10 points and still lost
                                if (game.isTeamMode()) {
                                    for (Team team : game.getTeams()) {
                                        if (team.getName().equals(entity)) {
                                            for (String pn : team.getPlayerNames()) {
                                                playerService.recordFailedFrom10(pn);
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    playerService.recordFailedFrom10(entity);
                                }
                                System.out.println("😬 CHOKE recorded for: " + entity + " (had " + score + " and lost)");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error recording closer/choker: " + e.getMessage());
                }

                if (matchWon) {
                    // ═══════════════════════════════════════════════════════════
                    // MATCH WINNER! Show epic victory screen
                    // ═══════════════════════════════════════════════════════════
                    System.out.println("🏆 MATCH WINNER: " + match.getMatchWinner());
                    
                    MatchResult matchResult = new MatchResult(match);

                    // ═══════════════════════════════════════════════════════════════
                    // PERSONALITY: Set(match???) completion quips????
                    // ═══════════════════════════════════════════════════════════════
                    try {
                    	List<String> matchQuips = quipDetector.checkMatchQuips(game, matchResult);
                        if (!matchQuips.isEmpty()) {
                        	session.setAttribute("hlj_matchQuips", matchQuips);
                            game.setPendingMatchQuips(matchQuips);  // Share with non-controller sessions
                        	System.out.println("🎭 MATCH QUIPS: " + matchQuips);
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error checking set quips: " + e.getMessage());
                    }

                    // ═══════════════════════════════════════════════════════════════
                    // UPDATE DATABASE - Record match stats for all players
                    // ═══════════════════════════════════════════════════════════════
                    try {
                        if (game.isTeamMode()) {
                            // Team mode: update stats for each player and each team
                            for (Team team : game.getTeams()) {
                                boolean teamWon = team.getName().equals(matchResult.getWinner());
                                int teamSetsWon = matchResult.getFinalSetWins().getOrDefault(team.getName(), 0);
                                List<String> teamPlayers = team.getPlayerNames();

                                for (String playerName : teamPlayers) {
                                    playerService.updateMatchStats(playerName, teamWon, teamSetsWon);
                                    System.out.println("📊 Updated stats for " + playerName +
                                                     " (team " + team.getName() + "): " +
                                                     (teamWon ? "WIN" : "LOSS"));
                                }

                                // Update team stats record
                                String p1 = teamPlayers.size() > 0 ? teamPlayers.get(0) : "";
                                String p2 = teamPlayers.size() > 1 ? teamPlayers.get(1) : "";
                                teamStatsService.updateMatchStats(team.getName(), p1, p2, teamWon, teamSetsWon);
                                System.out.println("📊 Updated team stats for " + team.getName() +
                                                 ": " + (teamWon ? "WIN" : "LOSS"));
                            }
                        } else {
                            // Individual mode: update stats for each player
                            for (String playerName : game.getPlayerNames()) {
                                boolean won = playerName.equals(matchResult.getWinner());
                                int setsWon = matchResult.getFinalSetWins().getOrDefault(playerName, 0);
                                playerService.updateMatchStats(playerName, won, setsWon);
                                System.out.println("📊 Updated stats for " + playerName + ": " +
                                                 (won ? "WIN" : "LOSS"));
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error updating player stats: " + e.getMessage());
                        e.printStackTrace();
                    }

                    // ═══════════════════════════════════════════════════════════════
                    // UPDATE HEAD-TO-HEAD RECORDS
                    // ═══════════════════════════════════════════════════════════════
                    try {
                        String matchWinner = matchResult.getWinner();
                        if (game.isTeamMode()) {
                            // Team vs team: one H2H record per match
                            for (Team team : game.getTeams()) {
                                if (!team.getName().equals(matchWinner)) {
                                    headToHeadService.recordResult(matchWinner, team.getName());
                                }
                            }
                        } else {
                            // Individual: winner beat every other player
                            for (String playerName : game.getPlayerNames()) {
                                if (!playerName.equals(matchWinner)) {
                                    headToHeadService.recordResult(matchWinner, playerName);
                                }
                            }
                        }
                        System.out.println("📊 Updated H2H records for match winner: " + matchWinner);
                    } catch (Exception e) {
                        System.err.println("❌ Error updating H2H stats: " + e.getMessage());
                        e.printStackTrace();
                    }

                    // Store match result in session AND in the shared game object so all players can see it
                    session.setAttribute("hlj_matchResult", matchResult);
                    game.setMatchResult(matchResult);
                    game.setState(Game.GameState.MATCH_COMPLETE);
                    session.setAttribute("hlj_game", game);

                    // Clear round data
                    session.removeAttribute("hlj_roundResult");
                    session.removeAttribute("hlj_roundNumber");

                    return "redirect:/highlowjack/match-winner";
                    
                } else {
                    // ═══════════════════════════════════════════════════════════
                    // SET WON, but match continues - show set winner screen
                    // ═══════════════════════════════════════════════════════════
                    System.out.println("🏆 SET WINNER: " + setResult.getWinner());
                    System.out.println("📊 Match score: " + match.getMatchScore(
                        game.isTeamMode() ? game.getTeams().get(0).getName() : game.getPlayerNames().get(0),
                        game.isTeamMode() ? game.getTeams().get(1).getName() : game.getPlayerNames().get(1)
                    ));
                    
                    // CRITICAL: Set game state so startNewSet() can work later
                    game.setState(Game.GameState.SET_COMPLETE);

                    // Store shared data in game object so non-controller sessions can read it
                    game.setLastSetResult(setResult);
                    game.setCurrentMatch(match);
                    session.setAttribute("hlj_game", game);

                    // Also store in controller's session for the GET endpoint
                    session.setAttribute("hlj_setResult", setResult);

                    // Clear round data (will be reset when next set starts)
                    session.removeAttribute("hlj_roundResult");

                    return "redirect:/highlowjack/set-winner";
                }
                
            } else {
                // ═══════════════════════════════════════════════════════════════
                // NO SET WINNER YET - Start next round
                // ═══════════════════════════════════════════════════════════════
                game.dealCards();
                session.setAttribute("hlj_game", game);
                session.removeAttribute("hlj_roundResult");
                
                Integer roundNumber = (Integer) session.getAttribute("hlj_roundNumber");
                if (roundNumber == null) {
                    roundNumber = 1;
                }
                roundNumber++;
                session.setAttribute("hlj_roundNumber", roundNumber);
            }
        }
        
        return "redirect:/highlowjack";
    }
    
    /**
     * Shows the match winner screen. Accessible via GET so non-controller multiplayer
     * players can be polled/redirected here after the match ends.
     */
    @GetMapping("/match-winner")
    public String showMatchWinner(HttpSession session, Model model) {
        Game game = (Game) session.getAttribute("hlj_game");
        MatchResult matchResult = (MatchResult) session.getAttribute("hlj_matchResult");

        // Non-controller players won't have matchResult in their session;
        // fall back to the shared game object which the controller populated.
        if (matchResult == null && game != null) {
            matchResult = game.getMatchResult();
        }

        if (matchResult == null) {
            return "redirect:/highlowjack/setup";
        }

        model.addAttribute("matchResult", matchResult);
        model.addAttribute("game", game);

        // isController: non-controller multiplayer players need to poll for rematch
        String sessionPlayerName = (String) session.getAttribute("hlj_playerName");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        boolean isController = sessionPlayerName == null || (setup != null && setup.getPlayers().stream()
            .filter(p -> p.getName().equals(sessionPlayerName))
            .anyMatch(PlayerInfo::isController));
        model.addAttribute("isController", isController);

        @SuppressWarnings("unchecked")
        List<String> matchQuips = (List<String>) session.getAttribute("hlj_matchQuips");
        if (matchQuips == null && game != null) {
            matchQuips = game.getPendingMatchQuips();  // Fall back to shared game object for non-controller
        }
        if (matchQuips != null) {
            model.addAttribute("matchQuips", matchQuips);
            session.removeAttribute("hlj_matchQuips");
        }

        return "highlowjack/match-winner";
    }

    /**
     * Shows the set winner screen. Accessible via GET so non-controller multiplayer
     * players can be redirected here after a set ends.
     */
    @GetMapping("/set-winner")
    public String showSetWinner(HttpSession session, Model model) {
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");

        // Try session first (controller), then shared game object (non-controller)
        SetResult setResult = (SetResult) session.getAttribute("hlj_setResult");
        if (setResult == null && game != null) {
            setResult = game.getLastSetResult();
        }

        Match match = (Match) session.getAttribute("hlj_match");
        if (match == null && game != null) {
            match = game.getCurrentMatch();
        }

        if (setResult == null || game == null) {
            return "redirect:/highlowjack/setup";
        }

        String humanPlayer = (String) session.getAttribute("hlj_playerName");
        boolean isController = humanPlayer == null ||
            (setup != null && setup.getPlayers().stream()
                .anyMatch(p -> p.getName().equals(humanPlayer) && p.isController()));

        model.addAttribute("setResult", setResult);
        model.addAttribute("game", game);
        model.addAttribute("match", match);
        model.addAttribute("isController", isController);

        return "highlowjack/set-winner";
    }

    /**
     * KIck off the next set
     * @param session
     * @return
     */
    @PostMapping("/next-set")
    public String startNextSet(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        Match match = (Match) session.getAttribute("hlj_match");

        if (game == null || match == null) {
            return "redirect:/highlowjack/setup";
        }

        // Prepare for new set (reset scores, increment set number) WITHOUT dealing cards.
        // The cut ceremony (GET /cut) will deal when done.
        game.prepareForNewSet();
        session.setAttribute("hlj_game", game);

        System.out.println("🎮 Preparing Set " + game.getCurrentSetNumber() + " — going to cut ceremony");

        return "redirect:/highlowjack/cut";
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CUT CEREMONY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Shows the cut ceremony page.
     * Controller sees player selectors; non-controllers see a waiting screen.
     */
    @GetMapping("/cut")
    public String showCut(Model model, HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");

        if (game == null || setup == null) {
            return "redirect:/highlowjack/setup";
        }

        // Determine if this session is the controller
        String sessionPlayerName = (String) session.getAttribute("hlj_playerName");
        boolean isController;
        if (sessionPlayerName != null) {
            // Multiplayer: controller is whoever has isController flag in setup
            isController = setup.getPlayers().stream()
                .filter(p -> p.getName().equals(sessionPlayerName))
                .anyMatch(PlayerInfo::isController);
        } else {
            isController = true;  // Single-player session
        }

        // Build cutter candidate lists for team vs individual mode
        List<String> team1Options = new ArrayList<>();
        List<String> team2Options = new ArrayList<>();

        List<String> names = game.getPlayerNames();

        if (setup.isTeamMode()) {
            // Team 1: players 0 & 2 (North/South); Team 2: players 1 & 3 (East/West)
            team1Options.add(names.get(0));
            team1Options.add(names.get(2));
            team2Options.add(names.get(1));
            team2Options.add(names.get(3));
        } else {
            // Individual: any two players — put all in both lists; JS prevents same selection
            team1Options.addAll(names);
            team2Options.addAll(names);
        }

        // Suggest defaults based on cutter rotation from last set
        String suggestedCutter1 = resolveSuggestedCutter(game, setup, 1, team1Options);
        String suggestedCutter2 = resolveSuggestedCutter(game, setup, 2, team2Options);

        // Pre-compute card image paths so template doesn't need complex Thymeleaf expressions
        if (game.getCutCard1() != null) {
            model.addAttribute("cutCard1Image", CardImageHelper.getCardImage(game.getCutCard1()));
            model.addAttribute("cutCard2Image", CardImageHelper.getCardImage(game.getCutCard2()));
            model.addAttribute("cutCard1Label", cardLabel(game.getCutCard1()));
            model.addAttribute("cutCard2Label", cardLabel(game.getCutCard2()));
        }

        // Read cut quips from game object (accessible from all sessions)
        if (game.getCutQuips() != null && !game.getCutQuips().isEmpty()) {
            model.addAttribute("cutQuips", game.getCutQuips());
        }

        // Determine if each cutter is human (for interactive flip mechanic)
        boolean cutter1IsHuman = game.getCutPlayer1() != null && setup.isHumanPlayer(game.getCutPlayer1());
        boolean cutter2IsHuman = game.getCutPlayer2() != null && setup.isHumanPlayer(game.getCutPlayer2());

        // showFlipBtnN = this session owns the flip button for cutter N.
        // Single-player (no sessionPlayerName): controller flips all human cards.
        // Multiplayer: each player only flips their own card.
        boolean showFlipBtn1 = cutter1IsHuman &&
            (sessionPlayerName == null || sessionPlayerName.equals(game.getCutPlayer1()));
        boolean showFlipBtn2 = cutter2IsHuman &&
            (sessionPlayerName == null || sessionPlayerName.equals(game.getCutPlayer2()));

        model.addAttribute("game", game);
        model.addAttribute("setup", setup);
        model.addAttribute("isController", isController);
        model.addAttribute("team1Options", team1Options);
        model.addAttribute("team2Options", team2Options);
        model.addAttribute("suggestedCutter1", suggestedCutter1);
        model.addAttribute("suggestedCutter2", suggestedCutter2);
        model.addAttribute("isTeamMode", setup.isTeamMode());
        model.addAttribute("cutter1IsHuman", cutter1IsHuman);
        model.addAttribute("cutter2IsHuman", cutter2IsHuman);
        model.addAttribute("showFlipBtn1", showFlipBtn1);
        model.addAttribute("showFlipBtn2", showFlipBtn2);

        return "highlowjack/cut-ceremony";
    }

    private String resolveSuggestedCutter(Game game, GameSetup setup, int slot, List<String> options) {
        int lastIdx = (slot == 1) ? game.getLastCutter1Index() : game.getLastCutter2Index();
        if (lastIdx < 0 || options.isEmpty()) return options.isEmpty() ? null : options.get(0);
        // Rotate within the option list
        String lastName = game.getPlayerNames().get(lastIdx);
        int posInOptions = options.indexOf(lastName);
        if (posInOptions < 0) return options.get(0);
        return options.get((posInOptions + 1) % options.size());
    }

    /**
     * Performs the cut: draws one card for each cutter and determines the winner.
     * Controller-only POST. Supports "random" cutter selection.
     */
    @PostMapping("/cut")
    public String performCut(
            @RequestParam(required = false) String cutter1,
            @RequestParam(required = false) String cutter2,
            @RequestParam(required = false, defaultValue = "false") boolean random,
            HttpSession session) {

        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");

        if (game == null || setup == null) return "redirect:/highlowjack/setup";

        List<String> names = game.getPlayerNames();

        if (random || cutter1 == null || cutter2 == null) {
            // Random selection
            if (setup.isTeamMode()) {
                // One from each team
                boolean flip = Math.random() < 0.5;
                cutter1 = flip ? names.get(0) : names.get(2);
                cutter2 = (Math.random() < 0.5) ? names.get(1) : names.get(3);
            } else {
                // Any two different players
                names = new ArrayList<>(names);
                java.util.Collections.shuffle(names);
                cutter1 = names.get(0);
                cutter2 = names.get(1);
                names = game.getPlayerNames(); // restore
            }
        }

        // Draw one card each from a fresh shuffled deck
        Deck cutDeck = new Deck();
        cutDeck.shuffle();
        Card card1 = cutDeck.dealHand(1).get(0);
        Card card2 = cutDeck.dealHand(1).get(0);

        game.setCutPlayer1(cutter1);
        game.setCutPlayer2(cutter2);
        game.setCutCard1(card1);
        game.setCutCard2(card2);

        int rank1 = card1.getRank().getValue();
        int rank2 = card2.getRank().getValue();

        if (rank1 == rank2) {
            // Tied — cut again
            game.setCutTied(true);
            game.setCutWinner(null);
        } else {
            game.setCutTied(false);
            String winner = (rank1 > rank2) ? cutter1 : cutter2;
            game.setCutWinner(winner);

            // Set pitcher to the winner
            int winnerIndex = game.getPlayerNames().indexOf(winner);
            game.setPitcherIndex(winnerIndex);

            // Track cutter indices for next-set rotation
            game.setLastCutter1Index(game.getPlayerNames().indexOf(cutter1));
            game.setLastCutter2Index(game.getPlayerNames().indexOf(cutter2));

            // Record stats (human players only — AI don't have DB records)
            recordCutStats(cutter1, card1, rank1 > rank2, setup);
            recordCutStats(cutter2, card2, rank2 > rank1, setup);
        }

        // Reset reveal flags for the new cut (so old revealed state doesn't persist)
        game.setCutPlayer1Revealed(false);
        game.setCutPlayer2Revealed(false);
        game.setCutQuips(null);

        // Cut quips — store on game object so all sessions (including non-controller) can display them
        if (!game.isCutTied()) {
            try {
                List<String> cutQuips = quipDetector.checkCutQuips(
                    cutter1, card1, cutter2, card2);
                if (!cutQuips.isEmpty()) {
                    game.setCutQuips(cutQuips);
                }
            } catch (Exception e) {
                System.err.println("❌ Error checking cut quips: " + e.getMessage());
            }
        }

        session.setAttribute("hlj_game", game);
        return "redirect:/highlowjack/cut";
    }

    private void recordCutStats(String playerName, Card card, boolean won, GameSetup setup) {
        boolean isHuman = setup.isHumanPlayer(playerName);
        if (isHuman) {
            try {
                playerService.recordCut(playerName, card.getRank().name(), won);
                if (card.getRank() == Card.Rank.TWO) {
                    playerService.recordTwoCut(playerName);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error recording cut stat for " + playerName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Called via AJAX when a human cutter clicks their flip button.
     * Marks the cutter as revealed on the shared game object so other sessions
     * can poll for it and auto-flip the matching card.
     */
    @PostMapping("/cut/player-revealed")
    @ResponseBody
    public Map<String, Object> markCutPlayerRevealed(@RequestParam int cutter, HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        if (game == null) return Map.of("ok", false);
        if (cutter == 1) game.setCutPlayer1Revealed(true);
        else if (cutter == 2) game.setCutPlayer2Revealed(true);
        session.setAttribute("hlj_game", game);
        return Map.of("ok", true, "cut1Revealed", game.isCutPlayer1Revealed(),
                                  "cut2Revealed", game.isCutPlayer2Revealed());
    }

    /**
     * Controller clicks "Start Game" after the cut winner is determined.
     * Deals the cards and starts the game.
     */
    @PostMapping("/cut/complete")
    public String completeCut(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");

        if (game == null) return "redirect:/highlowjack/setup";

        // Preserve cut quips so ALL sessions see them on the first game page load.
        // Without this, a non-controller whose poll fires after the controller clicks
        // "Start Game" navigates straight to IN_PROGRESS and never sees the cut page.
        List<String> cutQuips = game.getCutQuips();
        if (cutQuips != null && !cutQuips.isEmpty()) {
            game.setPendingRealtimeQuips(cutQuips);   // TTL-backed, visible to all sessions
        }

        game.dealCards();
        game.clearCutState();
        session.setAttribute("hlj_game", game);

        return "redirect:/highlowjack";
    }

    // Helper methods
    
    private String cardLabel(Card card) {
        if (card == null) return "";
        String rank = card.getRank().name();
        String suit = card.getSuit().name();
        // Capitalise first letter only
        rank = rank.charAt(0) + rank.substring(1).toLowerCase();
        suit = suit.charAt(0) + suit.substring(1).toLowerCase();
        return rank + " of " + suit;
    }

    private boolean isCurrentPlayerHuman(Game game, GameSetup setup) {
        String currentPlayer = game.getCurrentPlayer();
        return setup.isHumanPlayer(currentPlayer);
    }
    
    private String getHumanPlayerName(GameSetup setup) {
        return setup.getPlayers().stream()
            .filter(p -> p.getType() == PlayerInfo.PlayerType.HUMAN)
            .map(PlayerInfo::getName)
            .findFirst()
            .orElse(null);
    }
    
    private List<Card> calculateValidCards(Game game, String player) {
        Hand hand = game.getHand(player);
        if (hand == null) {
            return new ArrayList<>();
        }
        
        List<Card> validCards = new ArrayList<>();
        for (Card card : hand.getCards()) {
            if (GameEngine.isValidPlay(game, player, card)) {
                validCards.add(card);
            }
        }
        
        return validCards;
    }
    
    private void playAITurn(Game game) {
        String currentPlayer = game.getCurrentPlayer();
        Hand hand = game.getHand(currentPlayer);
        
        if (hand != null && !hand.isEmpty()) {
            Card card = SimpleAI.chooseCard(game, currentPlayer, hand);
            if (card != null) {
                game.playCard(card);
            }
        }
    }
}
