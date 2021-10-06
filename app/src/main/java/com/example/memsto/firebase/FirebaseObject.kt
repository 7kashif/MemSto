package com.example.memsto.firebase

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

object FirebaseObject {
    val firebaseAuth = Firebase.auth
    val storageRef = Firebase.storage.reference
    val usersReference = Firebase.firestore.collection("users")
}