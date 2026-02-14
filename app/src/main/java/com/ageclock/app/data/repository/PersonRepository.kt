package com.ageclock.app.data.repository

import com.ageclock.app.data.local.PersonDao
import com.ageclock.app.data.model.Person
import kotlinx.coroutines.flow.Flow

class PersonRepository(private val personDao: PersonDao) {
    val allPeople: Flow<List<Person>> = personDao.getAllPeople()
    val widgetPeople: Flow<List<Person>> = personDao.getWidgetPeople()

    suspend fun getWidgetPeopleSync(): List<Person> = personDao.getWidgetPeopleSync()

    suspend fun getById(id: Long): Person? = personDao.getById(id)

    suspend fun getByName(name: String): Person? = personDao.getByName(name)

    suspend fun insert(person: Person): Long = personDao.insert(person)

    suspend fun update(person: Person) = personDao.update(person)

    suspend fun delete(person: Person) = personDao.delete(person)

    suspend fun deleteById(id: Long) = personDao.deleteById(id)

    suspend fun toggleWidgetDisplay(person: Person) {
        personDao.update(person.copy(showInWidget = !person.showInWidget))
    }
}
