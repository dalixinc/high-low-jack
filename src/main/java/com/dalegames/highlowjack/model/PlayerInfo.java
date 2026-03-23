package com.dalegames.highlowjack.model;

import java.io.Serializable;

/**
 * Represents information about a player in the game.
 * 
 * @author Dale &amp; Primus
 * @version 1.0
 */
public class PlayerInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Type of player - HUMAN or COMPUTER controlled.
     */
    public enum PlayerType {
        HUMAN,
        COMPUTER
    }
    
    private final String name;
    private final PlayerType type;
    private final boolean isController;
    
    /**
     * Creates a new PlayerInfo.
     * 
     * @param name the player's name
     * @param type the player type (HUMAN or COMPUTER)
     * @param isController whether this player is the game controller
     */
    public PlayerInfo(String name, PlayerType type, boolean isController) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Player type cannot be null");
        }
        
        this.name = name.trim();
        this.type = type;
        this.isController = isController;
    }
    
    /**
     * Gets the player's name.
     * 
     * @return the player name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the player type.
     * 
     * @return HUMAN or COMPUTER
     */
    public PlayerType getType() {
        return type;
    }
    
    /**
     * Checks if this is a human player.
     * 
     * @return true if human-controlled
     */
    public boolean isHuman() {
        return type == PlayerType.HUMAN;
    }
    
    /**
     * Checks if this is a computer player.
     * 
     * @return true if computer-controlled
     */
    public boolean isComputer() {
        return type == PlayerType.COMPUTER;
    }
    
    /**
     * Checks if this player is the game controller.
     * Game controller has exclusive access to New Game and Continue buttons.
     * 
     * @return true if this is the game controller
     */
    public boolean isController() {
        return isController;
    }
    
    @Override
    public String toString() {
        return name + " (" + type + (isController ? ", CONTROLLER" : "") + ")";
    }
}
