package com.extremelygood.abfahrt.ui.profile

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.extremelygood.abfahrt.AbfahrtApplication
import com.extremelygood.abfahrt.classes.UserProfile
import com.extremelygood.abfahrt.utils.ImagePicker
import com.extremelygood.abfahrt.databinding.FragmentProfileBinding
import com.extremelygood.abfahrt.ui.viewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var imagePicker: ImagePicker
    private var googleMap: GoogleMap? = null
    private var destinationMarker: Marker? = null

    private val profileViewModel: ProfileViewModel by viewModels {
        viewModelFactory {
            ProfileViewModel(
                AbfahrtApplication.appModule.databaseManager
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.mapView.onCreate(savedInstanceState)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Connect all the text input methods to their fields below
        val textMethodMap = mapOf(
            binding.firstNameText to ::firstNameInput,
            binding.lastNameText to ::lastNameInput,
            binding.ageField to ::ageInput,
            binding.descriptionField to ::descriptionInput,
            binding.latitudeField to ::destinationInput,
            binding.longitudeField to ::destinationInput,
        )

        textMethodMap.forEach { entry ->
            val textWatcher: TextWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable?) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    entry.value(s)
                }
            }
            entry.key.addTextChangedListener(textWatcher)
        }

        binding.isDriverSwitch.setOnCheckedChangeListener { _, isChecked ->
            isDriverInput(isChecked)
        }

        binding.mapView.getMapAsync { googleMap ->
            val defaultLatLng = LatLng(52.520008, 13.404954)
            val defaultZoomLvl = 8f

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, defaultZoomLvl))
            googleMap.setOnMapClickListener { latLng ->
                mapClicked(latLng)
            }

            this.googleMap = googleMap
            setDestinationMarker(getCurrentLocationInput())
        }

        initViewModelBindings()

    }

    private fun initViewModelBindings() {
        profileViewModel.userProfile.observe(viewLifecycleOwner) { newProfileState ->
            drawFromProfile(newProfileState)
        }
    }

    private fun drawFromProfile(profile: UserProfile) {

        if (!binding.firstNameText.text.contentEquals(profile.firstName)) {
            binding.firstNameText.setText(profile.firstName)
        }
        if (!binding.lastNameText.text.contentEquals(profile.lastName)) {
            binding.lastNameText.setText(profile.lastName)
        }

        val ageText = if (profile.age < 1) "" else profile.age.toString()
        if (!binding.ageField.text.contentEquals(ageText)) {
            binding.ageField.setText(ageText)
        }
        

        if (!binding.descriptionField.text.contentEquals(profile.description)) {
            binding.descriptionField.setText(profile.description)
        }

        if (!binding.isDriverSwitch.isChecked == profile.isDriver) {
            binding.isDriverSwitch.isChecked = profile.isDriver
        }

        if (!binding.latitudeField.text.contentEquals(profile.destination.location.latitude.toString())) {
            binding.latitudeField.setText(profile.destination.location.latitude.toString())
            setDestinationMarker(getCurrentLocationInput())
        }
        if (!binding.longitudeField.text.contentEquals(profile.destination.location.longitude.toString())) {
            binding.longitudeField.setText(profile.destination.location.longitude.toString())
            setDestinationMarker(getCurrentLocationInput())
        }
    }

    private fun setDestinationMarker(latLng: LatLng?) {
        if (googleMap == null) {
            return
        }

        Log.d("ProfileFragment", "1")

        destinationMarker?.remove()
        destinationMarker = null
        if (latLng == null || (latLng.latitude == 0.toDouble() && latLng.longitude == 0.toDouble())) {
            return
        }

        Log.d("ProfileFragment", "Placing marker at lat " + latLng.latitude + " and long " + latLng.longitude)

        val options = MarkerOptions()
        options.position(latLng)
        options.title("Destination")
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

        destinationMarker = googleMap!!.addMarker(options)

        val cameraUpdate = CameraUpdateFactory.newLatLng(latLng)
        googleMap!!.animateCamera(cameraUpdate)
    }

    private fun getCurrentLocationInput(): LatLng? {

        try {
            val latValue = binding.latitudeField.text.toString().toDouble()
            val longValue = binding.longitudeField.text.toString().toDouble()

            return LatLng(latValue, longValue)
        } catch (e: Exception) {

        }
        return null
    }




    // Input bindings

    private fun mapClicked(latLng: LatLng) {
        binding.latitudeField.setText(latLng.latitude.toString())
        binding.longitudeField.setText(latLng.longitude.toString())

        destinationInput("")
    }

    private fun firstNameInput(newInput: CharSequence?) {
        profileViewModel.onFirstNameSelected(newInput)
    }

    private fun lastNameInput(newInput: CharSequence?) {
        profileViewModel.onLastNameSelected(newInput)
    }

    private fun ageInput(newInput: CharSequence?) {
        profileViewModel.onAgeSelected(newInput)
    }

    private fun descriptionInput(newInput: CharSequence?) {
        profileViewModel.onDescriptionSelected(newInput)
    }

    private fun destinationInput(_unused: CharSequence?) {
        val newLatLng = getCurrentLocationInput()
        if (newLatLng == null) {
            return
        }
        profileViewModel.onDestinationSelected(newLatLng)
        setDestinationMarker(newLatLng)
    }

    private fun isDriverInput(newState: Boolean) {
        profileViewModel.onIsDriverSelected(binding.isDriverSwitch.isChecked)
    }


    override fun onDestroyView() {
        super.onDestroyView()

        binding.mapView.onDestroy()

        _binding = null
    }
}