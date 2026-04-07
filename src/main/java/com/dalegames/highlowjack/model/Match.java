package com.dalegames.highlowjack.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks match progress across multiple sets.
 * 
 * <p>Uses GameSetup.MatchType to avoid duplicate enums.</p>
 * 
 * @author Dale &amp; Primus
 * @version 1.1 - Match tracking using existing GameSetup.MatchType
 */
public class Match implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final GameSetup.MatchType matchType;
    private final Map<String, Integer> setWins;           // Player/Team → sets won
    private final List<SetResult> setHistory;             // History of completed sets
    private String matchWinner;                           // null until match is won
    private int currentSetNumber;
    
    /**
     * Creates a new match.
     * 
     * @param matchType the type of match from GameSetup
     */
    public Match(GameSetup.MatchType matchType) {
        this.matchType = matchType;
        this.setWins = new HashMap<>();
        this.setHistory = new ArrayList<>();
        this.matchWinner = null;
        this.currentSetNumber = 1;
    }
    
    /**
     * Records a completed set and checks for match winner.
     * 
     * @param setResult the result of the completed set
     * @return true if this set win resulted in a match win
     */
    public boolean recordSetWin(SetResult setResult) {
        if (setResult == null) {
            throw new IllegalArgumentException("SetResult cannot be null");
        }
        
        if (matchWinner != null) {
            throw new IllegalStateException("Match is already complete");
        }
        
        // Add to history
        setHistory.add(setResult);
        
        // Increment set wins for winner
        String winner = setResult.getWinner();
        setWins.put(winner, setWins.getOrDefault(winner, 0) + 1);
        
        // Check for match winner
        int setsWon = setWins.get(winner);
        if (setsWon >= matchType.getSetsToWin()) {
            matchWinner = winner;
            return true;
        }
        
        // Prepare for next set
        currentSetNumber++;
        return false;
    }
    
    /**
     * Gets the number of sets a player/team has won.
     */
    public int getSetsWon(String playerOrTeam) {
        return setWins.getOrDefault(playerOrTeam, 0);
    }
    
    public int getCurrentSetNumber() {
        return currentSetNumber;
    }
    
    public GameSetup.MatchType getMatchType() {
        return matchType;
    }
    
    public boolean isComplete() {
        return matchWinner != null;
    }
    
    public String getMatchWinner() {
        return matchWinner;
    }
    
    public List<SetResult> getSetHistory() {
        return new ArrayList<>(setHistory);
    }
    
    public Map<String, Integer> getSetWins() {
        return new HashMap<>(setWins);
    }
    
    /**
     * Gets the current match score as a string.
     */
    public String getMatchScore(String player1, String player2) {
        int p1Wins = getSetsWon(player1);
        int p2Wins = getSetsWon(player2);
        return p1Wins + "-" + p2Wins;
    }
}