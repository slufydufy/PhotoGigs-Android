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
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.main_login.*
import org.json.JSONObject

class MainLogin : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        google_button.setOnClickListener {

        }

        facebook_button.setOnClickListener {
            fbLogin()
        }

        email_button.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }



    }

    // Check if user is signed in
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.uid
        if (currentUser != null) {
            updateUI()
        }
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

    fun saveFBResulttoFirebase(data : JSONObject) {
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
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    // return of FB callback
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

}