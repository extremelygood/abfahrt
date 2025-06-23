package com.extremelygood.abfahrt.classes

data class UserProfile(
    var firstName: String = "DefaultFirstName",
    var lastName: String = "DefaultLastName",
    var age: Int = -1,
    var description: String = "DefaultDescription",
    var destination: GeoLocation = GeoLocation(),
    var isDriver: Boolean = false,
)

