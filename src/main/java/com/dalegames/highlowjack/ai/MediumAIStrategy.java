package com.dalegames.highlowjack.ai;

import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.Trick;

import java.util.Comparator;
import java.util.List;

/**
 * Medium AI — the original SimpleAI logic promoted to a named strategy.
 *
 * <p>Core principles:</p>
 * <ul>
 *   <li>Never lead a Ten (too valuable to expose)</li>
 *   <li>Lead high (Ace/King) to capture points</li>
 *   <li>Lead low (2–9) when nothing better is available</li>
 *   <li>Protect the Jack of trumps — only play it on a 15+ point trick</li>
 *   <li>Win valuable tricks (≥10 pts) with the lowest winning card</li>
 *   <li>Discard worthless cards when unable to follow suit</li>
 * </ul>
 */
public class MediumAIStrategy extends BaseAIStrategy {

    @Override
    public Card chooseCard(Game game, String playerName, List<Card> validCards) {
        boolean isLeading = game.getCurrentTrick() == null || game.getCurrentTrick().size() == 0;
        return isLeading ? lead(validCards, game) : follow(validCards, game);
    }

    // ── Leading ───────────────────────────────────────────────────────────────

    private Card lead(List<Card> valid, Game game) {
        // Never lead a Ten
        List<Card> nonTens = valid.stream().filter(c -> c.getRank() != Card.Rank.TEN).toList();
        List<Card> candidates = nonTens.isEmpty() ? valid : nonTens;

        // Lead Ace or King to win the trick and capture points
        Card high = find(candidates,
            c -> c.getRank() == Card.Rank.ACE || c.getRank() == Card.Rank.KING);
        if (high != null) return high;

        // Lead a worthless card to pass the lead safely
        Card low = find(candidates, BaseAIStrategy::isWorthless);
        if (low != null) return low;

        return candidates.get(0);
    }

    // ── Following ─────────────────────────────────────────────────────────────

    private Card follow(List<Card> valid, Game game) {
        Card.Suit lead  = game.getCurrentTrick().getLeadSuit();
        Card.Suit trump = game.getTrumpSuit();

        boolean canFollow = valid.stream().anyMatch(c -> c.getSuit() == lead);
        if (!canFollow) return bestDiscard(valid, trump);

        List<Card> followCards = valid.stream().filter(c -> c.getSuit() == lead).toList();
        int trickVal = trickValue(game.getCurrentTrick());
        Card winner  = currentWinner(game.getCurrentTrick(), trump);

        // Protect the Jack of trumps — don't play it unless the trick is very valuable
        Card jack = find(followCards,
            c -> c.getSuit() == trump && c.getRank() == Card.Rank.JACK);
        if (jack != null && trickVal < 15) {
            List<Card> noJack = followCards.stream()
                .filter(c -> !(c.getSuit() == trump && c.getRank() == Card.Rank.JACK))
                .toList();
            if (!noJack.isEmpty()) followCards = noJack;
        }

        // Valuable trick — try to win with the lowest card that beats the current winner
        if (trickVal >= 10) {
            Card lowestWin = followCards.stream()
                .filter(c -> canBeat(c, winner, trump))
                .min(Comparator.comparingInt(c -> c.getRank().getValue()))
                .orElse(null);
            if (lowestWin != null) return lowestWin;
        }

        return duckLowest(followCards);
    }
}
