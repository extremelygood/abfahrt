package com.extremelygood.abfahrt.classes.database

import MatchProfile
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MatchProfileDao {
    @Query("SELECT * FROM match_profile WHERE userId = :id")
    suspend fun getById(id: String): MatchProfile?

    @Query("SELECT * FROM match_profile LIMIT :limit")
    suspend fun getAll(limit: Int): List<MatchProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: MatchProfile)

    @Query("DELETE FROM match_profile")
    suspend fun clear()
}