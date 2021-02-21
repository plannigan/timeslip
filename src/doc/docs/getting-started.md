# Getting started

## Installation

Releases are published to [maven central][maven].

[![MavenCentral](https://img.shields.io/maven-central/v/com.hypercubetools/timeslip)][maven_latest]
 
It can be included in your project by including the following in your project's build configuration.

```gradle
dependencies {
    testImplementation 'com.hypercubetools:timeslip:0.1.0'
}
```

```maven
<dependency>
  <groupId>com.hypercubetools</groupId>
  <artifactId>timeslip</artifactId>
  <version>0.1.0</version>
  <type>pom</type>
</dependency>
```

## Usage Examples

Let's look at a class which uses a `Clock` as part of the implementation.

```kotlin
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

class Greeter(private val _clock: Clock = Clock.systemDefaultZone()) {
    fun sayHello(name :String) = if (beforeNoon())  "Morning $name" else "Afternoon $name"
    
    fun displayTime() = 
        println(DateTimeFormatter.ISO_TIME.withZone(_clock.zone).format(_clock.instant()))
    
    private fun beforeNoon() = 
        LocalDateTime.ofInstant(_clock.instant(), _clock.zone).get(ChronoField.AMPM_OF_DAY) == 0
}
```

```java
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

class Greeter {
    private final Clock _clock;
    
    public Greeter() {
        this(Clock.systemDefaultZone());
    }
    
    public Greeter(Clock clock) {
        _clock = clock;
    }
    
    public String sayHello(String name) {
        if (beforeNoon()) {
            return "Morning " + name;
        }
        return "Afternoon " + name;
    }
    
    public void displayTime() {
        System.out.println(DateTimeFormatter.ISO_TIME.withZone(_clock.zone).format(_clock.instant()));
    }

    private boolean beforeNoon() {
        return LocalDateTime.ofInstant(_clock.instant(), _clock.getZone()).get(ChronoField.AMPM_OF_DAY) == 0;
    } 
}
```

Normally a `Greeter` would be created with the no argument constructor. Since the `Clock` returned by
`Clock.systemDefaultZone()` is non-deterministic, it is not suitable for automated testing. `TimeSlip` produces a
`Clock` that can be configured to operated in a known way.

```kotlin
import java.time.Instant
import com.hypercubetools.timeslip.TimeSlip

fun main() {
    val greeter = Greeter(TimeSlip.at(Instant.parse("2007-12-03T10:15:30.00Z")))

    println(greeter.sayHello("Alice"))
    println(greeter.sayHello("Bob"))
}
```

```java
import java.time.Instant;
import com.hypercubetools.timeslip.TimeSlip;

class Example {
    public static void main(String[] args) {
        Greeter greeter = Greeter(TimeSlip.at(Instant.parse("2007-12-03T10:15:30.00Z")));

        System.out.println(greeter.sayHello("Alice"));
        System.out.println(greeter.sayHello("Bob"));
    }
}
```

This code will always produce the following output.

```
Mourning Alice
Mourning Bob
```

However, since applications expect time to progress forward, it is useful to produce a `Clock` that moves in a
deterministic way.

```kotlin
import java.time.Duration
import java.time.Instant
import com.hypercubetools.timeslip.TimeSlip

fun main() {
    val greeter = Greeter(TimeSlip.startAt(Instant.parse("2007-12-03T10:15:30.00Z"), tickAmount = Duration.ofMinutes(2)))

    greeter.displayTime()
    greeter.displayTime()
    greeter.displayTime()
}
```

```java
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import com.hypercubetools.timeslip.TimeSlip;

class Example {
    public static void main(String[] args) {
        Greeter greeter = new Greeter(TimeSlip.startAt(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneOffset.UTC, Duration.ofMinutes(2)));

        greeter.displayTime();
        greeter.displayTime();
        greeter.displayTime();
    }
}
```

This code will always produce the following output.

```
10:15:30Z
10:17:30Z
10:19:30Z
```

Read the [Usage Guide][usage_guide] to see more detailed descriptions of the ways `TimeSlip` can be configured.

[maven]: https://mvnrepository.com/artifact/com.hypercubetools/timeslip
[maven_latest]: https://mvnrepository.com/artifact/com.hypercubetools/timeslip/latest
[usage_guide]: usage-guide.md
