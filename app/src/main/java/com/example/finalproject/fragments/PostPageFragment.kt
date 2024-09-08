package com.example.finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import com.example.finalproject.R
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
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
import com.example.finalproject.adapter.GalleryAdapter
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.GridLayoutManager
import android.util.Log
import com.google.firebase.storage.FirebaseStorage


class PostPageFragment : Fragment() {

    private var _binding: FragmentPostPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase

    private val args: PostPageFragmentArgs by navArgs()
    private lateinit var recommendationId: String
    private lateinit var commentsRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostPageBinding.inflate(inflater, container, false)
        //val view = binding?.root

        database = FirebaseDatabase.getInstance()
        commentsRef = FirebaseDatabase.getInstance().getReference("comments")
        recommendationId = args.recommendationId
        auth = FirebaseAuth.getInstance()

        //return view
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchRecommendation()
        binding.buttonAddComment?.setOnClickListener {
            addComment()
        }
    }

    private fun fetchRecommendation() {
        database.getReference("recommendations").child(recommendationId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recommendation = snapshot.getValue(Recommendation::class.java)
                recommendation?.let {
                    // Bind the recommendation data to the UI
                    bindRecommendation(it)
                    fetchComments()
                }

            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchRecommendation", "Failed to load recommendation data.", error.toException())
            }
        })
    }
    private fun bindRecommendation(recommendation: Recommendation) {
        if (_binding == null) {
            Log.e("PostPageFragment", "Binding is null, cannot bind recommendation")
            return
        }
        binding.textViewRestaurantName.text = recommendation.restaurantName
        binding.textViewAddress.text = recommendation.address
        Glide.with(binding.imageViewMain).load(recommendation.mainImageUrl).into(binding.imageViewMain)
        binding.textViewDescription.text = recommendation.description

        fetchUserProfile(recommendation.userId)

        val galleryAdapter = GalleryAdapter(recommendation.imageUrls, requireContext())
        val layoutManager = GridLayoutManager(context, 3)
        binding.galleryRecyclerView.layoutManager = layoutManager
        binding.galleryRecyclerView.adapter = galleryAdapter
    }

    private fun fetchUserProfile(userId: String) {
        val userDatabase = FirebaseDatabase.getInstance().getReference("users")
        val storage = FirebaseStorage.getInstance()
        val userRef = userDatabase.child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _binding?.let { binding ->
                    val userName =
                        snapshot.child("username").getValue(String::class.java) ?: "Unknown"
                    binding.textViewUserName.text = userName
                    val profilePicRef = storage.reference.child("profile_images/$userId.jpg")

                    profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                        if (isAdded) {
                            Glide.with(requireContext())
                                .load(uri)
                                .placeholder(R.drawable.profile2)
                                .circleCrop()
                                .into(binding.imageViewProfile)
                        }
                    }.addOnFailureListener {
                        binding.imageViewProfile.setImageResource(R.drawable.profile2)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostPageFragment", "Failed to load user data.", error.toException())
            }
        })
    }

    private fun fetchComments() {
        commentsRef.child(recommendationId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                val comments = mutableListOf<Comment>()
                snapshot.children.forEach { commentSnapshot ->
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    comment?.let { comments.add(it) }
                }
                updateCommentsUI(comments)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchComments", "Failed to load comments data.", error.toException())

            }
        })
    }

    private fun updateCommentsUI(comments: List<Comment>) {
        if (_binding == null) return
        val adapter = CommentsAdapter(comments)
        binding.recyclerViewComments.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewComments.adapter = adapter
    }

    private fun addComment() {
        val commentText = binding.editTextComment.text.toString()
        if (commentText.isNotBlank()) {
            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val userRef = database.reference.child("users").child(userId)
                userRef.get().addOnSuccessListener { dataSnapshot ->
                    val userName = dataSnapshot.child("username").getValue(String::class.java)

                    val comment = Comment(
                        userId,
                        userName,
                        commentText,
                        System.currentTimeMillis()
                    )
                    val newCommentRef = commentsRef.child(recommendationId).push()
                    newCommentRef.setValue(comment)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                binding.editTextComment.text.clear()
                            }
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
