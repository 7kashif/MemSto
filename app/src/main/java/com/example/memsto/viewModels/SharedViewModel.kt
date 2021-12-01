package com.example.memsto.viewModels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memsto.Utils
import com.example.memsto.dataClasses.MemoryItem
import com.example.memsto.firebase.FirebaseObject
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SharedViewModel : ViewModel() {
    private var userId: String = ""
    private var userFbDocId: String = ""
    private val _profilePicUri = MutableLiveData<Uri>()
    val profilePicUri: LiveData<Uri> get() = _profilePicUri
    private val _displayName = MutableLiveData<String>()
    val displayName: LiveData<String> get() = _displayName
    private val _showUploadingProgress = MutableLiveData<Loading>()
    val showUploadingProgress: LiveData<Loading> get() = _showUploadingProgress
    private val _uploadingProgress = MutableLiveData<Int>()
    val uploadingProgress: LiveData<Int> get() = _uploadingProgress
    private val _memoriesList = MutableLiveData<ArrayList<MemoryItem>>()
    val memoriesList: LiveData<ArrayList<MemoryItem>> get() = _memoriesList
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    init {
        getUserData()
    }

    fun getUserData() {
        FirebaseObject.firebaseAuth.currentUser?.let {
            _displayName.value = it.displayName
            _profilePicUri.value = it.photoUrl
            userId = it.uid
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val query = FirebaseObject.usersReference
                    .whereEqualTo("uid",userId)
                    .get()
                    .await()
                if(query.isEmpty)
                    _errorMessage.postValue("No user Found.")
                else {
                    query.documents.forEach {
                        userFbDocId = it.id
                    }
                }
                getAllMemories()
            } catch (e: Exception) {
                _errorMessage.postValue(e.message.toString())
            }
        }
    }

    fun uploadMemory(
        memoryDetail: String,
        imageUri: Uri,
        memoryDate: String
    ) {
        _showUploadingProgress.value = Loading.InProgress
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentTime = System.currentTimeMillis()
                FirebaseObject.storageRef.child("${userId}/$currentTime")
                    .putFile(imageUri)
                    .addOnProgressListener { snapShot ->
                        val progress =
                            (snapShot.bytesTransferred / snapShot.totalByteCount) * 100
                        _uploadingProgress.postValue(progress.toInt())
                    }
                    .await()

                val url = FirebaseObject.storageRef.child("${userId}/$currentTime")
                    .downloadUrl
                    .await()
                val memoryItem = MemoryItem(
                    memory = memoryDetail,
                    date = memoryDate,
                    imageUri = url.toString(),
                )
                saveMemoryInFirestore(memoryItem)
            } catch (e: Exception) {
                _showUploadingProgress.postValue(Loading.ErrorOccurred(e.message.toString()))
            }
        }
    }

    private fun saveMemoryInFirestore(memory: MemoryItem) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseObject.usersReference
                    .document(userFbDocId)
                    .collection(Utils.MEMORY_ITEMS)
                    .add(memory)
                    .await()
                _showUploadingProgress.postValue(Loading.Uploaded)
            } catch (e: Exception) {
                _showUploadingProgress.postValue(Loading.ErrorOccurred(e.message.toString()))
            }
        }
    }

    private fun getAllMemories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
               FirebaseObject.usersReference
                   .document(userFbDocId)
                   .collection(Utils.MEMORY_ITEMS)
                   .addSnapshotListener { value, error ->
                   error?.let {
                       _errorMessage.postValue(it.message.toString())
                       return@addSnapshotListener
                   }
                   value?.let {items->
                       val list = ArrayList<MemoryItem>()
                       for(item in items) {
                           val mem = item.toObject<MemoryItem>()
                           mem.fbDocId = item.id
                           list.add(mem)
                       }
                       _memoriesList.postValue(list)
                   }
               }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message.toString())
            }
        }
    }

    sealed class Loading {
        object InProgress : Loading()
        object Uploaded : Loading()
        data class ErrorOccurred(val error: String) : Loading()
    }

}