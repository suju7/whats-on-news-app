package com.example.whatsonnews.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsonnews.activities.MainActivity
import com.example.whatsonnews.adapters.NewsAdapter
import com.example.whatsonnews.R
import com.example.whatsonnews.viewmodels.MainViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar

class SavedNewsFragment : Fragment() {

    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var mNewsAdapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mMainViewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_saved_news, container, false)

        mMainViewModel = (activity as MainActivity).mMainViewModel
        progressIndicator = view.findViewById(R.id.savedNewsProgressIndicator)
        progressIndicator.visibility = View.GONE
        recyclerView = view.findViewById(R.id.savedNewsRecyclerView)
        val toolbar : MaterialToolbar = view.findViewById(R.id.savedNewsToolbar)
        toolbar.setNavigationOnClickListener { (activity as AppCompatActivity).onBackPressed() }


        mNewsAdapter = NewsAdapter(mMainViewModel, calledFromSavedNewsFragment = true, fragmentView = view)
        recyclerView.apply {
            adapter = mNewsAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        mNewsAdapter.setOnMyArticleClickListener {
            val bundle = Bundle().apply {
                putSerializable("arg_article", it)
            }
            findNavController().navigate(
                R.id.action_savedNewsFragment_to_fullArticleFragment,
                bundle
            )
        }

        mMainViewModel.getAllSavedArticles().observe(viewLifecycleOwner) {
            mNewsAdapter.differ.submitList(it.toMutableList())
            if(it.isEmpty())
                Snackbar.make(view,"Saved articles are displayed here. No saved article to display.",
                    Snackbar.LENGTH_INDEFINITE)
                    .setBackgroundTint(requireActivity().applicationContext.getColor(R.color.snack_bar_bluish_grey))
                    .setTextColor(ResourcesCompat.getColor(resources, R.color.white, null)).show()
        }

        mMainViewModel.allSavedUrls.observe(viewLifecycleOwner,
            Observer<List<String>> { list ->
                //Log.e("ADA","*********** UPDATED ********** ${list?.size}")
                mNewsAdapter.setSavedUrlList(list!!)
            })

        return view
    }

}