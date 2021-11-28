package com.example.memsto.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.memsto.databinding.MemoryDetailFragmentBinding
import com.example.memsto.databinding.ShareMemoryLayoutBinding
import com.example.memsto.viewModels.ChatsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.DelicateCoroutinesApi

class MemoryDetailFragment : Fragment() {
    private lateinit var binding: MemoryDetailFragmentBinding
    private val args: MemoryDetailFragmentArgs by navArgs()
    private val usersAdapter = UsersAdapter()
    private val chatsViewModel: ChatsViewModel by activityViewModels()

    @DelicateCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MemoryDetailFragmentBinding.inflate(inflater)
        setUpViews()
        return binding.root
    }

    @DelicateCoroutinesApi
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setUpViews() {
        binding.apply {
            val memory = args.memory
            Glide.with(requireActivity())
                .load(memory.imageUri)
                .placeholder(resources.getDrawable(R.drawable.ic_image_symbol, null))
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
                Utils.shareMemory(requireContext(),memory,layoutInflater,viewLifecycleOwner)
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

}