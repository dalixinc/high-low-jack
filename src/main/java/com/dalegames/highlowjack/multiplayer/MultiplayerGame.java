package com.dalegames.highlowjack.multiplayer;

import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.GameSetup;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a multiplayer game session.
 * Tracks 4 player positions and their connection tokens.
 * 
 * @author Dale & Primus
 * @version 1.0
 */
public class MultiplayerGame {
    
    private final String joinCode;
    private final Game game;
    private final GameSetup setup;
    private final Map<Integer, PlayerConnection> players; // position -> player
    private final LocalDateTime createdAt;
    private int stateVersion; // Increments when game state changes
    private boolean gameStarted; // True when host starts the game
    
    public MultiplayerGame(String joinCode, Game game, GameSetup setup) {
        this.joinCode = joinCode;
        this.game = game;
        this.setup = setup;
        this.players = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.stateVersion = 0;
        this.gameStarted = false;
    }
    
    /**
     * Adds a player to a specific position.
     * Returns the player's unique token.
     */
    public String joinPlayer(int position, String playerName) {
        if (position < 0 || position > 3) {
            throw new IllegalArgumentException("Position must be 0-3");
        }
        
        if (players.containsKey(position)) {
            throw new IllegalStateException("Position " + position + " already taken");
        }
        
        String token = UUID.randomUUID().toString();
        players.put(position, new PlayerConnection(playerName, token));
        stateVersion++;
        
        System.out.println("👤 Player joined: " + playerName + " at position " + position);
        return token;
    }
    
    /**
     * Checks if a player token is valid for a given position.
     */
    public boolean isValidPlayer(int position, String token) {
        PlayerConnection conn = players.get(position);
        return conn != null && conn.token.equals(token);
    }
    
    /**
     * Checks if a position is taken.
     */
    public boolean isPositionTaken(int position) {
        return players.containsKey(position);
    }
    
    /**
     * Checks if all 4 positions are filled.
     */
    public boolean isFullyPopulated() {
        return players.size() == 4;
    }
    
    /**
     * Gets the number of connected players.
     */
    public int getPlayerCount() {
        return players.size();
    }
    
    /**
     * Updates state version (call when game state changes).
     */
    public void updateState() {
        stateVersion++;
    }
    
    /**
     * Gets current position of a player by their token.
     */
    public Integer getCurrentPlayerPosition(String token) {
        for (Map.Entry<Integer, PlayerConnection> entry : players.entrySet()) {
            if (entry.getValue().token.equals(token)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    // Getters
    
    public String getJoinCode() {
        return joinCode;
    }
    
    public Game getGame() {
        return game;
    }
    
    public GameSetup getSetup() {
        return setup;
    }
    
    public Map<Integer, PlayerConnection> getPlayers() {
        return new HashMap<>(players);
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public int getStateVersion() {
        return stateVersion;
    }
    
    /**
     * Marks the game as started.
     * Called when host clicks "Start Game".
     */
    public void startGame() {
        this.gameStarted = true;
        this.stateVersion++;
        System.out.println("🚀 Game " + joinCode + " has been started!");
    }

    /**
     * Checks if the game has been started by the host.
     */
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    /**
     * Represents a player connection.
     */
    public static class PlayerConnection {
        private final String playerName;
        private final String token;
        
        public PlayerConnection(String playerName, String token) {
            this.playerName = playerName;
            this.token = token;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public String getToken() {
            return token;
        }
    }
}
