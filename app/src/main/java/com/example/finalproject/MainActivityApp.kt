package com.example.finalproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.finalproject.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.example.finalproject.databinding.ActivityMainAppBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import com.bumptech.glide.Glide
import androidx.navigation.NavOptions
import androidx.navigation.navOptions


class MainActivityApp : AppCompatActivity() {

    private lateinit var binding: ActivityMainAppBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    // for asynchronous username retrieval
    interface UsernameCallback {
        fun onUsernameRetrieved(username: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the binding layout and set it as the content view
        binding = ActivityMainAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_main) as? NavHostFragment
        navHostFragment?.let {
            navController = it.navController

            // Set up BottomNavigationView with NavController
            val bottomNavigationView = binding.bottomNavigation
            NavigationUI.setupWithNavController(bottomNavigationView, navController)
        }
        // Observe navigation result for profile image update
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("profileImageUpdated")?.observe(
            this
        ) { isUpdated ->
            if (isUpdated) {
                // Reload the profile image when it has been updated
                loadProfileImage()
            }
        }
        loadProfileImage()
        // Handle profile image click to show popup menu
        binding.profileImageView.setOnClickListener {
            showProfilePopupMenu(it)
            }
    }

    private fun showProfilePopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)
        getUsername(object : UsernameCallback {
            override fun onUsernameRetrieved(username: String) {
                popupMenu.menu.findItem(R.id.action_username).title = username
            }
        })
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_username -> {
                    // Navigate to ProfileFragment
                    val options = navOptions {
                        popUpTo(R.id.nav_feed) { inclusive = true }
                    }
                    navController.navigate(R.id.nav_profile, null, options)
                    true
                }

                R.id.action_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun loadProfileImage() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val storageRef = storage.reference.child("profile_images/$userId.jpg")

            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.profile1) // Placeholder image while loading
                    .circleCrop() // Ensures the image is cropped into a circle
                    .into(binding.profileImageView)
            }.addOnFailureListener {
                // If there's an error (e.g., no profile image exists), load the default image with Glide
                Glide.with(this)
                    .load(R.drawable.profile1)
                    .circleCrop()
                    .into(binding.profileImageView)
            }
        } else {
            // If the user is not logged in or user data is unavailable, load the default image with Glide
            Glide.with(this)
                .load(R.drawable.profile1)
                .circleCrop()
                .into(binding.profileImageView)
        }
    }
    private fun getUsername(callback: UsernameCallback) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
            userRef.child("username").get().addOnSuccessListener { dataSnapshot ->
                val username = dataSnapshot.getValue(String::class.java) ?: "No Display Name"
                callback.onUsernameRetrieved(username)
            }.addOnFailureListener {
                callback.onUsernameRetrieved("Error fetching name")
            }
        } else {
            callback.onUsernameRetrieved("No Display Name")
        }
    }

    private fun logout() {
            auth.signOut()
            // Redirect to login screen or handle logout logic
        }
}
