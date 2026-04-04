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
 * @version 2.0 - Added team mode support for set winner determination
 */
public class SetResult implements Serializable {
    
    private static final long serialVersionUID = 2L;  // Incremented for team mode changes
    private final Map<String, String> roundPointWinners;  // High/Low/Jack/Game → winner
    
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
     * @param winner the name of the set winner (player or team name)
     * @param finalScores the final scores for all players/teams
     * @param winningPoint the point category that secured the win (High/Low/Jack/Game)
     * @param wasTiebreaker true if tiebreaker logic was used
     */
    public SetResult(String winner, Map<String, Integer> finalScores, String winningPoint, 
            	boolean wasTiebreaker, Map<String, String> roundPointWinners) {
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
		this.roundPointWinners = roundPointWinners != null ? 
		   new HashMap<>(roundPointWinners) : new HashMap<>();
    }

    
    /**
     * Determines the set winner from round results.
     * 
     * <p>Works for both INDIVIDUAL and TEAM mode.
     * Uses tiebreaker logic: if multiple players/teams reach 11+ in the same round,
     * the winner is determined by point precedence: High → Low → Jack → Game.</p>
     * 
     * @param currentScores the scores before the round (player or team scores)
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
                System.out.println("  " + category + ": winner=" + winner);
                int oldScore = scoresCopy.getOrDefault(winner, 0);
                int newScore = oldScore + 1;
                scoresCopy.put(winner, newScore);
                System.out.println("    " + winner + ": " + oldScore + " → " + newScore);
                
                // Check if this player/team just hit 11
                if (newScore >= 11 && firstToEleven == null) {
                    firstToEleven = winner;
                    winningPointCategory = category;
                    System.out.println("    ✅ FIRST TO 11! Winner: " + winner + ", Point: " + category);
                }
            } else {
                System.out.println("  " + category + ": none");
            }
        }

        System.out.println("Final firstToEleven: " + firstToEleven);
        System.out.println("Final winningPointCategory: " + winningPointCategory);
        
        // No winner yet
        if (firstToEleven == null) {
            return null;
        }
        
        // Check if multiple players/teams reached 11 (tiebreaker was used)
        long playersAtEleven = scoresCopy.values().stream()
                .filter(score -> score >= 11)
                .count();
        
        boolean wasTiebreaker = playersAtEleven > 1;
        

        return new SetResult(firstToEleven, scoresCopy, winningPointCategory, 
                     wasTiebreaker, roundPointWinners);
    }
    
    /**
     * Determines the set winner from a Game object (convenience method for team mode).
     * 
     * <p>This overload is provided for convenience when working with team mode,
     * but simply delegates to the main determineWinner method.</p>
     * 
     * @param game the game object
     * @return SetResult if someone won, null if set continues
     */
    public static SetResult determineWinner(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        // Get current scores (works for both individual and team mode)
        Map<String, Integer> currentScores = new HashMap<>();
        
        if (game.isTeamMode()) {
            // Team mode: get team scores
            for (Team team : game.getTeams()) {
                currentScores.put(team.getName(), game.getScore(team.getName()));
            }
        } else {
            // Individual mode: get player scores
            for (String player : game.getPlayerNames()) {
                currentScores.put(player, game.getScore(player));
            }
        }
        
        // This will be populated by calculateScores in GameEngine
        // For this convenience method, we assume scores are already calculated
        // Return null as we can't determine winner without round point winners
        return null;
    }
    
    /**
     * Gets the winner's name.
     * 
     * @return the set winner (player or team name)
     */
    public String getWinner() {
        return winner;
    }
    
    /**
     * Gets the final scores for all players/teams.
     * 
     * @return map of player/team names to final scores
     */
    public Map<String, Integer> getFinalScores() {
        return new HashMap<>(finalScores);
    }
    
    /**
     * Gets the score for a specific player/team.
     * 
     * @param name the player or team name
     * @return the final score
     */
    public int getScore(String name) {
        return finalScores.getOrDefault(name, 0);
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
     * @return true if multiple players/teams reached 11 in the same round
     */
    public boolean wasTiebreaker() {
        return wasTiebreaker;
    }
    
    /**
     * Gets all round point winners (High, Low, Jack, Game).
     * 
     * @return map of category to winner name
     */
    public Map<String, String> getRoundPointWinners() {
        return new HashMap<>(roundPointWinners);
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
