package com.dalegames.highlowjack.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a team of two players in team mode High Low Jack.
 * 
 * <p>A team consists of two partners who sit across from each other:
 * <ul>
 *   <li>Team 1: Players 1 (North) and 3 (South)</li>
 *   <li>Team 2: Players 2 (East) and 4 (West)</li>
 * </ul>
 * 
 * <p>Points are awarded to the team, but individual player achievements
 * (who won High, Low, Jack, Game) are still tracked for glory.
 * 
 * @author Dale &amp; Primus
 * @version 1.0
 */
public class Team implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String name;
    private final String player1Name;  // North or East
    private final String player2Name;  // South or West
    private int score;
    private int setsWon;
    
    /**
     * Creates a new team with two players.
     * 
     * @param name the team name (e.g., "Team 1 (North-South)")
     * @param player1Name the first player's name
     * @param player2Name the second player's name
     */
    public Team(String name, String player1Name, String player2Name) {
        this.name = name;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.score = 0;
        this.setsWon = 0;
    }
    
    /**
     * Gets the team name.
     * 
     * @return the team name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the first player's name.
     * 
     * @return the first player name
     */
    public String getPlayer1Name() {
        return player1Name;
    }
    
    /**
     * Gets the second player's name.
     * 
     * @return the second player name
     */
    public String getPlayer2Name() {
        return player2Name;
    }
    
    /**
     * Gets both player names as a list.
     * 
     * @return list containing both player names
     */
    public List<String> getPlayerNames() {
        return Arrays.asList(player1Name, player2Name);
    }
    
    /**
     * Gets the current team score.
     * 
     * @return the current score
     */
    public int getScore() {
        return score;
    }
    
    /**
     * Sets the team score.
     * 
     * @param score the new score
     */
    public void setScore(int score) {
        this.score = score;
    }
    
    /**
     * Adds points to the team score.
     * 
     * @param points the number of points to add
     */
    public void addScore(int points) {
        this.score += points;
    }
    
    /**
     * Gets the number of sets this team has won.
     * 
     * @return the number of sets won
     */
    public int getSetsWon() {
        return setsWon;
    }
    
    /**
     * Sets the number of sets won.
     * 
     * @param setsWon the number of sets won
     */
    public void setSetsWon(int setsWon) {
        this.setsWon = setsWon;
    }
    
    /**
     * Increments the sets won counter.
     */
    public void incrementSetsWon() {
        this.setsWon++;
    }
    
    /**
     * Checks if a given player is on this team.
     * 
     * @param playerName the player name to check
     * @return true if the player is on this team, false otherwise
     */
    public boolean hasPlayer(String playerName) {
        return player1Name.equals(playerName) || player2Name.equals(playerName);
    }
    
    /**
     * Gets the partner of the specified player.
     * 
     * @param playerName the player whose partner to find
     * @return the partner's name, or null if the player is not on this team
     */
    public String getPartner(String playerName) {
        if (player1Name.equals(playerName)) {
            return player2Name;
        } else if (player2Name.equals(playerName)) {
            return player1Name;
        }
        return null;
    }
    
    /**
     * Resets the team score to zero (used when starting a new set).
     */
    public void resetScore() {
        this.score = 0;
    }
    
    @Override
    public String toString() {
        return name + " (" + player1Name + " & " + player2Name + ")";
    }
}
