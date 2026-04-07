package com.dalegames.highlowjack.persistence.service;

import com.dalegames.highlowjack.model.QuipTrigger;
import com.dalegames.highlowjack.persistence.entity.PersonalityQuip;
import com.dalegames.highlowjack.persistence.repository.PersonalityQuipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * Service for managing and triggering personality quips.
 * 
 * @author Dale & Primus
 * @version 1.0
 */
@Service
@Transactional
public class PersonalityService {
    
    private final PersonalityQuipRepository quipRepository;
    private final Random random = new Random();
    
    @Autowired
    public PersonalityService(PersonalityQuipRepository quipRepository) {
        this.quipRepository = quipRepository;
    }
    
    /**
     * Get a quip for a specific trigger and player.
     * Returns null if no quip found.
     * 
     * @param trigger the trigger context
     * @param playerName the player name (can be null for generic)
     * @return a random applicable quip, or null
     */
    public String getQuip(QuipTrigger trigger, String playerName) {
        return getQuip(trigger.name(), playerName);
    }
    
    /**
     * Get a quip for a specific trigger context and player.
     * 
     * @param triggerContext the trigger context string
     * @param playerName the player name (can be null for generic)
     * @return a random applicable quip, or null
     */
    public String getQuip(String triggerContext, String playerName) {
        List<PersonalityQuip> quips = quipRepository.findApplicableQuips(
            triggerContext, playerName);
        
        if (quips.isEmpty()) {
            return null;
        }
        
        // Pick a random quip
        PersonalityQuip selected = quips.get(random.nextInt(quips.size()));
        
        // Record usage
        selected.recordUsage();
        quipRepository.save(selected);
        
        return selected.getQuipText();
    }
    
    /**
     * Add a new quip to the database.
     */
    public PersonalityQuip addQuip(String playerName, String triggerContext, 
                                   String quipText, String category) {
        PersonalityQuip quip = new PersonalityQuip(
            playerName, triggerContext, quipText, category);
        return quipRepository.save(quip);
    }
    
    /**
     * Get all quips for a player.
     */
    public List<PersonalityQuip> getPlayerQuips(String playerName) {
        return quipRepository.findByPlayerNameAndIsActiveTrue(playerName);
    }
    
    /**
     * Deactivate a quip.
     */
    public void deactivateQuip(Long quipId) {
        quipRepository.findById(quipId).ifPresent(quip -> {
            quip.setActive(false);
            quipRepository.save(quip);
        });
    }
}
