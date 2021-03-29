package com.example.whatsonnews.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsonnews.activities.MainActivity
import com.example.whatsonnews.dataclasses.UserProfile
import com.example.whatsonnews.news.Article
import com.example.whatsonnews.news.NewsResponse
import com.example.whatsonnews.repos.NewsRepository
import com.example.whatsonnews.resource.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import okhttp3.internal.wait
import retrofit2.Response

class MainViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {

    val trendingNewsData: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var trendingPageNumber = 1
    var trendingNewsResponse: NewsResponse? = null
    var trendingRecyclerViewPosition: Int? = null

    val searchNewsData: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchPageNumber = 1
    var searchNewsResponse: NewsResponse? = null
    var searchRecyclerViewPosition: Int? = null

    val allSavedUrls: MutableLiveData<List<String>> = MutableLiveData()

    val sectionNewsData: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var sectionPageNumber = 1
    var sectionNewsResponse: NewsResponse? = null
    var sectionRecyclerViewPosition: Int? = null

    var previousCountryCode: String? = null
    var userProfile: MutableLiveData<UserProfile?> = MutableLiveData()

    var presentSearchQueryLiveData: MutableLiveData<String> = MutableLiveData()
    var previousSearchQuery: String? = null




    init {
        val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val user = mFirebaseAuth.currentUser
        if(user!=null){
            val mFirebaseDbUserRef: DatabaseReference = Firebase.database.reference.child("users").child(user.uid)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profile = snapshot.getValue(UserProfile::class.java)
                    if (profile != null) {
                        Log.e(
                            TAG,
                            "NEW NON NULL USER DATA RECEIVED AND STORED IN LIVE DATA USER PROFILE."
                        )
                        userProfile.value = profile
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Couldn't load the data. Error: $error.")
                }
            }
            mFirebaseDbUserRef.addValueEventListener(listener)
        }

        getAllSavedUrls()
        getUserInfoFromDatabaseMVM()
    }


    companion object {
        const val TAG = "MainViewModel"
    }

    fun getUserInfoFromDatabaseMVM() = viewModelScope.launch {

        /*
            var fullName: String
            var username: String
            var email: String
            var countryCode: String

            var exclusive: Boolean
            var health: Boolean
            var business:Boolean
            var entertainment:Boolean
            var sports: Boolean
            var technology: Boolean

            var dataReceived = false
*/
    }


    private fun getAllSavedUrls() = viewModelScope.launch {
        allSavedUrls.postValue(newsRepository.getAllSavedUrls())
    }

    fun getTrendingNews() = viewModelScope.launch {
        /* This method is called from trending fragment only when userProfile!=null */
        trendingNewsData.postValue(Resource.Loading())
        if (previousCountryCode == null) previousCountryCode = userProfile.value!!.countryCode
        val response = newsRepository.getTrendingNews(
            trendingPageNumber,
            userProfile.value!!.countryCode
        )
        trendingNewsData.postValue(verdictTrendingNews(response))
    }

    fun getSearchNews() = viewModelScope.launch {
        presentSearchQueryLiveData.value?.let {
            Log.e(TAG, "Search call performed!")
            searchNewsData.postValue(Resource.Loading())
            val response: Response<NewsResponse> = newsRepository.getSearchNews(it, searchPageNumber)
            searchNewsData.postValue(verdictSearchNews(response))
            previousSearchQuery = it
        }
    }

    fun getSectionNews(section: String) = viewModelScope.launch {
        sectionNewsData.postValue(Resource.Loading())
        val response = newsRepository.getSectionNews(section, sectionPageNumber)
        sectionNewsData.postValue(verdictSectionNews(response))
    }


    private fun verdictTrendingNews(response: Response<NewsResponse>)
            : Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                trendingPageNumber++

                if (trendingNewsResponse == null)
                    trendingNewsResponse = resultResponse
                else {
                    val old = trendingNewsResponse?.articles
                    val new = resultResponse.articles
                    old?.addAll(new)
                }
                return Resource.Success(trendingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.body(), response.message(), response.code().toString())

    }

    private fun verdictSearchNews(response: Response<NewsResponse>)
            : Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                searchPageNumber++

                if (searchNewsResponse == null)
                    searchNewsResponse = resultResponse
                else {
                    val old = searchNewsResponse?.articles
                    val new = resultResponse.articles
                    old?.addAll(new)
                }

                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.body(), response.message(), response.code().toString())
    }

    private fun verdictSectionNews(response: Response<NewsResponse>)
            : Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                sectionPageNumber++

                if (sectionNewsResponse == null)
                    sectionNewsResponse = resultResponse
                else {
                    val old = sectionNewsResponse?.articles
                    val new = resultResponse.articles
                    old?.addAll(new)
                }
                return Resource.Success(sectionNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.body(), response.message(), response.code().toString())
    }

    fun insertArticle(article: Article) = viewModelScope.launch {
        newsRepository.insertArticle(article)
        getAllSavedUrls()
    }

    fun getAllSavedArticles() = newsRepository.getAllSavedArticles()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
        getAllSavedUrls()
    }

    suspend fun getCountOfArticles() = newsRepository.getCountOfArticles()

    suspend fun articleIsAlreadyPresent(url: String): Int =
        newsRepository.articleIsAlreadyPresent(url)

    fun deleteAllTheArticlesFromDatabaseMVM() = viewModelScope.launch {
        deleteAllTheArticlesFromDatabase()
    }
    suspend fun deleteAllTheArticlesFromDatabase() = newsRepository.deleteAllTheArticlesFromDatabase()
}