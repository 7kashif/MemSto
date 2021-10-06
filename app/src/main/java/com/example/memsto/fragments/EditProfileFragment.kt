package com.example.memsto.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.example.memsto.databinding.EditProfileFragmentBinding

class EditProfileFragment:Fragment() {
    private lateinit var binding : EditProfileFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EditProfileFragmentBinding.inflate(inflater)

        binding.addImage.setOnClickListener {
            getImage.launch("image/*")
        }

        return binding.root
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let {
            binding.ivProfilePic.load(it) {
                transformations(CircleCropTransformation())
            }
        }
    }

}