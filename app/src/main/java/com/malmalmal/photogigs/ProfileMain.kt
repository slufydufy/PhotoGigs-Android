package com.malmalmal.photogigs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_main.*
import java.util.*


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

    var selectedPhotoUri : Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
//            val bitmapDraw = BitmapDrawable(bitmap)
            profile_imageView.setImageBitmap(bitmap)
        }
    }

    fun fetchUser() {

        val uuid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    username_textView.setText(user!!.name)
                    about_textView.setText(user.about)
                    Picasso.get().load(user.userImageUrl).into(profile_imageView)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.menu_save_profile -> {
                if (selectedPhotoUri == null) return false

                val filename = UUID.randomUUID().toString()
                val ref = FirebaseStorage.getInstance().getReference("/profileImage/$filename")
                ref.putFile(selectedPhotoUri!!)
                    .addOnSuccessListener {

                        ref.downloadUrl.addOnSuccessListener {
                            saveUserInfoToFirebase(it.toString())
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun saveUserInfoToFirebase(profileImageUrl : String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, username_textView.text.toString(), profileImageUrl, about_textView.text.toString())
        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
            }
    }


}
