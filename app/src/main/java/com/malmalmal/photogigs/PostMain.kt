package com.malmalmal.photogigs


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.post_main.*
import kotlinx.android.synthetic.main.post_main_row.view.*
import java.math.BigDecimal


class PostMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_main)

        checkUserLogin()
        showBottomBar()

        val adapter = GroupAdapter<ViewHolder>()


        post_main_recyclerView.layoutManager = LinearLayoutManager(this)
        post_main_recyclerView.adapter = adapter

        fetchPost()

        floatingActionButtonPost.setOnClickListener {
            val intent = Intent(this, PostAdd::class.java)
            startActivity(intent)
        }
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
//        post_bottomNavigationView.menu.getItem(0).setChecked(true)
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
}

class PostRow(private val post : Post) : Item<ViewHolder>() {

    val refLikeCount = FirebaseDatabase.getInstance().getReference("/posts/${post.postId}").child("likeCount")

    override fun bind(viewHolder: ViewHolder, position: Int) {

        //fetch user
        val ref = FirebaseDatabase.getInstance().getReference("/users/${post.uuid}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user: User? = p0.getValue(User::class.java)
                    viewHolder.itemView.userName_textView.text = user!!.name
                    val imageUser = viewHolder.itemView.profile_pic_imageView
                    val ro = RequestOptions().placeholder(R.drawable.baseline_person_white_24dp)
                    Glide.with(imageUser.context).applyDefaultRequestOptions(ro).load(user.userImageUrl).into(imageUser)
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("fetch user", "err : $p0")
            }
        })

        //like counter
        viewHolder.itemView.like_imageView.setOnClickListener {


            var likeStatus: Boolean = true

            val uid = FirebaseAuth.getInstance().uid

            refLikeCount.keepSynced(true)
            val refLike = FirebaseDatabase.getInstance().getReference("/likes/")
            refLike.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (likeStatus) {
                        if (p0.child(post.postId).hasChild(uid!!)) {
                            refLike.child(post.postId).child(uid).removeValue()
                            updateCounter(false)
                            likeStatus = false
                        } else {
                            refLike.child(post.postId).child(uid).setValue(uid)
                            updateCounter(true)
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
                viewHolder.itemView.comment_counter_textView.text = total + " comment"
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
    }

    fun updateCounter(inc : Boolean) {

        refLikeCount.runTransaction(object : Transaction.Handler {

            override fun doTransaction(p0: MutableData): Transaction.Result {
                if (p0.getValue() != null) {
                    val valueLike = p0.getValue(BigDecimal::class.java)
                    if (inc) {
                        valueLike!!.inc()
                    }   else {
                        valueLike!!.inc()
                    }
                    p0.value = valueLike
                }
                return Transaction.success(p0)
            }

            override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {

            }
//            Transaction.Result doTransaction(MutableData mutableData) {
//                if (mutableData.getValue() != null) {
//                    int value = mutableData.getValue(Integer.class);
//                    if(increment) {
//                        value++;
//                    } else {
//                        value--;
//                    }
//                    mutableData.setValue(value);
//                }
//                return Transaction.success(mutableData);
//            }
//
//            @Override
//            public void onComplete(DatabaseError databaseError, boolean b,
//                DataSnapshot dataSnapshot) {
//                // Transaction completed
//                Log.d(TAG, "likeTransaction:onComplete:" + databaseError);
//            }
//        });
        })
    }



    override fun getLayout(): Int {

        return R.layout.post_main_row
    }
}
