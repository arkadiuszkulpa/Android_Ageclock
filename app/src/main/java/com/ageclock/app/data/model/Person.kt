package com.ageclock.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dateOfBirth: Long, // Epoch millis
    val ageDisplayGranularity: AgeGranularity = AgeGranularity.YEARS_MONTHS_DAYS,
    val showInWidget: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
