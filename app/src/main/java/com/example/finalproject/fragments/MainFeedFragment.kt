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

class MainFeedFragment : Fragment() {

    private var _binding: FragmentMainFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecommendationAdapter
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainFeedBinding.inflate(inflater, container, false)
        val view = binding.root

        recyclerView = view.findViewById(R.id.recyclerViewRecommendations)
        recyclerView.layoutManager = LinearLayoutManager(context)

        /*val recommendations = listOf(
            // Sample data
            Recommendation("Restaurant A", "Description A", "https://example.com/image1.jpg"),
            Recommendation("Restaurant B", "Description B", "https://example.com/image2.jpg")
        )
        adapter = RecommendationAdapter(recommendations)
        recyclerView.adapter = adapter*/

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("recommendations")

        // Fetch data from Firebase
        fetchRecommendations()

        /*binding.buttonGoToaddRecommendation.setOnClickListener {
            findNavController().navigate(R.id.action_mainFeedFragment_to_uploadRecommendationFragment)
        }
        binding.buttonGoToProfile.setOnClickListener {
            Log.d("MainFeedFragment", "Button clicked")
            findNavController().navigate(R.id.action_mainFeedFragment_to_profileFragment)
        }*/
        return view
    }
    private fun fetchRecommendations() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recommendationsList = mutableListOf<Recommendation>()
                for (recommendationSnapshot in snapshot.children) {
                    val recommendation = recommendationSnapshot.getValue(Recommendation::class.java)
                    recommendation?.let { recommendationsList.add(it) }
                }
                // Update RecyclerView with the fetched data
                adapter = RecommendationAdapter(recommendationsList)
                recyclerView.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
                Log.e("MainFeedFragment", "Failed to load recommendations.", error.toException())
            }

        })

    }
    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }
}