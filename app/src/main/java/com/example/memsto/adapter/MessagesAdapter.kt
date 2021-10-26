package com.example.memsto.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memsto.R
import com.example.memsto.dataClasses.MessageItem
import com.example.memsto.databinding.MessageItemBinding
import com.example.memsto.firebase.FirebaseObject

class MessagesAdapter : ListAdapter<MessageItem, MessagesAdapter.MessageViewHolder>(diffCallBack) {
    private var multipleItemSelected: Boolean = false
    val selectedItemList: MutableList<MessageItem> = mutableListOf()

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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MessagesAdapter.MessageViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            holder.itemView.apply {
                messageLayout.setBackgroundColor(resources.getColor(R.color.transparent, null))
                val hrsMins = item.dateTime.substring(
                    item.dateTime.lastIndexOf('_') + 1,
                    item.dateTime.lastIndexOf('_') + 6
                )
                val meridian = item.dateTime.substring(
                    item.dateTime.lastIndexOf(' ') + 1,
                    item.dateTime.length
                )
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

                root.setOnLongClickListener {
                    messageLayout.setBackgroundColor(resources.getColor(R.color.calico_95, null))
                    multipleItemSelected = true
                    if (!selectedItemList.contains(item))
                        selectedItemList.add(item)
                    true
                }
                root.setOnClickListener {
                    if (multipleItemSelected) {
                        if(selectedItemList.contains(item)) {
                            messageLayout.setBackgroundColor(resources.getColor(R.color.transparent, null))
                            selectedItemList.remove(item)
                        } else {
                            selectedItemList.add(item)
                            messageLayout.setBackgroundColor(resources.getColor(R.color.calico_95, null))
                        }
                    }
                    if (selectedItemList.size == 0)
                        multipleItemSelected = false
                }
            }
        }
    }

    private var messageClickListener: ((MessageItem) -> Unit)? = null
//    private var messageLongClickListener: ((Boolean) -> Unit)? = null
    fun onMessageClickListener(listener: (MessageItem) -> Unit) {
        messageClickListener = listener
    }
//    fun onMessageLongClickListener(listener:(Boolean)->Unit) {
//        messageLongClickListener = listener
//    }

}