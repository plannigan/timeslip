package com.hypercubetools.timeslip

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class TimeSlipTest {
    @Test
    fun `noCall() throws on instant()`() {
        val clock = TimeSlip.noCall()

        assertThat({ clock.instant() }, throws<IllegalStateException>())
    }

    @Test
    fun `noCall() throws on withZone()`() {
        val clock = TimeSlip.noCall()

        assertThat({ clock.withZone(SOME_ZONE_ID) }, throws<IllegalStateException>())
    }

    @Test
    fun `at() sets instant and zone`() {
        val clock =
            TimeSlip.at(
                SOME_INSTANT,
                SOME_ZONE_ID,
            )

        clock.assertHas(SOME_INSTANT, SOME_ZONE_ID)
    }

    @Test
    fun `withZone() changes zone for same instant`() {
        val clock =
            TimeSlip.at(
                SOME_INSTANT,
                SOME_ZONE_ID,
            )

        val zonedClock = clock.withZone(SOME_OTHER_ZONE_ID)

        zonedClock.assertHas(SOME_INSTANT, SOME_OTHER_ZONE_ID)
    }

    @ParameterizedTest
    @MethodSource("com.hypercubetools.timeslip.SampleData#zoneProducer")
    fun `withZone() matches implementation of Clock-fixed()-withZone()`(zoneId: ZoneId) {
        val clock =
            TimeSlip.at(
                SOME_INSTANT,
                SOME_ZONE_ID,
            )
        val fixedClock = Clock.fixed(SOME_INSTANT, SOME_ZONE_ID)

        val newZoneClock = clock.withZone(zoneId)

        newZoneClock.assertEqualTo(fixedClock.withZone(zoneId))
    }

    @ParameterizedTest
    @MethodSource("com.hypercubetools.timeslip.SampleData#zoneProducer")
    fun `withZone() changes zone for same instant`(zoneId: ZoneId) {
        val clock =
            TimeSlip.at(
                SOME_INSTANT,
                SOME_ZONE_ID,
            )

        val zonedClock = clock.withZone(zoneId)

        zonedClock.assertHas(SOME_INSTANT, zoneId)
    }

    @Test
    fun `tick() no args moves instant forward one second`() {
        val clock =
            TimeSlip.at(
                SOME_INSTANT,
                SOME_ZONE_ID,
            )

        clock.tick()

        clock.assertHas(
            SOME_INSTANT.plus(Duration.ofSeconds(1)),
            SOME_ZONE_ID,
        )
    }

    @ParameterizedTest
    @MethodSource("com.hypercubetools.timeslip.SampleData#durationProducer")
    fun `tick() moves instant forward duration amount`(duration: Duration) {
        val clock =
            TimeSlip.at(
                SOME_INSTANT,
                SOME_ZONE_ID,
            )

        clock.tick(duration)

        clock.assertHas(
            SOME_INSTANT.plus(duration),
            SOME_ZONE_ID,
        )
    }

    @ParameterizedTest
    @MethodSource("com.hypercubetools.timeslip.SampleData#instantProducer")
    fun `moveTo() moves instant to given instant`(instant: Instant) {
        val clock =
            TimeSlip.at(
                SOME_INSTANT,
                SOME_ZONE_ID,
            )

        clock.moveTo(instant)

        clock.assertHas(instant, SOME_ZONE_ID)
    }

    @Test
    fun `done() causes instant() to throw`() {
        val clock = TimeSlip.at(SOME_INSTANT)

        clock.done()

        assertThat({ clock.instant() }, throws<IllegalStateException>())
    }

    @Test
    fun `done() causes withZone() to throw`() {
        val clock = TimeSlip.at(SOME_INSTANT)

        clock.done()

        assertThat({ clock.withZone(SOME_ZONE_ID) }, throws<IllegalStateException>())
    }

    @Test
    fun `done() causes getZone() to throw`() {
        val clock = TimeSlip.at(SOME_INSTANT)

        clock.done()

        assertThat({ clock.zone }, throws<IllegalStateException>())
    }

    @Test
    fun `tick() after done() causes exception`() {
        val clock = TimeSlip.at(SOME_INSTANT)

        clock.done()

        assertThat({ clock.tick() }, throws<IllegalStateException>())
    }

    @Test
    fun `moveTo() after done() causes exception`() {
        val clock = TimeSlip.at(SOME_INSTANT)

        clock.done()

        assertThat({ clock.moveTo(SOME_OTHER_INSTANT) }, throws<IllegalStateException>())
    }

    @Test
    fun `startAt(defaults) increases instant each call`() {
        val clock = TimeSlip.startAt(SOME_INSTANT)

        assertAll(
            { assertThat(clock.zone, equalTo(ZoneOffset.UTC as ZoneId)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT.plus(Duration.ofSeconds(1)))) },
        )
    }

    @Test
    fun `startAt(default tickAmount) increases instant each call`() {
        val clock =
            TimeSlip.startAt(
                SOME_INSTANT,
                SOME_ZONE_ID,
            )

        assertAll(
            { assertThat(clock.zone, equalTo(SOME_ZONE_ID)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT.plus(Duration.ofSeconds(1)))) },
        )
    }

    @Test
    fun `startAt(tickAmount) increases instant each call`() {
        val clock =
            TimeSlip.startAt(
                SOME_INSTANT,
                SOME_ZONE_ID,
                tickAmount = SOME_DURATION,
            )

        assertAll(
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER)) },
        )
    }

    @Test
    fun `startAt(tickForward) apply change each call`() {
        val clock =
            TimeSlip.startAt(SOME_INSTANT) { initial ->
                initial.plus(SOME_DURATION)
            }

        assertAll(
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER)) },
        )
    }

    @Test
    fun `startAt(zoneId, tickForward) apply change each call`() {
        val clock =
            TimeSlip.startAt(
                SOME_INSTANT,
                SOME_ZONE_ID,
            ) { initial -> initial.plus(SOME_DURATION) }

        assertAll(
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER)) },
        )
    }

    @Test
    fun `startAt(tickForward) - tick() causes tickForward to get initial value increased by duration`() {
        var tickForwardArg: Instant? = null
        val clock =
            TimeSlip.startAt(
                SOME_INSTANT,
                SOME_ZONE_ID,
            ) { instant ->
                tickForwardArg = instant
                SOME_OTHER_INSTANT
            }

        clock.tick(SOME_DURATION)
        clock.instant() // trigger call to tickForward

        assertThat(tickForwardArg, equalTo(SOME_INSTANT_SOME_DURATION_LATER))
    }

    @Test
    fun `startAt(tickForward) - moveTo() causes tickForward to get moveTo() arg`() {
        var tickForwardArg: Instant? = null
        val clock =
            TimeSlip.startAt(
                SOME_INSTANT,
                SOME_ZONE_ID,
            ) { instant ->
                tickForwardArg = instant
                SOME_OTHER_INSTANT
            }

        clock.moveTo(SOME_OTHER_INSTANT)
        clock.instant() // trigger call to tickForward

        assertThat(tickForwardArg, equalTo(SOME_OTHER_INSTANT))
    }

    @Test
    fun `sequence() empty - throw for instant`() {
        val clock = TimeSlip.sequence { }

        assertThat({ clock.instant() }, throws<IllegalStateException>())
    }

    @Test
    fun `sequence() - tick() throws exception`() {
        val clock =
            TimeSlip.sequence { first(SOME_INSTANT) }

        assertThat({ clock.tick() }, throws<IllegalStateException>())
    }

    @Test
    fun `sequence() - moveTo() throws exception`() {
        val clock =
            TimeSlip.sequence { first(SOME_INSTANT) }

        assertThat({ clock.moveTo(SOME_INSTANT) }, throws<IllegalStateException>())
    }

    @Test
    fun `sequence() one instants returns then throws exception`() {
        val clock =
            TimeSlip.sequence { first(SOME_INSTANT) }

        assertAll(
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { clock.assertInstantThrowsIllegalStateException() },
        )
    }

    @Test
    fun `sequence() two instants - returns twice, then throws exception`() {
        val clock =
            TimeSlip.sequence {
                first(SOME_INSTANT)
                then(SOME_OTHER_INSTANT)
            }

        assertAll(
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)) },
            { clock.assertInstantThrowsIllegalStateException() },
        )
    }

    @Test
    fun `sequence() varargs - expected order`() {
        val clock =
            TimeSlip.sequence {
                first(
                    SOME_INSTANT,
                    SOME_INSTANT_TWO_SOME_DURATION_LATER,
                )
                then(
                    SOME_OTHER_INSTANT,
                    SOME_INSTANT_SOME_DURATION_LATER,
                )
            }

        assertAll(
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER)) },
            { assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER)) },
        )
    }

    @Test
    fun `sequence() lists - expected order`() {
        val clock =
            TimeSlip.sequence {
                first(
                    listOf(
                        SOME_INSTANT,
                        SOME_INSTANT_TWO_SOME_DURATION_LATER,
                    ),
                )
                then(
                    listOf(
                        SOME_OTHER_INSTANT,
                        SOME_INSTANT_SOME_DURATION_LATER,
                    ),
                )
            }

        assertAll(
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER)) },
            { assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER)) },
        )
    }

    @Test
    fun `sequence() one instant cycle - constantly returns instance`() {
        val clock =
            TimeSlip.sequence {
                first(SOME_INSTANT)
                cycle = true
            }

        assertAll(
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
        )
    }

    @Test
    fun `sequence() two instants cycle - alternates instances`() {
        val clock =
            TimeSlip.sequence {
                first(SOME_INSTANT)
                then(SOME_OTHER_INSTANT)
                cycle = true
            }

        assertAll(
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)) },
        )
    }

    @Test
    fun `sequence() then before first - first returned first`() {
        val clock =
            TimeSlip.sequence {
                then(SOME_OTHER_INSTANT)
                first(SOME_INSTANT)
            }

        assertAll(
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)) },
        )
    }

    @Test
    fun `sequence() then before first varargs - first returned first`() {
        val clock =
            TimeSlip.sequence {
                then(
                    SOME_OTHER_INSTANT,
                    SOME_INSTANT_SOME_DURATION_LATER,
                )
                first(
                    SOME_INSTANT,
                    SOME_INSTANT_TWO_SOME_DURATION_LATER,
                )
            }

        assertAll(
            { assertThat(clock.instant(), equalTo(SOME_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER)) },
            { assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)) },
            { assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER)) },
        )
    }

    @Test
    fun `sequence() - clock defaults to UTC zone`() {
        val clock = TimeSlip.sequence { first(SOME_INSTANT) }

        assertThat(clock.zone, equalTo<ZoneId>(ZoneOffset.UTC))
    }

    @Test
    fun `sequence() zone set - clock has given zone`() {
        val clock =
            TimeSlip.sequence {
                zone = SOME_ZONE_ID
                first(SOME_INSTANT)
            }

        assertThat(clock.zone, equalTo(SOME_ZONE_ID))
    }

    // Workaround for https://youtrack.jetbrains.com/issue/KT-29475
    // Deeply nested lambda within a function with ")" in the name
    private fun TimeSlip.assertInstantThrowsIllegalStateException() {
        assertThat({ instant() }, throws<IllegalStateException>())
    }

    companion object {
        @JvmStatic
        fun Clock.assertEqualTo(expectedClock: Clock) = this.assertHas(expectedClock.instant(), expectedClock.zone)

        @JvmStatic
        fun Clock.assertHas(
            expectedInstant: Instant,
            expectedZoneId: ZoneId,
        ) {
            assertAll(
                { assertThat(this.instant(), equalTo(expectedInstant)) },
                { assertThat(this.zone, equalTo(expectedZoneId)) },
            )
        }
    }
}
