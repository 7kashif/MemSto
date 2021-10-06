package com.example.memsto.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.memsto.dataClasses.UserItem
import com.example.memsto.databinding.UsersListItemBinding

class UsersAdapter : ListAdapter<UserItem,UsersAdapter.UserViewHolder>(diffCallBack) {

    inner class UserViewHolder(val binding : UsersListItemBinding):RecyclerView.ViewHolder(binding.root)

    companion object {
        private val diffCallBack = object : DiffUtil.ItemCallback<UserItem>() {
            override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
                return oldItem.uid==newItem.uid
            }

            override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
                return oldItem==newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            UsersListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            holder.itemView.apply {
                tvName.text = item.name
                ivDp.load(item.imageUri) {
                    crossfade(true)
                    crossfade(500)
                    transformations(CircleCropTransformation())
                }
            }
        }
    }
}