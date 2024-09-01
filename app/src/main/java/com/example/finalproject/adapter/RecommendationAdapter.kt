package com.example.finalproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalproject.R
import com.example.finalproject.models.Recommendation

class RecommendationAdapter(
    private val recommendations: List<Pair<String, Recommendation>>, // List of Pair<recommendationId, Recommendation>
    private val onItemClick: (String) -> Unit,       // Pass recommendationId and Recommendation
    private val onLikeClick: (Recommendation) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val (recommendationId, recommendation) = recommendations[position]
        holder.bind(recommendationId, recommendation, onItemClick, onLikeClick)
    }

    override fun getItemCount() = recommendations.size

    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val description: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val image: ImageView = itemView.findViewById(R.id.imageView)
        private val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)

        fun bind(
            recommendationId: String,                             // Accept recommendationId
            recommendation: Recommendation,
            onItemClick: (String) -> Unit,        // Pass recommendationId and Recommendation
            onLikeClick: (Recommendation) -> Unit
        ) {
            title.text = recommendation.restaurantName
            description.text = recommendation.description
            Glide.with(itemView.context).load(recommendation.mainImageUrl).into(image)

            itemView.setOnClickListener {
                onItemClick(recommendationId)      // Pass recommendationId when clicked
            }

            likeButton.setOnClickListener {
                onLikeClick(recommendation)
            }
        }
    }
}
