package com.dalegames.highlowjack.web;

import com.dalegames.highlowjack.model.Card;

/**
 * Helper class for getting card image filenames.
 * 
 * @author Dale &amp; Primus
 * @version 1.0
 */
public class CardImageHelper {
    
    /**
     * Gets the image path for a card.
     * 
     * @param card the card
     * @return the image path (e.g., "/images/cards/heart_1.png")
     */
    public static String getCardImage(Card card) {
        if (card == null) {
            return "/images/cards/back.png";
        }
        
        String suit = card.getSuit().name().toLowerCase();
        String rank = getRankNumber(card.getRank());
        
        return "/images/cards/" + suit + "_" + rank + ".png";
    }
    
    /**
     * Converts rank enum to filename format.
     * 
     * @param rank the rank
     * @return the rank string for filename
     */
    private static String getRankNumber(Card.Rank rank) {
        return switch (rank) {
            case ACE -> "1";
            case TWO -> "2";
            case THREE -> "3";
            case FOUR -> "4";
            case FIVE -> "5";
            case SIX -> "6";
            case SEVEN -> "7";
            case EIGHT -> "8";
            case NINE -> "9";
            case TEN -> "10";
            case JACK -> "jack";
            case QUEEN -> "queen";
            case KING -> "king";
        };
    }
    
    /**
     * Gets the card back image path.
     * 
     * @return the back image path
     */
    public static String getBackImage() {
        return "/images/cards/back.png";
    }
}
