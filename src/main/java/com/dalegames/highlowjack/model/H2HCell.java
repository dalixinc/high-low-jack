package com.dalegames.highlowjack.model;

/**
 * Carries the win/loss record for one cell of the head-to-head grid.
 * All logic lives here so the Thymeleaf template needs no SpEL comparisons.
 */
public class H2HCell {

    private final int wins;
    private final int losses;
    private final boolean self;

    private H2HCell(int wins, int losses, boolean self) {
        this.wins = wins;
        this.losses = losses;
        this.self = self;
    }

    /** A cell on the diagonal (player vs themselves). */
    public static H2HCell selfCell() {
        return new H2HCell(0, 0, true);
    }

    /** A normal matchup cell. */
    public static H2HCell of(int wins, int losses) {
        return new H2HCell(wins, losses, false);
    }

    public int getWins()    { return wins; }
    public int getLosses()  { return losses; }
    public boolean isSelf() { return self; }

    /** Full CSS class string ready for th:class. */
    public String getCssClass() {
        if (self) return "h2h-cell h2h-self";
        if (wins > losses) return "h2h-cell h2h-win";
        if (wins < losses) return "h2h-cell h2h-loss";
        return "h2h-cell h2h-even";
    }
}
