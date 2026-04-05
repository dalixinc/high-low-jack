package com.dalegames.highlowjack.model;

import java.io.Serializable;

/**
 * Represents a notable game event that might trigger a quip or achievement.
 * Events are tracked during gameplay and cleared after each round.
 * 
 * @author Dale & Primus
 * @version 1.0
 */
public class GameEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Types of events that can occur during gameplay.
     */
    public enum EventType {
        /** First card of round played (sets trump) */
        TRUMP_SET,
        
        /** A two was pitched (first card) */
        TWO_PITCHED,
        
        /** Ace of Spades was played */
        ACE_SPADES_PLAYED,
        
        /** Jack of trump was won in a trick */
        JACK_WON,
        
        /** A trick was won */
        TRICK_WON
    }
    
    private final EventType type;
    private final String playerName;
    private final Card card;
    private final int currentScore;  // Player's score when event happened
    
    /**
     * Creates a new game event.
     * 
     * @param type the type of event
     * @param playerName the player involved
     * @param card the card involved (can be null for some events)
     * @param currentScore the player's current score
     */
    public GameEvent(EventType type, String playerName, Card card, int currentScore) {
        this.type = type;
        this.playerName = playerName;
        this.card = card;
        this.currentScore = currentScore;
    }
    
    // Getters
    
    public EventType getType() {
        return type;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public Card getCard() {
        return card;
    }
    
    public int getCurrentScore() {
        return currentScore;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s played %s (score: %d)", 
            type, playerName, card, currentScore);
    }
}
