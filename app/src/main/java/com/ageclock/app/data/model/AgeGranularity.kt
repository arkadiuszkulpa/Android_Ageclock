package com.ageclock.app.data.model

/**
 * Represents which time units to display for age.
 * Stored as a bitmask integer for flexibility.
 *
 * Single unit selected = shows total (e.g., "1,957 days")
 * Multiple units selected = shows breakdown (e.g., "5 years, 4 months, 7 days")
 */
object AgeUnits {
    const val YEARS = 1
    const val MONTHS = 2
    const val DAYS = 4
    const val HOURS = 8
    const val MINUTES = 16
    const val SECONDS = 32

    // Common presets
    const val YEARS_DAYS = YEARS or DAYS                          // 5
    const val YEARS_MONTHS_DAYS = YEARS or MONTHS or DAYS         // 7

    val ALL_UNITS = listOf(
        YEARS to "Years",
        MONTHS to "Months",
        DAYS to "Days",
        HOURS to "Hours",
        MINUTES to "Minutes",
        SECONDS to "Seconds"
    )

    fun hasUnit(flags: Int, unit: Int): Boolean = (flags and unit) != 0

    fun toggleUnit(flags: Int, unit: Int): Int {
        val newFlags = flags xor unit
        // Ensure at least one unit is selected
        return if (newFlags == 0) flags else newFlags
    }

    fun countUnits(flags: Int): Int {
        var count = 0
        var f = flags
        while (f != 0) {
            count += f and 1
            f = f shr 1
        }
        return count
    }
}
