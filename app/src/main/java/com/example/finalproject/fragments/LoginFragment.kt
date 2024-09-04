package com.example.finalproject.fragments
import com.example.finalproject.R
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.finalproject.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.example.finalproject.MainActivityApp
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        // Log in button click listener
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Sign up button click listener
        binding.buttonGoToSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_logInFragment_to_signUpFragment)
        }
        return binding.root
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Log-in success, update UI with the signed-in user's information
                    Toast.makeText(requireContext(), "Log-in successful!", Toast.LENGTH_SHORT).show()
                    //move to the main app activity
                    val intent = Intent(requireContext(), MainActivityApp::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    // If log-in fails, display a message to the user.
                    Toast.makeText(requireContext(), "Log-in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
