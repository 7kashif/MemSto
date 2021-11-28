package com.example.memsto.dataClasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MemoryItem  (
    var fbDocId: String="",
    var memory:String="",
    var date: String = "",
    var imageUri: String = "",
):Parcelable