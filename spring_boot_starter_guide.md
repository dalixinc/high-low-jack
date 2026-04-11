# 🍃 Spring Boot Starter Guide

**From Zero to Production-Ready Web Applications**

**Author:** Primus & Dale  
**Version:** 1.0.0  
**Date:** April 9, 2026  
**Based on:** Real-world experience building High Low Jack & RTA Portal

---

## 📋 Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Core Principles](#core-principles)
4. [Project Structure](#project-structure)
5. [Essential Dependencies](#essential-dependencies)
6. [Database with JPA & Hibernate](#database-with-jpa--hibernate)
7. [Web Layer with Thymeleaf](#web-layer-with-thymeleaf)
8. [Controllers & Routing](#controllers--routing)
9. [Services & Business Logic](#services--business-logic)
10. [Dev Tools & Hot Reload](#dev-tools--hot-reload)
11. [Pagination & Sorting](#pagination--sorting)
12. [Sessions & State Management](#sessions--state-management)
13. [Error Handling](#error-handling)
14. [Testing](#testing)
15. [Deployment](#deployment)
16. [Groovy Tricks We Learned](#groovy-tricks-we-learned)
17. [Common Pitfalls](#common-pitfalls)
18. [Best Practices](#best-practices)

---

## Introduction

### What is Spring Boot?

Spring Boot is a **convention-over-configuration** framework that makes it easy to create stand-alone, production-grade Spring-based applications. It takes an opinionated view of the Spring platform so you can get started with minimum fuss.

### Why Spring Boot?

✅ **Rapid Development** - Get running in minutes  
✅ **Production-Ready** - Built-in health checks, metrics, monitoring  
✅ **Auto-Configuration** - Intelligent defaults that just work  
✅ **Embedded Server** - No need to deploy WARs  
✅ **Vast Ecosystem** - Huge community and library support  
✅ **Battle-Tested** - Used by millions of applications worldwide

### What We Built

- **High Low Jack** - Multiplayer card game with WebSockets, sessions, PostgreSQL
- **RTA Portal** - User management, authentication, activity logging

Both projects taught us the real-world patterns you'll see in this guide.

---

## Getting Started

### Prerequisites

```bash
# Java 17 or higher
java -version

# Maven (or Gradle)
mvn -version

# IDE (IntelliJ IDEA recommended, but Eclipse/VS Code work too)

# PostgreSQL (or H2 for development)
psql --version
```

### Create Your First Spring Boot App

**Method 1: Spring Initializr (Easiest)**

1. Go to https://start.spring.io
2. Configure:
   - **Project:** Maven
   - **Language:** Java
   - **Spring Boot:** 3.2.x (latest stable)
   - **Group:** com.yourcompany
   - **Artifact:** myapp
   - **Package name:** com.yourcompany.myapp
   - **Packaging:** Jar
   - **Java:** 21

3. Add Dependencies:
   - Spring Web
   - Spring Data JPA
   - PostgreSQL Driver
   - Thymeleaf
   - Spring Boot DevTools

4. Click **Generate** and download the ZIP

**Method 2: Command Line**

```bash
# Using Spring CLI
spring init --dependencies=web,data-jpa,postgresql,thymeleaf,devtools myapp

cd myapp
mvn spring-boot:run
```

**Method 3: IntelliJ IDEA**

1. File → New → Project
2. Select Spring Initializr
3. Configure as above
4. Click Finish

### Your First Application

```java
package com.yourcompany.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main application entry point.
 * 
 * @SpringBootApplication combines:
 * - @Configuration: Marks this as source of bean definitions
 * - @EnableAutoConfiguration: Enables Spring Boot's auto-configuration
 * - @ComponentScan: Scans for components in this package and below
 * 
 * @author Dale
 * @version 1.0.0
 */
@SpringBootApplication
public class MyAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyAppApplication.class, args);
    }
}

/**
 * Simple REST controller to verify app is running.
 */
@RestController
class HelloController {
    
    @GetMapping("/")
    public String hello() {
        return "Hello, Spring Boot!";
    }
}
```

**Run it:**

```bash
mvn spring-boot:run

# Visit: http://localhost:8080
# You should see: "Hello, Spring Boot!"
```

🎉 **Congratulations! You just built your first Spring Boot app!**

---

## Core Principles

### 1. Convention Over Configuration

Spring Boot makes **intelligent assumptions** so you write less boilerplate.

**Traditional Spring:**
```xml
<!-- Hundreds of lines of XML configuration -->
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
    <property name="driverClassName" value="org.postgresql.Driver"/>
    <property name="url" value="jdbc:postgresql://localhost:5432/mydb"/>
    <!-- ... more configuration ... -->
</bean>
```

**Spring Boot:**
```properties
# application.properties - that's it!
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=user
spring.datasource.password=pass
```

### 2. Auto-Configuration

Spring Boot **automatically configures** beans based on classpath dependencies.

```java
// PostgreSQL driver on classpath?
// → Spring Boot auto-configures DataSource, JPA, TransactionManager

// Thymeleaf on classpath?
// → Spring Boot auto-configures ViewResolver, TemplateEngine

// You just use them!
@Autowired
private DataSource dataSource;  // Ready to use!
```

### 3. Standalone Applications

Spring Boot apps are **self-contained** with embedded servers.

```bash
# Build executable JAR
mvn clean package

# Run anywhere with Java installed
java -jar target/myapp-1.0.0.jar

# No Tomcat/JBoss/WebLogic installation needed!
```

### 4. Production-Ready Features

Built-in support for:
- Health checks (`/actuator/health`)
- Metrics (`/actuator/metrics`)
- Application info (`/actuator/info`)
- Configuration externalization
- Logging

### 5. Dependency Management

Spring Boot manages dependency versions for you.

```xml
<!-- You specify version once in parent -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.1</version>
</parent>

<!-- Then no versions needed for Spring dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <!-- No version! Parent manages it -->
</dependency>
```

---

## Project Structure

### Recommended Layout

```
myapp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/yourcompany/myapp/
│   │   │       ├── MyAppApplication.java       # Main entry point
│   │   │       ├── config/                     # Configuration classes
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── WebConfig.java
│   │   │       ├── model/                      # Domain models (POJOs)
│   │   │       │   ├── User.java
│   │   │       │   └── Game.java
│   │   │       ├── persistence/                # Database layer
│   │   │       │   ├── entity/                 # JPA entities
│   │   │       │   │   ├── Player.java
│   │   │       │   │   └── Match.java
│   │   │       │   └── repository/             # Spring Data repositories
│   │   │       │       ├── PlayerRepository.java
│   │   │       │       └── MatchRepository.java
│   │   │       ├── service/                    # Business logic
│   │   │       │   ├── GameService.java
│   │   │       │   └── UserService.java
│   │   │       ├── web/                        # Web layer
│   │   │       │   ├── controller/             # Controllers
│   │   │       │   │   ├── HomeController.java
│   │   │       │   │   └── GameController.java
│   │   │       │   ├── dto/                    # Data Transfer Objects
│   │   │       │   │   └── PlayerDTO.java
│   │   │       │   └── validator/              # Custom validators
│   │   │       │       └── PlayerValidator.java
│   │   │       └── util/                       # Utilities
│   │   │           └── DateUtil.java
│   │   └── resources/
│   │       ├── application.properties          # Main config
│   │       ├── application-dev.properties      # Dev profile
│   │       ├── application-prod.properties     # Production profile
│   │       ├── static/                         # Static resources
│   │       │   ├── css/
│   │       │   │   └── style.css
│   │       │   ├── js/
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       │       └── logo.png
│   │       └── templates/                      # Thymeleaf templates
│   │           ├── index.html
│   │           ├── game.html
│   │           └── fragments/
│   │               ├── header.html
│   │               └── footer.html
│   └── test/
│       └── java/
│           └── com/yourcompany/myapp/
│               ├── service/
│               │   └── GameServiceTest.java
│               └── web/
│                   └── controller/
│                       └── GameControllerTest.java
├── pom.xml                                     # Maven configuration
└── README.md
```

### Package Organization Principles

**By Layer (Recommended for small-medium apps):**
```
- model/          # Domain objects
- persistence/    # Database
- service/        # Business logic
- web/            # Controllers, DTOs
```

**By Feature (Recommended for large apps):**
```
- user/
  - UserController.java
  - UserService.java
  - User.java
  - UserRepository.java
- game/
  - GameController.java
  - GameService.java
  - Game.java
  - GameRepository.java
```

We used **layer-based** for High Low Jack and it worked great.

---

## Essential Dependencies

### Core Starters

```xml
<dependencies>
    <!-- Web applications with REST and MVC -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Database access with JPA/Hibernate -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Thymeleaf templating engine -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    
    <!-- Hot reload during development -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
    
    <!-- PostgreSQL driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Other Useful Starters

```xml
<!-- Security (authentication, authorization) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- WebSocket support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Actuator (health checks, metrics) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Caching -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

---

## Database with JPA & Hibernate

### Configuration

**application.properties:**

```properties
# PostgreSQL connection
spring.datasource.url=jdbc:postgresql://localhost:5432/myapp
spring.datasource.username=myuser
spring.datasource.password=mypassword

# JPA/Hibernate settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Connection pool (HikariCP is default and fast)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

**Understanding `ddl-auto` values:**

- `none` - Do nothing (production default)
- `validate` - Validate schema, make no changes
- `update` - Update schema (dev/test)
- `create` - Create schema, drop existing data
- `create-drop` - Create on start, drop on exit

🎯 **Use `update` for development, `none` or `validate` for production!**

### Creating an Entity

```java
package com.yourcompany.myapp.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Player entity representing a game player.
 * 
 * @author Dale
 * @version 1.0.0
 */
@Entity
@Table(name = "players")
public class Player {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(name = "email", unique = true, length = 100)
    private String email;
    
    @Column(name = "matches_played")
    private Integer matchesPlayed = 0;
    
    @Column(name = "matches_won")
    private Integer matchesWon = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Player() {}
    
    public Player(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Integer getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(Integer matchesPlayed) { 
        this.matchesPlayed = matchesPlayed; 
    }
    
    public Integer getMatchesWon() { return matchesWon; }
    public void setMatchesWon(Integer matchesWon) { 
        this.matchesWon = matchesWon; 
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public String toString() {
        return "Player{id=" + id + ", name='" + name + "'}";
    }
}
```

### Creating a Repository

```java
package com.yourcompany.myapp.persistence.repository;

import com.yourcompany.myapp.persistence.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Player entity.
 * 
 * Spring Data JPA auto-implements this interface!
 * 
 * @author Dale
 * @version 1.0.0
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    // Spring Data JPA derives query from method name!
    Optional<Player> findByName(String name);
    
    List<Player> findByEmail(String email);
    
    // Multiple conditions
    List<Player> findByNameAndEmail(String name, String email);
    
    // Comparisons
    List<Player> findByMatchesWonGreaterThan(Integer wins);
    
    // Ordering
    List<Player> findByOrderByMatchesWonDesc();
    
    // Pagination
    Page<Player> findAll(Pageable pageable);
    
    // Custom JPQL query
    @Query("SELECT p FROM Player p WHERE p.matchesWon > :minWins ORDER BY p.matchesWon DESC")
    List<Player> findTopPlayers(@Param("minWins") Integer minWins);
    
    // Native SQL query
    @Query(value = "SELECT * FROM players WHERE matches_won / NULLIF(matches_played, 0) > 0.5", 
           nativeQuery = true)
    List<Player> findPlayersWithAbove50PercentWinRate();
    
    // Exists query
    boolean existsByName(String name);
    
    // Count query
    long countByMatchesWonGreaterThan(Integer wins);
}
```

🎯 **Spring Data JPA auto-generates implementation at runtime!**

### Query Method Keywords

Spring Data JPA understands these keywords in method names:

| Keyword | Example | JPQL Snippet |
|---------|---------|--------------|
| `findBy` | `findByName` | `... where x.name = ?1` |
| `And` | `findByNameAndEmail` | `... where x.name = ?1 and x.email = ?2` |
| `Or` | `findByNameOrEmail` | `... where x.name = ?1 or x.email = ?2` |
| `Between` | `findByScoreBetween` | `... where x.score between ?1 and ?2` |
| `LessThan` | `findByScoreLessThan` | `... where x.score < ?1` |
| `GreaterThan` | `findByScoreGreaterThan` | `... where x.score > ?1` |
| `Like` | `findByNameLike` | `... where x.name like ?1` |
| `StartingWith` | `findByNameStartingWith` | `... where x.name like ?1` (param + '%') |
| `EndingWith` | `findByNameEndingWith` | `... where x.name like ?1` ('%' + param) |
| `Containing` | `findByNameContaining` | `... where x.name like ?1` ('%' + param + '%') |
| `OrderBy` | `findByOrderByNameAsc` | `... order by x.name asc` |
| `Not` | `findByNameNot` | `... where x.name <> ?1` |
| `IsNull` | `findByEmailIsNull` | `... where x.email is null` |
| `IsNotNull` | `findByEmailIsNotNull` | `... where x.email is not null` |
| `True` | `findByActiveTrue` | `... where x.active = true` |
| `False` | `findByActiveFalse` | `... where x.active = false` |

---

## Web Layer with Thymeleaf

### Why Thymeleaf?

- ✅ **Natural templating** - Templates are valid HTML
- ✅ **Server-side rendering** - No client-side JS required
- ✅ **Spring integration** - Works seamlessly with Spring MVC
- ✅ **Fragment reuse** - DRY principle for layouts

### Basic Template

**templates/index.html:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${pageTitle}">Default Title</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <h1 th:text="${message}">Default Message</h1>
    
    <!-- Conditional rendering -->
    <div th:if="${user != null}">
        <p>Welcome, <span th:text="${user.name}">Guest</span>!</p>
    </div>
    
    <div th:unless="${user != null}">
        <p>Please log in.</p>
    </div>
    
    <!-- Loops -->
    <ul>
        <li th:each="player : ${players}" 
            th:text="${player.name}">Player Name</li>
    </ul>
    
    <!-- Forms -->
    <form th:action="@{/submit}" th:object="${player}" method="post">
        <input type="text" th:field="*{name}" placeholder="Name">
        <input type="email" th:field="*{email}" placeholder="Email">
        <button type="submit">Submit</button>
    </form>
    
    <script th:src="@{/js/app.js}"></script>
</body>
</html>
```

### Thymeleaf Syntax Cheat Sheet

```html
<!-- Variable expressions -->
<p th:text="${user.name}">Name</p>

<!-- URL expressions -->
<a th:href="@{/games/{id}(id=${game.id})}">Game Link</a>
<link th:href="@{/css/style.css}">

<!-- Selection expressions (with th:object) -->
<form th:object="${player}">
    <input th:field="*{name}">  <!-- Same as ${player.name} -->
</form>

<!-- Message expressions (i18n) -->
<p th:text="#{welcome.message}">Welcome</p>

<!-- Conditionals -->
<div th:if="${score > 10}">High score!</div>
<div th:unless="${score > 10}">Keep trying!</div>

<!-- Switch -->
<div th:switch="${user.role}">
    <p th:case="'admin'">Admin Panel</p>
    <p th:case="'user'">User Dashboard</p>
    <p th:case="*">Guest View</p>
</div>

<!-- Loops -->
<tr th:each="player, iterStat : ${players}">
    <td th:text="${iterStat.count}">1</td>
    <td th:text="${player.name}">Name</td>
    <td th:text="${iterStat.even} ? 'Even' : 'Odd'">Even</td>
</tr>

<!-- Inline expressions -->
<p>Hello, [[${user.name}]]!</p>

<!-- Class manipulation -->
<div th:classappend="${active} ? 'active' : ''">Content</div>

<!-- Attribute manipulation -->
<input th:attr="data-player-id=${player.id}">
<input th:data-player-id="${player.id}">  <!-- Cleaner syntax -->
```

### Layout Fragments (DRY Templates)

**templates/fragments/header.html:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <header th:fragment="header">
        <nav>
            <a th:href="@{/}">Home</a>
            <a th:href="@{/games}">Games</a>
            <a th:href="@{/players}">Players</a>
        </nav>
    </header>
</body>
</html>
```

**templates/fragments/footer.html:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <footer th:fragment="footer">
        <p>&copy; 2026 My Game App</p>
    </footer>
</body>
</html>
```

**templates/game.html (using fragments):**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Game Page</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <!-- Include header fragment -->
    <div th:replace="~{fragments/header :: header}"></div>
    
    <main>
        <h1>Game Content</h1>
        <!-- Your game content here -->
    </main>
    
    <!-- Include footer fragment -->
    <div th:replace="~{fragments/footer :: footer}"></div>
</body>
</html>
```

🎯 **`th:replace` vs `th:insert`:**
- `th:replace` - Replaces the div with the fragment
- `th:insert` - Inserts fragment inside the div

---

## Controllers & Routing

### Basic Controller

```java
package com.yourcompany.myapp.web.controller;

import com.yourcompany.myapp.persistence.entity.Player;
import com.yourcompany.myapp.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for player-related pages.
 * 
 * @author Dale
 * @version 1.0.0
 */
@Controller
@RequestMapping("/players")
public class PlayerController {
    
    private final PlayerService playerService;
    
    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }
    
    /**
     * List all players.
     * GET /players
     */
    @GetMapping
    public String listPlayers(Model model) {
        List<Player> players = playerService.getAllPlayers();
        model.addAttribute("players", players);
        model.addAttribute("pageTitle", "All Players");
        return "players/list";  // → templates/players/list.html
    }
    
    /**
     * Show single player.
     * GET /players/5
     */
    @GetMapping("/{id}")
    public String showPlayer(@PathVariable Long id, Model model) {
        Player player = playerService.getPlayerById(id);
        model.addAttribute("player", player);
        return "players/show";
    }
    
    /**
     * Show create player form.
     * GET /players/new
     */
    @GetMapping("/new")
    public String newPlayerForm(Model model) {
        model.addAttribute("player", new Player());
        return "players/form";
    }
    
    /**
     * Create new player.
     * POST /players
     */
    @PostMapping
    public String createPlayer(@ModelAttribute Player player) {
        playerService.savePlayer(player);
        return "redirect:/players";
    }
    
    /**
     * Show edit player form.
     * GET /players/5/edit
     */
    @GetMapping("/{id}/edit")
    public String editPlayerForm(@PathVariable Long id, Model model) {
        Player player = playerService.getPlayerById(id);
        model.addAttribute("player", player);
        return "players/form";
    }
    
    /**
     * Update existing player.
     * POST /players/5
     */
    @PostMapping("/{id}")
    public String updatePlayer(@PathVariable Long id, 
                               @ModelAttribute Player player) {
        player.setId(id);
        playerService.savePlayer(player);
        return "redirect:/players/" + id;
    }
    
    /**
     * Delete player.
     * POST /players/5/delete
     */
    @PostMapping("/{id}/delete")
    public String deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return "redirect:/players";
    }
}
```

### REST Controller

```java
package com.yourcompany.myapp.web.controller;

import com.yourcompany.myapp.persistence.entity.Player;
import com.yourcompany.myapp.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for players.
 * Returns JSON, not HTML.
 * 
 * @author Dale
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/players")
public class PlayerApiController {
    
    @Autowired
    private PlayerService playerService;
    
    /**
     * GET /api/players
     * Returns: [{"id":1,"name":"Dale"}, ...]
     */
    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }
    
    /**
     * GET /api/players/5
     * Returns: {"id":5,"name":"Kreep"}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable Long id) {
        Player player = playerService.getPlayerById(id);
        if (player == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(player);
    }
    
    /**
     * POST /api/players
     * Body: {"name":"Preezbob","email":"pb@example.com"}
     * Returns: Created player with 201 status
     */
    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        Player saved = playerService.savePlayer(player);
        return ResponseEntity.status(201).body(saved);
    }
    
    /**
     * PUT /api/players/5
     * Body: {"name":"Updated Name"}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, 
                                               @RequestBody Player player) {
        player.setId(id);
        Player updated = playerService.savePlayer(player);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * DELETE /api/players/5
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Request Mapping Annotations

```java
// Method-level annotations
@GetMapping("/path")       // GET requests
@PostMapping("/path")      // POST requests
@PutMapping("/path")       // PUT requests
@DeleteMapping("/path")    // DELETE requests
@PatchMapping("/path")     // PATCH requests

// Path variables
@GetMapping("/players/{id}")
public String show(@PathVariable Long id) { ... }

// Request parameters
@GetMapping("/search")
public String search(@RequestParam String query) { ... }
// → /search?query=Kreep

// Optional parameters with defaults
@GetMapping("/search")
public String search(@RequestParam(defaultValue = "10") int limit) { ... }

// Multiple path variables
@GetMapping("/games/{gameId}/players/{playerId}")
public String show(@PathVariable Long gameId, 
                   @PathVariable Long playerId) { ... }
```

---

## Services & Business Logic

### Creating a Service

```java
package com.yourcompany.myapp.service;

import com.yourcompany.myapp.persistence.entity.Player;
import com.yourcompany.myapp.persistence.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for player business logic.
 * 
 * Services sit between controllers and repositories.
 * They contain business logic, transactions, and orchestration.
 * 
 * @author Dale
 * @version 1.0.0
 */
@Service
@Transactional  // All methods run in transactions
public class PlayerService {
    
    private final PlayerRepository playerRepository;
    
    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
    
    /**
     * Get all players.
     */
    @Transactional(readOnly = true)  // Optimization for read-only
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
    
    /**
     * Get player by ID.
     * 
     * @throws IllegalArgumentException if player not found
     */
    @Transactional(readOnly = true)
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Player not found: " + id));
    }
    
    /**
     * Save player (create or update).
     */
    public Player savePlayer(Player player) {
        // Business logic here
        validatePlayer(player);
        return playerRepository.save(player);
    }
    
    /**
     * Delete player.
     */
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new IllegalArgumentException("Player not found: " + id);
        }
        playerRepository.deleteById(id);
    }
    
    /**
     * Increment win count.
     * This is business logic that belongs in service, not controller!
     */
    public void recordWin(Long playerId) {
        Player player = getPlayerById(playerId);
        player.setMatchesPlayed(player.getMatchesPlayed() + 1);
        player.setMatchesWon(player.getMatchesWon() + 1);
        playerRepository.save(player);
    }
    
    /**
     * Validate player data.
     */
    private void validatePlayer(Player player) {
        if (player.getName() == null || player.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be empty");
        }
        // More validation...
    }
}
```

### Why Services?

✅ **Separation of Concerns** - Controllers handle HTTP, Services handle business logic  
✅ **Reusability** - Services can be called from multiple controllers  
✅ **Testability** - Easy to unit test business logic  
✅ **Transactions** - Control transaction boundaries  
✅ **Security** - Apply security at service layer  

---

## Dev Tools & Hot Reload

### Setup Spring Boot DevTools

**Already in dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### What DevTools Does

✅ **Auto-restart** - Restarts app when files change  
✅ **LiveReload** - Auto-refreshes browser  
✅ **Template cache disabled** - See Thymeleaf changes immediately  
✅ **Property defaults** - Dev-friendly defaults  

### How to Use

**Just run your app:**
```bash
mvn spring-boot:run
```

**Make a change to:**
- Java file → Auto-restart (fast!)
- Template file → No restart, instant reload
- Static files → Instant reload

### Configure DevTools

**application-dev.properties:**
```properties
# Disable auto-restart if annoying
spring.devtools.restart.enabled=true

# Exclude certain paths from triggering restart
spring.devtools.restart.exclude=static/**,public/**

# LiveReload
spring.devtools.livereload.enabled=true

# Template caching (disabled in dev by default)
spring.thymeleaf.cache=false
```

### IntelliJ IDEA Setup

For best DevTools experience in IntelliJ:

1. **File → Settings → Build → Compiler**
   - ✅ Check "Build project automatically"

2. **Help → Find Action** (Ctrl+Shift+A)
   - Search: "Registry"
   - ✅ Check "compiler.automake.allow.when.app.running"

3. Now code changes trigger automatic rebuild + restart!

### 🎯 GROOVY TRICK: Instant Template Reload

```properties
# application-dev.properties
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=file:src/main/resources/templates/
```

Now edit templates and **just refresh browser** - no restart needed!

---

## Pagination & Sorting

### Enable Pagination in Repository

```java
package com.yourcompany.myapp.persistence.repository;

import com.yourcompany.myapp.persistence.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    // Spring Data JPA provides this automatically!
    Page<Player> findAll(Pageable pageable);
    
    // Works with custom queries too
    Page<Player> findByNameContaining(String name, Pageable pageable);
}
```

### Controller with Pagination

```java
package com.yourcompany.myapp.web.controller;

import com.yourcompany.myapp.persistence.entity.Player;
import com.yourcompany.myapp.persistence.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller demonstrating pagination and sorting.
 * 
 * @author Dale
 * @version 1.0.0
 */
@Controller
public class PlayerListController {
    
    @Autowired
    private PlayerRepository playerRepository;
    
    /**
     * List players with pagination and sorting.
     * 
     * URL examples:
     * - /players?page=0&size=10
     * - /players?page=1&size=20&sort=name,asc
     * - /players?page=0&sort=matchesWon,desc
     */
    @GetMapping("/players")
    public String listPlayers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {
        
        // Create Sort object
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
        
        // Create Pageable object
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get page of results
        Page<Player> playerPage = playerRepository.findAll(pageable);
        
        // Add to model
        model.addAttribute("players", playerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", playerPage.getTotalPages());
        model.addAttribute("totalItems", playerPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        return "players/list";
    }
}
```

### Thymeleaf Template with Pagination

**templates/players/list.html:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Players</title>
    <style>
        .sortable { cursor: pointer; color: blue; text-decoration: underline; }
        .current { font-weight: bold; }
        .pagination { margin: 20px 0; }
        .pagination a { margin: 0 5px; padding: 5px 10px; border: 1px solid #ddd; }
        .pagination a.current { background: #007bff; color: white; }
    </style>
</head>
<body>
    <h1>Players</h1>
    
    <p>Showing [[${players.size()}]] of [[${totalItems}]] players</p>
    
    <table>
        <thead>
            <tr>
                <!-- Sortable column headers -->
                <th>
                    <a th:href="@{/players(page=${currentPage}, size=10, sortBy='name', 
                               sortDir=${sortBy == 'name' and sortDir == 'asc' ? 'desc' : 'asc'})}"
                       th:class="${sortBy == 'name'} ? 'sortable current' : 'sortable'">
                        Name 
                        <span th:if="${sortBy == 'name'}">
                            <span th:if="${sortDir == 'asc'}">▲</span>
                            <span th:if="${sortDir == 'desc'}">▼</span>
                        </span>
                    </a>
                </th>
                <th>
                    <a th:href="@{/players(page=${currentPage}, size=10, sortBy='matchesWon', 
                               sortDir=${sortBy == 'matchesWon' and sortDir == 'asc' ? 'desc' : 'asc'})}"
                       th:class="${sortBy == 'matchesWon'} ? 'sortable current' : 'sortable'">
                        Matches Won
                        <span th:if="${sortBy == 'matchesWon'}">
                            <span th:if="${sortDir == 'asc'}">▲</span>
                            <span th:if="${sortDir == 'desc'}">▼</span>
                        </span>
                    </a>
                </th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="player : ${players}">
                <td th:text="${player.name}">Name</td>
                <td th:text="${player.matchesWon}">0</td>
            </tr>
        </tbody>
    </table>
    
    <!-- Pagination controls -->
    <div class="pagination" th:if="${totalPages > 1}">
        <!-- Previous -->
        <a th:if="${currentPage > 0}"
           th:href="@{/players(page=${currentPage - 1}, size=10, sortBy=${sortBy}, sortDir=${sortDir})}">
            « Previous
        </a>
        
        <!-- Page numbers -->
        <span th:each="i : ${#numbers.sequence(0, totalPages - 1)}">
            <a th:href="@{/players(page=${i}, size=10, sortBy=${sortBy}, sortDir=${sortDir})}"
               th:text="${i + 1}"
               th:class="${i == currentPage} ? 'current' : ''">
                1
            </a>
        </span>
        
        <!-- Next -->
        <a th:if="${currentPage < totalPages - 1}"
           th:href="@{/players(page=${currentPage + 1}, size=10, sortBy=${sortBy}, sortDir=${sortDir})}">
            Next »
        </a>
    </div>
</body>
</html>
```

### 🎯 GROOVY TRICK: Clickable Table Headers!

The template above makes **column headers clickable for sorting**:
- Click "Name" → Sort by name ascending
- Click again → Sort descending
- Visual indicators (▲▼) show current sort

This is how we built the leaderboards in High Low Jack!

---

## Sessions & State Management

### Storing Data in Session

```java
package com.yourcompany.myapp.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller demonstrating session management.
 * 
 * @author Dale
 * @version 1.0.0
 */
@Controller
public class GameController {
    
    /**
     * Start a new game session.
     */
    @GetMapping("/game/start")
    public String startGame(HttpSession session) {
        session.setAttribute("score", 0);
        session.setAttribute("playerName", "Dale");
        session.setAttribute("gameId", UUID.randomUUID().toString());
        return "redirect:/game/play";
    }
    
    /**
     * Game play page.
     */
    @GetMapping("/game/play")
    public String playGame(HttpSession session, Model model) {
        Integer score = (Integer) session.getAttribute("score");
        String playerName = (String) session.getAttribute("playerName");
        
        if (score == null) {
            return "redirect:/game/start";  // Session expired
        }
        
        model.addAttribute("score", score);
        model.addAttribute("playerName", playerName);
        return "game/play";
    }
    
    /**
     * Increment score.
     */
    @PostMapping("/game/score")
    public String incrementScore(
            @RequestParam int points,
            HttpSession session) {
        
        Integer currentScore = (Integer) session.getAttribute("score");
        if (currentScore != null) {
            session.setAttribute("score", currentScore + points);
        }
        
        return "redirect:/game/play";
    }
    
    /**
     * End game and clear session.
     */
    @GetMapping("/game/end")
    public String endGame(HttpSession session) {
        session.invalidate();  // Clear all session data
        return "redirect:/";
    }
}
```

### Session Scoped Beans

```java
package com.yourcompany.myapp.model;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Session-scoped bean for managing game state.
 * 
 * Spring creates one instance per HTTP session.
 * 
 * @author Dale
 * @version 1.0.0
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GameSession implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String gameId;
    private String playerName;
    private int score;
    private List<String> history = new ArrayList<>();
    
    public void reset() {
        this.gameId = null;
        this.playerName = null;
        this.score = 0;
        this.history.clear();
    }
    
    public void addToHistory(String event) {
        this.history.add(event);
    }
    
    // Getters and setters...
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public List<String> getHistory() { return history; }
}
```

**Using in Controller:**

```java
@Controller
public class GameController {
    
    @Autowired
    private GameSession gameSession;  // Injected per session!
    
    @GetMapping("/game")
    public String game(Model model) {
        model.addAttribute("game", gameSession);
        return "game";
    }
    
    @PostMapping("/game/action")
    public String action(@RequestParam String action) {
        gameSession.addToHistory(action);
        gameSession.setScore(gameSession.getScore() + 10);
        return "redirect:/game";
    }
}
```

### Session Configuration

**application.properties:**

```properties
# Session timeout (30 minutes)
server.servlet.session.timeout=30m

# Session persistence (survives server restart)
server.servlet.session.persistent=true

# Cookie configuration
server.servlet.session.cookie.name=MYAPP_SESSION
server.servlet.session.cookie.max-age=3600
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
```

### 🎯 GROOVY TRICK: Multi-Player Sessions

In High Low Jack, we used sessions to manage **multiple concurrent games**:

```java
// Each browser gets its own session
// Session stores: gameId, playerHand, currentMatch
// This allows multiple games simultaneously!

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MultiplayerSession {
    private String gameCode;
    private String playerName;
    private List<Card> hand;
    // ... etc
}
```

---

## Error Handling

### Global Exception Handler

```java
package com.yourcompany.myapp.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global exception handler for all controllers.
 * 
 * @author Dale
 * @version 1.0.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle IllegalArgumentException (e.g., player not found).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(IllegalArgumentException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error/404";
    }
    
    /**
     * Handle generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(Exception ex, Model model) {
        model.addAttribute("error", "An unexpected error occurred");
        model.addAttribute("details", ex.getMessage());
        return "error/500";
    }
}
```

### Custom Error Pages

Create these templates:

**templates/error/404.html:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Not Found</title>
</head>
<body>
    <h1>404 - Not Found</h1>
    <p th:text="${error}">The requested resource was not found.</p>
    <a th:href="@{/}">Go Home</a>
</body>
</html>
```

**templates/error/500.html:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Server Error</title>
</head>
<body>
    <h1>500 - Server Error</h1>
    <p>Something went wrong on our end.</p>
    <p th:if="${details}" th:text="${details}">Error details</p>
    <a th:href="@{/}">Go Home</a>
</body>
</html>
```

---

## Testing

### Unit Testing a Service

```java
package com.yourcompany.myapp.service;

import com.yourcompany.myapp.persistence.entity.Player;
import com.yourcompany.myapp.persistence.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlayerService.
 * 
 * @author Dale
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {
    
    @Mock
    private PlayerRepository playerRepository;
    
    @InjectMocks
    private PlayerService playerService;
    
    private Player testPlayer;
    
    @BeforeEach
    void setUp() {
        testPlayer = new Player("Dale", "dale@example.com");
        testPlayer.setId(1L);
    }
    
    @Test
    @DisplayName("Get player by ID - success")
    void testGetPlayerById_Success() {
        // GIVEN
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        
        // WHEN
        Player result = playerService.getPlayerById(1L);
        
        // THEN
        assertNotNull(result);
        assertEquals("Dale", result.getName());
        verify(playerRepository, times(1)).findById(1L);
    }
    
    @Test
    @DisplayName("Get player by ID - not found")
    void testGetPlayerById_NotFound() {
        // GIVEN
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());
        
        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> {
            playerService.getPlayerById(999L);
        });
    }
    
    @Test
    @DisplayName("Save player - success")
    void testSavePlayer_Success() {
        // GIVEN
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
        
        // WHEN
        Player result = playerService.savePlayer(testPlayer);
        
        // THEN
        assertNotNull(result);
        assertEquals("Dale", result.getName());
        verify(playerRepository, times(1)).save(testPlayer);
    }
    
    @Test
    @DisplayName("Record win - increments counters")
    void testRecordWin_IncrementsCounters() {
        // GIVEN
        testPlayer.setMatchesPlayed(10);
        testPlayer.setMatchesWon(7);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
        
        // WHEN
        playerService.recordWin(1L);
        
        // THEN
        assertEquals(11, testPlayer.getMatchesPlayed());
        assertEquals(8, testPlayer.getMatchesWon());
        verify(playerRepository, times(1)).save(testPlayer);
    }
}
```

### Integration Testing a Controller

```java
package com.yourcompany.myapp.web.controller;

import com.yourcompany.myapp.persistence.entity.Player;
import com.yourcompany.myapp.persistence.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for PlayerController.
 * 
 * @author Dale
 * @version 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
class PlayerControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();
        playerRepository.save(new Player("Dale", "dale@example.com"));
    }
    
    @Test
    void testListPlayers() throws Exception {
        mockMvc.perform(get("/players"))
            .andExpect(status().isOk())
            .andExpect(view().name("players/list"))
            .andExpect(model().attributeExists("players"))
            .andExpect(model().attribute("players", hasSize(1)));
    }
    
    @Test
    void testShowPlayer() throws Exception {
        Player player = playerRepository.findAll().get(0);
        
        mockMvc.perform(get("/players/" + player.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("players/show"))
            .andExpect(model().attributeExists("player"))
            .andExpect(model().attribute("player", hasProperty("name", is("Dale"))));
    }
}
```

---

## Deployment

### Building for Production

```bash
# Create executable JAR
mvn clean package

# JAR location
ls target/myapp-1.0.0.jar

# Run it
java -jar target/myapp-1.0.0.jar
```

### Production Configuration

**application-prod.properties:**

```properties
# Server
server.port=8080

# Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA - DO NOT use 'update' in production!
spring.jpa.hibernate.ddl-auto=validate

# Disable DevTools
spring.devtools.restart.enabled=false

# Template caching (ENABLE in production)
spring.thymeleaf.cache=true

# Logging
logging.level.root=WARN
logging.level.com.yourcompany.myapp=INFO

# Actuator (optional)
management.endpoints.web.exposure.include=health,info
```

### Running with Profile

```bash
# Development
java -jar -Dspring.profiles.active=dev myapp.jar

# Production
java -jar -Dspring.profiles.active=prod myapp.jar
```

### Railway Deployment

**Create `Procfile`:**

```
web: java -Dserver.port=$PORT -jar target/myapp-1.0.0.jar
```

**Railway Environment Variables:**

```
DATABASE_URL=postgresql://...
DB_USERNAME=postgres
DB_PASSWORD=...
SPRING_PROFILES_ACTIVE=prod
```

### Docker Deployment

**Dockerfile:**

```dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/myapp-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build and run:**

```bash
docker build -t myapp .
docker run -p 8080:8080 myapp
```

---

## Groovy Tricks We Learned

### 1. 🔥 DevTools Hot Reload

**Problem:** Restart app every time you change code  
**Solution:** Add DevTools + configure IDE

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
</dependency>
```

**Result:** Change code → Auto-restart in 1 second!

### 2. 🎯 Clickable Table Headers for Sorting

**Problem:** Users can't sort tables  
**Solution:** Make headers clickable links with sort params

```html
<th>
    <a th:href="@{/players(sortBy='name', sortDir=${currentSort == 'asc' ? 'desc' : 'asc'})}">
        Name <span th:text="${currentSort == 'asc' ? '▲' : '▼'}"></span>
    </a>
</th>
```

**Result:** Click header → Sort! Click again → Reverse!

### 3. 📄 Pagination with Page Numbers

**Problem:** Too many records to show on one page  
**Solution:** Use Spring Data's Pageable

```java
@GetMapping("/players")
public String list(@RequestParam(defaultValue = "0") int page, Model model) {
    Page<Player> playerPage = repository.findAll(PageRequest.of(page, 10));
    model.addAttribute("players", playerPage.getContent());
    return "list";
}
```

### 4. 🎭 Thymeleaf Fragments for DRY

**Problem:** Copy-paste header/footer in every template  
**Solution:** Extract to fragments

```html
<!-- fragments/header.html -->
<header th:fragment="header">...</header>

<!-- In any template -->
<div th:replace="~{fragments/header :: header}"></div>
```

### 5. 🔄 Session-Scoped Beans

**Problem:** Need to track state across requests  
**Solution:** Session-scoped Spring beans

```java
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GameSession implements Serializable {
    // This exists PER USER SESSION!
}
```

### 6. 🗄️ JPA Lifecycle Callbacks

**Problem:** Need to set timestamps automatically  
**Solution:** Use @PrePersist and @PreUpdate

```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

### 7. 🔍 Spring Data Query Methods

**Problem:** Writing SQL for every query  
**Solution:** Let Spring Data derive queries from method names!

```java
// Spring generates the SQL!
List<Player> findByNameContainingAndMatchesWonGreaterThan(String name, int wins);
```

### 8. 🎨 Conditional CSS Classes

**Problem:** Need to highlight active items  
**Solution:** Thymeleaf conditional classes

```html
<div th:classappend="${isActive} ? 'active' : ''">Content</div>
```

### 9. 📊 Repository with Custom Queries

**Problem:** Complex queries not possible with method names  
**Solution:** Mix generated + custom queries in same repo

```java
public interface PlayerRepository extends JpaRepository<Player, Long> {
    // Auto-generated
    List<Player> findByName(String name);
    
    // Custom JPQL
    @Query("SELECT p FROM Player p WHERE p.matchesWon / p.matchesPlayed > 0.5")
    List<Player> findTopPerformers();
}
```

### 10. 🚀 Profile-Based Configuration

**Problem:** Different settings for dev vs prod  
**Solution:** application-{profile}.properties

```
application-dev.properties  → Dev settings
application-prod.properties → Prod settings
```

Run with: `java -Dspring.profiles.active=prod -jar app.jar`

---

## Common Pitfalls

### 1. ❌ Forgetting @Transactional

**Problem:**
```java
public void updatePlayer(Player player) {
    playerRepository.save(player);  // No transaction!
}
```

**Solution:**
```java
@Transactional
public void updatePlayer(Player player) {
    playerRepository.save(player);
}
```

### 2. ❌ Using ddl-auto=update in Production

**Never do this:**
```properties
spring.jpa.hibernate.ddl-auto=update  # DANGEROUS in prod!
```

**Instead:**
```properties
spring.jpa.hibernate.ddl-auto=validate  # Safe!
```

### 3. ❌ Not Handling Optional

**Problem:**
```java
Player player = repository.findById(id).get();  // Can throw!
```

**Solution:**
```java
Player player = repository.findById(id)
    .orElseThrow(() -> new IllegalArgumentException("Not found"));
```

### 4. ❌ Exposing Entities in REST APIs

**Problem:**
```java
@GetMapping("/players/{id}")
public Player getPlayer(@PathVariable Long id) {
    return repository.findById(id).get();  // Don't expose entity!
}
```

**Solution:** Use DTOs
```java
@GetMapping("/players/{id}")
public PlayerDTO getPlayer(@PathVariable Long id) {
    Player player = repository.findById(id).orElseThrow();
    return new PlayerDTO(player);  // DTO pattern
}
```

### 5. ❌ Caching Templates in Development

**Problem:** Changes to Thymeleaf not showing up

**Solution:**
```properties
# application-dev.properties
spring.thymeleaf.cache=false
```

### 6. ❌ Not Using Constructor Injection

**Avoid field injection:**
```java
@Autowired
private PlayerService playerService;  // Harder to test
```

**Use constructor injection:**
```java
private final PlayerService playerService;

@Autowired
public PlayerController(PlayerService playerService) {
    this.playerService = playerService;
}
```

### 7. ❌ Business Logic in Controllers

**Bad:**
```java
@PostMapping("/win")
public String recordWin(@RequestParam Long id) {
    Player p = repo.findById(id).get();
    p.setMatchesWon(p.getMatchesWon() + 1);  // Logic in controller!
    repo.save(p);
    return "redirect:/players";
}
```

**Good:**
```java
@PostMapping("/win")
public String recordWin(@RequestParam Long id) {
    playerService.recordWin(id);  // Logic in service!
    return "redirect:/players";
}
```

---

## Best Practices

### 1. ✅ Layer Your Application

```
Controller  → Handles HTTP
Service     → Business logic
Repository  → Database access
Entity      → Data model
```

### 2. ✅ Use DTOs for APIs

Don't expose database entities directly in REST APIs.

### 3. ✅ Write Tests

Especially for services and business logic.

### 4. ✅ Use Transactions Appropriately

Mark service methods with @Transactional.

### 5. ✅ Validate Input

Use Bean Validation (jakarta.validation).

### 6. ✅ Handle Exceptions Globally

Use @ControllerAdvice for consistent error handling.

### 7. ✅ Use Lombok (Optional)

Reduce boilerplate with @Data, @Builder, etc.

### 8. ✅ Profile Your Application

Use application-{profile}.properties.

### 9. ✅ Document Your Code

Javadoc for public methods and classes.

### 10. ✅ Version Your Entities

Use @Version for optimistic locking.

---

## Next Steps

### Expand Your Knowledge

- **Spring Security** - Authentication & authorization
- **Spring WebSocket** - Real-time communication (like we did in High Low Jack!)
- **Spring Actuator** - Production monitoring
- **Spring Cache** - Application caching
- **Spring Batch** - Batch processing
- **Spring Cloud** - Microservices

### Resources

- **Official Docs:** https://spring.io/projects/spring-boot
- **Spring Guides:** https://spring.io/guides
- **Baeldung:** https://www.baeldung.com/spring-boot
- **Spring Boot Reference:** https://docs.spring.io/spring-boot/docs/current/reference/html/

---

## Conclusion

Spring Boot is a **powerful, flexible framework** that lets you build production-ready applications quickly.

Key takeaways:
- ✅ Convention over configuration
- ✅ Auto-configuration magic
- ✅ Rich ecosystem
- ✅ Production-ready from day one

You now have the foundation to build **real-world web applications** like we did with High Low Jack and RTA Portal!

---

**Built with 💚 by Primus & Dale**  
*April 9, 2026 - From our journey to yours*

**"The best way to learn Spring Boot is to build something real."**  
*— And you did! 🎮*

---

## Changelog

**v1.0.0** (2026-04-09)
- Initial release
- Based on High Low Jack & RTA Portal experience
- Comprehensive examples and groovy tricks included
- Production-ready patterns documented
