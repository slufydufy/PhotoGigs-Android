package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*


class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login.setOnClickListener {
            loginFirebase()
        }
    }


    fun loginFirebase() {

        val email = textView_email_login.text.toString()
        val pass = textView_pass_login.text.toString()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Masukkan email dan password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d("login", "Sukses login : ${it.result.user.uid}")

                val intent = Intent(this, Home::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

            .addOnFailureListener {
                Toast.makeText(this, "failed : ${it.message}", Toast.LENGTH_SHORT).show()
                return@addOnFailureListener
            }



    }

}