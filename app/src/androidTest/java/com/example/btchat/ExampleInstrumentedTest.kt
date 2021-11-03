package com.example.btchat

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    //per verifica connessione Bt
    private var bluetoothAdapter: BluetoothAdapter? = null
    /*AGGIORNARE CON IL METODO NUOVO
      https://stackoverflow.com/questions/67122525/intentsrule-deprecated-espresso
      lateinit var scenario: ActivityScenario<MainActivity>
      val intent = Intent(ApplicationProvider.getApplicationContext(), EnableBluetoothActivity::class.java)
     */

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.btchat", appContext.packageName)
    }

    //verifica di base di navigazione
    @Test
    fun verificaNavigazione(){
        Espresso.onView(withId(R.id.buttonChoice)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("Selecting devices")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("Selecting devices")).perform(click())
        Espresso.onView(ViewMatchers.withText("Unità Bt accoppiate e disponibili")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Thread.sleep(10000)
        Espresso.onView(ViewMatchers.withId(R.id.chat_button)).perform(click())
    }

    @Test
    fun verificaVisualizzazione(){
        Espresso.onView(withId(R.id.buttonChoice)).perform(click())
        Thread.sleep(10000)
        //intended(IntentMatchers.hasAction(
          //  BluetoothAdapter.ACTION_REQUEST_ENABLE))
        Espresso.onView(ViewMatchers.withText("RPE-00002")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("RPE-00002")).perform(click())
        Espresso.onView(ViewMatchers.withText("E8:EB:1B:92:70:4D")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun riverificaDopoRitorno(){
        Espresso.onView(withId(R.id.buttonChoice)).perform(click())
        Thread.sleep(10000)
        Espresso.onView(ViewMatchers.withText("RPE-00002")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("RPE-00002")).perform(click())
        Espresso.onView(ViewMatchers.withText("E8:EB:1B:92:70:4D")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //ritorna e riverifica
        Espresso.pressBack()
        Thread.sleep(10000)
        Espresso.onView(ViewMatchers.withText("RPE-00002")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("RPE-00002")).perform(click())
        Espresso.onView(ViewMatchers.withText("E8:EB:1B:92:70:4D")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun verificaConnessione(){

    }
}