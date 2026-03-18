package com.dalegames.highlowjack;

import com.dalegames.highlowjack.engine.GameEngine;
import com.dalegames.highlowjack.model.*;

import java.util.Comparator;
import java.util.List;

/**
 * Simple AI for playing High Low Jack with proper card value awareness.
 * 
 * <p><strong>Core Strategy Principles:</strong></p>
 * <ul>
 *   <li><strong>NEVER waste point cards</strong> (10/J/Q/K/A) on unwinnable tricks</li>
 *   <li><strong>Jack of trump is SACRED</strong> - only play for 15+ point tricks</li>
 *   <li><strong>Lead high to win</strong> (Ace, King) - capture points early</li>
 *   <li><strong>Lead low to pass</strong> (2-9) - protect valuable cards</li>
 *   <li><strong>NEVER lead 10s</strong> - too risky (10 points at stake)</li>
 *   <li><strong>Always play worthless cards first</strong> (2-9) when discarding</li>
 * </ul>
 * 
 * @author Dale &amp; Primus
 * @version 3.0
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
            return cards.get(0);  // Shouldn't happen
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
     * <p><strong>Lead Strategy:</strong></p>
     * <ol>
     *   <li><strong>NEVER lead with 10</strong> - too valuable (10 points)</li>
     *   <li><strong>Lead high</strong> (Ace, King) to win tricks and capture points</li>
     *   <li><strong>Lead low</strong> (2-9) to pass lead and protect valuable cards</li>
     * </ol>
     * 
     * @param validCards cards we can legally play
     * @param game the game instance
     * @return the chosen card
     */
    private static Card chooseLeadCard(List<Card> validCards, Game game) {
        
        // RULE 1: NEVER lead with a 10
        List<Card> nonTens = validCards.stream()
            .filter(card -> card.getRank() != Card.Rank.TEN)
            .toList();
        
        List<Card> candidates = nonTens.isEmpty() ? validCards : nonTens;
        
        // RULE 2: Lead HIGH (Ace or King) to win tricks and capture points
        Card highCard = candidates.stream()
            .filter(card -> card.getRank() == Card.Rank.ACE || card.getRank() == Card.Rank.KING)
            .findFirst()
            .orElse(null);
        
        if (highCard != null) {
            return highCard;  // Lead high to win!
        }
        
        // RULE 3: Lead LOW (2-9) to pass the lead and protect valuable cards
        Card lowCard = candidates.stream()
            .filter(card -> isWorthlessCard(card))
            .findFirst()
            .orElse(null);
        
        if (lowCard != null) {
            return lowCard;  // Lead low to pass
        }
        
        // RULE 4: If only court cards left (Q, J), play them
        // Better than holding them and risking losing them later
        return candidates.get(0);
    }
    
    /**
     * Chooses a card to play when following (not leading).
     * 
     * <p><strong>Follow Strategy:</strong></p>
     * <ol>
     *   <li>If can't follow suit: <strong>Discard worthless cards first</strong></li>
     *   <li>If trick is valuable (10+ points): <strong>Try to win with lowest winning card</strong></li>
     *   <li>If trick not valuable: <strong>Duck with worthless card</strong></li>
     *   <li><strong>NEVER play Jack of trump</strong> unless trick worth 15+ points</li>
     * </ol>
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
            // DISCARDING - we can't follow suit, guaranteed to lose
            // Throw away worthless cards first!
            return chooseBestDiscard(validCards, trump);
        }
        
        // We CAN follow suit - play smartly
        List<Card> followCards = validCards.stream()
            .filter(card -> card.getSuit() == leadSuit)
            .toList();
        
        // Calculate trick value
        int trickValue = calculateTrickValue(game.getCurrentTrick());
        
        // Get current winning card
        Card currentWinner = getCurrentWinningCard(game.getCurrentTrick(), trump);
        
        // CARDINAL RULE: Protect Jack of trump
        Card jackOfTrump = followCards.stream()
            .filter(card -> card.getSuit() == trump && card.getRank() == Card.Rank.JACK)
            .findFirst()
            .orElse(null);
        
        if (jackOfTrump != null) {
            // Only play Jack if trick is worth 15+ points
            if (trickValue < 15) {
                List<Card> nonJackCards = followCards.stream()
                    .filter(card -> !(card.getSuit() == trump && card.getRank() == Card.Rank.JACK))
                    .toList();
                
                if (!nonJackCards.isEmpty()) {
                    followCards = nonJackCards;  // Remove Jack from consideration
                }
            }
        }
        
        // STRATEGY: Should we try to win this trick?
        if (trickValue >= 10) {
            // Trick is valuable - try to win it
            Card lowestWinner = followCards.stream()
                .filter(card -> canBeat(card, currentWinner, trump))
                .min(Comparator.comparingInt(card -> card.getRank().getValue()))
                .orElse(null);
            
            if (lowestWinner != null) {
                return lowestWinner;  // Win with lowest winning card
            }
        }
        
        // Can't/won't win - DUCK with lowest VALUE card
        return duckWithLowestValueCard(followCards);
    }
    
    /**
     * Chooses best card to discard when we can't follow suit.
     * 
     * <p><strong>Discard Priority (best to worst):</strong></p>
     * <ol>
     *   <li><strong>Worthless cards first</strong> (2-9 of non-trump) - NO POINTS</li>
     *   <li><strong>Queen</strong> (2 points, weak lead)</li>
     *   <li><strong>10</strong> (10 points hurts, but less useful than court cards)</li>
     *   <li><strong>King</strong> (3 points, strong lead - try to save!)</li>
     *   <li><strong>Ace</strong> (4 points, BEST lead - really want to save!)</li>
     *   <li><strong>Jack of trump</strong> (LAST RESORT - never if possible!)</li>
     * </ol>
     * 
     * @param validCards cards we can play
     * @param trump the trump suit
     * @return the best card to discard
     */
    private static Card chooseBestDiscard(List<Card> validCards, Card.Suit trump) {
        
        // PRIORITY 1: Worthless cards (2-9) of non-trump - ZERO POINTS
        Card worthless = validCards.stream()
            .filter(card -> card.getSuit() != trump)
            .filter(SimpleAI::isWorthlessCard)
            .findFirst()
            .orElse(null);
        
        if (worthless != null) {
            return worthless;  // Perfect discard!
        }
        
        // PRIORITY 2: Queen (only 2 points, weak for leading)
        Card queen = validCards.stream()
            .filter(card -> card.getRank() == Card.Rank.QUEEN)
            .filter(card -> card.getSuit() != trump)
            .findFirst()
            .orElse(null);
        
        if (queen != null) {
            return queen;
        }
        
        // PRIORITY 3: 10 (10 points hurts, but better than losing K or A)
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
        return validCards.get(0);
    }
    
    /**
     * Ducks (plays low) with the lowest VALUE card.
     * 
     * <p>Prefers worthless cards (2-9) over point cards (10/J/Q/K/A).</p>
     * 
     * @param cards cards to choose from
     * @return lowest value card
     */
    private static Card duckWithLowestValueCard(List<Card> cards) {
        // FIRST: Try to find worthless card (2-9)
        Card worthless = cards.stream()
            .filter(SimpleAI::isWorthlessCard)
            .min(Comparator.comparingInt(card -> card.getRank().getValue()))
            .orElse(null);
        
        if (worthless != null) {
            return worthless;  // Duck with garbage!
        }
        
        // NO worthless cards - must sacrifice a point card
        // Choose lowest POINT value (Q=2, K=3, A=4, 10=10, J=1)
        return cards.stream()
            .min(Comparator.comparingInt(card -> card.getRank().getPoints()))
            .orElse(cards.get(0));
    }
    
    /**
     * Checks if a card is worthless (has no point value).
     * 
     * @param card the card to check
     * @return true if card is 2-9 (worthless)
     */
    private static boolean isWorthlessCard(Card card) {
        int value = card.getRank().getValue();
        return value >= 2 && value <= 9;  // 2, 3, 4, 5, 6, 7, 8, 9
    }
    
    /**
     * Calculates total point value of cards in a trick.
     * 
     * @param trick the trick
     * @return total points (Ace=4, King=3, Queen=2, Jack=1, Ten=10)
     */
    private static int calculateTrickValue(Trick trick) {
        int value = 0;
        for (Trick.CardPlay play : trick.getPlays()) {
            value += play.card.getRank().getPoints();
        }
        return value;
    }
    
    /**
     * Determines the current winning card in a trick.
     * 
     * @param trick the trick
     * @param trump the trump suit
     * @return the currently winning card
     */
    private static Card getCurrentWinningCard(Trick trick, Card.Suit trump) {
        if (trick.getPlays().isEmpty()) {
            return null;
        }
        
        Trick.CardPlay winner = trick.getPlays().get(0);
        for (Trick.CardPlay play : trick.getPlays()) {
            if (beats(play.card, winner.card, trick.getLeadSuit(), trump)) {
                winner = play;
            }
        }
        
        return winner.card;
    }
    
    /**
     * Determines whether card1 beats card2.
     * 
     * @param card1 the first card
     * @param card2 the second card
     * @param leadSuit the lead suit
     * @param trump the trump suit
     * @return true if card1 beats card2
     */
    private static boolean beats(Card card1, Card card2, Card.Suit leadSuit, Card.Suit trump) {
        // Trump beats non-trump
        if (card1.getSuit() == trump && card2.getSuit() != trump) {
            return true;
        }
        if (card2.getSuit() == trump && card1.getSuit() != trump) {
            return false;
        }
        
        // Both same suit (trump or lead) - higher rank wins
        if (card1.getSuit() == card2.getSuit()) {
            return card1.getRank().getValue() > card2.getRank().getValue();
        }
        
        // Different suits, neither trump - can't beat
        return false;
    }
    
    /**
     * Checks if our card can beat the current winner.
     * 
     * @param ourCard our card
     * @param currentWinner the current winning card
     * @param trump the trump suit
     * @return true if our card can win
     */
    private static boolean canBeat(Card ourCard, Card currentWinner, Card.Suit trump) {
        if (currentWinner == null) {
            return true;  // We're first, we win by default
        }
        
        // Trump beats non-trump
        if (ourCard.getSuit() == trump && currentWinner.getSuit() != trump) {
            return true;
        }
        if (currentWinner.getSuit() == trump && ourCard.getSuit() != trump) {
            return false;
        }
        
        // Same suit - higher rank wins
        if (ourCard.getSuit() == currentWinner.getSuit()) {
            return ourCard.getRank().getValue() > currentWinner.getRank().getValue();
        }
        
        // Different suits - can't beat
        return false;
    }
}
