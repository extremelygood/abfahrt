package com.extremelygood.abfahrt.classes

import com.extremelygood.abfahrt.classes.MatchProfile
import android.content.Context
import androidx.lifecycle.LiveData
import com.extremelygood.abfahrt.database.AppDatabase
import androidx.room.*
import com.extremelygood.abfahrt.database.MIGRATION_1_2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class DatabaseManager private constructor(private val context: Context) {
    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "abfahrt.db"
    ).addMigrations(MIGRATION_1_2)
        .build()

    private val matchProfileDao = db.matchProfileDao()
    private val userProfileDao = db.userProfileDao()

    fun getOrCreateMyUserId(): String {
        val prefs = db.openHelper.readableDatabase.path.let {
            val context = context // Hole dir den Kontext aus der DB
            context.getSharedPreferences("abfahrt_prefs", Context.MODE_PRIVATE)
        }

        return prefs.getString("my_user_id", null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString("my_user_id", it).apply()
        }
    }

    private var onMatchesChangedListener: (() -> Unit)? = null

    fun setOnMatchesChangedListener(callback: (() -> Unit)) {
        onMatchesChangedListener = callback
    }

    suspend fun saveMatchProfile(profile: MatchProfile) {
        withContext(Dispatchers.IO) {
            matchProfileDao.upsert(profile)
            onMatchesChangedListener?.invoke()
        }
    }

    suspend fun getMatchProfile(userId: String): MatchProfile? {
        return withContext(Dispatchers.IO) {
                matchProfileDao.getById(userId)
            }
    }

    suspend fun getAllMatches(limit: Int): List<MatchProfile> {
        return withContext(Dispatchers.IO) {
            matchProfileDao.getAll(limit)
        }
    }

    suspend fun deleteMatchProfile(userId: String) {
        withContext(Dispatchers.IO) {
            matchProfileDao.deleteById(userId)
        }
    }

    suspend fun clearMatches() {
        withContext(Dispatchers.IO) {
            matchProfileDao.clear()
            onMatchesChangedListener?.invoke()
        }
    }

    suspend fun saveMyProfile(profile: UserProfile){
        withContext(Dispatchers.IO){
            userProfileDao.upsert(profile)
        }
    }

    suspend fun loadMyProfile(): UserProfile {
        return withContext(Dispatchers.IO) {
            val id = getOrCreateMyUserId()
            val existing = userProfileDao.getProfile(id)
            if (existing != null) {
                existing
            } else {
                val newProfile = UserProfile(id = id)
                userProfileDao.upsert(newProfile)
                newProfile
            }
        }
    }

    fun getMyUserId(): String? {
        val prefs = context.getSharedPreferences("abfahrt_prefs", Context.MODE_PRIVATE)
        return prefs.getString("my_user_id", null)
    }



    companion object {
        @Volatile private var INSTANCE: DatabaseManager? = null

        fun getInstance(context: Context): DatabaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseManager(context).also { INSTANCE = it }
            }
        }
    }


}