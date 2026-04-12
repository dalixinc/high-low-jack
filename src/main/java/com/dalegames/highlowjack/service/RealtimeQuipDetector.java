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
            String playerName = event.getPlayerName();
            String quip = null;

            switch (event.getType()) {
                case TWO_PITCHED:
                    QuipTrigger twoTrigger = (event.getCurrentScore() < 5)
                        ? QuipTrigger.CUT_TWO_LOSING
                        : QuipTrigger.CUT_TWO_WINNING;
                    quip = personalityService.getQuip(twoTrigger, playerName);
                    break;

                case ACE_SPADES_PLAYED:
                    quip = personalityService.getQuip(QuipTrigger.PLAY_ACE_SPADES, playerName);
                    if (quip != null) quip = "♠️ " + quip;
                    break;

                case NINE_DIAMONDS_PLAYED:
                    quip = personalityService.getQuip(QuipTrigger.CURSE_OF_SCOTLAND, playerName);
                    if (quip != null) quip = "♦️ " + quip;
                    break;

                default:
                    break;
            }

            if (quip != null) quips.add(quip);
        }

        return quips;
    }
}
