package com.example.memsto.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memsto.R
import com.example.memsto.dataClasses.MessageItem
import com.example.memsto.databinding.MessageItemBinding
import com.example.memsto.firebase.FirebaseObject

class MessagesAdapter :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>(){
    private var multipleItemSelected: Boolean = false
    val selectedItemList: MutableList<MessageItem> = mutableListOf()

    inner class MessageViewHolder(val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

            @RequiresApi(Build.VERSION_CODES.M)
            fun bind(item: MessageItem) {
                binding.apply {
                    this@MessageViewHolder.itemView.apply {
                        messageLayout.setBackgroundColor(resources.getColor(R.color.transparent, null))
                        if (item.senderId == FirebaseObject.firebaseAuth.currentUser!!.uid) {
                            cvSentMessage.isVisible = true
                            cvReceivedMessage.isVisible = false
                            tvTimeSent.text = item.dateTime
                            if (URLUtil.isHttpsUrl(item.message)) {
                                ivSentMemory.isVisible = true
                                tvSentMessage.isVisible = false
                                Glide.with(context).load(item.message).into(ivSentMemory)
                            } else {
                                tvSentMessage.isVisible = true
                                ivSentMemory.isVisible = false
                                tvSentMessage.text = item.message
                            }
                        } else {
                            cvReceivedMessage.isVisible = true
                            cvSentMessage.isVisible = false
                            tvTimeReceived.text = item.dateTime
                            if (URLUtil.isHttpsUrl(item.message)) {
                                ivReceivedMemory.isVisible = true
                                tvReceivedMessage.isVisible = false
                                Glide.with(context).load(item.message).into(ivReceivedMemory)
                            } else {
                                tvReceivedMessage.isVisible = true
                                ivReceivedMemory.isVisible = false
                                tvReceivedMessage.text = item.message
                            }
                        }

                        root.setOnLongClickListener {
                            messageLayout.setBackgroundColor(
                                resources.getColor(
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
                                        resources.getColor(
                                            R.color.transparent,
                                            null
                                        )
                                    )
                                    selectedItemList.remove(item)
                                } else {
                                    selectedItemList.add(item)
                                    messageLayout.setBackgroundColor(
                                        resources.getColor(
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

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            MessageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
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

    fun getMessages():MutableList<MessageItem> = differ.currentList

    val differ= AsyncListDiffer(this, diffCallBack)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MessagesAdapter.MessageViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var messageClickListener: ((MessageItem) -> Unit)? = null
    fun onMessageClickListener(listener: (MessageItem) -> Unit) {
        messageClickListener = listener
    }

}