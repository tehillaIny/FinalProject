package com.example.finalproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.finalproject.databinding.FragmentMainFeedBinding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.adapter.RecommendationAdapter
import com.example.finalproject.models.Recommendation
import com.google.firebase.database.*
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class MainFeedFragment : Fragment() {

    private var _binding: FragmentMainFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecommendationAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var scrollPosition: Int = 0
    private var scrollOffset: Int = 0

    private var lastClickTime = 0L
    private val clickInterval = 1000L

    private var lastLikeClickTime = 0L
    private val likeClickInterval = 1000L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainFeedBinding.inflate(inflater, container, false)
        val view = binding.root

        recyclerView = view.findViewById(R.id.recyclerViewRecommendations)
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                scrollPosition = layoutManager.findFirstVisibleItemPosition()
                val view = recyclerView.getChildAt(0)
                scrollOffset = view?.top ?: 0
            }
        })

        database = FirebaseDatabase.getInstance().getReference("recommendations")
        auth = FirebaseAuth.getInstance()

        adapter = RecommendationAdapter(
            mutableListOf(),
            onItemClick = { recommendationId ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > clickInterval) {
                    lastClickTime = currentTime
                    val action =
                        MainFeedFragmentDirections.actionMainFeedFragmentToPostPageFragment(
                            recommendationId
                        )
                    findNavController().navigate(action)
                }
            },
            onLikeClick = { recommendation, position ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastLikeClickTime > likeClickInterval) {
                    lastLikeClickTime = currentTime
                    updateLikeStatus(recommendation, position)
                }
            },
            currentUserId = auth.currentUser?.uid ?: "",
            isProfileView = false // Set to false for MainFeedFragment
        )
        recyclerView.adapter = adapter

        recyclerView.layoutManager?.scrollToPosition(scrollPosition)

        fetchRecommendations()

        return view
    }

    override fun onPause() {
        super.onPause()
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        scrollPosition = layoutManager.findFirstVisibleItemPosition()
        val view = recyclerView.getChildAt(0)
        scrollOffset = view?.top ?: 0
        Log.d("MainFeedFragment", "Saved scrollPosition: $scrollPosition, scrollOffset: $scrollOffset")
    }

    override fun onResume() {
        super.onResume()
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        layoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffset)
        Log.d("MainFeedFragment", "Restored scrollPosition: $scrollPosition, scrollOffset: $scrollOffset")
    }

    private fun fetchRecommendations() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recommendationsList = mutableListOf<Pair<String, Recommendation>>()
                for (recommendationSnapshot in snapshot.children) {
                    val recommendationId = recommendationSnapshot.key ?: continue
                    val recommendation = recommendationSnapshot.getValue(Recommendation::class.java)
                    recommendation?.let { recommendationsList.add(Pair(recommendationId, it)) }
                }
                recommendationsList.reverse()
                adapter.updateRecommendations(recommendationsList)
                recyclerView.post {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    layoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffset)
                    Log.d("MainFeedFragment", "Restored scrollPosition: $scrollPosition, scrollOffset: $scrollOffset")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainFeedFragment", "Failed to load recommendations.", error.toException())
            }
        })
    }

    private fun updateLikeStatus(recommendation: Recommendation, position: Int) {
        val recommendationId = adapter.getRecommendationId(position)
        val currentUserId = auth.currentUser?.uid ?: return
        val userLiked = recommendation.likes[currentUserId] == true
        val updates = hashMapOf<String, Any>(
            "likes/$currentUserId" to !userLiked,
            "likeCount" to if (userLiked) recommendation.likeCount - 1 else recommendation.likeCount + 1
        )

        database.child(recommendationId).updateChildren(updates)
            .addOnSuccessListener {
                // Update successful
                val updatedRecommendation = recommendation.copy(
                    likes = recommendation.likes.toMutableMap().apply {
                        if (userLiked) remove(currentUserId) else put(currentUserId, true)
                    },
                    likeCount = if (userLiked) recommendation.likeCount - 1 else recommendation.likeCount + 1
                )
                adapter.updateLikeStatus(position, updatedRecommendation)
            }
            .addOnFailureListener { e ->
                Log.e("MainFeedFragment", "Error updating like status", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}