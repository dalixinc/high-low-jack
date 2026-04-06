package com.dalegames.highlowjack.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dalegames.highlowjack.SimpleAI;
import com.dalegames.highlowjack.engine.GameEngine;
import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.GameSetup;
import com.dalegames.highlowjack.model.Hand;
import com.dalegames.highlowjack.model.PlayerInfo;
import com.dalegames.highlowjack.model.RoundResult;
import com.dalegames.highlowjack.model.SetResult;
import com.dalegames.highlowjack.model.Team;
import com.dalegames.highlowjack.model.Trick;
import com.dalegames.highlowjack.model.Match;
import com.dalegames.highlowjack.model.MatchResult;
import com.dalegames.highlowjack.persistence.entity.Player;
import com.dalegames.highlowjack.persistence.service.PlayerService;
import com.dalegames.highlowjack.persistence.service.TeamStatsService;
import com.dalegames.highlowjack.persistence.entity.TeamStats;
import com.dalegames.highlowjack.service.QuipDetector;
import com.dalegames.highlowjack.service.RealtimeQuipDetector;


import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Web controller for High Low Jack card game.
 * 
 * @author Dale &amp; Primus
 * @version 8.13 - Adding quip mechanism
 */
@Controller
@RequestMapping("/highlowjack")
public class HighLowJackController {
	
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private TeamStatsService teamStatsService;
    
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
        
        // Setup exists but no game - create new game
        if (game == null) {
            game = new Game(setup);
            game.dealCards();
            session.setAttribute("hlj_game", game);
        }
        
        // Check if we should show final trick before scoring
        Boolean showFinalTrick = (Boolean) session.getAttribute("hlj_showFinalTrick");
        ////System.out.println("🔍 showGame() - showFinalTrick flag: " + showFinalTrick);
        ////System.out.println("🔍 showGame() - game state: " + game.getState());

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
                List<String> testQuips = new ArrayList<>();
                testQuips.add("🔥 TEST QUIP - If you see this, the display works!");
                testQuips.add("⚡ THE ACE OF AFRICA STRIKES!");
                model.addAttribute("eventQuips", testQuips);
                System.out.println("🧪 FORCED TEST QUIPS IN showGame(): " + testQuips);
                
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
            // REALTIME QUIPS: Check events after every card play!?????
            // ═══════════════════════════════════════════════════════════════
            try {
                List<String> eventQuips = realtimeQuipDetector.checkRealtimeEvents(game);
                if (!eventQuips.isEmpty()) {
                    model.addAttribute("eventQuips", eventQuips);
                    System.out.println("⚡ REALTIME EVENT QUIPS: " + eventQuips);
                }
                
                // Clear events after displaying
                game.clearRecentEvents();
            } catch (Exception e) {
                System.err.println("❌ Error checking realtime quips: " + e.getMessage());
            }
        } 
        
        
        
        else if (game.getState() == Game.GameState.IN_PROGRESS &&
                 !isCurrentPlayerHuman(game, setup)) {
            playAITurn(game);
            
            completedTrick = game.getCompletedTrick();
            if (completedTrick != null) {
                session.setAttribute("hlj_clearTrick", true);
            }
            
            if (game.getState() == Game.GameState.ROUND_COMPLETE) {
                // Set flag for final trick display
                System.out.println("🎯 AI completed round - setting showFinalTrick flag");
                session.setAttribute("hlj_showFinalTrick", true);
                session.setAttribute("hlj_game", game);
                return "redirect:/highlowjack";
            }
            
            session.setAttribute("hlj_game", game);
        }
        
        String humanPlayer = getHumanPlayerName(setup);
        List<Card> validCards = calculateValidCards(game, humanPlayer);
        boolean isAITurn = !isCurrentPlayerHuman(game, setup);
        
        Card.Suit leadSuit = null;
        if (game.getCurrentTrick() != null && game.getCurrentTrick().size() > 0) {
            leadSuit = game.getCurrentTrick().getLeadSuit();
        }
        
        Map<String, String> pointStatus = GameEngine.getCurrentPointStatus(game);
        
        model.addAttribute("game", game);
        model.addAttribute("setup", setup);
        model.addAttribute("humanPlayer", humanPlayer);
        model.addAttribute("isController", true); // For now, always player 1
        model.addAttribute("isAITurn", isAITurn);
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

        // Round counter: Initialize if needed
        Integer roundNumber = (Integer) session.getAttribute("hlj_roundNumber");
        if (roundNumber == null) {
            roundNumber = 1;
            session.setAttribute("hlj_roundNumber", roundNumber);
        }
        model.addAttribute("roundNumber", roundNumber);
        
        return "highlowjack/game";
    }
    
    @GetMapping("/setup")
    public String showSetup(Model model) {
        return "highlowjack/setup";
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
        
        // Store in session and clear any existing game
        session.setAttribute("hlj_setup", setup);
        session.removeAttribute("hlj_game");
        session.removeAttribute("hlj_clearTrick");
        
     // ADD THIS - Create match tracker
        Match match = new Match(matchType);
        session.setAttribute("hlj_match", match);
        
        return "redirect:/highlowjack";
    }
    
    @PostMapping("/play")
    public String playCard(@RequestParam int cardIndex, HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        
        if (game != null && setup != null &&
            game.getState() == Game.GameState.IN_PROGRESS &&
            isCurrentPlayerHuman(game, setup)) {
            
            String currentPlayer = game.getCurrentPlayer();
            Hand hand = game.getHand(currentPlayer);
            
            if (cardIndex >= 0 && cardIndex < hand.getCards().size()) {
                Card card = hand.getCards().get(cardIndex);
                
                if (GameEngine.isValidPlay(game, currentPlayer, card)) {
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
    
    @PostMapping("/sort-hand")
    public String sortHand(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        
        if (game != null && setup != null) {
            String humanPlayer = getHumanPlayerName(setup);
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

        
        boolean isController = true;

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
            
            // Get team stats (for now, empty - we'll implement this next)
            List<TeamStats> teams = new ArrayList<>();
            
            model.addAttribute("players", players);
            model.addAttribute("teams", teams);
            model.addAttribute("totalMatches", totalMatches);
            model.addAttribute("totalPoints", totalPoints);
            
            return "highlowjack/stats";
        } catch (Exception e) {
            System.err.println("❌ Error loading stats: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("players", new ArrayList<>());
            model.addAttribute("teams", new ArrayList<>());
            model.addAttribute("totalMatches", 0);
            model.addAttribute("totalPoints", 0);
            return "highlowjack/stats";
        }
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
                
                if (matchWon) {
                    // ═══════════════════════════════════════════════════════════
                    // MATCH WINNER! Show epic victory screen
                    // ═══════════════════════════════════════════════════════════
                    System.out.println("🏆 MATCH WINNER: " + match.getMatchWinner());
                    
                    MatchResult matchResult = new MatchResult(match);
                    model.addAttribute("matchResult", matchResult);
                    model.addAttribute("game", game);
                    
                    // ═══════════════════════════════════════════════════════════════
                    // PERSONALITY: Set(match???) completion quips????
                    // ═══════════════════════════════════════════════════════════════
                    try {
                    	List<String> matchQuips = quipDetector.checkMatchQuips(game, matchResult);
                        if (!matchQuips.isEmpty()) {
                        	model.addAttribute("matchQuips", matchQuips);
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
                            // Team mode: update stats for each player
                            for (Team team : game.getTeams()) {
                                boolean teamWon = team.getName().equals(matchResult.getWinner());
                                int teamSetsWon = matchResult.getFinalSetWins().getOrDefault(team.getName(), 0);
                                
                                for (String playerName : team.getPlayerNames()) {
                                    playerService.updateMatchStats(playerName, teamWon, teamSetsWon);
                                    System.out.println("📊 Updated stats for " + playerName + 
                                                     " (team " + team.getName() + "): " + 
                                                     (teamWon ? "WIN" : "LOSS"));
                                }
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
                    
                    // Clear session for new match
                    session.removeAttribute("hlj_roundResult");
                    session.removeAttribute("hlj_roundNumber");
                    
                    return "highlowjack/match-winner";
                    
                } else {
                    // ═══════════════════════════════════════════════════════════
                    // SET WON, but match continues - show set winner screen
                    // ═══════════════════════════════════════════════════════════
                    System.out.println("🏆 SET WINNER: " + setResult.getWinner());
                    System.out.println("📊 Match score: " + match.getMatchScore(
                        game.isTeamMode() ? game.getTeams().get(0).getName() : game.getPlayerNames().get(0),
                        game.isTeamMode() ? game.getTeams().get(1).getName() : game.getPlayerNames().get(1)
                    ));
                    
                    model.addAttribute("setResult", setResult);
                    model.addAttribute("game", game);
                    model.addAttribute("match", match);
                    
                    // CRITICAL: Set game state so startNewSet() can work later
                    game.setState(Game.GameState.SET_COMPLETE);
                    session.setAttribute("hlj_game", game);
                    
                    // Clear round data (will be reset when next set starts)
                    session.removeAttribute("hlj_roundResult");
                    
                    return "highlowjack/set-winner";
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
        
        // Reset game for new set (clears scores, tricks, rounds)
        game.startNewSet();
        session.setAttribute("hlj_game", game);
        
        // Reset round counter
        session.setAttribute("hlj_roundNumber", 1);
        
        System.out.println("🎮 Starting Set " + match.getCurrentSetNumber());
        
        return "redirect:/highlowjack";
    }

    // Helper methods
    
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
