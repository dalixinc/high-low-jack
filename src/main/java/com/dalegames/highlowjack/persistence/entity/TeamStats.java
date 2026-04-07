package com.dalegames.highlowjack.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Team statistics entity - tracks team performance.
 * Teams are dynamically created from player combinations.
 * 
 * @author Dale & Primus
 * @version 1.0
 */
@Entity
@Table(name = "team_stats")
public class TeamStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String name;  // e.g., "North-South", "All Stars", "Young Pros"
    
    // Team Composition (stored as comma-separated)
    @Column(name = "player1", length = 50)
    private String player1;
    
    @Column(name = "player2", length = 50)
    private String player2;
    
    // Match Statistics
    @Column(name = "matches_played")
    private int matchesPlayed = 0;
    
    @Column(name = "matches_won")
    private int matchesWon = 0;
    
    @Column(name = "sets_won")
    private int setsWon = 0;
    
    // Point Statistics
    @Column(name = "highs_won")
    private int highsWon = 0;
    
    @Column(name = "lows_won")
    private int lowsWon = 0;
    
    @Column(name = "jacks_won")
    private int jacksWon = 0;
    
    @Column(name = "games_won")
    private int gamesWon = 0;
    
    // Streaks
    @Column(name = "current_streak")
    private int currentStreak = 0;
    
    @Column(name = "longest_streak")
    private int longestStreak = 0;
    
    // Metadata
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "last_played")
    private LocalDateTime lastPlayed = LocalDateTime.now();
    
    // Constructors
    public TeamStats() {}
    
    public TeamStats(String name, String player1, String player2) {
        this.name = name;
        this.player1 = player1;
        this.player2 = player2;
    }
    
    // Business Methods
    
    /**
     * Records a match win.
     */
    public void recordMatchWin(int setsWon) {
        this.matchesPlayed++;
        this.matchesWon++;
        this.setsWon += setsWon;
        this.currentStreak++;
        
        if (this.currentStreak > this.longestStreak) {
            this.longestStreak = this.currentStreak;
        }
        
        this.lastPlayed = LocalDateTime.now();
    }
    
    /**
     * Records a match loss.
     */
    public void recordMatchLoss(int setsWon) {
        this.matchesPlayed++;
        this.setsWon += setsWon;
        this.currentStreak = 0;
        this.lastPlayed = LocalDateTime.now();
    }
    
    /**
     * Records a point won.
     */
    public void recordPoint(String category) {
        switch (category) {
            case "High":
                this.highsWon++;
                break;
            case "Low":
                this.lowsWon++;
                break;
            case "Jack":
                this.jacksWon++;
                break;
            case "Game":
                this.gamesWon++;
                break;
        }
    }
    
    /**
     * Calculates win percentage.
     */
    public double getWinPercentage() {
        if (matchesPlayed == 0) return 0.0;
        return (double) matchesWon / matchesPlayed * 100.0;
    }
    
    /**
     * Gets player list as formatted string.
     */
    public String getPlayerList() {
        return player1 + " & " + player2;
    }
    
    /**
     * Gets total points.
     */
    public int getTotalPoints() {
        return highsWon + lowsWon + jacksWon + gamesWon;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPlayer1() {
        return player1;
    }
    
    public void setPlayer1(String player1) {
        this.player1 = player1;
    }
    
    public String getPlayer2() {
        return player2;
    }
    
    public void setPlayer2(String player2) {
        this.player2 = player2;
    }
    
    public int getMatchesPlayed() {
        return matchesPlayed;
    }
    
    public void setMatchesPlayed(int matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }
    
    public int getMatchesWon() {
        return matchesWon;
    }
    
    public void setMatchesWon(int matchesWon) {
        this.matchesWon = matchesWon;
    }
    
    public int getSetsWon() {
        return setsWon;
    }
    
    public void setSetsWon(int setsWon) {
        this.setsWon = setsWon;
    }
    
    public int getHighsWon() {
        return highsWon;
    }
    
    public void setHighsWon(int highsWon) {
        this.highsWon = highsWon;
    }
    
    public int getLowsWon() {
        return lowsWon;
    }
    
    public void setLowsWon(int lowsWon) {
        this.lowsWon = lowsWon;
    }
    
    public int getJacksWon() {
        return jacksWon;
    }
    
    public void setJacksWon(int jacksWon) {
        this.jacksWon = jacksWon;
    }
    
    public int getGamesWon() {
        return gamesWon;
    }
    
    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }
    
    public int getCurrentStreak() {
        return currentStreak;
    }
    
    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }
    
    public int getLongestStreak() {
        return longestStreak;
    }
    
    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getLastPlayed() {
        return lastPlayed;
    }
    
    public void setLastPlayed(LocalDateTime lastPlayed) {
        this.lastPlayed = lastPlayed;
    }
    
    @Override
    public String toString() {
        return "TeamStats{" +
                "name='" + name + '\'' +
                ", players=" + getPlayerList() +
                ", wins=" + matchesWon +
                ", played=" + matchesPlayed +
                ", winPct=" + String.format("%.1f", getWinPercentage()) +
                '}';
    }
}
