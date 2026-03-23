package com.dalegames.highlowjack.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the setup configuration for a High Low Jack match.
 * 
 * <p>This includes the match type (best of 1/3/5), player names and types,
 * and which player is the game controller.</p>
 * 
 * @author Dale &amp; Primus
 * @version 1.0
 */
public class GameSetup implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Type of match - determines how many sets must be won.
     */
    public enum MatchType {
        SINGLE_SET("Just One Set", 1),
        BEST_OF_THREE("Best of 3 Sets", 2),
        BEST_OF_FIVE("Best of 5 Sets", 3);
        
        private final String displayName;
        private final int setsToWin;
        
        MatchType(String displayName, int setsToWin) {
            this.displayName = displayName;
            this.setsToWin = setsToWin;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getSetsToWin() {
            return setsToWin;
        }
    }
    
    private final List<PlayerInfo> players;
    private final MatchType matchType;
    
    /**
     * Creates a new GameSetup.
     * 
     * @param players the list of 4 players (must be exactly 4)
     * @param matchType the match type (SINGLE_SET, BEST_OF_THREE, or BEST_OF_FIVE)
     * @throws IllegalArgumentException if players list is not exactly 4, or if no controller is designated
     */
    public GameSetup(List<PlayerInfo> players, MatchType matchType) {
        if (players == null || players.size() != 4) {
            throw new IllegalArgumentException("Must have exactly 4 players");
        }
        if (matchType == null) {
            throw new IllegalArgumentException("Match type cannot be null");
        }
        
        // Verify at least one controller exists
        boolean hasController = players.stream().anyMatch(PlayerInfo::isController);
        if (!hasController) {
            throw new IllegalArgumentException("At least one player must be designated as controller");
        }
        
        this.players = new ArrayList<>(players);
        this.matchType = matchType;
    }
    
    /**
     * Gets the list of players.
     * 
     * @return immutable list of 4 players
     */
    public List<PlayerInfo> getPlayers() {
        return new ArrayList<>(players);
    }
    
    /**
     * Gets a specific player by index (0-3).
     * 
     * @param index the player index (0-3)
     * @return the player info
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public PlayerInfo getPlayer(int index) {
        return players.get(index);
    }
    
    /**
     * Gets the match type.
     * 
     * @return the match type
     */
    public MatchType getMatchType() {
        return matchType;
    }
    
    /**
     * Gets the number of sets needed to win the match.
     * 
     * @return 1 for single set, 2 for best of 3, 3 for best of 5
     */
    public int getSetsToWin() {
        return matchType.getSetsToWin();
    }
    
    /**
     * Gets the player names as an array.
     * 
     * @return array of 4 player names
     */
    public String[] getPlayerNames() {
        return players.stream()
                .map(PlayerInfo::getName)
                .toArray(String[]::new);
    }
    
    /**
     * Gets the game controller player.
     * 
     * @return the player info for the controller
     */
    public PlayerInfo getController() {
        return players.stream()
                .filter(PlayerInfo::isController)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No controller found"));
    }
    
    /**
     * Checks if a player name is human-controlled.
     * 
     * @param playerName the player name to check
     * @return true if the player is human
     */
    public boolean isHumanPlayer(String playerName) {
        return players.stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst()
                .map(PlayerInfo::isHuman)
                .orElse(false);
    }
    
    /**
     * Checks if a player is the game controller.
     * 
     * @param playerName the player name to check
     * @return true if this player is the controller
     */
    public boolean isController(String playerName) {
        return players.stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst()
                .map(PlayerInfo::isController)
                .orElse(false);
    }
    
    @Override
    public String toString() {
        return "GameSetup{" +
                "matchType=" + matchType.getDisplayName() +
                ", players=" + players +
                '}';
    }
}
