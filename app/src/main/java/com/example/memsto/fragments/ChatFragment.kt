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
import coil.load
import coil.transform.CircleCropTransformation
import com.example.memsto.Utils
import com.example.memsto.adapter.MessagesAdapter
import com.example.memsto.dataClasses.MessageItem
import com.example.memsto.databinding.ChatFragmentBinding
import com.example.memsto.firebase.FirebaseObject
import com.example.memsto.viewModels.ChatsViewModel

class ChatFragment : Fragment() {
    private lateinit var binding: ChatFragmentBinding
    private val viewModel: ChatsViewModel by activityViewModels()
    private val messagesAdapter = MessagesAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChatFragmentBinding.inflate(inflater)

        binding.rvMessages.scrollToPosition(messagesAdapter.currentList.size - 1)
        addClickListeners()
        setUpChatsRv()
        addObserver()

        return binding.root
    }

    private fun setUpChatsRv() = binding.rvMessages.apply {
        adapter = messagesAdapter
        layoutManager = LinearLayoutManager(activity)
        (layoutManager as LinearLayoutManager).stackFromEnd = true
    }

    private fun addObserver() {
        binding.tvName.text = viewModel.chatUser.name
        binding.ivProfile.load(viewModel.chatUser.imageUri) {
            transformations(CircleCropTransformation())
        }

        viewModel.chatList.observe(viewLifecycleOwner, {
            messagesAdapter.submitList(it)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner,{
            Toast.makeText(activity,it,Toast.LENGTH_SHORT).show()
        })

    }

    private fun addClickListeners() {
        binding.ibBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ibSend.setOnClickListener {
            if (binding.etMessage.text.isNotEmpty()) {
                val message = MessageItem(
                    messageId = "",
                    message = binding.etMessage.text.toString(),
                    senderId = FirebaseObject.firebaseAuth.currentUser!!.uid,
                    dateTime = Utils.getDate() + '_' + Utils.getTime()
                )
                viewModel.sendMessage(message)
                binding.etMessage.text.clear()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.clearChatList()
    }

}