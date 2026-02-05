package com.ageclock.app.util

import com.ageclock.app.data.model.CalculatedAge
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object AgeCalculator {

    fun calculateAge(
        dateMillis: Long,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): CalculatedAge {
        val dateTime = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        val currentDateTime = Instant.ofEpochMilli(currentTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        // Determine if this is a future date (countdown) or past date (age)
        val isFuture = dateMillis > currentTimeMillis

        // For calculations, always go from earlier to later date
        val (startDateTime, endDateTime) = if (isFuture) {
            currentDateTime to dateTime  // Countdown: now -> future
        } else {
            dateTime to currentDateTime  // Age: past -> now
        }

        // Calculate total durations (always positive)
        val totalSeconds = ChronoUnit.SECONDS.between(startDateTime, endDateTime)
        val totalMinutes = ChronoUnit.MINUTES.between(startDateTime, endDateTime)
        val totalHours = ChronoUnit.HOURS.between(startDateTime, endDateTime)
        val totalDays = ChronoUnit.DAYS.between(startDateTime, endDateTime)

        // For breakdown: progressively calculate each unit
        var working = startDateTime

        // Calculate years
        val years = ChronoUnit.YEARS.between(working, endDateTime).toInt()
        working = working.plusYears(years.toLong())

        // Calculate months
        val months = ChronoUnit.MONTHS.between(working, endDateTime).toInt()
        working = working.plusMonths(months.toLong())

        // Calculate days
        val days = ChronoUnit.DAYS.between(working, endDateTime).toInt()
        working = working.plusDays(days.toLong())

        // Calculate hours
        val hours = ChronoUnit.HOURS.between(working, endDateTime).toInt()
        working = working.plusHours(hours.toLong())

        // Calculate minutes
        val minutes = ChronoUnit.MINUTES.between(working, endDateTime).toInt()
        working = working.plusMinutes(minutes.toLong())

        // Calculate seconds
        val seconds = ChronoUnit.SECONDS.between(working, endDateTime).toInt()

        return CalculatedAge(
            years = years,
            months = months,
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            totalDays = totalDays,
            totalHours = totalHours,
            totalMinutes = totalMinutes,
            totalSeconds = totalSeconds,
            isFuture = isFuture
        )
    }

    fun liveAgeFlow(birthDateMillis: Long): Flow<CalculatedAge> = flow {
        while (true) {
            emit(calculateAge(birthDateMillis))
            delay(1000L)
        }
    }

    fun localDateToMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun millisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
