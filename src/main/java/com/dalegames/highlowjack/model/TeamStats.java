package com.dalegames.highlowjack.model;

/**
 * Team statistics data transfer object.
 * Represents aggregated performance data for a team pairing.
 * 
 * @author Dale & Primus
 * @version 1.0
 */
public class TeamStats {
    private String name;
    private String memberNames;
    private int totalMatches;
    private int totalWins;
    private int totalSetsWon;
    private int currentStreak;
    
    public TeamStats() {}
    
    public TeamStats(String name, String memberNames) {
        this.name = name;
        this.memberNames = memberNames;
    }
    
    public double getWinPercentage() {
        if (totalMatches == 0) return 0.0;
        return (double) totalWins / totalMatches * 100.0;
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getMemberNames() {
        return memberNames;
    }
    
    public void setMemberNames(String memberNames) {
        this.memberNames = memberNames;
    }
    
    public int getTotalMatches() {
        return totalMatches;
    }
    
    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }
    
    public int getTotalWins() {
        return totalWins;
    }
    
    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }
    
    public int getTotalSetsWon() {
        return totalSetsWon;
    }
    
    public void setTotalSetsWon(int totalSetsWon) {
        this.totalSetsWon = totalSetsWon;
    }
    
    public int getCurrentStreak() {
        return currentStreak;
    }
    
    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }
}
