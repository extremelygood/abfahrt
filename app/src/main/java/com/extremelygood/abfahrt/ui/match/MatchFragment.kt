package com.extremelygood.abfahrt.ui.match

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.extremelygood.abfahrt.AbfahrtApplication
import com.extremelygood.abfahrt.classes.MatchProfile
import com.extremelygood.abfahrt.databinding.FragmentMatchBinding
import com.extremelygood.abfahrt.ui.profile.ProfileViewModel
import com.extremelygood.abfahrt.ui.viewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.getValue

class MatchFragment : Fragment() {

    private var _binding: FragmentMatchBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var matchDestinationMarker: Marker? = null


    private val matchViewModel: MatchViewModel by viewModels {
        viewModelFactory {
            MatchViewModel(
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
        _binding = FragmentMatchBinding.inflate(inflater, container, false)
        binding.mapView.onCreate(savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.getMapAsync { googleMap ->
            val defaultLatLng = LatLng(52.520008, 13.404954)
            val defaultZoomLvl = 10f

            // Kamera setzen oder Klicklistener
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, defaultZoomLvl))
            this.googleMap = googleMap
        }

        matchViewModel.matchProfile.observe(viewLifecycleOwner) { bestMatch ->
            onBestMatchChanged(bestMatch)
        }

    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    private fun displayNoMatch() {

    }


    private fun setMatchDestinationMarker(newLatLng: LatLng?) {
        if (googleMap == null) {
            return
        }

        matchDestinationMarker?.remove()
        binding.latitudeField.setText("")
        binding.longitudeField.setText("")
        if (newLatLng == null) {
            return
        }

        binding.latitudeField.setText(newLatLng.latitude.toString())
        binding.longitudeField.setText(newLatLng.longitude.toString())

        val markerOptions = MarkerOptions().apply {
            position(newLatLng)
            title("Match Destination")
            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        }

        matchDestinationMarker = googleMap!!.addMarker(markerOptions)

        val cameraUpdate = CameraUpdateFactory.newLatLng(newLatLng)
        googleMap!!.animateCamera(cameraUpdate)
    }


    private fun displayMatch(matchToDisplay: MatchProfile) {
        binding.firstNameText.setText(matchToDisplay.firstName)
        binding.lastNameText.setText(matchToDisplay.lastName)

        var ageAsString: String
        try {
            ageAsString = matchToDisplay.age.toString()
            if (matchToDisplay.age < 1) {
                ageAsString = ""
            }
        } catch(e: Exception) {
            Log.d("MatchFragment", "Exception while trying to convert age to string")
            ageAsString = ""
        }

        binding.ageField.setText(ageAsString)
        binding.descriptionField.setText(matchToDisplay.description)

        val newLatLng = LatLng(matchToDisplay.destination.location.latitude, matchToDisplay.destination.location.longitude)
        setMatchDestinationMarker(newLatLng)
    }

    private fun onBestMatchChanged(bestMatch: MatchProfile?) {
        if (bestMatch == null) {
            displayNoMatch()
        } else {
            displayMatch(bestMatch)
        }
    }
}
