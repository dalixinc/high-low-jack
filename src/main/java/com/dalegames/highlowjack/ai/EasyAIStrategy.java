package com.dalegames.highlowjack.ai;

import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.Game;

import java.util.List;
import java.util.Random;

/**
 * Easy AI — picks a legal card completely at random.
 *
 * <p>Deliberately makes no strategic decisions: no Jack protection, no
 * trick-value awareness, no partner awareness. Provides a fun, beatable
 * opponent for casual or learning games.</p>
 */
public class EasyAIStrategy extends BaseAIStrategy {

    private static final Random RANDOM = new Random();

    @Override
    public Card chooseCard(Game game, String playerName, List<Card> validCards) {
        return validCards.get(RANDOM.nextInt(validCards.size()));
    }
}
