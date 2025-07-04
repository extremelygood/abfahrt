package com.extremelygood.abfahrt.classes.database

import MatchProfile
import androidx.room.Database
import androidx.room.RoomDatabase
import com.extremelygood.abfahrt.classes.UserProfile

@Database(entities = [MatchProfile::class, UserProfile::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchProfileDao(): MatchProfileDao
    abstract fun userProfileDao(): UserProfileDao
}