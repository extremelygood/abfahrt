package com.extremelygood.abfahrt.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.extremelygood.abfahrt.classes.MatchProfile

@Dao
interface MatchProfileDao {
    @Query("SELECT * FROM match_profile WHERE userId = :id")
    suspend fun getById(id: String): MatchProfile?

    @Query("SELECT * FROM match_profile LIMIT :limit")
    suspend fun getAll(limit: Int): List<MatchProfile>

    /** passt nur Felder an, die sich ändern dürfen – Timestamp bleibt */
    @Update(entity = MatchProfile::class)
    suspend fun update(profile: MatchProfile)

    /** INSERT mit IGNORE: liefert -1, falls row schon existiert -> Timestamp bleibt */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(profile: MatchProfile): Long

    suspend fun upsert(profile: MatchProfile) {
        val inserted = insert(profile)
        if (inserted == -1L) {
            update(profile)
        }
    }

    @Query("DELETE FROM match_profile WHERE userId = :userId")
    suspend fun deleteById(userId: String)


    @Query("DELETE FROM match_profile")
    suspend fun clear()

    @Query("SELECT * FROM match_profile WHERE userId = :userId LIMIT 1")
    fun getByIdLive(userId: String): LiveData<MatchProfile?>

    @Query("SELECT * FROM match_profile LIMIT :limit")
    fun getAllLive(limit: Int): LiveData<List<MatchProfile>>
}