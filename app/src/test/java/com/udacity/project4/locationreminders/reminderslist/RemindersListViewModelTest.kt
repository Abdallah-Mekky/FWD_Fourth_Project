package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValueTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])

class RemindersListViewModelTest {

    private lateinit var reminderListViewModel: RemindersListViewModel

    private lateinit var fakeDataSource: FakeDataSource



    @Before
    fun model(){ stopKoin()
        fakeDataSource = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule= MainCoroutineRule()

    private var remindersList = mutableListOf(
        ReminderDTO(
            title = "test1",
            location ="test2" ,
            latitude =15.0 ,
            longitude = 15.0 ,
            description = "test1"
        ),
        ReminderDTO(
            title = "test2",
            description = "test2",
            location ="test2" ,
            latitude =14.0 ,
            longitude = 14.0
        ),
        ReminderDTO(
            title = "test3",
            description = "test3",
            location ="test3" ,
            latitude =14.0 ,
            longitude = 14.0
        )
    )

    @After
    fun finish() {
        stopKoin()
    }

    @Test
    fun `when list is null or empty return no reminder  `() = runBlockingTest {
        fakeDataSource.deleteAllReminders()
        fakeDataSource.setReturnError(false)


        reminderListViewModel.loadReminders()

        MatcherAssert.assertThat(reminderListViewModel.remindersList.getOrAwaitValueTest().size, Matchers.`is` (0))
        MatcherAssert.assertThat(reminderListViewModel.showNoData.getOrAwaitValueTest(), Matchers.`is` (true))


    }
    @Test
    fun `when list have data return the data `() = runBlockingTest {

        fakeDataSource.saveReminder(remindersList[0])

        reminderListViewModel.loadReminders()

        MatcherAssert.assertThat( reminderListViewModel.remindersList.getOrAwaitValueTest(), (Matchers.not(emptyList())))
        MatcherAssert.assertThat( reminderListViewModel.remindersList.getOrAwaitValueTest().size, Matchers.`is`(1))
    }
    @Test
    fun `when list not loaded should show loading `()= runBlockingTest{
        fakeDataSource.saveReminder(remindersList[0])

        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()

        MatcherAssert.assertThat(reminderListViewModel.showLoading.getOrAwaitValueTest(), Matchers.`is`(true))

    }

    @Test
    fun `load reminders should return error `()= runBlockingTest{

        fakeDataSource.setReturnError(true)

        reminderListViewModel.loadReminders()

        MatcherAssert.assertThat(reminderListViewModel.showSnackBar.getOrAwaitValueTest(), Matchers.`is`("Tasks not found"))
    }



}