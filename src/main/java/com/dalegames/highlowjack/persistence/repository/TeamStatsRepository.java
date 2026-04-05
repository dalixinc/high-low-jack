package com.dalegames.highlowjack.persistence.repository;

import com.dalegames.highlowjack.persistence.entity.TeamStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TeamStats entity operations.
 * 
 * @author Dale & Primus
 * @version 1.0
 */
@Repository
public interface TeamStatsRepository extends JpaRepository<TeamStats, Long> {
    
    /**
     * Finds a team by name (case-insensitive).
     */
    Optional<TeamStats> findByNameIgnoreCase(String name);
    
    /**
     * Checks if a team exists by name.
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Gets top teams by win count.
     */
    List<TeamStats> findTop10ByOrderByMatchesWonDesc();
    
    /**
     * Gets all teams ordered by last played (most recent first).
     */
    List<TeamStats> findAllByOrderByLastPlayedDesc();
}
