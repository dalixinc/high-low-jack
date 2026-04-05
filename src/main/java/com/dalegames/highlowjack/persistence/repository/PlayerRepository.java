package com.dalegames.highlowjack.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dalegames.highlowjack.persistence.entity.Player;

/**
 * Repository for Player entity operations.
 * 
 * @author Dale & Primus
 * @version 1.2 - Added player/team separation queries
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    /**
     * Finds a player by name (case-insensitive).
     */
    Optional<Player> findByNameIgnoreCase(String name);
    
    /**
     * Checks if a player exists by name.
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Gets top players by win count.
     */
    List<Player> findTop10ByOrderByTotalMatchesWonDesc();
    
    /**
     * Gets players ordered by win percentage.
     */
    @Query("SELECT p FROM Player p WHERE p.totalMatchesPlayed >= 5 ORDER BY (CAST(p.totalMatchesWon AS double) / p.totalMatchesPlayed) DESC")
    List<Player> findByWinPercentage();
    
    /**
     * Gets players with active win streaks.
     */
    @Query("SELECT p FROM Player p WHERE p.currentWinStreak > 0 ORDER BY p.currentWinStreak DESC")
    List<Player> findPlayersOnWinStreaks();
    
    /**
     * Gets all players ordered by last played (most recent first).
     */
    List<Player> findAllByOrderByLastPlayedDesc();
    
    /**
     * Get only actual players (not teams) ordered by wins.
     */
    List<Player> findByIsTeamFalseOrderByTotalMatchesWonDesc();
    
    /**
     * Get all actual players (not teams).
     */
    List<Player> findByIsTeamFalse();
    
    /**
     * Get teams only.
     */
    List<Player> findByIsTeamTrueOrderByTotalMatchesWonDesc();
}
