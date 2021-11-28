package com.example.memsto.fragments

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.memsto.R
import com.example.memsto.Utils
import com.example.memsto.databinding.AddImageFragmentBinding
import com.example.memsto.viewModels.SharedViewModel
import java.util.*

class AddImageFragment : Fragment() {
    private lateinit var binding: AddImageFragmentBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private var memory: String? = null
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddImageFragmentBinding.inflate(inflater)
        binding.etDate.showSoftInputOnFocus = false
        binding.etDate.setText(getString(R.string.set_date, Utils.getDate()))
        binding.etDate.showSoftInputOnFocus = false
        addClickListeners()
        addObservers()

        return binding.root
    }

    private fun checkData() {
        binding.apply {
            memory = if (etMemory.text?.isNotEmpty() == true)
                etMemory.text.toString().trim()
            else
                null

            if (imageUri != null && memory != null)
                viewModel.uploadMemory(memory!!, imageUri!!, binding.etDate.text.toString())
            else if (imageUri == null)
                Toast.makeText(activity, "Please select an image.", Toast.LENGTH_LONG).show()
            else
                Toast.makeText(activity, "Please enter a memory.", Toast.LENGTH_LONG).show()
        }
    }

    private fun addClickListeners() {
        binding.apply {
            btnUploadImage.setOnClickListener {
                checkData()
            }

            binding.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnAddNewImage.setOnClickListener {
                imageContract.launch("image/*")
            }

            ibEditDate.setOnClickListener {
                showDatePickerDialog()
            }
        }
    }

    private fun addObservers() {
        viewModel.uploadingProgress.observe(viewLifecycleOwner, {
            binding.pbImageUploading.progress = it
        })

        viewModel.showUploadingProgress.observe(viewLifecycleOwner, {
            binding.apply {
                when (it) {
                    is SharedViewModel.Loading.InProgress -> {
                        pbCustom.isVisible = true
                        tvUploading.text = getString(R.string.uploading)
                        tvUploading.isVisible = true
                        pbImageUploading.isVisible = true
                        btnUploadImage.isEnabled = false
                    }
                    is SharedViewModel.Loading.ErrorOccurred -> {
                        pbCustom.isVisible = false
                        tvUploading.text = getString(R.string.error_occurred)
                        btnUploadImage.isClickable = false
                        Toast.makeText(activity, it.error, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        pbCustom.visibility = View.INVISIBLE
                        tvUploading.text = getString(R.string.uploaded)
                        btnUploadImage.isEnabled = true
                    }
                }
            }
        })
    }

    private val imageContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                binding.ivNewImage.load(it) {
                    crossfade(true)
                    crossfade(300)
                    transformations(RoundedCornersTransformation(12.0F))
                }
            }
        }


    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            requireActivity(),
            { _, year, month, dayOfMonth ->
                val mon = when(month + 1) {
                    1 -> "Jan"
                    2 -> "Fab"
                    3 -> "Mar"
                    4 -> "Apr"
                    5 -> "May"
                    6 -> "Jun"
                    7 -> "Jul"
                    8 -> "Aug"
                    9 -> "Sep"
                    10 -> "Oct"
                    11-> "Nov"
                    else -> "Dec"
                }
                val date = "$mon $dayOfMonth, $year"
                binding.etDate.setText(getString(R.string.set_date, date))
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()
    }

}