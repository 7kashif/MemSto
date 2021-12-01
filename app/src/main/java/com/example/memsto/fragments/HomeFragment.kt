package com.example.memsto.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.bumptech.glide.Glide
import com.example.memsto.R
import com.example.memsto.Utils
import com.example.memsto.adapter.MemoriesAdapter
import com.example.memsto.adapter.UsersAdapter
import com.example.memsto.dataClasses.MemoryItem
import com.example.memsto.dataClasses.Progress
import com.example.memsto.databinding.HomeFragmentBinding
import com.example.memsto.databinding.LogoutDialogBinding
import com.example.memsto.databinding.ShareMemoryLayoutBinding
import com.example.memsto.viewModels.ChatsViewModel
import com.example.memsto.viewModels.SharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.home_fragment.view.*

class HomeFragment : Fragment() {
    private lateinit var binding: HomeFragmentBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private val chatsViewModel: ChatsViewModel by activityViewModels()
    private val memoriesAdapter = MemoriesAdapter()
    private val usersAdapter = UsersAdapter()

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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addObservers() {

        viewModel.displayName.observe(viewLifecycleOwner, { string ->
            binding.tvUserName.text = getString(R.string.hello_string, string)
            binding.drawerUserName.text = string
        })
        viewModel.profilePicUri.observe(viewLifecycleOwner, { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(resources.getDrawable(R.drawable.ic_person,null))
                .circleCrop()
                .into(binding.ivProfile)
            binding.drawerProfilePic.load(uri) {
                transformations(CircleCropTransformation())
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

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })

        chatsViewModel.usersList.observe(viewLifecycleOwner, {
            usersAdapter.submitList(it)
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
                showLogoutDialog()
            }

            btnEditProfile.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                this@HomeFragment.findNavController()
                    .navigate(HomeFragmentDirections.actionHomeFragmentToEditProfileFragment())
            }
            memoriesAdapter.onMemoryItemClickListener {
                val bundle = Bundle().apply {
                    putParcelable("memory",it)
                }
                findNavController().navigate(R.id.action_homeFragment_to_memoryDetailFragment,bundle)
            }
            memoriesAdapter.onMemoryItemLongClickListener {
                showShareBottomSheet(it)
            }
        }
    }

    private fun showShareBottomSheet(memory: MemoryItem) {
        val sheetBinding = ShareMemoryLayoutBinding.inflate(layoutInflater)
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.bottomSheetTheme)
        bottomSheet.apply {
            setContentView(sheetBinding.root)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }

        usersAdapter.onItemClickListener {
            chatsViewModel.shareMemory(it.uid, memory)
        }

        sheetBinding.apply {
            rvUsers.adapter = usersAdapter
            rvUsers.layoutManager = LinearLayoutManager(requireContext())
            rvUsers.setHasFixedSize(true)
        }

        chatsViewModel.memoryShareProgress.observe(viewLifecycleOwner, {
            when (it) {
                is Progress.Loading -> {
                    sheetBinding.progressBar.isVisible = true
                }
                is Progress.Error -> {
                    Toast.makeText(activity, it.e, Toast.LENGTH_LONG).show()
                }
                else -> {
                    sheetBinding.progressBar.isVisible = false
                    Toast.makeText(activity,"Memory Shared Successfully.",Toast.LENGTH_LONG).show()
                }
            }
        })

        bottomSheet.show()
    }

    private fun showLogoutDialog() {
        val dialogBinding = LogoutDialogBinding.inflate(layoutInflater)

        val dialog = Dialog(requireActivity())
        dialog.apply {
            setContentView(dialogBinding.root)
            window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawableResource(R.color.transparent)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }

        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnLogout.setOnClickListener {
            activity?.viewModelStore?.clear()
            Firebase.auth.signOut()
            dialog.dismiss()
            Toast.makeText(activity, "Logged out successfully.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
        }

        dialog.show()

    }


}