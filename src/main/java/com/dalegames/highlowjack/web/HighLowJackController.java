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
 * @author Dale &amp; Primus
 * @version 2.0
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
        
        // Let AI players play if it's their turn
        processAITurns(game);
        
        // Save updated game state
        session.setAttribute("hlj_game", game);
        
        model.addAttribute("game", game);
        model.addAttribute("humanPlayer", HUMAN_PLAYER);
        
        return "highlowjack/game";
    }
    
    /**
     * Play a card.
     * 
     * @param cardIndex the index of the card to play
     * @param session the HTTP session
     * @return redirect to game page
     */
    @PostMapping("/play")
    public String playCard(@RequestParam int cardIndex, HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        
        if (game != null && game.getCurrentPlayer().equals(HUMAN_PLAYER)) {
            Hand hand = game.getHand(HUMAN_PLAYER);
            
            if (cardIndex >= 0 && cardIndex < hand.getCards().size()) {
                Card card = hand.getCards().get(cardIndex);
                
                // Play the card - Game.java handles everything!
                // - Validates the play
                // - Adds to current trick
                // - Completes trick if 4th card
                // - Determines winner
                // - Advances to next player
                game.playCard(card);
                
                // Let AI players play their turns
                processAITurns(game);
                
                // Check if hand is complete
                if (game.getState() == Game.GameState.ROUND_COMPLETE) {
                    // TODO: Score the hand and deal new hand
                    // For now, just start a new game
                    game = createNewGame();
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
        return "redirect:/highlowjack";
    }
    
    /**
     * Creates a new game.
     * 
     * @return new game instance
     */
    private Game createNewGame() {
        Game game = new Game("Dale", "Kreep", "Carryn", "Primus");
        game.dealCards();
        return game;
    }
    
    /**
     * Processes AI turns until it's the human player's turn.
     * 
     * <p>Game.java automatically:
     * <ul>
     *   <li>Advances to next player after each card</li>
     *   <li>Completes tricks when 4 cards played</li>
     *   <li>Sets trick winner as next player</li>
     * </ul>
     * </p>
     * 
     * @param game the game
     */
    private void processAITurns(Game game) {
        // Keep playing AI turns until it's human's turn or round is complete
        while (!game.getCurrentPlayer().equals(HUMAN_PLAYER) && 
               game.getState() == Game.GameState.IN_PROGRESS) {
            
            playAITurn(game);
        }
    }
    
    /**
     * Plays a turn for the current AI player.
     * 
     * @param game the game
     */
    private void playAITurn(Game game) {
        String currentPlayer = game.getCurrentPlayer();
        Hand hand = game.getHand(currentPlayer);
        
        // Use SimpleAI to choose a card
        Card card = SimpleAI.chooseCard(game, currentPlayer, hand);
        
        // Play it - Game.java handles all the logic!
        game.playCard(card);
    }
}
