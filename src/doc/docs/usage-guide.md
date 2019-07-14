# Usage Guide

The TimeSlip API is specifically designed to be easy to use from Kotlin and Java code. In most cases the Kotlin API is
the same as the Java API. Unless noted, the examples below are in Kotlin.

## Manual Operations

The most basic usage is setting the clock to a specific instant. These clocks will stay at this fixed point in time
unless it is altered by the test case. 

```kotlin
val clock = TimeSlip.at(Instant.parse("2007-12-03T10:15:30.00Z")) // Defaults to UTC
val clock1 = TimeSlip.at(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneOffset.ofHours(1))
```

The clock can be moved to a new instant.

```kotlin
clock.moveTo(Instant.parse("2007-12-03T10:15:30.00Z"))
```

Or moved forward a specific amount of time.

```kotlin
clock.tick() // 1 second
clock.tick(Duration.ofHours(5))
```

In some situations, it might be useful to prevent the clock from providing any more values. Calling `clock.done()` will
cause the clock to throw `IllegalStateException` whenever any method is called.

If a test case needs to assert that the clock instance is never used. `TimeSlip.noCall()` is equivalent to creating an
instance and immediately calling `done()`.

## Automatic Operations

If the clock needs to progress forward, manually ticking the clock forward isn't possible when the time will be checked
multiple times during the execution of a single statement of the test case. TimeSlip provides a number of ways to
configure how the current instant should change after each time the current instant is accessed.

The most basic way to do that is to create a clock that starts at a specific instant and ticks forward one second each
time the current instant is checked.

```kotlin
val clock = TimeSlip.startAt(Instant.parse("2007-12-03T10:15:30.00Z")) // Defaults to 1 second and UTC
```

Just like `tick()`, a custom tick amount can be provided.

```kotlin
val clock = TimeSlip.startAt(Instant.parse("2007-12-03T10:15:30.00Z"), Duration.ofMinutes(3))
```

```java
Clock clock = TimeSlip.startAt(Instant.parse("2007-12-03T10:15:30.00Z"),  ZoneOffset.UTC, Duration.ofMinutes(3))
```

A sequence can be provided for when the clock should tick forward automatically, but not by a fixed amount.

```kotlin
val clock = TimeSlip.sequence {
    first(Instant.parse("2007-12-03T10:15:30.00Z"))
    then(Instant.parse("2007-12-03T10:16:00.00Z"), Instant.parse("2007-12-03T10:17:00.00Z"))
}
```

```java
Clock clock = TimeSlip.sequenceBuilder()
    .first(Instant.parse("2007-12-03T10:15:30.00Z"))
    .then(Instant.parse("2007-12-03T10:16:00.00Z"), Instant.parse("2007-12-03T10:17:00.00Z"))
    .build();
```

By default, after that last instant in the sequence it provided, it will not provide any more values as if `done()` was
called. Alternatively, the clock can return to the start of the sequence when `cycle` is set to `true`.

## Advanced Operations

If the clock needs to change values automatically, but the changes don't align with the functions already discussed, a
custom callback can be passed in to calculate the next instant to provide.

!!! note
    The callback to generate the next value is called while accessing the current value. This means that if the callback
    depends on external state, a change to that state will __not__ be seen in the first call to `instant()`. The change
    will be seen in the second call.

```kotlin
var large = true
val clock = TimeSlip.startAt(Instant.parse("2007-12-03T10:15:30.00Z")) {
    initial -> initial.plus(if (large) Duration.ofDays(10) else Duration.ofSeconds(10))
}

clock.instant() // 2007-12-03T10:15:30.00Z
large = false
clock.instant() // 2007-12-13T10:15:30.00Z
clock.instant() // 2007-12-13T10:15:30.10Z
```

```Java
boolean large = true;
Clock clock = TimeSlip.startAt(Instant.parse("2007-12-03T10:15:30.00Z"),
       (initial) -> {
           Duration amount;
           if (large) {
               amount = Duration.ofDays(10);
           } else {
               amount = Duration.ofDays(10);
           }
           return initial.plus(amount);
       });
}

clock.instant(); // 2007-12-03T10:15:30.00Z
large = false;
clock.instant(); // 2007-12-13T10:15:30.00Z
clock.instant(); // 2007-12-13T10:15:30.10Z
```
