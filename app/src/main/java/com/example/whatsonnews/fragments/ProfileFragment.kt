package com.example.whatsonnews.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.whatsonnews.R
import com.example.whatsonnews.activities.LoginScreen
import com.example.whatsonnews.activities.MainActivity
import com.example.whatsonnews.viewmodels.MainViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.*


class ProfileFragment : Fragment() {

    companion object{
        const val TAG = "ProfileFragment"
        lateinit var mFirebaseAuth: FirebaseAuth
        lateinit var mFirebaseDbUserRef: DatabaseReference
    }

    lateinit var mMainViewModel: MainViewModel

    private val countryList = arrayListOf("Australia","Brazil","Canada","China","France","Germany","India","Japan","Russia","South Korea","United Kingdom","United States")
    private val code = listOf("au","br","ca","cn","fr","de","in","jp","ru","kr","gb","us")

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        mFirebaseAuth = MainActivity.mFirebaseAuth
        mFirebaseDbUserRef = MainActivity.mFirebaseDbUserRef

        mMainViewModel = (activity as MainActivity).mMainViewModel
        val progressIndicator = view.findViewById<ProgressBar>(R.id.progressIndicator)
        progressIndicator.visibility = View.VISIBLE

        // SECTIONS
        val exclusive : CheckBox = view.findViewById(R.id.exclusiveSectionIsChecked)
        val health : CheckBox = view.findViewById(R.id.healthSectionIsChecked)
        val business : CheckBox = view.findViewById(R.id.businessSectionIsChecked)
        val entertainment : CheckBox = view.findViewById(R.id.entertainmentSectionIsChecked)
        val sports : CheckBox = view.findViewById(R.id.sportsSectionIsChecked)
        val technology : CheckBox = view.findViewById(R.id.technologySectionIsChecked)


        val fabSettings = view.findViewById<FloatingActionButton>(R.id.fabSettings)
        fabSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        val fullName = view.findViewById<TextView>(R.id.profileName)
        val username = view.findViewById<TextView>(R.id.profileUsername)
        val regionAutoCompleteTextView: AutoCompleteTextView = view.findViewById(R.id.countryAutoCompleteTextView)

        mMainViewModel.userProfile.value?.let {
            regionAutoCompleteTextView.setText(countryCodeToCountryName(it.countryCode))
        }

        mMainViewModel.userProfile.observe(viewLifecycleOwner, {
            Log.e(TAG, "user profile observe method called from profile fragment...")
            if(it!=null){ // it should always be not null anyways...

                fullName.text = it.fullName
                username.text = "@${it.username}"

                val adapterCountry = ArrayAdapter(requireContext(), R.layout.each_item_country, countryList)
                //regionAutoCompleteTextView.setText(countryCodeToCountryName(it.countryCode))
                regionAutoCompleteTextView.setAdapter(adapterCountry)

                regionAutoCompleteTextView.addTextChangedListener { text ->
                    val code = countryListToCountryCode(text.toString())
                    mFirebaseDbUserRef.child("countryCode").setValue(code)
                }

                exclusive.isChecked = it.exclusive
                health.isChecked = it.health
                business.isChecked = it.business
                entertainment.isChecked = it.entertainment
                sports.isChecked = it.sports
                technology.isChecked = it.technology

                progressIndicator.visibility = View.INVISIBLE
            }
        })

        exclusive.setOnCheckedChangeListener { buttonView, isChecked ->
            mFirebaseDbUserRef.child("exclusive").setValue(isChecked)
        }
        health.setOnCheckedChangeListener { buttonView, isChecked ->
            mFirebaseDbUserRef.child("health").setValue(isChecked)
        }
        business.setOnCheckedChangeListener { buttonView, isChecked ->
            mFirebaseDbUserRef.child("business").setValue(isChecked)
        }
        entertainment.setOnCheckedChangeListener { buttonView, isChecked ->
            mFirebaseDbUserRef.child("entertainment").setValue(isChecked)
        }
        sports.setOnCheckedChangeListener { buttonView, isChecked ->
            mFirebaseDbUserRef.child("sports").setValue(isChecked)
        }
        technology.setOnCheckedChangeListener { buttonView, isChecked ->
            mFirebaseDbUserRef.child("technology").setValue(isChecked)
        }


        val savedNewsCard: CardView = view.findViewById(R.id.goToSavedNewsCard)
        savedNewsCard.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_savedNewsFragment) }

        val signOutButton = view.findViewById<MaterialButton>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setBackground(ResourcesCompat.getDrawable(resources, R.color.snack_bar_bluish_grey, null))
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out? All your saved articles will be lost.")
                .setPositiveButton("Continue") { dialog, which ->
                    // deleting all articles from the database
                    mMainViewModel.deleteAllTheArticlesFromDatabaseMVM()
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent((activity as MainActivity), LoginScreen::class.java)
                    startActivity(intent)
                    (activity as MainActivity).finish()
                }
                .setNeutralButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .show().setCancelable(false)

        }

        return view
    }

    private fun countryListToCountryCode(country: String): String {
        return when(country){
            countryList[0]->code[0]
            countryList[1]->code[1]
            countryList[2]->code[2]
            countryList[3]->code[3]
            countryList[4]->code[4]
            countryList[5]->code[5]
            countryList[6]->code[6]
            countryList[7]->code[7]
            countryList[8]->code[8]
            countryList[9]->code[9]
            countryList[10]->code[10]
            countryList[11]->code[11]
            else->"in"
        }
    }

    private fun countryCodeToCountryName(countryCode: String): String{
        return when(countryCode){
            code[0]->countryList[0]
            code[1]->countryList[1]
            code[2]->countryList[2]
            code[3]->countryList[3]
            code[4]->countryList[4]
            code[5]->countryList[5]
            code[6]->countryList[6]
            code[7]->countryList[7]
            code[8]->countryList[8]
            code[9]->countryList[9]
            code[10]->countryList[10]
            code[11]->countryList[11]
            else->"India"
        }
    }

}