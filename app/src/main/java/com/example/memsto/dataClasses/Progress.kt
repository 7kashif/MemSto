package com.example.memsto.dataClasses

sealed class Progress{
    object Loading: Progress()
    object Done: Progress()
    data class Error(val e: String): Progress()
}
