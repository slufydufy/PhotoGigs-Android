package com.malmalmal.photogigs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.event_detail.*
import kotlinx.android.synthetic.main.mission_detail.*
import kotlinx.android.synthetic.main.profile_main_user_gallery.view.*

class MissionList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_detail)

        val adapter = GroupAdapter<ViewHolder>()

        eventDetail_recyclerView.layoutManager = LinearLayoutManager(this)
        eventDetail_recyclerView.addItemDecoration(CustomItemDecoration(0,20,0,0))
        eventDetail_recyclerView.adapter = adapter
        fetchPost()
    }

    private fun fetchPost() {
        //fetch post
        val uuid = FirebaseAuth.getInstance().uid
        val adapter = GroupAdapter<ViewHolder>()
        val ref = FirebaseDatabase.getInstance().getReference("/posts").orderByChild("uuid").equalTo("$uuid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val post = it.getValue(Post::class.java)
                    if (post != null) {
                        adapter.add(MissionListRow(post))
                    }
                    eventDetail_recyclerView.adapter = adapter
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}

class MissionListRow(val post : Post) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val img = viewHolder.itemView.gallery_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(img.context).applyDefaultRequestOptions(ro).load(post.imageUrl).into(img)
    }

    override fun getLayout(): Int {
        return R.layout.profile_main_user_gallery
    }
}
