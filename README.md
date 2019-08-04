# TimeSlip: Time manipulation for JVM test cases

TimeSlip allows test cases to manipulate time by providing a concrete `java.time.Clock` implementation that will operate
in a deterministic way, independent of the actual passage of time.

The TimeSlip API is specifically designed to be easy to use from Kotlin and Java code.

[![Kotlin](https://img.shields.io/badge/kotlin-1.3.31-blue.svg)](http://kotlinlang.org)
[![Bintray](https://img.shields.io/bintray/v/plannigan/com.hypercubetools/timeslip.svg?color=blue&label=jcenter)](https://bintray.com/plannigan/com.hypercubetools/timeslip/_latestVersion)
[![CircleCI](https://circleci.com/gh/plannigan/timeslip.svg?style=svg)](https://circleci.com/gh/plannigan/timeslip)

## Installation

Releases are published to bintray [jcenter][jcenter].
 
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

## Detailed Documentation

Find more information about the API and how to use it on the [project website][project_website]

## Examples

```kotlin
fun formatTime(clock: Clock) =
    DateTimeFormatter.ISO_TIME.withZone(clock.zone).format(clock.instant())

val constClock = TimeSlip.at(Instant.parse("2007-12-03T10:15:30.00Z"))
println(formatTime(constClock))
println(formatTime(constClock))
println(formatTime(constClock))

// 10:15:30Z
// 10:15:30Z
// 10:15:30Z

val tickingClock = TimeSlip.startAt(Instant.parse("2007-12-03T10:15:30.00Z"))
println(formatTime(tickingClock))
println(formatTime(tickingClock))
println(formatTime(tickingClock))

// 10:15:30Z
// 10:15:31Z
// 10:15:32Z

val sequenceClock = TimeSlip.sequence {
    first(Instant.parse("2007-12-03T10:15:30.00Z"))
    then(Instant.parse("2007-12-03T10:16:00.00Z"), Instant.parse("2007-12-03T10:17:00.00Z"))
}
println(formatTime(sequenceClock))
println(formatTime(sequenceClock))
println(formatTime(sequenceClock))

// 10:15:30Z
// 10:16:00Z
// 10:17:00Z
```

[jcenter]: https://bintray.com/plannigan/com.hypercubetools/timeslip
[project_website]: https://timeslip.hypercubetools.com/
