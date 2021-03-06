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
            binding.pbUploading.progress = it
        })

        viewModel.showUploadingProgress.observe(viewLifecycleOwner, {
            binding.apply {
                when (it) {
                    is SharedViewModel.Loading.InProgress -> {
                        fadeView.isVisible = true
                        btnUploadImage.isEnabled = false
                    }
                    is SharedViewModel.Loading.ErrorOccurred -> {
                        fadeView.isVisible = false
                        Toast.makeText(activity, it.error, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        fadeView.isVisible = false
                        btnUploadImage.isEnabled = true
                        Toast.makeText(activity, "Memory uploaded successfully", Toast.LENGTH_LONG).show()
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
                val date = "${Utils.arrayOfMonths[month]} $dayOfMonth, $year"
                binding.etDate.setText(getString(R.string.set_date, date))
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()
    }

}