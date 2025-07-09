package com.extremelygood.abfahrt

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import com.extremelygood.abfahrt.classes.NotificationHandler
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationHandlerTests {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        NotificationHandler.init(context)
    }

    @Test
    fun notificationChannel_shouldExist() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel("abfahrt_channel")
            assertNotNull("Notification Channel should be created", channel)
            assertEquals("Benachrichtigungen", channel.name)
        }
    }

    @Test
    fun notification_shouldBeEnabledForApp() {
        val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        assertTrue("Notifications should be enabled in system settings", enabled)
    }

    @Test
    fun notification_shouldTriggerWithoutCrash() {
        // Kein assert – nur sicherstellen, dass kein Fehler geworfen wird
        NotificationHandler.showNotification(
            context = context,
            title = "Test",
            message = "Dies ist ein Test",
            notificationId = 123
        )
    }
}