package com.ageclock.app.data.model

import java.text.NumberFormat
import java.util.Locale

data class CalculatedAge(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val totalHours: Long,
    val totalMinutes: Long,
    val totalSeconds: Long
) {
    fun format(granularity: AgeGranularity): String {
        return when (granularity) {
            AgeGranularity.YEARS_DAYS -> {
                val daysInYear = (totalDays % 365).toInt()
                buildString {
                    append(years)
                    append(if (years == 1) " year, " else " years, ")
                    append(daysInYear)
                    append(if (daysInYear == 1) " day" else " days")
                }
            }
            AgeGranularity.YEARS_MONTHS_DAYS -> {
                buildString {
                    if (years > 0) {
                        append(years)
                        append(if (years == 1) " year" else " years")
                    }
                    if (months > 0) {
                        if (isNotEmpty()) append(", ")
                        append(months)
                        append(if (months == 1) " month" else " months")
                    }
                    if (days > 0 || isEmpty()) {
                        if (isNotEmpty()) append(", ")
                        append(days)
                        append(if (days == 1) " day" else " days")
                    }
                }
            }
            AgeGranularity.TOTAL_DAYS -> "${totalDays.formatWithCommas()} days"
            AgeGranularity.TOTAL_HOURS -> "${totalHours.formatWithCommas()} hours"
            AgeGranularity.TOTAL_MINUTES -> "${totalMinutes.formatWithCommas()} minutes"
            AgeGranularity.TOTAL_SECONDS -> "${totalSeconds.formatWithCommas()} seconds"
        }
    }

    fun formatCompact(granularity: AgeGranularity): String {
        return when (granularity) {
            AgeGranularity.YEARS_DAYS -> {
                val daysInYear = (totalDays % 365).toInt()
                "${years}y, ${daysInYear}d"
            }
            AgeGranularity.YEARS_MONTHS_DAYS -> {
                buildString {
                    if (years > 0) append("${years}y")
                    if (months > 0) {
                        if (isNotEmpty()) append(", ")
                        append("${months}m")
                    }
                    if (days > 0 || isEmpty()) {
                        if (isNotEmpty()) append(", ")
                        append("${days}d")
                    }
                }
            }
            AgeGranularity.TOTAL_DAYS -> "${totalDays.formatWithCommas()}d"
            AgeGranularity.TOTAL_HOURS -> "${totalHours.formatWithCommas()}h"
            AgeGranularity.TOTAL_MINUTES -> "${totalMinutes.formatWithCommas()}m"
            AgeGranularity.TOTAL_SECONDS -> "${totalSeconds.formatWithCommas()}s"
        }
    }

    private fun Long.formatWithCommas(): String {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(this)
    }
}
