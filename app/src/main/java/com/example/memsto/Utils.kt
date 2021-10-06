package com.example.memsto

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

object CallBack{
    fun addBackPressedCallback(fragment: Fragment) {
        fragment.requireActivity().onBackPressedDispatcher.addCallback(fragment.viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                fragment.activity?.finish()
            }
        })
    }
}