package com.example.finalproject.models

data class Comment(
    val userId: String? = null,
    val userName: String? = null,
    val text: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
