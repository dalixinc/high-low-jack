# High Low Jack - Setup Guide

Complete setup instructions for the High Low Jack card game project.

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 17 or higher**
   - Download from: https://adoptium.net/
   - Verify installation: `java -version`
   - Should show version 17 or higher

2. **Apache Maven 3.6 or higher**
   - Download from: https://maven.apache.org/download.cgi
   - Verify installation: `mvn -version`
   - Should show Maven 3.6+ and Java 17+

3. **IDE (Recommended)**
   - IntelliJ IDEA Community Edition (recommended)
   - Eclipse IDE for Java Developers
   - VS Code with Java extensions

## Installation Steps

### Step 1: Extract Project

```bash
# Extract the ZIP file
unzip high-low-jack-complete.zip

# Navigate to project directory
cd high-low-jack
```

### Step 2: Verify Project Structure

```bash
# List project contents
ls -la

# You should see:
# - pom.xml
# - README.md
# - SETUP.md (this file)
# - src/ directory
```

### Step 3: Build the Project

```bash
# Clean and compile
mvn clean compile

# Expected output: BUILD SUCCESS
```

### Step 4: Run Tests

```bash
# Run all tests
mvn test

# Expected output:
# - All tests pass
# - BUILD SUCCESS
```

## IDE Setup

### IntelliJ IDEA

1. **Import Project**
   - File → Open
   - Select the `high-low-jack` directory
   - Choose "Maven" project
   - Wait for indexing to complete

2. **Verify JDK**
   - File → Project Structure → Project
   - Ensure SDK is set to Java 17 or higher

3. **Run Tests**
   - Right-click on `src/test/java`
   - Select "Run 'All Tests'"

4. **View JavaDoc**
   - Tools → Generate JavaDoc
   - Or run: `mvn javadoc:javadoc`
   - Open: `target/site/apidocs/index.html`

### Eclipse

1. **Import Project**
   - File → Import → Maven → Existing Maven Projects
   - Select the `high-low-jack` directory
   - Click Finish

2. **Verify JDK**
   - Right-click project → Properties → Java Build Path
   - Ensure JRE System Library is Java 17+

3. **Run Tests**
   - Right-click project → Run As → JUnit Test

## Running the Game

### Command Line

```bash
# Compile and package
mvn clean package

# Run the demo
java -cp target/high-low-jack-1.0.0-SNAPSHOT.jar \
  com.dalegames.highlowjack.GameEngineTest
```

### IntelliJ IDEA

1. Navigate to `src/test/java/com/dalegames/highlowjack/integration/FullGameTest.java`
2. Right-click the file
3. Select "Run 'FullGameTest'"

## Project Commands Reference

```bash
# Clean build artifacts
mvn clean

# Compile source code
mvn compile

# Compile and run all tests
mvn test

# Package as JAR
mvn package

# Generate JavaDoc
mvn javadoc:javadoc

# Run specific test
mvn test -Dtest=CardTest

# Skip tests during build
mvn package -DskipTests
```

## Troubleshooting

### "mvn: command not found"

Maven is not installed or not in PATH.

**Solution**:
1. Download Maven from https://maven.apache.org/
2. Add Maven's `bin` directory to your PATH
3. Restart terminal and verify: `mvn -version`

### "Invalid target release: 17"

Wrong Java version.

**Solution**:
1. Install JDK 17 or higher
2. Set JAVA_HOME environment variable
3. Verify: `java -version` shows 17+

### Tests Failing

**Solution**:
1. Ensure you have the latest code
2. Run `mvn clean test` (clean first)
3. Check test output for specific errors

### IDE Not Recognizing Maven Project

**Solution**:
1. Ensure pom.xml is in project root
2. In IntelliJ: Right-click pom.xml → Maven → Reload Project
3. In Eclipse: Right-click project → Maven → Update Project

## Next Steps

After successful setup:

1. **Explore the Code**
   - Review JavaDoc: `target/site/apidocs/index.html`
   - Read through model classes in `src/main/java/com/dalegames/highlowjack/model/`

2. **Run Tests**
   - Unit tests: `mvn test`
   - Review test coverage

3. **Experiment**
   - Modify `FullGameTest.java` to simulate different games
   - Add your own test scenarios

4. **Start Development**
   - Ready to build the web UI!
   - Ready to add multiplayer features!

## Support

For issues or questions:
- Check JavaDoc documentation
- Review test cases for usage examples
- Consult README.md for project overview

## Development Environment

Recommended setup for active development:

- **IDE**: IntelliJ IDEA (hot reload, excellent Maven support)
- **JDK**: Java 17 LTS (long-term support)
- **Build Tool**: Maven (for dependency management)
- **Testing**: JUnit 5 (modern testing framework)

Happy coding! 🃏
