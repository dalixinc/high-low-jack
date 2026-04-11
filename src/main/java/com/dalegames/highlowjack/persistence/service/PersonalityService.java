package com.dalegames.highlowjack.persistence.service;

import com.dalegames.highlowjack.model.QuipTrigger;
import com.dalegames.highlowjack.persistence.entity.PersonalityQuip;
import com.dalegames.highlowjack.persistence.repository.PersonalityQuipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
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

        // Weighted selection: prefer less-used and less-recently-used quips
        double[] weights = new double[quips.size()];
        double totalWeight = 0;
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < quips.size(); i++) {
            PersonalityQuip q = quips.get(i);
            // Base weight inversely proportional to usage count
            double weight = 1.0 / (q.getTimesUsed() + 1);
            // Penalise recency: used in last 30 min = 10%, last 2 hours = 50%
            if (q.getLastUsed() != null) {
                long minutes = Duration.between(q.getLastUsed(), now).toMinutes();
                if (minutes < 30) {
                    weight *= 0.1;
                } else if (minutes < 120) {
                    weight *= 0.5;
                }
            }
            weights[i] = weight;
            totalWeight += weight;
        }

        // Weighted random pick
        double rand = random.nextDouble() * totalWeight;
        double cumulative = 0;
        PersonalityQuip selected = quips.get(quips.size() - 1);
        for (int i = 0; i < quips.size(); i++) {
            cumulative += weights[i];
            if (rand <= cumulative) {
                selected = quips.get(i);
                break;
            }
        }

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
