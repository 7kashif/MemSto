package com.example.memsto

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun addBackPressedCallback(fragment: Fragment) {
        fragment.requireActivity().onBackPressedDispatcher.addCallback(fragment.viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    fragment.activity?.finish()
                }
            })
    }

    fun getTime(): String {
        val formatter = SimpleDateFormat.getTimeInstance()
        //time = time.removeRange(time.lastIndexOf(':'), time.lastIndexOf(':') + 3) //1:29 AM
        return formatter.format(Date()) //1:29:00 AM
    }

    fun getDate(): String {
        val formatter = SimpleDateFormat.getDateInstance()
        return formatter.format(Date())
    }

}