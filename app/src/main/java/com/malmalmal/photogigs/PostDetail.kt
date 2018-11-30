package com.malmalmal.photogigs

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.comment_row.view.*
import kotlinx.android.synthetic.main.post_detail.*
import kotlinx.android.synthetic.main.post_main_row.view.*
import java.sql.Timestamp
import java.util.*

class PostDetail : AppCompatActivity() {

    var name : String? = null
    var userImageUrl : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_detail)

        fetchUser()

        val pid = intent.getStringExtra("POST")

        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(PostDetailRow(pid))

        recyclerView_post_detail.layoutManager = LinearLayoutManager(this)
        recyclerView_post_detail.adapter = adapter

        fetchComment(pid)

        comment_send_btn.setOnClickListener {
            sendComment(pid)
        }

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

    private fun fetchComment(pid : String) {
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(PostDetailRow(pid))
        val ref = FirebaseDatabase.getInstance().getReference("/comments/$pid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val comment = it.getValue(Comment::class.java)
                    if (comment != null) {
                        adapter.add(PostCommentRow(comment))
                    }
                    recyclerView_post_detail.adapter = adapter

                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }


    fun sendComment(pid : String) {

        if (commentAdd_text.text != null) {
            val commentId = UUID.randomUUID().toString()
            val pd = Timestamp(System.currentTimeMillis()).toString()
            val commentText = commentAdd_text.text.toString()
            val postRef = FirebaseDatabase.getInstance().getReference("/comments/$pid/$commentId")
            val comment = Comment(userImageUrl,name!!,commentText,pd)
            postRef.setValue(comment)
                .addOnSuccessListener {
                    Log.d("add comment", "suksess $it")
                    fetchComment(pid)
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (imm.isAcceptingText) {
                        imm.hideSoftInputFromWindow(this.currentFocus.windowToken, 0)
                    }
                    commentAdd_text.text.clear()

                }

                .addOnFailureListener {
                    Log.d("add post", "error $it")
                }
        }
    }
}

class PostDetailRow(pid : String) : Item<ViewHolder>() {

    val postId = pid

    override fun bind(viewHolder: ViewHolder, position: Int) {

            val ref = FirebaseDatabase.getInstance().getReference("/posts/$postId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val post = p0.getValue(Post::class.java)
                        viewHolder.itemView.userName_textView.text = post!!.name
                        viewHolder.itemView.date_textView.text = post.pd
                        viewHolder.itemView.story_textView.text = post.caption

                        val image =  viewHolder.itemView.post_main_imageView
                        Glide.with(image.context).load(post.imageUrl).into(image)
                        val imageUser =  viewHolder.itemView.profile_pic_imageView
                        Glide.with(imageUser.context).load(post.userImageUrl).into(imageUser)
                    }

                }
                override fun onCancelled(p0: DatabaseError) {
                }
            })
    }

    override fun getLayout(): Int {
        return R.layout.post_main_row
    }
}

class PostCommentRow(val comment : Comment) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.comment_username_textView.text = comment.name
        viewHolder.itemView.comment_textView.text = comment.comment
        viewHolder.itemView.comment_date_textView.text = comment.pd

        val userImage = viewHolder.itemView.comment_profile_imageView
        Glide.with(userImage.context).load(comment.profileImage).into(userImage)
    }

    override fun getLayout(): Int {
        return R.layout.comment_row
    }
}

class Comment(val profileImage : String, val name : String, val comment : String, val pd : String) {
    constructor() : this("","","","")
}