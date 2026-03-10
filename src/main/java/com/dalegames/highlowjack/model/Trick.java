package com.dalegames.highlowjack.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one trick (round) in the game where each of 4 players plays one card.
 * 
 * <p>The first card played determines the lead suit. The trick winner is determined
 * by trump suit and rank precedence.</p>
 * 
 * @author Dale & Primus
 * @version 1.0
 */
public class Trick {
    private final List<CardPlay> plays;
    private final Card.Suit trumpSuit;
    private Card.Suit leadSuit;

    /**
     * Constructs a new empty trick with the specified trump suit.
     * 
     * @param trumpSuit the trump suit for this trick
     * @throws NullPointerException if trumpSuit is null
     */
    public Trick(Card.Suit trumpSuit) {
        if (trumpSuit == null) {
            throw new NullPointerException("Trump suit cannot be null");
        }
        this.plays = new ArrayList<>();
        this.trumpSuit = trumpSuit;
        this.leadSuit = null;
    }

    /**
     * Plays a card in this trick for the specified player.
     * The first card played establishes the lead suit.
     * 
     * @param playerName the name of the player playing the card
     * @param card the card being played
     * @throws IllegalStateException if 4 cards have already been played
     * @throws IllegalArgumentException if playerName is null/empty or card is null
     */
    public void playCard(String playerName, Card card) {
        if (plays.size() >= 4) {
            throw new IllegalStateException("Trick already has 4 cards");
        }
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }

        // First card determines lead suit
        if (plays.isEmpty()) {
            leadSuit = card.getSuit();
        }

        plays.add(new CardPlay(playerName, card));
    }

    /**
     * Returns whether this trick is complete (4 cards played).
     * 
     * @return true if 4 players have played cards
     */
    public boolean isComplete() {
        return plays.size() == 4;
    }

    /**
     * Determines and returns the name of the player who won this trick.
     * Trump cards beat non-trump cards. Among cards of the same suit (trump or lead),
     * higher rank wins.
     * 
     * @return the winning player's name
     * @throws IllegalStateException if the trick is not yet complete
     */
    public String getWinner() {
        if (!isComplete()) {
            throw new IllegalStateException("Cannot determine winner of incomplete trick");
        }

        CardPlay winner = plays.get(0);
        
        for (CardPlay play : plays) {
            if (beats(play.card, winner.card)) {
                winner = play;
            }
        }

        return winner.playerName;
    }

    /**
     * Determines whether card1 beats card2 according to High Low Jack rules.
     * 
     * @param card1 the first card
     * @param card2 the second card
     * @return true if card1 beats card2
     */
    private boolean beats(Card card1, Card card2) {
        // Trump beats non-trump
        if (card1.getSuit() == trumpSuit && card2.getSuit() != trumpSuit) {
            return true;
        }
        if (card2.getSuit() == trumpSuit && card1.getSuit() != trumpSuit) {
            return false;
        }

        // Both trump or both same suit - higher rank wins
        if (card1.getSuit() == card2.getSuit()) {
            return card1.getRank().getValue() > card2.getRank().getValue();
        }

        // Different suits, neither trump - second card (already winning) wins
        return false;
    }

    /**
     * Returns a copy of the plays in this trick.
     * 
     * @return a new list containing the card plays
     */
    public List<CardPlay> getPlays() {
        return new ArrayList<>(plays);
    }

    /**
     * Returns the lead suit (suit of the first card played).
     * 
     * @return the lead suit, or null if no cards have been played
     */
    public Card.Suit getLeadSuit() {
        return leadSuit;
    }

    /**
     * Returns the trump suit for this trick.
     * 
     * @return the trump suit
     */
    public Card.Suit getTrumpSuit() {
        return trumpSuit;
    }

    /**
     * Returns the number of cards played so far.
     * 
     * @return the number of plays (0-4)
     */
    public int size() {
        return plays.size();
    }

    /**
     * Returns a string representation of this trick.
     * 
     * @return a formatted string showing the trump, plays, and winner (if complete)
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Trick (Trump: ").append(trumpSuit.getSymbol()).append(")\n");
        for (CardPlay play : plays) {
            sb.append("  ").append(play.playerName).append(": ").append(play.card).append("\n");
        }
        if (isComplete()) {
            sb.append("Winner: ").append(getWinner());
        }
        return sb.toString();
    }

    /**
     * Represents one card played by a player in a trick.
     */
    public static class CardPlay {
        /** The name of the player who played this card */
        public final String playerName;
        /** The card that was played */
        public final Card card;

        /**
         * Constructs a new card play.
         * 
         * @param playerName the player's name
         * @param card the card played
         */
        public CardPlay(String playerName, Card card) {
            this.playerName = playerName;
            this.card = card;
        }
    }
}
