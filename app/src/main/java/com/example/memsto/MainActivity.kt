package com.example.memsto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.memsto.databinding.ActivityMainBinding
import com.example.memsto.fragments.HomeFragment
import com.example.memsto.fragments.LoginFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}