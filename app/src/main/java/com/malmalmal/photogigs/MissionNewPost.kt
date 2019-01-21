package com.malmalmal.photogigs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.post_add.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class MissionNewPost : AppCompatActivity() {
    var selectedPhotoUri : Uri? = null
    var missId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_add)
        addPost_progressBar.visibility = View.INVISIBLE

        //enabled back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //get uri from FAB
        val uriString = intent.getStringExtra("IMAGE")
        val uri = Uri.parse(uriString)
        selectedPhotoUri = uri

        val mid = intent.getStringExtra("MISSION")
        missId = mid

        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(PostAddTop(uri))
        adapter.add(InfoTambahan())

        post_add_recycleView.layoutManager = LinearLayoutManager(this)
        post_add_recycleView.adapter = adapter
    }

    //back button dismiss
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    //enabled post button action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //post image to firebase storage
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_save -> {
                addPost_progressBar.visibility = View.VISIBLE
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
        val sdf = SimpleDateFormat("dd MMM yyyy")
        val timeStamp = Timestamp(System.currentTimeMillis())
        val order = timeStamp.toString()
        val date = sdf.format(timeStamp)
        val postId = UUID.randomUUID().toString()
        val ca = findViewById<EditText>(R.id.caption_textView)
        val captionText = ca.text.toString()
        val postRef = FirebaseDatabase.getInstance().getReference("/posts/$postId")
        val post = Post(postId,date,imageUrlFireBase,captionText,userId!!,order)
        postRef.setValue(post)
            .addOnSuccessListener {
                Log.d("add post", "suksess $it")
                addInfo(postId)
                addPost_progressBar.visibility = View.INVISIBLE
                val intent = Intent(this, PostMain::class.java)
                startActivity(intent)
            }

            .addOnFailureListener {
                Log.d("add post", "error $it")
            }
    }

    //save additional info
    private fun addInfo(postId : String) {
        val kamera = findViewById<EditText>(R.id.kamera_editText)
        var k = ""
        if (kamera != null) {
            k = kamera.text.toString()
        }
        val lensa = findViewById<EditText>(R.id.lensa_editText)
        var l = ""
        if (lensa != null) {
            l = lensa.text.toString()
        }
        val flash = findViewById<EditText>(R.id.flash_editText)
        var f = ""
        if (flash != null) {
            f = flash.text.toString()
        }
        val diafragma = findViewById<EditText>(R.id.diafragma_editText)
        var d = ""
        if (diafragma != null) {
            d = diafragma.text.toString()
        }
        val iso = findViewById<EditText>(R.id.iso_editText)
        var i = ""
        if (iso != null) {
            i = iso.text.toString()
        }
        val speed = findViewById<EditText>(R.id.speed_editText)
        var s = ""
        if (speed != null) {
            s = speed.text.toString()
        }
        val infoRef = FirebaseDatabase.getInstance().getReference("/addInfo/$postId")
        val addInfo = AddInfo(postId, k, l, f, d, i, s)
        infoRef.setValue(addInfo)
            .addOnSuccessListener {
                Log.d("add info", "sukses $it")
                val postRef = FirebaseDatabase.getInstance().getReference("/posts/$postId")
                val missRef = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/mission/en-US/$missId/posts/$postId")
                copyPost(postRef, missRef)
                Toast.makeText(this, "Your photos has been submitted..!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MissionMain::class.java)
                this.startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("add info", "error $it")
            }
    }

    private fun copyPost(fromPath : DatabaseReference, toPath : DatabaseReference) {
        fromPath.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                toPath.setValue(p0.getValue()).addOnSuccessListener {
                    //success message
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}