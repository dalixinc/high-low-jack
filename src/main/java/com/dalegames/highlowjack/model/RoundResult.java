package com.dalegames.highlowjack.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the complete results of a scored round for display purposes.
 * 
 * @author Dale &amp; Primus
 * @version 2.1
 */
public class RoundResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final Map<String, List<Card>> capturedCards;
    private final Map<String, Integer> gamePointTotals;
    private final Map<String, String> roundPointWinners;
    private final Map<String, Integer> updatedScores;
    private final Card.Suit trumpSuit;
    
    private final Card highCard;
    private final Card lowCard;
    private final Integer gameWinnerPoints;
    
    public RoundResult(
            Map<String, List<Card>> capturedCards,
            Map<String, Integer> gamePointTotals,
            Map<String, String> roundPointWinners,
            Map<String, Integer> updatedScores,
            Card.Suit trumpSuit,
            Card highCard,
            Card lowCard,
            Integer gameWinnerPoints) {
        
        this.capturedCards = new HashMap<>(capturedCards);
        this.gamePointTotals = new HashMap<>(gamePointTotals);
        this.roundPointWinners = new HashMap<>(roundPointWinners);
        this.updatedScores = new HashMap<>(updatedScores);
        this.trumpSuit = trumpSuit;
        this.highCard = highCard;
        this.lowCard = lowCard;
        this.gameWinnerPoints = gameWinnerPoints;
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
    
    public String getRoundPointWinner(String category) {
        return roundPointWinners.get(category);
    }
    
    public int getScore(String playerName) {
        return updatedScores.getOrDefault(playerName, 0);
    }
    
    public Map<String, String> getRoundPointWinners() {
        return new HashMap<>(roundPointWinners);
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
