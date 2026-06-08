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

- [ ] `extends` keyword — **single inheritance only**
- [ ] `super(...)` for parent constructor / `super.method()` for parent method
- [ ] Method **overriding** vs **overloading** — and the `@Override` annotation
- [ ] `final` classes and methods (sealing inheritance)
- [ ] Constructor execution order (parent → child)
- [ ] Field hiding (avoid it — fields are not polymorphic)
- [ ] `Object` methods you commonly override

**Comparison:** Java has no multiple inheritance of classes (unlike C++), but allows multiple **interface** implementation (like C#).

**Exercise:** Model a small `Shape` hierarchy (`Circle`, `Rectangle`, `Triangle`) with `area()` overridden in each.

- [ ] **Phase 4 Done**

---

## Phase 5 — Polymorphism

**Goal:** Use runtime and compile-time polymorphism idiomatically.

- [ ] **Dynamic dispatch** — methods are virtual by default in Java (opposite of C#/C++)
- [ ] Upcasting / downcasting and `instanceof`
- [ ] **Pattern matching for `instanceof`** (Java 16+)
- [ ] **Switch pattern matching** (Java 21)
- [ ] Method overloading resolution rules
- [ ] Covariant return types

**Critical mindset shift:** In C++/C# you opt **in** to virtual. In Java you opt **out** with `final`. Plan your class designs accordingly.

**Exercise:** Extend the `Shape` hierarchy with a `describe(Object o)` method that uses pattern matching with `instanceof` and `switch`.

- [ ] **Phase 5 Done**

---

## Phase 6 — Abstraction (Abstract Classes & Interfaces)

**Goal:** Choose the right abstraction tool.

- [ ] `abstract` classes and methods
- [ ] `interface` — methods are implicitly `public abstract`
- [ ] **Default methods** in interfaces (Java 8+)
- [ ] **Static methods** in interfaces
- [ ] **Private methods** in interfaces (Java 9+)
- [ ] Multiple interface inheritance & the **diamond problem** resolution
- [ ] `Comparable<T>` vs `Comparator<T>`
- [ ] **Sealed interfaces** (Java 17+) for closed type hierarchies
- [ ] Functional interfaces (`@FunctionalInterface`) — bridge to Phase 11

**Decision guide:**
| Need | Use |
|---|---|
| Pure contract, multiple implementers | `interface` |
| Shared state or partial implementation | `abstract class` |
| Closed set of subtypes | `sealed` |

**Exercise:** Define `Drawable` and `Resizable` interfaces; make `Shape` implement both. Add a `Comparator<Shape>` that sorts by area.

- [ ] **Phase 6 Done**

---

## Phase 7 — Composition, Aggregation & Association

**Goal:** Prefer composition over inheritance.

- [ ] Composition: strong "has-a" (lifetime owned)
- [ ] Aggregation: weak "has-a" (shared lifetime)
- [ ] Association: "uses-a"
- [ ] Delegation pattern
- [ ] Dependency injection by constructor

**Exercise:** Refactor a small inheritance-heavy design (e.g., `Employee → Manager → Director`) into composition with role objects.

- [ ] **Phase 7 Done**

---

## Phase 8 — Generics

**Goal:** Write type-safe generic code and understand its limits.

- [ ] Generic classes, methods, and constructors
- [ ] **Type erasure** — what survives at runtime and what doesn't
- [ ] Bounded type parameters (`<T extends Number>`)
- [ ] Wildcards: `?`, `? extends T`, `? super T`
- [ ] **PECS rule** — Producer Extends, Consumer Super
- [ ] Generic restrictions: no `new T()`, no `T[]`, no primitives
- [ ] Raw types (and why to avoid them)

**Big difference vs C#:** Java generics are **erased** at runtime — you can't do `if (x instanceof List<String>)`. C# has reified generics.

**Exercise:** Implement a generic `Pair<A, B>` and a generic `Stack<T>` backed by an array (deal with the array-creation quirk).

- [ ] **Phase 8 Done**

---

## Phase 9 — Collections Framework

**Goal:** Know what to reach for and why.

- [ ] `Collection` hierarchy: `List`, `Set`, `Queue`, `Deque`, `Map`
- [ ] Implementations: `ArrayList`, `LinkedList`, `HashSet`, `TreeSet`, `LinkedHashSet`, `HashMap`, `TreeMap`, `LinkedHashMap`, `ArrayDeque`, `PriorityQueue`
- [ ] `Iterator`, `ListIterator`, and `for-each`
- [ ] Concurrent collections: `ConcurrentHashMap`, `CopyOnWriteArrayList`
- [ ] Immutable factories: `List.of(...)`, `Map.of(...)`, `Set.of(...)`
- [ ] `Collections` utility class (`sort`, `reverse`, `unmodifiableList`)
- [ ] Big-O of common operations

**Comparison:** `ArrayList<T>` ≈ `List<T>` (C#) ≈ `std::vector<T>` (C++). `HashMap<K,V>` ≈ `Dictionary<K,V>` ≈ `std::unordered_map`.

**Exercise:** Write a word-frequency counter that reads a file and prints the top 10 words using `HashMap` + sorting.

- [ ] **Phase 9 Done**

---

## Phase 10 — Exception Handling

**Goal:** Handle errors idiomatically.

- [ ] Exception hierarchy: `Throwable` → `Error` / `Exception` → `RuntimeException`
- [ ] **Checked vs unchecked** exceptions — Java's unique stance
- [ ] `try` / `catch` / `finally` / **try-with-resources** (`AutoCloseable`)
- [ ] Multi-catch (`catch (IOException | SQLException e)`)
- [ ] `throws` clause in method signatures
- [ ] Custom exception classes
- [ ] When to wrap vs. rethrow

**Critical difference:** Checked exceptions force callers to handle or declare them — there is no C# / C++ equivalent. Many modern Java libs avoid them; be deliberate.

**Exercise:** Write a file reader that uses try-with-resources and defines a custom `ConfigParseException`.

- [ ] **Phase 10 Done**

---

## Phase 11 — Functional Java (Lambdas & Streams)

**Goal:** Write expressive, declarative code.

- [ ] Lambda syntax: `(x, y) -> x + y`
- [ ] Method references: `String::length`, `System.out::println`
- [ ] Built-in functional interfaces: `Function`, `Predicate`, `Consumer`, `Supplier`, `BiFunction`
- [ ] `Optional<T>` — proper use (no `.get()` without a check)
- [ ] Stream API: `filter`, `map`, `flatMap`, `reduce`, `collect`
- [ ] `Collectors`: `toList`, `toMap`, `groupingBy`, `partitioningBy`
- [ ] Parallel streams — when (rarely) to use them
- [ ] Stream gotchas: laziness, single consumption

**C# analogy:** LINQ → Stream. `IEnumerable<T>` → `Stream<T>`. `Func<T, TResult>` → `Function<T, R>`. `Nullable<T>` → `Optional<T>` (but only for references).

**Exercise:** Re-implement the word-frequency counter from Phase 9 using Streams in one expression.

- [ ] **Phase 11 Done**

---

## Phase 12 — Concurrency Basics

**Goal:** Understand the JVM concurrency model.

- [ ] `Thread`, `Runnable`, `Callable<V>`
- [ ] `synchronized`, `volatile`, the Java Memory Model (high level)
- [ ] `ExecutorService` and thread pools
- [ ] `Future<V>` and `CompletableFuture<V>`
- [ ] `java.util.concurrent` primitives: `Lock`, `ReentrantLock`, `Semaphore`, `CountDownLatch`
- [ ] **Virtual threads** (Java 21) — Project Loom
- [ ] Common bugs: race conditions, deadlocks, visibility

**Comparison:** `CompletableFuture` ≈ `Task<T>` (C#). Virtual threads ≈ a different model — closer to goroutines than to `async/await`.

**Exercise:** Build a parallel URL fetcher using `CompletableFuture` and an `ExecutorService`.

- [ ] **Phase 12 Done**

---

## Phase 13 — I/O, Files & Modern APIs

**Goal:** Use modern I/O APIs.

- [ ] Legacy `java.io` vs modern `java.nio.file`
- [ ] `Path`, `Files`, `Files.readAllLines`, `Files.lines` (stream)
- [ ] Reading/writing JSON (use **Jackson** or **Gson**)
- [ ] HTTP client (`java.net.http.HttpClient`, Java 11+)
- [ ] Date/time API (`java.time.*`) — never use `java.util.Date`

**Exercise:** Fetch JSON from a public API (e.g., GitHub) and deserialize it into a record.

- [ ] **Phase 13 Done**

---

## Phase 14 — Testing with JUnit

**Goal:** Write tests as you go.

- [ ] **JUnit 5** basics: `@Test`, `@BeforeEach`, `@AfterEach`, `@DisplayName`
- [ ] Assertions: `assertEquals`, `assertThrows`, `assertAll`
- [ ] Parameterized tests (`@ParameterizedTest`, `@ValueSource`, `@MethodSource`)
- [ ] **Mockito** basics: `mock()`, `when().thenReturn()`, `verify()`
- [ ] **AssertJ** for fluent assertions (optional but idiomatic)
- [ ] Test-driven loop: red → green → refactor

**Exercise:** Add JUnit tests for the `Shape` hierarchy and the word-frequency counter.

- [ ] **Phase 14 Done**

---

## Phase 15 — Build Tools, Packaging & Ecosystem

**Goal:** Ship a real Java project.

- [ ] Maven `pom.xml` structure: `groupId`, `artifactId`, dependencies, plugins
- [ ] Gradle `build.gradle(.kts)` equivalents
- [ ] Dependency scopes (`compile`, `test`, `provided`)
- [ ] Building a JAR (`mvn package`) and running it (`java -jar`)
- [ ] **Java Modules (JPMS)** — module-info.java basics
- [ ] Logging: **SLF4J + Logback** (the de-facto standard)
- [ ] Skim **Spring Boot** to understand the dominant framework

**Exercise:** Convert one of your practice projects to Maven with at least one external dependency (Jackson or Gson).

- [ ] **Phase 15 Done**

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

---

### Overall Completion

- [x] Phase 0  &nbsp;&nbsp;·&nbsp;&nbsp; Setup
- [x] Phase 1  &nbsp;&nbsp;·&nbsp;&nbsp; Language Basics
- [x] Phase 2  &nbsp;&nbsp;·&nbsp;&nbsp; Classes & Objects
- [x] Phase 3  &nbsp;&nbsp;·&nbsp;&nbsp; Encapsulation
- [ ] Phase 4  &nbsp;&nbsp;·&nbsp;&nbsp; Inheritance
- [ ] Phase 5  &nbsp;&nbsp;·&nbsp;&nbsp; Polymorphism
- [ ] Phase 6  &nbsp;&nbsp;·&nbsp;&nbsp; Abstraction
- [ ] Phase 7  &nbsp;&nbsp;·&nbsp;&nbsp; Composition
- [ ] Phase 8  &nbsp;&nbsp;·&nbsp;&nbsp; Generics
- [ ] Phase 9  &nbsp;&nbsp;·&nbsp;&nbsp; Collections
- [ ] Phase 10 &nbsp;·&nbsp; Exceptions
- [ ] Phase 11 &nbsp;·&nbsp; Lambdas & Streams
- [ ] Phase 12 &nbsp;·&nbsp; Concurrency
- [ ] Phase 13 &nbsp;·&nbsp; I/O & Modern APIs
- [ ] Phase 14 &nbsp;·&nbsp; Testing
- [ ] Phase 15 &nbsp;·&nbsp; Build & Ecosystem
- [ ] Phase 16 &nbsp;·&nbsp; Capstone
- [ ] Phase 17 &nbsp;·&nbsp; Patterns
- [ ] Phase 18 &nbsp;·&nbsp; SOLID
