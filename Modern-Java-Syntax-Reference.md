# Modern Java Syntax Reference (Java 8+)
## The Complete Guide for Dale

This guide covers all the modern Java syntax we discussed: Streams, Lambdas, Method References, Generics, Threads, and Interfaces with Generics.

---

## Table of Contents

1. [Lambdas (The Arrow `->`)](#lambdas)
2. [Method References (The Double Colon `::`)](#method-references)
3. [Streams (Functional Pipelines)](#streams)
4. [Generics (Type Parameters)](#generics)
5. [Interface Generics](#interface-generics)
6. [Threads (Old vs New)](#threads)
7. [Quick Reference Tables](#quick-reference)

---

## Lambdas (The Arrow `->`) {#lambdas}

### What Is It?

**Anonymous function** - a function without a name.

### The Syntax

```java
parameter -> expression
```

**Or with multiple parameters:**
```java
(param1, param2) -> expression
```

**Or with multiple statements:**
```java
parameter -> {
    statement1;
    statement2;
    return result;
}
```

---

### Reading Lambdas

**Lambda:**
```java
block -> block.getShortName()
```

**Read as:** "Take a block, and return block.getShortName()"

**Lambda:**
```java
member -> member.getStatus().equals("ACTIVE")
```

**Read as:** "Take a member, check if their status equals ACTIVE"

**Lambda:**
```java
(a, b) -> a + b
```

**Read as:** "Take two numbers, add them together"

---

### Before Lambdas (Java 7)

```java
Thread thread = new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello!");
    }
});
thread.start();
```

**10 lines of boilerplate!**

---

### With Lambdas (Java 8+)

```java
Thread thread = new Thread(() -> System.out.println("Hello!"));
thread.start();
```

**2 lines! Same functionality!**

---

### Real Examples

#### Example 1: Filter a List

**Old way:**
```java
List<Member> activeMembers = new ArrayList<>();
for (Member member : members) {
    if (member.getStatus().equals("ACTIVE")) {
        activeMembers.add(member);
    }
}
```

**Lambda way:**
```java
List<Member> activeMembers = members.stream()
    .filter(member -> member.getStatus().equals("ACTIVE"))
    .collect(Collectors.toList());
```

---

#### Example 2: Transform a List

**Old way:**
```java
List<String> names = new ArrayList<>();
for (Block block : blocks) {
    names.add(block.getShortName());
}
```

**Lambda way:**
```java
List<String> names = blocks.stream()
    .map(block -> block.getShortName())
    .collect(Collectors.toList());
```

---

### Lambda Parameter Naming

**You choose the parameter name!**

```java
// All these are IDENTICAL:
.map(block -> block.getShortName())
.map(b -> b.getShortName())
.map(x -> x.getShortName())
.map(item -> item.getShortName())
```

**Convention:** Use descriptive names when clear, short names when obvious.

---

## Method References (The Double Colon `::`) {#method-references}

### What Is It?

**Shorthand for lambdas** that just call one method.

### The Syntax

```java
ClassName::methodName
```

---

### When to Use

**Lambda that just calls one method:**
```java
block -> block.getShortName()
```

**Can become method reference:**
```java
Block::getShortName
```

---

### Types of Method References

#### 1. Instance Method of Particular Type

```java
// Lambda:
block -> block.getShortName()

// Method reference:
Block::getShortName
```

#### 2. Static Method

```java
// Lambda:
s -> Integer.parseInt(s)

// Method reference:
Integer::parseInt
```

#### 3. Instance Method of Particular Object

```java
// Lambda:
member -> emailService.sendEmail(member)

// Method reference:
emailService::sendEmail
```

#### 4. Constructor

```java
// Lambda:
name -> new Block(name)

// Method reference:
Block::new
```

---

### When You CANNOT Use Method Reference

**Lambda does more than just call one method:**

```java
// Cannot be method reference - multiple operations
block -> block.getShortName().toUpperCase()

// Cannot be method reference - multiple parameters used differently
(a, b) -> a.compareTo(b.getName())
```

**Stay as lambda!**

---

## Streams (Functional Pipelines) {#streams}

### What Are Streams?

**Fancy loops** that let you transform, filter, and process collections.

### The Pattern

```java
collection.stream()              // Start the pipeline
    .filter(condition)           // Keep matching items
    .map(transformation)         // Transform each item
    .sorted()                    // Sort
    .collect(Collectors.toList()) // Gather results
```

---

### Old Way vs Stream Way

#### Get Names from Active Members

**Old way:**
```java
List<String> names = new ArrayList<>();
for (Member member : members) {
    if (member.getStatus().equals("ACTIVE")) {
        names.add(member.getName());
    }
}
```

**Stream way:**
```java
List<String> names = members.stream()
    .filter(member -> member.getStatus().equals("ACTIVE"))
    .map(Member::getName)
    .collect(Collectors.toList());
```

**Reads like English:**  
"From members, filter active ones, map to names, collect to list"

---

### Common Stream Operations

#### Intermediate Operations (Return Stream - Can Chain)

**`.filter(predicate)`** - Keep matching items
```java
.filter(member -> member.getAge() > 18)
```

**`.map(function)`** - Transform each item
```java
.map(Member::getName)
.map(name -> name.toUpperCase())
```

**`.sorted()`** - Sort elements
```java
.sorted()                                    // Natural order
.sorted(Comparator.comparing(Member::getName)) // By name
```

**`.distinct()`** - Remove duplicates
```java
.distinct()
```

**`.limit(n)`** - Take first n items
```java
.limit(10)
```

**`.skip(n)`** - Skip first n items
```java
.skip(5)
```

---

#### Terminal Operations (Return Result - End Pipeline)

**`.collect(Collectors.toList())`** - Gather to List
```java
.collect(Collectors.toList())
```

**`.forEach(action)`** - Do something with each
```java
.forEach(System.out::println)
.forEach(member -> sendEmail(member))
```

**`.count()`** - Count elements
```java
long count = members.stream().filter(...).count();
```

**`.findFirst()`** - Get first element
```java
Optional<Member> first = members.stream().findFirst();
```

**`.anyMatch(predicate)`** - Check if any match
```java
boolean hasActive = members.stream()
    .anyMatch(m -> m.getStatus().equals("ACTIVE"));
```

**`.allMatch(predicate)`** - Check if all match
```java
boolean allActive = members.stream()
    .allMatch(m -> m.getStatus().equals("ACTIVE"));
```

---

### Parallel Streams

**Use multiple CPU cores automatically!**

```java
// Sequential (one at a time)
members.stream()
    .map(this::processExpensiveOperation)
    .collect(Collectors.toList());

// Parallel (all cores at once)
members.parallelStream()
    .map(this::processExpensiveOperation)
    .collect(Collectors.toList());
```

**When to use:**
- ✅ Large datasets (1000+ items)
- ✅ CPU-intensive operations
- ❌ Small datasets (overhead not worth it)
- ❌ Simple operations (too fast already)

---

### Real RTA Examples

#### Get All Block Names

```java
List<String> blockNames = blockRepository.findAll()
    .stream()
    .map(Block::getShortName)
    .distinct()
    .sorted()
    .collect(Collectors.toList());
```

**Breakdown:**
1. Get all blocks
2. Start stream
3. Extract short name from each
4. Remove duplicates
5. Sort alphabetically
6. Collect to list

---

#### Count Active Members

```java
long activeCount = memberRepository.findAll()
    .stream()
    .filter(member -> member.getStatus().equals("ACTIVE"))
    .count();
```

---

## Generics (Type Parameters) {#generics}

### What Are Generics?

**Type safety at compile time** - tell Java what type you're working with.

### The Syntax

```java
ClassName<Type>
```

**Angle brackets `< >` contain the type parameter.**

---

### Why Generics Exist

#### Without Generics (Old Java)

```java
List list = new ArrayList();
list.add("Hello");
list.add(123);
list.add(new Member());

String text = (String) list.get(0);  // Manual cast - error prone!
Integer num = (Integer) list.get(1); // Manual cast
Member m = (Member) list.get(2);     // Manual cast

// Runtime error if you get index wrong!
String oops = (String) list.get(1);  // ClassCastException at RUNTIME!
```

**Problems:**
- No type safety ❌
- Manual casting everywhere ❌
- Errors only at runtime ❌

---

#### With Generics (Modern Java)

```java
List<String> list = new ArrayList<>();
list.add("Hello");
list.add(123);        // COMPILE ERROR! Can only add Strings
list.add(new Member()); // COMPILE ERROR! Can only add Strings

String text = list.get(0);  // No cast needed!
```

**Benefits:**
- Type safety at compile time ✅
- No manual casting ✅
- Errors caught immediately ✅

---

### Common Generic Classes

#### List

```java
List<String> names = new ArrayList<>();
List<Member> members = new ArrayList<>();
List<Integer> numbers = new ArrayList<>();
```

---

#### Map

```java
Map<String, Integer> scores = new HashMap<>();
     │       │
     │       └─ Value type
     └─ Key type

scores.put("Dale", 100);
Integer daleScore = scores.get("Dale");  // No cast needed!
```

---

#### Optional

```java
Optional<Member> maybeMember = memberRepository.findByEmail("dale@example.com");

if (maybeMember.isPresent()) {
    Member member = maybeMember.get();
}
```

---

### Generic Methods

```java
public <T> List<T> reverse(List<T> list) {
        │           │
        │           └─ Returns List of T
        └─ Generic type parameter

    Collections.reverse(list);
    return list;
}

// Use it:
List<String> names = reverse(Arrays.asList("a", "b", "c"));
List<Integer> nums = reverse(Arrays.asList(1, 2, 3));
```

---

## Interface Generics {#interface-generics}

### What Does `Interface<Type>` Mean?

**An interface that works with a specific type.**

---

### Example: JpaRepository

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
                                                         │      │
                                                         │      └─ ID type
                                                         └─ Entity type
}
```

**What this means:**

"This is a repository that works with `Member` entities that have `Long` IDs"

---

### How Spring Data Uses This

**Spring Data sees:**
```java
JpaRepository<Member, Long>
```

**And generates:**
```java
Member findById(Long id);              // Returns Member, takes Long
List<Member> findAll();                // Returns List of Members
Member save(Member member);            // Takes Member, returns Member
void deleteById(Long id);              // Takes Long ID
```

**The generic types tell Spring Data:**
- What entity to work with (Member)
- What type the ID is (Long)
- What to return from methods

---

### Another Example: Function Interface

```java
Function<Block, String> getName = block -> block.getShortName();
         │      │
         │      └─ OUTPUT type (what it returns)
         └─ INPUT type (what it takes)
```

**Means:** "A function that takes a Block and returns a String"

**Usage:**
```java
String name = getName.apply(myBlock);
```

---

### Multiple Type Parameters

**Two types:**
```java
Map<String, Integer> scores = new HashMap<>();
    │       │
    │       └─ Value type
    └─ Key type
```

**Three types:**
```java
BiFunction<Integer, Integer, String> addAndFormat = 
           │        │        │
           │        │        └─ RETURN type
           │        └─ SECOND parameter type
           └─ FIRST parameter type
    (a, b) -> "Sum: " + (a + b);

String result = addAndFormat.apply(5, 10);  // "Sum: 15"
```

---

### Why Use Interface Generics?

#### Without Generics

```java
public interface Repository {
    Object findById(Object id);        // Returns Object - need to cast
    List findAll();                    // List of what? Unknown!
    Object save(Object entity);        // Takes anything!
}

// Using it:
Repository repo = ...;
Member member = (Member) repo.findById(1L);  // Manual cast - error prone!
```

---

#### With Generics

```java
public interface Repository<T, ID> {
    T findById(ID id);           // Returns T, takes ID
    List<T> findAll();           // Returns List of T
    T save(T entity);            // Takes T, returns T
}

// Using it:
Repository<Member, Long> repo = ...;
Member member = repo.findById(1L);  // No cast! Type safe!
```

---

### Real RTA Example

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    // Spring Data knows:
    // - Entity type is Member
    // - ID type is Long
    
    // So it generates methods with correct types:
    Optional<Member> findById(Long id);           // Returns Member, takes Long
    List<Member> findAll();                       // Returns List<Member>
    Member save(Member member);                   // Takes/returns Member
    void deleteById(Long id);                     // Takes Long
    
    // Custom methods also use the types:
    Optional<Member> findByEmail(String email);   // Returns Member
    List<Member> findByStatus(String status);     // Returns List<Member>
}
```

---

### Generic Interface Pattern

**Pattern:**
```java
Interface<TypeParameter1, TypeParameter2, ...>
```

**What it means:**
- Interface works with these specific types
- Methods use these types
- Compiler checks type safety
- No casting needed

**Common examples:**
```java
List<String>                    // List of Strings
Map<String, Integer>            // Map with String keys, Integer values
Optional<Member>                // Optional that might contain Member
JpaRepository<Member, Long>     // Repository for Members with Long IDs
Function<Block, String>         // Function: Block → String
Comparator<Member>              // Compares Members
```

---

## Threads (Old vs New) {#threads}

### Old Way (Pre-Java 8)

#### Extend Thread

```java
class MyThread extends Thread {
    public void run() {
        System.out.println("Running!");
    }
}

MyThread thread = new MyThread();
thread.start();
```

---

#### Implement Runnable

```java
class MyTask implements Runnable {
    public void run() {
        System.out.println("Running!");
    }
}

Thread thread = new Thread(new MyTask());
thread.start();
```

---

#### Anonymous Class

```java
Thread thread = new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("Running!");
    }
});
thread.start();
```

**Verbose! 7 lines!**

---

### New Way (Java 8+)

#### Lambda

```java
Thread thread = new Thread(() -> System.out.println("Running!"));
thread.start();
```

**1 line! Same functionality!**

---

#### Lambda with Multiple Statements

```java
Thread thread = new Thread(() -> {
    System.out.println("Starting...");
    doWork();
    System.out.println("Done!");
});
thread.start();
```

---

### ExecutorService (Modern Threading)

#### Old Way

```java
ExecutorService executor = Executors.newFixedThreadPool(4);

executor.submit(new Runnable() {
    public void run() {
        sendEmail(member);
    }
});
```

---

#### New Way

```java
ExecutorService executor = Executors.newFixedThreadPool(4);

executor.submit(() -> sendEmail(member));
```

**Clean!**

---

### Parallel Streams (Easiest!)

**Automatic threading!**

```java
members.parallelStream()
    .forEach(member -> sendEmail(member));
```

**Uses all CPU cores automatically!**

---

## Quick Reference Tables {#quick-reference}

### Lambda Syntax

| Pattern | Meaning | Example |
|---------|---------|---------|
| `x -> expression` | Take x, return expression | `x -> x * 2` |
| `(x, y) -> expression` | Take x and y, return expression | `(a, b) -> a + b` |
| `x -> { statements }` | Take x, execute statements | `x -> { print(x); return x; }` |
| `() -> expression` | No parameters | `() -> 42` |

---

### Method Reference Types

| Type | Syntax | Lambda Equivalent |
|------|--------|-------------------|
| Static method | `ClassName::methodName` | `x -> ClassName.methodName(x)` |
| Instance method | `ClassName::methodName` | `x -> x.methodName()` |
| Specific object | `object::methodName` | `x -> object.methodName(x)` |
| Constructor | `ClassName::new` | `x -> new ClassName(x)` |

---

### Stream Operations

| Operation | Purpose | Returns Stream? |
|-----------|---------|-----------------|
| `filter(predicate)` | Keep matching | Yes |
| `map(function)` | Transform | Yes |
| `sorted()` | Sort | Yes |
| `distinct()` | Remove duplicates | Yes |
| `limit(n)` | Take first n | Yes |
| `collect()` | Gather results | No |
| `forEach()` | Do action | No |
| `count()` | Count | No |
| `findFirst()` | Get first | No |

---

### Common Generic Types

| Type | Meaning | Example |
|------|---------|---------|
| `List<T>` | List of T | `List<String>` |
| `Map<K, V>` | Map with key K, value V | `Map<String, Integer>` |
| `Optional<T>` | Optional T | `Optional<Member>` |
| `Function<T, R>` | Function: T → R | `Function<Block, String>` |
| `JpaRepository<T, ID>` | Repo for T with ID | `JpaRepository<Member, Long>` |

---

## Key Takeaways

### Lambdas
- Anonymous functions with `->` syntax
- Replace verbose anonymous classes
- Make code more readable and concise

### Method References
- Shorthand for lambdas that just call one method
- Use `::` syntax
- Even more concise than lambdas

### Streams
- Functional way to process collections
- Chain operations together
- More readable than loops
- Can parallelize easily

### Generics
- Type safety at compile time
- No manual casting
- Errors caught early
- Clearer code

### Interface Generics
- Interfaces that work with specific types
- Spring Data uses heavily
- Provides type safety
- Enables code generation

### Modern Threading
- Lambdas make threads cleaner
- ExecutorService for thread pools
- Parallel streams for easy parallelization

---

## Remember

**Old Java (pre-8):**
- Verbose
- Lots of boilerplate
- Anonymous classes everywhere
- Manual casting
- Runtime errors

**Modern Java (8+):**
- Concise
- Lambdas and streams
- Method references
- Type safety
- Compile-time errors

**Both work! Modern is just cleaner!**

---

**You don't need to master everything!**

**Know enough to:**
- Read modern code ✅
- Use common patterns ✅
- Ask "what does this do?" ✅
- Fix simple issues ✅

**The rest comes with practice!**

---

## Examples from RTA

### Get Block Names

```java
// Old way (what you know)
List<Block> blocks = blockRepository.findAll();
List<String> names = new ArrayList<>();
for (Block block : blocks) {
    names.add(block.getShortName());
}
Collections.sort(names);

// Modern way (what we use)
List<String> names = blockRepository.findAll()
    .stream()
    .map(Block::getShortName)
    .distinct()
    .sorted()
    .collect(Collectors.toList());
```

**Same result! Modern is more concise!**

---

### Filter Active Members

```java
// Old way
List<Member> active = new ArrayList<>();
for (Member m : memberRepository.findAll()) {
    if (m.getStatus().equals("ACTIVE")) {
        active.add(m);
    }
}

// Modern way
List<Member> active = memberRepository.findAll()
    .stream()
    .filter(m -> m.getStatus().equals("ACTIVE"))
    .collect(Collectors.toList());
```

---

## When to Use What

**Use lambdas when:**
- Passing simple functions to methods
- Making code more concise
- Working with streams

**Use method references when:**
- Lambda just calls one method
- Want even more concise code

**Use streams when:**
- Processing collections
- Want functional style
- Need to chain operations

**Use old style when:**
- More comfortable with it
- Code is clearer that way
- Simple operations

**Both are valid! Modern isn't always better!**

**Use what makes sense to you!**

---

**End of Reference**

**Dale - you now have the complete modern Java syntax reference!** 🎯

**Bookmark this for when you need it!** 📚
