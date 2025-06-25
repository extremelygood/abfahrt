import android.content.Context
import androidx.room.*
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Datenklasse (Entity)
@Entity
data class MatchProfile(
    @PrimaryKey val userId: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val description: String,
    val isDriver: Boolean,
    @Embedded val destination: GeoLocation
)

// Eingebettete Klasse für Geolocation
data class GeoLocation(
    val latitude: Double,
    val longitude: Double
)

// DAO-Schnittstelle
@Dao
interface MatchProfileDao {
    @Query("SELECT * FROM MatchProfile WHERE userId = :id")
    suspend fun getById(id: String): MatchProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: MatchProfile)

    @Query("DELETE FROM MatchProfile")
    suspend fun clear()
}

// Datenbankklasse
@Database(entities = [MatchProfile::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchProfileDao(): MatchProfileDao
}

// Datenbank-Manager
class DataBaseManager private constructor(context: Context) {
    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "abfahrt.db"
    ).build()

    private val dao = db.matchProfileDao()

    suspend fun saveMatchProfile(profile: MatchProfile) {
        withContext(Dispatchers.IO) {
            dao.upsert(profile)
        }
    }

    suspend fun getMatchProfile(userId: String): MatchProfile? {
        return withContext(Dispatchers.IO) {
            dao.getById(userId)
        }
    }

    suspend fun clearMatches() {
        withContext(Dispatchers.IO) {
            dao.clear()
        }
    }

    companion object {
        @Volatile private var INSTANCE: DataBaseManager? = null

        fun getInstance(context: Context): DataBaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataBaseManager(context).also { INSTANCE = it }
            }
        }
    }
}