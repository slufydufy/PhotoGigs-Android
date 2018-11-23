package com.malmalmal.photogigs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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


        //pick profile image
        profile_imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        fetchUser()
    }

    //enable back button
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    //fetch user info
    var profileImageData : String? = ""
    var aboutInfo : String? = ""

    fun fetchUser() {
        val uuid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    username_textView.setText(user!!.name)
                    aboutInfo = user.about
                    if (user.about.isEmpty())  return
                    about_textView.setText(user.about)
                    profileImageData = user.userImageUrl
                    if (user.userImageUrl.isEmpty()) return
                    Picasso.get().load(profileImageData).into(profile_imageView)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("fetch user", "err : $p0")
            }

        })
    }

    //set picked image to image profile
    var selectedPhotoUri : Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            profile_imageView.setImageBitmap(bitmap)
        }
    }

    //enable save button action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //save menu button action
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.menu_save_profile -> {
                if (selectedPhotoUri == null) {

                    saveUserInfoToFirebase(profileImageData!!)
                } else {

                    val filename = UUID.randomUUID().toString()
                    val ref = FirebaseStorage.getInstance().getReference("/profileImage/$filename")
                    ref.putFile(selectedPhotoUri!!)
                        .addOnSuccessListener {

                            //get image url from firebase
                            ref.downloadUrl.addOnSuccessListener {

                                saveUserInfoToFirebase(it.toString())
                            }
                        }

                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //save change to firebase
    fun saveUserInfoToFirebase(piu : String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        if (username_textView.text.isEmpty()) {
            Toast.makeText(this, "username, harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (about_textView.text.isEmpty()) {
            aboutInfo = ""
        } else {
            aboutInfo = about_textView.text.toString()
        }

        val user = User(uid, username_textView.text.toString(), piu, aboutInfo!!)
        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
            }

            .addOnFailureListener {
                Log.d("profile save", "error : $it")
            }
    }


}
