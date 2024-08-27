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
import android.content.Intent


class MainActivityApp : AppCompatActivity() {

    private lateinit var binding: ActivityMainAppBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the binding layout and set it as the content view
        binding = ActivityMainAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Initialize NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_main) as? NavHostFragment
        navHostFragment?.let {
            navController = it.navController

            // Set up BottomNavigationView with NavController
            val bottomNavigationView = binding.bottomNavigation
            NavigationUI.setupWithNavController(bottomNavigationView, navController)
        }

        // Handle profile image click to show popup menu
        binding.profileImageView.setOnClickListener {
            showProfilePopupMenu(it)
        }
    }

    private fun showProfilePopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
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

        private fun logout() {
            auth.signOut()
            // Redirect to login screen or handle logout logic
        }
}
