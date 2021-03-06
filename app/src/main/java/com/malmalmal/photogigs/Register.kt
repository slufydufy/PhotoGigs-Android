package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class Register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //register User by email
        button_registerMain.setOnClickListener {
            createUser()
        }
    }

    private fun createUser() {

        val email = email_register.text.toString()
        val pass = password_register.text.toString()
        val user = username_register.text.toString()

        if (email.isEmpty() || pass.isEmpty() || user.isEmpty()) {
            Toast.makeText(this, "Masukkan username, email, dan password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d("register", "sukses auth user!!")

                saveUserToFirebase()
            }

            .addOnFailureListener {
                Toast.makeText(this, "Failed : $it", Toast.LENGTH_SHORT).show()
                return@addOnFailureListener
            }
    }

    private fun saveUserToFirebase() {

        val uuid = FirebaseAuth.getInstance().uid ?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        val user = User(uuid, username_register.text.toString(), "", "")
        ref.setValue(user)
            .addOnSuccessListener {

                Log.d("create", "sukses create to Firebase db")
                val intent = Intent(this, PostMain::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

            .addOnFailureListener {
                Toast.makeText(this, "Failed : $it", Toast.LENGTH_SHORT).show()
                return@addOnFailureListener
            }
    }

}

