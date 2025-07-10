package com.extremelygood.abfahrt.ui.match

import android.os.Bundle
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
import com.google.android.gms.maps.model.LatLng
import kotlin.getValue

class MatchFragment : Fragment() {

    private var _binding: FragmentMatchBinding? = null
    private val binding get() = _binding!!

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

            // Kamera setzen, keine Marker oder Klicklistener
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, defaultZoomLvl))
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

    private fun displayMatch(matchToDisplay: MatchProfile) {
        binding.firstNameText.setText(matchToDisplay.firstName)
        binding.lastNameText.setText(matchToDisplay.lastName)
        binding.ageField.setText(matchToDisplay.age)
        binding.descriptionField.setText(matchToDisplay.description)
    }

    private fun onBestMatchChanged(bestMatch: MatchProfile?) {
        if (bestMatch == null) {
            displayNoMatch()
        } else {
            displayMatch(bestMatch)
        }
    }
}
