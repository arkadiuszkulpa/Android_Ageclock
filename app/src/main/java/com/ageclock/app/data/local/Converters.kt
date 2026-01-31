package com.ageclock.app.data.local

import androidx.room.TypeConverter
import com.ageclock.app.data.model.AgeGranularity

class Converters {
    @TypeConverter
    fun fromAgeGranularity(value: AgeGranularity): String {
        return value.name
    }

    @TypeConverter
    fun toAgeGranularity(value: String): AgeGranularity {
        return AgeGranularity.valueOf(value)
    }
}
