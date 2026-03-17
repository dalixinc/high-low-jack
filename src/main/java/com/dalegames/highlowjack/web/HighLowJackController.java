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

import jakarta.servlet.http.HttpSession;

/**
 * Web controller for High Low Jack card game.
 * 
 * <p>Implements turn-based gameplay with proper pacing:
 * <ul>
 *   <li>Human player clicks to play their card</li>
 *   <li>AI players play ONE card per page load</li>
 *   <li>JavaScript auto-refreshes with 1-3 second delays</li>
 *   <li>Tricks complete after 4th card with visual pause</li>
 * </ul>
 * 
 * @author Dale &amp; Primus
 * @version 3.0
 */
@Controller
@RequestMapping("/highlowjack")
public class HighLowJackController {
    
    private static final String HUMAN_PLAYER = "Dale";
    
    /**
     * Show the game page.
     * 
     * <p>If it's an AI player's turn, plays ONE card and returns.
     * Browser will auto-refresh to show next turn.</p>
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
        
        // Check if we need to clear completed trick
        Boolean trickJustCompleted = (Boolean) session.getAttribute("hlj_trickCompleted");
        if (Boolean.TRUE.equals(trickJustCompleted)) {
            // Give user time to see the completed trick before clearing
            session.removeAttribute("hlj_trickCompleted");
        }
        
        // If it's AI's turn and game is in progress, play ONE card
        if (game.getState() == Game.GameState.IN_PROGRESS &&
            !game.getCurrentPlayer().equals(HUMAN_PLAYER)) {
            
            playAITurn(game);
            
            // Check if trick just completed
            if (game.getCurrentTrick() == null) {
                session.setAttribute("hlj_trickCompleted", true);
            }
            
            // Check if round is complete
            if (game.getState() == Game.GameState.ROUND_COMPLETE) {
                // TODO: Score the hand and deal new hand
                // For now, just start a new game
                game = createNewGame();
            }
            
            session.setAttribute("hlj_game", game);
        }
        
        model.addAttribute("game", game);
        model.addAttribute("humanPlayer", HUMAN_PLAYER);
        model.addAttribute("isAITurn", !game.getCurrentPlayer().equals(HUMAN_PLAYER));
        
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
                
                // Check if trick just completed
                if (game.getCurrentTrick() == null) {
                    session.setAttribute("hlj_trickCompleted", true);
                }
                
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
        session.removeAttribute("hlj_trickCompleted");
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
     * <p>Game.java automatically handles:
     * <ul>
     *   <li>Advancing to next player</li>
     *   <li>Completing trick if 4th card</li>
     *   <li>Setting winner as next player</li>
     * </ul>
     * </p>
     * 
     * @param game the game
     */
    private void playAITurn(Game game) {
        String currentPlayer = game.getCurrentPlayer();
        Hand hand = game.getHand(currentPlayer);
        
        // Use SimpleAI to choose a card
        Card card = SimpleAI.chooseCard(game, currentPlayer, hand);
        
        // Play the card - Game.java handles all the logic
        game.playCard(card);
    }
}
