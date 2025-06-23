package com.extremelygood.abfahrt.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.extremelygood.abfahrt.classes.UserProfile
import com.extremelygood.abfahrt.databinding.FragmentProfileBinding
import com.extremelygood.abfahrt.ui.dashboard.DashboardFragment

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profilePictureButton.setOnClickListener({
            profilePictureClicked()
        })
    }


    private fun profilePictureClicked() {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}