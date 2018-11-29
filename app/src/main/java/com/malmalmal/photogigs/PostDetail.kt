package com.malmalmal.photogigs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.post_detail.*
import kotlinx.android.synthetic.main.post_main_row.view.*

class PostDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_detail)

        val pid = intent.getStringExtra("POST")

        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(PostDetailRow(pid))
        adapter.add(PostCommentRow())
        adapter.add(PostCommentRow())
        adapter.add(PostCommentRow())
        adapter.add(PostCommentRow())
        adapter.add(PostCommentRow())
        adapter.add(PostCommentRow())


        postDetail_rycyclerView.layoutManager = LinearLayoutManager(this)
        postDetail_rycyclerView.adapter = adapter



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

                    val image = viewHolder.itemView.post_main_imageView
                    Glide.with(viewHolder.itemView.post_main_imageView.context).load(post.imageUrl).into(image)
                    val imageUser = viewHolder.itemView.profile_pic_imageView
                    Glide.with(viewHolder.itemView.profile_pic_imageView.context).load(post.userImageUrl).into(imageUser)
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

class PostCommentRow : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.comment_row
    }
}