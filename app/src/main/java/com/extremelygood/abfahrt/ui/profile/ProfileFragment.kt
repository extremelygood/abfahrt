package com.extremelygood.abfahrt.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.extremelygood.abfahrt.databinding.FragmentProfileBinding

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

        // Connect all the text input methods to their fields below
        val textMethodMap = mapOf(
            binding.firstNameText to ::firstNameInput,
            binding.lastNameText to ::lastNameInput,
            binding.ageField to ::ageInput,
            binding.descriptionField to ::descriptionInput,
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

    }


    private fun profilePictureClicked() {
        Toast.makeText(requireContext(), "Hello whats up", Toast.LENGTH_SHORT).show()
    }

    private fun firstNameInput(newInput: CharSequence?) {
        Toast.makeText(requireContext(), newInput, Toast.LENGTH_SHORT).show()
    }

    private fun lastNameInput(newInput: CharSequence?) {

    }

    private fun ageInput(newInput: CharSequence?) {

    }

    private fun descriptionInput(newInput: CharSequence?) {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}