package com.example.memsto.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memsto.adapter.UsersAdapter
import com.example.memsto.databinding.UsersFragmentBinding
import com.example.memsto.viewModels.ChatsViewModel


class UsersFragment : Fragment() {
    private lateinit var binding: UsersFragmentBinding
    private val usersAdapter = UsersAdapter()
    private val viewModel: ChatsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UsersFragmentBinding.inflate(inflater)
        setUpUsersRv()
        addObservers()
        addClickListeners()

        return binding.root
    }

    private fun setUpUsersRv() = binding.rvUsers.apply {
        adapter = usersAdapter
        layoutManager = LinearLayoutManager(activity)
        setHasFixedSize(true)
    }

    private fun addClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun addObservers() {
        viewModel.usersList.observe(viewLifecycleOwner, {
            usersAdapter.submitList(it)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
    }
}