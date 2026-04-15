package com.dalegames.highlowjack.model;

/**
 * Difficulty level for AI-controlled players.
 *
 * <ul>
 *   <li>EASY   — plays a random legal card every turn</li>
 *   <li>MEDIUM — plays sensibly: protects the Jack, avoids wasting point cards</li>
 *   <li>HARD   — full heuristic suite: partner protection, Jack tracking,
 *                game-point aggression, strategic discard</li>
 * </ul>
 *
 * Future: LEARNING — adjusts heuristic weights based on match outcomes.
 */
public enum AIDifficulty {
    EASY, MEDIUM, HARD;

    public String displayName() {
        return switch (this) {
            case EASY   -> "Easy";
            case MEDIUM -> "Medium";
            case HARD   -> "Hard";
        };
    }
}
