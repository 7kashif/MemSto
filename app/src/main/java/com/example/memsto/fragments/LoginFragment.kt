package com.example.memsto.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.memsto.databinding.SigninFragmentBinding
import com.example.memsto.firebase.FirebaseObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {
    private lateinit var binding: SigninFragmentBinding
    private var email: String? = null
    private var password: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SigninFragmentBinding.inflate(inflater)
        addClickListeners()

        FirebaseObject.firebaseAuth.currentUser?.let {
            this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
        }

        return binding.root
    }

    private fun addClickListeners() {
        binding.newAccount.setOnClickListener {
            this.findNavController()
                .navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment())
        }
        binding.btnLogin.setOnClickListener {
            checkData()
        }
    }

    private fun checkData() {
        if (binding.etPassword.text?.isNotEmpty() == true)
            password = binding.etPassword.text.toString().trim()
        if (binding.etEmail.text?.isNotEmpty() == true)
            email = binding.etEmail.text.toString().trim()

        if (email != null && password != null) {
            login(email!!,password!!)
        } else if (email == null)
            Toast.makeText(activity, "Please enter email", Toast.LENGTH_SHORT).show()
        else if (password == null)
            Toast.makeText(activity, "Please enter password", Toast.LENGTH_SHORT).show()
    }

    private fun login(email:String,password:String) {
        binding.progressBar.isVisible = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseObject.firebaseAuth.signInWithEmailAndPassword(email,password).await()
                withContext(Dispatchers.Main) {
                    binding.progressBar.isVisible = false
                    this@LoginFragment.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.isVisible = false
                    Toast.makeText(activity,e.message,Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}

