package com.example.memsto.viewModels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memsto.dataClasses.UserItem
import com.example.memsto.firebase.FirebaseObject
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpViewModel : ViewModel() {
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean> get() = _showProgressBar
    private val _goToHomeFragment = MutableLiveData<Boolean>()
    val goToHomeFragment: LiveData<Boolean> get() = _goToHomeFragment
    private val _progressText = MutableLiveData<String>()
    val progressText: LiveData<String> get() = _progressText
    private val _picUploadingProgress = MutableLiveData<Int>()
    val picUploadingProgress: LiveData<Int> get() = _picUploadingProgress
    private val _showDoneIcon = MutableLiveData<Boolean>()
    val showDoneIcon: LiveData<Boolean> get() = _showDoneIcon

    private val _signUpProgress = MutableLiveData<SignUpProgress>()
    val signUpProgress : LiveData<SignUpProgress> get() = _signUpProgress

    fun signUp(
        name: String,
        imageUri: Uri,
        email: String,
        password: String
    ) {
        _showProgressBar.value = true
        _progressText.value = "Creating user..."
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseObject.firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                _progressText.postValue("Uploading profile picture...")
                updateUserProfile(imageUri, name)
            } catch (e: Exception) {
                _showProgressBar.postValue(false)
                _errorMessage.postValue(e.message)
            }
        }
    }

    private fun updateUserProfile(imageUri: Uri, name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = FirebaseObject.firebaseAuth.currentUser?.uid
                FirebaseObject.storageRef.child("$uid/profilePic") //uploading user's profile pic to firebase storage
                    .putFile(imageUri)
                    .addOnProgressListener {
                        val progress = ((100 * it.bytesTransferred) / it.totalByteCount).toInt()
                        _picUploadingProgress.postValue(progress)
                    }
                    .await()
                _progressText.postValue("Updating profile...")

                val url =
                    FirebaseObject.storageRef.child("$uid/profilePic").downloadUrl.await() //getting profile pic url from storage
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .setPhotoUri(url)
                    .build()

                FirebaseObject.firebaseAuth.currentUser?.updateProfile(profileUpdate)
                    ?.await() //updating profile
                createUserInFirestore(name, uid!!, url.toString())

                _progressText.postValue("Done...!")
                _showDoneIcon.postValue(true)
                delay(1000)
                _showProgressBar.postValue(false)
                _goToHomeFragment.postValue(true)
            } catch (e: Exception) {
                _showProgressBar.postValue(false)
                _errorMessage.postValue(e.message)
            }
        }
    }

    private fun createUserInFirestore(
        userName: String,
        userId: String,
        userImage: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = UserItem(
                userName,
                userId,
                userImage
            )
            FirebaseObject.usersReference.add(user).await()  //adding user into firestore
        }
    }

    sealed class SignUpProgress {
        object Loading : SignUpProgress()
        object Done : SignUpProgress()
        data class ProgressText(val text: String) : SignUpProgress()
        data class Progress(val progress: Int) : SignUpProgress()
        data class Error(val e: String) : SignUpProgress()
    }

}