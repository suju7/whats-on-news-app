package com.example.whatsonnews.repos

import com.example.whatsonnews.database.ArticleDatabase
import com.example.whatsonnews.news.Article
import com.example.whatsonnews.news.RetrofitInstance

class NewsRepository(private val db: ArticleDatabase) {

    suspend fun getTrendingNews(pageNumber: Int, countryCode: String = "in")
    = RetrofitInstance.api.getTrendingNews(pageNumber = pageNumber, countryCode = countryCode)

    suspend fun getSearchNews(query: String, pageNumber: Int)
    = RetrofitInstance.api.searchForNews(searchQuery = query, pageNumber = pageNumber)

    suspend fun getSectionNews(section: String, pageNumber: Int)
    = RetrofitInstance.api.getSectionNews(section,pageNumber)



    // now to save our liked news in our local database

    suspend fun insertArticle(article: Article) = db.getArticleDAO().insertArticle(article)

    suspend fun getAllSavedUrls() = db.getArticleDAO().getAllSavedUrls()

    fun getAllSavedArticles() = db.getArticleDAO().getAllSavedArticles()

    suspend fun deleteAllTheArticlesFromDatabase() = db.getArticleDAO().deleteAllTheArticlesFromDatabase()

    suspend fun deleteArticle(article: Article) = db.getArticleDAO().deleteArticle(article.url!!)

    suspend fun getCountOfArticles() = db.getArticleDAO().count()

    suspend fun articleIsAlreadyPresent(url: String) : Int = db.getArticleDAO().articleIsAlreadyPresent(url)

}