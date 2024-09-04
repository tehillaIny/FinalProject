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
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject.adapter.RecommendationAdapter
import com.example.finalproject.models.Recommendation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import androidx.recyclerview.widget.GridLayoutManager


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var recommendationAdapter: RecommendationAdapter
    private var recommendations = mutableListOf<Pair<String, Recommendation>>()

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
            verifyCurrentPasswordAndSave()
        }

        binding.changeProfileImageTextView.setOnClickListener {
            selectImageFromGallery()
        }

        // Setup RecyclerView
        setupRecyclerView()

        // Load user recommendations
        loadUserRecommendations()
        return binding.root
    }
    // functions for gallery display of user's posts //
    private fun setupRecyclerView() {
        recommendationAdapter = RecommendationAdapter(
            recommendations,
            { recommendationId -> /* Handle item click */ },
            { recommendation, position -> updateLikeStatus(recommendation, position) },
            auth.uid ?: "",
        )

        // Set up the GridLayoutManager with 3 columns
        binding.recommendationRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recommendationRecyclerView.adapter = recommendationAdapter
    }

    private fun loadUserRecommendations() {
        val userId = auth.uid
        if (userId != null) {
            database.reference.child("recommendations").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        recommendations.clear()
                        for (recommendationSnapshot in snapshot.children) {
                            val recommendation = recommendationSnapshot.getValue(Recommendation::class.java)
                            if (recommendation != null) {
                                recommendations.add(recommendationSnapshot.key!! to recommendation)
                            }
                        }
                        recommendationAdapter.updateRecommendations(recommendations)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle possible errors
                        Log.e("ProfileFragment", "Failed to load recommendations: ${error.message}")
                    }
                })
        }
    }
    private fun updateLikeStatus(recommendation: Recommendation, position: Int) {
        val userId = auth.uid ?: return
        val updatedLikeStatus = !recommendation.likes[userId]!!

        recommendation.likes[userId] = updatedLikeStatus
        recommendation.likeCount += if (updatedLikeStatus) 1 else -1

        database.reference.child("recommendations")
            .child(recommendationAdapter.getRecommendationId(position))
            .setValue(recommendation)

        recommendationAdapter.updateLikeStatus(position, recommendation)
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
        binding.recommendationRecyclerView.visibility = if (isEditing) View.GONE else View.VISIBLE
    }

    // Verify current password and then save changes
    private fun verifyCurrentPasswordAndSave() {
        val user = auth.currentUser
        if (user != null) {
            val currentPassword = binding.currentPasswordEditText.text.toString()
            val email = user.email!!

            if (currentPassword.isEmpty()) {
                binding.currentPasswordEditText.error = "Current password is required"
                return
            }

            // Re-authenticate the user with the current password
            auth.signInWithEmailAndPassword(email, currentPassword)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        // Current password is correct, proceed with saving profile changes
                        saveUserProfile()
                    } else {
                        // Current password is incorrect, show error message
                        Log.e("ProfileFragment", "Current password is incorrect: ${authTask.exception?.message}")
                        binding.currentPasswordEditText.error = "Current password is incorrect"
                    }
                }
        }
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

            // Check if passwords match
            if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
                Log.e("ProfileFragment", "Passwords do not match.")
                binding.confirmPasswordEditText.error = "Passwords do not match"
                return
            }
            // Check if passwords are at least 6 characters long
            if (newPassword.isNotEmpty() && newPassword.length < 6) {
                Log.e("ProfileFragment", "Password is too short.")
                binding.passwordEditText.error = "Password must be at least 6 characters long"
                return
            }

            // Update username in Firebase Realtime Database
            userRef.child("username").setValue(newUsername)

            // Update password in Firebase Authentication
            if (newPassword.isNotEmpty()) {
                user.updatePassword(newPassword).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("ProfileFragment", "User password updated successfully.")
                    } else {
                        Log.e("ProfileFragment", "Failed to update password: ${task.exception?.message}")
                        binding.passwordEditText.error = "Failed to update password: ${task.exception?.message}"
                    }
                }
            }

            // Optionally show a message to the user
            Log.d("ProfileFragment", "User profile updated successfully.")

            // Switch back to view mode
            toggleEditMode(false)

            // Update the profile view with new data
            binding.usernameTextView.text = newUsername
        } else {
            Log.e("ProfileFragment", "User is null.")
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

                    // Set the navigation result to notify the MainActivityApp
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("profileImageUpdated", true)
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




