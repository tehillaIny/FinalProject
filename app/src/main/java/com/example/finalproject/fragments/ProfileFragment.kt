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
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity


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

        binding.editProfileButton.setOnClickListener {
            toggleEditMode(true)
        }
        // save changes
        binding.saveButton.setOnClickListener {
            saveUserProfile()
        }

        binding.changeProfileImageTextView.setOnClickListener {
            selectImageFromGallery()
        }
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

                binding.usernameTextView.text = displayName ?: "No Display Name"
                binding.usernameEditText.setText(displayName ?: "No Display Name")

                // Display email
                val email = user.email
                binding.emailTextView.text = email ?: "No Email"

                // Fetch and display the user's profile image from Firebase Storage
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
            binding.profileImageView.setImageResource(R.drawable.profile2) // default image
        }
    }
    // Edit profile //
    private fun toggleEditMode(isEditing: Boolean) {
        binding.usernameTextView.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.emailTextView.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.editProfileButton.visibility = if (isEditing) View.GONE else View.VISIBLE

        binding.editProfileLayout.visibility = if (isEditing) View.VISIBLE else View.GONE
    }
    // save changes
    private fun saveUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = database.reference.child("users").child(userId)
            val newUsername = binding.usernameEditText.text.toString()
            val newPassword = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            val currentPassword = binding.currentPasswordEditText.text.toString()

            // Check if passwords match
            if (newPassword != confirmPassword) {
                // Show error message to user if passwords do not match
                Log.e("ProfileFragment", "Passwords do not match.")
                binding.confirmPasswordEditText.error = "Passwords do not match"
                return
            }
            // Update username in Firebase Realtime Database
            userRef.child("username").setValue(newUsername)

            // Update password in Firebase Authentication
            if (newPassword.isNotEmpty()) {
                // Re-authenticate the user before updating the password
                auth.signInWithEmailAndPassword(user.email!!, currentPassword).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        user.updatePassword(newPassword).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("ProfileFragment", "User password updated successfully.")
                            } else {
                                Log.e("ProfileFragment", "Failed to update password: ${task.exception?.message}")
                                binding.passwordEditText.error = "Failed to update password: ${task.exception?.message}"
                            }
                        }
                    } else {
                        Log.e("ProfileFragment", "Re-authentication failed: ${authTask.exception?.message}")
                        binding.currentPasswordEditText.error = "Re-authentication failed: ${authTask.exception?.message}"
                    }
                }
            }
            // Optionally show a message to the user
            Log.d("ProfileFragment", "User profile updated successfully.")

            // Switch back to view mode
            toggleEditMode(false)

            // Update the profile view with new data
            binding.usernameTextView.text = newUsername
        }
    }
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                uploadProfileImageToFirebase(imageUri)
            }
        }
    }
    private fun uploadProfileImageToFirebase(imageUri: Uri) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val storageRef = storage.reference.child("profile_images/$userId.jpg")

            storageRef.putFile(imageUri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.profile2)
                        .into(binding.profileImageView)
                    Log.d("ProfileFragment", "Profile image uploaded successfully.")
                }
            }.addOnFailureListener {
                Log.e("ProfileFragment", "Failed to upload profile image: ${it.message}")
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        private const val REQUEST_CODE_IMAGE_PICK = 1001
    }
}




