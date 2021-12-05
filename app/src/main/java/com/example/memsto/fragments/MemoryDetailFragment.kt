package com.example.memsto.fragments

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.memsto.R
import com.example.memsto.Utils
import com.example.memsto.adapter.UsersAdapter
import com.example.memsto.dataClasses.MemoryItem
import com.example.memsto.dataClasses.Progress
import com.example.memsto.databinding.LogoutDialogBinding
import com.example.memsto.databinding.MemoryDetailFragmentBinding
import com.example.memsto.databinding.ShareMemoryLayoutBinding
import com.example.memsto.viewModels.ChatsViewModel
import com.example.memsto.viewModels.SharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MemoryDetailFragment : Fragment() {
    private val requiredPermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private lateinit var binding: MemoryDetailFragmentBinding
    private val args: MemoryDetailFragmentArgs by navArgs()
    private val usersAdapter = UsersAdapter()
    private val chatsViewModel: ChatsViewModel by activityViewModels()
    private val shareViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MemoryDetailFragmentBinding.inflate(inflater)
        setUpViews()
        return binding.root
    }


    private fun setUpViews() {
        binding.apply {
            val memory = args.memory
            Glide.with(requireActivity())
                .load(memory.imageUri)
                .placeholder(requireActivity().getDrawable(R.drawable.ic_image_symbol))
                .into(ivMemory)
            tvMemory.text = memory.memory
            tvMemoryDate.text = memory.date

            ibSendMemory.setOnClickListener {
                showShareBottomSheet(memory)
            }
            ibBack.setOnClickListener {
                findNavController().popBackStack()
            }
            ibShareMemory.setOnClickListener {
                Utils.shareMemory(requireContext(), memory, layoutInflater)
            }
            ibDownloadMemory.setOnClickListener {
                if(!allPermissionsGranted())
                    requestPermissions()
                else {
                    val isSaved = Utils.savePhotoToExternalStorage(memory, requireContext())
                    if(isSaved)
                        Toast.makeText(activity,"Photo saved successfully.",Toast.LENGTH_LONG).show()
                    else
                        Toast.makeText(activity,"Photo not saved.",Toast.LENGTH_LONG).show()
                }
            }
            ibDeleteMemory.setOnClickListener {
                showConfirmationDialog(memory)
            }
        }
        chatsViewModel.usersList.observe(viewLifecycleOwner,{
            usersAdapter.submitList(it)
        })
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
                    Toast.makeText(activity, "Memory Shared Successfully.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        })

        bottomSheet.show()
    }

    private val multiPermissionCallBack = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map->
        if(map.isEmpty())
            Toast.makeText(requireActivity(),"Allow app to store photos.",Toast.LENGTH_LONG).show()
    }

    private fun requestPermissions() {
        multiPermissionCallBack.launch(
            requiredPermissions
        )
    }

    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        )== PackageManager.PERMISSION_GRANTED
    }

    private fun showConfirmationDialog(memoryItem: MemoryItem) {
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

        dialogBinding.btnLogout.text = "Delete"
        dialogBinding.tvConfirmation.text = "Are you sure you want to delete this memory?"

        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnLogout.setOnClickListener {
            shareViewModel.deleteMemory(memoryItem)
            dialog.dismiss()
            this.findNavController().navigate(MemoryDetailFragmentDirections.actionMemoryDetailFragmentToHomeFragment())
        }

        dialog.show()

    }


}