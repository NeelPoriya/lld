# LLD Interview Problems — Java

A curated set of **Low-Level Design** solutions built for interview preparation. Every problem ships with:

- **`DESIGN.md`** next to the code — the exact way to attack the problem in an interview (clarify requirements → core entities → class diagram → patterns → concurrency).
- **Heavily commented, production-grade Java** using the design patterns interviewers look for.
- **JUnit 5 tests** that demonstrate *testable* design (injected clocks, seeded randomness, interfaces over concretions).

## Build & test

```powershell
# JAVA_HOME is set inline because the machine's env var is stale.
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.2'
& "$env:USERPROFILE\scoop\apps\maven\current\bin\mvn.cmd" -f (Resolve-Path .\pom.xml) test
```

- Java 25, Maven 3.9.16, JUnit 5.11.3.

## Reading guide — the marker comments
Scan the code for these tags; they call out exactly what earns points in an interview:

| Tag | Meaning |
| --- | --- |
| `// DESIGN PATTERN:` | The pattern being applied and the reason. |
| `// INTERVIEW INSIGHT:` | A point that impresses interviewers. |
| `// CONCURRENCY:` | Thread-safety reasoning, race conditions, locking. |
| `// TESTABILITY:` | Dependency injection for deterministic tests. |
| `// EXTENSIBILITY:` | How to extend behaviour without editing existing code. |

## Problem index

### Easy
| # | Problem | Package |
| - | ------- | ------- |
| 1 | Parking Lot | `in.neelporiya.parkinglot` |
| 2 | Stack Overflow | `in.neelporiya.stackoverflow` |
| 3 | Vending Machine | `in.neelporiya.vendingmachine` |
| 4 | Logging Framework | `in.neelporiya.loggingframework` |
| 5 | Traffic Signal Control System | `in.neelporiya.trafficsignal` |
| 6 | Coffee Vending Machine | `in.neelporiya.coffeevendingmachine` |
| 7 | Task Management System | `in.neelporiya.taskmanagement` |

### Medium
| # | Problem | Package |
| - | ------- | ------- |
| 8 | ATM | `in.neelporiya.atm` |
| 9 | LinkedIn | `in.neelporiya.linkedin` |
| 10 | LRU Cache | `in.neelporiya.lrucache` |
| 11 | Tic Tac Toe | `in.neelporiya.tictactoe` |
| 12 | Pub Sub System | `in.neelporiya.pubsub` |
| 13 | Elevator System | `in.neelporiya.elevator` |
| 14 | Car Rental System | `in.neelporiya.carrental` |
| 15 | Online Auction System | `in.neelporiya.auction` |
| 16 | Hotel Management System | `in.neelporiya.hotelmanagement` |
| 17 | Digital Wallet Service | `in.neelporiya.digitalwallet` |
| 18 | Airline Management System | `in.neelporiya.airline` |
| 19 | Library Management System | `in.neelporiya.librarymanagement` |
| 20 | Social Network (Facebook) | `in.neelporiya.socialnetwork` |
| 21 | Restaurant Management System | `in.neelporiya.restaurant` |
| 22 | Concert Ticket Booking System | `in.neelporiya.concertbooking` |

### Hard
| # | Problem | Package |
| - | ------- | ------- |
| 23 | CricInfo | `in.neelporiya.cricinfo` |
| 24 | Splitwise | `in.neelporiya.splitwise` |
| 25 | Chess Game | `in.neelporiya.chess` |
| 26 | Snake and Ladder | `in.neelporiya.snakeandladder` |
| 27 | Ride-Sharing (Uber) | `in.neelporiya.ridesharing` |
| 28 | Course Registration System | `in.neelporiya.courseregistration` |
| 29 | Movie Ticket Booking System | `in.neelporiya.movieticket` |
| 30 | Online Shopping (Amazon) | `in.neelporiya.onlineshopping` |
| 31 | Online Stock Brokerage System | `in.neelporiya.stockbrokerage` |
| 32 | Music Streaming (Spotify) | `in.neelporiya.musicstreaming` |
| 33 | Online Food Delivery (Swiggy) | `in.neelporiya.fooddelivery` |
| 34 | Facebook Search | `in.neelporiya.facebooksearch` |
| 35 | Meeting Scheduler | `in.neelporiya.meetingscheduler` |
| 36 | Task/Job Scheduler | `in.neelporiya.jobscheduler` |
| 37 | Connection Pool | `in.neelporiya.connectionpool` |
| 38 | Stock Trading System | `in.neelporiya.stocktrading` |
