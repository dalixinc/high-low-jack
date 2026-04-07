package com.dalegames.highlowjack.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the complete state of a High Low Jack game.
 * 
 * <p>Manages the game flow including dealing cards, playing tricks, tracking scores,
 * and determining the winner. Supports exactly 4 players in either individual or team mode.</p>
 * 
 * <p>Game flow:
 * <ol>
 *   <li>Create game with GameSetup configuration</li>
 *   <li>Deal 7 cards to each player</li>
 *   <li>First card played determines trump suit</li>
 *   <li>Play 7 tricks</li>
 *   <li>Score High, Low, Jack, Game points</li>
 *   <li>First to 11 total points wins the SET (individual or team)</li>
 *   <li>First to win required sets wins the MATCH</li>
 * </ol>
 *
 * @author Dale &amp; Primus
 * @version 2.3 - Added mutator method to allow for remote state change
 */
public class Game implements Serializable{
    private static final long serialVersionUID = 2L;  // Incremented for team mode

    private static final int NUM_PLAYERS = 4;
    private static final int CARDS_PER_PLAYER = 7;
    private static final int WINNING_SCORE = 11;
    
    private final List<String> playerNames;
    private final Map<String, Hand> hands;
    private final Map<String, Integer> scores;  // Player scores (individual) OR Team scores (team mode)
    private final List<Trick> tricks;
    private final GameSetup gameSetup;
    private final Map<String, Integer> setsWon;  // Player sets (individual) OR Team sets (team mode)
    
    // NEW: Team mode support
    private final List<Team> teams;  // null for individual mode, 2 teams for team mode
    
    private Deck deck;
    private Card.Suit trumpSuit;
    private int currentPlayerIndex;
    private Trick currentTrick;
    private GameState state;
    private Trick completedTrick;
    private int currentSetNumber;
    private int pitcherIndex;  // BUG #3 FIX: Tracks who pitched this round
    private  List<GameEvent> recentEvents;  // Events from current round
    private boolean roundScoresApplied;     // Guard against double-scoring in multiplayer
    private Map<String, String> lastRoundScores;  // Stored after first scoring call
    private int roundNumber;               // Round within the current set (1-based); shared across sessions
    private MatchResult matchResult;       // Set when match is complete; shared across sessions
    private SetResult lastSetResult;       // Set when a set is won; shared across sessions
    private Match currentMatch;            // Authoritative match state from controller; shared across sessions
    private List<String> pendingMatchQuips;        // Quips for match-winner screen; shared across sessions
    private List<String> pendingRealtimeQuips;     // Realtime event quips; shown to all humans then cleared
    private long realtimeQuipTimestamp;            // Epoch ms when quips were set; cleared after 6s

    // Cut ceremony state (shared across sessions; cleared once dealing begins)
    private String cutPlayer1;
    private String cutPlayer2;
    private Card cutCard1;
    private Card cutCard2;
    private String cutWinner;
    private boolean cutTied;
    private int lastCutter1Index = -1;  // For suggesting next-set cutter rotation
    private int lastCutter2Index = -1;

    /**
     * Constructs a new game with the specified game setup.
     * 
     * @param gameSetup the game configuration including players, match type, and game mode
     * @throws IllegalArgumentException if gameSetup is null
     */
    public Game(GameSetup gameSetup) {
        if (gameSetup == null) {
            throw new IllegalArgumentException("GameSetup cannot be null");
        }
        
        this.gameSetup = gameSetup;
        this.playerNames = Arrays.asList(gameSetup.getPlayerNames());
        this.hands = new HashMap<>();
        this.scores = new HashMap<>();
        this.tricks = new ArrayList<>();
        this.setsWon = new HashMap<>();
        this.currentSetNumber = 1;
        this.pitcherIndex = 0;  // Player 0 pitches first round
        
        // Initialize teams if team mode
        if (gameSetup.isTeamMode()) {
            this.teams = new ArrayList<>(gameSetup.getTeams());
            
            // Initialize scores and sets won by TEAM
            for (Team team : teams) {
                scores.put(team.getName(), 0);
                setsWon.put(team.getName(), 0);
            }
        } else {
            this.teams = null;
            
            // Initialize scores and sets won by PLAYER
            for (String name : playerNames) {
                scores.put(name, 0);
                setsWon.put(name, 0);
            }
        }
        
        // Initialize hands for all players (always individual)
        for (String name : playerNames) {
            hands.put(name, new Hand(name));
        }
        
        this.recentEvents = new ArrayList<>();
        this.roundScoresApplied = false;
        this.lastRoundScores = new HashMap<>();
        this.roundNumber = 0;
        this.matchResult = null;
        this.lastSetResult = null;
        this.currentMatch = null;
        this.pendingMatchQuips = null;
        this.pendingRealtimeQuips = null;
        this.realtimeQuipTimestamp = 0;
        this.cutPlayer1 = null;
        this.cutPlayer2 = null;
        this.cutCard1 = null;
        this.cutCard2 = null;
        this.cutWinner = null;
        this.cutTied = false;
        this.lastCutter1Index = -1;
        this.lastCutter2Index = -1;
        this.currentPlayerIndex = 0;
        this.state = GameState.NOT_STARTED;
    }

    /**
     * Legacy constructor for backwards compatibility.
     * Creates a simple single-set game with all human players in individual mode.
     * 
     * @param playerNames exactly 4 player names
     * @throws IllegalArgumentException if not exactly 4 players or any name is null/empty
     * @deprecated Use Game(GameSetup) instead
     */
    @Deprecated
    public Game(List<String> playerNames) {
        if (playerNames == null || playerNames.size() != NUM_PLAYERS) {
            throw new IllegalArgumentException("Must have exactly " + NUM_PLAYERS + " players");
        }
        
        for (String name : playerNames) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Player names cannot be null or empty");
            }
        }
        
        // Create default GameSetup with all human players in individual mode
        List<PlayerInfo> players = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            players.add(new PlayerInfo(
                playerNames.get(i), 
                PlayerInfo.PlayerType.HUMAN,
                i == 0  // First player is controller
            ));
        }
        
        this.gameSetup = GameSetup.createIndividual(players, GameSetup.MatchType.SINGLE_SET);
        this.playerNames = new ArrayList<>(playerNames);
        this.hands = new HashMap<>();
        this.scores = new HashMap<>();
        this.tricks = new ArrayList<>();
        this.setsWon = new HashMap<>();
        this.currentSetNumber = 1;
        this.pitcherIndex = 0;
        this.teams = null;  // Individual mode
        
        // Initialize hands, scores, and sets won by PLAYER
        for (String name : playerNames) {
            hands.put(name, new Hand(name));
            scores.put(name, 0);
            setsWon.put(name, 0);
        }

        this.recentEvents = new ArrayList<>();
        this.roundScoresApplied = false;
        this.lastRoundScores = new HashMap<>();
        this.roundNumber = 0;
        this.matchResult = null;
        this.lastSetResult = null;
        this.currentMatch = null;
        this.pendingMatchQuips = null;
        this.pendingRealtimeQuips = null;
        this.realtimeQuipTimestamp = 0;
        this.cutPlayer1 = null;
        this.cutPlayer2 = null;
        this.cutCard1 = null;
        this.cutCard2 = null;
        this.cutWinner = null;
        this.cutTied = false;
        this.lastCutter1Index = -1;
        this.lastCutter2Index = -1;
        this.currentPlayerIndex = 0;
        this.state = GameState.NOT_STARTED;
    }

    /**
     * Legacy convenience constructor for 4 individual player names.
     * 
     * @param player1 first player's name
     * @param player2 second player's name
     * @param player3 third player's name
     * @param player4 fourth player's name
     * @throws IllegalArgumentException if any name is null/empty
     * @deprecated Use Game(GameSetup) instead
     */
    @Deprecated
    public Game(String player1, String player2, String player3, String player4) {
        this(Arrays.asList(player1, player2, player3, player4));
    }
    
    /**
     * Deals cards to all players and starts a new round.
     * Creates a new shuffled deck and deals 7 cards to each player.
     * Clears tricks but preserves scores and sets won.
     * 
     * @throws IllegalStateException if game is already in progress
     */
    public void dealCards() {
        if (state != GameState.NOT_STARTED && state != GameState.ROUND_COMPLETE) {
            throw new IllegalStateException("Cannot deal cards during active game");
        }

        // BUG #3 FIX: Rotate pitcher clockwise each round (except first)
        if (state == GameState.ROUND_COMPLETE) {
            pitcherIndex = (pitcherIndex + 1) % NUM_PLAYERS;
        }

        deck = new Deck();
        deck.shuffle();

        // Deal 7 cards to each player
        for (String playerName : playerNames) {
            Hand hand = hands.get(playerName);
            hand.addCards(deck.dealHand(CARDS_PER_PLAYER));
        }

        // BUG #3 FIX: Start with pitcher as current player
        currentPlayerIndex = pitcherIndex;
        trumpSuit = null;
        currentTrick = null;
        completedTrick = null;
        tricks.clear();
        roundScoresApplied = false;
        lastRoundScores = new HashMap<>();
        roundNumber++;
        state = GameState.IN_PROGRESS;
    }
    
    /**
     * Starts a new set after a set has been won.
     * Resets scores to 0, increments set number, and deals new cards.
     * 
     * @throws IllegalStateException if called when set is not complete
     */
    public void startNewSet() {
        if (state != GameState.SET_COMPLETE) {
            throw new IllegalStateException("Cannot start new set - current set not complete");
        }
        
        // Reset scores for new set
        if (gameSetup.isTeamMode()) {
            for (Team team : teams) {
                team.resetScore();
                scores.put(team.getName(), 0);
            }
        } else {
            for (String player : playerNames) {
                scores.put(player, 0);
            }
        }
        
        currentSetNumber++;
        roundNumber = 0;  // dealCards() will increment to 1
        state = GameState.NOT_STARTED;
        dealCards();
    }
    
    /**
     * Records a set win for a player or team.
     * 
     * @param winner the name of the set winner (player name or team name)
     * @throws IllegalArgumentException if winner is not in the game
     */
    public void recordSetWin(String winner) {
        if (!setsWon.containsKey(winner)) {
            throw new IllegalArgumentException("Winner not in game: " + winner);
        }
        
        int newTotal = setsWon.get(winner) + 1;
        setsWon.put(winner, newTotal);
        
        // Update team object if team mode
        if (gameSetup.isTeamMode()) {
            for (Team team : teams) {
                if (team.getName().equals(winner)) {
                    team.incrementSetsWon();
                    break;
                }
            }
        }
        
        // Check if match is complete
        if (newTotal >= gameSetup.getSetsToWin()) {
            state = GameState.MATCH_COMPLETE;
        } else {
            state = GameState.SET_COMPLETE;
        }
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
                
                // DETECT: Trump pitch (first card of round)
                int pitcherScore = getScore(currentPlayer);
                recentEvents.add(new GameEvent(
                    GameEvent.EventType.TRUMP_SET, 
                    currentPlayer, 
                    card, 
                    pitcherScore
                ));
                
                // DETECT: Two pitched
                if (card.getRank() == Card.Rank.TWO) {
                    recentEvents.add(new GameEvent(
                        GameEvent.EventType.TWO_PITCHED, 
                        currentPlayer, 
                        card, 
                        pitcherScore
                    ));
                }
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
        
        // DETECT: Ace of Spades played
        if (card.getRank() == Card.Rank.ACE && card.getSuit() == Card.Suit.SPADES) {
            int playerScore = getScore(currentPlayer);
            recentEvents.add(new GameEvent(
                GameEvent.EventType.ACE_SPADES_PLAYED, 
                currentPlayer, 
                card, 
                playerScore
            ));
        }
        
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
    
    // Getters and utility methods
    
    public String getCurrentPlayer() {
        return playerNames.get(currentPlayerIndex);
    }
    
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    public Hand getHand(String playerName) {
        return hands.get(playerName);
    }

    /**
     * Renames a player at the given position. Updates playerNames, hands, and
     * (in individual mode) scores and setsWon. Also updates GameSetup so that
     * isHumanPlayer() and AI detection remain consistent.
     */
    public void renamePlayer(int position, String newName) {
        String oldName = playerNames.get(position);
        if (oldName.equals(newName)) return;

        playerNames.set(position, newName);

        Hand hand = hands.remove(oldName);
        if (hand != null) hands.put(newName, hand);

        if (!isTeamMode()) {
            Integer score = scores.remove(oldName);
            if (score != null) scores.put(newName, score);
            Integer sw = setsWon.remove(oldName);
            if (sw != null) setsWon.put(newName, sw);
        }

        gameSetup.getPlayers().get(position).setName(newName);
    }
    
    public Card.Suit getTrumpSuit() {
        return trumpSuit;
    }
    
    public Trick getCurrentTrick() {
        return currentTrick;
    }
    
    public List<Trick> getTricks() {
        return new ArrayList<>(tricks);
    }
    
    public GameState getState() {
        return state;
    }
    
    // Dale added to allow other classes to set game state
    public void setState(GameState gs) {
        this.state = gs;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(MatchResult result) {
        this.matchResult = result;
    }

    public SetResult getLastSetResult() {
        return lastSetResult;
    }

    public void setLastSetResult(SetResult result) {
        this.lastSetResult = result;
    }

    public Match getCurrentMatch() {
        return currentMatch;
    }

    public void setCurrentMatch(Match match) {
        this.currentMatch = match;
    }

    public List<String> getPendingMatchQuips() {
        return pendingMatchQuips;
    }

    public void setPendingMatchQuips(List<String> quips) {
        this.pendingMatchQuips = quips;
    }

    /**
     * Returns pending realtime quips if they were set within the last 6 seconds,
     * otherwise clears and returns null. This lets all human players see the quips
     * during the polling window without double-applying them.
     */
    public List<String> getAndMaybeExpireRealtimeQuips() {
        if (pendingRealtimeQuips == null || pendingRealtimeQuips.isEmpty()) return null;
        if (System.currentTimeMillis() - realtimeQuipTimestamp > 6000) {
            pendingRealtimeQuips = null;
            realtimeQuipTimestamp = 0;
            return null;
        }
        return pendingRealtimeQuips;
    }

    public void setPendingRealtimeQuips(List<String> quips) {
        this.pendingRealtimeQuips = quips;
        this.realtimeQuipTimestamp = System.currentTimeMillis();
    }

    public void clearRealtimeQuips() {
        this.pendingRealtimeQuips = null;
        this.realtimeQuipTimestamp = 0;
    }

    public boolean isRoundScoresApplied() {
        return roundScoresApplied;
    }

    public void setRoundScoresApplied(boolean applied) {
        this.roundScoresApplied = applied;
    }

    public Map<String, String> getLastRoundScores() {
        return lastRoundScores;
    }

    public void setLastRoundScores(Map<String, String> scores) {
        this.lastRoundScores = scores;
    }
    
    public List<String> getPlayerNames() {
        return new ArrayList<>(playerNames);
    }
    
    /**
     * Gets the name of the player who pitched (led) this round.
     * FEATURE #1: For display purposes.
     *
     * @return pitcher's name
     */
    public String getPitcherName() {
        return playerNames.get(pitcherIndex);
    }
    
    /** Sets the pitcher (first to play) by player index. Used by cut ceremony. */
    public void setPitcherIndex(int index) {
        this.pitcherIndex = index;
    }

    /**
     * Prepares for a new set (resets scores, increments set number) WITHOUT dealing cards.
     * Call dealCards() separately (e.g. after cut ceremony completes).
     */
    public void prepareForNewSet() {
        if (state != GameState.SET_COMPLETE) {
            throw new IllegalStateException("Cannot prepare new set - current set not complete");
        }
        if (gameSetup.isTeamMode()) {
            for (Team team : teams) {
                team.resetScore();
                scores.put(team.getName(), 0);
            }
        } else {
            for (String player : playerNames) {
                scores.put(player, 0);
            }
        }
        currentSetNumber++;
        roundNumber = 0;
        state = GameState.NOT_STARTED;
        // Clear cut state for new ceremony
        cutPlayer1 = null;
        cutPlayer2 = null;
        cutCard1 = null;
        cutCard2 = null;
        cutWinner = null;
        cutTied = false;
    }

    // ── Cut ceremony state ──────────────────────────────────────────────────

    public String getCutPlayer1() { return cutPlayer1; }
    public void setCutPlayer1(String cutPlayer1) { this.cutPlayer1 = cutPlayer1; }

    public String getCutPlayer2() { return cutPlayer2; }
    public void setCutPlayer2(String cutPlayer2) { this.cutPlayer2 = cutPlayer2; }

    public Card getCutCard1() { return cutCard1; }
    public void setCutCard1(Card cutCard1) { this.cutCard1 = cutCard1; }

    public Card getCutCard2() { return cutCard2; }
    public void setCutCard2(Card cutCard2) { this.cutCard2 = cutCard2; }

    public String getCutWinner() { return cutWinner; }
    public void setCutWinner(String cutWinner) { this.cutWinner = cutWinner; }

    public boolean isCutTied() { return cutTied; }
    public void setCutTied(boolean cutTied) { this.cutTied = cutTied; }

    public int getLastCutter1Index() { return lastCutter1Index; }
    public void setLastCutter1Index(int idx) { this.lastCutter1Index = idx; }

    public int getLastCutter2Index() { return lastCutter2Index; }
    public void setLastCutter2Index(int idx) { this.lastCutter2Index = idx; }

    /** Clears cut ceremony card/result state (keeps last cutter indices for rotation). */
    public void clearCutState() {
        cutPlayer1 = null;
        cutPlayer2 = null;
        cutCard1 = null;
        cutCard2 = null;
        cutWinner = null;
        cutTied = false;
    }

    /**
     * Gets the most recently completed trick.
     * This allows the UI to display the completed trick before starting the next one.
     *
     * @return the completed trick, or null if no trick has been completed recently
     */
    public Trick getCompletedTrick() { 
        return completedTrick; 
    }

    /**
     * Clears the completed trick from memory.
     * Should be called after the UI has displayed the completed trick.
     */
    public void clearCompletedTrick() { 
        this.completedTrick = null; 
    }
    
    /**
     * Accessor/Mutator for Recent Events
     */
    public List<GameEvent> getRecentEvents() {
        return new ArrayList<>(recentEvents);
    }
    
    public void clearRecentEvents() {
        recentEvents.clear();
    }
    
    /**
     * Gets current scores.
     * In individual mode: returns player scores.
     * In team mode: returns team scores.
     * 
     * @return map of player/team names to scores
     */
    public Map<String, Integer> getScores() {
        return new HashMap<>(scores);
    }
    
    /**
     * Gets a specific score.
     * In individual mode: player score.
     * In team mode: team score.
     * 
     * @param name player or team name
     * @return the score
     */
    public int getScore(String name) {
        return scores.getOrDefault(name, 0);
    }
    
    /**
     * Adds points to a player's score (individual mode) or their team's score (team mode).
     * Does NOT check for set winner - use SetResult.determineWinner() for that.
     * 
     * <p>In team mode, accepts either a player name OR a team name.
     * If a team name is passed, the points are added directly to that team.</p>
     * 
     * @param name the player's name or team name (in team mode)
     * @param points the points to add
     * @throws IllegalArgumentException if name is not valid or points is negative
     */
    public void addScore(String name, int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        if (gameSetup.isTeamMode()) {
            // Check if this is a team name
            boolean isTeamName = teams.stream()
                .anyMatch(team -> team.getName().equals(name));
            
            if (isTeamName) {
                // Direct team name - add to team
                Team team = teams.stream()
                    .filter(t -> t.getName().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Team not in game: " + name));
                
                team.addScore(points);
                scores.put(team.getName(), team.getScore());
            } else if (playerNames.contains(name)) {
                // Player name - find their team and add
                Team team = getTeamForPlayer(name);
                team.addScore(points);
                scores.put(team.getName(), team.getScore());
            } else {
                throw new IllegalArgumentException("Player or team not in game: " + name);
            }
        } else {
            // Individual mode - must be a player name
            if (!playerNames.contains(name)) {
                throw new IllegalArgumentException("Player not in game: " + name);
            }
            if (!scores.containsKey(name)) {
                throw new IllegalArgumentException("Player not in game: " + name);
            }
            scores.put(name, scores.get(name) + points);
        }
    }
    
    /**
     * Gets the team for a given player (team mode only).
     * 
     * @param playerName the player's name
     * @return the team containing this player
     * @throws IllegalStateException if not in team mode
     */
    public Team getTeamForPlayer(String playerName) {
        if (!gameSetup.isTeamMode()) {
            throw new IllegalStateException("Not in team mode");
        }
        return gameSetup.getTeamForPlayer(playerName);
    }
    
    /**
     * Gets all teams (team mode only).
     * 
     * @return list of teams
     * @throws IllegalStateException if not in team mode
     */
    public List<Team> getTeams() {
        if (!gameSetup.isTeamMode()) {
            throw new IllegalStateException("Not in team mode");
        }
        return new ArrayList<>(teams);
    }
    
    /**
     * Checks if this game is in team mode.
     * 
     * @return true if team mode, false if individual mode
     */
    public boolean isTeamMode() {
        return gameSetup.isTeamMode();
    }
    
    /**
     * Gets the game setup configuration.
     * 
     * @return the GameSetup
     */
    public GameSetup getGameSetup() {
        return gameSetup;
    }
    
    /**
     * Gets all sets won by all players/teams.
     * 
     * @return map of player/team names to sets won
     */
    public Map<String, Integer> getSetsWon() {
        return new HashMap<>(setsWon);
    }
    
    /**
     * Gets sets won by a specific player/team.
     * 
     * @param name the player or team name
     * @return number of sets won
     * @throws IllegalArgumentException if name is not in game
     */
    public int getSetsWonByPlayer(String name) {
        Integer sets = setsWon.get(name);
        if (sets == null) {
            throw new IllegalArgumentException("Player/team not in game: " + name);
        }
        return sets;
    }
    
    /**
     * Gets the current set number (1, 2, 3, etc.).
     * 
     * @return the current set number
     */
    public int getCurrentSetNumber() {
        return currentSetNumber;
    }
    
    /**
     * Checks if the match is complete (a player/team has won required sets).
     * 
     * @return true if match is over
     */
    public boolean isMatchComplete() {
        return state == GameState.MATCH_COMPLETE;
    }
    
    /**
     * Returns the match winner, or null if match is not complete.
     * 
     * @return the winning player/team name, or null
     */
    public String getMatchWinner() {
        if (state != GameState.MATCH_COMPLETE) {
            return null;
        }
        
        int setsNeeded = gameSetup.getSetsToWin();
        
        for (Map.Entry<String, Integer> entry : setsWon.entrySet()) {
            if (entry.getValue() >= setsNeeded) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    /**
     * Legacy method - returns whether the game is over.
     * Now checks for set completion instead of game over.
     * 
     * @return true if set or match is complete
     * @deprecated Use isMatchComplete() or check state directly
     */
    @Deprecated
    public boolean isGameOver() {
        return state == GameState.SET_COMPLETE || state == GameState.MATCH_COMPLETE;
    }
    
    /**
     * Legacy method - returns the winner.
     * 
     * @return the winning player/team name, or null
     * @deprecated Use getMatchWinner() instead
     */
    @Deprecated
    public String getWinner() {
        return getMatchWinner();
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
        sb.append("Mode: ").append(gameSetup.getGameMode()).append("\n");
        sb.append("Match: ").append(gameSetup.getMatchType().getDisplayName()).append("\n");
        sb.append("Set: ").append(currentSetNumber).append("\n");
        sb.append("State: ").append(state).append("\n");
        sb.append("Trump: ").append(trumpSuit != null ? trumpSuit.getSymbol() : "Not set").append("\n");
        
        if (gameSetup.isTeamMode()) {
            sb.append("\nTeams, Scores, and Sets Won:\n");
            for (Team team : teams) {
                sb.append("  ").append(team.getName()).append(": ");
                sb.append(scores.get(team.getName())).append(" points, ");
                sb.append(setsWon.get(team.getName())).append(" sets\n");
                sb.append("    Players: ").append(team.getPlayer1Name());
                sb.append(" & ").append(team.getPlayer2Name()).append("\n");
            }
        } else {
            sb.append("\nPlayers, Scores, and Sets Won:\n");
            for (String player : playerNames) {
                sb.append("  ").append(player).append(": ").append(scores.get(player));
                sb.append(" points, ").append(setsWon.get(player)).append(" sets");
                if (player.equals(getCurrentPlayer()) && state == GameState.IN_PROGRESS) {
                    sb.append(" (current)");
                }
                sb.append("\n");
            }
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
        /** A set has been won, ready to start new set or end match */
        SET_COMPLETE,
        /** Match is complete, a player/team has won required sets */
        MATCH_COMPLETE
    }
}
