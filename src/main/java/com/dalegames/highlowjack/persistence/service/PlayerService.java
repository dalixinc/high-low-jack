package com.dalegames.highlowjack.persistence.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalegames.highlowjack.persistence.entity.Player;
import com.dalegames.highlowjack.persistence.repository.PlayerRepository;

/**
 * Service layer for Player operations.
 * Handles business logic and transactions.
 * 
 * @author Dale & Primus
 * @version 1.2 - Added player/team separation and stats methods
 */
@Service
@Transactional
public class PlayerService {
    
    private final PlayerRepository playerRepository;
    
    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
    
    /**
     * Gets or creates a player by name.
     * 
     * @param name the player name
     * @return the existing or newly created player
     */
    public Player getOrCreatePlayer(String name) {
        return playerRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> createPlayer(name));
    }
    
    /**
     * Creates a new player.
     */
    public Player createPlayer(String name) {
        if (playerRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Player already exists: " + name);
        }
        
        Player player = new Player(name);
        return playerRepository.save(player);
    }
    
    /**
     * Gets a player by name.
     */
    public Optional<Player> getPlayer(String name) {
        return playerRepository.findByNameIgnoreCase(name);
    }
    
    /**
     * Gets all players.
     */
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
    
    /**
     * Gets leaderboard (top players by wins).
     */
    public List<Player> getLeaderboard() {
        return playerRepository.findTop10ByOrderByTotalMatchesWonDesc();
    }
    
    /**
     * Gets player-only leaderboard (excludes teams).
     */
    public List<Player> getPlayerLeaderboard() {
        return playerRepository.findByIsTeamFalseOrderByTotalMatchesWonDesc();
    }
    
    /**
     * Gets players by win percentage (minimum 5 games).
     */
    public List<Player> getPlayersByWinPercentage() {
        return playerRepository.findByWinPercentage();
    }
    
    /**
     * Gets players with active win streaks.
     */
    public List<Player> getPlayersOnWinStreaks() {
        return playerRepository.findPlayersOnWinStreaks();
    }
    
    /**
     * Gets total number of unique matches played.
     * Uses the maximum matches played by any single player.
     */
    public int getTotalMatches() {
        List<Player> allPlayers = playerRepository.findByIsTeamFalse();
        
        if (allPlayers.isEmpty()) return 0;
        
        // Get the player with most matches (they've played in all matches)
        return allPlayers.stream()
            .mapToInt(Player::getTotalMatchesPlayed)
            .max()
            .orElse(0);
    }
    
    /**
     * Updates a player's stats after a match.
     * 
     * @param playerName the player name
     * @param won true if player won
     * @param setsWon number of sets won by this player
     */
    public void updateMatchStats(String playerName, boolean won, int setsWon) {
        Player player = getOrCreatePlayer(playerName);
        
        if (won) {
            player.recordMatchWin(setsWon);
        } else {
            player.recordMatchLoss(setsWon);
        }
        
        playerRepository.save(player);
    }
    
    /**
     * Records a point won by a player.
     * 
     * @param playerName the player name
     * @param category the point category (High/Low/Jack/Game)
     */
    public void recordPoint(String playerName, String category) {
        Player player = getOrCreatePlayer(playerName);
        player.recordPoint(category);
        playerRepository.save(player);
    }
    
    /**
     * Records a two being cut (Preezbob's specialty!).
     */
    public void recordTwoCut(String playerName) {
        Player player = getOrCreatePlayer(playerName);
        player.recordTwoCut();
        playerRepository.save(player);
    }
    
    /**
     * Records Ace of Spades being played.
     */
    public void recordAceSpadesPlayed(String playerName) {
        Player player = getOrCreatePlayer(playerName);
        player.recordAceSpadesPlayed();
        playerRepository.save(player);
    }
    
    /**
     * Resets all player statistics to zero.
     */
    public void resetAllStats() {
        List<Player> allPlayers = playerRepository.findByIsTeamFalse();
        
        for (Player player : allPlayers) {
            player.setTotalMatchesPlayed(0);
            player.setTotalMatchesWon(0);
            player.setTotalSetsPlayed(0);
            player.setTotalSetsWon(0);
            player.setTotalRoundsPlayed(0);
            player.setHighsWon(0);
            player.setLowsWon(0);
            player.setJacksWon(0);
            player.setGamesWon(0);
            player.setTotalPoints(0);
            player.setCurrentWinStreak(0);
            player.setLongestWinStreak(0);
            player.setTotalTwosCut(0);
            player.setTotalAceSpadesPlayed(0);
            
            playerRepository.save(player);
        }
    }
    
    /**
     * Deletes a player (admin operation).
     */
    public void deletePlayer(String name) {
        playerRepository.findByNameIgnoreCase(name)
                .ifPresent(playerRepository::delete);
    }
}
