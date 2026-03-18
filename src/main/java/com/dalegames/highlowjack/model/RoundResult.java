package com.dalegames.highlowjack.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the complete results of a scored round for display purposes.
 * 
 * <p>Contains all information needed to show the scoring screen:
 * <ul>
 *   <li>Cards captured by each player</li>
 *   <li>Game points earned by each player (card values)</li>
 *   <li>Round points awarded (High/Low/Jack/Game winners)</li>
 *   <li>Updated game scores</li>
 * </ul>
 * </p>
 * 
 * @author Dale &amp; Primus
 * @version 1.0
 */
public class RoundResult implements Serializable {
    
    private final Map<String, List<Card>> capturedCards;
    private final Map<String, Integer> gamePointTotals;
    private final Map<String, String> roundPointWinners;  // "High" -> "Dale", etc.
    private final Map<String, Integer> updatedScores;
    private final Card.Suit trumpSuit;
    
    /**
     * Creates a new round result.
     * 
     * @param capturedCards cards captured by each player
     * @param gamePointTotals game points (card values) per player
     * @param roundPointWinners winners of High/Low/Jack/Game
     * @param updatedScores current game scores after this round
     * @param trumpSuit the trump suit for this round
     */
    public RoundResult(
            Map<String, List<Card>> capturedCards,
            Map<String, Integer> gamePointTotals,
            Map<String, String> roundPointWinners,
            Map<String, Integer> updatedScores,
            Card.Suit trumpSuit) {
        
        this.capturedCards = new HashMap<>(capturedCards);
        this.gamePointTotals = new HashMap<>(gamePointTotals);
        this.roundPointWinners = new HashMap<>(roundPointWinners);
        this.updatedScores = new HashMap<>(updatedScores);
        this.trumpSuit = trumpSuit;
    }
    
    /**
     * Gets all cards captured by a player.
     * 
     * @param playerName the player's name
     * @return list of captured cards (empty if none)
     */
    public List<Card> getCapturedCards(String playerName) {
        return capturedCards.getOrDefault(playerName, new ArrayList<>());
    }
    
    /**
     * Gets the total game points (card values) for a player.
     * 
     * @param playerName the player's name
     * @return game points (0 if none)
     */
    public int getGamePoints(String playerName) {
        return gamePointTotals.getOrDefault(playerName, 0);
    }
    
    /**
     * Gets the winner of a specific round point category.
     * 
     * @param category "High", "Low", "Jack", or "Game"
     * @return winning player's name, or null if no winner
     */
    public String getRoundPointWinner(String category) {
        return roundPointWinners.get(category);
    }
    
    /**
     * Gets a player's updated score after this round.
     * 
     * @param playerName the player's name
     * @return current game score
     */
    public int getScore(String playerName) {
        return updatedScores.getOrDefault(playerName, 0);
    }
    
    /**
     * Gets all round point winners.
     * 
     * @return map of category to winner ("High" -> "Dale", etc.)
     */
    public Map<String, String> getRoundPointWinners() {
        return new HashMap<>(roundPointWinners);
    }
    
    /**
     * Gets all captured cards by player.
     * 
     * @return map of player name to list of captured cards
     */
    public Map<String, List<Card>> getAllCapturedCards() {
        return new HashMap<>(capturedCards);
    }
    
    /**
     * Gets all game point totals.
     * 
     * @return map of player name to game points
     */
    public Map<String, Integer> getGamePointTotals() {
        return new HashMap<>(gamePointTotals);
    }
    
    /**
     * Gets all updated scores.
     * 
     * @return map of player name to game score
     */
    public Map<String, Integer> getUpdatedScores() {
        return new HashMap<>(updatedScores);
    }
    
    /**
     * Gets the trump suit for this round.
     * 
     * @return the trump suit
     */
    public Card.Suit getTrumpSuit() {
        return trumpSuit;
    }
}
