package com.malmalmal.photogigs


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import java.text.SimpleDateFormat
import java.util.*

class PostAdd : AppCompatActivity() {

    var uuid : String? = null
    var name : String? = null
    var userImageUrl : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_add)
        //enabled back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        addPost_progressBar.visibility = View.INVISIBLE

        //open gallery if image tap
        post_add_imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        fetchUser()
    }

    private fun fetchUser() {
        val userId = FirebaseAuth.getInstance().uid
        uuid = userId
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
            post_add_imageView.requestLayout()
            post_add_imageView.layoutParams.width = 0
            post_add_imageView.layoutParams.width = 0
            Glide.with(this).load(selectedPhotoUri).into(post_add_imageView)
        }
    }

    //enabled post button action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.post_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    //save image to firebase storage
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        addPost_progressBar.visibility = View.VISIBLE
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
        val userId = FirebaseAuth.getInstance().uid
        val sdf = SimpleDateFormat("MMM dd yyyy")
        val timeStamp = Timestamp(System.currentTimeMillis())
        val date = sdf.format(timeStamp)
        val postId = UUID.randomUUID().toString()
        val postCaption = caption_textView.text.toString()
        val postRef = FirebaseDatabase.getInstance().getReference("/posts/$postId")
        val post = Post(postId,date,imageUrlFireBase,postCaption,userId!!)
        postRef.setValue(post)
            .addOnSuccessListener {
                Log.d("add post", "suksess $it")
                addPost_progressBar.visibility = View.INVISIBLE
                val intent = Intent(this, PostMain::class.java)
                startActivity(intent)
            }

            .addOnFailureListener {
                Log.d("add post", "error $it")
            }
    }
}