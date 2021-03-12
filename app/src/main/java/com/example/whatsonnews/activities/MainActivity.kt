package com.example.whatsonnews.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.whatsonnews.R
import com.example.whatsonnews.database.ArticleDatabase
import com.example.whatsonnews.dataclasses.UserProfile
import com.example.whatsonnews.fragments.SettingsFragment
import com.example.whatsonnews.repos.NewsRepository
import com.example.whatsonnews.viewmodels.MainViewModel
import com.example.whatsonnews.viewmodels.MainViewModelProviderFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    companion object {

        const val TAG = "MainActivity"

        lateinit var mFirebaseAuth: FirebaseAuth
        lateinit var mFirebaseDbUserRef: DatabaseReference

    }

    lateinit var mMainViewModel: MainViewModel

/*
        exclusive
        health
        business
        entertainment
        sports
        technology
*/

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.semi_transparent));

        val newsRepository = NewsRepository(ArticleDatabase.getInstance(applicationContext))
        val newsViewModelProviderFactory = MainViewModelProviderFactory(newsRepository)
        mMainViewModel = ViewModelProvider(this, newsViewModelProviderFactory).get(MainViewModel::class.java)
        mMainViewModel.getUserInfoFromDatabaseMVM()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.setupWithNavController(newsNavHostFragment.findNavController())

/*
        val navController = Navigation.findNavController(this, R.id.newsNavHostFragment)
        val navGraph = navController.navInflater.inflate(R.navigation.custom_nav_graph)

        val isFirstTimeUser = intent.getBooleanExtra("isFirstTimeUser", false)
        Log.e(TAG, "isFirstTimeUser : $isFirstTimeUser")

        if(isFirstTimeUser){
            navGraph.startDestination = R.id.settingsFragment
        } else {
            navGraph.startDestination = R.id.trendingFragment
        }
        navController.graph = navGraph
*/

    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth = FirebaseAuth.getInstance()
        val user = mFirebaseAuth.currentUser
        if (user == null) { // user is signed out
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
            finish()
        } else {
            mFirebaseDbUserRef = Firebase.database.reference.child("users").child(mFirebaseAuth.currentUser!!.uid)
        }

    }

}