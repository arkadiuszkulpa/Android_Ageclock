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
        birthDateMillis: Long,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): CalculatedAge {
        val birthDateTime = Instant.ofEpochMilli(birthDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        val currentDateTime = Instant.ofEpochMilli(currentTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        val birthDate = birthDateTime.toLocalDate()
        val currentDate = currentDateTime.toLocalDate()

        // Calculate period for years, months, days
        val period = Period.between(birthDate, currentDate)

        // Calculate time components within the current day
        // We need the time difference from the birth time to now, accounting for the date
        val birthTimeOfDay = birthDateTime.toLocalTime()
        val currentTimeOfDay = currentDateTime.toLocalTime()

        // Calculate hours, minutes, seconds remaining after accounting for full days
        val fullDaysDateTime = birthDateTime.plusYears(period.years.toLong())
            .plusMonths(period.months.toLong())
            .plusDays(period.days.toLong())

        val remainingDuration = Duration.between(fullDaysDateTime, currentDateTime)
        val remainingHours = remainingDuration.toHours().toInt()
        val remainingMinutes = (remainingDuration.toMinutes() % 60).toInt()
        val remainingSeconds = (remainingDuration.seconds % 60).toInt()

        // Calculate total durations
        val totalDays = ChronoUnit.DAYS.between(birthDateTime, currentDateTime)
        val totalHours = ChronoUnit.HOURS.between(birthDateTime, currentDateTime)
        val totalMinutes = ChronoUnit.MINUTES.between(birthDateTime, currentDateTime)
        val totalSeconds = ChronoUnit.SECONDS.between(birthDateTime, currentDateTime)

        return CalculatedAge(
            years = period.years,
            months = period.months,
            days = period.days,
            hours = remainingHours.coerceAtLeast(0),
            minutes = remainingMinutes.coerceAtLeast(0),
            seconds = remainingSeconds.coerceAtLeast(0),
            totalDays = totalDays,
            totalHours = totalHours,
            totalMinutes = totalMinutes,
            totalSeconds = totalSeconds
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
