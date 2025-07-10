package com.extremelygood.abfahrt.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
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
class MatchFragmentTest {

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
    fun testMatchFragmentDisplaysCorrectly() {
        // Navigiere zum MatchFragment
        onView(withId(R.id.navigation_match)).perform(click())

        // Vorname sichtbar
        onView(withId(R.id.firstNameText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        // Nachname sichtbar
        onView(withId(R.id.lastNameText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        // Alter sichtbar
        onView(withId(R.id.ageField))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        // Beschreibung sichtbar
        onView(withId(R.id.descriptionField))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        // Latitude sichtbar
        onView(withId(R.id.latitudeField))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        // Longitude sichtbar
        onView(withId(R.id.longitudeField))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        // Map sichtbar
        onView(withId(R.id.mapView))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }
}
