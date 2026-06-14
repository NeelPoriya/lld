# Java + OOP Refresher Plan

> A structured learning plan to refresh **Object-Oriented Programming** fundamentals while learning **Java**, tailored for a developer experienced in **C#** and **C++**.
>
> Use the checkboxes to track progress. Each phase contains: **Concepts → Java specifics → C#/C++ comparison → Practice exercise**.

---

## Table of Contents

1. [How to Use This Plan](#how-to-use-this-plan)
2. [Environment Setup](#phase-0--environment-setup)
3. [Java Language Basics](#phase-1--java-language-basics)
4. [Classes & Objects (Pillar 0)](#phase-2--classes--objects-pillar-0)
5. [Encapsulation](#phase-3--encapsulation)
6. [Inheritance](#phase-4--inheritance)
7. [Polymorphism](#phase-5--polymorphism)
8. [Abstraction (Abstract Classes & Interfaces)](#phase-6--abstraction-abstract-classes--interfaces)
9. [Composition, Aggregation & Association](#phase-7--composition-aggregation--association)
10. [Generics](#phase-8--generics)
11. [Collections Framework](#phase-9--collections-framework)
12. [Exception Handling](#phase-10--exception-handling)
13. [Functional Java (Lambdas & Streams)](#phase-11--functional-java-lambdas--streams)
14. [Concurrency Basics](#phase-12--concurrency-basics)
15. [I/O, Files & Modern APIs](#phase-13--io-files--modern-apis)
16. [Testing with JUnit](#phase-14--testing-with-junit)
17. [Build Tools, Packaging & Ecosystem](#phase-15--build-tools-packaging--ecosystem)
18. [Capstone Project](#phase-16--capstone-project)
19. [Design Patterns Refresher](#phase-17--design-patterns-refresher)
20. [SOLID & Clean Code](#phase-18--solid--clean-code)
21. [Resources](#resources)
22. [Progress Log](#progress-log)

---

## How to Use This Plan

- Work top-down; each phase builds on the previous.
- For **every phase**: read → write a tiny sample → do the exercise → commit to a `practice/` folder.
- Maintain a `notes/` folder with one markdown file per phase summarizing what you learned.
- When a concept maps cleanly to C# or C++, write the analogy in your notes — that's where retention sticks.
- Mark `[x]` as you complete each item. Tick the **Phase Done** box only after the exercise compiles and runs.

Suggested workspace layout:

```
temp/
├── README.md                  <- this file
├── notes/
│   ├── phase-01-basics.md
│   └── ...
└── practice/
    ├── phase-01/
    ├── phase-02/
    └── ...
```

---

## Phase 0 — Environment Setup

**Goal:** Have a working Java toolchain and IDE workflow.

- [x] Install **JDK 21 (LTS)** — verify with `java -version` and `javac -version`
- [x] Install **Maven** and/or **Gradle** — verify with `mvn -v` / `gradle -v`
- [x] Install VS Code **Extension Pack for Java** (or use IntelliJ IDEA Community)
- [x] Configure `JAVA_HOME` environment variable
- [x] Create and run a `HelloWorld.java` from the command line (`javac` + `java`)
- [x] Create the same project using Maven (`mvn archetype:generate`)
- [x] Get familiar with debugging in VS Code/IntelliJ (breakpoints, watch, step-into)

**C# / C++ analogy:** JDK ≈ .NET SDK / C++ toolchain. Maven/Gradle ≈ `dotnet`/MSBuild or CMake. `javac` ≈ `csc`/`g++`. JVM ≈ CLR.

- [x] **Phase 0 Done**

---

## Phase 1 — Java Language Basics

**Goal:** Read and write idiomatic Java syntax confidently.

- [x] Primitive types: `byte`, `short`, `int`, `long`, `float`, `double`, `char`, `boolean`
- [x] Wrapper classes & **autoboxing** (`Integer`, `Double`, …)
- [x] `String` immutability, `StringBuilder`, `String.format`, text blocks (`"""`)
- [x] Operators, control flow (`if`, `switch` expressions, loops)
- [x] Arrays (`int[] a = new int[5];`) vs. collections
- [x] `var` (local-variable type inference)
- [x] Packages and `import` statements
- [x] `public static void main(String[] args)` — entry point conventions

**Comparison cheat sheet:**

| C# | Java |
|---|---|
| `string` | `String` (capital S) |
| `bool` | `boolean` |
| `using` | `import` |
| `namespace` | `package` |
| `Console.WriteLine` | `System.out.println` |
| properties | explicit `get`/`set` methods (or records) |

**Exercise:** Write a small CLI that reads numbers from `args`, sums them, and prints both the integer sum and a formatted message using a text block.

- [x] **Phase 1 Done**

---

## Phase 2 — Classes & Objects (Pillar 0)

**Goal:** Understand Java's object model.

- [x] Class declaration, fields, methods, constructors
- [x] **Default constructor** rules
- [x] `this` reference and constructor chaining (`this(...)`)
- [x] `static` fields and methods (class-level state)
- [x] `final` on variables, methods, classes
- [x] **Everything (except primitives) is a reference** — pass-by-value of references
- [x] `Object` class — every class extends it implicitly
- [x] Override `toString()`, `equals()`, `hashCode()` — and **why they must agree**
- [x] **Records** (Java 16+) — concise immutable data classes

**Key gotcha:** Java has no value types (yet) — no `struct`. Even `Integer` is heap-allocated. Use primitives for performance, wrappers for collections.

**Exercise:** Build a `Point` class and a `Point` record. Implement `equals`/`hashCode` manually for the class; observe the record gives them for free.

- [x] **Phase 2 Done**

---

## Phase 3 — Encapsulation

**Goal:** Master access control and data hiding.

- [x] Access modifiers: `public`, `protected`, **package-private (default)**, `private`
- [x] Getter/setter conventions (no language properties like C#)
- [x] Immutability patterns (`final` fields, defensive copies)
- [x] Builder pattern for objects with many fields
- [x] **Nested classes**: static nested, inner, local, anonymous
- [x] Sealed classes (Java 17+) — restrict who may extend

**Gotcha:** Java's default access is **package-private**, not `internal` like C# and not `private` like C++.

**Exercise:** Implement an immutable `Money(amount, currency)` class with a `Builder`; ensure no mutation is possible.

- [x] **Phase 3 Done**

---

## Phase 4 — Inheritance

**Goal:** Use inheritance correctly and sparingly.

- [x] `extends` keyword — **single inheritance only**
- [x] `super(...)` for parent constructor / `super.method()` for parent method
- [x] Method **overriding** vs **overloading** — and the `@Override` annotation
- [x] `final` classes and methods (sealing inheritance)
- [x] Constructor execution order (parent → child)
- [x] Field hiding (avoid it — fields are not polymorphic)
- [x] `Object` methods you commonly override

**Comparison:** Java has no multiple inheritance of classes (unlike C++), but allows multiple **interface** implementation (like C#).

**Exercise:** Model a small `Shape` hierarchy (`Circle`, `Rectangle`, `Triangle`) with `area()` overridden in each.

- [x] **Phase 4 Done**

---

## Phase 5 — Polymorphism

**Goal:** Use runtime and compile-time polymorphism idiomatically.

- [x] **Dynamic dispatch** — methods are virtual by default in Java (opposite of C#/C++)
- [x] Upcasting / downcasting and `instanceof`
- [x] **Pattern matching for `instanceof`** (Java 16+)
- [x] **Switch pattern matching** (Java 21)
- [x] Method overloading resolution rules
- [x] Covariant return types

**Critical mindset shift:** In C++/C# you opt **in** to virtual. In Java you opt **out** with `final`. Plan your class designs accordingly.

**Exercise:** Extend the `Shape` hierarchy with a `describe(Object o)` method that uses pattern matching with `instanceof` and `switch`.

- [x] **Phase 5 Done**

---

## Phase 6 — Abstraction (Abstract Classes & Interfaces)

**Goal:** Choose the right abstraction tool.

- [x] `abstract` classes and methods
- [x] `interface` — methods are implicitly `public abstract`
- [x] **Default methods** in interfaces (Java 8+)
- [x] **Static methods** in interfaces
- [x] **Private methods** in interfaces (Java 9+)
- [x] Multiple interface inheritance & the **diamond problem** resolution
- [x] `Comparable<T>` vs `Comparator<T>`
- [x] **Sealed interfaces** (Java 17+) for closed type hierarchies
- [x] Functional interfaces (`@FunctionalInterface`) — bridge to Phase 11

**Decision guide:**
| Need | Use |
|---|---|
| Pure contract, multiple implementers | `interface` |
| Shared state or partial implementation | `abstract class` |
| Closed set of subtypes | `sealed` |

**Exercise:** Define `Drawable` and `Resizable` interfaces; make `Shape` implement both. Add a `Comparator<Shape>` that sorts by area.

- [x] **Phase 6 Done**

---

## Phase 7 — Composition, Aggregation & Association

**Goal:** Prefer composition over inheritance.

- [x] Composition: strong "has-a" (lifetime owned)
- [x] Aggregation: weak "has-a" (shared lifetime)
- [x] Association: "uses-a"
- [x] Delegation pattern
- [x] Dependency injection by constructor

**Exercise:** Refactor a small inheritance-heavy design (e.g., `Employee → Manager → Director`) into composition with role objects.

- [x] **Phase 7 Done**

---

## Phase 8 — Generics

**Goal:** Write type-safe generic code and understand its limits.

- [x] Generic classes, methods, and constructors
- [x] **Type erasure** — what survives at runtime and what doesn't
- [x] Bounded type parameters (`<T extends Number>`)
- [x] Wildcards: `?`, `? extends T`, `? super T`
- [x] **PECS rule** — Producer Extends, Consumer Super
- [x] Generic restrictions: no `new T()`, no `T[]`, no primitives
- [x] Raw types (and why to avoid them)

**Big difference vs C#:** Java generics are **erased** at runtime — you can't do `if (x instanceof List<String>)`. C# has reified generics.

**Exercise:** Implement a generic `Pair<A, B>` and a generic `Stack<T>` backed by an array (deal with the array-creation quirk).

- [x] **Phase 8 Done**

---

## Phase 9 — Collections Framework

**Goal:** Know what to reach for and why.

- [x] `Collection` hierarchy: `List`, `Set`, `Queue`, `Deque`, `Map`
- [x] Implementations: `ArrayList`, `LinkedList`, `HashSet`, `TreeSet`, `LinkedHashSet`, `HashMap`, `TreeMap`, `LinkedHashMap`, `ArrayDeque`, `PriorityQueue`
- [x] `Iterator`, `ListIterator`, and `for-each`
- [x] Concurrent collections: `ConcurrentHashMap`, `CopyOnWriteArrayList`
- [x] Immutable factories: `List.of(...)`, `Map.of(...)`, `Set.of(...)`
- [x] `Collections` utility class (`sort`, `reverse`, `unmodifiableList`)
- [x] Big-O of common operations

**Comparison:** `ArrayList<T>` ≈ `List<T>` (C#) ≈ `std::vector<T>` (C++). `HashMap<K,V>` ≈ `Dictionary<K,V>` ≈ `std::unordered_map`.

**Exercise:** Write a word-frequency counter that reads a file and prints the top 10 words using `HashMap` + sorting.

- [x] **Phase 9 Done**

---

## Phase 10 — Exception Handling

**Goal:** Handle errors idiomatically.

- [x] Exception hierarchy: `Throwable` → `Error` / `Exception` → `RuntimeException`
- [x] **Checked vs unchecked** exceptions — Java's unique stance
- [x] `try` / `catch` / `finally` / **try-with-resources** (`AutoCloseable`)
- [x] Multi-catch (`catch (IOException | SQLException e)`)
- [x] `throws` clause in method signatures
- [x] Custom exception classes
- [x] When to wrap vs. rethrow

**Critical difference:** Checked exceptions force callers to handle or declare them — there is no C# / C++ equivalent. Many modern Java libs avoid them; be deliberate.

**Exercise:** Write a file reader that uses try-with-resources and defines a custom `ConfigParseException`.

- [x] **Phase 10 Done**

---

## Phase 11 — Functional Java (Lambdas & Streams)

**Goal:** Write expressive, declarative code.

- [x] Lambda syntax: `(x, y) -> x + y`
- [x] Method references: `String::length`, `System.out::println`
- [x] Built-in functional interfaces: `Function`, `Predicate`, `Consumer`, `Supplier`, `BiFunction`
- [x] `Optional<T>` — proper use (no `.get()` without a check)
- [x] Stream API: `filter`, `map`, `flatMap`, `reduce`, `collect`
- [x] `Collectors`: `toList`, `toMap`, `groupingBy`, `partitioningBy`
- [x] Parallel streams — when (rarely) to use them
- [x] Stream gotchas: laziness, single consumption

**C# analogy:** LINQ → Stream. `IEnumerable<T>` → `Stream<T>`. `Func<T, TResult>` → `Function<T, R>`. `Nullable<T>` → `Optional<T>` (but only for references).

**Exercise:** Re-implement the word-frequency counter from Phase 9 using Streams in one expression.

- [x] **Phase 11 Done**

---

## Phase 12 — Concurrency Basics

**Goal:** Understand the JVM concurrency model.

- [x] `Thread`, `Runnable`, `Callable<V>`
- [x] `synchronized`, `volatile`, the Java Memory Model (high level)
- [x] `ExecutorService` and thread pools
- [x] `Future<V>` and `CompletableFuture<V>`
- [x] `java.util.concurrent` primitives: `Lock`, `ReentrantLock`, `Semaphore`, `CountDownLatch`
- [x] **Virtual threads** (Java 21) — Project Loom
- [x] Common bugs: race conditions, deadlocks, visibility

**Comparison:** `CompletableFuture` ≈ `Task<T>` (C#). Virtual threads ≈ a different model — closer to goroutines than to `async/await`.

**Exercise:** Build a parallel URL fetcher using `CompletableFuture` and an `ExecutorService`.

- [x] **Phase 12 Done**

---

## Phase 13 — I/O, Files & Modern APIs

**Goal:** Use modern I/O APIs.

- [x] Legacy `java.io` vs modern `java.nio.file`
- [x] `Path`, `Files`, `Files.readAllLines`, `Files.lines` (stream)
- [x] Reading/writing JSON (use **Jackson** or **Gson**)
- [x] HTTP client (`java.net.http.HttpClient`, Java 11+)
- [x] Date/time API (`java.time.*`) — never use `java.util.Date`

**Exercise:** Fetch JSON from a public API (e.g., GitHub) and deserialize it into a record.

- [x] **Phase 13 Done**

---

## Phase 14 — Testing with JUnit

**Goal:** Write tests as you go.

- [x] **JUnit 5** basics: `@Test`, `@BeforeEach`, `@AfterEach`, `@DisplayName`
- [x] Assertions: `assertEquals`, `assertThrows`, `assertAll`
- [x] Parameterized tests (`@ParameterizedTest`, `@ValueSource`, `@MethodSource`)
- [ ] **Mockito** basics: `mock()`, `when().thenReturn()`, `verify()`
- [ ] **AssertJ** for fluent assertions (optional but idiomatic)
- [x] Test-driven loop: red → green → refactor

**Exercise:** Add JUnit tests for the `Shape` hierarchy and the word-frequency counter.

- [x] **Phase 14 Done**

---

## Phase 15 — Build Tools, Packaging & Ecosystem

**Goal:** Ship a real Java project.

- [x] Maven `pom.xml` structure: `groupId`, `artifactId`, dependencies, plugins
- [x] Gradle `build.gradle(.kts)` equivalents
- [x] Dependency scopes (`compile`, `test`, `provided`)
- [x] Building a JAR (`mvn package`) and running it (`java -jar`)
- [ ] **Java Modules (JPMS)** — module-info.java basics
- [ ] Logging: **SLF4J + Logback** (the de-facto standard)
- [x] Skim **Spring Boot** to understand the dominant framework

**Exercise:** Convert one of your practice projects to Maven with at least one external dependency (Jackson or Gson).

- [x] **Phase 15 Done**

---

## Phase 16 — Capstone Project

**Goal:** Tie everything together in one non-trivial app.

**Pick one** (or invent your own):

- [ ] **Mini Library System** — REST-less CLI: add/lend/return books, persists to JSON. Uses records, generics, streams, JUnit.
- [ ] **Task Scheduler** — schedule and run tasks concurrently via `ExecutorService` / virtual threads.
- [ ] **Mini Bank** — accounts, transactions, transfers; emphasize thread safety and immutability.
- [ ] **Markdown → HTML converter** — exercises parsing, polymorphism, and the visitor pattern.

Requirements for the capstone:

- [ ] Multi-package Maven project
- [ ] At least one interface, one abstract class, one record, one sealed type
- [ ] Generics used meaningfully
- [ ] Streams used in at least one data-processing path
- [ ] JUnit tests with >70% coverage of core logic
- [ ] README in the project with build/run instructions

- [ ] **Phase 16 Done**

---

## Phase 17 — Design Patterns Refresher

**Goal:** Re-anchor classic patterns in Java.

Implement each in a tiny file under `practice/patterns/`:

- [ ] **Creational:** Singleton (enum form), Factory Method, Builder, Prototype
- [ ] **Structural:** Adapter, Decorator, Composite, Proxy, Facade
- [ ] **Behavioral:** Strategy, Observer, Template Method, Command, Iterator, Visitor, State

For each, write a one-paragraph note: *"In C# I'd do X; in Java the difference is Y."*

- [ ] **Phase 17 Done**

---

## Phase 18 — SOLID & Clean Code

**Goal:** Internalize design principles in Java idioms.

- [ ] **S**ingle Responsibility
- [ ] **O**pen/Closed
- [ ] **L**iskov Substitution
- [ ] **I**nterface Segregation
- [ ] **D**ependency Inversion
- [ ] DRY, KISS, YAGNI
- [ ] Read *Effective Java* (Joshua Bloch) — at minimum items 1–20

- [ ] **Phase 18 Done**

---

## Resources

**Books**
- *Effective Java*, 3rd ed. — Joshua Bloch **(essential)**
- *Java Concurrency in Practice* — Brian Goetz
- *Modern Java in Action* — Urma, Fusco, Mycroft
- *Head First Design Patterns* (Java edition)

**Online**
- Official tutorials: <https://dev.java/learn/>
- Oracle docs: <https://docs.oracle.com/en/java/javase/21/>
- Baeldung: <https://www.baeldung.com/>
- JEP index (language evolution): <https://openjdk.org/jeps/0>

**Practice**
- <https://exercism.org/tracks/java>
- <https://leetcode.com/> (filter by Java)

---

## Progress Log

Add a one-line entry per session. Date · phase · what you learned / got stuck on.

| Date | Phase | Notes |
|---|---|---|
| YYYY-MM-DD | 0 | Installed JDK 21, set JAVA_HOME |
| 2026-06-08 | 1 | Primitives, wrappers/boxing (Integer cache), Strings (pool/`==`), switch expressions + `var`, arrays (covariance/`ArrayStoreException`). Built menu-driven Concept/Registry runner. |
| 2026-06-08 | 2 | Classes, constructors + `this()` chaining, `static`/`final`, `Object` contract (`toString`/`equals`/`hashCode`), records, pass-by-value-of-references. |
| 2026-06-08 | 3 | Access modifiers (package-private default), defensive copies (copy in/out), Builder pattern, four nested-class kinds, sealed interface + exhaustive `switch` (ADTs). |
| 2026-06-08 | 4 | `extends`/`super`, virtual-by-default dispatch, field hiding (compile-time vs runtime resolution), ctor order, `final` classes/methods, overload resolution (widening > boxing > varargs). |
| 2026-06-09 | 5 | Up/downcasting + `ClassCastException`, pattern-matching `instanceof` (flow scoping), `switch` type patterns + `when` guards + `case null` + record deconstruction, covariant returns. |
| 2026-06-09 | 6 | Abstract class vs interface, default/static/private interface methods, diamond resolution, `Comparable` vs `Comparator` (+ `TreeSet` uses `compareTo` not `equals`), functional interfaces + lambdas. |
| 2026-06-09 | 7 | Composition vs aggregation vs association (by lifetime), delegation, refactoring inheritance → composition with swappable roles, constructor DI. |
| 2026-06-09 | 8 | Generic classes/methods, bounded type params, **type erasure** (`getClass()` equality, `Stack<T>` via `Object[]`, restrictions), wildcards + PECS (invariance vs array covariance). |
| 2026-06-09 | 9 | List/Set/Map implementations + ordering (Hash/Tree/Linked), Big-O, `list.remove(int)` index-vs-value trap, Queue/Deque/PriorityQueue, CME + `removeIf`. |
| 2026-06-09 | 10 | Throwable hierarchy, checked vs unchecked (Java-unique), try/catch/finally, try-with-resources (`AutoCloseable`), multi-catch, custom exceptions, wrap-with-cause. |
| 2026-06-09 | 11 | Stream pipeline (lazy + single-use), filter/map/reduce/collect, `Collectors` (groupingBy/partitioningBy/toMap), word-frequency, `Optional`. LINQ analogy throughout. |
| 2026-06-09 | 12 | Thread/Runnable/Callable, race condition + `AtomicInteger`/`synchronized` fixes, `ExecutorService`, `CompletableFuture` composition (join the tail!), virtual threads ≈ goroutines. |
| 2026-06-09 | 13 | NIO `Files`/`Path` (+ lazy `Files.lines`), `java.time` (use `Instant` for timestamps, never `LocalDateTime`/`Date`), `HttpClient` real GET, Jackson JSON → record (1st dependency). |
| 2026-06-09 | 14 | JUnit 5 (`@Test`/`@BeforeEach`/`@DisplayName`), `assertEquals`/`assertThrows`/`assertAll`, `@ParameterizedTest`. Ran `mvn test` → 7 green. (Mockito/AssertJ skipped.) |
| 2026-06-09 | 15 | `pom.xml` + dependency scopes (hands-on via Jackson/JUnit), maven-shade-plugin → runnable fat JAR, `java -jar` verified launching the menu. Gradle/JPMS/SLF4J/Spring read-only. |

---

### Overall Completion

- [x] Phase 0  &nbsp;&nbsp;·&nbsp;&nbsp; Setup
- [x] Phase 1  &nbsp;&nbsp;·&nbsp;&nbsp; Language Basics
- [x] Phase 2  &nbsp;&nbsp;·&nbsp;&nbsp; Classes & Objects
- [x] Phase 3  &nbsp;&nbsp;·&nbsp;&nbsp; Encapsulation
- [x] Phase 4  &nbsp;&nbsp;·&nbsp;&nbsp; Inheritance
- [x] Phase 5  &nbsp;&nbsp;·&nbsp;&nbsp; Polymorphism
- [x] Phase 6  &nbsp;&nbsp;·&nbsp;&nbsp; Abstraction
- [x] Phase 7  &nbsp;&nbsp;·&nbsp;&nbsp; Composition
- [x] Phase 8  &nbsp;&nbsp;·&nbsp;&nbsp; Generics
- [x] Phase 9  &nbsp;&nbsp;·&nbsp;&nbsp; Collections
- [x] Phase 10 &nbsp;·&nbsp; Exceptions
- [x] Phase 11 &nbsp;·&nbsp; Lambdas & Streams
- [x] Phase 12 &nbsp;·&nbsp; Concurrency
- [x] Phase 13 &nbsp;·&nbsp; I/O & Modern APIs
- [x] Phase 14 &nbsp;·&nbsp; Testing
- [x] Phase 15 &nbsp;·&nbsp; Build & Ecosystem
- [ ] Phase 16 &nbsp;·&nbsp; Capstone
- [ ] Phase 17 &nbsp;·&nbsp; Patterns
- [ ] Phase 18 &nbsp;·&nbsp; SOLID
