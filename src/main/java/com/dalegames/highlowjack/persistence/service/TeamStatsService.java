package com.dalegames.highlowjack.persistence.service;

import com.dalegames.highlowjack.persistence.entity.TeamStats;
import com.dalegames.highlowjack.persistence.repository.TeamStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Team statistics operations.
 * Handles business logic and transactions for teams.
 * 
 * @author Dale & Primus
 * @version 1.0
 */
@Service
@Transactional
public class TeamStatsService {
    
    private final TeamStatsRepository teamStatsRepository;
    
    @Autowired
    public TeamStatsService(TeamStatsRepository teamStatsRepository) {
        this.teamStatsRepository = teamStatsRepository;
    }
    
    /**
     * Gets or creates a team by name and players.
     * 
     * @param teamName the team name
     * @param player1 first player
     * @param player2 second player
     * @return the existing or newly created team
     */
    public TeamStats getOrCreateTeam(String teamName, String player1, String player2) {
        return teamStatsRepository.findByNameIgnoreCase(teamName)
                .orElseGet(() -> createTeam(teamName, player1, player2));
    }
    
    /**
     * Creates a new team.
     */
    public TeamStats createTeam(String teamName, String player1, String player2) {
        if (teamStatsRepository.existsByNameIgnoreCase(teamName)) {
            throw new IllegalArgumentException("Team already exists: " + teamName);
        }
        
        TeamStats team = new TeamStats(teamName, player1, player2);
        return teamStatsRepository.save(team);
    }
    
    /**
     * Gets a team by name.
     */
    public Optional<TeamStats> getTeam(String teamName) {
        return teamStatsRepository.findByNameIgnoreCase(teamName);
    }
    
    /**
     * Gets all teams.
     */
    public List<TeamStats> getAllTeams() {
        return teamStatsRepository.findAll();
    }
    
    /**
     * Gets team leaderboard (top teams by wins).
     */
    public List<TeamStats> getLeaderboard() {
        return teamStatsRepository.findTop10ByOrderByMatchesWonDesc();
    }
    
    /**
     * Updates a team's stats after a match.
     * 
     * @param teamName the team name
     * @param player1 first player
     * @param player2 second player
     * @param won true if team won
     * @param setsWon number of sets won by this team
     */
    public void updateMatchStats(String teamName, String player1, String player2, 
                                 boolean won, int setsWon) {
        TeamStats team = getOrCreateTeam(teamName, player1, player2);
        
        if (won) {
            team.recordMatchWin(setsWon);
        } else {
            team.recordMatchLoss(setsWon);
        }
        
        teamStatsRepository.save(team);
    }
    
    /**
     * Records a point won by a team.
     * 
     * @param teamName the team name
     * @param player1 first player
     * @param player2 second player
     * @param category the point category (High/Low/Jack/Game)
     */
    public void recordPoint(String teamName, String player1, String player2, String category) {
        TeamStats team = getOrCreateTeam(teamName, player1, player2);
        team.recordPoint(category);
        teamStatsRepository.save(team);
    }
    
    /**
     * Deletes a team (admin operation).
     */
    public void deleteTeam(String teamName) {
        teamStatsRepository.findByNameIgnoreCase(teamName)
                .ifPresent(teamStatsRepository::delete);
    }
}