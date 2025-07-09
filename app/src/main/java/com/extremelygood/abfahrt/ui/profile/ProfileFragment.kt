package com.extremelygood.abfahrt.ui.profile

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.extremelygood.abfahrt.utils.ImagePicker
import com.extremelygood.abfahrt.databinding.FragmentProfileBinding
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var imagePicker: ImagePicker
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagePicker = ImagePicker(requireContext(), this, ::profileImageSelected)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.mapView.onCreate(savedInstanceState)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profilePictureButton.setOnClickListener({
            profilePictureClicked()
        })

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

        binding.mapView.getMapAsync { googleMap ->
            val defaultLatLng = LatLng(52.520008, 13.404954)
            val defaultZoomLvl = 8f

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, defaultZoomLvl))
            googleMap.setOnMapClickListener { latLng ->
                mapClicked(latLng)
            }
        }

        initViewModelBindings()

    }

    private fun initViewModelBindings() {
        profileViewModel.profilePicture.observe(viewLifecycleOwner) { newUri ->
            setProfilePicture(newUri)
        }
    }

    private fun setProfilePicture(newUri: Uri?) {
        binding.profilePictureButton.setImageURI(newUri)
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

    private fun profilePictureClicked() {
        imagePicker.startPickImage()
    }

    private fun profileImageSelected(uri: Uri?) {
        profileViewModel.onProfileImageSelected(uri)
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

        Toast.makeText(requireContext(), newLatLng.toString(), Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()

        binding.mapView.onDestroy()

        _binding = null
    }
}