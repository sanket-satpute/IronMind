package com.sanket_satpute_20.ironmind.mission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class MissionExecutionTimeUtilsTest {

    @Test
    fun `parseTime accepts 24h format`() {
        val time = parseTime("14:30")
        assertNotNull(time)
        assertEquals(14, time!!.hour)
        assertEquals(30, time.minute)
    }

    @Test
    fun `parseTime accepts single digit hour`() {
        val time = parseTime("9:05")
        assertNotNull(time)
        assertEquals(9, time!!.hour)
        assertEquals(5, time.minute)
    }

    @Test
    fun `parseTime accepts 12h format with am pm`() {
        val time = parseTime("2:30 PM")
        assertNotNull(time)
        assertEquals(14, time!!.hour)
    }

    @Test
    fun `parseTime returns null for garbage input`() {
        assertNull(parseTime("not-a-time"))
    }

    @Test
    fun `plannedDurationMinutes computes minutes between valid times`() {
        assertEquals(90, plannedDurationMinutes("09:00", "10:30"))
    }

    @Test
    fun `plannedDurationMinutes never returns negative for end before start`() {
        assertEquals(0, plannedDurationMinutes("10:30", "09:00"))
    }

    @Test
    fun `plannedDurationMinutes returns zero when either time is unparsable`() {
        assertEquals(0, plannedDurationMinutes("garbage", "10:30"))
        assertEquals(0, plannedDurationMinutes("09:00", "garbage"))
    }

    @Test
    fun `resolveTaskEndMillis returns null for invalid date`() {
        assertNull(resolveTaskEndMillis("not-a-date", "10:30"))
    }

    @Test
    fun `resolveTaskEndMillis returns null for invalid time`() {
        assertNull(resolveTaskEndMillis("2026-01-01", "garbage"))
    }

    @Test
    fun `resolveTaskEndMillis resolves a valid date and time to an epoch millis in the future`() {
        val millis = resolveTaskEndMillis("2099-01-01", "10:30")
        assertNotNull(millis)
        assert(millis!! > System.currentTimeMillis())
    }
}
