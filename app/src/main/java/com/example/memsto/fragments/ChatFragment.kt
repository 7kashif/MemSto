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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.memsto.R
import com.example.memsto.Utils
import com.example.memsto.adapter.MessagesAdapter
import com.example.memsto.dataClasses.MessageItem
import com.example.memsto.dataClasses.Progress
import com.example.memsto.databinding.ChatFragmentBinding
import com.example.memsto.firebase.FirebaseObject
import com.example.memsto.viewModels.ChatsViewModel
import com.google.firebase.Timestamp

class ChatFragment : Fragment() {
    private lateinit var binding: ChatFragmentBinding
    private val viewModel: ChatsViewModel by activityViewModels()
    private lateinit var messagesAdapter : MessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChatFragmentBinding.inflate(inflater)
        messagesAdapter = MessagesAdapter()
        binding.rvMessages.scrollToPosition(messagesAdapter.getMessages().size-1)
        addClickListeners()
        setUpChatsRv()
        addObserver()

        return binding.root
    }

    private fun setUpChatsRv() = binding.rvMessages.apply {
        adapter = messagesAdapter
        layoutManager = LinearLayoutManager(activity)
        (layoutManager as LinearLayoutManager).stackFromEnd = true
        setHasFixedSize(true)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addObserver() {
        binding.tvName.text = viewModel.chatUser.name
        Glide.with(this)
            .load(viewModel.chatUser.imageUri)
            .placeholder(resources.getDrawable(R.drawable.ic_person,null))
            .circleCrop()
            .into(binding.ivProfile)

        viewModel.chatList.observe(viewLifecycleOwner, {messages->
            messagesAdapter.differ.submitList(messages)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
        })

        viewModel.messageProgress.observe(viewLifecycleOwner,{
            when(it) {
                is Progress.Loading -> {
                    binding.progressbar.isVisible = true
                }
                is Progress.Error -> {
                    binding.progressbar.isVisible = false
                    Toast.makeText(activity, it.e, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.progressbar.isVisible = false
                    val position= messagesAdapter.itemCount
                    binding.rvMessages.scrollToPosition(position-1)
                }
            }
        })

    }

    private fun addClickListeners() {
        messagesAdapter.onMessageClickListener {
            Toast.makeText(activity, it.messageId, Toast.LENGTH_SHORT).show()
        }

        binding.ibBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ibDelete.setOnClickListener {
            if (messagesAdapter.selectedItemList.size == 0)
                Toast.makeText(activity, "No item selected", Toast.LENGTH_SHORT).show()
            else
                viewModel.deleteMessages(messagesAdapter.selectedItemList)
        }

        binding.ibSend.setOnClickListener {
            if (binding.etMessage.text.isNotEmpty()) {
                val message = MessageItem(
                    message = binding.etMessage.text.toString(),
                    senderId = FirebaseObject.firebaseAuth.currentUser!!.uid,
                    dateTime = Utils.getDateAndTime(),
                    timeStamp = Timestamp.now()
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