package com.extremelygood.abfahrt.classes

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Serializable
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "me",
    var firstName: String = "",
    var lastName: String = "",
    var age: Int = -1,
    var description: String = "",
    @Embedded var destination: GeoLocation = GeoLocation(),
    var isDriver: Boolean = false,
)

