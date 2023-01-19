package com.udacity.project4.locationreminders.savereminder


import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValueTest
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])

class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    // change name of list and item
    private var reminderDataItemList= mutableListOf<ReminderDataItem>(
        ReminderDataItem(
            title ="" ,
            description ="test" ,
            longitude = 13.0,
            latitude =14.0,
            location ="55"
        ) ,
        ReminderDataItem(
            title ="test1" ,
            description ="test1" ,
            longitude = 13.0,
            latitude =14.0,
            location =""
        ) , ReminderDataItem(
            title ="test2" ,
            description ="test2" ,
            longitude = 13.0,
            latitude =14.0,
            location ="test2"
        )
    )

    @Before
    fun setup(){
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }



    @After
    fun finish() {
        stopKoin()
    }

    @Test
    fun `when list is no title  should return enter your title `() = runBlockingTest {



        saveReminderViewModel.validateEnteredData(reminderDataItemList[0])

        MatcherAssert.assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValueTest(), Matchers.`is`(
            R.string.err_enter_title)
        )

    }
    @Test
    fun `when list is no location should  return enter your title `() = runBlockingTest {


        saveReminderViewModel.validateEnteredData(reminderDataItemList[1])

        MatcherAssert.assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValueTest(), Matchers.`is`(
            R.string.err_select_location)
        )

    }
    @Test
    fun `when list is not loaded should show loading `() = mainCoroutineRule.runBlockingTest {
            saveReminderViewModel.validateAndSaveReminder(reminderDataItemList[2])

            MatcherAssert.assertThat(saveReminderViewModel.showLoading.getOrAwaitValueTest(), Matchers.`is`(
                true)
            )

        }



    @Test
    fun `when list is  loaded should show massage saved Reminder Saved ! `() = runBlockingTest {

        saveReminderViewModel.validateAndSaveReminder(reminderDataItemList[2])

        Truth.assertThat(saveReminderViewModel.showToast.getOrAwaitValueTest()).isEqualTo("Reminder Saved !")

    }

    @Test
    fun `when save data should navigate go back `() {
        saveReminderViewModel.validateAndSaveReminder(reminderDataItemList[2])
        Truth.assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValueTest()).isEqualTo(
            NavigationCommand.Back)
    }




}