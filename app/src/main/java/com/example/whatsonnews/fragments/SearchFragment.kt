package com.example.whatsonnews.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.example.whatsonnews.activities.MainActivity
import com.example.whatsonnews.adapters.NewsAdapter
import com.example.whatsonnews.R
import com.example.whatsonnews.resource.Resource
import com.example.whatsonnews.viewmodels.MainViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_search.view.*

class SearchFragment : Fragment() {

    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var mNewsAdapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: FloatingSearchView
    private lateinit var mMainViewModel: MainViewModel

    var isLoading = false
    var isScrolling = false
    var isLastPage = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        progressIndicator = view.findViewById(R.id.searchProgressIndicator)
        searchView = view.findViewById(R.id.searchView)

        mMainViewModel = (activity as MainActivity).mMainViewModel

        mNewsAdapter = NewsAdapter(mMainViewModel)

        recyclerView = view.findViewById(R.id.searchRecyclerView)

        recyclerView.apply {
            adapter = mNewsAdapter
            layoutManager = LinearLayoutManager(activity)
        }


        mMainViewModel.presentSearchQueryLiveData.observe(viewLifecycleOwner, { presentQuery ->

            if(mMainViewModel.previousSearchQuery != presentQuery) {
                mMainViewModel.searchPageNumber = 1
                mMainViewModel.searchNewsResponse = null
                isLoading = false
                isScrolling = false
                isLastPage = false

                mMainViewModel.getSearchNews()

            } else {
                Log.e(TAG, "displaying old data for same search query: $presentQuery")
                mMainViewModel.searchRecyclerViewPosition?.let {
                    recyclerView.layoutManager?.scrollToPosition(it)
                }

            }
        })

     /*   if(mMainViewModel.searchNewsResponse != null){

            if( ! mMainViewModel.presentSearchQuery.value.equals(mMainViewModel.previousSearchQuery)){ // fetch new data for our new search string
                mMainViewModel.searchNewsResponse = null
                mMainViewModel.searchPageNumber = 1
                Log.e(TAG, "Fetching NEW data with NEW search query: ${mMainViewModel.presentSearchQuery}")
                mMainViewModel.getSearchNews()
            }
            else{ // display previous data with previous searchQuery
                Log.e(TAG, "displaying old data for same search query: ${mMainViewModel.presentSearchQuery}...")
                mMainViewModel.searchRecyclerViewPosition?.let {
                    recyclerView.layoutManager?.scrollToPosition(it)
                }
            }
        }
*/

        mNewsAdapter.setOnMyArticleClickListener {
            val bundle = Bundle().apply {
                putSerializable("arg_article",it)
            }
            findNavController().navigate(R.id.action_searchFragment_to_fullArticleFragment,bundle)
        }


        searchView.setOnSearchListener(object : FloatingSearchView.OnSearchListener{
            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion?) {
                //("Not yet implemented")
            }

            override fun onSearchAction(currentQuery: String?) {
                if(!currentQuery.isNullOrEmpty()) {
                    mMainViewModel.presentSearchQueryLiveData.value = currentQuery
                }
            }
        })

        mMainViewModel.allSavedUrls.observe(viewLifecycleOwner,
            Observer<List<String>> { list ->
                //Log.e("ADA","*********** UPDATED ********** ${list?.size}")
                mNewsAdapter.setSavedUrlList(list!!)
            })


        mMainViewModel.searchNewsData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    hideProgressIndicator()
                    it.data?.let { newsResponse ->
                        mNewsAdapter.differ.submitList(newsResponse.articles.toMutableList())
                        if(newsResponse.articles.size==0){
                            showCustomSnackBar(view, "No result found for this search. Try using less words.", Snackbar.LENGTH_INDEFINITE)
                        }
                        Log.e(TAG, "Total Results : ${newsResponse.totalResults}, Current total items : ${newsResponse.articles.size}")
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2 // page size is 40 here
                        isLastPage = mMainViewModel.searchPageNumber == totalPages
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
                        showCustomSnackBar(view, errorText, Snackbar.LENGTH_INDEFINITE)
                    }
                }
                is Resource.Loading -> {
                    showProgressIndicator()
                }
            }
        }

        manageLoading()

        return view
    }

    private fun manageLoading() {

        val myScrollListener = object : RecyclerView.OnScrollListener(){

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                mMainViewModel.searchRecyclerViewPosition = layoutManager.findFirstVisibleItemPosition()

                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount

                val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
                val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
                val isNotAtBeginning = firstVisibleItemPosition >= 0
                val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE // page size is 40 here
                val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                        && isTotalMoreThanVisible && isScrolling
                //Log.e(TAG, "$shouldPaginate = !$isLoading && !$isLastPage && $isNotAtBeginning && $isTotalMoreThanVisible && $isScrolling")
                if(shouldPaginate){
                    mMainViewModel.getSearchNews()
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
        const val TAG = "SearchFragment"
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

    private fun showCustomSnackBar(view: View, msg: String, time: Int) {
        Snackbar.make(view, msg, time)
            .setBackgroundTint(requireActivity().applicationContext.getColor(R.color.snack_bar_bluish_grey))
            .setTextColor(ResourcesCompat.getColor(resources, R.color.white, null)).show()
    }


}
