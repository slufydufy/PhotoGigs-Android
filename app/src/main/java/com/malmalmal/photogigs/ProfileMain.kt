package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.profile_main.*

class ProfileMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_main)

        profile_main__bottomNavigationView.itemIconTintList = null
        profile_main__bottomNavigationView.menu.getItem(3).setChecked(true)
        profile_main__bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_home -> {
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_post -> {
                    val intent = Intent(this, PostMain::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_article -> {
                    val intent = Intent(this, ArticleMain::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
            }
                return@setOnNavigationItemSelectedListener false
            }

        profile_main_userImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        edit_imageView.setOnClickListener {
            val intent = Intent(this, ProfileEdit::class.java)
            startActivity(intent)
        }



        fetchUser()

    }

    var profileImageData : String? = ""
    private fun fetchUser() {
        val uuid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    profile_main_progressBar.visibility = View.INVISIBLE
                    val user = p0.getValue(User::class.java)
                    profile_main_usernameTextView.text = user!!.name
                    profileImageData = user.userImageUrl
                    val ro = RequestOptions().placeholder(R.drawable.baseline_person_white_24dp)
                    Glide.with(profile_main_userImageView.context).applyDefaultRequestOptions(ro).load(user.userImageUrl).into(profile_main_userImageView)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                    Log.d("fetch user", "err : $p0")
            }
        })
    }
}