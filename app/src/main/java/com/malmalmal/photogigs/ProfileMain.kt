package com.malmalmal.photogigs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.post_add.*
import kotlinx.android.synthetic.main.profile_main.*


class ProfileMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        profile_imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        fetchUser()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
//            val bitmapDraw = BitmapDrawable(bitmap)
            profile_imageView.setImageBitmap(bitmap)
        }


    }

    fun fetchUser() {

        val uuid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                var userFetch = User()
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    userFetch = user!!
                    username_profile.text = userFetch.name
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

}
