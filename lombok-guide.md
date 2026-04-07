# Project Lombok - The Complete Guide

## 📚 What is Lombok?

**Project Lombok** is a Java library that uses annotations to automatically generate common boilerplate code at compile time. Instead of writing repetitive getters, setters, constructors, and utility methods, you simply add annotations and Lombok generates the code for you.

**Official Website:** https://projectlombok.org/

---

## 🎯 The Problem Lombok Solves

### Without Lombok:
```java
public class Member {
    private Long id;
    private String fullName;
    private String email;
    
    // Constructor
    public Member() {}
    
    public Member(Long id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    // equals()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id) &&
               Objects.equals(fullName, member.fullName) &&
               Objects.equals(email, member.email);
    }
    
    // hashCode()
    @Override
    public int hashCode() {
        return Objects.hash(id, fullName, email);
    }
    
    // toString()
    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
```

**70+ lines of repetitive boilerplate code!**

### With Lombok:
```java
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private Long id;
    private String fullName;
    private String email;
}
```

**10 lines - does EXACTLY the same thing!**

---

## 🔧 Essential Lombok Annotations

### 1. @Data
**The Swiss Army Knife of Lombok**

Generates all of:
- Getters for all fields
- Setters for all non-final fields
- `toString()` method
- `equals()` and `hashCode()` methods
- Required args constructor (for final fields)

```java
@Data
public class Product {
    private Long id;
    private String name;
    private Double price;
}

// Usage:
Product p = new Product();
p.setName("Laptop");
p.setPrice(999.99);
System.out.println(p.getName()); // Laptop
System.out.println(p);           // Product(id=null, name=Laptop, price=999.99)
```

---

### 2. @Getter / @Setter
**Fine-Grained Control**

Generate only getters or only setters:

```java
public class User {
    @Getter @Setter
    private String username;
    
    @Getter              // Read-only
    private String password;
    
    @Setter              // Write-only
    private String token;
    
    private String internal; // No getter or setter
}
```

**Class-level usage:**
```java
@Getter @Setter
public class Customer {
    private String name;
    private String email;
    // All fields get getters and setters
}
```

---

### 3. @NoArgsConstructor
**Empty Constructor**

Generates a constructor with no parameters:

```java
@NoArgsConstructor
public class Member {
    private String name;
}

// Generated:
// public Member() {}
```

**Common use with JPA entities:**
```java
@Entity
@NoArgsConstructor  // Required by JPA
public class Member {
    @Id
    private Long id;
    private String name;
}
```

---

### 4. @AllArgsConstructor
**Full Constructor**

Generates a constructor with all fields:

```java
@AllArgsConstructor
public class Point {
    private int x;
    private int y;
}

// Usage:
Point p = new Point(10, 20);
```

---

### 5. @RequiredArgsConstructor
**Smart Constructor**

Generates constructor for:
- `final` fields
- Fields marked with `@NonNull`

```java
@RequiredArgsConstructor
public class Service {
    private final Repository repository;  // Will be in constructor
    @NonNull private final Logger logger; // Will be in constructor
    private String optionalConfig;        // NOT in constructor
}

// Generated:
// public Service(Repository repository, Logger logger) {
//     this.repository = repository;
//     this.logger = logger;
// }
```

**Perfect for dependency injection:**
```java
@Service
@RequiredArgsConstructor  // Spring will inject via constructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final EmailService emailService;
}
```

---

### 6. @ToString
**Debug-Friendly Output**

Generates a readable `toString()` method:

```java
@ToString
public class Book {
    private String title;
    private String author;
    private int pages;
}

Book book = new Book();
book.setTitle("Clean Code");
book.setAuthor("Robert Martin");
book.setPages(464);

System.out.println(book);
// Output: Book(title=Clean Code, author=Robert Martin, pages=464)
```

**Exclude sensitive fields:**
```java
@ToString(exclude = {"password", "secretKey"})
public class User {
    private String username;
    private String password;
    private String secretKey;
}
```

---

### 7. @EqualsAndHashCode
**Proper Object Comparison**

Generates `equals()` and `hashCode()` based on fields:

```java
@EqualsAndHashCode
public class Person {
    private String name;
    private int age;
}

Person p1 = new Person();
p1.setName("Dale");
p1.setAge(40);

Person p2 = new Person();
p2.setName("Dale");
p2.setAge(40);

System.out.println(p1.equals(p2)); // true
```

**Exclude fields from comparison:**
```java
@EqualsAndHashCode(exclude = {"lastModified", "id"})
public class Document {
    private Long id;
    private String content;
    private LocalDateTime lastModified;
}
```

**Use only specific fields:**
```java
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @EqualsAndHashCode.Include
    private String email;  // Only email used for equals/hashCode
    
    private String name;
    private int age;
}
```

---

### 8. @Builder
**Fluent Object Creation**

Generates the Builder pattern:

```java
@Builder
public class Member {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
}

// Usage:
Member member = Member.builder()
    .id(1L)
    .fullName("Dale Smith")
    .email("dale@willows.top")
    .phoneNumber("555-1234")
    .build();
```

**With default values:**
```java
@Builder
public class Config {
    @Builder.Default
    private int timeout = 30;
    
    @Builder.Default
    private boolean enabled = true;
    
    private String apiKey;
}

Config config = Config.builder()
    .apiKey("secret123")
    .build();
// timeout=30, enabled=true are already set!
```

**Combine with other annotations:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    @Builder.Default
    private Double price = 0.0;
}
```

---

### 9. @Slf4j
**Instant Logging**

Generates a logger field automatically:

```java
@Slf4j
public class MemberService {
    
    public void registerMember(Member member) {
        log.info("Registering new member: {}", member.getEmail());
        
        try {
            // ... business logic
            log.debug("Member saved successfully");
        } catch (Exception e) {
            log.error("Failed to register member", e);
            throw e;
        }
    }
}

// Generated field:
// private static final org.slf4j.Logger log = 
//     org.slf4j.LoggerFactory.getLogger(MemberService.class);
```

**Other logger annotations:**
- `@Log` - java.util.logging.Logger
- `@Log4j` - Apache Log4j
- `@Log4j2` - Apache Log4j 2
- `@CommonsLog` - Apache Commons Logging
- `@Slf4j` - SLF4J (most popular)

---

### 10. @Value
**Immutable Objects**

Creates an immutable class (all fields final, no setters):

```java
@Value
public class Point {
    int x;
    int y;
}

// Equivalent to:
public final class Point {
    private final int x;
    private final int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    
    // equals(), hashCode(), toString() also generated
}

// Usage:
Point p = new Point(10, 20);
// p.setX(5); // Compile error - no setters!
```

---

### 11. @NonNull
**Null Safety**

Generates null checks:

```java
public class UserService {
    
    public void processUser(@NonNull User user) {
        // Lombok generates:
        // if (user == null) {
        //     throw new NullPointerException("user is marked non-null but is null");
        // }
        
        System.out.println(user.getName());
    }
}
```

---

### 12. @Cleanup
**Auto Resource Management**

Automatically calls `close()` on resources:

```java
public void readFile(String path) throws IOException {
    @Cleanup InputStream in = new FileInputStream(path);
    // ... use the stream
    
    // Lombok automatically generates:
    // try {
    //     InputStream in = new FileInputStream(path);
    //     // ... your code
    // } finally {
    //     if (in != null) in.close();
    // }
}
```

---

## 📦 Adding Lombok to Your Project

### Maven (pom.xml):
```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle (build.gradle):
```gradle
dependencies {
    compileOnly 'org.projectlombok:lombok:1.18.30'
    annotationProcessor 'org.projectlombok:lombok:1.18.30'
}
```

---

## 🛠️ IDE Setup

### IntelliJ IDEA:
1. Install "Lombok Plugin" from marketplace
2. Enable annotation processing:
   - File → Settings → Build → Compiler → Annotation Processors
   - Check "Enable annotation processing"

### Eclipse:
1. Download `lombok.jar` from https://projectlombok.org/download
2. Run: `java -jar lombok.jar`
3. Select Eclipse installation directory
4. Click "Install/Update"

### VS Code:
1. Install "Language Support for Java" extension
2. Lombok support is built-in (but may have issues with NetBeans compiler)

---

## 💡 Real-World Examples

### Spring Boot Entity:
```java
@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String phoneNumber;
    
    @Builder.Default
    private LocalDateTime registrationDate = LocalDateTime.now();
    
    @Builder.Default
    private String status = "ACTIVE";
}
```

### Spring Service:
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    
    public Member createMember(MemberDTO dto) {
        log.info("Creating new member: {}", dto.getEmail());
        
        Member member = Member.builder()
            .fullName(dto.getFullName())
            .email(dto.getEmail())
            .phoneNumber(dto.getPhoneNumber())
            .build();
        
        Member saved = memberRepository.save(member);
        log.debug("Member created with ID: {}", saved.getId());
        
        emailService.sendWelcome(saved.getEmail());
        
        return saved;
    }
}
```

### DTO (Data Transfer Object):
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String preferredCommunication;
}
```

---

## ⚖️ Pros and Cons

### ✅ Advantages:

1. **Less Code**
   - Reduce boilerplate by 50-80%
   - Cleaner, more readable classes
   
2. **Fewer Bugs**
   - No typos in getters/setters
   - Standardized implementations
   
3. **Easy Refactoring**
   - Add/remove fields without updating methods
   - Lombok regenerates everything automatically
   
4. **Consistency**
   - All classes follow same patterns
   - Team members write code the same way
   
5. **Focus on Business Logic**
   - Less time on boilerplate
   - More time on what matters

### ❌ Disadvantages:

1. **"Magic" Code**
   - Generated code not visible in source files
   - Can confuse new developers
   
2. **IDE Setup Required**
   - Plugins needed for IntelliJ/Eclipse
   - Occasional compatibility issues
   
3. **Debugging Challenges**
   - Can't set breakpoints in generated code
   - Stack traces reference non-existent code
   
4. **Learning Curve**
   - Team needs to understand annotations
   - Not obvious what code is generated
   
5. **Compile-Time Dependency**
   - Annotation processing adds build time
   - Can conflict with other processors

---

## 🎯 Best Practices

### 1. Use @Data Sparingly
**Don't use on entities with relationships:**
```java
// BAD - can cause infinite loops in toString()
@Data
@Entity
public class Parent {
    @OneToMany
    private List<Child> children;
}

@Data
@Entity
public class Child {
    @ManyToOne
    private Parent parent;
}

// GOOD - use specific annotations
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Parent {
    @OneToMany
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Child> children;
}
```

### 2. Exclude Lazy Fields
```java
@ToString(exclude = {"lazyField"})
@EqualsAndHashCode(exclude = {"lazyField"})
@Entity
public class MyEntity {
    @OneToMany(fetch = FetchType.LAZY)
    private List<Child> lazyField;
}
```

### 3. Use @Builder for Complex Objects
```java
// Much cleaner than constructor with 10 parameters
Member member = Member.builder()
    .fullName("Dale Smith")
    .email("dale@example.com")
    .phoneNumber("555-1234")
    .address("123 Main St")
    .city("London")
    .postcode("SW1A 1AA")
    .isLeaseholder(true)
    .preferredCommunication("EMAIL")
    .build();
```

### 4. Combine Annotations Wisely
```java
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @EqualsAndHashCode.Include
    private String email;
    private String name;
    private String password;
}
```

---

## 🚫 Common Pitfalls

### 1. Infinite Recursion with Bidirectional Relationships
```java
// PROBLEM:
@Data
public class Parent {
    private List<Child> children;
}

@Data
public class Child {
    private Parent parent;
}
// toString() causes infinite loop!

// SOLUTION:
@ToString(exclude = "children")
public class Parent { ... }

@ToString(exclude = "parent")
public class Child { ... }
```

### 2. Mutable @Data Objects in Collections
```java
@Data
public class Person {
    private String name;
}

Set<Person> people = new HashSet<>();
Person p = new Person();
p.setName("Dale");
people.add(p);

p.setName("John");  // Changes hashCode!
people.contains(p); // Might return false!

// SOLUTION: Use @Value for immutable objects
```

### 3. @Builder with Inheritance
```java
// Doesn't work as expected
@Builder
public class Child extends Parent {
    private String childField;
}

// Parent fields not accessible in builder!

// SOLUTION: Use @SuperBuilder
@SuperBuilder
public class Parent {
    private String parentField;
}

@SuperBuilder
public class Child extends Parent {
    private String childField;
}
```

---

## 📊 Comparison: Manual vs Lombok

| Aspect | Manual | Lombok |
|--------|--------|--------|
| **Lines of Code** | 70+ for simple POJO | 10-15 lines |
| **Maintenance** | Update manually | Auto-updates |
| **Readability** | Verbose, cluttered | Clean, concise |
| **IDE Support** | Perfect | Requires plugin |
| **Debugging** | Easy | Slightly harder |
| **Team Onboarding** | Easy | Learning curve |
| **Build Time** | Faster | Slightly slower |
| **Flexibility** | Full control | Some limitations |

---

## 🔍 When to Use Lombok

### ✅ Great For:
- **DTOs** (Data Transfer Objects)
- **Simple POJOs** (Plain Old Java Objects)
- **Value Objects** (Immutable data holders)
- **Spring Beans** (Services with DI)
- **JPA Entities** (with caution)

### ❌ Avoid For:
- **Classes with complex logic**
- **Public APIs/Libraries** (consumers need Lombok too)
- **Teams strongly opposed to "magic"**
- **Legacy codebases** (consistency matters)

---

## 🎓 Learning Resources

- **Official Docs:** https://projectlombok.org/features/
- **GitHub:** https://github.com/projectlombok/lombok
- **Video Tutorials:** Search "Project Lombok" on YouTube
- **IntelliJ Plugin:** https://plugins.jetbrains.com/plugin/6317-lombok

---

## 📝 Configuration File

Create `lombok.config` in project root for global settings:

```properties
# Make generated code cleaner
lombok.addLombokGeneratedAnnotation = true

# Configure builders
lombok.builder.className = Builder
lombok.singular.auto = true

# Logging
lombok.log.fieldName = logger
lombok.log.fieldIsStatic = true

# Stop bubbling (don't inherit config from parent dirs)
config.stopBubbling = true

# Add constructor properties for Jackson/Spring
lombok.anyConstructor.addConstructorProperties = true
```

---

## 🎯 Summary

**Lombok is a powerful tool that:**
- ✅ Reduces boilerplate by 50-80%
- ✅ Makes code cleaner and more maintainable
- ✅ Enforces consistency across projects
- ✅ Speeds up development

**But requires:**
- ⚠️ IDE plugin installation
- ⚠️ Team buy-in and training
- ⚠️ Careful use with JPA entities
- ⚠️ Understanding of what code is generated

**The Decision:**
- Use it for new projects where team is on board
- Skip it if team prefers explicit code
- Both approaches are valid - choose what works for YOUR team!

---

**Written by Dale & Primus**  
**March 2026**
