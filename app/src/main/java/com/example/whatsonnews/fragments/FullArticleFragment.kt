package com.example.whatsonnews.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.whatsonnews.activities.MainActivity
import com.example.whatsonnews.R
import com.example.whatsonnews.viewmodels.MainViewModel


class FullArticleFragment : Fragment() {

    private lateinit var mMainViewModel: MainViewModel

    private val args: FullArticleFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_full_article, container, false)
        mMainViewModel = (activity as MainActivity).mMainViewModel

        val article = args.argArticle

        val webView = view.findViewById<WebView>(R.id.fullArticleWebView)
        webView.apply {
            webViewClient = WebViewClient()
            article.url?.let { loadUrl(it) }
        }

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(
                true
            ) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack())
                        webView.goBack()
                    else {
                        isEnabled = false
                        activity?.onBackPressed()
                    }
                }
            })

        return view

    }
}