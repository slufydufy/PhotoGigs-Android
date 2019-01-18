package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.mission_list.*
import kotlinx.android.synthetic.main.profile_main_user_gallery.view.*

class MissionList : AppCompatActivity() {

    var missId : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mission_list)

        //enabled back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mid = intent.getStringExtra("MISSION")
        missId = mid

        val adapter = GroupAdapter<ViewHolder>()

        missionList_recyclerView.layoutManager = LinearLayoutManager(this)
        missionList_recyclerView.addItemDecoration(CustomItemDecoration(0,20,0,0))
        missionList_recyclerView.adapter = adapter
        fetchPost()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
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
                        adapter.add(MissionListRow(post, missId!!))
                    }
                    missionList_recyclerView.adapter = adapter
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}

class MissionListRow(val post : Post, val mid : String) : Item<ViewHolder>() {


    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.userGallery_card.radius = 0f

        val img = viewHolder.itemView.gallery_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(img.context).applyDefaultRequestOptions(ro).load(post.imageUrl).into(img)

        img.setOnClickListener {

            val postRef = FirebaseDatabase.getInstance().getReference("/posts/${post.postId}")
            val missRef = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/mission/en-US/$mid/posts/${post.postId}")
            copyPost(postRef, missRef)
            Toast.makeText(img.context, "Your photos has been submitted..!", Toast.LENGTH_LONG).show()
            val intent = Intent(img.context, MissionMain::class.java)
            it.context.startActivity(intent)
        }

    }

    private fun copyPost(fromPath : DatabaseReference, toPath : DatabaseReference ) {
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

    override fun getLayout(): Int {
        return R.layout.profile_main_user_gallery
    }
}
