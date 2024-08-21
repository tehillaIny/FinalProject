package com.example.finalproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment
import com.example.finalproject.fragments.LoginFragment
import com.example.finalproject.fragments.SignUpFragment
import android.content.Intent



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
/*
add this after successful sign-up or log in
    val intent = Intent(this, MainActivityApp::class.java)
    startActivity(intent)
    finish()  // Optional: Call finish() if you want to close the login activity
 */
}