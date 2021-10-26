package com.example.memsto.viewModels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memsto.dataClasses.MemoryItem
import com.example.memsto.firebase.FirebaseObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SharedViewModel : ViewModel() {
    private var userId: String = ""
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
    private val _memoriesDownloadingProgress = MutableLiveData<Loading>()
    val memoriesDownloadingProgress:LiveData<Loading>get() = _memoriesDownloadingProgress

    init {
        getUserData()
        getAllMemories()
    }

    fun getUserData() {
        FirebaseObject.firebaseAuth.currentUser?.let {
            _displayName.value = it.displayName
            _profilePicUri.value = it.photoUrl
            userId = it.uid
        }
    }

    fun uploadMemory(
        memory: String,
        imageUri: Uri,
        date: String
    ) {
        _showUploadingProgress.value = Loading.InProgress
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseObject.storageRef.child("${userId}/$memory _ $date")
                    .putFile(imageUri)
                    .addOnProgressListener { snapShot ->
                        val progress =
                            ((100 * snapShot.bytesTransferred) / (snapShot.totalByteCount)) * 1024
                        _uploadingProgress.postValue(progress.toInt())
                    }
                    .await()
                _showUploadingProgress.postValue(Loading.Uploaded)
            } catch (e: Exception) {
                _showUploadingProgress.postValue(Loading.ErrorOccurred(e.message.toString()))
            }
        }
    }

    fun getAllMemories() {
        _memoriesDownloadingProgress.value = Loading.InProgress
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imagesList = ArrayList<MemoryItem>()
                val images = FirebaseObject.storageRef.child("$userId/").listAll().await()
                for(image in images.items) {
                    val url = image.downloadUrl.await()
                    if(image.name != "profilePic")
                        imagesList.add(MemoryItem(image.name,url))
                }
                _memoriesList.postValue(imagesList)
                _memoriesDownloadingProgress.postValue(Loading.Downloaded)
            }catch (e:Exception) {
                _memoriesDownloadingProgress.postValue(Loading.ErrorOccurred(e.message.toString()))
            }
        }
    }

    sealed class Loading {
        object InProgress : Loading()
        object Uploaded : Loading()
        object Downloaded : Loading()
        data class ErrorOccurred(val error: String) : Loading()
    }

}