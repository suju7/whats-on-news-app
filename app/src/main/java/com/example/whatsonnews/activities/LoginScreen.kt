package com.example.whatsonnews.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsonnews.R
import com.example.whatsonnews.dataclasses.UserProfile
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*

class LoginScreen : AppCompatActivity() {

    companion object {
        const val RC_SIGN_IN = 1
        const val TAG = "LoginScreen"
    }

    private lateinit var callbackManager: CallbackManager
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mFirebaseUserDbRef: DatabaseReference
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUserDbRef = FirebaseDatabase.getInstance().reference.child("users")
        val textEmailOrUsername = findViewById<TextInputEditText>(R.id.emailOrUsername)
        val textPassword = findViewById<TextInputEditText>(R.id.password)
        textPassword.transformationMethod = PasswordTransformationMethod()
        //val facebookButton = findViewById<Button>(R.id.facebook_button)
        val googleButton = findViewById<Button>(R.id.google_button)
        val forgotPassword = findViewById<Button>(R.id.forgot_password)
        val submitButton = findViewById<Button>(R.id.login_button)
        val signUpButton = findViewById<Button>(R.id.new_user_signup)
        progressBar = findViewById(R.id.loginProgress)
        progressBar.isIndeterminate = true
        progressBar.visibility = View.GONE

        forgotPassword.visibility = View.GONE

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mGoogleSignInClient.signOut()



        submitButton.setOnClickListener {

            val emailOrUsername = textEmailOrUsername.text.toString().toLowerCase(Locale.ROOT).trim()
            if (emailOrUsername.isEmpty()) {
                textEmailOrUsername.error = "This field cannot be empty"
                textEmailOrUsername.requestFocus()
                return@setOnClickListener
            }

            val password = textPassword.text.toString()
            if (password.length < 6) {
                textPassword.error = "Password should be at least 6 characters long"
                textPassword.requestFocus()
                textPassword.setSelection(password.length)
                return@setOnClickListener
            }

            if (isValidEmail(emailOrUsername)) {
                // for email and password log in
                manualSignInWithEmailOrUsernameAndPassword(emailOrUsername, password)
            } else {
                // for username and password login
                val query = mFirebaseUserDbRef.orderByChild("username").equalTo(emailOrUsername)
                query.addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                val key = snap.key
                                val email = snap.child("email").value.toString()
                                Log.e(TAG, "KEY (uid) and EMAIL VALUES ARE : $key and $email")
                                manualSignInWithEmailOrUsernameAndPassword(email, password)
                            }
                        } else {
                            Toast.makeText(
                                this@LoginScreen,
                                "No such username/email exists",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(TAG, "no such blah blah")
                        }
                    }


                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "some log in error. retry...")
                    }
                })

            }
        }

/*

        facebookButton.setOnClickListener {

            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().unregisterCallback(callbackManager)
            LoginManager.getInstance().logOut()
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
            var accessToken = AccessToken.getCurrentAccessToken()
            */
/*
            while (accessToken.declinedPermissions == listOf("email")) {
                Toast.makeText(this, "email is required", Toast.LENGTH_SHORT).show()
                LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
                accessToken = AccessToken.getCurrentAccessToken()
            }
            *//*

            val isLoggedIn = accessToken != null && !accessToken.isExpired
            LoginManager.getInstance()
                .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        Log.e(TAG, "I AM HERE")
                        val credential =
                            FacebookAuthProvider.getCredential(result.accessToken.token)
                        firebaseAuthWithGoogleOrFacebook(credential)
                    }

                    override fun onCancel() {}
                    override fun onError(error: FacebookException?) {
                        Log.e(TAG, "Some Facebook error occurred...")
                        Toast.makeText(this@LoginScreen, error?.message, Toast.LENGTH_LONG).show();
                    }
                })
        }

*/


        googleButton.setOnClickListener {
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val user = mFirebaseAuth.currentUser
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return if (target == null) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
            Log.e(TAG, "request code is : $requestCode")
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.e(TAG, "GOOGLE SIGNED YOU IN. MY TURN NOW...")
            // Signed in successfully, show authenticated UI.
            val id: String? = account.idToken
            Log.v(TAG, "Your token: $id")
            val credential = GoogleAuthProvider.getCredential(id, null)
            firebaseAuthWithGoogleOrFacebook(credential)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogleOrFacebook(credential: AuthCredential?) {
        if (credential != null) {
            progressBar.visibility = View.VISIBLE
            mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.e(TAG, "signInWithCredential:Auth success....now the db part")

                        var isNewUser = task.result?.additionalUserInfo?.isNewUser == true
                        signInWithGoogleOrFacebook(isNewUser)
                        // then call main activity from there
                    } else {
                        Log.e(TAG, "some auth problem from google/facebook")
                    }
                    progressBar.visibility = View.GONE
                }
        }
    }

    private fun signInWithGoogleOrFacebook(isFirstTimeUser: Boolean) {

        val intent = Intent(this@LoginScreen, MainActivity::class.java)
        intent.putExtra("isFirstTimeUser",isFirstTimeUser)

        if(isFirstTimeUser) { // create database for our new user

            val user = FirebaseAuth.getInstance().currentUser
            val displayName = user?.displayName ?: "Anonymous"
            val username = "anonymous${System.currentTimeMillis()}"
            val email = user?.email ?: "no_email_provided"
            if(user!=null){
                val userForDatabase = UserProfile(displayName, username, email,"in",true,true,false,false,true,true)
                mFirebaseUserDbRef.child(user.uid).setValue(userForDatabase).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Log.e(TAG, "Database created for new user.")

                        startActivity(intent)
                        finish()
                    }
                    else {
                        Log.e(TAG, "Database entry not successful")
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Database entry error:")
                            .setMessage("Database entry not successful. Please try again.")
                            .setPositiveButton("Retry") { dialog, which ->
                                signInWithGoogleOrFacebook(isFirstTimeUser)
                            }
                            .setNeutralButton("Cancel") { dialog, which ->
                                user?.delete()
                                dialog.dismiss()
                            }
                            .show().setCancelable(false)
                    }
                }
            }
        } else {
            startActivity(intent)
            finish()
        }
    }

    private fun emailExistsInDatabase(email: String): Boolean {
        val query = mFirebaseUserDbRef.orderByChild("email").equalTo(email)
        var exists = false
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                    exists = true
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        Log.e(TAG, "Value of exists : $exists")
        return exists
    }



    private fun usernameExistsInDatabase(username: String): Boolean {
        val query = mFirebaseUserDbRef.orderByChild("username").equalTo(username)
        var exists = false
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                    exists = true
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        Log.e(TAG, "VAlue of exists : $exists")
        return exists
    }

    private fun manualSignInWithEmailOrUsernameAndPassword(email: String, password: String) {

        val query = mFirebaseUserDbRef.orderByChild("email").equalTo(email)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    mFirebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this@LoginScreen,
                                    "Successfully logged in",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this@LoginScreen, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@LoginScreen,
                                    "Couldn't log you in. Please check if your password is correct.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this@LoginScreen,
                        "No such email/username exists",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "SOME ERROR")
            }
        })

    }
}