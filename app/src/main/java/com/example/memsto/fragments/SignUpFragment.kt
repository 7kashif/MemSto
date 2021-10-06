package com.example.memsto.fragments

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
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
import coil.transform.CircleCropTransformation
import com.example.memsto.databinding.SignupFragmentBinding
import com.example.memsto.viewModels.SignUpViewModel

class SignUpFragment : Fragment() {
    private lateinit var binding: SignupFragmentBinding
    private val viewModel: SignUpViewModel by activityViewModels()
    private var email: String? = null
    private var password: String? = null
    private var name: String? = null
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SignupFragmentBinding.inflate(inflater)
        addVmObservers()
        addConfirmPasswordObserver()
        addClickListeners()
        addEmailListener()

        return binding.root
    }

    private fun addClickListeners() {
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.addImage.setOnClickListener {
            getImage.launch("image/*")
        }
        binding.btnSignup.setOnClickListener {
            checkData()
        }
    }

    private fun addVmObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
        })
        viewModel.showProgressBar.observe(viewLifecycleOwner, {
            binding.progressCardView.isVisible = it
            binding.cvFaded.isVisible = it
        })
        viewModel.goToHomeFragment.observe(viewLifecycleOwner, {
            if (it)
                this.findNavController()
                    .navigate(SignUpFragmentDirections.actionSignUpFragmentToHomeFragment())
        })
        viewModel.progressText.observe(viewLifecycleOwner,{
            binding.tvProgress.text = it
        })
        viewModel.picUploadingProgress.observe(viewLifecycleOwner,{
            binding.pbPictureUploading.progress = it
        })
        viewModel.showDoneIcon.observe(viewLifecycleOwner,{
            binding.ivSignupDone.isVisible = it
            binding.progressBar.visibility = View.INVISIBLE
        })
    }
    private fun checkData() {
        if (binding.etPassword.text?.isNotEmpty() == true)
            password = binding.etPassword.text.toString().trim()
        if (binding.etName.text?.isNotEmpty() == true)
            name = binding.etName.text.toString().trim()

        if (email != null && password != null && name != null && imageUri != null) {
            viewModel.signUp(name!!, imageUri!!, email!!, password!!)
        } else if (name == null) {
            binding.nameinput.error = "Please enter name."
        } else if (email == null) {
            binding.emailInput.error = "Please enter email"
        } else if (password == null)
            binding.passwordInput.error = "Please enter password"
        else
            Toast.makeText(activity, "Please select an image...!", Toast.LENGTH_SHORT).show()
    }

    private fun addEmailListener() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun afterTextChanged(s: Editable?) = Unit

            override fun onTextChanged(string: CharSequence?, start: Int, before: Int, count: Int) {
                string?.let {
                    if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                        binding.emailInput.isErrorEnabled = false
                        email = it.toString().trim()
                    } else {
                        email = null
                        binding.emailInput.error = "Invalid Email"
                    }
                }
            }
        })
    }

    private fun addConfirmPasswordObserver() {
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(string: CharSequence?, start: Int, before: Int, count: Int) {
                password = binding.etPassword.text.toString().trim()
                if(string.toString().trim() == password) {
                    binding.confirmPasswordInput.isErrorEnabled = false
                    binding.btnSignup.isEnabled = true
                } else {
                    binding.btnSignup.isEnabled = false
                    binding.confirmPasswordInput.error = "Password mismatch"
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let {uri->
            imageUri = uri
            binding.ivProfile.load(it) {
                transformations(CircleCropTransformation())
            }
        }
    }

}