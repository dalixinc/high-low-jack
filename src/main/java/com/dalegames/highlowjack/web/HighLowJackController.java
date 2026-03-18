package com.dalegames.highlowjack.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dalegames.highlowjack.SimpleAI;
import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.Hand;
import com.dalegames.highlowjack.model.Trick;

import jakarta.servlet.http.HttpSession;

/**
 * Web controller for High Low Jack card game.
 * 
 * <p>Implements turn-based gameplay with proper pacing and trick display.
 * 
 * @author Dale &amp; Primus
 * @version 4.0
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
                // TODO: Score the hand and deal new hand
                // For now, just start a new game
                game = createNewGame();
                completedTrick = null;
            }
            
            session.setAttribute("hlj_game", game);
        }
        
        // Determine if we should auto-refresh
        boolean isAITurn = !game.getCurrentPlayer().equals(HUMAN_PLAYER);
        
        model.addAttribute("game", game);
        model.addAttribute("humanPlayer", HUMAN_PLAYER);
        model.addAttribute("isAITurn", isAITurn);
        model.addAttribute("completedTrick", completedTrick);
        
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
                game.playCard(card);
                
                session.setAttribute("hlj_game", game);
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
}
