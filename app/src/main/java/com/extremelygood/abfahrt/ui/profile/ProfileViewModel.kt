package com.extremelygood.abfahrt.ui.profile

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.classes.GeoLocation
import com.extremelygood.abfahrt.classes.UserProfile
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min


const val MAX_AGE = 100
const val MAX_SHORT_CHARS = 35

class ProfileViewModel(
    private val databaseManager: DatabaseManager
) : ViewModel() {
    private var saveToDatabaseJob: Job? = null

    private val _userProfile = MutableLiveData<UserProfile>().apply {
        value = UserProfile()
    }
    val userProfile: LiveData<UserProfile> = _userProfile


    fun onDestinationSelected(latLng: LatLng) {
        val current = _userProfile.value ?: return

        // nothing changed
        if (current.destination.location.latitude  == latLng.latitude &&
            current.destination.location.longitude == latLng.longitude) return

        val freshLocation = Location(GeoLocation.PROVIDER_MANUAL).apply {
            latitude  = latLng.latitude
            longitude = latLng.longitude
        }

        val updatedProfile = current.copy(
            destination = current.destination.copy(location = freshLocation)
        )

        _userProfile.value = updatedProfile
        saveNewProfileState()
    }

    fun onFirstNameSelected(newFirstName: CharSequence? = "") {

        if (_userProfile.value!!.firstName.contentEquals(newFirstName)) {
            return
        }

        var nameToBeAssigned = newFirstName

        if (newFirstName != null) {
            if (newFirstName.length > MAX_SHORT_CHARS) {
                nameToBeAssigned = ""
            }
        }

        val newProfile = _userProfile.value!!.copy(firstName = nameToBeAssigned.toString())
        _userProfile.value = newProfile

        saveNewProfileState()
    }

    fun onLastNameSelected(newLastName: CharSequence?) {

        if (_userProfile.value!!.lastName.contentEquals(newLastName)) {
            return
        }

        var nameToBeAssigned = newLastName

        if (newLastName != null) {
            if (newLastName.length > MAX_SHORT_CHARS) {
                nameToBeAssigned = ""
            }
        }


        val newProfile = _userProfile.value!!.copy(lastName = nameToBeAssigned.toString())
        _userProfile.value = newProfile

        saveNewProfileState()
    }

    fun onAgeSelected(newAge: CharSequence?) {

        var ageAsInt: Int? = newAge.toString().toIntOrNull()
        if (ageAsInt == null) {
            return
        }

        val ageTobeAssigned = min(ageAsInt, MAX_AGE)

        if (_userProfile.value!!.age == ageTobeAssigned) {
            return
        }

        val newProfile = _userProfile.value!!.copy(age = ageTobeAssigned)
        _userProfile.value = newProfile

        saveNewProfileState()
    }

    fun onDescriptionSelected(newDescription: CharSequence?) {
        if (_userProfile.value!!.description.contentEquals(newDescription)) {
            return
        }

        val newProfile = _userProfile.value!!.copy(description = newDescription.toString())
        _userProfile.value = newProfile

        saveNewProfileState()
    }

    fun onIsDriverSelected(newState: Boolean) {
        if (_userProfile.value!!.isDriver == newState) {
            return
        }

        val newProfile = _userProfile.value!!.copy(isDriver = newState)
        _userProfile.value = newProfile

        saveNewProfileState()
    }

    private fun saveNewProfileState() {
        saveToDatabaseJob?.cancel()

        saveToDatabaseJob = viewModelScope.launch {
            delay(1000)
            val currentMutableProfile = _userProfile.value
            if (currentMutableProfile != null) {
                databaseManager.saveMyProfile(currentMutableProfile)
            }
        }
    }

    init {
        viewModelScope.launch {
            val dbProfile = databaseManager.loadMyProfile()
            if (dbProfile != null) {
                _userProfile.value = dbProfile
            }
        }
    }
}