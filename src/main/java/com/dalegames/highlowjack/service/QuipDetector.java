package com.dalegames.highlowjack.service;

import com.dalegames.highlowjack.model.*;
import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.persistence.service.PersonalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    private final Random random = new Random();

    @Autowired
    public QuipDetector(PersonalityService personalityService) {
        this.personalityService = personalityService;
    }
    
    /**
     * Check for quips triggered by the cut ceremony.
     * Fires for both cutters based on their card rank.
     */
    public List<String> checkCutQuips(String cutter1, Card card1, String cutter2, Card card2) {
        List<String> quips = new ArrayList<>();
        addCutCardQuip(quips, cutter1, card1);
        addCutCardQuip(quips, cutter2, card2);
        return quips;
    }

    private void addCutCardQuip(List<String> quips, String playerName, Card card) {
        if (playerName == null || card == null) return;
        int value = card.getRank().getValue();
        QuipTrigger trigger;
        if (card.getRank() == Card.Rank.ACE) {
            trigger = QuipTrigger.CUT_ACE;
        } else if (card.getRank() == Card.Rank.TWO) {
            trigger = QuipTrigger.CUT_TWO;
        } else if (value >= 10) {
            trigger = QuipTrigger.CUT_HIGH_CARD;
        } else if (value <= 5) {
            trigger = QuipTrigger.CUT_LOW_CARD;
        } else {
            return; // 6-9: no quip
        }
        addQuip(quips, trigger, playerName, "");
    }

    /** Always fire a quip (if one exists in DB). */
    private void addQuip(List<String> quips, QuipTrigger trigger, String playerName, String prefix) {
        String quip = personalityService.getQuip(trigger, playerName);
        if (quip != null) quips.add(prefix + quip);
    }

    /** Fire a quip with a given probability (weighted selection still applies within DB). */
    private void addQuipChance(List<String> quips, QuipTrigger trigger, String playerName, String prefix, double chance) {
        if (random.nextDouble() < chance) {
            addQuip(quips, trigger, playerName, prefix);
        }
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

        String highWinner = roundResult.getRoundPointWinner("High");
        String lowWinner  = roundResult.getRoundPointWinner("Low");
        String jackWinner = roundResult.getRoundPointWinner("Jack");
        String gameWinner = roundResult.getRoundPointWinner("Game");

        // Sweep: all 4 points to same player/team
        if (highWinner != null &&
            highWinner.equals(lowWinner) &&
            highWinner.equals(jackWinner) &&
            highWinner.equals(gameWinner)) {
            addQuip(quips, QuipTrigger.SWEEP_ALL_FOUR, highWinner, "🎯 ");
        }

        // Individual point winners — any player can have DB entries for these
        String highPlayer = roundResult.getRoundPointPlayer("High");
        String lowPlayer  = roundResult.getRoundPointPlayer("Low");
        String jackPlayer = roundResult.getRoundPointPlayer("Jack");
        String gamePlayer = roundResult.getRoundPointPlayer("Game");

        if (highPlayer != null)  addQuipChance(quips, QuipTrigger.WON_WITH_HIGH, highPlayer, "", 0.4);
        if (lowPlayer != null)   addQuipChance(quips, QuipTrigger.WON_WITH_LOW,  lowPlayer,  "", 0.4);
        if (jackPlayer != null)  addQuipChance(quips, QuipTrigger.WON_WITH_JACK, jackPlayer, "", 0.4);
        if (gamePlayer != null)  addQuipChance(quips, QuipTrigger.WON_WITH_GAME, gamePlayer, "", 0.4);

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
            addQuipChance(quips, QuipTrigger.FIRST_SET_WIN, winner, "", 0.5);
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
