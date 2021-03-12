package com.example.whatsonnews.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsonnews.activities.MainActivity
import com.example.whatsonnews.adapters.NewsAdapter
import com.example.whatsonnews.R
import com.example.whatsonnews.dataclasses.UserProfile
import com.example.whatsonnews.news.Article
import com.example.whatsonnews.resource.Resource
import com.example.whatsonnews.viewmodels.MainViewModel
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay

class TrendingFragment : Fragment() {

    lateinit var mMainViewModel: MainViewModel
    lateinit var mNewsAdapter: NewsAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var progressIndicator: CircularProgressIndicator

    var isLoading = false
    var isScrolling = false
    var isLastPage = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_trending, container, false)
        progressIndicator = view.findViewById(R.id.trendingProgressIndicator)

        mMainViewModel = (activity as MainActivity).mMainViewModel

        mNewsAdapter = NewsAdapter(mMainViewModel)

        recyclerView = view.findViewById(R.id.trendingRecyclerView)
        recyclerView.apply {
            adapter = mNewsAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        mNewsAdapter.setOnMyArticleClickListener {
            val bundle = Bundle().apply {
                putSerializable("arg_article",it)
            }
            findNavController().navigate(R.id.action_trendingFragment_to_fullArticleFragment,bundle)
        }

        mMainViewModel.userProfile.observe(viewLifecycleOwner, {
            Log.e(TAG, "user profile observe method called from trending fragment...")
            if(it!=null) {

                val previousCountryCode = mMainViewModel.previousCountryCode
                val presentCountryCode = mMainViewModel.userProfile.value!!.countryCode

                if (mMainViewModel.trendingNewsResponse != null) {

                    if (presentCountryCode != previousCountryCode) { // fetch new data for new country
                        mMainViewModel.trendingNewsResponse = null
                        mMainViewModel.trendingPageNumber = 1
                        Log.e(TAG, "Fetching NEW data with NEW countryCode...")
                        mMainViewModel.getTrendingNews()
                    } else { // display previous data with previous countryCode
                        Log.e(TAG, "displaying old data for country code $presentCountryCode...")
                        mMainViewModel.trendingRecyclerViewPosition?.let {
                            recyclerView.layoutManager?.scrollToPosition(it)
                        }
                    }
                } else {
                    // wait till countryCode is not null then call
                    mMainViewModel.getTrendingNews()
                }

            }
        })

        mMainViewModel.trendingNewsData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    hideProgressIndicator()
                    it.data?.let { newsResponse ->
                        mNewsAdapter.differ.submitList(newsResponse.articles.toMutableList())

                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        Log.e(TAG, "Total Results : ${newsResponse.totalResults}, Current total items : ${newsResponse.articles.size}")
                        isLastPage = mMainViewModel.trendingPageNumber == totalPages
                        if(isLastPage){
                            // a little bottom-spacing bug-fix
                            recyclerView.setPadding(0,0,0,0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressIndicator()
                    it.code.let { code ->
                        Log.e(TAG, "Error occurred : code is $code")
                        val errorText = when(code){
                            "429" -> "Error code: 429. Too many API calls. Please upgrade to a paid plan if you want to make more requests."
                            "426" -> "Error code: 426. You have made too many requests."
                            else -> "Some error occurred. Code: $code"
                        }
                        Snackbar.make(view, errorText, Snackbar.LENGTH_INDEFINITE)
                            .setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
                            .setBackgroundTint(ContextCompat.getColor(view.context, R.color.snack_bar_bluish_grey))
                            .setAnchorView((activity as MainActivity).findViewById(R.id.bottom_nav))
                            .show()
                    }
                }
                is Resource.Loading -> {
                    showProgressIndicator()
                }
            }
        }

        manageLoading()

        mMainViewModel.allSavedUrls.observe(viewLifecycleOwner,
            Observer<List<String>> { list ->
                //Log.e("ADA","*********** UPDATED ********** ${list?.size}")
                mNewsAdapter.setSavedUrlList(list!!)
            })

        return view
    }

    private fun manageLoading() {

        val myScrollListener = object : RecyclerView.OnScrollListener(){



            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                mMainViewModel.trendingRecyclerViewPosition = layoutManager.findFirstVisibleItemPosition()

                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount

                val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
                val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
                val isNotAtBeginning = firstVisibleItemPosition >= 0
                val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
                val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                        && isTotalMoreThanVisible && isScrolling
                if(shouldPaginate){
                    mMainViewModel.getTrendingNews()
                    isScrolling = false
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    isScrolling = true
            }
        }
        recyclerView.addOnScrollListener(myScrollListener)
    }

    companion object{
        const val TAG = "TrendingFragment"
        const val QUERY_PAGE_SIZE = 20
    }

    private fun hideProgressIndicator(){
        progressIndicator.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressIndicator(){
        progressIndicator.visibility = View.VISIBLE
        isLoading = true
    }

}