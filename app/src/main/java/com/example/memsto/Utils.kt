package com.example.memsto

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.memsto.dataClasses.MemoryItem
import com.example.memsto.databinding.LoaderDialogBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    const val MEMORY_ITEMS = "memoryItems"
    val arrayOfMonths= arrayOf(
        "Jan",
        "Fab",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    )
    fun addBackPressedCallback(fragment: Fragment) {
        fragment.requireActivity().onBackPressedDispatcher.addCallback(fragment.viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    fragment.activity?.finish()
                }
            })
    }

    fun getDateAndTime(): String {
        val date = Date()
        val formatter = SimpleDateFormat("EEE, d MMM yyyy h:mm a", Locale.getDefault()) //Fri, 19 Nov 2021 11:13 AM
        return formatter.format(date)
    }

    fun getDate(): String {
        val formatter = SimpleDateFormat.getDateInstance()
        return formatter.format(Date())
    }

    @DelicateCoroutinesApi
    fun shareMemory(context: Context, memory: MemoryItem, inflater: LayoutInflater) {
        val dialog= showProgressDialog(context, inflater)
        dialog.show()
        var contentUri: Uri? = null
        val cachePath = File(context.cacheDir,"images")
        cachePath.mkdir()
        val timeMillis = System.currentTimeMillis()
        GlobalScope.launch {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(memory.imageUri)
                .allowHardware(false)
                .build()
            when(loader.execute(request)) {
                is SuccessResult -> {
                    val result = (loader.execute(request) as SuccessResult).drawable
                    val bitmap = (result as BitmapDrawable).bitmap
                    val stream =
                        FileOutputStream("$cachePath/${timeMillis}.jpeg")
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val newFile = File(cachePath, "$timeMillis.jpeg")
                    contentUri= FileProvider.getUriForFile(
                        context,
                        "${BuildConfig.APPLICATION_ID}.provider",
                        newFile
                    )
                }
                else -> Unit
            }
        }.invokeOnCompletion {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Intent.EXTRA_STREAM,contentUri)
            intent.putExtra(Intent.EXTRA_TEXT,memory.memory+" on "+memory.date)
            dialog.dismiss()
            try {
                context.startActivity(
                    Intent.createChooser(
                        intent,
                        "Choose an app."
                    )
                )
            } catch (e: Exception) {
                Log.e("shareMemoryExc", e.toString() + " " + e.cause + " " + e.message + " " + e.localizedMessage)
            }
        }

    }

    fun showProgressDialog(
        context: Context,
        inflater: LayoutInflater
    ):Dialog {
        val binding = LoaderDialogBinding.inflate(inflater)
        val dialog = Dialog(context)
        dialog.apply {
            setContentView(binding.root)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawableResource(R.color.transparent)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }

        return dialog
    }
}