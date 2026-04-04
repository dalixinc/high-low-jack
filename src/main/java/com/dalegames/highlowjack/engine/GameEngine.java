package com.dalegames.highlowjack.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.RoundResult;
import com.dalegames.highlowjack.model.Team;
import com.dalegames.highlowjack.model.Trick;

/**
 * Game engine for High Low Jack scoring and validation.
 * 
 * @author Dale &amp; Primus
 * @version 2.5 - Track both players and teams for display purposes
 */
public class GameEngine {
    
    /**
     * Calculates scores and returns team/player names who won each point.
     * Stores the results in game.playerPointWinners for later access.
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
        
        // Get PLAYER names first, then score to teams
        String highPlayer = findHighTrump(tricks, trump, null);  // null = return player name
        if (highPlayer != null) {
            String scoreTo = game.isTeamMode() ? game.getTeamForPlayer(highPlayer).getName() : highPlayer;
            game.addScore(scoreTo, 1);
            results.put("High", scoreTo);  // Store team/player for scoring
        }
        
        String lowPlayer = findLowTrump(tricks, trump, null);  // null = return player name
        if (lowPlayer != null) {
            String scoreTo = game.isTeamMode() ? game.getTeamForPlayer(lowPlayer).getName() : lowPlayer;
            game.addScore(scoreTo, 1);
            results.put("Low", scoreTo);  // Store team/player for scoring
        }
        
        String jackPlayer = findJackWinner(tricks, trump, null);  // null = return player name
        if (jackPlayer != null) {
            String scoreTo = game.isTeamMode() ? game.getTeamForPlayer(jackPlayer).getName() : jackPlayer;
            game.addScore(scoreTo, 1);
            results.put("Jack", scoreTo);  // Store team/player for scoring
        }
        
        // TEAM MODE FIX: Pass game object to combine partners' game points
        String gameWinner = findGameWinner(tricks, game);
        if (gameWinner != null) {
            game.addScore(gameWinner, 1);
            results.put("Game", gameWinner);
        }
        
        return results;
    }
    
    /**
     * Gets the player point winners (before team conversion).
     * Used for display purposes in team mode.
     */
    public static Map<String, String> getPlayerPointWinners(Game game) {
        Map<String, String> players = new HashMap<>();
        List<Trick> tricks = game.getTricks();
        Card.Suit trump = game.getTrumpSuit();
        
        if (trump == null) {
            return players;
        }
        
        String highPlayer = findHighTrump(tricks, trump, null);
        if (highPlayer != null) {
            players.put("High", highPlayer);
        }
        
        String lowPlayer = findLowTrump(tricks, trump, null);
        if (lowPlayer != null) {
            players.put("Low", lowPlayer);
        }
        
        String jackPlayer = findJackWinner(tricks, trump, null);
        if (jackPlayer != null) {
            players.put("Jack", jackPlayer);
        }
        
        // For Game point in team mode, show team name; in individual mode, show player
        String gameWinner = findGameWinner(tricks, game);
        if (gameWinner != null) {
            players.put("Game", gameWinner);
        }
        
        return players;
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
        Map<String, String> roundPointPlayers = getPlayerPointWinners(game);  // NEW: Get player names
        
        // Extract card details for display
        Card highCard = findHighTrumpCard(tricks, trump);
        Card lowCard = findLowTrumpCard(tricks, trump);
        Integer gameWinnerPoints = null;
        String gameWinner = roundPointWinners.get("Game");
        if (gameWinner != null) {
            if (game.isTeamMode()) {
                // Sum team's combined game points
                Team winningTeam = null;
                for (Team team : game.getTeams()) {
                    if (team.getName().equals(gameWinner)) {
                        winningTeam = team;
                        break;
                    }
                }
                if (winningTeam != null) {
                    gameWinnerPoints = 0;
                    for (String player : winningTeam.getPlayerNames()) {
                        gameWinnerPoints += gamePointTotals.getOrDefault(player, 0);
                    }
                }
            } else {
                gameWinnerPoints = gamePointTotals.get(gameWinner);
            }
        }
        
        Map<String, Integer> scores = new HashMap<>();
        for (String player : game.getPlayerNames()) {
            scores.put(player, game.getScore(player));
        }
        
        return new RoundResult(capturedCards, gamePointTotals, roundPointWinners, scores, trump, 
                               highCard, lowCard, gameWinnerPoints, null, roundPointPlayers);
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
                    // Only get winner if trick is complete (4 cards)
                    if (trick.isComplete()) {
                        jackWinner = trick.getWinner();
                    } else {
                        // For incomplete trick, show who played the Jack
                        jackWinner = play.playerName;
                    }
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
    
    /**
     * Finds the player or team who won the High trump point.
     * 
     * @param tricks the completed tricks
     * @param trump the trump suit
     * @param game the game object (for team mode conversion)
     * @return player name in individual mode, team name in team mode
     */
    public static String findHighTrump(List<Trick> tricks, Card.Suit trump, Game game) {
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
        
        // Convert to team name in team mode
        if (winner != null && game != null && game.isTeamMode()) {
            Team team = game.getTeamForPlayer(winner);
            return team.getName();
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
    
    /**
     * Finds the player or team who won the Low trump point.
     * 
     * @param tricks the completed tricks
     * @param trump the trump suit
     * @param game the game object (for team mode conversion)
     * @return player name in individual mode, team name in team mode
     */
    public static String findLowTrump(List<Trick> tricks, Card.Suit trump, Game game) {
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
        
        // Convert to team name in team mode
        if (winner != null && game != null && game.isTeamMode()) {
            Team team = game.getTeamForPlayer(winner);
            return team.getName();
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
    
    /**
     * Finds the player or team who won the Jack point.
     * 
     * @param tricks the completed tricks
     * @param trump the trump suit
     * @param game the game object (for team mode conversion)
     * @return player name in individual mode, team name in team mode
     */
    public static String findJackWinner(List<Trick> tricks, Card.Suit trump, Game game) {
        if (tricks == null || trump == null) {
            return null;
        }
        
        for (Trick trick : tricks) {
            for (Trick.CardPlay play : trick.getPlays()) {
                if (play.card.getSuit() == trump && play.card.getRank() == Card.Rank.JACK) {
                    String winner = trick.getWinner();
                    
                    // Convert to team name in team mode
                    if (winner != null && game != null && game.isTeamMode()) {
                        Team team = game.getTeamForPlayer(winner);
                        return team.getName();
                    }
                    
                    return winner;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Finds the winner of the "Game" point.
     * 
     * <p>In INDIVIDUAL mode: Player with the most game points wins.
     * In TEAM mode: Team with the most combined game points wins.
     * If there's a tie, no one wins the Game point.</p>
     * 
     * @param tricks the completed tricks
     * @param game the game object (to check team mode and get teams)
     * @return the winner's name (player or team name), or null if tie
     */
    public static String findGameWinner(List<Trick> tricks, Game game) {
        if (tricks == null || tricks.isEmpty() || game == null) {
            return null;
        }
        
        // Calculate individual player game points
        Map<String, Integer> playerGamePoints = new HashMap<>();
        
        for (Trick trick : tricks) {
            String winner = trick.getWinner();
            int points = 0;
            
            for (Trick.CardPlay play : trick.getPlays()) {
                points += play.card.getRank().getPoints();
            }
            
            playerGamePoints.put(winner, playerGamePoints.getOrDefault(winner, 0) + points);
        }
        
        // TEAM MODE: Combine partners' game points
        if (game.isTeamMode()) {
            Map<String, Integer> teamGamePoints = new HashMap<>();
            
            // Sum each team's combined game points
            for (Map.Entry<String, Integer> entry : playerGamePoints.entrySet()) {
                String player = entry.getKey();
                Team team = game.getTeamForPlayer(player);
                String teamName = team.getName();
                
                teamGamePoints.put(teamName, 
                    teamGamePoints.getOrDefault(teamName, 0) + entry.getValue());
            }
            
            // Find winning team
            String gameWinner = null;
            int maxPoints = 0;
            boolean tie = false;
            
            for (Map.Entry<String, Integer> entry : teamGamePoints.entrySet()) {
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
        
        // INDIVIDUAL MODE: Original logic (highest individual game points)
        String gameWinner = null;
        int maxPoints = 0;
        boolean tie = false;
        
        for (Map.Entry<String, Integer> entry : playerGamePoints.entrySet()) {
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
