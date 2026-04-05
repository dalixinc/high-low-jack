package com.dalegames.highlowjack.persistence.repository;

import com.dalegames.highlowjack.persistence.entity.PersonalityQuip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PersonalityQuip operations.
 * 
 * @author Dale & Primus
 * @version 1.0
 */
@Repository
public interface PersonalityQuipRepository extends JpaRepository<PersonalityQuip, Long> {
    
    /**
     * Find quips for a specific player and trigger.
     */
    List<PersonalityQuip> findByPlayerNameAndTriggerContextAndIsActiveTrue(
        String playerName, String triggerContext);
    
    /**
     * Find generic quips for a trigger (no specific player).
     */
    List<PersonalityQuip> findByPlayerNameIsNullAndTriggerContextAndIsActiveTrue(
        String triggerContext);
    
    /**
     * Find all quips for a trigger (both player-specific and generic).
     */
    @Query("SELECT q FROM PersonalityQuip q WHERE " +
           "q.isActive = true AND q.triggerContext = :trigger AND " +
           "(q.playerName = :playerName OR q.playerName IS NULL)")
    List<PersonalityQuip> findApplicableQuips(
        @Param("trigger") String trigger,
        @Param("playerName") String playerName);
    
    /**
     * Get all quips for a specific player.
     */
    List<PersonalityQuip> findByPlayerNameAndIsActiveTrue(String playerName);
    
    /**
     * Get all active quips by category.
     */
    List<PersonalityQuip> findByCategoryAndIsActiveTrue(String category);
}
