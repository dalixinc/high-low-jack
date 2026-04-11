package com.dalegames.highlowjack.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks head-to-head win/loss records between two named competitors
 * (players in individual mode, team names in team mode).
 * Records are stored canonically with playerA alphabetically before playerB.
 *
 * @author Dale & Primus
 * @version 1.0
 */
@Entity
@Table(name = "head_to_head", uniqueConstraints = {
    @UniqueConstraint(name = "uq_h2h_players", columnNames = {"player_a", "player_b"})
})
public class HeadToHead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Alphabetically first of the two competitors. */
    @Column(name = "player_a", nullable = false, length = 100)
    private String playerA;

    /** Alphabetically second of the two competitors. */
    @Column(name = "player_b", nullable = false, length = 100)
    private String playerB;

    @Column(name = "player_a_wins")
    private int playerAWins = 0;

    @Column(name = "player_b_wins")
    private int playerBWins = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public HeadToHead() {}

    public HeadToHead(String playerA, String playerB) {
        this.playerA = playerA;
        this.playerB = playerB;
    }

    // ── Business methods ─────────────────────────────────────────────────────

    /** Record a win for the named competitor. */
    public void recordWin(String winner) {
        if (winner.equals(playerA)) {
            playerAWins++;
        } else if (winner.equals(playerB)) {
            playerBWins++;
        }
        lastUpdated = LocalDateTime.now();
    }

    /** Wins for the named competitor (0 if not in this record). */
    public int getWinsFor(String name) {
        if (name.equals(playerA)) return playerAWins;
        if (name.equals(playerB)) return playerBWins;
        return 0;
    }

    /** Losses for the named competitor (0 if not in this record). */
    public int getLossesFor(String name) {
        if (name.equals(playerA)) return playerBWins;
        if (name.equals(playerB)) return playerAWins;
        return 0;
    }

    /** The opponent of the named competitor in this record. */
    public String getOpponentOf(String name) {
        if (name.equals(playerA)) return playerB;
        if (name.equals(playerB)) return playerA;
        return null;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getPlayerA() { return playerA; }
    public String getPlayerB() { return playerB; }
    public int getPlayerAWins() { return playerAWins; }
    public int getPlayerBWins() { return playerBWins; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
