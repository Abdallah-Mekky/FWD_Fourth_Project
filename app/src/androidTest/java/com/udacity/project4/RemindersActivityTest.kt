package com.udacity.project4


import android.app.Application
import android.os.IBinder
import android.os.SystemClock
import android.view.WindowManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.google.android.material.internal.ContextUtils
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class cRemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val activityRule = ActivityTestRule(RemindersActivity::class.java)


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext, get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext, get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get repo
        repository = get()

        //clear the data (repo)
        runBlocking {
            repository.deleteAllReminders()
        }

    }

    @After
    fun finish() {
        stopKoin()
    }

    @Test
    fun check_add_reminder() {
        //Launch Activity Scenario
        val activityScenario = launchActivity<RemindersActivity>()
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //Click to Fab Button
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        //Enter title
        Espresso.onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("title"))
        Espresso.onView(ViewMatchers.withText("title"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //Enter Description
        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("desc"))
        Espresso.onView(ViewMatchers.withText("desc"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //close keyboard
        ViewActions.closeSoftKeyboard()

        //click in select location button navigate to map to chose my location
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())

        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        SystemClock.sleep(5000)
        //click to chose my current location
        uiDevice.findObject(UiSelector().descriptionContains("My Location")).click()

        SystemClock.sleep(3000)

        //click in map to select location
        Espresso.onView(withId(R.id.map)).perform(ViewActions.click())
        SystemClock.sleep(1000)
        //click in save button navigate back
        Espresso.onView(withId(R.id.saveLocation)).perform(ViewActions.click())

        //click in save reminder navigate to activity
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        //check the data is displayed as expected
        Espresso.onView(ViewMatchers.withText("desc"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //close Scenario
        activityScenario.close()
    }


    @Test
    fun show_Snack_When_NoLocation_Added() {

        //Launch Activity Scenario
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        //click in map to select location
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        //click in save button navigate back
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        //check error massage when no location is founded
        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_enter_title)))
        //close Scenario
        scenario.close()
    }


    @Test
    fun saveReminderScreen_showToastMessage() {

        //Launch Activity Scenario
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //Click to Fab Button
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        //Enter title
        Espresso.onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("title"))
        Espresso.onView(ViewMatchers.withText("title"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //Enter Description
        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("desc"))
        Espresso.onView(ViewMatchers.withText("desc"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //close keyboard
        ViewActions.closeSoftKeyboard()

        //click in select location button navigate to map to chose my location
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())

        var uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        SystemClock.sleep(5000)
        //when click should to chose my current location
        uiDevice.findObject(UiSelector().descriptionContains("My Location")).click()

        SystemClock.sleep(3000)

        //when click in map should to select location
        Espresso.onView(withId(R.id.map)).perform(ViewActions.click())

        SystemClock.sleep(1000)
        //when click in save button should navigate back
        Espresso.onView(withId(R.id.saveLocation)).perform(ViewActions.click())

        //when click in save reminder should navigate to activity
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        //check toast appearance
        // problem in toast not work on api 29 && 30
        Espresso.onView(ViewMatchers.withText(R.string.reminder_saved)).inRoot(ViewMatcher())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }


    @Test
    fun saveReminderScreen_emptyTitleSnackBar() {

        //declare activity Scenario
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("desc"))
        ViewActions.closeSoftKeyboard()
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        //stop untill map show
        Thread.sleep(5000)
        Espresso.onView(withId(R.id.map)).perform(ViewActions.longClick())
        SystemClock.sleep(1000)
        Espresso.onView(withId(R.id.map)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.saveLocation)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        val snackBarMessage = appContext.getString(R.string.err_enter_title)
        //Then a SnackBar should appear when trying to save empty Title
        Espresso.onView(ViewMatchers.withText(snackBarMessage))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        activityScenario.close()
    }


    class ViewMatcher : TypeSafeMatcher<Root>() {
        override fun describeTo(description: Description?) {
            description?.appendText("toast")
        }

        override fun matchesSafely(item: Root?): Boolean {
            val type: Int = item!!.windowLayoutParams?.get()?.type ?: 0
            if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                val windowToken: IBinder = item.decorView!!.windowToken
                val appToken: IBinder = item.decorView.applicationWindowToken
                if (windowToken === appToken) {
                    return true
                }
            }
            return false
        }


    }


}
