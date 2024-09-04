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
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.storage.FirebaseStorage
    import com.google.firebase.database.ValueEventListener
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import android.util.Log
    import android.net.Uri

    class RecommendationAdapter(
        private var recommendations: MutableList<Pair<String, Recommendation>>,
        private val onItemClick: (String) -> Unit,
        private val onLikeClick: (Recommendation, Int) -> Unit,
        private val currentUserId: String,
        private val isProfileView: Boolean
    ) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {
        companion object {
            private const val VIEW_TYPE_PROFILE = 1
            private const val VIEW_TYPE_MAIN_FEED = 2
        }
        private val usersDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        private val storage: FirebaseStorage = FirebaseStorage.getInstance()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
            val layout = if (viewType == VIEW_TYPE_PROFILE) {
                R.layout.item_recommendation_image_only // Layout for profile view
            } else {
                R.layout.item_recommendation // Layout for main feed view
            }
            val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            return RecommendationViewHolder(view, isProfileView)
        }

        override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
            val (recommendationId, recommendation) = recommendations[position]
            holder.bind(recommendationId, recommendation, onItemClick, onLikeClick, currentUserId, position, usersDatabase, storage)
        }

        override fun getItemCount() = recommendations.size

        override fun getItemViewType(position: Int): Int {
            return if (isProfileView) VIEW_TYPE_PROFILE else VIEW_TYPE_MAIN_FEED
        }

        fun updateRecommendations(newRecommendations: List<Pair<String, Recommendation>>) {
            recommendations = newRecommendations.toMutableList()
            notifyDataSetChanged()
        }

        fun updateLikeStatus(position: Int, updatedRecommendation: Recommendation) {
            if (position in recommendations.indices) {
                recommendations[position] = recommendations[position].first to updatedRecommendation
                notifyItemChanged(position, "like_updated")
            }
        }

        fun getRecommendationId(position: Int): String {
            return recommendations[position].first
        }

        class RecommendationViewHolder(itemView: View, private val isProfileView: Boolean) : RecyclerView.ViewHolder(itemView) {
            private val title: TextView? = itemView.findViewById(R.id.titleTextView)
            private val image: ImageView = itemView.findViewById(R.id.imageView)
            private val likeButton: ImageButton? = if (!isProfileView) itemView.findViewById(R.id.likeButton) else null
            private val likeCount: TextView? = if (!isProfileView) itemView.findViewById(R.id.likeCountTextView) else null
            private val profileImage: ImageView? = if(!isProfileView) itemView.findViewById(R.id.profileImageView) else null
            private val username: TextView? = if(!isProfileView) itemView.findViewById(R.id.usernameTextView) else null

            fun bind(
                recommendationId: String,
                recommendation: Recommendation,
                onItemClick: (String) -> Unit,
                onLikeClick: (Recommendation, Int) -> Unit,
                currentUserId: String,
                position: Int,
                usersDatabase: DatabaseReference,
                storage: FirebaseStorage
            ) {

                title?.text = recommendation.restaurantName
                Glide.with(itemView.context).load(recommendation.mainImageUrl).into(image)

                if (!isProfileView) {
                    fetchUserDetails(usersDatabase, storage, recommendation)
                    updateLikeButton(recommendation.likes[currentUserId] == true, recommendation.likeCount)
                    itemView.setOnClickListener {
                        onItemClick(recommendationId)
                    }

                    likeButton?.setOnClickListener {
                        onLikeClick(recommendation, position)
                    }
                }
            }

            fun updateLikeButton(userHasLiked: Boolean, likeCount: Int) {
                likeButton?.setImageResource(if (userHasLiked) R.drawable.full_heart else R.drawable.empty_heart)
                likeCount?.let { this.likeCount?.text = it.toString() }
            }

            fun fetchUserDetails(
                usersDatabase: DatabaseReference,
                storage: FirebaseStorage,
                recommendation: Recommendation
            ) {
                usersDatabase.child(recommendation.userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userName =
                            snapshot.child("username").getValue(String::class.java) ?: "Unknown"
                        val userId = recommendation.userId
                        val profilePicRef = storage.reference.child("profile_images/$userId.jpg")
                        Log.d(
                            "RecommendationAdapter",
                            "Fetching image from path: profile_images/$userId.jpg"
                        )

                        username?.text = userName

                        profilePicRef.listAll().addOnSuccessListener { result ->
                            if (result.items.isEmpty()) {
                                // Handle the case where the image does not exist
                                profileImage?.setImageResource(R.drawable.profile2)
                            } else {
                                // Image exists, proceed with download URL
                                profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                                    Glide.with(itemView.context)
                                        .load(uri)
                                        .placeholder(R.drawable.profile2)
                                        .circleCrop()
                                        .into(profileImage ?: return@addOnSuccessListener)
                                }.addOnFailureListener { exception ->
                                    Log.e(
                                        "RecommendationAdapter",
                                        "Failed to load profile image",
                                        exception
                                    )
                                    profileImage?.setImageResource(R.drawable.profile2)
                                }
                            }
                        }.addOnFailureListener { exception ->
                            Log.e("RecommendationAdapter", "Failed to list images", exception)
                            profileImage?.setImageResource(R.drawable.profile2)
                        }
                    }
                        override fun onCancelled(error: DatabaseError) {
                        Log.e("RecommendationAdapter", "Failed to load user data.", error.toException())
                    }
                })
            }
        }
    }