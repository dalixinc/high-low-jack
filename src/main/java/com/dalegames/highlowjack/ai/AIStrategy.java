package com.dalegames.highlowjack.ai;

import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.Game;

import java.util.List;

/**
 * Strategy interface for AI card selection.
 *
 * <p>Each implementation represents one difficulty level. The contract is simple:
 * given the full game state, the AI player's name, and the list of cards it is
 * legally allowed to play, return the card it chooses to play.</p>
 *
 * <p>The {@code validCards} list is guaranteed to be non-empty and to contain only
 * cards that pass {@link com.dalegames.highlowjack.engine.GameEngine#isValidPlay}.</p>
 */
public interface AIStrategy {
    Card chooseCard(Game game, String playerName, List<Card> validCards);
}
