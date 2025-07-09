package com.extremelygood.abfahrt.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.classes.UserProfile
import com.google.android.gms.maps.model.LatLng

class ProfileViewModel(
    private val databaseManager: DatabaseManager
) : ViewModel() {

    private val userProfile = UserProfile()


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
    }

    fun onProfileImageSelected(uri: Uri?) {
        _profilePicture.value = uri
    }

    fun onFirstNameSelected(newFirstName: CharSequence?) {
        _firstName.value = newFirstName.toString()
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

}