package com.ageclock.app.data.model

enum class AgeGranularity(val displayName: String) {
    YEARS_DAYS("Years + Days"),
    YEARS_MONTHS_DAYS("Years + Months + Days"),
    TOTAL_DAYS("Total Days"),
    TOTAL_HOURS("Total Hours"),
    TOTAL_MINUTES("Total Minutes"),
    TOTAL_SECONDS("Total Seconds")
}
