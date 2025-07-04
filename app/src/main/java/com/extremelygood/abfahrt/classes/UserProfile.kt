package com.extremelygood.abfahrt.classes

import kotlinx.serialization.Serializable

@Serializable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "me",
    var firstName: String = "DefaultFirstName",
    var lastName: String = "DefaultLastName",
    var age: Int = -1,
    var description: String = "DefaultDescription",
    var destination: GeoLocation = GeoLocation(),
    var isDriver: Boolean = false,
)

