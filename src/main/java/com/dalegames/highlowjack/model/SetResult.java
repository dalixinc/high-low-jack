package com.dalegames.highlowjack.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the result of a completed set (first to 11 points).
 * 
 * <p>Includes winner determination with tiebreaker logic based on 
 * point award precedence: High → Low → Jack → Game.</p>
 * 
 * @author Dale &amp; Primus
 * @version 1.0
 */
public class SetResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Point award precedence for tiebreaker.
     * Lower value = higher precedence.
     */
    private enum PointPrecedence {
        HIGH(0),
        LOW(1),
        JACK(2),
        GAME(3);
        
        private final int order;
        
        PointPrecedence(int order) {
            this.order = order;
        }
        
        public int getOrder() {
            return order;
        }
    }
    
    private final String winner;
    private final Map<String, Integer> finalScores;
    private final String winningPoint;
    private final boolean wasTiebreaker;
    
    /**
     * Creates a SetResult.
     * 
     * @param winner the name of the set winner
     * @param finalScores the final scores for all players
     * @param winningPoint the point category that secured the win (High/Low/Jack/Game)
     * @param wasTiebreaker true if tiebreaker logic was used
     */
    public SetResult(String winner, Map<String, Integer> finalScores, String winningPoint, boolean wasTiebreaker) {
        if (winner == null || winner.trim().isEmpty()) {
            throw new IllegalArgumentException("Winner cannot be null or empty");
        }
        if (finalScores == null) {
            throw new IllegalArgumentException("Final scores cannot be null");
        }
        
        this.winner = winner;
        this.finalScores = new HashMap<>(finalScores);
        this.winningPoint = winningPoint;
        this.wasTiebreaker = wasTiebreaker;
    }
    
    /**
     * Determines the set winner from round results.
     * 
     * <p>Uses tiebreaker logic: if multiple players reach 11+ in the same round,
     * the winner is determined by point precedence: High → Low → Jack → Game.</p>
     * 
     * @param currentScores the scores before the round
     * @param roundPointWinners map of point categories to winners (High/Low/Jack/Game)
     * @return SetResult if someone won, null if set continues
     */
    public static SetResult determineWinner(Map<String, Integer> currentScores, 
                                           Map<String, String> roundPointWinners) {
        if (currentScores == null || roundPointWinners == null) {
            throw new IllegalArgumentException("Scores and round winners cannot be null");
        }
        
        // Track scores as points are awarded in precedence order
        Map<String, Integer> scoresCopy = new HashMap<>(currentScores);
        
        // Award points in precedence order: High → Low → Jack → Game
        String[] precedenceOrder = {"High", "Low", "Jack", "Game"};
        
        String firstToEleven = null;
        String winningPointCategory = null;
        
        for (String category : precedenceOrder) {
            String winner = roundPointWinners.get(category);
            if (winner != null) {
                int newScore = scoresCopy.getOrDefault(winner, 0) + 1;
                scoresCopy.put(winner, newScore);
                
                // Check if this player just hit 11
                if (newScore >= 11 && firstToEleven == null) {
                    firstToEleven = winner;
                    winningPointCategory = category;
                }
            }
        }
        
        // No winner yet
        if (firstToEleven == null) {
            return null;
        }
        
        // Check if multiple players reached 11 (tiebreaker was used)
        long playersAtEleven = scoresCopy.values().stream()
                .filter(score -> score >= 11)
                .count();
        
        boolean wasTiebreaker = playersAtEleven > 1;
        
        return new SetResult(firstToEleven, scoresCopy, winningPointCategory, wasTiebreaker);
    }
    
    /**
     * Gets the winner's name.
     * 
     * @return the set winner
     */
    public String getWinner() {
        return winner;
    }
    
    /**
     * Gets the final scores for all players.
     * 
     * @return map of player names to final scores
     */
    public Map<String, Integer> getFinalScores() {
        return new HashMap<>(finalScores);
    }
    
    /**
     * Gets the score for a specific player.
     * 
     * @param playerName the player name
     * @return the player's final score
     */
    public int getScore(String playerName) {
        return finalScores.getOrDefault(playerName, 0);
    }
    
    /**
     * Gets the point category that secured the win.
     * 
     * @return High, Low, Jack, or Game
     */
    public String getWinningPoint() {
        return winningPoint;
    }
    
    /**
     * Checks if tiebreaker logic was used.
     * 
     * @return true if multiple players reached 11 in the same round
     */
    public boolean wasTiebreaker() {
        return wasTiebreaker;
    }
    
    @Override
    public String toString() {
        return "SetResult{" +
                "winner='" + winner + '\'' +
                ", winningPoint='" + winningPoint + '\'' +
                ", wasTiebreaker=" + wasTiebreaker +
                ", finalScores=" + finalScores +
                '}';
    }
}
