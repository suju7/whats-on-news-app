package com.example.whatsonnews.fragments

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.renderscript.Sampler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.load.engine.Resource
import com.example.whatsonnews.R
import com.example.whatsonnews.activities.MainActivity
import com.example.whatsonnews.activities.SignUp
import com.example.whatsonnews.dataclasses.UserProfile
import com.example.whatsonnews.viewmodels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.system.measureTimeMillis

class SettingsFragment : Fragment() {

    lateinit var fabBackButton: FloatingActionButton
    lateinit var fullName: TextInputEditText
    lateinit var username: TextInputEditText
    lateinit var email: TextInputEditText
    lateinit var loggedInMethodIcon: ImageView
    lateinit var loggedInMethodText: TextView
    lateinit var progressIndicator: ProgressBar

    companion object {
        const val TAG = "SettingsFragment"
    }

    lateinit var mMainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        mMainViewModel = (activity as MainActivity).mMainViewModel

        fabBackButton = view.findViewById(R.id.fabBackButton)
        fullName = view.findViewById(R.id.fullName)
        username = view.findViewById(R.id.username)
        email = view.findViewById(R.id.email)
        progressIndicator = view.findViewById(R.id.progressIndicator)
        progressIndicator.visibility = View.GONE
        loggedInMethodText = view.findViewById(R.id.loggedInMethodText)
        loggedInMethodIcon = view.findViewById(R.id.loggedInMethodIcon)

        fabBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_profileFragment)
        }

        val mFirebaseAuth = FirebaseAuth.getInstance()

        var providerName = ""
        for(userInfo in mFirebaseAuth.currentUser?.providerData!!){
            Log.e(TAG, "info = ${userInfo.providerId}")
            providerName = when (userInfo.providerId) {
                            "google.com" -> "Google"
                            else -> "Email"
                            }
        }


        if (providerName=="Google") {
            loggedInMethodText.text = providerName
            loggedInMethodIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.googleg_standard_color_18,
                    null
                )
            )

        }
        else {
            loggedInMethodText.text = providerName
            loggedInMethodIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_email_24,
                    null
                )
            )
        }



        mMainViewModel.userProfile.observe(viewLifecycleOwner, { userProfile ->

            fullName.setText(userProfile?.fullName)
            username.setText(userProfile?.username)
            email.setText(userProfile?.email)

            fullName.clearFocus()
            username.clearFocus()

            val mFirebaseDbUserRef = FirebaseDatabase.getInstance().reference.child("users")
            username.clearFocus()

            Log.e(
                TAG,
                "user profile observe method called from SETTINGS fragment..."
            )

            if (userProfile != null) {

                var tokenFullName = true
                fullName.addTextChangedListener {
                    val str = it.toString().trim()
                    if (str.isEmpty()) {
                        fullName.error = "This field cannot be empty!"
                        fullName.requestFocus()
                        fullName.setSelection(str.length)
                        tokenFullName = false
                    } else {
                        tokenFullName = true
                    }


                }


                var tokenUsername = true
                var tokenDatabaseCheck = true

                var job: Job? = null
                var usernameTextUnTouched = true

                var query: Query? = null
                var listener: ValueEventListener? = null
                listener?.let { query?.addValueEventListener(it) }


                username.addTextChangedListener {

                        usernameTextUnTouched = false
                        job?.cancel()

                        job = MainScope().launch {
                            delay(500)

                            tokenUsername = true
                            tokenDatabaseCheck = true
                            val str = it.toString().toLowerCase(Locale.ENGLISH).trim()

                            query = mFirebaseDbUserRef.orderByChild("username").equalTo(str)

                            listener = object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (str != userProfile.username && snapshot.exists()) {
                                        username.error = "This username is already taken!"
                                        username.requestFocus()
                                        username.setSelection(username.length())
                                        tokenDatabaseCheck = false
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e(SignUp.TAG, "some error")
                                }
                            }

                            for (ch in str) {
                                if (!(ch.isLetterOrDigit() || ch == '_' || ch == '.')) {
                                    username.error = "Only letters, digits, underscore, period!"
                                    username.requestFocus()
                                    username.setSelection(username.length())
                                    tokenUsername = false
                                }
                            }

                            if (str.isEmpty()) {
                                username.error = "This field cannot be empty!"
                                username.requestFocus()
                                tokenUsername = false
                            }
                        }

                }


                saveChanges.setOnClickListener {

                    listener?.let{ query?.removeEventListener(it) }

                    fullName.clearFocus()
                    username.clearFocus()

                    val handleJob =
                        if (usernameTextUnTouched) {
                            true
                        } else {
                            job?.isCompleted == true
                        }

                    if (tokenFullName && tokenUsername && tokenDatabaseCheck && handleJob) {

                        val newFullName = fullName.text.toString().trim()
                        val newUsername = username.text.toString().trim().toLowerCase()

                        fullName.setText(newFullName)
                        username.setText(newUsername)

                        if (userProfile.fullName != newFullName) {
                            mFirebaseDbUserRef.child(mFirebaseAuth.currentUser!!.uid)
                                .child("fullName").setValue(newFullName)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Log.e(TAG, "fullName updated")
                                    } else {
                                        showCustomSnackBar(
                                            view,
                                            "Couldn't update your name. Please try again.",
                                            Snackbar.LENGTH_LONG
                                        )
                                        Log.e(TAG, "Some database error occurred")
                                    }
                                }
                        }

                        if (userProfile.fullName == newFullName && userProfile.username == newUsername) {
                            showCustomSnackBar(
                                view,
                                "No change is needed to be saved.",
                                Snackbar.LENGTH_SHORT
                            )
                        } else {

                            if(userProfile.username != newUsername){
                                mFirebaseDbUserRef.child(mFirebaseAuth.currentUser!!.uid).child("username")
                                    .setValue(newUsername)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Log.e(TAG, "username updated")
                                            username.clearFocus()
                                        } else {
                                            showCustomSnackBar(
                                                view,
                                                "Couldn't update your username. Please try again.",
                                                Snackbar.LENGTH_LONG
                                            )
                                            Log.e(TAG, "Some database error occurred")
                                        }
                                    }
                            }

                            showCustomSnackBar(
                                view,
                                "Profile info updated successfully.",
                                Snackbar.LENGTH_SHORT
                            )

                        }
                        fullName.clearFocus()
                        username.clearFocus()
                    }
                }
            }

        })



        return view
    }


    private fun showCustomSnackBar(view: View, msg: String, time: Int) {
        Snackbar.make(view, msg, time)
            .setBackgroundTint(requireActivity().applicationContext.getColor(R.color.snack_bar_bluish_grey))
            .setTextColor(ResourcesCompat.getColor(resources, R.color.white, null)).show()
    }


}