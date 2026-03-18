package com.dalegames.highlowjack.web;

import java.util.ArrayList;
import java.util.List;

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
import com.dalegames.highlowjack.model.Hand;
import com.dalegames.highlowjack.model.RoundResult;
import com.dalegames.highlowjack.model.Trick;

import jakarta.servlet.http.HttpSession;

/**
 * Web controller for High Low Jack card game.
 * 
 * <p>Implements turn-based gameplay with proper pacing, trick display,
 * and valid card enforcement.
 * 
 * @author Dale &amp; Primus
 * @version 5.0
 */
@Controller
@RequestMapping("/highlowjack")
public class HighLowJackController {
    
    private static final String HUMAN_PLAYER = "Dale";
    
    /**
     * Show the game page.
     * 
     * @param model the model
     * @param session the HTTP session
     * @return the game view
     */
    @GetMapping
    public String showGame(Model model, HttpSession session) {
        // Initialize game if not present
        Game game = (Game) session.getAttribute("hlj_game");
        
        if (game == null) {
            game = createNewGame();
            session.setAttribute("hlj_game", game);
        }
        
        // Check if we should clear a completed trick
        Boolean shouldClearTrick = (Boolean) session.getAttribute("hlj_clearTrick");
        if (Boolean.TRUE.equals(shouldClearTrick)) {
            game.clearCompletedTrick();
            session.removeAttribute("hlj_clearTrick");
        }
        
        // Get the completed trick for display (if any)
        Trick completedTrick = game.getCompletedTrick();
        
        // Handle different states
        if (completedTrick != null) {
            // A trick just completed - show it and set flag to clear on next load
            session.setAttribute("hlj_clearTrick", true);
        } 
        else if (game.getState() == Game.GameState.IN_PROGRESS &&
                 !game.getCurrentPlayer().equals(HUMAN_PLAYER)) {
            // AI's turn and no completed trick showing - play ONE card
            playAITurn(game);
            
            // Check if this AI play completed a trick
            completedTrick = game.getCompletedTrick();
            if (completedTrick != null) {
                // Trick just completed - set flag to clear on next load
                session.setAttribute("hlj_clearTrick", true);
            }
            
            // Check if round is complete

            if (game.getState() == Game.GameState.ROUND_COMPLETE) {
                // Redirect to scoring screen
                session.setAttribute("hlj_game", game);
                return "redirect:/highlowjack/scoring";
            }   
                   
            session.setAttribute("hlj_game", game);
        }
        
        // Calculate valid cards for human player
        List<Card> validCards = calculateValidCards(game, HUMAN_PLAYER);
        
        // Determine if we should auto-refresh
        boolean isAITurn = !game.getCurrentPlayer().equals(HUMAN_PLAYER);
        
        // Get lead suit if applicable
        Card.Suit leadSuit = null;
        if (game.getCurrentTrick() != null && game.getCurrentTrick().size() > 0) {
            leadSuit = game.getCurrentTrick().getLeadSuit();
        }
        
        model.addAttribute("game", game);
        model.addAttribute("humanPlayer", HUMAN_PLAYER);
        model.addAttribute("isAITurn", isAITurn);
        model.addAttribute("completedTrick", completedTrick);
        model.addAttribute("validCards", validCards);
        model.addAttribute("leadSuit", leadSuit);
        
        return "highlowjack/game";
    }
    
    /**
     * Play a card for the human player.
     * 
     * @param cardIndex the index of the card to play
     * @param session the HTTP session
     * @return redirect to game page
     */
    @PostMapping("/play")
    public String playCard(@RequestParam int cardIndex, HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        
        if (game != null && 
            game.getCurrentPlayer().equals(HUMAN_PLAYER) &&
            game.getState() == Game.GameState.IN_PROGRESS) {
            
            Hand hand = game.getHand(HUMAN_PLAYER);
            
            if (cardIndex >= 0 && cardIndex < hand.getCards().size()) {
                Card card = hand.getCards().get(cardIndex);
                
                // Validate the play before executing
                if (GameEngine.isValidPlay(game, HUMAN_PLAYER, card)) {
                    game.playCard(card);
                    session.setAttribute("hlj_game", game);
                }
                // If invalid, just redirect back - front-end should prevent this
            }
        }
        
        return "redirect:/highlowjack";
    }
    
    /**
     * Start a new game.
     * 
     * @param session the HTTP session
     * @return redirect to game page
     */
    @PostMapping("/new")
    public String newGame(HttpSession session) {
        session.removeAttribute("hlj_game");
        session.removeAttribute("hlj_clearTrick");
        return "redirect:/highlowjack";
    }
    
    /**
     * Creates a new game with 4 players.
     * 
     * @return new game instance
     */
    private Game createNewGame() {
        Game game = new Game("Dale", "Kreep", "Carryn", "Primus");
        game.dealCards();
        return game;
    }
    
    /**
     * Plays ONE turn for the current AI player.
     * 
     * @param game the game
     */
    private void playAITurn(Game game) {
        String currentPlayer = game.getCurrentPlayer();
        Hand hand = game.getHand(currentPlayer);
        
        // Use SimpleAI to choose a card
        Card card = SimpleAI.chooseCard(game, currentPlayer, hand);
        
        // Play the card
        game.playCard(card);
    }
    
    /**
     * Calculates which cards are valid plays for the given player.
     * 
     * <p>Uses GameEngine.isValidPlay() to check each card in the player's hand.
     * 
     * @param game the game
     * @param playerName the player's name
     * @return list of valid cards
     */
    private List<Card> calculateValidCards(Game game, String playerName) {
        List<Card> validCards = new ArrayList<>();
        
        // If it's not this player's turn, no cards are valid
        if (!game.getCurrentPlayer().equals(playerName)) {
            return validCards;
        }
        
        // If game is not in progress, no cards are valid
        if (game.getState() != Game.GameState.IN_PROGRESS) {
            return validCards;
        }
        
        // Check each card in hand
        Hand hand = game.getHand(playerName);
        for (Card card : hand.getCards()) {
            if (GameEngine.isValidPlay(game, playerName, card)) {
                validCards.add(card);
            }
        }
        
        return validCards;
    }

    /**
     * Shows the scoring screen after a round completes.
     * 
     * @param model the model
     * @param session the HTTP session
     * @return the scoring view
     */
    @GetMapping("/scoring")
    public String showScoring(Model model, HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        
        if (game == null) {
            return "redirect:/highlowjack";
        }
        
        // Calculate complete round results
        RoundResult results = GameEngine.calculateRoundResults(game);
        
        model.addAttribute("results", results);
        model.addAttribute("playerNames", game.getPlayerNames());
        model.addAttribute("winningScore", 11);  // First to 11 wins
        
        return "highlowjack/scoring";
    }

    /**
     * Continues to next round after scoring.
     * 
     * @param session the HTTP session
     * @return redirect to game page
     */
    @PostMapping("/continue")
    public String continueGame(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        
        if (game != null) {
            // Check if someone won the game
            for (String player : game.getPlayerNames()) {
                if (game.getScore(player) >= 11) {
                    // TODO: Show game over screen
                    session.removeAttribute("hlj_game");
                    return "redirect:/highlowjack";
                }
            }
            
            // Deal new round
            game.dealCards();
            session.setAttribute("hlj_game", game);
        }
        
        return "redirect:/highlowjack";
    }

}
