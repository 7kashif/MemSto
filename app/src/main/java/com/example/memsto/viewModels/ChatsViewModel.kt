package com.example.memsto.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memsto.dataClasses.ChatPersons
import com.example.memsto.dataClasses.MessageItem
import com.example.memsto.dataClasses.UserItem
import com.example.memsto.firebase.FirebaseObject
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatsViewModel : ViewModel() {
    lateinit var chatUser:UserItem
    private var chatCollection : String = ""
    private var currentUser = FirebaseObject.firebaseAuth.currentUser!!
    private val _usersList = MutableLiveData<ArrayList<UserItem>>()
    val usersList: LiveData<ArrayList<UserItem>> get() = _usersList
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    private val _chatList = MutableLiveData<ArrayList<MessageItem>>()
    val chatList:LiveData<ArrayList<MessageItem>> get() = _chatList

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
                value?.let { it ->
                    val list = ArrayList<UserItem>()
                    for (items in it) {
                        val user = items.toObject<UserItem>()
                        if (user.uid != FirebaseObject.firebaseAuth.currentUser?.uid)
                            list.add(user)
                    }
                    _usersList.postValue(list)
                }
            }
        }
    }

    fun findChatCollection() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userIdsOne = chatUser.uid + currentUser.uid
                val userIdSecond = currentUser.uid + chatUser.uid
                val list = mutableListOf(userIdsOne,userIdSecond)
                val persons = ChatPersons(list)
                val query = FirebaseObject.chatsReference.whereArrayContainsAny("users",list).get().await()

                if(query.documents.isEmpty()) {
                    val doc = FirebaseObject.chatsReference.add(persons).await()
                    chatCollection = doc.id
                }
                else {
                    for (item in query.documents)
                        chatCollection = item.id
                }
                getMessages()
            } catch (e:Exception) {
                _errorMessage.postValue(e.message)
            }
        }
    }

    fun sendMessage(message:MessageItem) {
        CoroutineScope(Dispatchers.IO).launch {
           try {
               FirebaseObject.chatsReference
                   .document(chatCollection)
                   .collection("chat")
                   .add(message)
                   .await()
           } catch (e:Exception) {
               _errorMessage.postValue(e.message)
           }
        }
    }

    private fun getMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseObject.chatsReference
                    .document(chatCollection)
                    .collection("chat")
                    .orderBy("dateTime",Query.Direction.ASCENDING)
                    .addSnapshotListener { value, error ->
                        error?.let {
                            _errorMessage.postValue(error.message)
                            return@addSnapshotListener
                        }
                        value?.let { chat->
                            val list = ArrayList<MessageItem>()
                            for(text in chat) {
                                val message = text.toObject<MessageItem>()
                                message.messageId = text.id
                                list.add(message)
                            }
                            _chatList.postValue(list)
                        }
                    }
            } catch (e:Exception) {
                _errorMessage.postValue(e.message)
            }
        }
    }

    fun clearChatList() {
        _chatList.value?.clear()
        chatCollection = ""
    }
}