package com.malmalmal.photogigs


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.ActionBar.LayoutParams.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.*
import kotlinx.android.synthetic.main.post_add.*
import kotlinx.android.synthetic.main.post_add_expandable_header.view.*
import kotlinx.android.synthetic.main.post_add_top.view.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class PostAdd : AppCompatActivity() {

    var selectedPhotoUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_add)

        //enabled back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //hide progress bar on start
        addPost_progressBar.visibility = View.INVISIBLE

        //openGallery()
        val uriString = intent.getStringExtra("IMAGE")
        val uri = Uri.parse(uriString)
        selectedPhotoUri = uri

        //create recyclerView
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
                }
                .addOnFailureListener {
                    Log.d("add info", "error $it")
                }
    }
}

class PostAddTop(private val uri : Uri) : Item<ViewHolder>() {

    override fun bind(p0: ViewHolder, p1: Int) {
        //show picked image from gallery
        val image = p0.itemView.post_add_top_imageView
        Glide.with(image.context).load(uri).into(image)
    }

    override fun getLayout(): Int {
        return R.layout.post_add_top
    }
}

class InfoTambahan : Item<ViewHolder>() /*,ExpandableItem*/ {
    override fun bind(p0: ViewHolder, p1: Int) {

        val c = p0.itemView.post_add_cons
        var isExpanded = true

        //show - hide additional Info
        p0.itemView.expandable_textView.setOnClickListener {
            if (isExpanded) {
                c.requestLayout()
                c.layoutParams.height = 0
                isExpanded = false
                p0.itemView.expand_imageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_white_24dp)

            } else {
                c.requestLayout()
                val h = WRAP_CONTENT
                c.layoutParams.height = h
                isExpanded = true
                p0.itemView.expand_imageView.setImageResource(R.drawable.baseline_keyboard_arrow_up_white_24dp)
            }
        }

        p0.itemView.expand_imageView.setOnClickListener {
            if (isExpanded) {
                c.requestLayout()
                c.layoutParams.height = 0
                isExpanded = false
                p0.itemView.expand_imageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_white_24dp)

            } else {
                c.requestLayout()
                val h = WRAP_CONTENT
                c.layoutParams.height = h
                isExpanded = true
                p0.itemView.expand_imageView.setImageResource(R.drawable.baseline_keyboard_arrow_up_white_24dp)
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.post_add_expandable_header
    }
}