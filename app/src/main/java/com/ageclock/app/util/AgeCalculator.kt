package com.ageclock.app.util

import com.ageclock.app.data.model.CalculatedAge
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
        val birthDate = Instant.ofEpochMilli(birthDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val currentDate = Instant.ofEpochMilli(currentTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val currentDateTime = Instant.ofEpochMilli(currentTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        val birthDateTime = birthDate.atStartOfDay()

        // Calculate period for years, months, days
        val period = Period.between(birthDate, currentDate)

        // Calculate total durations
        val totalDays = ChronoUnit.DAYS.between(birthDate, currentDate)
        val totalHours = ChronoUnit.HOURS.between(birthDateTime, currentDateTime)
        val totalMinutes = ChronoUnit.MINUTES.between(birthDateTime, currentDateTime)
        val totalSeconds = ChronoUnit.SECONDS.between(birthDateTime, currentDateTime)

        return CalculatedAge(
            years = period.years,
            months = period.months,
            days = period.days,
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
