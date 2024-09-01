package com.example.finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.finalproject.databinding.FragmentPostPageBinding
import com.example.finalproject.models.Recommendation
import com.example.finalproject.models.Comment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.bumptech.glide.Glide
import androidx.navigation.fragment.navArgs
import com.example.finalproject.adapters.CommentsAdapter
import com.google.firebase.auth.FirebaseAuth



class PostPageFragment : Fragment() {

    private var _binding: FragmentPostPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private val args: PostPageFragmentArgs by navArgs()
    private lateinit var recommendationId: String
    private lateinit var commentsRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostPageBinding.inflate(inflater, container, false)
        // Get the recommendation ID from the arguments
        val view = binding.root
        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("recommendations")
        commentsRef = FirebaseDatabase.getInstance().getReference("comments") // Adjust as needed
        recommendationId = args.recommendationId

        // Fetch and display the recommendation data
        fetchRecommendation()
        binding.buttonAddComment.setOnClickListener {
            addComment()
        }
        return view
    }

    private fun fetchRecommendation() {
        database.child(recommendationId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recommendation = snapshot.getValue(Recommendation::class.java)
                recommendation?.let {
                    // Bind the recommendation data to the UI
                    bindRecommendation(it)
                    fetchComments()
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

    private fun fetchComments() {
        commentsRef.child(recommendationId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = mutableListOf<Comment>()
                snapshot.children.forEach { commentSnapshot ->
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    comment?.let { comments.add(it) }
                }
                updateCommentsUI(comments)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
    }


    /*private fun fetchUserNamesForComments(comments: List<Comment>) {
        val userNameMap = mutableMapOf<String, String>()
        val userIds = comments.mapNotNull { it.userId }.distinct() // Ensure userId is non-null

        userIds.forEach { userId ->
            fetchUserName(userId) { userName ->
                if (userId != null) { // Ensure userId is non-null
                    userNameMap[userId] = userName ?: "Unknown"
                    if (userNameMap.size == userIds.size) {
                        updateCommentsUI(comments, userNameMap)
                    }
                }
            }
        }
    }

    private fun fetchUserName(userId: String, callback: (String) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val userName = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
            callback(userName)
        }.addOnFailureListener {
            callback("Unknown")
        }
    }*/


    private fun updateCommentsUI(comments: List<Comment>) {
        val adapter = CommentsAdapter(comments)
        binding.recyclerViewComments.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewComments.adapter = adapter
    }

    private fun addComment() {
        val commentText = binding.editTextComment.text.toString()
        if (commentText.isNotBlank()) {
            val userId = "userId" // Replace with actual user ID
            val comment = Comment(userId, commentText, System.currentTimeMillis())
            val newCommentRef = commentsRef.child(recommendationId).push()
            newCommentRef.setValue(comment)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        binding.editTextComment.text.clear()
                    }
                }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
