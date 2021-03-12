package com.example.whatsonnews.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import com.example.whatsonnews.R
import com.example.whatsonnews.dataclasses.UserProfile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.*
import com.google.firebase.database.*
import java.util.*

class SignUp : AppCompatActivity() {

    companion object{
        const val TAG = "SignUpActivity"
    }

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mFirebaseAuthListener: FirebaseAuth.AuthStateListener
    private lateinit var mFirebaseUserDbRef: DatabaseReference
    private lateinit var progressIndicator: LinearProgressIndicator
    lateinit var consView: View

    private lateinit var textUsername: TextInputEditText
    private lateinit var textEmail: TextInputEditText

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUserDbRef = FirebaseDatabase.getInstance().reference.child("users")
        consView = findViewById(R.id.signUpConstraintLayout)

        val textName = findViewById<TextInputEditText>(R.id.fullname)
        textEmail = findViewById<TextInputEditText>(R.id.email)
        textUsername = findViewById<TextInputEditText>(R.id.username)
        val textPassword = findViewById<TextInputEditText>(R.id.setPassword)
        textPassword.transformationMethod = PasswordTransformationMethod()
        val regButton = findViewById<Button>(R.id.register_button)
        progressIndicator = findViewById(R.id.signUpProgress)
        progressIndicator.isIndeterminate = true
        progressIndicator.visibility = View.GONE

        val backButton = findViewById<FloatingActionButton>(R.id.fabBackButton)
        backButton.setOnClickListener {
            finish()
        }

        var emailTicket = true
        val listener1 = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    textEmail.error = "This email is already taken"
                    textEmail.setSelection(textEmail.length())
                    textEmail.requestFocus()
                    emailTicket = false
                }
                else{
                    emailTicket = true
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "some error")
            }
        }

        var query1 : Query? = null
        query1?.addValueEventListener(listener1)

        textEmail.addTextChangedListener {
            query1 = mFirebaseUserDbRef.orderByChild("email").equalTo(textEmail.text.toString().trim())
        }

        var usernameTicket = true

        var listener2 = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    textUsername.error = "This username is already taken"
                    textUsername.requestFocus()
                    textUsername.setSelection(textUsername.length())
                    usernameTicket = false
                }
                else{
                    usernameTicket = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "some error")
            }
        }
        var query2 : Query? = null
        query2?.addValueEventListener(listener2)

        textUsername.addTextChangedListener {
            query2 = mFirebaseUserDbRef.orderByChild("username").equalTo(textUsername.text.toString().toLowerCase(Locale.ENGLISH).trim())
        }

        var name = textName.text.toString().trim()
        var username = textUsername.text.toString().toLowerCase(Locale.ENGLISH).trim()
        var email = textEmail.text.toString().toLowerCase(Locale.ENGLISH).trim()


        regButton.setOnClickListener {

            name = textName.text.toString().trim()
            username = textUsername.text.toString().toLowerCase(Locale.ENGLISH).trim()
            email = textEmail.text.toString().toLowerCase(Locale.ENGLISH).trim()
            val password = textPassword.text.toString()

            if (name.isEmpty()) {
                textName.error = "Name field cannot be empty"
                textName.requestFocus()
                return@setOnClickListener
            }

            if (username.isEmpty()) {
                val currentTime = System.currentTimeMillis()
                textUsername.setText("anonymous$currentTime")
                textUsername.requestFocus()
                textUsername.setSelection(textUsername.length())
                return@setOnClickListener
            } else {
                for (ch in username) {
                    if (!(ch.isLetterOrDigit() || ch == '_' || ch == '.')) {
                        textUsername.error = "Only letters, digits, underscore, period"
                        textUsername.requestFocus()
                        textUsername.setSelection(textUsername.length())
                        return@setOnClickListener
                    }
                }
            }

            if (!isValidEmail(email)) {
                textEmail.error = "Please enter a valid email"
                textEmail.requestFocus()
                textEmail.setSelection(email.length)
                return@setOnClickListener
            }

            if (password.length < 6) {
                textPassword.error = "Password should be at least 6 characters long"
                textPassword.requestFocus()
                textPassword.setSelection(password.length)
                return@setOnClickListener
            }


            if(usernameTicket && emailTicket) {
                query1?.removeEventListener(listener1)
                query2?.removeEventListener(listener2)
                progressIndicator.visibility = View.VISIBLE
                mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {

                            // database will be created after verifying the user

                            // if auth user does not verify himself, delete this newly created auth profile

                        } else {
                            when (it.exception) {
                                is FirebaseAuthUserCollisionException -> {
                                    Toast.makeText(
                                        this,
                                        "User already exists with this email!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                is FirebaseAuthWeakPasswordException -> {
                                    Toast.makeText(
                                        this,
                                        "Password is not strong enough!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        progressIndicator.visibility = View.GONE

                    }
            }

            val user = mFirebaseAuth.currentUser
            if( user!=null ){
                createUserProfile(user, name, username, email)
            }
        }

    }


    private fun createUserProfile(user: FirebaseUser, name: String, username: String, email: String){
        Toast.makeText(this,"Creating your profile...",Toast.LENGTH_SHORT).show()
        val userForDatabase = UserProfile(name, username, email,"in",true,true,false,false,true,true)
        mFirebaseUserDbRef.child(user.uid).setValue(userForDatabase).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this@SignUp, MainActivity::class.java)
                startActivity(intent)
                finish()
                Log.e(TAG,"database created successfully")
            } else {
                Toast.makeText(this,"Unexpected database error! Please try again.",Toast.LENGTH_LONG).show()
                user.delete()
            }
        }
    }

/*
    private fun makeAlertDialog(user: FirebaseUser, name: String, username: String, email: String) {

        progressIndicator.visibility = View.VISIBLE

        MaterialAlertDialogBuilder(this).setTitle("Email Verification Required!")
            .setBackground(ResourcesCompat.getDrawable(resources, R.color.snack_bar_bluish_grey, null))
            .setMessage("Verification link has been sent to your email: $email. Please check your inbox.")
            .setPositiveButton("Done"){ dialog, which ->
                if(user.isEmailVerified) {
                    Toast.makeText(this,"Email verified. Creating your profile...",Toast.LENGTH_SHORT).show()
                    val userForDatabase = UserProfile(name, username, email,"in",true,true,false,false,true,true)
                        mFirebaseUserDbRef.child(user.uid).setValue(userForDatabase).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(this@SignUp, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                                Log.e(TAG,"database created successfully")
                            } else {
                                Toast.makeText(this,"Unexpected database error! Please try again.",Toast.LENGTH_LONG).show()
                                user.delete()
                                dialog.dismiss()
                            }
                        }
                } else{ // bad bad user, but we won't create the database entry without verification. so, calling this method again.
                    user.reload()
                    Toast.makeText(this,"Email has not been verified!",Toast.LENGTH_LONG).show()
                        makeAlertDialog(user, name, username, email)
                }
            }
            .setNeutralButton("Cancel"){ dialog, which ->
                // user registration not completed thus deleting this user firebase auth account
                user.delete().addOnCompleteListener{
                    if(it.isSuccessful)
                        Log.e(TAG,"Auth user deleted. yass!!")
                    mFirebaseAuth.signOut()
                    progressIndicator.visibility = View.GONE
                    dialog.dismiss()
                }
            }.show().setCancelable(false)

        }
*/

    private fun isValidEmail(target: CharSequence?): Boolean {
        return if (target == null) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

}