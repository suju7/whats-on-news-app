package com.example.whatsonnews.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsonnews.R
import com.example.whatsonnews.activities.MainActivity
import com.example.whatsonnews.adapters.BigCardAdapter
import com.example.whatsonnews.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class PersonalizedFragment : Fragment() {

    private lateinit var mMainViewModel: MainViewModel
    private lateinit var mRecyclerView: RecyclerView

    companion object {
        const val TAG = "PersonalizedFragment"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_personalized, container, false)
        mMainViewModel = (activity as MainActivity).mMainViewModel

        val progressIndicator = view.findViewById<ProgressBar>(R.id.progressIndicator)
        progressIndicator.visibility = View.VISIBLE
        
        mMainViewModel.userProfile.observe(viewLifecycleOwner, { userProfile ->
            Log.e(TAG, "user profile observe method called from PERSONALIZED fragment...")
            if(userProfile!=null){
                val preferenceList: MutableList<String> = mutableListOf()
                if (userProfile.exclusive)
                    preferenceList.add("Exclusive")
                if (userProfile.health)
                    preferenceList.add("Health")
                if (userProfile.business)
                    preferenceList.add("Business")
                if (userProfile.entertainment)
                    preferenceList.add("Entertainment")
                if (userProfile.sports)
                    preferenceList.add("Sports")
                if (userProfile.technology)
                    preferenceList.add("Technology")
                preferenceList.shuffle()

                if(preferenceList.size==0){
                    Snackbar.make(
                        view,
                        "Add sections from Profile tab to get started.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setBackgroundTint(requireActivity().applicationContext.getColor(R.color.snack_bar_bluish_grey))
                        .setTextColor(ResourcesCompat.getColor(resources, R.color.white, null)).show()
                }

                val mAdapter = BigCardAdapter(
                    preferenceList,
                    requireActivity().applicationContext.resources
                )
                mRecyclerView = view.findViewById(R.id.personalizedRecyclerView)
                mRecyclerView.apply {
                    adapter = mAdapter
                    layoutManager = LinearLayoutManager(activity)
                }
                mAdapter.setOnMyCardClickListener {
                    val bundle = Bundle().apply {
                        putString("sectionTitle", it)
                    }
                    findNavController().navigate(
                        R.id.action_personalizedFragment_to_eachSectionFragment,
                        bundle
                    )
                }

            }
            progressIndicator.visibility = View.INVISIBLE
        })


        return view

    }


}