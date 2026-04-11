package com.dalegames.highlowjack.model;

import java.util.Map;

/**
 * Result of an early-wrap-up availability check.
 *
 * <p>Indicates that the High, Low, and Jack points for the current round
 * are already fully determined (locked), and that at least one player or
 * team is guaranteed to reach the winning score of 11 from those points
 * alone — so the remaining tricks need not be played.</p>
 *
 * <p>Created by {@code GameEngine.checkWrapUpLocked(Game)}.</p>
 *
 * @author Dale &amp; Primus
 */
public class WrapUpInfo {

    /** Team/player who holds High (highest trump in a completed trick, unbeatable). */
    private final String highWinner;

    /** Team/player who holds Low (lowest trump in a completed trick, unbeatable). */
    private final String lowWinner;

    /**
     * Team/player who won the Jack of trump; {@code null} if the Jack was never
     * dealt into play ({@link #jackAbsent} will be {@code true} in that case).
     */
    private final String jackWinner;

    /** True if the Jack of trump was not dealt to any player this round. */
    private final boolean jackAbsent;

    /** Locked points per entity (team name in team mode, player name in individual mode). */
    private final Map<String, Integer> lockedPoints;

    /**
     * The entity (team or player) that is guaranteed to win the set if it is
     * claimed now.  Their current score plus their locked points is ≥ 11.
     */
    private final String setWinner;

    public WrapUpInfo(String highWinner, String lowWinner, String jackWinner,
                      boolean jackAbsent, Map<String, Integer> lockedPoints,
                      String setWinner) {
        this.highWinner   = highWinner;
        this.lowWinner    = lowWinner;
        this.jackWinner   = jackWinner;
        this.jackAbsent   = jackAbsent;
        this.lockedPoints = lockedPoints;
        this.setWinner    = setWinner;
    }

    public String getHighWinner()               { return highWinner; }
    public String getLowWinner()                { return lowWinner; }
    public String getJackWinner()               { return jackWinner; }
    public boolean isJackAbsent()               { return jackAbsent; }
    public Map<String, Integer> getLockedPoints(){ return lockedPoints; }
    public String getSetWinner()                { return setWinner; }

    /** Convenience: locked points for the given entity (0 if none). */
    public int getLockedTotal(String entity) {
        return lockedPoints.getOrDefault(entity, 0);
    }
}
