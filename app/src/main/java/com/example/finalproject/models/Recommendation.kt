package com.example.finalproject.models

data class Recommendation(
//check if we should not defined empty string to the rquired fields
    val userId: String = "",
    val restaurantName: String = "",
    val address: String = "",
    val type: String = "",
    val description: String = "",
    val mainImageUrl: String = "",
    val imageUrls: List<String> = emptyList()
)
