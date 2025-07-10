package com.extremelygood.abfahrt.classes

import com.extremelygood.abfahrt.classes.MatchProfile
import android.content.Context
import androidx.lifecycle.LiveData
import com.extremelygood.abfahrt.database.AppDatabase
import androidx.room.*
import com.extremelygood.abfahrt.database.MIGRATION_1_2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseManager private constructor(context: Context) {
    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "abfahrt.db"
    ).addMigrations(MIGRATION_1_2)
        .build()

    private val matchProfileDao = db.matchProfileDao()
    private val userProfileDao = db.userProfileDao()

    private var onMatchesChangedListener: (() -> Unit)? = null
    private var onProfileChangedListener: (() -> Unit)? = null

    fun setOnMatchesChangedListener(callback: (() -> Unit)) {
        onMatchesChangedListener = callback
    }

    fun setOnProfileChangedListener(callback: (() -> Unit)) {
        onProfileChangedListener = callback
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
            onMatchesChangedListener?.invoke()
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
            onProfileChangedListener?.invoke()
        }
    }

    suspend fun loadMyProfile(): UserProfile? {
        return withContext(Dispatchers.IO) {
            userProfileDao.getProfile()
        }
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