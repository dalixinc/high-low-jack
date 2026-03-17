package com.dalegames.highlowjack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.Serializable;

/**
 * Represents a standard 52-card deck.
 * 
 * <p>The deck contains one card of each combination of suit and rank.
 * Provides methods for shuffling and dealing cards.</p>
 * 
 * @author Dale &amp; Primus
 * @version 1.0
 */
public class Deck implements Serializable{
    private final List<Card> cards;

    /**
     * Constructs a new deck containing all 52 standard playing cards.
     * The deck is created in a standard order (not shuffled).
     */
    public Deck() {
        cards = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    /**
     * Shuffles the deck into a random order.
     * Uses {@link Collections#shuffle(List)} for randomization.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Deals (removes and returns) one card from the top of the deck.
     * 
     * @return the dealt card
     * @throws IllegalStateException if the deck is empty
     */
    public Card deal() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Cannot deal from empty deck");
        }
        return cards.remove(0);
    }

    /**
     * Deals multiple cards from the deck.
     * 
     * @param numCards the number of cards to deal
     * @return a list containing the dealt cards
     * @throws IllegalStateException if there aren't enough cards in the deck
     * @throws IllegalArgumentException if numCards is negative
     */
    public List<Card> dealHand(int numCards) {
        if (numCards < 0) {
            throw new IllegalArgumentException("Cannot deal negative number of cards");
        }
        if (numCards > cards.size()) {
            throw new IllegalStateException(
                String.format("Not enough cards: requested %d, available %d", numCards, cards.size())
            );
        }
        
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < numCards; i++) {
            hand.add(deal());
        }
        return hand;
    }

    /**
     * Returns the number of cards remaining in the deck.
     * 
     * @return the number of undealt cards
     */
    public int cardsRemaining() {
        return cards.size();
    }

    /**
     * Returns a copy of the current cards in the deck.
     * Changes to the returned list will not affect the deck.
     * 
     * @return a new list containing the current deck cards
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Returns whether the deck is empty.
     * 
     * @return true if no cards remain in the deck
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
