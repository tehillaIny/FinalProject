package com.example.finalproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.adapter.RecommendationAdapter
import com.example.finalproject.models.Recommendation

class MainFeedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecommendationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_feed, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewRecommendations)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val recommendations = listOf(
            // Sample data
            Recommendation("Restaurant A", "Description A", "https://example.com/image1.jpg"),
            Recommendation("Restaurant B", "Description B", "https://example.com/image2.jpg")
        )

        /*adapter = RecommendationAdapter(recommendations) { recommendation ->
            // Handle click event
        }*/

        recyclerView.adapter = adapter

        return view
    }
}