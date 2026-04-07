package com.dalegames.highlowjack.service;

import com.dalegames.highlowjack.model.*;
import com.dalegames.highlowjack.persistence.service.PersonalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects realtime quip opportunities from game events.
 * 
 * @author Dale & Primus
 * @version 1.0
 */
@Service
public class RealtimeQuipDetector {
    
    private final PersonalityService personalityService;
    
    @Autowired
    public RealtimeQuipDetector(PersonalityService personalityService) {
        this.personalityService = personalityService;
    }
    
    /**
     * Check recent game events for realtime quip triggers.
     * 
     * @param game the game with recent events
     * @return list of quips triggered by recent events
     */
    public List<String> checkRealtimeEvents(Game game) {
        List<String> quips = new ArrayList<>();
        List<GameEvent> events = game.getRecentEvents();
        
        for (GameEvent event : events) {
            switch (event.getType()) {
                case TWO_PITCHED:
                    // Preezbob cutting a 2!
                    if ("Preezbob".equals(event.getPlayerName())) {
                        String quip = getPreezbobTwoQuip(event.getCurrentScore());
                        if (quip != null) quips.add(quip);
                    }
                    break;
                    
                case ACE_SPADES_PLAYED:
                    // Preezbob plays Ace of Spades!
                    if ("Preezbob".equals(event.getPlayerName())) {
                        String quip = personalityService.getQuip(
                            QuipTrigger.PLAY_ACE_SPADES, "Preezbob");
                        if (quip != null) quips.add("♠️ " + quip);
                    }
                    break;
                    
                default:
                    // Other events don't trigger quips yet
                    break;
            }
        }
        
        return quips;
    }
    
    /**
     * Gets appropriate quip for Preezbob cutting a 2 based on his score.
     */
    private String getPreezbobTwoQuip(int score) {
        QuipTrigger trigger = (score < 5) ? 
            QuipTrigger.CUT_TWO_LOSING : 
            QuipTrigger.CUT_TWO_WINNING;
        
        return personalityService.getQuip(trigger, "Preezbob");
    }
}
