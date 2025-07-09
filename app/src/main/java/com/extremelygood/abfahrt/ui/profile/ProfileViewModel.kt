package com.extremelygood.abfahrt.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.classes.UserProfile
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val databaseManager: DatabaseManager
) : ViewModel() {
    private var saveToDatabaseJob: Job? = null

    private val _userProfile = MutableLiveData<UserProfile>().apply {
        value = UserProfile()
    }
    val userProfile: LiveData<UserProfile> = _userProfile


    private val _text = MutableLiveData<String>().apply {
        value = "This is profile Fragment"
    }
    val text: LiveData<String> = _text

    private val _profilePicture = MutableLiveData<Uri>().apply {

    }
    val profilePicture: LiveData<Uri> = _profilePicture

    private val _firstName = MutableLiveData<String>().apply {
        value = "Init"
    }
    val firstName: LiveData<String> = _firstName

    private val _lastName = MutableLiveData<String>().apply {
        value = "Init"
    }
    val lastName: LiveData<String> = _lastName

    private val _age = MutableLiveData<String>().apply {
        value = "Init"
    }
    val age: LiveData<String> = _age

    private val _description = MutableLiveData<String>().apply {
        value = "Init"
    }
    val description: LiveData<String> = _description

    private val _destination = MutableLiveData<LatLng>().apply {

    }
    val destination: LiveData<LatLng> = _destination



    fun onDestinationSelected(latLng: LatLng) {
        _destination.value = (latLng)

        if (_userProfile.value!!.destination.location.latitude == latLng.latitude &&
            _userProfile.value!!.destination.location.longitude == latLng.longitude) {
            return
        }

        val newProfile = _userProfile.value!!.copy()

        newProfile.destination.location.latitude = latLng.latitude
        newProfile.destination.location.longitude = latLng.longitude

        _userProfile.value = newProfile

        saveNewProfileState()
    }

    fun onProfileImageSelected(uri: Uri?) {
        _profilePicture.value = uri
    }

    fun onFirstNameSelected(newFirstName: CharSequence? = "") {
        _firstName.value = newFirstName.toString()

        if (_userProfile.value!!.firstName.contentEquals(newFirstName)) {
            return
        }

        val newProfile = _userProfile.value!!.copy(firstName = newFirstName.toString())
        _userProfile.value = newProfile

        saveNewProfileState()
    }

    fun onLastNameSelected(newLastName: CharSequence?) {
        _lastName.value = newLastName.toString()
    }

    fun onAgeSelected(newAge: CharSequence?) {
        _age.value = newAge.toString()
    }

    fun onDescriptionSelected(newDescription: CharSequence?) {
        _description.value = newDescription.toString()
    }


    private fun saveNewProfileState() {
        saveToDatabaseJob?.cancel()

        saveToDatabaseJob = viewModelScope.launch {
            delay(1000)
            val currentMutableProfile = _userProfile.value
            if (currentMutableProfile != null) {
                Log.d("ProfileViewModel", "Saving latitude - " + currentMutableProfile.destination.location.latitude)
                Log.d("ProfileViewModel", "Saving longitude - " + currentMutableProfile.destination.location.longitude)

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