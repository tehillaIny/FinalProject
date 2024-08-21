package com.example.finalproject.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalproject.R
import com.example.finalproject.models.Recommendation

class RecommendationAdapter(private val recommendations: List<Recommendation>) :
    RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    val onItemClick: (Recommendation) -> Unit = { recommendation ->
        // Handle item click here
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = recommendations[position]
        holder.bind(recommendation, onItemClick)
    }

    override fun getItemCount() = recommendations.size

    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val description: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val image: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(recommendation: Recommendation, onItemClick: (Recommendation) -> Unit) {
            title.text = recommendation.title
            description.text = recommendation.description
            Glide.with(itemView.context).load(recommendation.imageUrl).into(image)

            itemView.setOnClickListener {
                onItemClick(recommendation)
            }
        }
    }

}