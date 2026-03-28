package com.dalegames.highlowjack.integration;

import com.dalegames.highlowjack.engine.GameEngine;
import com.dalegames.highlowjack.model.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for a complete High Low Jack game.
 * Tests full game flow from dealing to scoring.
 * 
 * @author Dale & Primus
 */
class FullGameTest {
    
    @Test
    void testCompleteGameRound() {
        // Create game with 4 players
        Game game = new Game("Dale", "Kreep", "Carryn", "Player4");
        
        // Verify initial state
        assertEquals(Game.GameState.NOT_STARTED, game.getState());
        assertEquals(0, game.getScore("Dale"));
        
        // Deal cards
        game.dealCards();
        assertEquals(Game.GameState.IN_PROGRESS, game.getState());
        
        // Verify each player has 7 cards
        assertEquals(7, game.getHand("Dale").size());
        assertEquals(7, game.getHand("Kreep").size());
        assertEquals(7, game.getHand("Carryn").size());
        assertEquals(7, game.getHand("Player4").size());
        
        // Play through 7 tricks (simulated)
        playEntireRound(game);
        
        // Round should be complete
        assertEquals(Game.GameState.ROUND_COMPLETE, game.getState());
        assertEquals(7, game.getTricks().size());
        
        // Calculate scores
        Map<String, String> results = GameEngine.calculateScores(game);
        
        // Verify results exist (actual winners depend on cards dealt)
        assertNotNull(results);
        assertTrue(results.containsKey("High") || results.containsKey("Low") || 
                   results.containsKey("Jack") || results.containsKey("Game"));
        
        // Verify at least one player scored
        boolean someoneScored = false;
        for (String player : game.getPlayerNames()) {
            if (game.getScore(player) > 0) {
                someoneScored = true;
                break;
            }
        }
        assertTrue(someoneScored);
    }
    
    @Test
    void testTrumpDetermination() {
        Game game = new Game("Dale", "Kreep", "Carryn", "Player4");
        game.dealCards();
        
        // Trump not set until first card played
        assertNull(game.getTrumpSuit());
        
        // Play first card
        String firstPlayer = game.getCurrentPlayer();
        com.dalegames.highlowjack.model.Hand hand = game.getHand(firstPlayer);
        Card firstCard = hand.getCards().get(0);
        
        game.playCard(firstCard);
        
        // Trump should now be set to first card's suit
        assertEquals(firstCard.getSuit(), game.getTrumpSuit());
    }
    
    @Test
    void testFollowSuitRule() {
        Game game = new Game("Dale", "Kreep", "Carryn", "Player4");
        game.dealCards();
        
        // Play first card to establish lead suit
        String player1 = game.getCurrentPlayer();
        com.dalegames.highlowjack.model.Hand hand1 = game.getHand(player1);
        Card leadCard = hand1.getCards().get(0);
        Card.Suit leadSuit = leadCard.getSuit();
        
        game.playCard(leadCard);
        
        // Next player must follow suit if possible
        String player2 = game.getCurrentPlayer();
        com.dalegames.highlowjack.model.Hand hand2 = game.getHand(player2);
        
        if (hand2.hasSuit(leadSuit)) {
            // Try to play non-lead suit
            Card wrongSuit = null;
            for (Card card : hand2.getCards()) {
                if (card.getSuit() != leadSuit) {
                    wrongSuit = card;
                    break;
                }
            }
            
            if (wrongSuit != null) {
                Card finalWrongSuit = wrongSuit;
                assertThrows(IllegalArgumentException.class, () -> {
                    game.playCard(finalWrongSuit);
                });
            }
        }
    }
    
    @Test
    void testGamePointCalculation() {
        // Test individual card values
        assertEquals(4, GameEngine.getGamePoints(new Card(Card.Suit.HEARTS, Card.Rank.ACE)));
        assertEquals(3, GameEngine.getGamePoints(new Card(Card.Suit.HEARTS, Card.Rank.KING)));
        assertEquals(2, GameEngine.getGamePoints(new Card(Card.Suit.HEARTS, Card.Rank.QUEEN)));
        assertEquals(1, GameEngine.getGamePoints(new Card(Card.Suit.HEARTS, Card.Rank.JACK)));
        assertEquals(10, GameEngine.getGamePoints(new Card(Card.Suit.HEARTS, Card.Rank.TEN)));
        assertEquals(0, GameEngine.getGamePoints(new Card(Card.Suit.HEARTS, Card.Rank.NINE)));
    }
    
    @Test
    void testWinningCondition() {
        Game game = new Game("Dale", "Kreep", "Carryn", "Player4");
        
        // Add points manually
        game.addScore("Dale", 3);
        assertFalse(game.isMatchComplete());
        assertNull(game.getMatchWinner());
        
        // Game now requires 11 points to win a set, and recordSetWin must be called
        game.addScore("Dale", 8);  // Total = 11
        assertFalse(game.isMatchComplete());  // No match winner yet - must call recordSetWin
        
        // Record the set win
        game.recordSetWin("Dale");
        assertTrue(game.isMatchComplete());  // Single set match, so match is complete
        assertEquals("Dale", game.getMatchWinner());
    }
    
    /**
     * Helper method to play through an entire round.
     * Plays valid cards for each player until round is complete.
     */
    private void playEntireRound(Game game) {
        int maxTricks = 7;
        int tricksPlayed = 0;
        
        while (game.getState() == Game.GameState.IN_PROGRESS && tricksPlayed < maxTricks) {
            int cardsInTrick = 0;
            
            while (cardsInTrick < 4) {
                String player = game.getCurrentPlayer();
                com.dalegames.highlowjack.model.Hand hand = game.getHand(player);
                
                if (hand.isEmpty()) {
                    break;
                }
                
                // Find a valid card to play
                Card cardToPlay = findValidCard(game, player, hand);
                
                if (cardToPlay == null) {
                    // No valid card (shouldn't happen)
                    cardToPlay = hand.getCards().get(0);
                }
                
                game.playCard(cardToPlay);
                cardsInTrick++;
            }
            
            tricksPlayed++;
        }
    }
    
    /**
     * Finds a valid card for the player to play based on current trick.
     */
    private Card findValidCard(Game game, String player, com.dalegames.highlowjack.model.Hand hand) {
        com.dalegames.highlowjack.model.Trick currentTrick = game.getCurrentTrick();
        
        // If leading, play any card
        if (currentTrick == null || currentTrick.size() == 0) {
            return hand.getCards().get(0);
        }
        
        // Try to follow suit
        Card.Suit leadSuit = currentTrick.getLeadSuit();
        for (Card card : hand.getCards()) {
            if (card.getSuit() == leadSuit) {
                return card;
            }
        }
        
        // Can't follow suit, play any card
        return hand.getCards().get(0);
    }
}
