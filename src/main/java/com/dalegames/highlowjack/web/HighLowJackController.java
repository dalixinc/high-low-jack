package com.dalegames.highlowjack.web;

import java.util.ArrayList;
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
import com.dalegames.highlowjack.model.Hand;
import com.dalegames.highlowjack.model.RoundResult;
import com.dalegames.highlowjack.model.Trick;

import jakarta.servlet.http.HttpSession;

/**
 * Web controller for High Low Jack card game.
 * 
 * @author Dale &amp; Primus
 * @version 6.1
 */
@Controller
@RequestMapping("/highlowjack")
public class HighLowJackController {
    
    private static final String HUMAN_PLAYER = "Dale";
    
    @GetMapping
    public String showGame(Model model, HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        
        if (game == null) {
            game = createNewGame();
            session.setAttribute("hlj_game", game);
        }
        
        Boolean shouldClearTrick = (Boolean) session.getAttribute("hlj_clearTrick");
        if (Boolean.TRUE.equals(shouldClearTrick)) {
            game.clearCompletedTrick();
            session.removeAttribute("hlj_clearTrick");
        }
        
        Trick completedTrick = game.getCompletedTrick();
        
        if (completedTrick != null) {
            session.setAttribute("hlj_clearTrick", true);
        } 
        else if (game.getState() == Game.GameState.IN_PROGRESS &&
                 !game.getCurrentPlayer().equals(HUMAN_PLAYER)) {
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
        
        List<Card> validCards = calculateValidCards(game, HUMAN_PLAYER);
        boolean isAITurn = !game.getCurrentPlayer().equals(HUMAN_PLAYER);
        
        Card.Suit leadSuit = null;
        if (game.getCurrentTrick() != null && game.getCurrentTrick().size() > 0) {
            leadSuit = game.getCurrentTrick().getLeadSuit();
        }
        
        Map<String, String> pointStatus = GameEngine.getCurrentPointStatus(game);
        
        model.addAttribute("game", game);
        model.addAttribute("humanPlayer", HUMAN_PLAYER);
        model.addAttribute("isAITurn", isAITurn);
        model.addAttribute("completedTrick", completedTrick);
        model.addAttribute("validCards", validCards);
        model.addAttribute("leadSuit", leadSuit);
        model.addAttribute("pointStatus", pointStatus);
        
        return "highlowjack/game";
    }
    
    @PostMapping("/play")
    public String playCard(@RequestParam int cardIndex, HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        
        if (game != null && 
            game.getCurrentPlayer().equals(HUMAN_PLAYER) &&
            game.getState() == Game.GameState.IN_PROGRESS) {
            
            Hand hand = game.getHand(HUMAN_PLAYER);
            
            if (cardIndex >= 0 && cardIndex < hand.getCards().size()) {
                Card card = hand.getCards().get(cardIndex);
                
                if (GameEngine.isValidPlay(game, HUMAN_PLAYER, card)) {
                    game.playCard(card);
                    session.setAttribute("hlj_game", game);
                }
            }
        }
        
        return "redirect:/highlowjack";
    }
    
    @PostMapping("/new")
    public String newGame(HttpSession session) {
        session.removeAttribute("hlj_game");
        session.removeAttribute("hlj_clearTrick");
        return "redirect:/highlowjack";
    }
    
    @PostMapping("/sort-hand")
    public String sortHand(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        
        if (game != null) {
            Hand hand = game.getHand(HUMAN_PLAYER);
            hand.sort();
            session.setAttribute("hlj_game", game);
        }
        
        return "redirect:/highlowjack";
    }
    
    @GetMapping("/scoring")
    public String showScoring(Model model, HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        
        if (game == null) {
            return "redirect:/highlowjack";
        }
        
        RoundResult results = GameEngine.calculateRoundResults(game);
        
        model.addAttribute("results", results);
        model.addAttribute("playerNames", game.getPlayerNames());
        model.addAttribute("winningScore", 11);
        
        return "highlowjack/scoring";
    }

    @PostMapping("/continue")
    public String continueGame(HttpSession session) {
        Game game = (Game) session.getAttribute("hlj_game");
        
        if (game != null) {
            for (String player : game.getPlayerNames()) {
                if (game.getScore(player) >= 11) {
                    session.removeAttribute("hlj_game");
                    return "redirect:/highlowjack";
                }
            }
            
            game.dealCards();
            session.setAttribute("hlj_game", game);
        }
        
        return "redirect:/highlowjack";
    }

    private Game createNewGame() {
        Game game = new Game("Dale", "Kreep", "Carryn", "Primus");
        game.dealCards();
        return game;
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
}
