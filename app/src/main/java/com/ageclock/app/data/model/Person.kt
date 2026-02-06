package com.ageclock.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dateOfBirth: Long, // Epoch millis
    val displayUnits: Int = AgeUnits.YEARS_MONTHS_DAYS, // Bitmask of AgeUnits
    val showInWidget: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val description: String? = null, // Optional description for AI context
    val aiMessages: String? = null, // JSON array of AI-generated messages
    val aiMessagesGeneratedAt: Long? = null // Timestamp of last AI generation
)
