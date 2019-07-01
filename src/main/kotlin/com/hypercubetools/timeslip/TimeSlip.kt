package com.hypercubetools.timeslip

import java.time.*

/**
 *  Produce a value based on the previous value.
 */
typealias TickForward = (Instant) -> Instant

/**
 * Type that allows for dealing with signature internally.
 */
private typealias OptionallyTickForward = (Instant) -> Instant?

private val ONE_SECOND = Duration.ofSeconds(1)
private val DEFAULT_ZONE = ZoneOffset.UTC
private val noopTick: TickForward = { instant -> instant }

/**
 * A concrete [Clock] implementation that is configurable and alterable for the purposes of reproducible
 * tests.
 *
 * @constructor Create an instance
 * @param initialInstant The time the instance is initially set to.
 * @param zone The time-zone the instance should be based in. Defaults to UTC.
 * @param isSequence `true` if [tickForward] ignores the given [Instant] when generating the next time. This will cause
 * [tick] and [moveTo] to throw [IllegalStateException] as they won't have an effect on this instance.
 * @param tickForward A function that specifies how the clock's time should change when the time is requested.
 * Defaults to returning the given [Instant]. The function can return `null` to indicate that there are no more times
 * to provide.
 */
class TimeSlip private constructor(
    initialInstant: Instant?,
    private val zone: ZoneId,
    private val isSequence: Boolean = false,
    private val tickForward: OptionallyTickForward = noopTick
) : Clock() {
    private var nextInstant: Instant? = initialInstant

    /**
     * Returns a copy of this clock with a different time-zone.
     *
     * @param newZone the time-zone to change to
     * @return a clock based on this clock with the specified time-zone
     * @throws IllegalStateException This clock doesn't have any more times to provide.
     */
    override fun withZone(newZone: ZoneId): Clock = TimeSlip(
        nextInstant ?: throw IllegalStateException("No more times to provide."),
        newZone, isSequence, tickForward
    )

    /**
     * Gets the time-zone being used to create dates and times.
     *
     * @return the time-zone being used to interpret instants
     * @throws IllegalStateException This clock doesn't have any more times to provide.
     */
    override fun getZone() = if (nextInstant == null) throw IllegalStateException("No more times to provide.") else zone

    /**
     * Gets the current instant of the clock.
     *
     * @return the current instant from this clock
     * @throws IllegalStateException This clock doesn't have any more times to provide.
     */
    override fun instant(): Instant {
        val result = nextInstant ?: throw IllegalStateException("No more times to provide.")
        nextInstant = tickForward(result)
        return result
    }

    /**
     * Move the clock forward in time.
     *
     * @param delta the amount time to move forward. A negative duration will move the clock backwards.
     * Defaults to 1 second.
     * @throws IllegalStateException [sequence] was used to create this instance.
     */
    @JvmOverloads
    fun tick(delta: Duration = ONE_SECOND) {
        if (isSequence) {
            throw notSupported("sequences")
        }
        nextInstant = nextInstant.let {
            if (it == null) {
                throw notSupported("TimeSlip.noCall() or after done() was called")
            } else {
                it.plus(delta)
            }
        }
    }

    /**
     * Changes the clock to the given instant.
     *
     * @param newInstant the new instant for the clock.
     * @throws IllegalStateException [sequence] was used to create this instance.
     */
    fun moveTo(newInstant: Instant) {
        if (isSequence) {
            throw IllegalStateException("Calling moveTo() is not supported with sequences.")
        }
        if (nextInstant == null) {
            throw IllegalStateException("Calling moveTo() is not supported when initialized with noCall().")
        }
        nextInstant = newInstant
    }

    /**
     * Invalidate the clock.
     *
     * Any method calls will throw [IllegalStateException] after this is called.
     */
    fun done() {
        nextInstant = null
    }

    private fun notSupported(condition: String) =
        IllegalStateException("Calling tick() is not supported with $condition.")

    companion object {
        /**
         * Create an instance that will throw [IllegalStateException] whenever [Clock] methods are called.
         *
         * This can be useful for asserting that [Clock] methods are not called.
         *
         * @return A newly constructed instance.
         */
        @JvmStatic
        fun noCall() = TimeSlip(null, DEFAULT_ZONE)

        /**
         * Create an instance that starts at a given time and won't move unless [moveTo] or [tick] are called.
         *
         * @param initialInstant The time the instance is initially set to.
         * @param zone The time-zone the instance should be based in. Defaults to UTC.
         * @return A newly constructed instance.
         */
        @JvmStatic
        @JvmOverloads
        fun at(initialInstant: Instant, zone: ZoneId = DEFAULT_ZONE) = TimeSlip(initialInstant, zone)

        /**
         * Create an instance that starts at a given time, but moves forward by a constant amount each time the current
         * time is requested.
         *
         * @param initialInstant The time the instance is initially set to.
         * @param zone The time-zone the instance should be based in. Defaults to UTC.
         * @param tickAmount The amount of time that should be added to the previous time to calculate the current time.
         * If desired, a negative value can be used to move the time backwards instead of forwards.
         * @return A newly constructed instance.
         */
        @JvmStatic
        @JvmOverloads
        fun startAt(initialInstant: Instant, zone: ZoneId = DEFAULT_ZONE, tickAmount: Duration = ONE_SECOND): TimeSlip {
            return TimeSlip(initialInstant, zone, tickForward = { instant -> instant.plus(tickAmount) })
        }

        /**
         * Create an instance that starts at a given time, but changes based on a given function each time the current
         * time is requested.
         *
         * @param initialInstant The time the instance is initially set to.
         * @param zone The time-zone the instance should be based in. Defaults to UTC.
         * @param tickForward A function that specifies how the clock's time should change when the time is requested.
         * This is called immediately *after* a value is requested. So when the first time is requested, this function
         * will be called to to pre-load the next time to return. This should be considered if this function has any
         * side effects.
         * @return A newly constructed instance.
         */
        @JvmStatic
        @JvmOverloads
        fun startAt(initialInstant: Instant, zone: ZoneId = DEFAULT_ZONE, tickForward: TickForward): TimeSlip {
            return TimeSlip(initialInstant, zone, isSequence = false, tickForward = tickForward)
        }

        /**
         * Create a [TimeSlip] instance backed by a sequence of [Instant]s.
         *
         * @param body Initialization steps of [SequenceBuilder].
         * @return A newly constructed instance.
         * @throws IllegalStateException No [Instant]s were added to the builder. Use [noCall] to create a [TimeSlip]
         * that does not produce any times.
         */
        fun sequence(body: SequenceBuilder.() -> Unit) = sequenceBuilder().apply(body).build()

        /**
         * Create a builder for a [TimeSlip] instance backed by a sequence of [Instant]s.
         */
        @JvmStatic
        fun sequenceBuilder() = SequenceBuilder()
    }

    /**
     * Builder for a [TimeSlip] instance backed by a sequence of [Instant]s.
     */
    class SequenceBuilder {
        /**
         * Zone Id to be used by [Clock]. Defaults to UTC.
         */
        var zone: ZoneId = DEFAULT_ZONE

        /**
         * Set the zone Id to be used by [Clock].
         * @param zoneId The zone id to use.
         * @return This builder.
         */
        fun zone(zoneId: ZoneId) = apply { zone = zoneId }

        /**
         * When `true`, after returning the last [Instant] in the sequence, the next sequence will repeat again from
         * the first [Instant].
         * When `false`, after returning the last [Instant] in the sequence, [IllegalStateException] will be thrown
         * the next time the time is requested.
         */
        var cycle: Boolean = false

        /**
         * Specify if the sequence should repeat.
         *
         * @param shouldCycle When `true`, after returning the last [Instant] in the sequence, the next sequence
         * will repeat again from the first [Instant]. When `false`, after returning the last [Instant] in the
         * sequence, [IllegalStateException] will be thrown the next time the time is requested.
         * @return This builder.
         */
        fun cycle(shouldCycle: Boolean) = apply { cycle = shouldCycle }

        private val _instants = arrayListOf<Instant>()

        /**
         * Prepend a number of instants to the start of the sequence of instants.
         *
         * @param instants Instants to add to the sequence.
         * @return This builder.
         */
        fun first(vararg instants: Instant) = apply { first(instants.toList()) }

        /**
         * Prepend a number of instants to the start of the sequence of instants.
         *
         * @param instants Instants to add to the sequence.
         * @return This builder.
         */
        fun first(instants: Collection<Instant>) = apply { _instants.addAll(0, instants) }

        /**
         * Apend a number of instants to the end of the sequence of instants.
         *
         * @param instants Instants to add to the sequence.
         * @return This builder.
         */
        fun then(vararg instants: Instant) = apply { then(instants.toList()) }

        /**
         * Apend a number of instants to the end of the sequence of instants.
         *
         * @param instants Instants to add to the sequence.
         * @return This builder.
         */
        fun then(instants: Collection<Instant>) = apply { _instants.addAll(instants) }

        /**
         * Construct a [TimeSlip] instance that will provide [Instant]s based on the configured sequence.
         *
         * @return A newly constructed instance.
         * @throws IllegalStateException No [Instant]s were added to the builder. Use [noCall] to create a [TimeSlip]
         * that does not produce any times.
         */
        fun build(): TimeSlip {
            val values = _instants.toList()
            if (values.isEmpty()) {
                return TimeSlip(null, zone, isSequence = true)
            }
            var nextIndex = 1
            @Suppress("UNUSED_PARAMETER")
            fun tickForward(current: Instant): Instant? {
                if (!cycle && nextIndex >= values.size) {
                    return null
                }
                val result = values[nextIndex % values.size]
                nextIndex++
                return result
            }
            return TimeSlip(values[0], zone, true, ::tickForward)
        }
    }
}
