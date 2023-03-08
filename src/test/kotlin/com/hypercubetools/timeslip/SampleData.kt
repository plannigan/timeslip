@file:JvmName("SampleData")

package com.hypercubetools.timeslip

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@JvmField
val SOME_INSTANT: Instant = Instant.ofEpochSecond(1548723723)

@JvmField
val SOME_OTHER_INSTANT: Instant = Instant.ofEpochSecond(1549166706)

@JvmField
val SOME_DURATION: Duration = Duration.ofSeconds(5)

@JvmField
val SOME_INSTANT_SOME_DURATION_LATER: Instant = SOME_INSTANT.plus(SOME_DURATION)

@JvmField
val SOME_INSTANT_TWO_SOME_DURATION_LATER: Instant = SOME_INSTANT.plus(SOME_DURATION).plus(
    SOME_DURATION
)

@JvmField
val SOME_ZONE_ID: ZoneId = ZoneOffset.ofHours(1)

@JvmField
val SOME_OTHER_ZONE_ID: ZoneId = ZoneOffset.ofHours(4)

fun zoneProducer() =
    arrayOf(SOME_OTHER_ZONE_ID, ZoneOffset.ofHours(1), ZoneOffset.ofHours(3), ZoneOffset.ofHours(-4))

fun durationProducer() = arrayOf(
    SOME_DURATION,
    Duration.ofSeconds(15),
    Duration.ofHours(5),
    Duration.ofSeconds(-5),
    Duration.ofHours(-25)
)

fun instantProducer() = arrayOf(
    SOME_INSTANT,
    SOME_INSTANT_SOME_DURATION_LATER,
    SOME_OTHER_INSTANT,
    Instant.EPOCH,
    Instant.EPOCH,
    Instant.MIN,
    Instant.MAX
)
