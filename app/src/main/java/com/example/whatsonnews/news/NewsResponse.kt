package com.example.whatsonnews.news

data class NewsResponse(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)