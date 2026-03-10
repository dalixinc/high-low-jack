package com.dalegames.highlowjack.model;

/**
 * Represents a standard playing card with a suit and rank.
 * 
 * <p>Cards are immutable and use enums for both suit and rank to ensure type safety.
 * The rank values range from 2 (lowest) to ACE (highest) for game comparison purposes.</p>
 * 
 * @author Dale & Primus
 * @version 1.0
 */
public class Card {
    private final Suit suit;
    private final Rank rank;

    /**
     * Constructs a new Card with the specified suit and rank.
     * 
     * @param suit the suit of the card (HEARTS, DIAMONDS, CLUBS, or SPADES)
     * @param rank the rank of the card (TWO through ACE)
     * @throws NullPointerException if suit or rank is null
     */
    public Card(Suit suit, Rank rank) {
        if (suit == null || rank == null) {
            throw new NullPointerException("Suit and rank cannot be null");
        }
        this.suit = suit;
        this.rank = rank;
    }

    /**
     * Returns the suit of this card.
     * 
     * @return the suit (HEARTS, DIAMONDS, CLUBS, or SPADES)
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Returns the rank of this card.
     * 
     * @return the rank (TWO through ACE)
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Returns a string representation of this card.
     * Format: "RANK SUIT_SYMBOL" (e.g., "A♠", "K♥")
     * 
     * @return a human-readable string representation of the card
     */
    @Override
    public String toString() {
        return rank + suit.getSymbol();
    }

    /**
     * Compares this card to another object for equality.
     * Two cards are equal if they have the same suit and rank.
     * 
     * @param obj the object to compare with
     * @return true if the cards have the same suit and rank
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Card)) return false;
        Card other = (Card) obj;
        return suit == other.suit && rank == other.rank;
    }

    /**
     * Returns a hash code for this card.
     * 
     * @return a hash code based on suit and rank
     */
    @Override
    public int hashCode() {
        return 31 * suit.hashCode() + rank.hashCode();
    }

    /**
     * Enumeration of the four card suits.
     * Each suit has a Unicode symbol for display purposes.
     */
    public enum Suit {
        /** Hearts suit (♥) */
        HEARTS("♥"),
        /** Diamonds suit (♦) */
        DIAMONDS("♦"),
        /** Clubs suit (♣) */
        CLUBS("♣"),
        /** Spades suit (♠) */
        SPADES("♠");
        
        private final String symbol;
        
        Suit(String symbol) {
            this.symbol = symbol;
        }
        
        /**
         * Returns the Unicode symbol for this suit.
         * 
         * @return the suit symbol (♥, ♦, ♣, or ♠)
         */
        public String getSymbol() {
            return symbol;
        }
    }

    /**
     * Enumeration of card ranks from TWO (lowest) to ACE (highest).
     * Each rank has a numeric value used for comparison in trick-taking.
     */
    public enum Rank {
        /** Rank 2 (value 2) */
        TWO(2),
        /** Rank 3 (value 3) */
        THREE(3),
        /** Rank 4 (value 4) */
        FOUR(4),
        /** Rank 5 (value 5) */
        FIVE(5),
        /** Rank 6 (value 6) */
        SIX(6),
        /** Rank 7 (value 7) */
        SEVEN(7),
        /** Rank 8 (value 8) */
        EIGHT(8),
        /** Rank 9 (value 9) */
        NINE(9),
        /** Rank 10 (value 10) */
        TEN(10),
        /** Jack (value 11) */
        JACK(11),
        /** Queen (value 12) */
        QUEEN(12),
        /** King (value 13) */
        KING(13),
        /** Ace (value 14, highest) */
        ACE(14);
        
        private final int value;
        
        Rank(int value) {
            this.value = value;
        }
        
        /**
         * Returns the numeric value of this rank for comparison purposes.
         * Higher values indicate higher ranks (ACE = 14 is highest).
         * 
         * @return the numeric value (2-14)
         */
        public int getValue() {
            return value;
        }
    }
}
