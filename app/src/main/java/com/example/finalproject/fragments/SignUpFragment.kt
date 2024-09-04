package com.example.finalproject.fragments

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.finalproject.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import android.net.Uri
import com.example.finalproject.MainActivityApp
import androidx.navigation.fragment.findNavController
import com.example.finalproject.R
import android.util.Log
import com.google.firebase.storage.FirebaseStorage

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var profileImage: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.uploadProfilePhotoButton.setOnClickListener {
            pickImageFromGallery()
        }

        binding.buttonSignUp.setOnClickListener {
            val username = binding.editTextUserName.text.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                signUpUser(username, email, password)
            } else {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
        // Log In button click listener
        binding.buttonGoToLogIn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_logInFragment)
        }
        return binding.root
    }
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            profileImage = data?.data
            binding.uploadProfilePhotoButton.text = "Image Selected"
        }
    }

    private fun signUpUser(username: String, email: String, password: String) {
        // Check if the password is less than 6 characters
        if (password.length < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }
        // Show the progress bar
        binding.signUpProgressBar.visibility = View.VISIBLE
        binding.uploadProgressTextView.visibility = View.VISIBLE
        binding.buttonSignUp.isEnabled = false // Disable the sign-up button

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    // If profileImage is selected, upload it to Firebase Storage
                    if (profileImage != null && userId != null) {
                        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
                        val uploadTask = storageRef.putFile(profileImage!!)

                        uploadTask.addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                val profileImageUrl = uri.toString()
                                saveUserToDatabase(userId, username, profileImageUrl)
                            }
                        }.addOnFailureListener {
                            // Hide the progress bar if there's an error
                            binding.signUpProgressBar.visibility = View.GONE
                            binding.uploadProgressTextView.visibility = View.GONE
                            binding.buttonSignUp.isEnabled = true // Re-enable the sign-up button
                            Toast.makeText(requireContext(), "Profile image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // If no profile image selected, just save the user details without an image
                        saveUserToDatabase(userId!!, username, null)
                    }

                } else {
                    // Hide the progress bar if there's an error
                    binding.signUpProgressBar.visibility = View.GONE
                    binding.buttonSignUp.isEnabled = true // Re-enable the sign-up button
                    Toast.makeText(requireContext(), "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(userId: String, username: String, profileImageUrl: String?) {
        val userRef = database.reference.child("users").child(userId)
        userRef.child("username").setValue(username)
        profileImageUrl?.let {
            userRef.child("profileImageUrl").setValue(it)
        }

        // Hide the progress bar after the data is saved
        binding.signUpProgressBar.visibility = View.GONE
        binding.buttonSignUp.isEnabled = true // Re-enable the sign-up button

        Toast.makeText(requireContext(), "Sign-up successful!", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), MainActivityApp::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}