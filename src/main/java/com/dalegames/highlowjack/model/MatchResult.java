package com.dalegames.highlowjack.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the complete result of a finished match.
 * 
 * <p>Contains all information needed to display an epic victory screen,
 * including match winner, final score, set history, and statistics.</p>
 * 
 * @author Dale &amp; Primus
 * @version 1.1 - Initial match result system
 */
public class MatchResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String winner;
    private final GameSetup.MatchType matchType;
    private final Map<String, Integer> finalSetWins;      // Player/Team → sets won
    private final List<SetSummary> setSummaries;          // Summary of each set
    private final Map<String, MatchStats> playerStats;    // Player/Team → statistics
    
    /**
     * Summary of a single set for display.
     */
    public static class SetSummary implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final int setNumber;
        private final String winner;
        private final Map<String, Integer> finalScores;   // Player/Team → final score
        private final String winningPoint;                // High/Low/Jack/Game
        private final boolean wasTiebreaker;
        
        public SetSummary(int setNumber, SetResult result) {
            this.setNumber = setNumber;
            this.winner = result.getWinner();
            this.finalScores = result.getFinalScores();
            this.winningPoint = result.getWinningPoint();
            this.wasTiebreaker = result.wasTiebreaker();
        }
        
        public int getSetNumber() { return setNumber; }
        public String getWinner() { return winner; }
        public Map<String, Integer> getFinalScores() { return new HashMap<>(finalScores); }
        public String getWinningPoint() { return winningPoint; }
        public boolean wasTiebreaker() { return wasTiebreaker; }
        
        public String getScoreDisplay(String player1, String player2) {
            int p1Score = finalScores.getOrDefault(player1, 0);
            int p2Score = finalScores.getOrDefault(player2, 0);
            return p1Score + "-" + p2Score;
        }
    }
    
    /**
     * Match statistics for a player/team.
     */
    public static class MatchStats implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final String playerOrTeam;
        private int totalHighs;
        private int totalLows;
        private int totalJacks;
        private int totalGames;
        private int totalPoints;
        private int setsWon;
        
        public MatchStats(String playerOrTeam) {
            this.playerOrTeam = playerOrTeam;
        }
        
        public void recordSet(SetResult result, String playerOrTeam) {
            Map<String, String> winners = result.getRoundPointWinners();

            // Count point types won
            for (Map.Entry<String, String> entry : winners.entrySet()) {
                if (entry.getValue() != null && entry.getValue().equals(playerOrTeam)) {
                    switch (entry.getKey()) {
                        case "High": totalHighs++; break;
                        case "Low": totalLows++; break;
                        case "Jack": totalJacks++; break;
                        case "Game": totalGames++; break;
                    }
                }
            }
            
            // Add final score
            totalPoints += result.getScore(playerOrTeam);
            
            // Check if won this set
            if (result.getWinner().equals(playerOrTeam)) {
                setsWon++;
            }
        }
        
        public String getPlayerOrTeam() { return playerOrTeam; }
        public int getTotalHighs() { return totalHighs; }
        public int getTotalLows() { return totalLows; }
        public int getTotalJacks() { return totalJacks; }
        public int getTotalGames() { return totalGames; }
        public int getTotalPoints() { return totalPoints; }
        public int getSetsWon() { return setsWon; }
    }
    
    /**
     * Creates a match result from a completed match.
     * 
     * @param match the completed match
     */
    public MatchResult(Match match) {
        if (!match.isComplete()) {
            throw new IllegalArgumentException("Match must be complete");
        }
        
        this.winner = match.getMatchWinner();
        this.matchType = match.getMatchType();
        this.finalSetWins = match.getSetWins();
        this.setSummaries = new ArrayList<>();
        this.playerStats = new HashMap<>();
        
        // Build set summaries
        List<SetResult> history = match.getSetHistory();
        for (int i = 0; i < history.size(); i++) {
            setSummaries.add(new SetSummary(i + 1, history.get(i)));
        }
        
        // Calculate statistics
        for (SetResult result : history) {
            for (String playerOrTeam : result.getFinalScores().keySet()) {
                MatchStats stats = playerStats.computeIfAbsent(
                    playerOrTeam, 
                    k -> new MatchStats(playerOrTeam)
                );
                stats.recordSet(result, playerOrTeam);
            }
        }
    }
    
    public String getWinner() {
        return winner;
    }
    
    public GameSetup.MatchType getMatchType() {
        return matchType;
    }
    
    public Map<String, Integer> getFinalSetWins() {
        return new HashMap<>(finalSetWins);
    }
    
    public List<SetSummary> getSetSummaries() {
        return new ArrayList<>(setSummaries);
    }
    
    public MatchStats getStats(String playerOrTeam) {
        return playerStats.get(playerOrTeam);
    }
    
    public Map<String, MatchStats> getAllStats() {
        return new HashMap<>(playerStats);
    }
    
    /**
     * Gets the match score as a string.
     * 
     * @param player1 first player/team
     * @param player2 second player/team
     * @return score like "2-1"
     */
    public String getMatchScore(String player1, String player2) {
        int p1Wins = finalSetWins.getOrDefault(player1, 0);
        int p2Wins = finalSetWins.getOrDefault(player2, 0);
        return p1Wins + "-" + p2Wins;
    }
}
