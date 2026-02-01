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

        // Calculate total durations first
        val totalSeconds = ChronoUnit.SECONDS.between(birthDateTime, currentDateTime)
        val totalMinutes = ChronoUnit.MINUTES.between(birthDateTime, currentDateTime)
        val totalHours = ChronoUnit.HOURS.between(birthDateTime, currentDateTime)
        val totalDays = ChronoUnit.DAYS.between(birthDateTime, currentDateTime)

        // For breakdown: progressively calculate each unit
        var working = birthDateTime

        // Calculate years
        val years = ChronoUnit.YEARS.between(working, currentDateTime).toInt()
        working = working.plusYears(years.toLong())

        // Calculate months
        val months = ChronoUnit.MONTHS.between(working, currentDateTime).toInt()
        working = working.plusMonths(months.toLong())

        // Calculate days
        val days = ChronoUnit.DAYS.between(working, currentDateTime).toInt()
        working = working.plusDays(days.toLong())

        // Calculate hours
        val hours = ChronoUnit.HOURS.between(working, currentDateTime).toInt()
        working = working.plusHours(hours.toLong())

        // Calculate minutes
        val minutes = ChronoUnit.MINUTES.between(working, currentDateTime).toInt()
        working = working.plusMinutes(minutes.toLong())

        // Calculate seconds
        val seconds = ChronoUnit.SECONDS.between(working, currentDateTime).toInt()

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
