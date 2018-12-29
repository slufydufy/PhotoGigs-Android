package com.malmalmal.photogigs


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.post_main.*
import kotlinx.android.synthetic.main.post_main_row.view.*


class PostMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_main)

        //hide action bar
        val actionBar = supportActionBar
        actionBar!!.hide()

        //check if user login, else log out
        checkUserLogin()

        //show bottom bar
        showBottomBar()

        //create recyclerView
        val adapter = GroupAdapter<ViewHolder>()
        val ll = LinearLayoutManager(this)
        //reverse post order
        ll.reverseLayout = true
        ll.stackFromEnd = true
        post_main_recyclerView.layoutManager = ll
        post_main_recyclerView.adapter = adapter

        //fetch post
        fetchPost()

        //floating button intent
        floatingActionButtonPost.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
        }
    }

//    choosen image action
    private var selectedPhotoUri : Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val intent = Intent(this, PostAdd::class.java)
            intent.putExtra("IMAGE", data.data.toString())
            startActivity(intent)
        }
    }

    //back button action
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    //check user login otherwise to mainlogin
    private fun checkUserLogin() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, MainLogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }
    }

    //fetch post
    private fun fetchPost() {
        val adapter = GroupAdapter<ViewHolder>()
        val ref = FirebaseDatabase.getInstance().getReference("/posts").orderByChild("pd")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val post = it.getValue(Post::class.java)
                    if (post != null) {
                        adapter.add(PostRow(post))
                    }
                    post_main_recyclerView.adapter = adapter
                    post_progressBar.visibility = View.INVISIBLE
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    //show bottom bar view
    private fun showBottomBar() {
        post_bottomNavigationView.itemIconTintList = null
        post_bottomNavigationView.menu.getItem(0).setChecked(true)
        post_bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_article -> {
                    val intent = Intent(this, ArticleMain::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_profile -> {
                    val intent = Intent(this, ProfileMain::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
    }

    //reload post on loading page
    override fun onPostResume() {
        super.onPostResume()
        fetchPost()
    }
}

class PostRow(private val post : Post) : Item<ViewHolder>() {

    var likeTv : TextView? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        likeTv = viewHolder.itemView.like_counter_textView

        likeCounter()

        val imageUser = viewHolder.itemView.profile_pic_imageView

        //intent to user profile
        imageUser.setOnClickListener {
            val uuid = FirebaseAuth.getInstance().uid
            if (uuid == post.uuid) {
                val intent = Intent(it.context, ProfileMain::class.java)
                it.context.startActivity(intent)
            } else {
                val intent = Intent(it.context, UserMain::class.java)
                intent.putExtra("USER", post.uuid)
                it.context.startActivity(intent)
            }
        }
        viewHolder.itemView.userName_textView.setOnClickListener {
            val uuid = FirebaseAuth.getInstance().uid
            if (uuid == post.uuid) {
                val intent = Intent(it.context, ProfileMain::class.java)
                it.context.startActivity(intent)
            } else {
                val intent = Intent(it.context, UserMain::class.java)
                intent.putExtra("USER", post.uuid)
                it.context.startActivity(intent)
            }
        }


        //fetch user
        val ref = FirebaseDatabase.getInstance().getReference("/users/${post.uuid}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user: User? = p0.getValue(User::class.java)
                    viewHolder.itemView.userName_textView.text = user!!.name
                    val ro = RequestOptions().placeholder(R.drawable.baseline_person_white_24dp)
                    Glide.with(imageUser.context).applyDefaultRequestOptions(ro).load(user.userImageUrl).into(imageUser)
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("fetch user", "err : $p0")
            }
        })

        //get like status
        val uid = FirebaseAuth.getInstance().uid
        val refLikeCount = FirebaseDatabase.getInstance().getReference("/likes/${post.postId}").child(uid!!)
        refLikeCount.keepSynced(true)
        refLikeCount.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    viewHolder.itemView.like_imageView.setImageResource(R.drawable.hearts)
                } else {
                    viewHolder.itemView.like_imageView.setImageResource(R.drawable.baseline_favorite_border_white_24dp)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        //like counter
        viewHolder.itemView.like_imageView.setOnClickListener {
            var likeStatus = true
            val uuid = FirebaseAuth.getInstance().uid
            val refLike = FirebaseDatabase.getInstance().getReference("/likes/")
            refLike.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (likeStatus) {
                        if (p0.child(post.postId).hasChild(uuid!!)) {
                            viewHolder.itemView.like_imageView.setImageResource(R.drawable.baseline_favorite_border_white_24dp)
                            refLike.child(post.postId).child(uuid).removeValue()
                            likeCounter()
//                            updateCounter(false)
                            likeStatus = false
                        } else {
                            viewHolder.itemView.like_imageView.setImageResource(R.drawable.hearts)
                            refLike.child(post.postId).child(uuid).setValue("1")
                            likeCounter()
//                            updateCounter(true)
                            likeStatus = false
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }

        viewHolder.itemView.date_textView.text = post.pd
        viewHolder.itemView.story_textView.text = post.caption

        val postImage = viewHolder.itemView.post_main_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(postImage.context).applyDefaultRequestOptions(ro).load(post.imageUrl).into(postImage)

        postImage.setOnClickListener {
            val intent = Intent(it.context, ImageFullscreen::class.java)
            intent.putExtra("IMAGE", post.imageUrl)
            it.context.startActivity(intent)
        }

        //fetch message counter
        val refMessageCounter = FirebaseDatabase.getInstance().getReference("/comments/${post.postId}")
        refMessageCounter.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val total = p0.childrenCount.toString()
                val inTotal = total.toInt()
                val totals = "s"
                if (inTotal > 1) {

                    viewHolder.itemView.comment_counter_textView.text = total + " comment" + totals
                } else {
                    viewHolder.itemView.comment_counter_textView.text = total + " comment"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        viewHolder.itemView.comment_imageView.setOnClickListener {
            val intent = Intent(it.context, PostDetail::class.java)
            intent.putExtra("POST", post.postId)
            intent.putExtra("USER", post.uuid)
            it.context.startActivity(intent)
        }

        viewHolder.itemView.comment_counter_textView.setOnClickListener {
            val intent = Intent(it.context, PostDetail::class.java)
            intent.putExtra("POST", post.postId)
            intent.putExtra("USER", post.uuid)
            it.context.startActivity(intent)
        }

        viewHolder.itemView.detail_imageView.setOnClickListener {
            val intent = Intent(it.context, PostDetail::class.java)
            intent.putExtra("POST", post.postId)
            intent.putExtra("USER", post.uuid)
            it.context.startActivity(intent)
        }
    }

    fun likeCounter() {
        //like counter
        val refLikeCounter = FirebaseDatabase.getInstance().getReference("/likes/${post.postId}")
        refLikeCounter.keepSynced(true)
        refLikeCounter.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val total = p0.childrenCount.toString()
                val inTotal = total.toInt()
                val totals = "s"
                if (inTotal > 1) {

                            likeTv!!.text = total + " like" + totals
                } else {
                    likeTv!!.text = total + " like"
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
