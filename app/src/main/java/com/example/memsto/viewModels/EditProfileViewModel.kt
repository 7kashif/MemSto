package com.example.memsto.viewModels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memsto.firebase.FirebaseObject
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditProfileViewModel : ViewModel() {
    private val _progress = MutableLiveData<Progress>()
    val progress: LiveData<Progress> get() = _progress


    fun updateProfile(userName: String, photoUrl: Uri) {
        _progress.postValue(Progress.Uploading)
        val uid = FirebaseObject.firebaseAuth.currentUser?.uid.toString()
        val updateMap = mutableMapOf<String, Any>()
        updateMap["name"] = userName
        updateMap["uid"] = uid
        updateMap["imageUri"] = photoUrl.toString()

        val updateRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .setPhotoUri(photoUrl)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseObject.firebaseAuth.currentUser?.updateProfile(updateRequest)?.await()
                updateUserInFirestore(uid, updateMap)
            } catch (e: Exception) {
                _progress.postValue(Progress.Error(e.message.toString()))
            }
        }
    }

    private fun updateUserInFirestore(uid: String, updateMap: Map<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            val userQuery = FirebaseObject.usersReference
                .whereEqualTo("uid", uid)
                .get()
                .await()
            if (userQuery.documents.isNotEmpty()) {
               userQuery.forEach { user ->
                   try {
                       FirebaseObject.usersReference.document(user.id).set(
                           updateMap,
                           SetOptions.merge()
                       )
                       _progress.postValue(Progress.Done)
                   } catch (e: Exception) {
                       _progress.postValue(Progress.Error(e.message.toString()))
                   }
               }
            }
        }
    }

    sealed class Progress {
        object Uploading : Progress()
        object Done : Progress()
        data class Error(val error: String) : Progress()
    }

}