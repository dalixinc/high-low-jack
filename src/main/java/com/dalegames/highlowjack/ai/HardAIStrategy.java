package com.dalegames.highlowjack.ai;

import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.Trick;

import java.util.Comparator;
import java.util.List;

/**
 * Hard AI — full heuristic suite on top of the Medium strategy.
 *
 * <p>Additional heuristics over Medium:</p>
 * <ul>
 *   <li><strong>Partner protection</strong> (team mode) — never waste trump when
 *       a partner is already winning the trick.</li>
 *   <li><strong>Jack tracking</strong> — knows whether the Jack of trumps has
 *       been played; plays or protects it accordingly.</li>
 *   <li><strong>Game-point aggression</strong> — lowers the "valuable trick"
 *       threshold to 7 pts (e.g. a single King is worth fighting for).</li>
 *   <li><strong>Strategic lead</strong> — pitches the Ace of trump to lock in
 *       High, or leads the Two of trump when it is the lowest live trump.</li>
 *   <li><strong>Smart discard</strong> — prefers voiding a suit over random
 *       discard to create future ruffing opportunities.</li>
 * </ul>
 */
public class HardAIStrategy extends BaseAIStrategy {

    /** Below this trick value we still try to win — Hard AI is more aggressive. */
    private static final int VALUABLE_TRICK_THRESHOLD = 7;

    /** Only play the Jack when the trick is worth at least this many points. */
    private static final int JACK_PLAY_THRESHOLD = 10;

    @Override
    public Card chooseCard(Game game, String playerName, List<Card> validCards) {
        boolean isLeading = game.getCurrentTrick() == null || game.getCurrentTrick().size() == 0;
        return isLeading ? lead(validCards, game, playerName) : follow(validCards, game, playerName);
    }

    // ── Leading ───────────────────────────────────────────────────────────────

    private Card lead(List<Card> valid, Game game, String playerName) {
        Card.Suit trump = game.getTrumpSuit();

        // Strategic trump lead: lock in High with the Ace of trump
        Card aceOfTrump = find(valid, c -> c.getSuit() == trump && c.getRank() == Card.Rank.ACE);
        if (aceOfTrump != null) return aceOfTrump;

        // Strategic trump lead: lock in Low with the lowest trump in hand,
        // but only if it is the absolute lowest unplayed trump (Two is ideal)
        Card lowestTrump = valid.stream()
            .filter(c -> c.getSuit() == trump)
            .min(Comparator.comparingInt(c -> c.getRank().getValue()))
            .orElse(null);
        if (lowestTrump != null && lowestTrump.getRank().getValue() <= 3) {
            // Two or Three — likely to be Low; lead it to claim the point
            return lowestTrump;
        }

        // Fall back to Medium lead logic (avoid Tens, lead high, lead low)
        List<Card> nonTens = valid.stream().filter(c -> c.getRank() != Card.Rank.TEN).toList();
        List<Card> candidates = nonTens.isEmpty() ? valid : nonTens;

        Card high = find(candidates,
            c -> c.getRank() == Card.Rank.ACE || c.getRank() == Card.Rank.KING);
        if (high != null) return high;

        Card low = find(candidates, BaseAIStrategy::isWorthless);
        if (low != null) return low;

        return candidates.get(0);
    }

    // ── Following ─────────────────────────────────────────────────────────────

    private Card follow(List<Card> valid, Game game, String playerName) {
        Card.Suit lead  = game.getCurrentTrick().getLeadSuit();
        Card.Suit trump = game.getTrumpSuit();
        Trick trick     = game.getCurrentTrick();

        // ── Partner protection (team mode) ────────────────────────────────────
        // If our partner is already winning, throw our cheapest card.
        if (isPartnerWinning(game, playerName)) {
            boolean canFollow = valid.stream().anyMatch(c -> c.getSuit() == lead);
            List<Card> pool = canFollow
                ? valid.stream().filter(c -> c.getSuit() == lead).toList()
                : valid;
            return duckLowest(pool);
        }

        // ── Can we follow suit? ───────────────────────────────────────────────
        boolean canFollow = valid.stream().anyMatch(c -> c.getSuit() == lead);
        if (!canFollow) return smartDiscard(valid, trump, game, playerName);

        List<Card> followCards = valid.stream().filter(c -> c.getSuit() == lead).toList();
        int trickVal = trickValue(trick);
        Card winner  = currentWinner(trick, trump);

        // ── Jack of trump awareness ───────────────────────────────────────────
        // Use a lower play threshold because Hard AI is more confident in its reads.
        Card jack = find(followCards,
            c -> c.getSuit() == trump && c.getRank() == Card.Rank.JACK);
        if (jack != null) {
            boolean jackAlreadyPlayed = jackOfTrumpPlayed(game);
            // If Jack is still out there, be more willing to play ours to capture tricks
            int jackThreshold = jackAlreadyPlayed ? 5 : JACK_PLAY_THRESHOLD;
            if (trickVal < jackThreshold) {
                List<Card> noJack = followCards.stream()
                    .filter(c -> !(c.getSuit() == trump && c.getRank() == Card.Rank.JACK))
                    .toList();
                if (!noJack.isEmpty()) followCards = noJack;
            }
        }

        // ── Try to win valuable tricks ────────────────────────────────────────
        if (trickVal >= VALUABLE_TRICK_THRESHOLD) {
            Card lowestWin = followCards.stream()
                .filter(c -> canBeat(c, winner, trump))
                .min(Comparator.comparingInt(c -> c.getRank().getValue()))
                .orElse(null);
            if (lowestWin != null) return lowestWin;
        }

        return duckLowest(followCards);
    }

    // ── Smart discard ─────────────────────────────────────────────────────────

    /**
     * Hard AI discard: prefer to void a suit (to enable future ruffing)
     * rather than discarding purely by point value.
     */
    private Card smartDiscard(List<Card> valid, Card.Suit trump, Game game, String playerName) {
        // Find the non-trump suit in which we have the fewest cards — void it
        long minCount = Long.MAX_VALUE;
        Card.Suit voidTarget = null;
        for (Card.Suit suit : Card.Suit.values()) {
            if (suit == trump) continue;
            long count = valid.stream().filter(c -> c.getSuit() == suit).count();
            if (count > 0 && count < minCount) {
                minCount = count;
                voidTarget = suit;
            }
        }
        if (voidTarget != null) {
            final Card.Suit vs = voidTarget;
            // Discard the worthless card from that suit first, then any card
            Card voidCard = find(valid, c -> c.getSuit() == vs && isWorthless(c));
            if (voidCard == null) voidCard = find(valid, c -> c.getSuit() == vs);
            if (voidCard != null) return voidCard;
        }

        // Fall back to standard discard priority
        return bestDiscard(valid, trump);
    }
}
