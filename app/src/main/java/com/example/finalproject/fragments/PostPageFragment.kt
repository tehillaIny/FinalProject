package com.example.finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.finalproject.databinding.FragmentPostPageBinding
import com.example.finalproject.models.Recommendation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.bumptech.glide.Glide
import androidx.navigation.fragment.navArgs

class PostPageFragment : Fragment() {

    private var _binding: FragmentPostPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private val args: PostPageFragmentArgs by navArgs()
    private lateinit var recommendationId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostPageBinding.inflate(inflater, container, false)
        // Get the recommendation ID from the arguments
        val view = binding.root
        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("recommendations")
        recommendationId = args.recommendationId

        // Fetch and display the recommendation data
        fetchRecommendation()
        return view
    }

    private fun fetchRecommendation() {
        database.child(recommendationId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recommendation = snapshot.getValue(Recommendation::class.java)
                recommendation?.let {
                    // Bind the recommendation data to the UI
                    bindRecommendation(it)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
    }
    private fun bindRecommendation(recommendation: Recommendation) {
        binding.textViewRestaurantName.text = recommendation.restaurantName
        binding.textViewAddress.text = recommendation.address

        Glide.with(binding.imageViewMain)
            .load(recommendation.mainImageUrl)
            .into(binding.imageViewMain)

        binding.textViewDescription.text = recommendation.description

        recommendation.imageUrls.forEach { imageUrl ->
            val imageView = ImageView(context)
            Glide.with(this@PostPageFragment)
                .load(imageUrl)
                .into(imageView)
            binding.galleryLayout.addView(imageView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
