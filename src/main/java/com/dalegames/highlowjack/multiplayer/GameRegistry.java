package com.dalegames.highlowjack.multiplayer;

import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.GameSetup;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Registry of active multiplayer games.
 * Manages join codes and game cleanup.
 * 
 * @author Dale & Primus
 * @version 1.1 - Fixed createGame to build MultiplayerGame
 */
@Component
public class GameRegistry {
    
    private final Map<String, MultiplayerGame> activeGames = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    // Characters for join codes (no ambiguous chars: O/0, I/1, etc.)
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    
    /**
     * Creates a new multiplayer game and returns it.
     */
    public MultiplayerGame createGame(Game game, GameSetup setup) {
        return createGame(game, setup, 4);
    }

    public MultiplayerGame createGame(Game game, GameSetup setup, int maxObservers) {
        String code = generateUniqueCode();
        MultiplayerGame mpGame = new MultiplayerGame(code, game, setup, maxObservers);
        activeGames.put(code, mpGame);
        System.out.println("🎮 Created multiplayer game: " + code);
        return mpGame;
    }
    
    /**
     * Gets a game by its join code.
     */
    public MultiplayerGame getGame(String code) {
        return activeGames.get(code.toUpperCase());
    }
    
    /**
     * Removes a game from the registry.
     */
    public void removeGame(String code) {
        activeGames.remove(code.toUpperCase());
        System.out.println("🗑️ Removed multiplayer game: " + code);
    }
    
    /**
     * Generates a unique 6-character join code.
     */
    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (activeGames.containsKey(code));
        
        return code;
    }
    
    /**
     * Cleans up stale games (older than 4 hours).
     */
    public void cleanupStaleGames() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(4);
        
        activeGames.entrySet().removeIf(entry -> {
            if (entry.getValue().getCreatedAt().isBefore(cutoff)) {
                System.out.println("🧹 Cleaning up stale game: " + entry.getKey());
                return true;
            }
            return false;
        });
    }
}
