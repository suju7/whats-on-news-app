package com.example.whatsonnews.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.whatsonnews.news.Article

@Dao
interface ArticleDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article): Long

    @Query("SELECT * FROM articles")
    fun getAllSavedArticles(): LiveData<List<Article>>

    @Query("SELECT url FROM articles")
    suspend fun getAllSavedUrls(): List<String>

    @Query("DELETE FROM articles WHERE url=:url")
    suspend fun deleteArticle(url: String)

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM articles WHERE url=:url")
    suspend fun articleIsAlreadyPresent(url: String): Int

    @Query("DELETE FROM articles")
    suspend fun deleteAllTheArticlesFromDatabase()

}