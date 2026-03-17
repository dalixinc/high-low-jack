package com.dalegames.highlowjack;

import com.dalegames.highlowjack.engine.GameEngine;
import com.dalegames.highlowjack.model.*;

import java.util.List;

/**
 * Simple AI for playing High Low Jack.
 * 
 * <p>Strategy based on real High Low Jack gameplay:
 * <ul>
 *   <li><strong>Never lead with 10s</strong> - Too valuable (10 points) and risky</li>
 *   <li><strong>Lead high (Ace, King)</strong> - Win tricks to capture card points</li>
 *   <li><strong>Lead low (2-9)</strong> - Pass the lead away, protect valuable cards</li>
 *   <li><strong>Protect Jack of trumps</strong> - Save it for when you can win the trick</li>
 * </ul>
 * </p>
 * 
 * @author Dale &amp; Primus
 * @version 2.0
 */
public class SimpleAI {
    
    /**
     * Chooses a card to play for an AI player.
     * 
     * @param game the game instance
     * @param playerName the AI player's name
     * @param hand the AI player's hand
     * @return the chosen card
     */
    public static Card chooseCard(Game game, String playerName, Hand hand) {
        List<Card> cards = hand.getCards();
        
        // Get all valid cards
        List<Card> validCards = cards.stream()
            .filter(card -> GameEngine.isValidPlay(game, playerName, card))
            .toList();
        
        if (validCards.isEmpty()) {
            // Shouldn't happen, but fallback
            return cards.get(0);
        }
        
        // Check if we're leading (first card of trick)
        boolean isLeading = game.getCurrentTrick() == null || game.getCurrentTrick().size() == 0;
        
        if (isLeading) {
            return chooseLeadCard(validCards, game);
        } else {
            return chooseFollowCard(validCards, game);
        }
    }
    
    /**
     * Chooses a card to lead with.
     * 
     * <p>Strategy:
     * <ol>
     *   <li>NEVER lead with 10 (too risky - 10 points)</li>
     *   <li>Prefer high cards (A, K) to win tricks and capture points</li>
     *   <li>Lead low cards (2-9) to pass lead and protect valuable cards</li>
     * </ol>
     * </p>
     * 
     * @param validCards cards we can legally play
     * @param game the game instance
     * @return the chosen card
     */
    private static Card chooseLeadCard(List<Card> validCards, Game game) {
        
        // NEVER lead with a 10 - too valuable!
        // First, try to find any non-10 card
        Card avoid10 = validCards.stream()
            .filter(card -> card.getRank() != Card.Rank.TEN)
            .findFirst()
            .orElse(null);
        
        if (avoid10 == null) {
            // Only have 10s - must play one (unlucky!)
            return validCards.get(0);
        }
        
        // Now we have non-10 options. Choose strategically:
        
        // STRATEGY 1: Lead HIGH (Ace or King) to win tricks and capture points
        // Aces and Kings are strong leads - likely to win the trick
        Card highCard = validCards.stream()
            .filter(card -> card.getRank() == Card.Rank.ACE || card.getRank() == Card.Rank.KING)
            .findFirst()
            .orElse(null);
        
        if (highCard != null) {
            // Lead high to win points!
            return highCard;
        }
        
        // STRATEGY 2: Lead LOW (2-9, not 10) to pass the lead
        // This protects your valuable cards (like Jack of trumps!)
        Card lowCard = validCards.stream()
            .filter(card -> card.getRank() != Card.Rank.TEN &&
                           card.getRank() != Card.Rank.QUEEN &&
                           card.getRank() != Card.Rank.JACK)
            .findFirst()
            .orElse(null);
        
        if (lowCard != null) {
            // Lead low to pass the lead away
            return lowCard;
        }
        
        // STRATEGY 3: If only Queens or Jacks left (besides 10s), play them
        // Avoid the 10 at all costs!
        return avoid10;
    }
    
    /**
     * Chooses a card to play when following (not leading).
     * 
     * <p>Simple strategy for now - just play first valid card.
     * Could be improved later with:
     * <ul>
     *   <li>Try to win if we have high cards</li>
     *   <li>Duck if we want to avoid winning</li>
     *   <li>Trump strategically</li>
     * </ul>
     * </p>
     * 
     * @param validCards cards we can legally play
     * @param game the game instance
     * @return the chosen card
     */
    private static Card chooseFollowCard(List<Card> validCards, Game game) {
        // For now: simple - just play first valid card
        // This can be made MUCH smarter later!
        // (Try to win with high cards, duck with low cards, etc.)
        return validCards.get(0);
    }
}
