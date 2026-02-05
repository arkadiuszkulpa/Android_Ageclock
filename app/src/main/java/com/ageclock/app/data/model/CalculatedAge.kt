package com.ageclock.app.data.model

import java.text.NumberFormat
import java.util.Locale

data class CalculatedAge(
    val years: Int,
    val months: Int,
    val days: Int,
    val hours: Int,       // Hours within the day (0-23)
    val minutes: Int,     // Minutes within the hour (0-59)
    val seconds: Int,     // Seconds within the minute (0-59)
    val totalDays: Long,
    val totalHours: Long,
    val totalMinutes: Long,
    val totalSeconds: Long,
    val isFuture: Boolean = false  // True if this is a countdown to a future date
) {
    /**
     * Format age based on selected unit flags.
     * If only one unit selected: shows total of that unit
     * If multiple units selected: shows breakdown
     * For future dates, prefixes with "in " (e.g., "in 5 days, 3 hours")
     */
    fun format(units: Int): String {
        val unitCount = AgeUnits.countUnits(units)

        val result = if (unitCount == 1) {
            // Single unit = show total
            when {
                AgeUnits.hasUnit(units, AgeUnits.YEARS) -> "$years ${pluralize(years, "year", "years")}"
                AgeUnits.hasUnit(units, AgeUnits.MONTHS) -> "${totalMonths().formatWithCommas()} ${pluralize(totalMonths(), "month", "months")}"
                AgeUnits.hasUnit(units, AgeUnits.DAYS) -> "${totalDays.formatWithCommas()} ${pluralize(totalDays, "day", "days")}"
                AgeUnits.hasUnit(units, AgeUnits.HOURS) -> "${totalHours.formatWithCommas()} ${pluralize(totalHours, "hour", "hours")}"
                AgeUnits.hasUnit(units, AgeUnits.MINUTES) -> "${totalMinutes.formatWithCommas()} ${pluralize(totalMinutes, "minute", "minutes")}"
                AgeUnits.hasUnit(units, AgeUnits.SECONDS) -> "${totalSeconds.formatWithCommas()} ${pluralize(totalSeconds, "second", "seconds")}"
                else -> ""
            }
        } else {
            // Multiple units = show breakdown
            buildString {
                if (AgeUnits.hasUnit(units, AgeUnits.YEARS) && years > 0) {
                    append("$years ${pluralize(years, "year", "years")}")
                }
                if (AgeUnits.hasUnit(units, AgeUnits.MONTHS) && months > 0) {
                    if (isNotEmpty()) append(", ")
                    append("$months ${pluralize(months, "month", "months")}")
                }
                if (AgeUnits.hasUnit(units, AgeUnits.DAYS) && days > 0) {
                    if (isNotEmpty()) append(", ")
                    append("$days ${pluralize(days, "day", "days")}")
                }
                // For time units: show even if 0 when part of a time breakdown
                if (AgeUnits.hasUnit(units, AgeUnits.HOURS)) {
                    if (isNotEmpty()) append(", ")
                    append("$hours ${pluralize(hours, "hour", "hours")}")
                }
                if (AgeUnits.hasUnit(units, AgeUnits.MINUTES)) {
                    if (isNotEmpty()) append(", ")
                    append("$minutes ${pluralize(minutes, "minute", "minutes")}")
                }
                if (AgeUnits.hasUnit(units, AgeUnits.SECONDS)) {
                    if (isNotEmpty()) append(", ")
                    append("$seconds ${pluralize(seconds, "second", "seconds")}")
                }
                // Fallback if all selected values are 0
                if (isEmpty()) {
                    append("0 ${getSmallestUnitName(units)}")
                }
            }
        }

        return if (isFuture) "in $result" else result
    }

    /**
     * Compact format for widgets
     * For future dates, prefixes with "in " (e.g., "in 5d, 3h")
     */
    fun formatCompact(units: Int): String {
        val unitCount = AgeUnits.countUnits(units)

        val result = if (unitCount == 1) {
            // Single unit = show total
            when {
                AgeUnits.hasUnit(units, AgeUnits.YEARS) -> "${years}y"
                AgeUnits.hasUnit(units, AgeUnits.MONTHS) -> "${totalMonths().formatWithCommas()}mo"
                AgeUnits.hasUnit(units, AgeUnits.DAYS) -> "${totalDays.formatWithCommas()}d"
                AgeUnits.hasUnit(units, AgeUnits.HOURS) -> "${totalHours.formatWithCommas()}h"
                AgeUnits.hasUnit(units, AgeUnits.MINUTES) -> "${totalMinutes.formatWithCommas()}m"
                AgeUnits.hasUnit(units, AgeUnits.SECONDS) -> "${totalSeconds.formatWithCommas()}s"
                else -> ""
            }
        } else {
            // Multiple units = show breakdown
            buildString {
                if (AgeUnits.hasUnit(units, AgeUnits.YEARS) && years > 0) {
                    append("${years}y")
                }
                if (AgeUnits.hasUnit(units, AgeUnits.MONTHS) && months > 0) {
                    if (isNotEmpty()) append(", ")
                    append("${months}mo")
                }
                if (AgeUnits.hasUnit(units, AgeUnits.DAYS) && days > 0) {
                    if (isNotEmpty()) append(", ")
                    append("${days}d")
                }
                // For time units: show even if 0 when part of a time breakdown
                if (AgeUnits.hasUnit(units, AgeUnits.HOURS)) {
                    if (isNotEmpty()) append(", ")
                    append("${hours}h")
                }
                if (AgeUnits.hasUnit(units, AgeUnits.MINUTES)) {
                    if (isNotEmpty()) append(", ")
                    append("${minutes}m")
                }
                if (AgeUnits.hasUnit(units, AgeUnits.SECONDS)) {
                    if (isNotEmpty()) append(", ")
                    append("${seconds}s")
                }
                if (isEmpty()) {
                    append("0${getSmallestUnitSuffix(units)}")
                }
            }
        }

        return if (isFuture) "in $result" else result
    }

    private fun totalMonths(): Long = (years * 12L) + months

    private fun pluralize(count: Int, singular: String, plural: String): String =
        if (count == 1) singular else plural

    private fun pluralize(count: Long, singular: String, plural: String): String =
        if (count == 1L) singular else plural

    private fun getSmallestUnitName(units: Int): String = when {
        AgeUnits.hasUnit(units, AgeUnits.SECONDS) -> "seconds"
        AgeUnits.hasUnit(units, AgeUnits.MINUTES) -> "minutes"
        AgeUnits.hasUnit(units, AgeUnits.HOURS) -> "hours"
        AgeUnits.hasUnit(units, AgeUnits.DAYS) -> "days"
        AgeUnits.hasUnit(units, AgeUnits.MONTHS) -> "months"
        else -> "years"
    }

    private fun getSmallestUnitSuffix(units: Int): String = when {
        AgeUnits.hasUnit(units, AgeUnits.SECONDS) -> "s"
        AgeUnits.hasUnit(units, AgeUnits.MINUTES) -> "m"
        AgeUnits.hasUnit(units, AgeUnits.HOURS) -> "h"
        AgeUnits.hasUnit(units, AgeUnits.DAYS) -> "d"
        AgeUnits.hasUnit(units, AgeUnits.MONTHS) -> "mo"
        else -> "y"
    }

    private fun Long.formatWithCommas(): String {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(this)
    }
}
