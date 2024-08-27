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
import com.google.firebase.storage.FirebaseStorage
import androidx.navigation.fragment.findNavController
import android.util.Log
import com.bumptech.glide.Glide


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ProfileFragment", "onCreateView: check ")
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // Fetch user data from Firebase and populate the UI
        populateUserProfile()
        // back to mainFeed
        /*binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_mainFeedFragment)
        }*/

        return binding.root
    }

    private fun populateUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = database.reference.child("users").child(userId)

            // Fetch user data from Firebase Realtime Database
            userRef.get().addOnSuccessListener { dataSnapshot ->
                val displayName = dataSnapshot.child("username").getValue(String::class.java)
                val email = user.email

                Log.d("ProfileFragment", "User display name: $displayName")
                Log.d("ProfileFragment", "User email: $email")

                binding.usernameTextView.text = displayName ?: "No Display Name"
                binding.emailTextView.text = email ?: "No Email"

                // Fetch and load the profile image from Firebase Storage
                val userId = user.uid
                val storageRef = storage.reference.child("profile_images/$userId.jpg")

                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.profile2) // Placeholder image while loading
                        .into(binding.profileImageView)
                }.addOnFailureListener {
                    // Handle the error if the image retrieval fails
                    Log.e("ProfileFragment", "Error loading profile image: ${it.message}")
                    binding.profileImageView.setImageResource(R.drawable.profile2) // Default image
                }
            }

        } else {
            binding.usernameTextView.text = "User not logged in"
            binding.emailTextView.text = "No Email"
            binding.profileImageView.setImageResource(R.drawable.profile2) // Default image
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }}


