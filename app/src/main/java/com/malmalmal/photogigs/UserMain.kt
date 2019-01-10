package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.profile_main_bottom.view.*
import kotlinx.android.synthetic.main.profile_main_top.view.*
import kotlinx.android.synthetic.main.profile_main_user_gallery.view.*
import kotlinx.android.synthetic.main.user_main.*

class UserMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_main)

        //enabled back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val uid = intent.getStringExtra("USER")

        //create main recyclerView
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(UserMainTop(uid))
        adapter.add(UserMainBottom(uid))

        userMain_recyclerView.layoutManager = LinearLayoutManager(this)
        userMain_recyclerView.adapter = adapter

        fetchUser(uid)

    }

    //back buton action
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    //set title to user name
    private fun fetchUser(uuid : String) {
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    this@UserMain.title = user!!.name
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("fetch user", "err : $p0")
            }
        })
    }

}

class UserMainTop(private val uuid : String) : Item<ViewHolder>() {

    override fun bind(v: ViewHolder, p1: Int) {
        //hide edit user image
        v.itemView.edit_imageView.visibility = View.INVISIBLE

        //fetch user
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    v.itemView.user_textView.text = user!!.name

                    val image = v.itemView.profile_imageView
                    val ro = RequestOptions().placeholder(R.drawable.user123)
                    Glide.with(image.context).applyDefaultRequestOptions(ro).load(user.userImageUrl).into(image)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("fetch user", "err : $p0")
            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.profile_main_top
    }
}

class UserMainBottom(val id : String) : Item<ViewHolder>() {
    override fun bind(v: ViewHolder, p1: Int) {

        //set recyclerView layout
        val rV = v.itemView.profile_main_bottom_recycleView
        rV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rV.addItemDecoration(CustomItemDecoration(0,20,10,10))

        //fetch post and add item to recyclerView
        val adapter = GroupAdapter<ViewHolder>()
        val ref = FirebaseDatabase.getInstance().getReference("/posts").orderByChild("uuid").equalTo(id)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val post = it.getValue(Post::class.java)
                    if (post != null) {
                        adapter.add(UserMainGallery(post))
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

class UserMainGallery(private val post : Post) : Item<ViewHolder>() {
    override fun bind(p0: ViewHolder, p1: Int) {
        //set image post to gallery
        val image = p0.itemView.gallery_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(image.context).applyDefaultRequestOptions(ro).load(post.imageUrl).into(image)

        //intent to post detail
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




