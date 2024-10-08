package com.example.finalproject.models

data class Recommendation(
    val userId: String = "",
    val restaurantName: String = "",
    val address: String = "",
    val type: String = "",
    val description: String = "",
    val mainImageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    var likeCount: Int = 0,
    var isLikedByUser: Boolean = false,
    var likes: MutableMap<String, Boolean> = mutableMapOf()
)
