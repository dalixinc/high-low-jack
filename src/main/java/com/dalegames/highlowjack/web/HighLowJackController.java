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
import com.dalegames.highlowjack.model.Trick;

import jakarta.servlet.http.HttpSession;

/**
 * Web controller for High Low Jack card game.
 * 
 * @author Dale &amp; Primus
 * @version 8.1 - Added pitcher indicator and tricks won counter
 */
@Controller
@RequestMapping("/highlowjack")
public class HighLowJackController {
    
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
        
        Boolean shouldClearTrick = (Boolean) session.getAttribute("hlj_clearTrick");
        if (Boolean.TRUE.equals(shouldClearTrick)) {
            game.clearCompletedTrick();
            session.removeAttribute("hlj_clearTrick");
        }
        
        // Check if round is complete and redirect to scoring
        if (game.getState() == Game.GameState.ROUND_COMPLETE) {
            session.setAttribute("hlj_game", game);
            return "redirect:/highlowjack/scoring";
        }
        
        Trick completedTrick = game.getCompletedTrick();
        
        if (completedTrick != null) {
            session.setAttribute("hlj_clearTrick", true);
        } 
        else if (game.getState() == Game.GameState.IN_PROGRESS &&
                 !isCurrentPlayerHuman(game, setup)) {
            playAITurn(game);
            
            completedTrick = game.getCompletedTrick();
            if (completedTrick != null) {
                session.setAttribute("hlj_clearTrick", true);
            }
            
            if (game.getState() == Game.GameState.ROUND_COMPLETE) {
                session.setAttribute("hlj_game", game);
                return "redirect:/highlowjack/scoring";
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
            setup = GameSetup.createTeam(players, matchType);
        } else {
            setup = GameSetup.createIndividual(players, matchType);
        }
        
        // Store in session and clear any existing game
        session.setAttribute("hlj_setup", setup);
        session.removeAttribute("hlj_game");
        session.removeAttribute("hlj_clearTrick");
        
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
            return "redirect:/highlowjack";
        }
        
        RoundResult results = GameEngine.calculateRoundResults(game);
        session.setAttribute("hlj_roundResult", results);  // Store for continueGame()
        
        // Check for set winner using tiebreaker logic
        // Calculate scores BEFORE this round by subtracting points just awarded
        Map<String, Integer> scoresBefore = new HashMap<>();
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
        
        SetResult setResult = SetResult.determineWinner(scoresBefore, results.getRoundPointWinners());
        
        model.addAttribute("results", results);
        model.addAttribute("setup", setup);
        model.addAttribute("playerNames", game.getPlayerNames());
        model.addAttribute("winningScore", 11);
        model.addAttribute("setResult", setResult);
        model.addAttribute("currentSetNumber", game.getCurrentSetNumber());
        model.addAttribute("setsWon", game.getSetsWon());
        model.addAttribute("isController", true); // For now, always player 1
        
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

        // PHASE 5: need to pass the game reference to the scoring page to display current scores and sets won
        model.addAttribute("game", game);

        return "highlowjack/scoring";
    }

    @PostMapping("/continue")
    public String continueGame(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        GameSetup setup = (GameSetup) session.getAttribute("hlj_setup");
        RoundResult results = (RoundResult) session.getAttribute("hlj_roundResult");
        
        if (game != null && setup != null && results != null) {
            // Get scores BEFORE the round (subtract the points just awarded)
            Map<String, Integer> scoresBefore = new HashMap<>();
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
            
            SetResult setResult = SetResult.determineWinner(scoresBefore, results.getRoundPointWinners());
            
            if (setResult != null) {
                // Someone won the set!
                game.recordSetWin(setResult.getWinner());
                session.setAttribute("hlj_game", game);
                
                if (game.isMatchComplete()) {
                    // Match is over
                    session.removeAttribute("hlj_roundResult");
                    return "redirect:/highlowjack/setup";
                } else {
                    // Start new set
                    game.startNewSet();
                    session.setAttribute("hlj_game", game);
                    session.removeAttribute("hlj_roundResult");
                }
            } else {
                // No set winner yet - deal new round
                game.dealCards();
                session.setAttribute("hlj_game", game);
                session.removeAttribute("hlj_roundResult");
            }
        }
        
        return "redirect:/highlowjack";
    }

    
    private void playAITurn(Game game) {
        String currentPlayer = game.getCurrentPlayer();
        Hand hand = game.getHand(currentPlayer);
        
        Card card = SimpleAI.chooseCard(game, currentPlayer, hand);
        game.playCard(card);
    }
    
    private List<Card> calculateValidCards(Game game, String playerName) {
        List<Card> validCards = new ArrayList<>();
        
        if (!game.getCurrentPlayer().equals(playerName)) {
            return validCards;
        }
        
        if (game.getState() != Game.GameState.IN_PROGRESS) {
            return validCards;
        }
        
        Hand hand = game.getHand(playerName);
        for (Card card : hand.getCards()) {
            if (GameEngine.isValidPlay(game, playerName, card)) {
                validCards.add(card);
            }
        }
        
        return validCards;
    }
    
    private boolean isCurrentPlayerHuman(Game game, GameSetup setup) {
        String currentPlayer = game.getCurrentPlayer();
        return setup.isHumanPlayer(currentPlayer);
    }
    
    private String getHumanPlayerName(GameSetup setup) {
        // Return the first human player found
        for (PlayerInfo player : setup.getPlayers()) {
            if (player.isHuman()) {
                return player.getName();
            }
        }
        // Fallback to Player 1
        return setup.getPlayer(0).getName();
    }
}
