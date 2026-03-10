# High Low Jack - Part 2 Files

## What's in Part 2

This package contains the remaining core classes and tests to complete your High Low Jack game engine.

### Files Included:

1. **Game.java** - Complete game state management
2. **GameEngine.java** - Scoring logic (High, Low, Jack, Game)
3. **FullGameTest.java** - Integration test for complete game

### Additional Test Files (Create These):

You'll need to create these unit test files in your project:

**Test Directory Structure:**
```
src/test/java/com/dalegames/highlowjack/
├── model/
│   ├── CardTest.java
│   ├── DeckTest.java
│   ├── HandTest.java
│   ├── TrickTest.java
│   └── GameTest.java
├── engine/
│   └── GameEngineTest.java
└── integration/
    └── FullGameTest.java  ← Included in this package
```

## Installation Instructions

### Step 1: Copy Files to Correct Packages

**Game.java:**
- Copy to: `src/main/java/com/dalegames/highlowjack/model/`
- Package should be: `com.dalegames.highlowjack.model`

**GameEngine.java:**
- Copy to: `src/main/java/com/dalegames/highlowjack/engine/`
- Package should be: `com.dalegames.highlowjack.engine`

**FullGameTest.java:**
- Copy to: `src/test/java/com/dalegames/highlowjack/integration/`
- Package should be: `com.dalegames.highlowjack.integration`

### Step 2: Verify Package Declarations

After copying, each file should have the correct package declaration at the top:

**Game.java:**
```java
package com.dalegames.highlowjack.model;
```

**GameEngine.java:**
```java
package com.dalegames.highlowjack.engine;
```

**FullGameTest.java:**
```java
package com.dalegames.highlowjack.integration;
```

### Step 3: Compile

```bash
# In project root
mvn clean compile
```

**Expected output:**
```
[INFO] BUILD SUCCESS
```

### Step 4: Run Integration Test

```bash
# Run the full game test
mvn test -Dtest=FullGameTest

# Or run all tests
mvn test
```

**Expected output:**
```
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## What You Can Do Now

### 1. Test the Engine

```bash
# Compile and run tests
mvn clean test
```

### 2. Play a Game (Console)

Create a simple main method to test:

```java
package com.dalegames.highlowjack;

import com.dalegames.highlowjack.model.*;
import com.dalegames.highlowjack.engine.GameEngine;

public class TestGame {
    public static void main(String[] args) {
        // Create game
        Game game = new Game("Dale", "Kreep", "Carryn", "Player4");
        
        // Deal cards
        game.dealCards();
        
        System.out.println("Trump suit: " + game.getTrumpSuit());
        System.out.println("\nStarting hands:");
        
        for (String player : game.getPlayerNames()) {
            System.out.println(game.getHand(player));
        }
        
        // Play a few cards...
        // (See FullGameTest for examples)
    }
}
```

### 3. Generate JavaDoc

```bash
mvn javadoc:javadoc
```

Then open: `target/site/apidocs/index.html`

## Key Classes Overview

### Game.java

**Purpose:** Manages complete game state

**Key Methods:**
- `dealCards()` - Deal 7 cards to each player
- `playCard(Card)` - Play a card for current player
- `getCurrentPlayer()` - Get whose turn it is
- `getTrumpSuit()` - Get the trump suit
- `getState()` - Get current game state
- `addScore(player, points)` - Add points to player

**Game States:**
- `NOT_STARTED` - Game created, not dealt
- `IN_PROGRESS` - Cards dealt, playing tricks
- `ROUND_COMPLETE` - All 7 tricks played
- `GAME_OVER` - Someone reached 7 points

### GameEngine.java

**Purpose:** Scoring and validation logic

**Key Methods:**
- `calculateScores(Game)` - Calculate High, Low, Jack, Game
- `findHighTrump(...)` - Find highest trump player
- `findLowTrump(...)` - Find lowest trump player  
- `findJackWinner(...)` - Find who captured Jack
- `findGameWinner(...)` - Find most game points
- `getGamePoints(Card)` - Get card's game point value
- `isValidPlay(...)` - Validate a card play

**Scoring:**
- High: 1 point (highest trump)
- Low: 1 point (lowest trump)
- Jack: 1 point (if captured)
- Game: 1 point (most card points)

**Card Values:**
- Ace = 4 points
- King = 3 points
- Queen = 2 points
- Jack = 1 point
- Ten = 10 points

### FullGameTest.java

**Purpose:** Integration testing

**Tests:**
- Complete game round (deal → play → score)
- Trump suit determination
- Follow suit rules
- Game point calculation
- Winning conditions

## Project Status

### ✅ Complete (Part 1 + Part 2):

**Model Classes:**
- Card.java ✅
- Deck.java ✅
- Hand.java ✅
- Trick.java ✅
- Game.java ✅

**Engine:**
- GameEngine.java ✅

**Tests:**
- FullGameTest.java ✅

### ⏳ Still Needed:

**Unit Tests (Optional but Recommended):**
- CardTest.java
- DeckTest.java
- HandTest.java
- TrickTest.java
- GameTest.java
- GameEngineTest.java

**Future (Tomorrow and Beyond):**
- Web UI (Spring Boot controllers + Thymeleaf)
- REST API for multiplayer
- WebSocket for real-time play
- Game persistence (database)
- Player authentication
- Game history and statistics

## Troubleshooting

### Compilation Errors

**Problem:** "Cannot find symbol" errors

**Solution:** 
- Verify all files are in correct packages
- Run `mvn clean compile` (clean first)
- Check IntelliJ has indexed project (File → Invalidate Caches)

### Test Failures

**Problem:** FullGameTest fails

**Solution:**
- Ensure JUnit 5 dependency in pom.xml
- Run `mvn clean test`
- Check test output for specific error

### Package Issues

**Problem:** Wrong package declarations

**Solution:**
- Each file must have correct package at top
- Game.java → `package com.dalegames.highlowjack.model;`
- GameEngine.java → `package com.dalegames.highlowjack.engine;`
- FullGameTest.java → `package com.dalegames.highlowjack.integration;`

## Next Steps

1. **Copy Part 2 files** to correct packages
2. **Run `mvn clean compile`** to verify
3. **Run `mvn test`** to run integration test
4. **Review JavaDoc** (mvn javadoc:javadoc)
5. **Tomorrow: Build web UI!**

## Questions?

- Check JavaDoc documentation
- Review FullGameTest for usage examples
- See main README.md for game rules
- Tomorrow we'll build the web interface!

---

**You now have a complete, tested High Low Jack game engine!** 🃏

**Ready to play with Dale, Kreep, Carryn, and friends!** 🎮

---

**Authors:** Dale & Primus  
**Version:** 1.0.0-SNAPSHOT  
**Date:** March 2026
