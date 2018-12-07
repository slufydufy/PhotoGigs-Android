package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.View
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
import kotlinx.android.synthetic.main.profile_main_user_gallery.view.*

class ProfileMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_main)

        showBottomBar()
        fetchUser()

        val adapter = GroupAdapter<ViewHolder>()
        profile_main_recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
//        profile_main_recyclerView.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
//        val decoration = RecyclerView.ItemDecoration
        profile_main_recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        profile_main_recyclerView.adapter = adapter

        edit_imageView.setOnClickListener {
            val intent = Intent(this, ProfileEdit::class.java)
            startActivity(intent)
        }

        fetchUserPost()
    }



    private fun fetchUser() {
        val uuid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    profile_main_progressBar.visibility = View.INVISIBLE
                    val user = p0.getValue(User::class.java)
                    profile_main_usernameTextView.text = user!!.name
                    val ro = RequestOptions().placeholder(R.drawable.baseline_person_white_24dp)
                    Glide.with(profile_main_userImageView.context).applyDefaultRequestOptions(ro).load(user.userImageUrl).into(profile_main_userImageView)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("fetch user", "err : $p0")
            }
        })
    }

    fun fetchUserPost() {

        val uuid = FirebaseAuth.getInstance().uid
        val adapter = GroupAdapter<ViewHolder>()
        val ref = FirebaseDatabase.getInstance().getReference("/posts").orderByChild("uuid").equalTo("$uuid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val post = it.getValue(Post::class.java)
                    if (post != null) {
                        adapter.add(ProfileGallery(post))
                    }
                    profile_main_recyclerView.adapter = adapter
//                    post_progressBar.visibility = View.INVISIBLE
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    fun showBottomBar() {
        profile_main__bottomNavigationView.itemIconTintList = null
        profile_main__bottomNavigationView.menu.getItem(3).setChecked(true)
        profile_main__bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_home -> {
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_post -> {
                    val intent = Intent(this, PostMain::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_article -> {
                    val intent = Intent(this, ArticleMain::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
    }
}


class ProfileGallery(private val post : Post) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val image = viewHolder.itemView.gallery_imageView
        val ro = RequestOptions().placeholder(R.drawable.baseline_photo_white_48dp)
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













