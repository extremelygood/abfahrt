package com.extremelygood.abfahrt.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.extremelygood.abfahrt.R
import com.extremelygood.abfahrt.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_ADVERTISE,
        android.Manifest.permission.NEARBY_WIFI_DEVICES,
        android.Manifest.permission.ACCESS_WIFI_STATE,
        android.Manifest.permission.CHANGE_WIFI_STATE,
        android.Manifest.permission.POST_NOTIFICATIONS,
        android.Manifest.permission.ACCESS_WIFI_STATE,
        android.Manifest.permission.CHANGE_WIFI_STATE,
        android.Manifest.permission.INTERNET,
    )

    @Test
    fun testProfileFormInteraction() {
        // Navigiere zum ProfileFragment
        onView(withId(R.id.navigation_profile)).perform(click())

        // Vorname
        onView(withId(R.id.firstNameText))
            .perform(scrollTo(), clearText(), typeText("Max"), closeSoftKeyboard())
            .check(matches(withText("Max")))

        // Nachname
        onView(withId(R.id.lastNameText))
            .perform(scrollTo(), clearText(), typeText("Mustermann"), closeSoftKeyboard())
            .check(matches(withText("Mustermann")))

        // Alter
        onView(withId(R.id.ageField))
            .perform(scrollTo(), clearText(), typeText("27"), closeSoftKeyboard())
            .check(matches(withText("27")))

        // Beschreibung
        onView(withId(R.id.descriptionField))
            .perform(scrollTo(), clearText(), typeText("Mag Espresso"), closeSoftKeyboard())
            .check(matches(withText("Mag Espresso")))

        // Latitude
        onView(withId(R.id.latitudeField))
            .perform(scrollTo(), clearText(), replaceText("52.52"), closeSoftKeyboard())
            .check(matches(withText("52.52")))

        // Longitude
        onView(withId(R.id.longitudeField))
            .perform(scrollTo(), clearText(), replaceText("13.405"), closeSoftKeyboard())
            .check(matches(withText("13.405")))
    }

}
