package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.main_login.*
import org.json.JSONObject

class MainLogin : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private val callbackManager = CallbackManager.Factory.create()
    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_login)

        signOut()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        //google sign in button
        google_button.setOnClickListener {
            googleLogin()
        }

        //facebook sign in button
        facebook_button.setOnClickListener {
            fbLogin()
        }

        //email signin button
        email_button.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    //google Login
    private fun googleLogin() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 101)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("Google Sign In", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Google Sin In Sukses", "signInWithCredential:success")
//                    val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
                    val name = acct.displayName
                    val ppUri = acct.photoUrl.toString()
                    val uid = auth.currentUser!!.uid
                    val user = User(uid, name!!, ppUri, "")
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                    ref.setValue(user)
                        .addOnSuccessListener {
                            updateUI()
                        }

                        .addOnFailureListener {

                        }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Google Sign In", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
            }
    }

    private fun signOut() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        //set google option
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        this.googleSignInClient.signOut()

        LoginManager.getInstance().logOut()
    }

    // Initialize Facebook Login button
    private fun fbLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("FB Login", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult)
            }

            override fun onCancel() {
                Log.d("FB Login", "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d("FB Login", "facebook:onError", error)
                LoginManager.getInstance().logOut()
            }
        })
    }

    //action if FB login successfull
    private fun handleFacebookAccessToken(loginResult: LoginResult) {
        val token = loginResult.accessToken
        Log.d("FB Login handle token", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FB Login handle token", "signInWithCredential:success")
                    getFbInfo(loginResult)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FB Login handle token", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //get user info from FB
    private fun getFbInfo(loginResult: LoginResult) {

        val request = GraphRequest.newMeRequest(loginResult.accessToken) { `object`, response ->
            try {
                if (`object`.has("id")) {
                    saveFBResulttoFirebase(`object`)
                    Log.d("FB INFO", "ini : $`object`")
                } else {
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val parameters = Bundle()
        parameters.putString("fields", "name,email,id,picture.type(large)")
        request.parameters = parameters
        request.executeAsync()
    }

    //save FB Resultto Firebase
    private fun saveFBResulttoFirebase(data : JSONObject) {
        try {
            val name = data.getString("name")
            val uid = auth.currentUser!!.uid
            val user = User(uid, name, "", "")
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
            ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("create", "sukses create ke Firebase dari FB")
                    updateUI()
                }

                .addOnFailureListener {
                    Toast.makeText(this, "Failed : $it", Toast.LENGTH_SHORT).show()
                    return@addOnFailureListener
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //intent for success login
    private fun updateUI() {
        val intent = Intent(this, PostMain::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    // return of Google and FB activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent
        if (requestCode == 101) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("Google Sign In", "Google sign in failed", e)
                // ...
            }
        }

        // else pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}