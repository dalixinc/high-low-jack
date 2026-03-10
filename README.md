# High Low Jack Card Game

A Java implementation of the classic "High Low Jack" (also known as "All Fours" or "Seven Up") card game.

## About the Game

High Low Jack is a trick-taking card game for 4 players. Players are dealt 7 cards each, and the first card played determines the trump suit. Points are scored for:

- **High**: Having the highest trump card in play
- **Low**: Having the lowest trump card in play
- **Jack**: Capturing the Jack of trumps (if in play)
- **Game**: Winning the most game points from captured cards

Card values for "Game" points:
- Ace = 4 points
- King = 3 points
- Queen = 2 points
- Jack = 1 point
- Ten = 10 points

First player to 7 points wins!

## Project Status

**Version**: 1.0.0-SNAPSHOT  
**Authors**: Dale & Primus  
**Purpose**: Family game for Dale, Kreep, Carryn, and friends

## Features

- ✅ Complete game engine with full rules implementation
- ✅ Comprehensive unit tests (JUnit 5)
- ✅ Integration tests for full game scenarios
- ✅ Full JavaDoc documentation
- ⏳ Web UI (planned)
- ⏳ Mobile UI (planned)
- ⏳ Desktop UI (planned)

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Building the Project

```bash
# Clone or download the project
cd high-low-jack

# Compile
mvn clean compile

# Run tests
mvn test

# Generate JavaDoc
mvn javadoc:javadoc
```

### Running a Test Game

```bash
# Compile the project
mvn clean package

# Run the game engine test
java -cp target/high-low-jack-1.0.0-SNAPSHOT.jar com.dalegames.highlowjack.GameEngineTest
```

## Project Structure

```
high-low-jack/
├── pom.xml                          # Maven configuration
├── README.md                        # This file
├── SETUP.md                         # Detailed setup instructions
└── src/
    ├── main/java/com/dalegames/highlowjack/
    │   ├── model/
    │   │   ├── Card.java           # Playing card
    │   │   ├── Deck.java           # 52-card deck
    │   │   ├── Hand.java           # Player's hand
    │   │   ├── Trick.java          # One round of 4 cards
    │   │   └── Game.java           # Complete game state
    │   └── engine/
    │       └── GameEngine.java     # Game rules & scoring
    └── test/java/com/dalegames/highlowjack/
        ├── model/
        │   ├── CardTest.java
        │   ├── DeckTest.java
        │   ├── HandTest.java
        │   ├── TrickTest.java
        │   └── GameTest.java
        ├── engine/
        │   └── GameEngineTest.java
        └── integration/
            └── FullGameTest.java    # End-to-end game tests
```

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CardTest

# Run with coverage (requires jacoco plugin)
mvn test jacoco:report
```

## Documentation

- **JavaDoc**: Run `mvn javadoc:javadoc` then open `target/site/apidocs/index.html`
- **Setup Guide**: See [SETUP.md](SETUP.md) for detailed installation and configuration
- **Game Rules**: See "About the Game" section above

## Development

This project follows professional Java development practices:

- **Test-Driven Development**: Comprehensive unit and integration tests
- **JavaDoc**: All public APIs fully documented
- **Maven**: Standard project structure and dependency management
- **Code Quality**: Clean code with proper error handling

## Future Enhancements

- [ ] Web-based multiplayer UI (Spring Boot + Thymeleaf)
- [ ] REST API for mobile clients
- [ ] WebSocket support for real-time gameplay
- [ ] Player statistics and game history
- [ ] AI opponents for solo play
- [ ] Tournament mode

## License

Private project for personal use.

## Authors

- **Dale** - Project lead, game design
- **Primus** - Technical implementation, testing

## Acknowledgments

- Built for family game nights with Dale, Kreep, Carryn, and friends
- Classic High Low Jack rules and traditions
