package com.example.memsto.dataClasses

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class MessageItem(
    var messageId:String="",
    var message : String="",
    var senderId : String="",
    var dateTime : String="",
    var timeStamp: Timestamp? = null
)
