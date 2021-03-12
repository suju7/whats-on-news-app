package com.example.whatsonnews.news

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {
    @GET("v2/top-headlines")
    suspend fun getTrendingNews(
        @Query("country")
        countryCode: String,
        @Query("page")
        pageNumber: Int,
        @Query("apiKey")
        apiKey: String = ConstantsAndKeys.NewsAPI_API_KEY
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery: String,
        @Query("page")
        pageNumber: Int,
        @Query("apiKey")
        apiKey: String = ConstantsAndKeys.NewsAPI_API_KEY
    ): Response<NewsResponse>

    @GET("v2/top-headlines")
    suspend fun getSectionNews(
        @Query("category")
        section: String,
        @Query("page")
        pageNumber: Int,
        @Query("apiKey")
        apiKey: String = ConstantsAndKeys.NewsAPI_API_KEY
    ): Response<NewsResponse>

}