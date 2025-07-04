package com.extremelygood.abfahrt.classes

import MatchProfile
import android.content.Context
import com.extremelygood.abfahrt.classes.database.AppDatabase
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseManager private constructor(context: Context) {
    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "abfahrt.db"
    ).build()

    private val matchProfileDao = db.matchProfileDao()
    private val userProfileDao = db.userProfileDao()

    suspend fun saveMatchProfile(profile: MatchProfile) {
        withContext(Dispatchers.IO) {
            matchProfileDao.upsert(profile)
        }
    }

    suspend fun getMatchProfile(userId: String): MatchProfile? {
        return withContext(Dispatchers.IO) {
            matchProfileDao.getById(userId)
        }
    }

    suspend fun clearMatches() {
        withContext(Dispatchers.IO) {
            matchProfileDao.clear()
        }
    }

    suspend fun saveMyProfile(profile: UserProfile){
        withContext(Dispatchers.IO){
            userProfileDao.upsert(profile)
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