package com.example.finalproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finalproject.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment



class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Load LoginFragment by default
        if (savedInstanceState == null) {
            loadFragment(LoginFragment())
        }

        // Button listeners
        binding.buttonLogin.setOnClickListener {
            loadFragment(LoginFragment())
        }
        binding.buttonSignUp.setOnClickListener {
            loadFragment(SignUpFragment())
        }
    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}