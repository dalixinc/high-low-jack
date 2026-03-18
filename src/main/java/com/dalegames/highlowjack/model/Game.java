package com.dalegames.highlowjack.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.Serializable;

/**
 * Represents the complete state of a High Low Jack game.
 * 
 * <p>Manages the game flow including dealing cards, playing tricks, tracking scores,
 * and determining the winner. Supports exactly 4 players.</p>
 * 
 * <p>Game flow:
 * <ol>
 *   <li>Create game with 4 player names</li>
 *   <li>Deal 7 cards to each player</li>
 *   <li>First card played determines trump suit</li>
 *   <li>Play 7 tricks</li>
 *   <li>Score High, Low, Jack, Game points</li>
 *   <li>First to 7 total points wins</li>
 * </ol>
 *
 * @author Dale &amp; Primus
 * @version 1.0
 */
public class Game implements Serializable{
    private static final int NUM_PLAYERS = 4;
    private static final int CARDS_PER_PLAYER = 7;
    private static final int WINNING_SCORE = 7;
    
    private final List<String> playerNames;
    private final Map<String, Hand> hands;
    private final Map<String, Integer> scores;
    private final List<Trick> tricks;
    
    private Deck deck;
    private Card.Suit trumpSuit;
    private int currentPlayerIndex;
    private Trick currentTrick;
    private GameState state;
    private Trick completedTrick;  // Last completed trick (kept for display before clearing)

    /**
     * Constructs a new game with the specified player names.
     * 
     * @param playerNames exactly 4 player names
     * @throws IllegalArgumentException if not exactly 4 players or any name is null/empty
     */
    public Game(List<String> playerNames) {
        if (playerNames == null || playerNames.size() != NUM_PLAYERS) {
            throw new IllegalArgumentException("Must have exactly " + NUM_PLAYERS + " players");
        }
        
        for (String name : playerNames) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Player names cannot be null or empty");
            }
        }
        
        this.playerNames = new ArrayList<>(playerNames);
        this.hands = new HashMap<>();
        this.scores = new HashMap<>();
        this.tricks = new ArrayList<>();
        
        // Initialize hands and scores
        for (String name : playerNames) {
            hands.put(name, new Hand(name));
            scores.put(name, 0);
        }
        
        this.currentPlayerIndex = 0;
        this.state = GameState.NOT_STARTED;
    }
    
    /**
     * Convenience constructor for 4 individual player names.
     * 
     * @param player1 first player's name
     * @param player2 second player's name
     * @param player3 third player's name
     * @param player4 fourth player's name
     * @throws IllegalArgumentException if any name is null/empty
     */
    public Game(String player1, String player2, String player3, String player4) {
        this(Arrays.asList(player1, player2, player3, player4));
    }
    
    /**
     * Deals cards to all players and starts the game.
     * Creates a new shuffled deck and deals 7 cards to each player.
     * 
     * @throws IllegalStateException if game is already in progress
     */
    public void dealCards() {
        if (state != GameState.NOT_STARTED && state != GameState.ROUND_COMPLETE) {
            throw new IllegalStateException("Cannot deal cards during active game");
        }
        
        deck = new Deck();
        deck.shuffle();
        
        // Deal 7 cards to each player
        for (String playerName : playerNames) {
            Hand hand = hands.get(playerName);
            hand.addCards(deck.dealHand(CARDS_PER_PLAYER));
        }

        trumpSuit = null;
        currentTrick = null;
        completedTrick = null;  // ← ADD THIS LINE
        tricks.clear();
        state = GameState.IN_PROGRESS;
    }
    
    /**
     * Plays a card for the current player.
     * 
     * <p>The first card played in the first trick determines the trump suit.
     * Players must follow suit if possible. When a trick is complete (4 cards),
     * the winner is determined and becomes the next player to lead.</p>
     * 
     * @param card the card to play
     * @return the current trick after playing the card
     * @throws IllegalStateException if game is not in progress or no cards dealt
     * @throws IllegalArgumentException if card is not in player's hand or play is invalid
     */
    public Trick playCard(Card card) {
        if (state != GameState.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }
        
        String currentPlayer = getCurrentPlayer();
        Hand hand = hands.get(currentPlayer);
        
        if (!hand.hasCard(card)) {
            throw new IllegalArgumentException(currentPlayer + " does not have card: " + card);
        }
        
        // Create new trick if needed
        if (currentTrick == null) {
            // First card of first trick determines trump
            if (trumpSuit == null) {
                trumpSuit = card.getSuit();
            }
            currentTrick = new Trick(trumpSuit);
        }
        
        // Validate play is legal (must follow suit if possible)
        if (currentTrick.size() > 0) {
            Card.Suit leadSuit = currentTrick.getLeadSuit();
            if (hand.hasSuit(leadSuit) && card.getSuit() != leadSuit) {
                throw new IllegalArgumentException(
                    "Must follow suit " + leadSuit + " if possible"
                );
            }
        }
        
        // Play the card
        hand.playCard(card);
        currentTrick.playCard(currentPlayer, card);

        // Check if trick is complete
        if (currentTrick.isComplete()) {
            tricks.add(currentTrick);
            String winner = currentTrick.getWinner();

            // Save completed trick for display
            completedTrick = currentTrick;

            // Winner leads next trick
            currentPlayerIndex = playerNames.indexOf(winner);
            currentTrick = null;

            // Check if round is complete (all 7 tricks played)
            if (tricks.size() == CARDS_PER_PLAYER) {
                state = GameState.ROUND_COMPLETE;
            }
        } else {

            // Move to next player
            currentPlayerIndex = (currentPlayerIndex + 1) % NUM_PLAYERS;
        }
        
        return currentTrick;
    }
    
    /**
     * Returns the name of the current player whose turn it is.
     * 
     * @return the current player's name
     */
    public String getCurrentPlayer() {
        return playerNames.get(currentPlayerIndex);
    }
    
    /**
     * Returns the hand for the specified player.
     * 
     * @param playerName the player's name
     * @return the player's hand
     * @throws IllegalArgumentException if player name is not in game
     */
    public Hand getHand(String playerName) {
        Hand hand = hands.get(playerName);
        if (hand == null) {
            throw new IllegalArgumentException("Player not in game: " + playerName);
        }
        return hand;
    }
    
    /**
     * Returns all player names in turn order.
     * 
     * @return a copy of the player names list
     */
    public List<String> getPlayerNames() {
        return new ArrayList<>(playerNames);
    }
    
    /**
     * Returns the current trump suit, or null if not yet determined.
     * Trump is set by the first card played in the game.
     * 
     * @return the trump suit, or null if game hasn't started
     */
    public Card.Suit getTrumpSuit() {
        return trumpSuit;
    }
    
    /**
     * Returns all completed tricks in the order they were played.
     * 
     * @return a copy of the tricks list
     */
    public List<Trick> getTricks() {
        return new ArrayList<>(tricks);
    }
    
    /**
     * Returns the current trick being played, or null if between tricks.
     * 
     * @return the current trick, or null
     */
    public Trick getCurrentTrick() {
        return currentTrick;
    }

    /**
     * Returns the last completed trick that hasn't been cleared yet.
     * This allows the UI to display the completed trick before starting the next one.
     *
     * @return the completed trick, or null if no trick has been completed recently
     */
    public Trick getCompletedTrick() { return completedTrick; }

    /**
     * Clears the completed trick from memory.
     * Should be called after the UI has displayed the completed trick.
     */
    public void clearCompletedTrick() { this.completedTrick = null; }

    /**
     * Returns the current game state.
     * 
     * @return the current state
     */
    public GameState getState() {
        return state;
    }
    
    /**
     * Returns a copy of the current scores map.
     * 
     * @return map of player names to their scores
     */
    public Map<String, Integer> getScores() {
        return new HashMap<>(scores);
    }
    
    /**
     * Returns the score for a specific player.
     * 
     * @param playerName the player's name
     * @return the player's current score
     * @throws IllegalArgumentException if player name is not in game
     */
    public int getScore(String playerName) {
        Integer score = scores.get(playerName);
        if (score == null) {
            throw new IllegalArgumentException("Player not in game: " + playerName);
        }
        return score;
    }
    
    /**
     * Adds points to a player's score.
     * 
     * @param playerName the player's name
     * @param points the points to add
     * @throws IllegalArgumentException if player name is not in game or points is negative
     */
    public void addScore(String playerName, int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }
        if (!scores.containsKey(playerName)) {
            throw new IllegalArgumentException("Player not in game: " + playerName);
        }
        scores.put(playerName, scores.get(playerName) + points);
        
        // Check for winner
        if (scores.get(playerName) >= WINNING_SCORE) {
            state = GameState.GAME_OVER;
        }
    }
    
    /**
     * Returns whether the game is over (a player has reached winning score).
     * 
     * @return true if game is over
     */
    public boolean isGameOver() {
        return state == GameState.GAME_OVER;
    }
    
    /**
     * Returns the winner of the game, or null if game is not over.
     * 
     * @return the winning player's name, or null
     */
    public String getWinner() {
        if (state != GameState.GAME_OVER) {
            return null;
        }
        
        String winner = null;
        int maxScore = 0;
        
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() >= WINNING_SCORE && entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                winner = entry.getKey();
            }
        }
        
        return winner;
    }
    
    /**
     * Returns a string representation of the current game state.
     * 
     * @return formatted game state including players, scores, and current status
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("High Low Jack Game\n");
        sb.append("State: ").append(state).append("\n");
        sb.append("Trump: ").append(trumpSuit != null ? trumpSuit.getSymbol() : "Not set").append("\n");
        sb.append("\nPlayers and Scores:\n");
        
        for (String player : playerNames) {
            sb.append("  ").append(player).append(": ").append(scores.get(player));
            if (player.equals(getCurrentPlayer()) && state == GameState.IN_PROGRESS) {
                sb.append(" (current)");
            }
            sb.append("\n");
        }
        
        if (currentTrick != null) {
            sb.append("\nCurrent Trick:\n").append(currentTrick);
        }
        
        sb.append("\nTricks completed: ").append(tricks.size()).append("/").append(CARDS_PER_PLAYER);
        
        return sb.toString();
    }
    
    /**
     * Enumeration of possible game states.
     */
    public enum GameState {
        /** Game created but not yet started */
        NOT_STARTED,
        /** Cards dealt and game in progress */
        IN_PROGRESS,
        /** All 7 tricks complete, ready for scoring */
        ROUND_COMPLETE,
        /** A player has reached winning score */
        GAME_OVER
    }
}
