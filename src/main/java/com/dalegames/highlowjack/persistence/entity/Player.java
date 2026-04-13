package com.dalegames.highlowjack.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Player entity - represents a player profile with lifetime statistics.
 * 
 * @author Dale & Primus
 * @version 1.2 - Added isTeam flag to separate players from teams
 */
@Entity
@Table(name = "players")
public class Player {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    // Flag to distinguish players from teams
    @Column(name = "is_team")
    private boolean isTeam = false;
    
    // Lifetime Statistics
    @Column(name = "total_matches_played")
    private int totalMatchesPlayed = 0;
    
    @Column(name = "total_matches_won")
    private int totalMatchesWon = 0;
    
    @Column(name = "total_sets_played")
    private int totalSetsPlayed = 0;
    
    @Column(name = "total_sets_won")
    private int totalSetsWon = 0;
    
    @Column(name = "total_rounds_played")
    private int totalRoundsPlayed = 0;
    
    // Point Category Totals
    @Column(name = "highs_won")
    private int highsWon = 0;
    
    @Column(name = "lows_won")
    private int lowsWon = 0;
    
    @Column(name = "jacks_won")
    private int jacksWon = 0;
    
    @Column(name = "games_won")
    private int gamesWon = 0;
    
    @Column(name = "total_points")
    private int totalPoints = 0;
    
    // Win Streaks
    @Column(name = "current_win_streak")
    private int currentWinStreak = 0;
    
    @Column(name = "longest_win_streak")
    private int longestWinStreak = 0;
    
    // Style & Personality
    @Column(name = "favorite_suit", length = 10)
    private String favoriteSuit;
    
    @Column(name = "signature_move", length = 100)
    private String signatureMove;
    
    @Column(name = "total_twos_cut")
    private int totalTwosCut = 0;
    
    @Column(name = "total_ace_spades_played")
    private int totalAceSpadesPlayed = 0;

    @Column(name = "total_aces_cut")
    private int totalAcesCut = 0;

    @Column(name = "total_cuts_won")
    private int totalCutsWon = 0;

    // Special Awards
    @Column(name = "sweeps_won")
    private int sweepsWon = 0;

    @Column(name = "close_set_wins")
    private int closeSetWins = 0;

    @Column(name = "failed_from_10")
    private int failedFrom10 = 0;

    // Metadata
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "last_played")
    private LocalDateTime lastPlayed = LocalDateTime.now();
    
    // Constructors
    public Player() {}
    
    public Player(String name) {
        this.name = name;
    }
    
    // Business Methods
    
    /**
     * Updates stats after a match win.
     */
    public void recordMatchWin(int setsWon) {
        this.totalMatchesPlayed++;
        this.totalMatchesWon++;
        this.totalSetsWon += setsWon;
        this.currentWinStreak++;
        
        if (this.currentWinStreak > this.longestWinStreak) {
            this.longestWinStreak = this.currentWinStreak;
        }
        
        this.lastPlayed = LocalDateTime.now();
    }
    
    /**
     * Updates stats after a match loss.
     */
    public void recordMatchLoss(int setsWon) {
        this.totalMatchesPlayed++;
        this.totalSetsWon += setsWon;
        this.currentWinStreak = 0;
        this.lastPlayed = LocalDateTime.now();
    }
    
    /**
     * Records a round point won.
     */
    public void recordPoint(String category) {
        this.totalPoints++;
        
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
     * Records a two being cut (Preezbob's specialty!).
     */
    public void recordTwoCut() {
        this.totalTwosCut++;
    }
    
    /**
     * Records Ace of Spades being played.
     */
    public void recordAceSpadesPlayed() {
        this.totalAceSpadesPlayed++;
    }

    /**
     * Records a cut result for this player.
     * @param cutAnAce true if this player cut an Ace
     * @param won true if this player won the cut
     */
    public void recordCutResult(boolean cutAnAce, boolean won) {
        if (cutAnAce) this.totalAcesCut++;
        if (won) this.totalCutsWon++;
    }
    
    /** Records a round where this player/team won all four points. */
    public void recordSweep() {
        this.sweepsWon++;
    }

    /** Records a set win where the loser had 9 or 10 points (close win). */
    public void recordCloseSetWin() {
        this.closeSetWins++;
    }

    /** Records a set loss where this player/team finished with 10 points. */
    public void recordFailedFrom10() {
        this.failedFrom10++;
    }

    /**
     * Calculates win percentage.
     */
    public double getWinPercentage() {
        if (totalMatchesPlayed == 0) return 0.0;
        return (double) totalMatchesWon / totalMatchesPlayed * 100.0;
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
    
    public boolean isTeam() {
        return isTeam;
    }
    
    public void setTeam(boolean team) {
        isTeam = team;
    }
    
    public int getTotalMatchesPlayed() {
        return totalMatchesPlayed;
    }
    
    public void setTotalMatchesPlayed(int totalMatchesPlayed) {
        this.totalMatchesPlayed = totalMatchesPlayed;
    }
    
    public int getTotalMatchesWon() {
        return totalMatchesWon;
    }
    
    public void setTotalMatchesWon(int totalMatchesWon) {
        this.totalMatchesWon = totalMatchesWon;
    }
    
    public int getTotalSetsPlayed() {
        return totalSetsPlayed;
    }
    
    public void setTotalSetsPlayed(int totalSetsPlayed) {
        this.totalSetsPlayed = totalSetsPlayed;
    }
    
    public int getTotalSetsWon() {
        return totalSetsWon;
    }
    
    public void setTotalSetsWon(int totalSetsWon) {
        this.totalSetsWon = totalSetsWon;
    }
    
    public int getTotalRoundsPlayed() {
        return totalRoundsPlayed;
    }
    
    public void setTotalRoundsPlayed(int totalRoundsPlayed) {
        this.totalRoundsPlayed = totalRoundsPlayed;
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
    
    public int getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
    
    public int getCurrentWinStreak() {
        return currentWinStreak;
    }
    
    public void setCurrentWinStreak(int currentWinStreak) {
        this.currentWinStreak = currentWinStreak;
    }
    
    public int getLongestWinStreak() {
        return longestWinStreak;
    }
    
    public void setLongestWinStreak(int longestWinStreak) {
        this.longestWinStreak = longestWinStreak;
    }
    
    public String getFavoriteSuit() {
        return favoriteSuit;
    }
    
    public void setFavoriteSuit(String favoriteSuit) {
        this.favoriteSuit = favoriteSuit;
    }
    
    public String getSignatureMove() {
        return signatureMove;
    }
    
    public void setSignatureMove(String signatureMove) {
        this.signatureMove = signatureMove;
    }
    
    public int getTotalTwosCut() {
        return totalTwosCut;
    }
    
    public void setTotalTwosCut(int totalTwosCut) {
        this.totalTwosCut = totalTwosCut;
    }
    
    public int getTotalAceSpadesPlayed() {
        return totalAceSpadesPlayed;
    }

    public void setTotalAceSpadesPlayed(int totalAceSpadesPlayed) {
        this.totalAceSpadesPlayed = totalAceSpadesPlayed;
    }

    public int getTotalAcesCut() {
        return totalAcesCut;
    }

    public void setTotalAcesCut(int totalAcesCut) {
        this.totalAcesCut = totalAcesCut;
    }

    public int getTotalCutsWon() {
        return totalCutsWon;
    }

    public void setTotalCutsWon(int totalCutsWon) {
        this.totalCutsWon = totalCutsWon;
    }

    public int getSweepsWon() {
        return sweepsWon;
    }

    public void setSweepsWon(int sweepsWon) {
        this.sweepsWon = sweepsWon;
    }

    public int getCloseSetWins() {
        return closeSetWins;
    }

    public void setCloseSetWins(int closeSetWins) {
        this.closeSetWins = closeSetWins;
    }

    public int getFailedFrom10() {
        return failedFrom10;
    }

    public void setFailedFrom10(int failedFrom10) {
        this.failedFrom10 = failedFrom10;
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
        return "Player{" +
                "name='" + name + '\'' +
                ", wins=" + totalMatchesWon +
                ", played=" + totalMatchesPlayed +
                ", winPct=" + String.format("%.1f", getWinPercentage()) +
                ", streak=" + currentWinStreak +
                ", isTeam=" + isTeam +
                '}';
    }
}
