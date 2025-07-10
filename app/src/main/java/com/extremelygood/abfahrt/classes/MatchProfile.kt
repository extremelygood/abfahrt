package com.extremelygood.abfahrt.classes


import androidx.room.*
import androidx.room.PrimaryKey

// Datenklasse (Entity)
@Entity(tableName = "match_profile")
data class MatchProfile(
    @PrimaryKey val userId: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val description: String,
    val isDriver: Boolean,
    @Embedded val destination: GeoLocation,

    @ColumnInfo(defaultValue = "0")
    val firstSeenAt: Long = System.currentTimeMillis()
)