package com.dalegames.highlowjack;

import com.dalegames.highlowjack.engine.GameEngine;
import com.dalegames.highlowjack.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Command-line interface for High Low Jack card game.
 * 
 * <p>Allows four players (Dale, Kreep, Carryn, Primus) to play an interactive
 * game of High Low Jack from the terminal with exciting fanfares!</p>
 * 
 * @author Dale &amp; Primus
 * @version 2.0
 */
public class HighLowJackCLI {
    
    private static final String[] PLAYERS = {"Dale", "Kreep", "Carryn", "Primus"};
    private static final Scanner scanner = new Scanner(System.in);
    private static final int WINNING_SCORE = 11;  // First to 11!
    
    // Track current high/low in trump suit during play
    private static Card currentHigh = null;
    private static Card currentLow = null;
    
    /**
     * Main entry point for the CLI game.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        printWelcome();
        
        Game game = new Game(PLAYERS[0], PLAYERS[1], PLAYERS[2], PLAYERS[3]);
        
        boolean playAgain = true;
        
        while (playAgain && !game.isGameOver()) {
            playRound(game);
            
            if (!game.isGameOver()) {
                playAgain = askPlayAgain();
            }
        }
        
        if (game.isGameOver()) {
            announceWinner(game);
        }
        
        System.out.println("\nThanks for playing High Low Jack!");
        scanner.close();
    }
    
    /**
     * Prints welcome banner.
     */
    private static void printWelcome() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║       HIGH LOW JACK CARD GAME       ║");
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println();
        System.out.println("Players: " + String.join(", ", PLAYERS));
        System.out.println("First to " + WINNING_SCORE + " points wins!");
        System.out.println();
    }
    
    /**
     * Plays one complete round of the game.
     * 
     * @param game the game instance
     */
    private static void playRound(Game game) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("NEW ROUND");
        System.out.println("=".repeat(50));
        
        // Reset current high/low tracking
        currentHigh = null;
        currentLow = null;
        
        // Deal cards
        game.dealCards();
        
        System.out.println("\nCards dealt! Each player has 7 cards.");
        pause();
        
        // Play 7 tricks
        for (int trickNum = 1; trickNum <= 7; trickNum++) {
            playTrick(game, trickNum);
        }
        
        // Score the round
        scoreRound(game);
        
        // Show current scores
        showScores(game);
    }
    
    /**
     * Plays one trick (all 4 players play one card each).
     * 
     * @param game the game instance
     * @param trickNum the trick number (1-7)
     */
    private static void playTrick(Game game, int trickNum) {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("TRICK #" + trickNum);
        System.out.println("-".repeat(50));
        
        if (game.getTrumpSuit() != null) {
            System.out.println("Trump: " + game.getTrumpSuit().getSymbol());
        }
        
        // Each player plays a card
        for (int i = 0; i < 4; i++) {
            String currentPlayer = game.getCurrentPlayer();
            Hand hand = game.getHand(currentPlayer);
            
            System.out.println("\n" + currentPlayer + "'s turn");
            
            // Show current trick state
            if (game.getCurrentTrick() != null && game.getCurrentTrick().size() > 0) {
                System.out.println("\nCards played so far:");
                for (Trick.CardPlay play : game.getCurrentTrick().getPlays()) {
                    System.out.println("  " + play.playerName + ": " + play.card);
                }
            }
            
            // Show hand and get card choice
            Card chosenCard = getCardChoice(currentPlayer, hand, game);
            
            // Play the card
            game.playCard(chosenCard);
            System.out.println(currentPlayer + " plays: " + chosenCard);
            
            // Show trump if first card
            if (game.getTrumpSuit() != null && trickNum == 1 && i == 0) {
                System.out.println("\n*** TRUMP SUIT: " + game.getTrumpSuit().getSymbol() + " ***");
            }
            
            // Check for fanfare moments!
            checkForFanfare(chosenCard, currentPlayer, game);
            
            pause();
        }
        
        // Show trick winner
        List<Trick> tricks = game.getTricks();
        Trick completedTrick = tricks.get(tricks.size() - 1);
        String winner = completedTrick.getWinner();
        
        System.out.println("\n" + winner + " wins the trick!");
        pause();
    }
    
    /**
     * Checks if the played card deserves a fanfare announcement.
     * 
     * @param card the card just played
     * @param player the player who played it
     * @param game the game instance
     */
    private static void checkForFanfare(Card card, String player, Game game) {
        Card.Suit trump = game.getTrumpSuit();
        
        if (trump == null || card.getSuit() != trump) {
            return; // Only trump cards matter
        }
        
        // Check for JACK capture!
        if (card.getRank() == Card.Rank.JACK) {
            System.out.println("\n🎺🎺🎺 JACK OF TRUMPS! 🎺🎺🎺");
            System.out.println("💎 " + player + " plays the JACK! 💎");
            System.out.println("🏆 HUGE POINT POTENTIAL! 🏆");
        }
        // Check for ABSOLUTE HIGH (Ace of trump)
        else if (card.getRank() == Card.Rank.ACE) {
            System.out.println("\n🎉🎉 ABSOLUTE HIGH! 🎉🎉");
            System.out.println("👑 " + player + " plays the ACE of trump! 👑");
            currentHigh = card;
        }
        // Check for ABSOLUTE LOW (2 of trump)
        else if (card.getRank() == Card.Rank.TWO) {
            System.out.println("\n🎊🎊 ABSOLUTE LOW! 🎊🎊");
            System.out.println("🔽 " + player + " plays the 2 of trump! 🔽");
            currentLow = card;
        }
        // Check for current high/low
        else {
            boolean isNewHigh = false;
            boolean isNewLow = false;
            
            if (currentHigh == null || card.getRank().getValue() > currentHigh.getRank().getValue()) {
                currentHigh = card;
                isNewHigh = true;
            }
            
            if (currentLow == null || card.getRank().getValue() < currentLow.getRank().getValue()) {
                currentLow = card;
                isNewLow = true;
            }
            
            if (isNewHigh) {
                System.out.println("\n🎺 " + player + " holds CURRENT HIGH! 🎺");
            }
            if (isNewLow) {
                System.out.println("\n🎺 " + player + " holds CURRENT LOW! 🎺");
            }
        }
    }
    
    /**
     * Gets card choice from player (human or simulated).
     * 
     * @param playerName the player's name
     * @param hand the player's hand
     * @param game the game instance
     * @return the chosen card
     */
    private static Card getCardChoice(String playerName, Hand hand, Game game) {
        List<Card> cards = hand.getCards();
        
        // Dale gets interactive choice
        if (playerName.equals("Dale")) {
            return getHumanCardChoice(hand, game);
        }
        
        // Other players: simple AI (play first valid card)
        for (Card card : cards) {
            if (GameEngine.isValidPlay(game, playerName, card)) {
                return card;
            }
        }
        
        // Fallback (shouldn't happen)
        return cards.get(0);
    }
    
    /**
     * Gets interactive card choice from human player (Dale).
     * 
     * @param hand Dale's hand
     * @param game the game instance
     * @return the chosen card
     */
    private static Card getHumanCardChoice(Hand hand, Game game) {
        List<Card> cards = hand.getCards();
        
        System.out.println("\nYour hand:");
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            boolean valid = GameEngine.isValidPlay(game, "Dale", card);
            System.out.printf("  %d. %s%s\n", i + 1, card, valid ? "" : " (invalid)");
        }
        
        while (true) {
            System.out.print("\nChoose card (1-" + cards.size() + "): ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                if (choice < 1 || choice > cards.size()) {
                    System.out.println("Invalid choice. Please choose 1-" + cards.size());
                    continue;
                }
                
                Card chosen = cards.get(choice - 1);
                
                if (!GameEngine.isValidPlay(game, "Dale", chosen)) {
                    System.out.println("Invalid play! You must follow suit if possible.");
                    continue;
                }
                
                return chosen;
                
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    /**
     * Calculates game points for each player based on captured cards.
     *
     * @param game the game instance
     * @return map of player names to their game points
     */
    private static Map<String, Integer> calculateGamePoints(Game game) {
        Map<String, Integer> gamePoints = new HashMap<>();
        for (String player : PLAYERS) {
            gamePoints.put(player, 0);
        }

        for (Trick trick : game.getTricks()) {
            String winner = trick.getWinner();
            int points = 0;

            for (Trick.CardPlay play : trick.getPlays()) {
                Card card = play.card;
                // Count ALL cards, not just trump!
                switch (card.getRank()) {
                    case ACE:
                        points += 4;
                        break;
                    case KING:
                        points += 3;
                        break;
                    case QUEEN:
                        points += 2;
                        break;
                    case JACK:
                        points += 1;
                        break;
                    case TEN:
                        points += 10;
                        break;
                    default:
                        break;
                }
            }

            gamePoints.put(winner, gamePoints.get(winner) + points);
        }

        return gamePoints;
    }
    
    /**
     * Scores the completed round.
     * 
     * @param game the game instance
     */
    private static void scoreRound(Game game) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("SCORING ROUND");
        System.out.println("=".repeat(50));
        
        // Show Game points breakdown first
        Map<String, Integer> gamePoints = calculateGamePoints(game);
        System.out.println("\n📊 GAME POINTS (card values captured):");
        for (String player : PLAYERS) {
            System.out.printf("  %-10s: %2d points\n", player, gamePoints.get(player));
        }
        System.out.println();
        
        Map<String, String> results = GameEngine.calculateScores(game);
        
        System.out.println("🏆 ROUND SCORING:");
        
        if (results.containsKey("High")) {
            System.out.println("  ⬆️  High: " + results.get("High") + " (1 point)");
        }
        
        if (results.containsKey("Low")) {
            System.out.println("  ⬇️  Low: " + results.get("Low") + " (1 point)");
        }
        
        if (results.containsKey("Jack")) {
            System.out.println("  💎 Jack: " + results.get("Jack") + " (1 point)");
        } else {
            System.out.println("  💎 Jack: Not in play");
        }
        
        if (results.containsKey("Game")) {
            System.out.println("  🎯 Game: " + results.get("Game") + " (1 point) - won with " + 
                             gamePoints.get(results.get("Game")) + " card points");
        } else {
            System.out.println("  🎯 Game: Tie (no point awarded)");
        }
        
        pause();
    }
    
    /**
     * Shows current game scores.
     * 
     * @param game the game instance
     */
    private static void showScores(Game game) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("CURRENT SCORES (First to " + WINNING_SCORE + " wins!)");
        System.out.println("=".repeat(50));
        
        for (String player : PLAYERS) {
            int score = game.getScore(player);
            String bar = "█".repeat(score) + "░".repeat(WINNING_SCORE - score);
            System.out.printf("  %-10s: %2d [%s]\n", player, score, bar);
        }
        
        pause();
    }
    
    /**
     * Announces the game winner.
     * 
     * @param game the game instance
     */
    private static void announceWinner(Game game) {
        String winner = game.getWinner();
        
        System.out.println("\n" + "╔".repeat(50));
        System.out.println();
        System.out.println("        🎉🎉🎉 " + winner.toUpperCase() + " WINS THE GAME! 🎉🎉🎉");
        System.out.println("        🏆🏆🏆 CHAMPION OF HIGH LOW JACK! 🏆🏆🏆");
        System.out.println();
        System.out.println("╚".repeat(50));
        
        showScores(game);
    }
    
    /**
     * Asks if players want to play another round.
     * 
     * @return true if they want to continue
     */
    private static boolean askPlayAgain() {
        System.out.print("\nPlay another round? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.startsWith("y");
    }
    
    /**
     * Pauses for user to press Enter.
     */
    private static void pause() {
        System.out.print("\n[Press Enter to continue]");
        scanner.nextLine();
    }
}
