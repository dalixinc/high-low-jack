package com.dalegames.highlowjack.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the complete results of a scored round for display purposes.
 * 
 * <p>In individual mode, tracks individual player scores.
 * In team mode, tracks both individual winners (for glory) and team points.
 * 
 * @author Dale &amp; Primus
 * @version 3.0 - Added team mode support
 */
public class RoundResult implements Serializable {
    
    private static final long serialVersionUID = 2L;  // Incremented for team mode
    
    private final Map<String, List<Card>> capturedCards;
    private final Map<String, Integer> gamePointTotals;
    private final Map<String, String> roundPointWinners;  // Category → Player who won it
    private final Map<String, Integer> updatedScores;     // Player/Team name → Score
    private final Card.Suit trumpSuit;
    
    private final Card highCard;
    private final Card lowCard;
    private final Integer gameWinnerPoints;
    
    // NEW: Team mode support
    private final Map<String, Integer> teamPointsThisRound;  // Team name → Points earned this round
    
    /**
     * Constructor for individual mode (backward compatible).
     */
    public RoundResult(
            Map<String, List<Card>> capturedCards,
            Map<String, Integer> gamePointTotals,
            Map<String, String> roundPointWinners,
            Map<String, Integer> updatedScores,
            Card.Suit trumpSuit,
            Card highCard,
            Card lowCard,
            Integer gameWinnerPoints) {
        
        this(capturedCards, gamePointTotals, roundPointWinners, updatedScores, 
             trumpSuit, highCard, lowCard, gameWinnerPoints, null);
    }
    
    /**
     * Constructor with team mode support.
     */
    public RoundResult(
            Map<String, List<Card>> capturedCards,
            Map<String, Integer> gamePointTotals,
            Map<String, String> roundPointWinners,
            Map<String, Integer> updatedScores,
            Card.Suit trumpSuit,
            Card highCard,
            Card lowCard,
            Integer gameWinnerPoints,
            Map<String, Integer> teamPointsThisRound) {
        
        this.capturedCards = new HashMap<>(capturedCards);
        this.gamePointTotals = new HashMap<>(gamePointTotals);
        this.roundPointWinners = new HashMap<>(roundPointWinners);
        this.updatedScores = new HashMap<>(updatedScores);
        this.trumpSuit = trumpSuit;
        this.highCard = highCard;
        this.lowCard = lowCard;
        this.gameWinnerPoints = gameWinnerPoints;
        this.teamPointsThisRound = teamPointsThisRound != null ? 
            new HashMap<>(teamPointsThisRound) : null;
    }
    
    /**
     * Gets all cards captured by a player, sorted by point value (highest to lowest).
     */
    public List<Card> getCapturedCards(String playerName) {
        List<Card> cards = capturedCards.getOrDefault(playerName, new ArrayList<>());
        
        List<Card> sorted = new ArrayList<>(cards);
        sorted.sort((a, b) -> Integer.compare(b.getRank().getPoints(), a.getRank().getPoints()));
        
        return sorted;
    }
    
    public int getGamePoints(String playerName) {
        return gamePointTotals.getOrDefault(playerName, 0);
    }
    
    /**
     * Gets the player who won a specific round point category.
     * 
     * @param category the category ("High", "Low", "Jack", or "Game")
     * @return the player name who won that category
     */
    public String getRoundPointWinner(String category) {
        return roundPointWinners.get(category);
    }
    
    public int getScore(String playerName) {
        return updatedScores.getOrDefault(playerName, 0);
    }
    
    /**
     * Gets all round point winners (individual players who won High, Low, Jack, Game).
     * 
     * @return map of category to player name
     */
    public Map<String, String> getRoundPointWinners() {
        return new HashMap<>(roundPointWinners);
    }
    
    /**
     * Gets team points earned this round (only valid in team mode).
     * 
     * @return map of team name to points earned, or null if individual mode
     */
    public Map<String, Integer> getTeamPointsThisRound() {
        return teamPointsThisRound != null ? new HashMap<>(teamPointsThisRound) : null;
    }
    
    /**
     * Checks if this result includes team scoring.
     * 
     * @return true if team mode, false if individual mode
     */
    public boolean isTeamMode() {
        return teamPointsThisRound != null;
    }
    
    public Map<String, List<Card>> getAllCapturedCards() {
        return new HashMap<>(capturedCards);
    }
    
    public Map<String, Integer> getGamePointTotals() {
        return new HashMap<>(gamePointTotals);
    }
    
    public Map<String, Integer> getUpdatedScores() {
        return new HashMap<>(updatedScores);
    }
    
    public Card.Suit getTrumpSuit() {
        return trumpSuit;
    }
    
    public Card getHighCard() {
        return highCard;
    }
    
    public Card getLowCard() {
        return lowCard;
    }
    
    public Integer getGameWinnerPoints() {
        return gameWinnerPoints;
    }
}
