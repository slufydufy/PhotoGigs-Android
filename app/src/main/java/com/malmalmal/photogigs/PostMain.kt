package com.malmalmal.photogigs


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.post_main.*
import kotlinx.android.synthetic.main.post_main_row.view.*

class PostMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_main)

        val adapter = GroupAdapter<ViewHolder>()


        post_main_recyclerView.layoutManager = LinearLayoutManager(this)
        post_main_recyclerView.adapter = adapter

        fetchPost()

        floatingActionButtonPost.setOnClickListener {
            val intent = Intent(this, PostAdd::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun fetchPost() {

        val adapter = GroupAdapter<ViewHolder>()
        val ref = FirebaseDatabase.getInstance().getReference("/posts")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val post = it.getValue(Post::class.java)
                    if (post != null) {
                        adapter.add(PostRow(post))
                    }

                    post_main_recyclerView.adapter = adapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

}

class PostRow(val post : Post) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.userName_textView.text = post.name
        viewHolder.itemView.date_textView.text = post.datePost
        viewHolder.itemView.story_textView.text = post.story

        val image = viewHolder.itemView.post_main_imageView
        Picasso.get().load(post.imageUrl).into(image)
        val imageUser = viewHolder.itemView.profile_pic_imageView
        Picasso.get().load(post.userImageUrl).into(imageUser)

        viewHolder.itemView.post_main_imageView.setOnClickListener {
            val intent = Intent(it.context, PostDetail::class.java)
            intent.putExtra("IMAGE", post.imageUrl)
            it.context.startActivity(intent)
        }

    }

    override fun getLayout(): Int {

        return R.layout.post_main_row
    }
}

class Post(val name : String, val datePost : String, val imageUrl : String, val story : String, val userImageUrl : String) {
    constructor() : this("", "", "", "", "")
}