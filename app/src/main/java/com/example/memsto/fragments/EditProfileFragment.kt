package com.example.memsto.fragments

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.example.memsto.R
import com.example.memsto.databinding.EditProfileFragmentBinding
import com.example.memsto.databinding.ImageChoiceDialogBinding
import com.example.memsto.firebase.FirebaseObject
import com.example.memsto.viewModels.EditProfileViewModel
import com.example.memsto.viewModels.SharedViewModel

class EditProfileFragment : Fragment() {
    private lateinit var binding: EditProfileFragmentBinding
    private val viewModel: EditProfileViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var userName: String? = null
    private var photoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EditProfileFragmentBinding.inflate(inflater)
        addClickListeners()
        addObservers()

        return binding.root
    }


    private fun updateProfile() {
        binding.apply {
            userName = if (etName.text?.isNotEmpty() == true)
                etName.text.toString()
            else
                null

            if (photoUri == null && userName != null) {
                photoUri = FirebaseObject.firebaseAuth.currentUser?.photoUrl
                viewModel.updateProfile(userName!!, photoUri!!)
            } else if (userName == null && photoUri != null) {
                userName = FirebaseObject.firebaseAuth.currentUser?.displayName
                viewModel.updateProfile(userName!!, photoUri!!)
            } else if (userName != null && photoUri != null)
                viewModel.updateProfile(userName!!, photoUri!!)
            else
                Toast.makeText(activity, "You haven't changed anything...", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private fun addObservers() {
        viewModel.progress.observe(viewLifecycleOwner, {
            when (it) {
                is EditProfileViewModel.Progress.Uploading -> {
                    binding.progressbar.isVisible = true
                }
                is EditProfileViewModel.Progress.Error -> {
                    binding.progressbar.isVisible = false
                    Toast.makeText(activity, it.error, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.progressbar.isVisible = false
                    Toast.makeText(activity, "Successfully updated profile...", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    private fun addClickListeners() {
        binding.apply {
            addImage.setOnClickListener {
                getImage.launch("image/*")
            }

            btnSaveChanges.setOnClickListener {
                updateProfile()
            }

            btnBack.setOnClickListener {
                sharedViewModel.getUserData()
                findNavController().popBackStack()
            }
        }
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let {
            photoUri = it
            binding.ivProfilePic.load(it) {
                transformations(CircleCropTransformation())
            }
        }
    }

}