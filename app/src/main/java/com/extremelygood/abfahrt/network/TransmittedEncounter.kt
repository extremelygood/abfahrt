package com.extremelygood.abfahrt.network

import com.extremelygood.abfahrt.classes.UserProfile
import kotlinx.serialization.Serializable

@Serializable
data class TransmittedEncounter(
    val userProfile: UserProfile = UserProfile(),
    val encounterTime: Long = 0
)