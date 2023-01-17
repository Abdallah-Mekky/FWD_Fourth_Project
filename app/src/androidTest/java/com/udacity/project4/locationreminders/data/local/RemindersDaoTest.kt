package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var myDatabase: RemindersDatabase


    @Before
    fun setup() {
        myDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    fun finish() {
        myDatabase.close()
    }

    @Test
    fun checkGetRemindersDatabase() = runBlockingTest {
        // add reminder item in DataBase
        val currentReminder = mutableListOf(
            ReminderDTO(
                title = "test1",
                description = "test1",
                location = "test2",
                latitude = 15.0,
                longitude = 15.0
            )
        )
        myDatabase.reminderDao().saveReminder(currentReminder[0])
        //return list from DataBase
        val reminderItem = myDatabase.reminderDao().getReminders()
        //check if list return expected values
        assertThat(currentReminder.size, `is`(1))
        assertThat(currentReminder[0].id, `is`(reminderItem[0].id))
        assertThat(currentReminder[0].title, `is`(reminderItem[0].title))
        assertThat(currentReminder[0].description, `is`(reminderItem[0].description))
        assertThat(currentReminder[0].location, `is`(reminderItem[0].location))
        assertThat(currentReminder[0].latitude, `is`(reminderItem[0].latitude))
        assertThat(currentReminder[0].longitude, `is`(reminderItem[0].longitude))
    }

    @Test
    fun checkGetRemindersByIDDatabase() = runBlockingTest {
        // add reminder item in DataBase

        val currentReminder = mutableListOf(
            ReminderDTO(
                id = "1",
                title = "test1",
                description = "test1",
                location = "test2",
                latitude = 15.0,
                longitude = 15.0
            )
        )
        myDatabase.reminderDao().saveReminder(currentReminder[0])
        //return list from DataBase ById
        val reminderItem = myDatabase.reminderDao().getReminderById("1")
        //check if list return expected values

        assertThat(currentReminder.size, `is`(1))
        assertThat(currentReminder[0].id, `is`(reminderItem?.id))
        assertThat(currentReminder[0].title, `is`(reminderItem?.title))
        assertThat(currentReminder[0].description, `is`(reminderItem?.description))
        assertThat(currentReminder[0].location, `is`(reminderItem?.location))
        assertThat(currentReminder[0].latitude, `is`(reminderItem?.latitude))
        assertThat(currentReminder[0].longitude, `is`(reminderItem?.longitude))
    }

    @Test
    fun checkDeleteAllItemDatabase() = runBlockingTest {
        // add reminder item in DataBase

        val currentReminder = mutableListOf(
            ReminderDTO(
                id = "1",
                title = "test1",
                description = "test1",
                location = "test2",
                latitude = 15.0,
                longitude = 15.0
            )
        )
        myDatabase.reminderDao().saveReminder(currentReminder[0])
        //Delete All Items
        myDatabase.reminderDao().deleteAllReminders()

        //return list from DataBase
        val reminders = myDatabase.reminderDao().getReminders()

        //check if list return expected values And list is empty
        assertThat(reminders.isEmpty(), `is`(true))
    }

    @Test
    fun checkItemExiestenceInDatabase() = runBlockingTest {
        //return list from DataBase
        val currentReminder = myDatabase.reminderDao().getReminders()

        //check if list return expected values
        assertThat(currentReminder.isEmpty(), `is`(true))


    }

    @Test
    fun checkItemNotFoundReturn_Null() = runBlockingTest {
        // add reminder item in DataBase

        val reminder = mutableListOf(
            ReminderDTO(
                id = "1",
                title = "test1",
                description = "test1",
                location = "test2",
                latitude = 15.0,
                longitude = 15.0
            )
        )
        myDatabase.reminderDao().saveReminder(reminder[0])
        //return list from DataBase and chose a wrong id

        val reminders = myDatabase.reminderDao().getReminderById("5")
        //check if list return expected values

        Assert.assertNull(reminders)


    }


}