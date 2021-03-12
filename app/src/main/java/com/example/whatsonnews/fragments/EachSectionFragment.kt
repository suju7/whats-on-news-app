package com.example.whatsonnews.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsonnews.R
import com.example.whatsonnews.activities.MainActivity
import com.example.whatsonnews.adapters.NewsAdapter
import com.example.whatsonnews.resource.Resource
import com.example.whatsonnews.viewmodels.MainViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import java.util.*

class EachSectionFragment : Fragment(){

    private val args: EachSectionFragmentArgs by navArgs()

    lateinit var mMainViewModel: MainViewModel
    lateinit var mNewsAdapter: NewsAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var progressIndicator: CircularProgressIndicator

    var isLoading = false
    var isScrolling = false
    var isLastPage = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_each_section, container, false)
        progressIndicator = view.findViewById(R.id.progressIndicator)

        mMainViewModel = (activity as MainActivity).mMainViewModel

        val toolbar: MaterialToolbar = view.findViewById(R.id.eachSectionToolbar)
        var title = args.sectionTitle

        title = title[0].toString().toUpperCase(Locale.ROOT) + title.substring(1)
        toolbar.title = title
        toolbar.setNavigationOnClickListener { (activity as AppCompatActivity).onBackPressed() }

        mNewsAdapter = NewsAdapter(mMainViewModel)
        mNewsAdapter.notifyDataSetChanged()
        recyclerView = view.findViewById(R.id.eachSectionRecyclerView)
        recyclerView.apply {
            adapter = mNewsAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        var section = title.toLowerCase()
        if(section=="exclusive")
            section = "general"

        // making them reusable  ...
        mMainViewModel.sectionPageNumber = 1
        mMainViewModel.sectionNewsResponse = null

        mMainViewModel.getSectionNews(section)
        mMainViewModel.sectionNewsData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    hideProgressIndicator()
                    it.data?.let { newsResponse ->
                        mNewsAdapter.differ.submitList(newsResponse.articles.toMutableList())

                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        Log.e(TAG, "Total Results : ${newsResponse.totalResults}, Total Items : ${newsResponse.articles.size}")
                        isLastPage = mMainViewModel.sectionPageNumber == totalPages
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
                            .setTextColor(Color.WHITE)
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

        mNewsAdapter.setOnMyArticleClickListener {
            val bundle = Bundle().apply {
                putSerializable("arg_article", it)
            }
            findNavController().navigate(
                R.id.action_eachSectionFragment_to_fullArticleFragment,
                bundle
            )
        }


        mMainViewModel.allSavedUrls.observe(viewLifecycleOwner,
            Observer<List<String>> { list ->
                //Log.e("ADA","*********** UPDATED ********** ${list?.size}")
                mNewsAdapter.setSavedUrlList(list!!)
            })


        manageLoading(section)

        return view
    }

    private fun manageLoading(section: String) {

        val myScrollListener = object : RecyclerView.OnScrollListener(){

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                mMainViewModel.sectionRecyclerViewPosition = layoutManager.findFirstVisibleItemPosition()

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
                    mMainViewModel.getSectionNews(section)
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
        const val TAG = "EachSectionFragment"
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