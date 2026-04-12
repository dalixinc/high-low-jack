package com.dalegames.highlowjack.model;

/**
 * Triggers for personality quips - contextual moments in the game.
 * 
 * @author Dale & Primus
 * @version 1.0
 */
public enum QuipTrigger {
    // PLAYER SPECIALS (player_name in DB controls who triggers these)
    CUT_TWO_LOSING("Player cuts a 2 while losing"),
    CUT_TWO_WINNING("Player cuts a 2 while winning"),
    PLAY_ACE_SPADES("Player plays Ace of Spades"),
    WIN_WITH_ACE_SPADES("Player wins with Ace of Spades"),
    CURSE_OF_SCOTLAND("Player plays the 9 of Diamonds"),

    // CUT CEREMONY
    CUT_ACE("Player cuts an Ace"),
    CUT_TWO("Player cuts a Two"),
    CUT_HIGH_CARD("Player cuts a high card (10, J, Q, K)"),
    CUT_LOW_CARD("Player cuts a low card (3-5)"),

    // ROUND EVENTS
    TIEBREAKER_WIN("Won on tiebreaker (precedence rules)"),
    SWEEP_ALL_FOUR("Won all 4 points in a round"),
    WON_WITH_HIGH("Won round with High"),
    WON_WITH_LOW("Won round with Low"),
    WON_WITH_JACK("Won round with Jack"),
    WON_WITH_GAME("Won round with Game point"),
    
    // COMEBACK MOMENTS
    COMEBACK_FROM_ZERO("Won set after being down 0-10"),
    COMEBACK_FROM_FIVE("Won set after being down 0-5"),
    CLOSE_WIN("Won set 11-10 or 11-9"),
    DOMINATING_WIN("Won set 11-0 to 11-3"),
    
    // MATCH EVENTS
    PERFECT_MATCH("Won match without losing a set"),
    MATCH_WINNER("Won the match"),
    FIRST_SET_WIN("Won first set"),
    
    // WIN STREAKS
    WIN_STREAK_3("Won 3 matches in a row"),
    WIN_STREAK_5("Won 5 matches in a row"),
    WIN_STREAK_10("Won 10 matches in a row"),
    
    // GENERIC
    GENERIC_WIN("Generic win message"),
    GENERIC_LOSS("Generic loss message");
    
    private final String description;
    
    QuipTrigger(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
