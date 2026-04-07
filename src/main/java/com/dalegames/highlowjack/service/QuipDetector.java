package com.dalegames.highlowjack.service;

import com.dalegames.highlowjack.model.*;
import com.dalegames.highlowjack.persistence.service.PersonalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Detects quip opportunities during gameplay.
 * ONLY USES METHODS THAT ACTUALLY EXIST IN YOUR CLASSES!
 * 
 * @author Dale & Primus
 * @version 2.0 - Rebuilt from scratch using actual methods
 */
@Service
public class QuipDetector {
    
    private final PersonalityService personalityService;
    
    @Autowired
    public QuipDetector(PersonalityService personalityService) {
        this.personalityService = personalityService;
    }
    
    /**
     * Check for real-time event quips.
     */
    public List<String> checkGameEvents(Game game) {
        List<String> quips = new ArrayList<>();
        
        for (GameEvent event : game.getRecentEvents()) {
            String quip = null;
            
            switch (event.getType()) {
                case TWO_PITCHED:
                    if ("Preezbob".equals(event.getPlayerName())) {
                        if (event.getCurrentScore() < 5) {
                            quip = personalityService.getQuip(
                                QuipTrigger.CUT_TWO_LOSING, "Preezbob");
                        } else {
                            quip = personalityService.getQuip(
                                QuipTrigger.CUT_TWO_WINNING, "Preezbob");
                        }
                    }
                    break;
                    
                case ACE_SPADES_PLAYED:
                    if ("Preezbob".equals(event.getPlayerName())) {
                        quip = personalityService.getQuip(
                            QuipTrigger.PLAY_ACE_SPADES, "Preezbob");
                    }
                    break;
            }
            
            if (quip != null) {
                quips.add(quip);
            }
        }
        
        return quips;
    }
    
    /**
     * Check for quips after a round completes.
     * 
     * AVAILABLE METHODS IN RoundResult:
     * - getRoundPointWinner(String category)
     * - getRoundPointPlayer(String category)
     * - getTrumpSuit()
     * - getHighCard()
     * - getLowCard()
     * - getScore(String playerName)
     * 
     * @param game the current game
     * @param roundResult the round results
     * @return list of quips to display
     */
    public List<String> checkRoundQuips(Game game, RoundResult roundResult) {
        List<String> quips = new ArrayList<>();
        
        // Check for sweep (all 4 points won by same player/team)
        String highWinner = roundResult.getRoundPointWinner("High");
        String lowWinner = roundResult.getRoundPointWinner("Low");
        String jackWinner = roundResult.getRoundPointWinner("Jack");
        String gameWinner = roundResult.getRoundPointWinner("Game");
        
        if (highWinner != null && 
            highWinner.equals(lowWinner) &&
            highWinner.equals(jackWinner) &&
            highWinner.equals(gameWinner)) {
            
            String quip = personalityService.getQuip(
                QuipTrigger.SWEEP_ALL_FOUR, highWinner);
            if (quip != null) quips.add("🎯 " + quip);
        }
        
        // Check for individual players winning points (for player-specific quips)
        String highPlayer = roundResult.getRoundPointPlayer("High");
        String lowPlayer = roundResult.getRoundPointPlayer("Low");
        String jackPlayer = roundResult.getRoundPointPlayer("Jack");
        String gamePlayer = roundResult.getRoundPointPlayer("Game");
        
        // Dale's strategic wins
        if ("Dale".equals(highPlayer) || "Dale".equals(gamePlayer)) {
            String quip = personalityService.getQuip(
                QuipTrigger.WON_WITH_HIGH, "Dale");
            if (quip != null && Math.random() < 0.3) { // 30% chance
                quips.add(quip);
            }
        }
        
        // Kreep's shadow strikes
        if ("Kreep".equals(jackPlayer)) {
            String quip = personalityService.getQuip(
                QuipTrigger.WON_WITH_JACK, "Kreep");
            if (quip != null && Math.random() < 0.3) {
                quips.add(quip);
            }
        }
        
        return quips;
    }
    
    /**
     * Check for quips after a set completes.
     * 
     * AVAILABLE METHODS IN SetResult:
     * - getWinner()
     * - getFinalScores() - Map<String, Integer>
     * - wasTiebreaker()
     * - getWinningPoint()
     * 
     * @param game the current game
     * @param setResult the set results
     * @return list of quips to display
     */
    public List<String> checkSetQuips(Game game, SetResult setResult) {
        List<String> quips = new ArrayList<>();
        
        String winner = setResult.getWinner();
        Map<String, Integer> scores = setResult.getFinalScores();
        int winnerScore = scores.get(winner);
        
        // Get loser score
        int loserScore = 0;
        for (var entry : scores.entrySet()) {
            if (!entry.getKey().equals(winner)) {
                loserScore = Math.max(loserScore, entry.getValue());
            }
        }
        
        // Tiebreaker win
        if (setResult.wasTiebreaker()) {
            String quip = personalityService.getQuip(
                QuipTrigger.TIEBREAKER_WIN, winner);
            if (quip != null) quips.add("⚡ " + quip);
        }
        
        // Dominating win (11-0 to 11-3)
        else if (winnerScore == 11 && loserScore <= 3) {
            String quip = personalityService.getQuip(
                QuipTrigger.DOMINATING_WIN, winner);
            if (quip != null) quips.add("💪 " + quip);
        }
        
        // Close win (11-9 or 11-10)
        else if (winnerScore == 11 && loserScore >= 9) {
            String quip = personalityService.getQuip(
                QuipTrigger.CLOSE_WIN, winner);
            if (quip != null) quips.add("🔥 " + quip);
        }
        
        // Generic set win
        else {
            String quip = personalityService.getQuip(
                QuipTrigger.FIRST_SET_WIN, winner);
            if (quip != null && Math.random() < 0.3) {
                quips.add(quip);
            }
        }
        
        return quips;
    }
    
    /**
     * Check for quips after a match completes.
     * 
     * AVAILABLE METHODS IN MatchResult:
     * - getWinner()
     * - getFinalSetWins() - Map<String, Integer>
     * - getMatchType()
     * 
     * @param game the current game
     * @param matchResult the match results
     * @return list of quips to display
     */
    public List<String> checkMatchQuips(Game game, MatchResult matchResult) {
        List<String> quips = new ArrayList<>();
        
        String winner = matchResult.getWinner();
        
        // Perfect match (didn't lose a set)
        int loserSets = 0;
        for (var entry : matchResult.getFinalSetWins().entrySet()) {
            if (!entry.getKey().equals(winner)) {
                loserSets = Math.max(loserSets, entry.getValue());
            }
        }
        
        if (loserSets == 0) {
            String quip = personalityService.getQuip(
                QuipTrigger.PERFECT_MATCH, winner);
            if (quip != null) quips.add("👑 " + quip);
        }
        
        // Generic match winner
        String quip = personalityService.getQuip(
            QuipTrigger.MATCH_WINNER, winner);
        if (quip != null) quips.add("🏆 " + quip);
        
        return quips;
    }
}
