package com.extremelygood.abfahrt.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is profile Fragment"
    }
    val text: LiveData<String> = _text

    fun onDestinationSelected(latLng: LatLng) {

    }

    fun onProfileImageSelected(uri: Uri?) {

    }

    fun onFirstNameSelected(newFirstName: CharSequence?) {

    }

    fun onLastNameSelected(newLastName: CharSequence?) {

    }

    fun onAgeSelected(newAge: CharSequence?) {

    }

    fun onDescriptionSelected(newDescription: CharSequence?) {

    }

}