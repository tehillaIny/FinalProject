package com.example.finalproject.fragments

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
import com.example.finalproject.MainActivityApp
import androidx.navigation.fragment.findNavController
import com.example.finalproject.R

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.buttonGoToLogIn.setOnClickListener {
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

    private fun signUpUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    // Save the username to the Realtime Database
                    userId?.let {
                        val userRef = database.reference.child("users").child(it)
                        userRef.child("username").setValue(username)
                    }
                    Toast.makeText(requireContext(), "Sign-up successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), MainActivityApp::class.java)
                    startActivity(intent)
                    requireActivity().finish()

                } else {
                    Toast.makeText(requireContext(), "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun signUp(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    // User is signed up successfully
                } else {
                    // Handle sign up failure
                }
            }
    }

}