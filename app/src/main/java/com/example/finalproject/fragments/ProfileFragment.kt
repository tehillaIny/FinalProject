package com.example.finalproject.fragments

import com.example.finalproject.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.finalproject.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.navigation.fragment.findNavController
import android.util.Log

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ProfileFragment", "onCreateView: check ")
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Fetch user data from Firebase and populate the UI
        populateUserProfile()
        // back to mainFeed
        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_mainFeedFragment)
        }

        return binding.root
    }

    private fun populateUserProfile() {
        Log.d("ProfileFragment", "onCreateView: check profile data")
        val user = auth.currentUser
        if (user != null) {
            binding.usernameTextView.text = user.displayName
            binding.emailTextView.text = user.email
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
