package com.dalegames.highlowjack.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the setup configuration for a High Low Jack match.
 * 
 * <p>This includes the game mode (individual or team), match type (best of 1/3/5),
 * player names and types, team assignments (if team mode), and which player is
 * the game controller.</p>
 * 
 * @author Dale &amp; Primus
 * @version 2.0 - Added team mode support
 */
public class GameSetup implements Serializable {
    
    private static final long serialVersionUID = 2L;  // Incremented for team mode changes
    
    /**
     * Type of match - determines how many sets must be won.
     */
    public enum MatchType {
        SINGLE_SET("Just One Set", 1),
        BEST_OF_THREE("Best of 3 Sets", 2),
        BEST_OF_FIVE("Best of 5 Sets", 3);
        
        private final String displayName;
        private final int setsToWin;
        
        MatchType(String displayName, int setsToWin) {
            this.displayName = displayName;
            this.setsToWin = setsToWin;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getSetsToWin() {
            return setsToWin;
        }
    }
    
    private final GameMode gameMode;
    private final List<PlayerInfo> players;
    private final MatchType matchType;
    private final List<Team> teams;  // null for INDIVIDUAL mode, 2 teams for TEAM mode
    
    /**
     * Creates a new GameSetup for individual mode.
     * 
     * @param players the list of 4 players (must be exactly 4)
     * @param matchType the match type (SINGLE_SET, BEST_OF_THREE, or BEST_OF_FIVE)
     * @throws IllegalArgumentException if players list is not exactly 4, or if no controller is designated
     */
    public GameSetup(List<PlayerInfo> players, MatchType matchType) {
        this(GameMode.INDIVIDUAL, players, matchType, null);
    }
    
    /**
     * Creates a new GameSetup with specified game mode.
     * 
     * @param gameMode the game mode (INDIVIDUAL or TEAM)
     * @param players the list of 4 players (must be exactly 4)
     * @param matchType the match type (SINGLE_SET, BEST_OF_THREE, or BEST_OF_FIVE)
     * @param teams the list of teams (required for TEAM mode, must be null for INDIVIDUAL)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public GameSetup(GameMode gameMode, List<PlayerInfo> players, MatchType matchType, List<Team> teams) {
        if (players == null || players.size() != 4) {
            throw new IllegalArgumentException("Must have exactly 4 players");
        }
        if (matchType == null) {
            throw new IllegalArgumentException("Match type cannot be null");
        }
        if (gameMode == null) {
            throw new IllegalArgumentException("Game mode cannot be null");
        }
        
        // Verify at least one controller exists
        boolean hasController = players.stream().anyMatch(PlayerInfo::isController);
        if (!hasController) {
            throw new IllegalArgumentException("At least one player must be designated as controller");
        }
        
        // Verify team configuration
        if (gameMode == GameMode.TEAM) {
            if (teams == null || teams.size() != 2) {
                throw new IllegalArgumentException("Team mode requires exactly 2 teams");
            }
        } else {
            if (teams != null) {
                throw new IllegalArgumentException("Individual mode should not have teams");
            }
        }
        
        this.gameMode = gameMode;
        this.players = new ArrayList<>(players);
        this.matchType = matchType;
        this.teams = teams != null ? new ArrayList<>(teams) : null;
    }
    
    /**
     * Factory method to create individual mode setup.
     * 
     * @param players the list of 4 players
     * @param matchType the match type
     * @return a new GameSetup for individual mode
     */
    public static GameSetup createIndividual(List<PlayerInfo> players, MatchType matchType) {
        return new GameSetup(GameMode.INDIVIDUAL, players, matchType, null);
    }
    
    /**
     * Factory method to create team mode setup.
     * 
     * @param players the list of 4 players
     * @param matchType the match type
     * @return a new GameSetup for team mode with auto-generated teams
     */
    public static GameSetup createTeam(List<PlayerInfo> players, MatchType matchType) {
        // Create Team 1: Players 1 & 3 (North-South)
        Team team1 = new Team(
            "Team 1 (North-South)",
            players.get(0).getName(),  // Player 1 (North)
            players.get(2).getName()   // Player 3 (South)
        );
        
        // Create Team 2: Players 2 & 4 (East-West)
        Team team2 = new Team(
            "Team 2 (East-West)",
            players.get(1).getName(),  // Player 2 (East)
            players.get(3).getName()   // Player 4 (West)
        );
        
        List<Team> teams = new ArrayList<>();
        teams.add(team1);
        teams.add(team2);
        
        return new GameSetup(GameMode.TEAM, players, matchType, teams);
    }
    
    /**
     * Gets the game mode.
     * 
     * @return the game mode (INDIVIDUAL or TEAM)
     */
    public GameMode getGameMode() {
        return gameMode;
    }
    
    /**
     * Checks if this is team mode.
     * 
     * @return true if game mode is TEAM
     */
    public boolean isTeamMode() {
        return gameMode == GameMode.TEAM;
    }
    
    /**
     * Gets the list of teams (only valid in team mode).
     * 
     * @return list of 2 teams, or null if individual mode
     */
    public List<Team> getTeams() {
        return teams != null ? new ArrayList<>(teams) : null;
    }
    
    /**
     * Gets a specific team by index (0 or 1).
     * 
     * @param index the team index (0 or 1)
     * @return the team
     * @throws IllegalStateException if not in team mode
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public Team getTeam(int index) {
        if (!isTeamMode()) {
            throw new IllegalStateException("Cannot get team in individual mode");
        }
        return teams.get(index);
    }
    
    /**
     * Gets the team for a given player.
     * 
     * @param playerName the player's name
     * @return the team containing this player
     * @throws IllegalStateException if not in team mode or player not found
     */
    public Team getTeamForPlayer(String playerName) {
        if (!isTeamMode()) {
            throw new IllegalStateException("Cannot get team in individual mode");
        }
        return teams.stream()
                .filter(team -> team.hasPlayer(playerName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Player not found in any team: " + playerName));
    }
    
    /**
     * Gets the list of players.
     * 
     * @return immutable list of 4 players
     */
    public List<PlayerInfo> getPlayers() {
        return new ArrayList<>(players);
    }
    
    /**
     * Gets a specific player by index (0-3).
     * 
     * @param index the player index (0-3)
     * @return the player info
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public PlayerInfo getPlayer(int index) {
        return players.get(index);
    }
    
    /**
     * Gets the match type.
     * 
     * @return the match type
     */
    public MatchType getMatchType() {
        return matchType;
    }
    
    /**
     * Gets the number of sets needed to win the match.
     * 
     * @return 1 for single set, 2 for best of 3, 3 for best of 5
     */
    public int getSetsToWin() {
        return matchType.getSetsToWin();
    }
    
    /**
     * Gets the player names as an array.
     * 
     * @return array of 4 player names
     */
    public String[] getPlayerNames() {
        return players.stream()
                .map(PlayerInfo::getName)
                .toArray(String[]::new);
    }
    
    /**
     * Gets the game controller player.
     * 
     * @return the player info for the controller
     */
    public PlayerInfo getController() {
        return players.stream()
                .filter(PlayerInfo::isController)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No controller found"));
    }
    
    /**
     * Checks if a player name is human-controlled.
     * 
     * @param playerName the player name to check
     * @return true if the player is human
     */
    public boolean isHumanPlayer(String playerName) {
        return players.stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst()
                .map(PlayerInfo::isHuman)
                .orElse(false);
    }
    
    /**
     * Checks if a player is the game controller.
     * 
     * @param playerName the player name to check
     * @return true if this player is the controller
     */
    public boolean isController(String playerName) {
        return players.stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst()
                .map(PlayerInfo::isController)
                .orElse(false);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GameSetup{");
        sb.append("gameMode=").append(gameMode);
        sb.append(", matchType=").append(matchType.getDisplayName());
        sb.append(", players=").append(players);
        if (isTeamMode()) {
            sb.append(", teams=").append(teams);
        }
        sb.append('}');
        return sb.toString();
    }
}
