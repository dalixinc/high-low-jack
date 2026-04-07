# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
mvn clean compile                 # Compile
mvn test                          # Run all tests
mvn test -Dtest=FullGameTest      # Run specific test class
mvn clean package                 # Build JAR
mvn spring-boot:run               # Run the app (requires PostgreSQL on localhost:5432)
mvn javadoc:javadoc               # Generate docs → target/site/apidocs/index.html
```

App runs on **port 8089** (`server.port=8089` in `application.properties`).

## Architecture

This is a **Java 21 / Spring Boot 3.2.1** web application for High Low Jack (a 4-player trick-taking card game). It has a full Thymeleaf MVC web UI, PostgreSQL persistence for player stats, in-memory multiplayer, and a personality/quip system.

### Package structure

- `com.dalegames.highlowjack.model` — game state classes (Card, Deck, Hand, Trick, Game, Match, etc.)
- `com.dalegames.highlowjack.engine` — `GameEngine` (static utility: scoring, rule validation)
- `com.dalegames.highlowjack.web` — Spring MVC controllers + `CardImageHelper`
- `com.dalegames.highlowjack.multiplayer` — `GameRegistry` + `MultiplayerGame` (in-memory, not DB-backed)
- `com.dalegames.highlowjack.service` — `QuipDetector`, `RealtimeQuipDetector` (personality system)
- `com.dalegames.highlowjack.persistence` — JPA entities, repositories, services (Player stats, quips, team stats)

### Model layer

| Class | Role |
|-------|------|
| `Card` | Immutable card with `Suit` and `Rank` enums |
| `Deck` | 52-card deck with shuffle/deal |
| `Hand` | A player's cards; enforces play/follow-suit |
| `Trick` | One 4-card round; determines winner by trump precedence |
| `Game` | Authoritative game state; owns the state machine |
| `GameSetup` | Configuration (GameMode: INDIVIDUAL/TEAM, match format) |
| `Match` | Tracks multi-set match progress (sets won per player/team) |
| `SetResult` | Set winner with tiebreaker logic |
| `MatchResult` | Match winner for final display |
| `RoundResult` | Per-round scoring breakdown |
| `GameEvent` | In-game event log entries (TWO_PITCHED, ACE_SPADES_PLAYED) |
| `QuipTrigger` | Enum of personality trigger conditions |
| `SimpleAI` | AI player logic |

### Game state machine

```
NOT_STARTED → IN_PROGRESS → [7 tricks] → ROUND_COMPLETE
                                              ↓
                                    check set winner (score ≥ 11)
                                       ↓              ↓
                                  SET_COMPLETE    new round
                                       ↓
                                  check match winner
                                   ↓           ↓
                               MATCH_OVER   new set
```

**Game flow:** `dealCards()` → `playCard()` × 28 (7 tricks × 4 players) → `ROUND_COMPLETE` → `GameEngine.calculateScores()` → `addScore()` → `SetResult.determineWinner()` → repeat until match is won.

**Scoring:** High (1 pt), Low (1 pt), Jack of trumps (1 pt, only if in play), Game (1 pt — most card-point value from captured tricks: Ten=10, Ace=4, King=3, Queen=2, Jack=1). Set won at 11 points.

### Web layer (`web/`)

**`HighLowJackController`** — main game controller; routes under `/highlowjack`:
- `GET /` → current game state or redirect to setup
- `POST /setup` → create `Game` + `Match`, store in HTTP session
- `POST /play` → validate + play card, auto-advance AI turns
- `GET /scoring` → round results display
- `POST /continue` → advance to next round/set/match
- `GET /stats` → player leaderboard

Game state is stored in the HTTP session under keys: `hlj_game`, `hlj_setup`, `hlj_match`, `hlj_roundResult`.

**`MultiplayerController`** — lobby/join flow; routes under `/highlowjack/multiplayer`:
- `POST /host` → creates game in `GameRegistry`, returns 6-char join code
- `POST /join` → player joins at a position (NORTH/SOUTH/EAST/WEST), gets UUID token
- `GET /lobby` → lobby waiting screen
- `GET /poll` → AJAX polling endpoint (returns JSON `{stateVersion, playerCount, gameStarted}`)
- `POST /start` → host starts game; unfilled positions auto-filled with AI

Multiplayer uses **HTTP polling**, not WebSockets. Games are in-memory only (`GameRegistry` uses `ConcurrentHashMap`; state is lost on restart). Each player is authenticated to their position by a UUID token stored in session as `mp_token`.

### Persistence layer (`persistence/`)

PostgreSQL database (`highlowjack` on localhost:5432, `ddl-auto=validate` — schema must exist).

**Entities:** `Player` (lifetime stats: matches, sets, rounds, point categories, streaks, favoriteSuit), `PersonalityQuip` (quip text keyed by trigger + player name), `TeamStats`.

**Services:** `PlayerService.getOrCreatePlayer()`, `updateMatchStats()`, `recordPoint(category)` — called by `HighLowJackController` at match end. `PersonalityService.getQuip(trigger, playerName)` — fetches from DB.

### Personality/Quip system (`service/`)

- `RealtimeQuipDetector` — reads `GameEvent` list mid-round; triggers on `TWO_PITCHED` and `ACE_SPADES_PLAYED`
- `QuipDetector` — called at round/set/match completion; retrieves quip text from `PersonalityService`
- Quip display rendered via `quip_display_component.html` fragment

### Templates (`resources/templates/highlowjack/`)

`setup.html` → `game.html` → `scoring.html` → `set-winner.html` → `match-winner.html`

Also: `multiplayer-join.html`, `multiplayer-lobby.html`, `stats.html`, `quip_display_component.html`.

Card images served from `resources/static/images/cards/` (52 cards + backs in multiple colors).

### Test layer

Only integration tests exist (`src/test/java/.../integration/FullGameTest`). Unit tests are planned but not written.
