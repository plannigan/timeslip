# TimeSlip: Time manipulation for JVM test cases

TimeSlip allows test cases to manipulate time by providing a concrete `java.time.Clock` implementation that will operate
in a deterministic way, independent of the actual passage of time.

The TimeSlip API is specifically designed to be easy to use from Kotlin and Java code.

[![Kotlin](https://img.shields.io/badge/kotlin-1.3.31-blue.svg)](http://kotlinlang.org)
[![Bintray](https://img.shields.io/bintray/v/plannigan/com.hypercubetools/timeslip.svg?color=blue&label=jcenter)](https://bintray.com/plannigan/com.hypercubetools/timeslip/_latestVersion)
[![CircleCI](https://circleci.com/gh/plannigan/timeslip.svg?style=svg)](https://circleci.com/gh/plannigan/timeslip)
[![codecov](https://codecov.io/gh/plannigan/timeslip/branch/master/graph/badge.svg)](https://codecov.io/gh/plannigan/timeslip)

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

val ticking2MinClock = TimeSlip.startAt(
    Instant.parse("2007-12-03T10:15:30.00Z"),
    tickAmount = Duration.ofMinutes(2)
)
println(formatTime(ticking2MinClock))
println(formatTime(ticking2MinClock))
println(formatTime(ticking2MinClock))

// 10:15:30Z
// 10:17:30Z
// 10:19:30Z

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

## Where to Start?

To learn the basics of how to start using TimeSlip, read the [Getting Started][getting_started] page.

## Detailed Documentation

To learn more about the various ways TimeSlip can be configured, read the [Usage Guide][usage_guide] page.

## API Reference

To find specific information about a specific function, read the [API Reference][api_reference]. Most of the API
exists on the [TimeSlip][timeslip_api] page.

[getting_started]: getting-started.md
[usage_guide]: usage-guide.md
[timeslip_api]: api/com.hypercubetools.timeslip/-time-slip/index.md
[api_reference]: api/index.md
