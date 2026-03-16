package com.dalegames.highlowjack.engine;

import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.Trick;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Game engine for High Low Jack scoring and validation.
 * 
 * <p>Provides static methods for calculating scores based on the four point categories:
 * <ul>
 *   <li><b>High</b>: Highest trump card in play (1 point)</li>
 *   <li><b>Low</b>: Lowest trump card in play (1 point)</li>
 *   <li><b>Jack</b>: Capturing the Jack of trumps, if in play (1 point)</li>
 *   <li><b>Game</b>: Most game points from captured cards (1 point)</li>
 * </ul>
 *
 * <p>Card point values for "Game" calculation:
 * <ul>
 *   <li>Ace = 4 points</li>
 *   <li>King = 3 points</li>
 *   <li>Queen = 2 points</li>
 *   <li>Jack = 1 point</li>
 *   <li>Ten = 10 points</li>
 *   <li>All other cards = 0 points</li>
 * </ul>
 *
 * @author Dale &amp; Primus
 * @version 1.0
 */
public class GameEngine {
    
    /**
     * Calculates and awards scores for a completed round.
     * Awards points for High, Low, Jack, and Game to the appropriate players.
     * 
     * @param game the completed game (must be in ROUND_COMPLETE state)
     * @return map of point categories to winning players
     * @throws IllegalStateException if round is not complete
     * @throws IllegalArgumentException if game is null
     */
    public static Map<String, String> calculateScores(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        if (game.getState() != Game.GameState.ROUND_COMPLETE) {
            throw new IllegalStateException("Round must be complete to calculate scores");
        }
        
        Map<String, String> results = new HashMap<>();
        List<Trick> tricks = game.getTricks();
        Card.Suit trump = game.getTrumpSuit();
        
        if (trump == null) {
            throw new IllegalStateException("Trump suit not set");
        }
        
        // High: Player who was dealt highest trump
        String highWinner = findHighTrump(tricks, trump);
        if (highWinner != null) {
            game.addScore(highWinner, 1);
            results.put("High", highWinner);
        }
        
        // Low: Player who was dealt lowest trump
        String lowWinner = findLowTrump(tricks, trump);
        if (lowWinner != null) {
            game.addScore(lowWinner, 1);
            results.put("Low", lowWinner);
        }
        
        // Jack: Player who captured Jack of trumps (if in play)
        String jackWinner = findJackWinner(tricks, trump);
        if (jackWinner != null) {
            game.addScore(jackWinner, 1);
            results.put("Jack", jackWinner);
        }
        
        // Game: Player with most game points from captured cards
        String gameWinner = findGameWinner(tricks);
        if (gameWinner != null) {
            game.addScore(gameWinner, 1);
            results.put("Game", gameWinner);
        }
        
        return results;
    }
    
    /**
     * Finds the player who was dealt the highest trump card.
     * Searches all tricks for trump cards and returns the player who played the highest.
     * 
     * @param tricks list of completed tricks
     * @param trump the trump suit
     * @return name of player with highest trump, or null if no trumps played
     */
    public static String findHighTrump(List<Trick> tricks, Card.Suit trump) {
        if (tricks == null || trump == null) {
            return null;
        }
        
        Card highestTrump = null;
        String winner = null;
        
        for (Trick trick : tricks) {
            for (Trick.CardPlay play : trick.getPlays()) {
                Card card = play.card;
                if (card.getSuit() == trump) {
                    if (highestTrump == null || card.getRank().getValue() > highestTrump.getRank().getValue()) {
                        highestTrump = card;
                        winner = play.playerName;
                    }
                }
            }
        }
        
        return winner;
    }
    
    /**
     * Finds the player who was dealt the lowest trump card.
     * Searches all tricks for trump cards and returns the player who played the lowest.
     * 
     * @param tricks list of completed tricks
     * @param trump the trump suit
     * @return name of player with lowest trump, or null if no trumps played
     */
    public static String findLowTrump(List<Trick> tricks, Card.Suit trump) {
        if (tricks == null || trump == null) {
            return null;
        }
        
        Card lowestTrump = null;
        String winner = null;
        
        for (Trick trick : tricks) {
            for (Trick.CardPlay play : trick.getPlays()) {
                Card card = play.card;
                if (card.getSuit() == trump) {
                    if (lowestTrump == null || card.getRank().getValue() < lowestTrump.getRank().getValue()) {
                        lowestTrump = card;
                        winner = play.playerName;
                    }
                }
            }
        }
        
        return winner;
    }
    
    /**
     * Finds the player who captured the Jack of trumps.
     * Returns the winner of the trick containing the Jack of the trump suit.
     * 
     * @param tricks list of completed tricks
     * @param trump the trump suit
     * @return name of player who won the trick with Jack of trumps, or null if not in play
     */
    public static String findJackWinner(List<Trick> tricks, Card.Suit trump) {
        if (tricks == null || trump == null) {
            return null;
        }
        
        for (Trick trick : tricks) {
            for (Trick.CardPlay play : trick.getPlays()) {
                if (play.card.getSuit() == trump && play.card.getRank() == Card.Rank.JACK) {
                    return trick.getWinner();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Finds the player who won the most game points from captured cards.
     * 
     * <p>Game points are awarded as follows:
     * <ul>
     *   <li>Ace = 4 points</li>
     *   <li>King = 3 points</li>
     *   <li>Queen = 2 points</li>
     *   <li>Jack = 1 point</li>
     *   <li>Ten = 10 points</li>
     * </ul>
     *
     * @param tricks list of completed tricks
     * @return name of player with most game points, or null on tie
     */
    public static String findGameWinner(List<Trick> tricks) {
        if (tricks == null || tricks.isEmpty()) {
            return null;
        }
        
        Map<String, Integer> gamePoints = new HashMap<>();
        
        // Calculate game points for each player
        for (Trick trick : tricks) {
            String winner = trick.getWinner();
            int points = 0;
            
            for (Trick.CardPlay play : trick.getPlays()) {
                points += getGamePoints(play.card);
            }
            
            gamePoints.put(winner, gamePoints.getOrDefault(winner, 0) + points);
        }
        
        // Find player with most points
        String gameWinner = null;
        int maxPoints = 0;
        boolean tie = false;
        
        for (Map.Entry<String, Integer> entry : gamePoints.entrySet()) {
            if (entry.getValue() > maxPoints) {
                maxPoints = entry.getValue();
                gameWinner = entry.getKey();
                tie = false;
            } else if (entry.getValue() == maxPoints && maxPoints > 0) {
                tie = true;
            }
        }
        
        // No winner on tie
        return tie ? null : gameWinner;
    }
    
    /**
     * Returns the game point value for a card.
     * 
     * @param card the card to evaluate
     * @return game points (0-10)
     */
    public static int getGamePoints(Card card) {
        if (card == null) {
            return 0;
        }
        
        switch (card.getRank()) {
            case ACE:
                return 4;
            case KING:
                return 3;
            case QUEEN:
                return 2;
            case JACK:
                return 1;
            case TEN:
                return 10;
            default:
                return 0;
        }
    }
    
    /**
     * Validates whether a card play is legal according to High Low Jack rules.
     * 
     * <p>Rules:
     * <ul>
     *   <li>Player must have the card in their hand</li>
     *   <li>If not leading, must follow suit if possible</li>
     *   <li>If cannot follow suit, any card may be played</li>
     * </ul>
     *
     * @param game the current game
     * @param playerName the player attempting to play
     * @param card the card being played
     * @return true if the play is legal
     * @throws IllegalArgumentException if game, playerName, or card is null
     */
    public static boolean isValidPlay(Game game, String playerName, Card card) {
        if (game == null || playerName == null || card == null) {
            throw new IllegalArgumentException("Game, player, and card cannot be null");
        }
        
        // Check player has the card
        if (!game.getHand(playerName).hasCard(card)) {
            return false;
        }
        
        // If leading, any card is valid
        Trick currentTrick = game.getCurrentTrick();
        if (currentTrick == null || currentTrick.size() == 0) {
            return true;
        }
        
        // Must follow suit if possible
        Card.Suit leadSuit = currentTrick.getLeadSuit();
        if (game.getHand(playerName).hasSuit(leadSuit)) {
            return card.getSuit() == leadSuit;
        }
        
        // If cannot follow suit, any card is valid
        return true;
    }
}
