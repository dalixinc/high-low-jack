package com.dalegames.highlowjack.ai;

import com.dalegames.highlowjack.model.AIDifficulty;

/**
 * Returns the correct {@link AIStrategy} for a given {@link AIDifficulty}.
 * Strategies are stateless singletons — safe to reuse across threads.
 */
public final class AIStrategyFactory {

    private static final AIStrategy EASY   = new EasyAIStrategy();
    private static final AIStrategy MEDIUM = new MediumAIStrategy();
    private static final AIStrategy HARD   = new HardAIStrategy();

    private AIStrategyFactory() {}

    public static AIStrategy select(AIDifficulty difficulty) {
        if (difficulty == null) return MEDIUM;
        return switch (difficulty) {
            case EASY   -> EASY;
            case MEDIUM -> MEDIUM;
            case HARD   -> HARD;
        };
    }
}
