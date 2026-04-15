package com.dalegames.highlowjack.ai;

import com.dalegames.highlowjack.model.Card;
import com.dalegames.highlowjack.model.Team;
import com.dalegames.highlowjack.model.Trick;
import com.dalegames.highlowjack.model.Game;

import java.util.Comparator;
import java.util.List;

/**
 * Shared helper methods used by multiple AI strategy implementations.
 */
abstract class BaseAIStrategy implements AIStrategy {

    // ── Card classification ───────────────────────────────────────────────────

    /** True for 2–9 (zero game-point value). */
    static boolean isWorthless(Card card) {
        int v = card.getRank().getValue();
        return v >= 2 && v <= 9;
    }

    // ── Trick analysis ────────────────────────────────────────────────────────

    /** Sum of game-point values of all cards played so far in the trick. */
    static int trickValue(Trick trick) {
        int total = 0;
        for (Trick.CardPlay p : trick.getPlays()) total += p.card.getRank().getPoints();
        return total;
    }

    /**
     * The card currently winning the trick (highest trump, or highest of lead
     * suit if no trump played).
     */
    static Card currentWinner(Trick trick, Card.Suit trump) {
        if (trick.getPlays().isEmpty()) return null;
        Trick.CardPlay best = trick.getPlays().get(0);
        for (Trick.CardPlay p : trick.getPlays()) {
            if (beats(p.card, best.card, trick.getLeadSuit(), trump)) best = p;
        }
        return best.card;
    }

    /** The name of the player whose card is currently winning the trick. */
    static String currentWinnerName(Trick trick, Card.Suit trump) {
        if (trick.getPlays().isEmpty()) return null;
        Trick.CardPlay best = trick.getPlays().get(0);
        for (Trick.CardPlay p : trick.getPlays()) {
            if (beats(p.card, best.card, trick.getLeadSuit(), trump)) best = p;
        }
        return best.playerName;
    }

    static boolean beats(Card c1, Card c2, Card.Suit lead, Card.Suit trump) {
        boolean c1Trump = c1.getSuit() == trump;
        boolean c2Trump = c2.getSuit() == trump;
        if (c1Trump && !c2Trump) return true;
        if (!c1Trump && c2Trump) return false;
        if (c1.getSuit() == c2.getSuit()) return c1.getRank().getValue() > c2.getRank().getValue();
        return false;
    }

    static boolean canBeat(Card ours, Card winner, Card.Suit trump) {
        if (winner == null) return true;
        boolean ourTrump     = ours.getSuit()   == trump;
        boolean theirTrump   = winner.getSuit() == trump;
        if (ourTrump && !theirTrump) return true;
        if (!ourTrump && theirTrump) return false;
        if (ours.getSuit() == winner.getSuit()) return ours.getRank().getValue() > winner.getRank().getValue();
        return false;
    }

    // ── Discard helper ────────────────────────────────────────────────────────

    /**
     * Standard discard priority (shared by Medium and Hard):
     * worthless non-trump → off-suit Queen → off-suit Ten →
     * off-suit King → off-suit Ace → anything else.
     */
    static Card bestDiscard(List<Card> validCards, Card.Suit trump) {
        return firstNonNull(
            find(validCards, c -> c.getSuit() != trump && isWorthless(c)),
            find(validCards, c -> c.getRank() == Card.Rank.QUEEN && c.getSuit() != trump),
            find(validCards, c -> c.getRank() == Card.Rank.TEN   && c.getSuit() != trump),
            find(validCards, c -> c.getRank() == Card.Rank.KING  && c.getSuit() != trump),
            find(validCards, c -> c.getRank() == Card.Rank.ACE   && c.getSuit() != trump),
            validCards.get(0)
        );
    }

    /** Duck with the lowest game-point-value card available. */
    static Card duckLowest(List<Card> cards) {
        Card worthless = cards.stream()
            .filter(BaseAIStrategy::isWorthless)
            .min(Comparator.comparingInt(c -> c.getRank().getValue()))
            .orElse(null);
        if (worthless != null) return worthless;
        return cards.stream()
            .min(Comparator.comparingInt(c -> c.getRank().getPoints()))
            .orElse(cards.get(0));
    }

    // ── Team helpers ──────────────────────────────────────────────────────────

    /** True when the game is in team mode and the given player's partner is currently winning the trick. */
    static boolean isPartnerWinning(Game game, String myName) {
        if (!game.isTeamMode()) return false;
        Trick trick = game.getCurrentTrick();
        if (trick == null || trick.getPlays().isEmpty()) return false;
        try {
            for (Team team : game.getTeams()) {
                if (!team.hasPlayer(myName)) continue;
                String partner = team.getPlayer1Name().equals(myName)
                        ? team.getPlayer2Name() : team.getPlayer1Name();
                return partner.equals(currentWinnerName(trick, game.getTrumpSuit()));
            }
        } catch (Exception ignored) {}
        return false;
    }

    // ── Card memory ───────────────────────────────────────────────────────────

    /** True if the Jack of trumps has already been played in a previous trick. */
    static boolean jackOfTrumpPlayed(Game game) {
        Card.Suit trump = game.getTrumpSuit();
        if (trump == null) return false;
        for (Trick trick : game.getTricks()) {
            for (Trick.CardPlay play : trick.getPlays()) {
                if (play.card.getSuit() == trump && play.card.getRank() == Card.Rank.JACK)
                    return true;
            }
        }
        return false;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    static Card find(List<Card> cards, java.util.function.Predicate<Card> pred) {
        return cards.stream().filter(pred).findFirst().orElse(null);
    }

    @SafeVarargs
    static <T> T firstNonNull(T... values) {
        for (T v : values) if (v != null) return v;
        return null;
    }
}
