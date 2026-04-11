package com.dalegames.highlowjack.persistence.service;

import com.dalegames.highlowjack.model.H2HCell;
import com.dalegames.highlowjack.persistence.entity.HeadToHead;
import com.dalegames.highlowjack.persistence.repository.HeadToHeadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Manages head-to-head records between players or teams.
 *
 * @author Dale & Primus
 * @version 1.0
 */
@Service
@Transactional
public class HeadToHeadService {

    private final HeadToHeadRepository repository;

    @Autowired
    public HeadToHeadService(HeadToHeadRepository repository) {
        this.repository = repository;
    }

    /**
     * Records a win for {@code winner} against {@code loser}.
     * Creates the record if it doesn't exist yet.
     */
    public void recordResult(String winner, String loser) {
        // Canonical ordering: alphabetically first is playerA
        String playerA, playerB;
        if (winner.compareToIgnoreCase(loser) <= 0) {
            playerA = winner;
            playerB = loser;
        } else {
            playerA = loser;
            playerB = winner;
        }

        HeadToHead record = repository.findByPlayerAAndPlayerB(playerA, playerB)
            .orElseGet(() -> new HeadToHead(playerA, playerB));

        record.recordWin(winner);
        repository.save(record);
    }

    /**
     * Returns the H2H record between two competitors regardless of canonical order.
     * Returns empty Optional if they've never played.
     */
    public Optional<HeadToHead> getRecord(String name1, String name2) {
        String playerA = name1.compareToIgnoreCase(name2) <= 0 ? name1 : name2;
        String playerB = name1.compareToIgnoreCase(name2) <= 0 ? name2 : name1;
        return repository.findByPlayerAAndPlayerB(playerA, playerB);
    }

    /**
     * Returns all H2H records that involve the given competitor.
     */
    public List<HeadToHead> getRecordsForPlayer(String name) {
        return repository.findAllByPlayer(name);
    }

    /**
     * Builds a 2D grid (list of rows, each row a list of cells) ordered by {@code names}.
     * Diagonal cells are self-cells. Safe to iterate in Thymeleaf without map indexing.
     */
    public List<List<H2HCell>> buildGrid(List<String> names) {
        int n = names.size();

        // Initialise n×n grid with zero cells; diagonal = self
        H2HCell[][] raw = new H2HCell[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                raw[i][j] = (i == j) ? H2HCell.selfCell() : H2HCell.of(0, 0);
            }
        }

        // Fill from DB
        List<HeadToHead> all = repository.findAll();
        for (HeadToHead h : all) {
            int a = names.indexOf(h.getPlayerA());
            int b = names.indexOf(h.getPlayerB());
            if (a >= 0 && b >= 0) {
                raw[a][b] = H2HCell.of(h.getPlayerAWins(), h.getPlayerBWins());
                raw[b][a] = H2HCell.of(h.getPlayerBWins(), h.getPlayerAWins());
            }
        }

        // Convert to List<List<H2HCell>>
        List<List<H2HCell>> grid = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<H2HCell> row = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                row.add(raw[i][j]);
            }
            grid.add(row);
        }
        return grid;
    }
}
