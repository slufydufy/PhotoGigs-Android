package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.post_add.*
import kotlinx.android.synthetic.main.post_add_expandable_header.view.*
import kotlinx.android.synthetic.main.post_add_top.view.*

class PostDetailEdit : AppCompatActivity() {

    private var ppid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_add)

        //enabled back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val pid = intent.getStringExtra("PID")
        ppid = pid

        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(PostDetailEditTop(pid))
        adapter.add(PostDetailEditInfo(pid))

        post_add_recycleView.layoutManager = LinearLayoutManager(this)
        post_add_recycleView.adapter = adapter

        addPost_progressBar.visibility = View.INVISIBLE
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

    //submit edit post
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        addPost_progressBar.visibility = View.VISIBLE
        when (item?.itemId) {
            R.id.menu_save -> {
                addPost_progressBar.visibility = View.VISIBLE
                val c = findViewById<EditText>(R.id.caption_textView)
                val capText = c.text.toString()
                val refPost = FirebaseDatabase.getInstance().getReference("/posts/$ppid/caption")
                refPost.setValue(capText)

                val k = findViewById<EditText>(R.id.kamera_editText)
                val kamera = k.text.toString()
                val l = findViewById<EditText>(R.id.lensa_editText)
                val lensa = l.text.toString()
                val f = findViewById<EditText>(R.id.flash_editText)
                val flash = f.text.toString()
                val d = findViewById<EditText>(R.id.diafragma_editText)
                val diafragma = d.text.toString()
                val i = findViewById<EditText>(R.id.iso_editText)
                val iso = i.text.toString()
                val s = findViewById<EditText>(R.id.speed_editText)
                val speed = s.text.toString()

                val infoRef = FirebaseDatabase.getInstance().getReference("/addInfo/$ppid")
                infoRef.child("kamera").setValue(kamera)
                infoRef.child("lensa").setValue(lensa)
                infoRef.child("flash").setValue(flash)
                infoRef.child("diafragma").setValue(diafragma)
                infoRef.child("iso").setValue(iso)
                infoRef.child("speed").setValue(speed)

                val uid = FirebaseAuth.getInstance().uid
                val intent = Intent(this, PostDetail::class.java)
                intent.putExtra("POST", ppid)
                intent.putExtra("USER", uid)
                startActivity(intent)

                addPost_progressBar.visibility = View.INVISIBLE
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

class PostDetailEditTop(private val pid : String) : Item<ViewHolder>() {

    override fun bind(v: ViewHolder, p1: Int) {
        //fetch post
        val refPost = FirebaseDatabase.getInstance().getReference("/posts/$pid")
        refPost.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(Post::class.java)
                    val imagePost = v.itemView.post_add_top_imageView
                    val ro = RequestOptions().placeholder(R.drawable.placeholder1)
                    Glide.with(imagePost.context).applyDefaultRequestOptions(ro).load(post!!.imageUrl).into(imagePost)

                    v.itemView.caption_textView.setText(post.caption)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.post_add_top
    }
}

class PostDetailEditInfo(private val pid: String) : Item<ViewHolder>() {
    override fun bind(v: ViewHolder, p1: Int) {

        //fetch add Info
        val refAddInfo = FirebaseDatabase.getInstance().getReference("/addInfo/$pid")
        refAddInfo.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val addInfo = p0.getValue(AddInfo::class.java)
                    v.itemView.kamera_editText.setText(addInfo!!.kamera)
                    v.itemView.lensa_editText.setText(addInfo.lensa)
                    v.itemView.flash_editText.setText(addInfo.flash)
                    v.itemView.diafragma_editText.setText(addInfo.diafragma)
                    v.itemView.iso_editText.setText(addInfo.iso)
                    v.itemView.speed_editText.setText(addInfo.speed)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        val c = v.itemView.post_add_cons
        var isExpanded = true
        v.itemView.expand_imageView.setOnClickListener {

            if (isExpanded) {
                c.requestLayout()
                c.layoutParams.height = 0
                isExpanded = false
                v.itemView.expand_imageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_white_24dp)
            } else {
                c.requestLayout()
                val h = ActionBar.LayoutParams.WRAP_CONTENT
                c.layoutParams.height = h
                isExpanded = true
                v.itemView.expand_imageView.setImageResource(R.drawable.baseline_keyboard_arrow_up_white_24dp)
            }

        }
    }

    override fun getLayout(): Int {
        return R.layout.post_add_expandable_header
    }
}




