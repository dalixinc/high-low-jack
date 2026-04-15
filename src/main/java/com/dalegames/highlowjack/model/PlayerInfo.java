package com.dalegames.highlowjack.model;

import java.io.Serializable;

/**
 * Represents information about a player in the game.
 *
 * @author Dale &amp; Primus
 * @version 1.1 - Added AIDifficulty
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

    private String name;
    private final PlayerType type;
    private final boolean isController;
    private final AIDifficulty difficulty;   // meaningful only for COMPUTER players

    /**
     * Creates a new PlayerInfo with default Medium difficulty for AI players.
     */
    public PlayerInfo(String name, PlayerType type, boolean isController) {
        this(name, type, isController, AIDifficulty.MEDIUM);
    }

    /**
     * Creates a new PlayerInfo with an explicit AI difficulty.
     *
     * @param name         the player's name
     * @param type         HUMAN or COMPUTER
     * @param isController whether this player is the game controller
     * @param difficulty   AI difficulty (ignored for human players)
     */
    public PlayerInfo(String name, PlayerType type, boolean isController, AIDifficulty difficulty) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Player type cannot be null");
        }
        this.name         = name.trim();
        this.type         = type;
        this.isController = isController;
        this.difficulty   = (type == PlayerType.COMPUTER && difficulty != null)
                            ? difficulty : AIDifficulty.MEDIUM;
    }
    
    /**
     * Gets the player's name.
     * 
     * @return the player name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        this.name = name.trim();
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
    
    public AIDifficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public String toString() {
        return name + " (" + type
            + (type == PlayerType.COMPUTER ? "/" + difficulty : "")
            + (isController ? ", CONTROLLER" : "") + ")";
    }
}
