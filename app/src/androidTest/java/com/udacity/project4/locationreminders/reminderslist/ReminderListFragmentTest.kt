package com.udacity.project4.locationreminders.reminderslist


import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsNot
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {
    private var remindersList = mutableListOf<ReminderDTO>(
        ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 15.0,
            longitude = 15.0
        ),
        ReminderDTO(
            title = "test2",
            description = "test2",
            location = "test2",
            latitude = 14.0,
            longitude = 14.0
        ),
        ReminderDTO(
            title = "test3",
            description = "test3",
            location = "test3",
            latitude = 14.0,
            longitude = 14.0
        )
    )

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()
        appContext = getApplicationContext()
        val newModule = module {
            viewModel {
                RemindersListViewModel(appContext, get() as ReminderDataSource)
            }
            single {
                SaveReminderViewModel(appContext, get() as ReminderDataSource)
            }
            single {
                LocalDB.createRemindersDao(appContext)
            }
            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }
        }
        startKoin {
            modules(newModule)
        }
        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }

    }


    @Test
    fun checkFragmentIn_ui() {
        // add reminder item in DataBase
        runBlocking {
            repository.saveReminder(remindersList[0])

        }
        //launch fragment scenario
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        //check if data displayed as expected
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(IsNot.not(ViewMatchers.isDisplayed())))
        onView(ViewMatchers.withText(remindersList[0].title)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
        onView(ViewMatchers.withText(remindersList[0].description)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
        onView(ViewMatchers.withText(remindersList[0].location)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )

    }

    @Test
    fun deleteAllReminders() {
        //delete all item from dataBase
        runBlocking {
            repository.deleteAllReminders()

        }

        //launch fragment scenario
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        //check if no data displayed
        onView(ViewMatchers.withText(R.string.no_data)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText(remindersList[0].title)).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun clickFabNavigateToFragmentReminder() {

        //launch fragment scenario
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }
        //click in add reminder button
        onView(withId(R.id.addReminderFAB)).perform(click())
        //we will go to to set value in Reminder List Fragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }


}