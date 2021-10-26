package com.example.memsto.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.example.memsto.R
import com.example.memsto.Utils
import com.example.memsto.adapter.MemoriesAdapter
import com.example.memsto.databinding.HomeFragmentBinding
import com.example.memsto.viewModels.SharedViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.home_fragment.view.*

class HomeFragment : Fragment() {
    private lateinit var binding: HomeFragmentBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private val memoriesAdapter = MemoriesAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater)
        Utils.addBackPressedCallback(this)
        addClickListeners()
        addObservers()
        setUpMemoriesRv()

        return binding.root
    }

    private fun setUpMemoriesRv() = binding.rvImages.apply {
        adapter = memoriesAdapter
        layoutManager = GridLayoutManager(activity, 2)
        setHasFixedSize(true)
    }

    private fun addObservers() {

        viewModel.displayName.observe(viewLifecycleOwner, { string ->
            binding.tvUserName.text = getString(R.string.hello_string, string)
            binding.drawerUserName.text = string
        })
        viewModel.profilePicUri.observe(viewLifecycleOwner, { uri ->
            binding.ivProfile.load(uri) {
                crossfade(true)
                crossfade(300)
                transformations(CircleCropTransformation())
            }
            binding.drawerProfilePic.load(uri) {
                transformations(CircleCropTransformation())
            }
        })

        viewModel.memoriesDownloadingProgress.observe(viewLifecycleOwner, {
            binding.apply {
                when (it) {
                    is SharedViewModel.Loading.InProgress -> {
                        pbMemories.isVisible = true
                    }
                    is SharedViewModel.Loading.ErrorOccurred -> {
                        pbMemories.isVisible = false
                        Toast.makeText(activity, it.error, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        pbMemories.isVisible = false
                    }
                }
            }
        })

        viewModel.memoriesList.observe(viewLifecycleOwner, {
            memoriesAdapter.submitList(it)
            if (it.isEmpty()) {
                binding.tvNoMemory.visibility = View.VISIBLE
                Toast.makeText(activity, "No memories found...!!!", Toast.LENGTH_LONG).show()
            } else
                binding.tvNoMemory.visibility = View.GONE
        })

    }

    private fun addClickListeners() {
        binding.apply {
            tvAddImage.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                this@HomeFragment.findNavController()
                    .navigate(HomeFragmentDirections.actionHomeFragmentToAddImageFragment())
            }

            ivProfile.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            btnChats.setOnClickListener {
                this@HomeFragment.findNavController()
                    .navigate(HomeFragmentDirections.actionHomeFragmentToUsersFragment())
            }

            tvSignOut.setOnClickListener {
                activity?.viewModelStore?.clear()
                Firebase.auth.signOut()
                Toast.makeText(activity, "Logged out successfully.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }

            btnEditProfile.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                this@HomeFragment.findNavController()
                    .navigate(HomeFragmentDirections.actionHomeFragmentToEditProfileFragment())
            }
        }
    }


}