package com.example.memsto.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memsto.R
import com.example.memsto.dataClasses.MessageItem
import com.example.memsto.databinding.MessageItemBinding
import com.example.memsto.firebase.FirebaseObject

class MessagesAdapter(private val context: Context) :
    ListAdapter<MessageItem, MessagesAdapter.MessageViewHolder>(diffCallBack) {
    private var multipleItemSelected: Boolean = false
    val selectedItemList: MutableList<MessageItem> = mutableListOf()

    inner class MessageViewHolder(val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
            @RequiresApi(Build.VERSION_CODES.M)
            fun bind(item: MessageItem) {
                binding.apply {
                    messageLayout.setBackgroundColor(context.resources.getColor(R.color.transparent, null))
                    if (item.senderId == FirebaseObject.firebaseAuth.currentUser!!.uid) {
                        clSentMessage.isVisible = true
                        ivSentMessageTail.isVisible = true
                        tvTimeSent.text = item.dateTime
                        if (URLUtil.isHttpsUrl(item.message)) {
                            ivSentMemory.isVisible = true
                            Glide.with(context).load(item.message).into(ivSentMemory)
                        } else {
                            tvSentMessage.isVisible = true
                            tvSentMessage.text = item.message
                        }
                    } else {
                        clReceivedMessage.isVisible = true
                        ivReceivedMessageTail.isVisible = true
                        tvTimeReceived.text = item.dateTime
                        if (URLUtil.isHttpsUrl(item.message)) {
                            ivReceivedMemory.isVisible = true
                            Glide.with(context).load(item.message).into(ivReceivedMemory)
                        } else {
                            tvReceivedMessage.isVisible = true
                            tvReceivedMessage.text = item.message
                        }
                    }

                    root.setOnLongClickListener {
                        messageLayout.setBackgroundColor(
                            context.resources.getColor(
                                R.color.calico_95,
                                null
                            )
                        )
                        multipleItemSelected = true
                        if (!selectedItemList.contains(item))
                            selectedItemList.add(item)
                        true
                    }

                    root.setOnClickListener {
                        if (multipleItemSelected) {
                            if (selectedItemList.contains(item)) {
                                messageLayout.setBackgroundColor(
                                    context.resources.getColor(
                                        R.color.transparent,
                                        null
                                    )
                                )
                                selectedItemList.remove(item)
                            } else {
                                selectedItemList.add(item)
                                messageLayout.setBackgroundColor(
                                    context.resources.getColor(
                                        R.color.calico_95,
                                        null
                                    )
                                )
                            }
                        }
                        if (selectedItemList.size == 0)
                            multipleItemSelected = false
                    }
                }
            }
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            MessageItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    companion object {
        private val diffCallBack = object : DiffUtil.ItemCallback<MessageItem>() {
            override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
                return oldItem.messageId == newItem.messageId
            }

            override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MessagesAdapter.MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private var messageClickListener: ((MessageItem) -> Unit)? = null
    fun onMessageClickListener(listener: (MessageItem) -> Unit) {
        messageClickListener = listener
    }

}