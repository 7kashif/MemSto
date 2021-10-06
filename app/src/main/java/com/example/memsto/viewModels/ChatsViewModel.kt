package com.example.memsto.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memsto.dataClasses.UserItem
import com.example.memsto.firebase.FirebaseObject
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatsViewModel:ViewModel() {
    private val _usersList = MutableLiveData<ArrayList<UserItem>> ()
    val usersList : LiveData<ArrayList<UserItem>> get() = _usersList
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String> get() = _errorMessage

    init {
        realtimeUserUpdates()
    }

    private fun realtimeUserUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseObject.usersReference.addSnapshotListener { value, error ->
                error?.let {
                    _errorMessage.postValue(error.message)
                    return@addSnapshotListener
                }
                value?.let {
                    val list = ArrayList<UserItem>()
                    for(items in it) {
                        val user = items.toObject<UserItem>()
                        list.add(user)
                    }
                    _usersList.postValue(list)
                }
            }
        }
    }

}