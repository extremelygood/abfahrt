package com.extremelygood.abfahrt.databaseTests

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.classes.UserProfile
import kotlinx.coroutines.runBlocking
import com.extremelygood.abfahrt.classes.MatchProfile
import android.content.Context
import android.location.Location
import com.extremelygood.abfahrt.classes.GeoLocation
import com.extremelygood.abfahrt.database.AppDatabase
import com.extremelygood.abfahrt.database.UserProfileDao
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DatabaseManagerTests {
    private lateinit var db: AppDatabase
    private lateinit var databaseManager: DatabaseManager
    private lateinit var dao: UserProfileDao


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val field = DatabaseManager::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.set(null, null)
        databaseManager = DatabaseManager.getInstance(context)

        val dbField = DatabaseManager::class.java.getDeclaredField("db")
        dbField.isAccessible = true
        dbField.set(databaseManager, db)

        dao = db.userProfileDao()

    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        if (this::db.isInitialized) {
            db.close()
        }
        val field = DatabaseManager::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.set(null, null)
    }

    @Test
    fun testSaveAndGetMatchProfile() = runBlocking {
        val profile = MatchProfile(
            userId = "123",
            firstName = "Test",
            lastName = "User",
            age = 25,
            description = "Default",
            isDriver = true,
            destination = GeoLocation(
                locationName = "Berlin",
                location = Location("").apply {
                    latitude = 52.52
                    longitude = 13.405
                }
            )
        )

        databaseManager.saveMatchProfile(profile)
        val loaded = databaseManager.getMatchProfile("123")

        assertNotNull(loaded)
        assertEquals("Test", loaded?.firstName)
        assertEquals("User", loaded?.lastName)
        assertEquals("Berlin", loaded?.destination?.locationName)
        assertEquals(52.52, loaded?.destination?.location?.latitude)
        assertEquals(13.405, loaded?.destination?.location?.longitude)
    }

    // --------------------------------------------------------------------
    // 2) firstSeenAt‑Timestamp bleibt stabil bei Upsert
    // --------------------------------------------------------------------
    @Test
    fun testFirstSeenTimestampStable() = runBlocking {
        val p1 = MatchProfile(
            userId = "42",
            firstName = "Alice",
            lastName = "A.",
            age = 26,
            description = "🚗",
            isDriver = true,
            destination = GeoLocation()
        )
        databaseManager.saveMatchProfile(p1)
        val first = databaseManager.getMatchProfile("42")!!
        val t1 = first.firstSeenAt
        assertTrue(t1 > 0)

        //  Kurze Pause, dann mit geändertem Text erneut speichern
        Thread.sleep(20)
        val p2 = p1.copy(description = "🚗⚽")
        databaseManager.saveMatchProfile(p2)

        val reloaded = databaseManager.getMatchProfile("42")!!
        assertEquals(t1, reloaded.firstSeenAt)      // Timestamp gleich geblieben
        assertEquals("🚗⚽", reloaded.description)
    }

    // --------------------------------------------------------------------
    // 3) getAllMatches() respektiert Limit
    // --------------------------------------------------------------------
    @Test
    fun testGetAllMatchesLimit() = runBlocking {
        repeat(3) { idx ->
            databaseManager.saveMatchProfile(
                MatchProfile(
                    userId = "u$idx",
                    firstName = "N°$idx",
                    lastName = "Test",
                    age = 20 + idx,
                    description = "p$idx",
                    isDriver = false,
                    destination = GeoLocation()
                )
            )
        }

        val one = databaseManager.getAllMatches(1)
        val two = databaseManager.getAllMatches(2)

        assertEquals(1, one.size)
        assertEquals(2, two.size)
    }

    // --------------------------------------------------------------------
    // 4) clearMatches() leert die Tabelle
    // --------------------------------------------------------------------
    @Test
    fun testClearMatches() = runBlocking {
        val profile = MatchProfile(
            userId = "456",
            firstName = "To",
            lastName = "Delete",
            age = 30,
            description = "Default",
            isDriver = true,
            destination = GeoLocation()
        )
        databaseManager.saveMatchProfile(profile)
        databaseManager.clearMatches()

        val loaded = databaseManager.getMatchProfile("456")
        assertNull(loaded)
    }

    // --------------------------------------------------------------------
    // 5) Speichern und Laden des eigenen UserProfiles
    // --------------------------------------------------------------------
    @Test
    fun testSaveAndLoadUserProfile() = runBlocking {
        val me = UserProfile(
            firstName = "Emil",
            lastName = "Schmiade",
            age = 20,
            description = "App‑Dev",
            isDriver = true
        )
        databaseManager.saveMyProfile(me)
        val loaded = databaseManager.loadMyProfile()

        assertNotNull(loaded)
        assertEquals("Emil", loaded!!.firstName)
        assertEquals(true, loaded.isDriver)
    }
    @Test
    fun testUpsertMatchProfileUpdatesFieldsButKeepsTimestamp() = runBlocking {
        val original = MatchProfile(
            userId = "999",
            firstName = "Original",
            lastName = "User",
            age = 40,
            description = "First desc",
            isDriver = false,
            destination = GeoLocation()
        )
        databaseManager.saveMatchProfile(original)
        val loadedOriginal = databaseManager.getMatchProfile("999")!!
        val timestampBefore = loadedOriginal.firstSeenAt

        // Jetzt updaten mit anderem Namen und Beschreibung, Timestamp soll gleich bleiben
        val updated = original.copy(firstName = "Updated", description = "New desc")
        databaseManager.saveMatchProfile(updated)
        val loadedUpdated = databaseManager.getMatchProfile("999")!!

        assertEquals(timestampBefore, loadedUpdated.firstSeenAt)
        assertEquals("Updated", loadedUpdated.firstName)
        assertEquals("New desc", loadedUpdated.description)
    }

    @Test
    fun testGetMatchProfileReturnsNullIfNotExists() = runBlocking {
        val loaded = databaseManager.getMatchProfile("non_existing_id")
        assertNull(loaded)
    }

    @Test
    fun testSaveAndLoadUserProfileDefaults() = runBlocking {
        val defaultProfile = UserProfile()
        databaseManager.saveMyProfile(defaultProfile)
        val loaded = databaseManager.loadMyProfile()
        assertNotNull(loaded)
        assertEquals("DefaultFirstName", loaded!!.firstName)
        assertEquals("DefaultLastName", loaded.lastName)
        assertEquals("DefaultDescription", loaded.description)
        assertEquals(false, loaded.isDriver)
    }

    @Test
    fun testGetAllMatchesWithZeroLimitReturnsEmptyList() = runBlocking {
        databaseManager.saveMatchProfile(
            MatchProfile(
                userId = "a",
                firstName = "A",
                lastName = "LastA",
                age = 20,
                description = "desc",
                isDriver = false,
                destination = GeoLocation()
            )
        )
        val result = databaseManager.getAllMatches(0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testGeoLocationSerializationAndStorage() = runBlocking {
        // 1. Testdaten
        val location = android.location.Location("test").apply {
            latitude = 52.52
            longitude = 13.405
        }

        val geo = GeoLocation(locationName = "Berlin", location = location)

        val user = UserProfile(
            id = "me",
            firstName = "Test",
            lastName = "User",
            age = 30,
            description = "Test desc",
            destination = geo,
            isDriver = true
        )

        // 2. In DB speichern
        dao.upsert(user)

        // 3. Wieder auslesen
        val fromDb = dao.getProfile("me")
        requireNotNull(fromDb)

        // 4. Vergleich
        assertEquals("Berlin", fromDb.destination.locationName)
        assertEquals(52.52, fromDb.destination.location.latitude, 0.0001)
        assertEquals(13.405, fromDb.destination.location.longitude, 0.0001)

        // 5. Direkter JSON-Vergleich
        val json = Json { encodeDefaults = true }
        val serialized = json.encodeToString(GeoLocation.serializer(), geo)
        val expected = """{"locationName":"Berlin","location":{"latitude":52.52,"longitude":13.405}}"""

        assertEquals(expected, serialized)
    }

}
