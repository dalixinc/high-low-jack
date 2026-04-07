# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
mvn clean compile          # Compile
mvn test                   # Run all tests
mvn test -Dtest=FullGameTest  # Run specific test class
mvn clean package          # Build JAR
mvn javadoc:javadoc        # Generate docs → target/site/apidocs/index.html
```

## Architecture

This is a Java 17 / Maven card game engine for High Low Jack (a 4-player trick-taking game, first to 7 points wins). No UI yet — logic is tested via JUnit 5.

### Package structure

- `com.dalegames.highlowjack.model` — game state classes
- `com.dalegames.highlowjack.engine` — scoring and rule validation

### Model layer (`model/`)

| Class | Role |
|-------|------|
| `Card` | Immutable card with `Suit` and `Rank` enums |
| `Deck` | 52-card deck with shuffle/deal |
| `Hand` | A player's cards; enforces play/follow-suit |
| `Trick` | One 4-card round; determines winner by trump precedence |
| `Game` | Authoritative game state; owns the state machine |

**Game state machine:** `NOT_STARTED → IN_PROGRESS → ROUND_COMPLETE → GAME_OVER`

**Game flow:** `dealCards()` → `playCard()` × 28 (7 tricks × 4 players) → state becomes `ROUND_COMPLETE` → call `GameEngine.calculateScores()` → `addScore()` → repeat until a player reaches 7 points.

### Engine layer (`engine/`)

`GameEngine` is a static utility class responsible for:
- `calculateScores(Game)` — awards High/Low/Jack/Game points after a round
- `isValidPlay(Game, playerName, Card)` — enforces follow-suit rules
- Helper finders for High, Low, Jack, and Game-point winner among completed tricks

**Scoring:** High (1 pt), Low (1 pt), Jack of trumps (1 pt, only if in play), Game (1 pt — most card-point value from captured tricks: Ten=10, Ace=4, King=3, Queen=2, Jack=1).

### Test layer

Only integration tests exist so far (`integration/FullGameTest`). Unit test stubs are planned but not yet written.
