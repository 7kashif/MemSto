package com.example.memsto.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.memsto.dataClasses.MemoryItem
import com.example.memsto.databinding.MemoriesListItemBinding

class MemoriesAdapter : ListAdapter<MemoryItem, MemoriesAdapter.MemoryViewHolder>(diffCallBack) {

    inner class MemoryViewHolder(val binding: MemoriesListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        val diffCallBack = object : DiffUtil.ItemCallback<MemoryItem>() {
            override fun areItemsTheSame(oldItem: MemoryItem, newItem: MemoryItem): Boolean {
                return oldItem.fbDocId == newItem.fbDocId
            }

            override fun areContentsTheSame(oldItem: MemoryItem, newItem: MemoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        return MemoryViewHolder(
            MemoriesListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            holder.itemView.apply {
                tvMemory.text = item.memory
                ivMemoryImage.load(item.imageUri) {
                    crossfade(true)
                    crossfade(300)
                }.isDisposed.apply {
                    progressbar.isVisible  = !this
                }
                root.setOnLongClickListener {
                    memoryLongClickListener?.let {
                        it(item)
                    }
                    true
                }
                root.setOnClickListener {
                    memoryClickListener?.let {
                        it(item)
                    }
                }
            }
        }
    }

    private var memoryLongClickListener: ((MemoryItem) -> Unit)? = null
    private var memoryClickListener: ((MemoryItem) -> Unit)? = null
    fun onMemoryItemClickListener(listener: (MemoryItem)->Unit) {
        memoryClickListener = listener
    }
    fun onMemoryItemLongClickListener(listener: (MemoryItem)->Unit) {
        memoryLongClickListener = listener
    }
}