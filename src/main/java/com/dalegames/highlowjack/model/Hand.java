package com.dalegames.highlowjack.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.io.Serializable;

/**
 * Represents a player's hand of cards in the game.
 * 
 * <p>A hand can hold any number of cards (typically 7 in High Low Jack) and
 * provides methods for adding, playing, and querying cards.</p>
 * 
 * @author Dale &amp; Primus
 * @version 1.1
 */
public class Hand implements Serializable{
    private final List<Card> cards;
    private final String playerName;

    /**
     * Constructs a new empty hand for the specified player.
     * 
     * @param playerName the name of the player who owns this hand
     * @throws IllegalArgumentException if playerName is null or empty
     */
    public Hand(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        this.playerName = playerName;
        this.cards = new ArrayList<>();
    }

    /**
     * Adds a single card to this hand.
     * 
     * @param card the card to add
     * @throws NullPointerException if card is null
     */
    public void addCard(Card card) {
        if (card == null) {
            throw new NullPointerException("Cannot add null card to hand");
        }
        cards.add(card);
    }

    /**
     * Adds multiple cards to this hand.
     * 
     * @param newCards the cards to add
     * @throws NullPointerException if newCards is null or contains null elements
     */
    public void addCards(List<Card> newCards) {
        if (newCards == null) {
            throw new NullPointerException("Cannot add null list of cards");
        }
        for (Card card : newCards) {
            addCard(card);
        }
    }

    /**
     * Plays (removes and returns) the card at the specified index.
     * 
     * @param index the position of the card to play (0-based)
     * @return the played card
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public Card playCard(int index) {
        if (index < 0 || index >= cards.size()) {
            throw new IndexOutOfBoundsException(
                String.format("Invalid card index: %d (hand size: %d)", index, cards.size())
            );
        }
        return cards.remove(index);
    }

    /**
     * Plays (removes and returns) the specified card from this hand.
     * 
     * @param card the card to play
     * @return the played card
     * @throws IllegalArgumentException if the card is not in this hand
     * @throws NullPointerException if card is null
     */
    public Card playCard(Card card) {
        if (card == null) {
            throw new NullPointerException("Cannot play null card");
        }
        if (!cards.remove(card)) {
            throw new IllegalArgumentException("Card not in hand: " + card);
        }
        return card;
    }

    /**
     * Returns a copy of the cards in this hand.
     * Changes to the returned list will not affect the hand.
     * 
     * @return a new list containing this hand's cards
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Returns the number of cards in this hand.
     * 
     * @return the hand size
     */
    public int size() {
        return cards.size();
    }

    /**
     * Returns whether this hand is empty.
     * 
     * @return true if the hand contains no cards
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Returns the name of the player who owns this hand.
     * 
     * @return the player's name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Returns whether this hand contains the specified card.
     * 
     * @param card the card to check for
     * @return true if the hand contains the card
     */
    public boolean hasCard(Card card) {
        return cards.contains(card);
    }

    /**
     * Returns whether this hand contains any cards of the specified suit.
     * 
     * @param suit the suit to check for
     * @return true if at least one card of the suit is in the hand
     */
    public boolean hasSuit(Card.Suit suit) {
        return cards.stream().anyMatch(c -> c.getSuit() == suit);
    }

    /**
     * Sorts the hand by suit (Spades, Hearts, Diamonds, Clubs) and rank (high to low).
     * The sort is performed in-place.
     */
    public void sort() {
        cards.sort(Comparator
            .comparing((Card c) -> getSuitOrder(c.getSuit()))
            .thenComparing((Card c) -> c.getRank().getValue(), Comparator.reverseOrder())
        );
    }

    /**
     * Returns the sort order for suits: Spades=0, Hearts=1, Diamonds=2, Clubs=3.
     * 
     * @param suit the suit to get the order for
     * @return the sort order (0-3)
     */
    private int getSuitOrder(Card.Suit suit) {
        switch (suit) {
            case SPADES: return 0;
            case HEARTS: return 1;
            case DIAMONDS: return 2;
            case CLUBS: return 3;
            default: return 4;
        }
    }

    /**
     * Returns a string representation of this hand.
     * 
     * @return a formatted string showing the player name and cards
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(playerName).append("'s hand: ");
        for (int i = 0; i < cards.size(); i++) {
            sb.append(i + 1).append(". ").append(cards.get(i));
            if (i < cards.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
