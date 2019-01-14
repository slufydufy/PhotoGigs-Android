package com.malmalmal.photogigs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.KeyEvent
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
import kotlinx.android.synthetic.main.profile_main.*
import kotlinx.android.synthetic.main.profile_main_bottom.view.*
import kotlinx.android.synthetic.main.profile_main_top.view.*
import kotlinx.android.synthetic.main.profile_main_user_gallery.view.*

class ProfileMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_main)

        showBottomBar()

        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(ProfileMainTop())
        adapter.add(ProfileMainBottom())

        profile_main_recyclerView.layoutManager = LinearLayoutManager(this)
        profile_main_recyclerView.adapter = adapter

        profile_main_floating_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent(this, PostMain::class.java)
            startActivity(intent)
            return true;
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            val intent = Intent(this, PostAdd::class.java)
            intent.putExtra("IMAGE", data.data.toString())
            startActivity(intent)
        }
    }

    private fun showBottomBar() {
        profile_main__bottomNavigationView.itemIconTintList = null
        profile_main__bottomNavigationView.menu.getItem(3).setChecked(true)
        profile_main__bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_home -> {
                    val intent = Intent(this, PostMain::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_news -> {
                    val intent = Intent(this, EventsMain::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_article -> {
                    val intent = Intent(this, MissionMain::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
    }
}

class ProfileMainTop : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val uuid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
//                    profile_main_progressBar.visibility = View.INVISIBLE
                    val user = p0.getValue(User::class.java)
                    viewHolder.itemView.user_textView.text = user!!.name

                    val image = viewHolder.itemView.profile_imageView
                    val ro = RequestOptions().placeholder(R.drawable.user123)
                    Glide.with(image.context).applyDefaultRequestOptions(ro).load(user.userImageUrl).into(image)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("fetch user", "err : $p0")
            }
        })

        val btn = viewHolder.itemView.edit_imageView
        btn.setOnClickListener {

                val intent = Intent(it.context, ProfileEdit::class.java)
                it.context.startActivity(intent)
            }
    }

    override fun getLayout(): Int {
        return R.layout.profile_main_top
    }
}



class ProfileMainBottom : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val rV = viewHolder.itemView.profile_main_bottom_recycleView
        rV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rV.addItemDecoration(CustomItemDecoration(0,20,10,10))

        val uuid = FirebaseAuth.getInstance().uid
        val adapter = GroupAdapter<ViewHolder>()
        val ref = FirebaseDatabase.getInstance().getReference("/posts").orderByChild("uuid").equalTo("$uuid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val post = it.getValue(Post::class.java)
                    if (post != null) {
                        adapter.add(ImageGallery(post))
                    }
                    rV.adapter = adapter
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.profile_main_bottom
    }

}

class ImageGallery(val post : Post) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        val image = viewHolder.itemView.gallery_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(image.context).applyDefaultRequestOptions(ro).load(post.imageUrl).into(image)

        image.setOnClickListener {
            val intent = Intent(it.context, PostDetail::class.java)
            intent.putExtra("POST", post.postId)
            intent.putExtra("USER", post.uuid)
            it.context.startActivity(intent)
        }
    }

    override fun getLayout(): Int {
        return R.layout.profile_main_user_gallery
    }

}













