package com.dalegames.highlowjack.web;

import com.dalegames.highlowjack.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

/**
 * Web controller for High Low Jack card game.
 * 
 * @author Dale &amp; Primus
 * @version 1.0
 */
@Controller
@RequestMapping("/highlowjack")
public class HighLowJackController {
    
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
            // New game - for now hardcode players
            // Later: add player configuration screen
            game = new Game("Dale", "Kreep", "Carryn", "Primus");
            game.dealCards();
            session.setAttribute("hlj_game", game);
        }
        
        model.addAttribute("game", game);
        model.addAttribute("humanPlayer", "Dale"); // For now, Dale is human
        
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
        
        if (game != null) {
            String currentPlayer = game.getCurrentPlayer();
            Hand hand = game.getHand(currentPlayer);
            
            if (cardIndex >= 0 && cardIndex < hand.getCards().size()) {
                Card card = hand.getCards().get(cardIndex);
                game.playCard(card);
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
}
