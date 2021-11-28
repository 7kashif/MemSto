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
import coil.load
import coil.transform.CircleCropTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addObserver() {
        binding.tvName.text = viewModel.chatUser.name
        Glide.with(this)
            .load(viewModel.chatUser.imageUri)
            .placeholder(resources.getDrawable(R.drawable.ic_person,null))
            .circleCrop()
            .into(binding.ivProfile)

        viewModel.chatList.observe(viewLifecycleOwner, {
            messagesAdapter.submitList(it)
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
            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
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

//    private fun addSwipeListener() {
//
//        ItemTouchHelper(object :
//            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean = true
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                messagesAdapter.showMessageTimeTv()
//            }
//
//            override fun onChildDraw(
//                c: Canvas,
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                dX: Float,
//                dY: Float,
//                actionState: Int,
//                isCurrentlyActive: Boolean
//            ) {
//                super.onChildDraw(
//                    c,
//                    recyclerView,
//                    viewHolder,
//                    0F,
//                    0F,
//                    actionState,
//                    isCurrentlyActive
//                )
//            }
//
//        }).attachToRecyclerView(binding.rvMessages)
//    }

    override fun onPause() {
        super.onPause()
        viewModel.clearChatList()
    }

}