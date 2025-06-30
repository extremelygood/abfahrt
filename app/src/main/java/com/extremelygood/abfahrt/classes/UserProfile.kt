package com.extremelygood.abfahrt.classes

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    var firstName: String = "DefaultFirstName",
    var lastName: String = "DefaultLastName",
    var age: Int = -1,
    var description: String = "DefaultDescription",
    var destination: GeoLocation = GeoLocation(),
    var isDriver: Boolean = false,
)

