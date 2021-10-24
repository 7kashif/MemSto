package com.example.memsto.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memsto.dataClasses.MessageItem
import com.example.memsto.databinding.MessageItemBinding
import com.example.memsto.firebase.FirebaseObject

class MessagesAdapter : ListAdapter<MessageItem, MessagesAdapter.MessageViewHolder>(diffCallBack) {

    inner class MessageViewHolder(val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root)

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

    override fun onBindViewHolder(holder: MessagesAdapter.MessageViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            holder.itemView.apply {
                val hrsMins = item.dateTime.substring(item.dateTime.lastIndexOf('_')+1,item.dateTime.lastIndexOf('_')+6)
                val meridian = item.dateTime.substring(item.dateTime.lastIndexOf(' ')+1,item.dateTime.length)
                val time = "$hrsMins $meridian"
                if (item.senderId == FirebaseObject.firebaseAuth.currentUser!!.uid) {
                    sentMessageLayout.isVisible = true
                    tvSentMessage.text = item.message
                    tvTimeSent.text = time
                } else {
                    receivedMessageLayout.isVisible = true
                    tvReceivedMessage.text = item.message
                    tvTimeReceived.text = time
                }
            }
        }
    }

}