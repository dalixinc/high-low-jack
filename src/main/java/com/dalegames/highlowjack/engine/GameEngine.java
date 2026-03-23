package com.dalegames.highlowjack.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.RoundResult;
import com.dalegames.highlowjack.model.Trick;

/**
 * Game engine for High Low Jack scoring and validation.
 * 
 * @author Dale &amp; Primus
 * @version 2.2 - Added card detail extraction for scoring display
 */
public class GameEngine {
    
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
        
        String highWinner = findHighTrump(tricks, trump);
        if (highWinner != null) {
            game.addScore(highWinner, 1);
            results.put("High", highWinner);
        }
        
        String lowWinner = findLowTrump(tricks, trump);
        if (lowWinner != null) {
            game.addScore(lowWinner, 1);
            results.put("Low", lowWinner);
        }
        
        String jackWinner = findJackWinner(tricks, trump);
        if (jackWinner != null) {
            game.addScore(jackWinner, 1);
            results.put("Jack", jackWinner);
        }
        
        String gameWinner = findGameWinner(tricks);
        if (gameWinner != null) {
            game.addScore(gameWinner, 1);
            results.put("Game", gameWinner);
        }
        
        return results;
    }

    public static RoundResult calculateRoundResults(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        if (game.getState() != Game.GameState.ROUND_COMPLETE) {
            throw new IllegalStateException("Round must be complete to calculate results");
        }
        
        List<Trick> tricks = game.getTricks();
        Card.Suit trump = game.getTrumpSuit();
        
        Map<String, List<Card>> capturedCards = calculateCapturedCards(tricks);
        Map<String, Integer> gamePointTotals = calculateGamePointTotals(capturedCards);
        Map<String, String> roundPointWinners = calculateScores(game);
        
        // Extract card details for display
        Card highCard = findHighTrumpCard(tricks, trump);
        Card lowCard = findLowTrumpCard(tricks, trump);
        Integer gameWinnerPoints = null;
        String gameWinner = roundPointWinners.get("Game");
        if (gameWinner != null) {
            gameWinnerPoints = gamePointTotals.get(gameWinner);
        }
        
        Map<String, Integer> scores = new HashMap<>();
        for (String player : game.getPlayerNames()) {
            scores.put(player, game.getScore(player));
        }
        
        return new RoundResult(capturedCards, gamePointTotals, roundPointWinners, scores, trump, 
                               highCard, lowCard, gameWinnerPoints);
    }

    public static Map<String, List<Card>> calculateCapturedCards(List<Trick> tricks) {
        Map<String, List<Card>> capturedCards = new HashMap<>();
        
        for (Trick trick : tricks) {
            String winner = trick.getWinner();
            capturedCards.putIfAbsent(winner, new ArrayList<>());
            
            for (Trick.CardPlay play : trick.getPlays()) {
                capturedCards.get(winner).add(play.card);
            }
        }
        
        return capturedCards;
    }

    public static Map<String, Integer> calculateGamePointTotals(Map<String, List<Card>> capturedCards) {
        Map<String, Integer> gamePoints = new HashMap<>();
        
        for (Map.Entry<String, List<Card>> entry : capturedCards.entrySet()) {
            String player = entry.getKey();
            int points = 0;
            
            for (Card card : entry.getValue()) {
                points += card.getRank().getPoints();
            }
            
            gamePoints.put(player, points);
        }
        
        return gamePoints;
    }

    public static Map<String, String> getCurrentPointStatus(Game game) {
        Map<String, String> status = new HashMap<>();
        
        if (game == null || game.getTrumpSuit() == null) {
            return status;
        }
        
        List<Trick> tricks = game.getTricks();
        Card.Suit trump = game.getTrumpSuit();
        
        List<Trick> allTricks = new ArrayList<>(tricks);
        if (game.getCurrentTrick() != null && game.getCurrentTrick().size() > 0) {
            allTricks.add(game.getCurrentTrick());
        }
        
        Card highestTrump = null;
        String highPlayer = null;
        
        for (Trick trick : allTricks) {
            for (Trick.CardPlay play : trick.getPlays()) {
                if (play.card.getSuit() == trump) {
                    if (highestTrump == null || play.card.getRank().getValue() > highestTrump.getRank().getValue()) {
                        highestTrump = play.card;
                        highPlayer = play.playerName;
                    }
                }
            }
        }
        
        if (highestTrump != null) {
            status.put("High", highestTrump.toString() + " - " + highPlayer);
        }
        
        Card lowestTrump = null;
        String lowPlayer = null;
        
        for (Trick trick : allTricks) {
            for (Trick.CardPlay play : trick.getPlays()) {
                if (play.card.getSuit() == trump) {
                    if (lowestTrump == null || play.card.getRank().getValue() < lowestTrump.getRank().getValue()) {
                        lowestTrump = play.card;
                        lowPlayer = play.playerName;
                    }
                }
            }
        }
        
        if (lowestTrump != null) {
            status.put("Low", lowestTrump.toString() + " - " + lowPlayer);
        }
        
        String jackWinner = null;
        for (Trick trick : allTricks) {
            for (Trick.CardPlay play : trick.getPlays()) {
                if (play.card.getSuit() == trump && play.card.getRank() == Card.Rank.JACK) {
                    jackWinner = trick.getWinner();
                    break;
                }
            }
            if (jackWinner != null) break;
        }
        
        if (jackWinner != null) {
            status.put("Jack", "J" + trump.getSymbol() + " - " + jackWinner);
        }
        
        return status;
    }
        
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
     * Finds and returns the actual highest trump card.
     */
    public static Card findHighTrumpCard(List<Trick> tricks, Card.Suit trump) {
        if (tricks == null || trump == null) {
            return null;
        }
        
        Card highestTrump = null;
        
        for (Trick trick : tricks) {
            for (Trick.CardPlay play : trick.getPlays()) {
                Card card = play.card;
                if (card.getSuit() == trump) {
                    if (highestTrump == null || card.getRank().getValue() > highestTrump.getRank().getValue()) {
                        highestTrump = card;
                    }
                }
            }
        }
        
        return highestTrump;
    }
    
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
     * Finds and returns the actual lowest trump card.
     */
    public static Card findLowTrumpCard(List<Trick> tricks, Card.Suit trump) {
        if (tricks == null || trump == null) {
            return null;
        }
        
        Card lowestTrump = null;
        
        for (Trick trick : tricks) {
            for (Trick.CardPlay play : trick.getPlays()) {
                Card card = play.card;
                if (card.getSuit() == trump) {
                    if (lowestTrump == null || card.getRank().getValue() < lowestTrump.getRank().getValue()) {
                        lowestTrump = card;
                    }
                }
            }
        }
        
        return lowestTrump;
    }
    
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
    
    public static String findGameWinner(List<Trick> tricks) {
        if (tricks == null || tricks.isEmpty()) {
            return null;
        }
        
        Map<String, Integer> gamePoints = new HashMap<>();
        
        for (Trick trick : tricks) {
            String winner = trick.getWinner();
            int points = 0;
            
            for (Trick.CardPlay play : trick.getPlays()) {
                points += play.card.getRank().getPoints();
            }
            
            gamePoints.put(winner, gamePoints.getOrDefault(winner, 0) + points);
        }
        
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
        
        return tie ? null : gameWinner;
    }
    
    public static int getGamePoints(Card card) {
        if (card == null) {
            return 0;
        }
        return card.getRank().getPoints();
    }
    
    public static boolean isValidPlay(Game game, String playerName, Card card) {
        if (game == null || playerName == null || card == null) {
            throw new IllegalArgumentException("Game, player, and card cannot be null");
        }
        
        if (!game.getHand(playerName).hasCard(card)) {
            return false;
        }
        
        Trick currentTrick = game.getCurrentTrick();
        if (currentTrick == null || currentTrick.size() == 0) {
            return true;
        }
        
        Card.Suit leadSuit = currentTrick.getLeadSuit();
        if (game.getHand(playerName).hasSuit(leadSuit)) {
            return card.getSuit() == leadSuit;
        }
        
        return true;
    }
}
