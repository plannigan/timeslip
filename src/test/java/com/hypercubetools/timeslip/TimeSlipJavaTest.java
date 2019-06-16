package com.hypercubetools.timeslip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.*;
import java.util.Arrays;

import static com.hypercubetools.timeslip.SampleData.*;
import static com.hypercubetools.timeslip.TimeSlipTest.assertEqualTo;
import static com.hypercubetools.timeslip.TimeSlipTest.assertHas;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;


class TimeSlipJavaTest {
    @Test
    public void noCall_throws_on_instant() {
        Clock clock = TimeSlip.noCall();

        assertThrows(IllegalStateException.class, clock::instant);
    }

    @Test
    public void noCall_throws_on_withZone() {
        Clock clock = TimeSlip.noCall();

        assertThrows(IllegalStateException.class, () -> clock.withZone(SOME_ZONE_ID));
    }

    @Test
    public void at_sets_instant_and_zone() {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT, SOME_ZONE_ID);

        assertHas(clock, SOME_INSTANT, SOME_ZONE_ID);
    }

    @Test
    public void withZone_changes_zone_for_same_instant() {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT, SOME_ZONE_ID);

        Clock zonedClock = clock.withZone(SOME_OTHER_ZONE_ID);

        assertHas(zonedClock, SOME_INSTANT, SOME_OTHER_ZONE_ID);
    }

    @ParameterizedTest
    @MethodSource("com.hypercubetools.timeslip.SampleData#zoneProducer")
    public void withZone_matches_implementation_of_Clock_fixed_withZone(ZoneId zoneId) {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT, SOME_ZONE_ID);
        Clock fixedClock = Clock.fixed(SOME_INSTANT, SOME_ZONE_ID);

        Clock newZoneClock = clock.withZone(zoneId);

        assertEqualTo(newZoneClock, fixedClock.withZone(zoneId));
    }

    @ParameterizedTest
    @MethodSource("com.hypercubetools.timeslip.SampleData#zoneProducer")
    public void withZone_changes_zone_for_same_instant(ZoneId zoneId) {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT, SOME_ZONE_ID);

        Clock zonedClock = clock.withZone(zoneId);

        assertHas(zonedClock, SOME_INSTANT, zoneId);
    }

    @Test
    public void tick_no_args_moves_instant_forward_one_second() {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT, SOME_ZONE_ID);

        clock.tick();

        assertHas(clock, SOME_INSTANT.plus(Duration.ofSeconds(1)), SOME_ZONE_ID);
    }

    @ParameterizedTest
    @MethodSource("com.hypercubetools.timeslip.SampleData#durationProducer")
    public void tick_moves_instant_forward_duration_amount(Duration duration) {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT, SOME_ZONE_ID);

        clock.tick(duration);

        assertHas(clock, SOME_INSTANT.plus(duration), SOME_ZONE_ID);
    }

    @ParameterizedTest
    @MethodSource("com.hypercubetools.timeslip.SampleData#instantProducer")
    public void moveTo_moves_instant_to_given_instant(Instant instant) {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT, SOME_ZONE_ID);

        clock.moveTo(instant);

        assertHas(clock, instant, SOME_ZONE_ID);
    }

    @Test
    public void done_causes_instant_to_throw() {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT);

        clock.done();

        assertThrows(IllegalStateException.class, clock::instant);
    }

    @Test
    public void done_causes_withZone_to_throw() {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT);

        clock.done();

        assertThrows(IllegalStateException.class, () -> clock.withZone(SOME_ZONE_ID));
    }

    @Test
    public void done_causes_getZone_to_throw() {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT);

        clock.done();

        assertThrows(IllegalStateException.class, clock::getZone);
    }

    @Test
    public void tick_after_done_causes_exception() {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT);

        clock.done();

        assertThrows(IllegalStateException.class, clock::tick);
    }

    @Test
    public void moveTo_after_done_causes_exception() {
        TimeSlip clock = TimeSlip.at(SOME_INSTANT);

        clock.done();

        assertThrows(IllegalStateException.class, () -> clock.moveTo(SOME_OTHER_INSTANT));
    }

    @Test
    public void startAt_defaults_increases_instant_each_call() {
        TimeSlip clock = TimeSlip.startAt(SOME_INSTANT);

        assertAll(
                () -> assertThat(clock.getZone(), equalTo(ZoneOffset.UTC)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT.plus(Duration.ofSeconds(1))))
        );
    }

    @Test
    public void startAt_default_tickAmount_increases_instant_each_call() {
        TimeSlip clock = TimeSlip.startAt(SOME_INSTANT, SOME_ZONE_ID);

        assertAll(
                () -> assertThat(clock.getZone(), equalTo(SOME_ZONE_ID)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT.plus(Duration.ofSeconds(1))))
        );
    }


    @Test
    public void startAt_tickAmount_increases_instant_each_call() {
        TimeSlip clock = TimeSlip.startAt(SOME_INSTANT, SOME_ZONE_ID, SOME_DURATION);

        assertAll(
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER))
        );
    }

    @Test
    public void startAt_tickForward_apply_change_each_call() {
        TimeSlip clock = TimeSlip.startAt(SOME_INSTANT, (initial) -> initial.plus(SOME_DURATION));

        assertAll(
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER))
        );
    }

    @Test
    public void startAt_zoneId_tickForward_apply_change_each_call() {
        TimeSlip clock = TimeSlip.startAt(SOME_INSTANT, SOME_ZONE_ID, (initial) -> initial.plus(SOME_DURATION));

        assertAll(
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER))
        );
    }

    @Test
    public void startAt_tickForward__tick_causes_tickForward_to_get_initial_value_increased_by_duration() {
        CaptureTickForwardArg capture = new CaptureTickForwardArg();
        TimeSlip clock = TimeSlip.startAt(SOME_INSTANT, SOME_ZONE_ID, capture::tickForward);

        clock.tick(SOME_DURATION);
        clock.instant(); // trigger call to tickForward

        assertThat(capture.tickForwardArg, equalTo(SOME_INSTANT_SOME_DURATION_LATER));
    }

    @Test
    public void startAt_tickForward__moveTo_causes_tickForward_to_get_moveTo_arg() {
        CaptureTickForwardArg capture = new CaptureTickForwardArg();
        TimeSlip clock = TimeSlip.startAt(SOME_INSTANT, SOME_ZONE_ID, capture::tickForward);

        clock.moveTo(SOME_OTHER_INSTANT);
        clock.instant(); // trigger call to tickForward

        assertThat(capture.tickForwardArg, equalTo(SOME_OTHER_INSTANT));
    }

    @Test
    public void sequence__empty__instant_throws_exception() {
        TimeSlip clock = TimeSlip.sequenceBuilder().build();

        assertThrows(IllegalStateException.class, clock::instant);
    }

    @Test
    public void sequence__tick_throws_exception() {
        TimeSlip clock = TimeSlip.sequenceBuilder().first(SOME_INSTANT).build();

        assertThrows(IllegalStateException.class, clock::tick);
    }

    @Test
    public void sequence__moveTo_throws_exception() {
        TimeSlip clock = TimeSlip.sequenceBuilder().first(SOME_INSTANT).build();

        assertThrows(IllegalStateException.class, () -> clock.moveTo(SOME_INSTANT));
    }

    @Test
    public void sequence_one_instants_returns_then_throws_exception() {
        TimeSlip clock = TimeSlip.sequenceBuilder()
                .first(SOME_INSTANT)
                .build();

        assertAll(
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThrows(IllegalStateException.class, clock::instant)
        );
    }

    @Test
    public void sequence_two_instants__returns_twice_then_throws_exception() {
        TimeSlip clock = TimeSlip.sequenceBuilder()
                .first(SOME_INSTANT)
                .then(SOME_OTHER_INSTANT)
                .build();

        assertAll(
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)),
                () -> assertThrows(IllegalStateException.class, clock::instant)
        );
    }

    @Test
    public void sequence_varargs__expected_order() {
        Clock clock = TimeSlip.sequenceBuilder()
                .first(SOME_INSTANT, SOME_INSTANT_TWO_SOME_DURATION_LATER)
                .then(SOME_OTHER_INSTANT, SOME_INSTANT_SOME_DURATION_LATER)
                .build();

        assertAll(
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER)),
                () -> assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER))
        );
    }

    @Test
    public void sequence_lists__expected_order() {
        Clock clock = TimeSlip.sequenceBuilder()
                .first(Arrays.asList(SOME_INSTANT, SOME_INSTANT_TWO_SOME_DURATION_LATER))
                .then(Arrays.asList(SOME_OTHER_INSTANT, SOME_INSTANT_SOME_DURATION_LATER))
                .build();


        assertAll(
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER)),
                () -> assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER))
        );
    }

    @SuppressWarnings("DuplicateExpressions")
    @Test
    public void sequence_one_instant_cycle__constantly_returns_instance() {
        TimeSlip clock = TimeSlip.sequenceBuilder()
                .first(SOME_INSTANT)
                .cycle(true)
                .build();

        assertAll(
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT))
        );
    }

    @SuppressWarnings("DuplicateExpressions")
    @Test
    public void sequence_two_instants_cycle__alternates_instances() {
        TimeSlip clock = TimeSlip.sequenceBuilder()
                .cycle(true)
                .first(SOME_INSTANT)
                .then(SOME_OTHER_INSTANT)
                .build();

        assertAll(
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT))
        );
    }

    @Test
    public void builder_then_before_first__first_returned_first() {
        TimeSlip clock = TimeSlip.sequenceBuilder()
                .then(SOME_OTHER_INSTANT)
                .first(SOME_INSTANT)
                .build();

        assertAll(
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT))
        );
    }

    @Test
    public void builder_then_before_first_varargs__first_returned_first() {
        TimeSlip clock = TimeSlip.sequenceBuilder()
                .then(SOME_OTHER_INSTANT, SOME_INSTANT_SOME_DURATION_LATER)
                .first(SOME_INSTANT, SOME_INSTANT_TWO_SOME_DURATION_LATER)
                .build();

        assertAll(
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_TWO_SOME_DURATION_LATER)),
                () -> assertThat(clock.instant(), equalTo(SOME_OTHER_INSTANT)),
                () -> assertThat(clock.instant(), equalTo(SOME_INSTANT_SOME_DURATION_LATER))
        );
    }

    @Test
    public void builder_defaults_to_UTC_zone() {
        TimeSlip.SequenceBuilder builder = TimeSlip.sequenceBuilder();

        assertThat(builder.getZone(), equalTo(ZoneOffset.UTC));
    }

    @Test
    public void builder__clock_defaults_to_UTC_zone() {
        TimeSlip clock = TimeSlip.sequenceBuilder().first(SOME_INSTANT).build();

        assertThat(clock.getZone(), equalTo(ZoneOffset.UTC));
    }

    @Test
    public void builder_zone_set_clock_has_given_zone() {
        TimeSlip clock = TimeSlip.sequenceBuilder().zone(SOME_ZONE_ID).first(SOME_INSTANT).build();

        assertThat(clock.getZone(), equalTo(SOME_ZONE_ID));
    }

    private class CaptureTickForwardArg {
        Instant tickForwardArg;

        public Instant tickForward(Instant instant) {
            tickForwardArg = instant;
            return SOME_OTHER_INSTANT;
        }
    }
}
