package com.sanket_satpute_20.ironmind.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface AppClock {
    fun nowMillis(): Long
    fun now(): LocalDateTime
    fun today(): LocalDate
    fun currentTime(): LocalTime
}

class DefaultAppClock : AppClock {
    override fun nowMillis(): Long = System.currentTimeMillis()
    override fun now(): LocalDateTime = LocalDateTime.now()
    override fun today(): LocalDate = LocalDate.now()
    override fun currentTime(): LocalTime = LocalTime.now()
}

/**
 * Global instance for production usage.
 * In unit tests, you can inject or substitute this with a fake.
 */
object AppClockProvider {
    var clock: AppClock = DefaultAppClock()
}
