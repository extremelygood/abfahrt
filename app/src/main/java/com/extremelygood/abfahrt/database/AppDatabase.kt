package com.extremelygood.abfahrt.database

import com.extremelygood.abfahrt.classes.Converters
import com.extremelygood.abfahrt.classes.MatchProfile
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.extremelygood.abfahrt.classes.UserProfile

@Database(
    entities = [MatchProfile::class, UserProfile::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchProfileDao(): MatchProfileDao
    abstract fun userProfileDao(): UserProfileDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE match_profile 
            ADD COLUMN firstSeenAt INTEGER NOT NULL DEFAULT 0
        """.trimIndent())
    }
}
