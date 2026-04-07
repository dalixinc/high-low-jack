package com.dalegames.highlowjack.model;

/**
 * Represents the game mode for High Low Jack.
 * 
 * <p>In INDIVIDUAL mode, players compete separately and points are awarded
 * to individual players.
 * 
 * <p>In TEAM mode, players are grouped into two teams (North-South vs East-West)
 * and points are awarded to teams, though individual winners are still
 * recognized for glory.
 * 
 * @author Dale &amp; Primus
 * @version 1.0
 */
public enum GameMode {
    /**
     * Individual play mode - 4 players compete separately.
     * First player to 11 points wins the set.
     */
    INDIVIDUAL,
    
    /**
     * Team play mode - 2 teams of 2 players each.
     * Players 1 & 3 (North-South) vs Players 2 & 4 (East-West).
     * First team to 11 points wins the set.
     */
    TEAM
}
