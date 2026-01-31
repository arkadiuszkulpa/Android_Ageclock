package com.ageclock.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ageclock.app.data.model.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY createdAt DESC")
    fun getAllPeople(): Flow<List<Person>>

    @Query("SELECT * FROM people WHERE showInWidget = 1 ORDER BY createdAt ASC")
    fun getWidgetPeople(): Flow<List<Person>>

    @Query("SELECT * FROM people WHERE showInWidget = 1 ORDER BY createdAt ASC")
    suspend fun getWidgetPeopleSync(): List<Person>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getById(id: Long): Person?

    @Insert
    suspend fun insert(person: Person): Long

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person)

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun deleteById(id: Long)
}
