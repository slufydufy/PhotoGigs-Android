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
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.post_add.*
import java.sql.Timestamp
import java.util.*

class PostAdd : AppCompatActivity() {

    var name : String? = null
    var userImageUrl : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_add)
        //enabled back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        progressBar2.visibility = View.INVISIBLE

        //open gallery if image tap
        post_add_imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        fetchUser()
    }

    private fun fetchUser() {
        val uuid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    name = user!!.name
                    userImageUrl = user.userImageUrl
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("fetch user", "err : $p0")
            }
        })
    }

    //back button dismiss
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    //displayed choosen image
    private var selectedPhotoUri : Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            post_add_imageView.requestLayout()
            post_add_imageView.layoutParams.width = 0
            post_add_imageView.layoutParams.width = 0
            Glide.with(this).load(selectedPhotoUri).into(post_add_imageView)
//            Picasso.get().load(selectedPhotoUri).into(post_add_imageView)
//            post_add_imageView.setImageBitmap(bitmap)
        }
    }

    //enabled post button action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.post_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    //save image to firebase storage
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        progressBar2.visibility = View.VISIBLE
        when (item?.itemId) {
            R.id.menu_post -> {
                val filename = UUID.randomUUID().toString()
                val ref = FirebaseStorage.getInstance().getReference("/postsImage/$filename")
                if (selectedPhotoUri == null) {
                    Toast.makeText(this, "Gambar harus dipilih", Toast.LENGTH_SHORT).show()
                    return false
                }
                ref.putFile(selectedPhotoUri!!)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            savePost(it.toString())
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //save post to firebase database
    private fun savePost(imageUrlFireBase : String) {
        val postId = UUID.randomUUID().toString()
        val pd = Timestamp(System.currentTimeMillis()).toString()
        val postCaption = caption_textView.text.toString()
        val comment = ""
        val postRef = FirebaseDatabase.getInstance().getReference("/posts/$postId")
        val post = Post(name!!, pd, imageUrlFireBase, postCaption, userImageUrl, postId, comment)
        postRef.setValue(post)
            .addOnSuccessListener {
                Log.d("add post", "suksess $it")
                val intent = Intent(this, PostMain::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

            .addOnFailureListener {
                Log.d("add post", "error $it")
            }
    }
}