package com.example.memsto.dataClasses

import com.google.firebase.Timestamp

data class MessageItem(
    var messageId:String="",
    var message : String="",
    var senderId : String="",
    var dateTime : String="",
    var timeStamp: Timestamp? = null
)
