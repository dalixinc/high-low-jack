package com.dalegames.highlowjack;

import java.util.List;

import com.dalegames.highlowjack.engine.GameEngine;
import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.Hand;

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
 * @version 2.1
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
     * <p>Strategy:
     * <ul>
     *   <li>If following suit: Try to win with high cards, duck with low cards</li>
     *   <li>If can't follow (discarding): Throw worthless cards first, save valuable cards</li>
     *   <li>Trump strategically to win important tricks</li>
     * </ul>
     * </p>
     * 
     * @param validCards cards we can legally play
     * @param game the game instance
     * @return the chosen card
     */
    private static Card chooseFollowCard(List<Card> validCards, Game game) {
        Card.Suit leadSuit = game.getCurrentTrick().getLeadSuit();
        Card.Suit trump = game.getTrumpSuit();
        
        // Check if we can follow suit
        boolean canFollowSuit = validCards.stream()
            .anyMatch(card -> card.getSuit() == leadSuit);
        
        if (!canFollowSuit) {
            // DISCARDING - we can't follow suit, so we're guaranteed to lose
            // Throw away worthless cards first, save valuable cards for later!
            return chooseBestDiscard(validCards, trump);
        }
        
        // We CAN follow suit - try to play smart
        // For now: simple - just play first valid card
        // TODO: Could be smarter - try to win with high cards, duck with low cards
        return validCards.get(0);
    }
    
    /**
     * Chooses best card to discard when we can't follow suit.
     * 
     * <p>Discard priority (best to worst):
     * <ol>
     *   <li>Low non-point cards (2-9 of non-trump)</li>
     *   <li>Queen (2 points, weak for leading)</li>
     *   <li>10 (10 points, but less useful than court cards for leading)</li>
     *   <li>King (3 points, GOOD for leading - try to save!)</li>
     *   <li>Ace (4 points, GREAT for leading - definitely save!)</li>
     *   <li>Jack of trump (NEVER discard if possible!)</li>
     * </ol>
     * </p>
     * 
     * @param validCards cards we can play
     * @param trump the trump suit
     * @return the best card to discard
     */
    private static Card chooseBestDiscard(List<Card> validCards, Card.Suit trump) {
        
        // PRIORITY 1: Low non-point cards (2-9) - completely worthless!
        Card lowCard = validCards.stream()
            .filter(card -> card.getSuit() != trump)  // Not trump
            .filter(card -> {
                Card.Rank rank = card.getRank();
                return rank != Card.Rank.TEN && 
                       rank != Card.Rank.JACK && 
                       rank != Card.Rank.QUEEN && 
                       rank != Card.Rank.KING && 
                       rank != Card.Rank.ACE;
            })
            .findFirst()
            .orElse(null);
        
        if (lowCard != null) {
            return lowCard;  // Throw away garbage!
        }
        
        // PRIORITY 2: Queen (only 2 points, weak lead)
        Card queen = validCards.stream()
            .filter(card -> card.getRank() == Card.Rank.QUEEN)
            .filter(card -> card.getSuit() != trump)
            .findFirst()
            .orElse(null);
        
        if (queen != null) {
            return queen;
        }
        
        // PRIORITY 3: 10 (10 points hurts, but less useful for leading than King/Ace)
        Card ten = validCards.stream()
            .filter(card -> card.getRank() == Card.Rank.TEN)
            .filter(card -> card.getSuit() != trump)
            .findFirst()
            .orElse(null);
        
        if (ten != null) {
            return ten;
        }
        
        // PRIORITY 4: King (3 points, good lead - try to save!)
        Card king = validCards.stream()
            .filter(card -> card.getRank() == Card.Rank.KING)
            .filter(card -> card.getSuit() != trump)
            .findFirst()
            .orElse(null);
        
        if (king != null) {
            return king;
        }
        
        // PRIORITY 5: Ace (4 points, GREAT lead - really want to save!)
        Card ace = validCards.stream()
            .filter(card -> card.getRank() == Card.Rank.ACE)
            .filter(card -> card.getSuit() != trump)
            .findFirst()
            .orElse(null);
        
        if (ace != null) {
            return ace;
        }
        
        // LAST RESORT: Must throw trump or Jack of trump (ouch!)
        // Just play first available - we're in trouble!
        return validCards.get(0);
    }
}
