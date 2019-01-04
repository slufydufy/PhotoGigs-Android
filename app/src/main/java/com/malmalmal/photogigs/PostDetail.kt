package com.malmalmal.photogigs

import android.app.ActionBar.LayoutParams.*
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.add_info_detail.view.*
import kotlinx.android.synthetic.main.comment_row.view.*
import kotlinx.android.synthetic.main.post_detail.*
import kotlinx.android.synthetic.main.post_main_row.view.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class PostDetail : AppCompatActivity() {

    var dpid : String? = null
    var uuids : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_detail)

        //enabled back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val pid = intent.getStringExtra("POST")
        val uid = intent.getStringExtra("USER")
        dpid = pid
        uuids = uid

        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(PostDetailRow(pid,uid))
        adapter.add(AddInfoDetail(pid))
        adapter.add(PostCommentTitle())

        recyclerView_post_detail.layoutManager = LinearLayoutManager(this)
        recyclerView_post_detail.adapter = adapter

        fetchComment(pid,uid)

        comment_send_button.setOnClickListener {
            sendComment(pid)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    //enable option button action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        val uid = FirebaseAuth.getInstance().uid
        if (uuids == uid) {
            return super.onCreateOptionsMenu(menu)
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.edit_menu -> {
                val intent = Intent(this, PostDetailEdit::class.java)
                intent.putExtra("PID", dpid)
                startActivity(intent)
            }
            R.id.delete_menu -> {
                deletePost(dpid!!)
            }
        }


        return super.onOptionsItemSelected(item)
    }

    //delete post Dialog
    private fun deletePost(dpid: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hapus Post")
        builder.setMessage("Hapus post ini ?")
        builder.setCancelable(true)
        builder.setPositiveButton(
                "Ya"
        ) {dialog, which ->
            val refPost = FirebaseDatabase.getInstance().getReference("/posts/")
            val refImage = FirebaseStorage.getInstance()
            val refaddInfo = FirebaseDatabase.getInstance().getReference("/addInfo/")
            val refLike = FirebaseDatabase.getInstance().getReference("/likes/")
            val refComment = FirebaseDatabase.getInstance().getReference("/comments/")
            val refPostDel = FirebaseDatabase.getInstance().getReference("/posts/$dpid")

            refPostDel.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val post = p0.getValue(Post::class.java)
                    val posturl = post!!.imageUrl
                    refImage.getReferenceFromUrl(posturl).delete().addOnSuccessListener {
                        refPost.child(dpid).removeValue()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
            refaddInfo.child(dpid).removeValue()
            refLike.child(dpid).removeValue()
            refComment.child(dpid).removeValue()
            val intent = Intent(this, ProfileMain::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        builder.setNeutralButton(
                "Tidak"
        ) {dialog, which ->
            return@setNeutralButton
        }
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

    private fun fetchComment(pid : String, uid: String) {
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(PostDetailRow(pid,uid))
        adapter.add(AddInfoDetail(pid))
        adapter.add(PostCommentTitle())
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


    private fun sendComment(pid : String) {

        if (commentAdd_text.text.isEmpty()) {
            Toast.makeText(this, "Masukkan komentar anda", Toast.LENGTH_SHORT).show()
            return
        }
        val uuid = FirebaseAuth.getInstance().uid
        val commentId = UUID.randomUUID().toString()
        val sdf = SimpleDateFormat("MMM dd yyyy")
        val timeStamp = Timestamp(System.currentTimeMillis())
        val date = sdf.format(timeStamp)
        val commentText = commentAdd_text.text.toString()
        val postRef = FirebaseDatabase.getInstance().getReference("/comments/$pid/$commentId")
        val comment = Comment(uuid!!,commentText,date)
        postRef.setValue(comment)
            .addOnSuccessListener {
                Log.d("add comment", "suksess $it")
                fetchComment(pid,uuid)
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm.isAcceptingText) imm.hideSoftInputFromWindow(this.currentFocus.windowToken, 0)
                commentAdd_text.text.clear()
            }

            .addOnFailureListener {
                Log.d("add post", "error $it")
            }
    }
}

class PostDetailRow(pid : String, uid : String) : Item<ViewHolder>() {

    private val postId = pid
    private val userId = uid
    var likeTv : TextView? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {

        likeTv = viewHolder.itemView.like_counter_textView

        likeCounter()

        //hide comment image and detail image
        viewHolder.itemView.comment_imageView.visibility = View.INVISIBLE
        viewHolder.itemView.detail_imageView.visibility = View.INVISIBLE

        //fetch user
        val refUser = FirebaseDatabase.getInstance().getReference("/users/$userId")
        refUser.addListenerForSingleValueEvent(object: ValueEventListener {
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

        //fetch post
        val refPost = FirebaseDatabase.getInstance().getReference("/posts/$postId")
        refPost.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val post = p0.getValue(Post::class.java)
                        viewHolder.itemView.date_textView.text = post!!.pd
                        viewHolder.itemView.story_textView.text = post.caption

                        val image =  viewHolder.itemView.post_main_imageView
                        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
                        Glide.with(image.context).applyDefaultRequestOptions(ro).load(post.imageUrl).into(image)

                        image.setOnClickListener {
                            val intent = Intent(it.context, ImageFullscreen::class.java)
                            intent.putExtra("IMAGE", post.imageUrl)
                            it.context.startActivity(intent)
                        }
                    }

                }
                override fun onCancelled(p0: DatabaseError) {
                }
            })

        //get like status
        val uid = FirebaseAuth.getInstance().uid
        val refLikeCount = FirebaseDatabase.getInstance().getReference("/likes/$postId").child(uid!!)
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
                        if (p0.child(postId).hasChild(uuid!!)) {
                            viewHolder.itemView.like_imageView.setImageResource(R.drawable.baseline_favorite_border_white_24dp)
                            refLike.child(postId).child(uuid).removeValue()
                            likeCounter()
//                            updateCounter(false)
                            likeStatus = false
                        } else {
                            viewHolder.itemView.like_imageView.setImageResource(R.drawable.hearts)
                            refLike.child(postId).child(uuid).setValue("1")
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

        //fetch comment counter
        val refMessageCounter = FirebaseDatabase.getInstance().getReference("/comments/$postId")
        refMessageCounter.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val total = p0.childrenCount.toString()
                viewHolder.itemView.comment_counter_textView.text = total + " comment"
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    fun likeCounter() {
        //fetch like counter
        val refLikeCounter = FirebaseDatabase.getInstance().getReference("/likes/$postId")
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

class AddInfoDetail(val pid : String) : Item<ViewHolder>() {
    override fun bind(v: ViewHolder, p1: Int) {

        val c = v.itemView.addInfo_cons
        c.layoutParams.height = 0
        var isExpand = false
        v.itemView.addInfo_textView.setOnClickListener {
            if (!isExpand) {
                c.requestLayout()
                val h = WRAP_CONTENT
                c.layoutParams.height = h
                isExpand = true
                v.itemView.addInfo_imageView.setImageResource(R.drawable.baseline_keyboard_arrow_up_white_24dp)
            } else {
                c.requestLayout()
                c.layoutParams.height = 0
                isExpand = false
                v.itemView.addInfo_imageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_white_24dp)
            }
        }
        v.itemView.addInfo_imageView.setOnClickListener {

            if (!isExpand) {
                c.requestLayout()
                val h = WRAP_CONTENT
                c.layoutParams.height = h
                isExpand = true
                v.itemView.addInfo_imageView.setImageResource(R.drawable.baseline_keyboard_arrow_up_white_24dp)
            } else {
                c.requestLayout()
                c.layoutParams.height = 0
                isExpand = false
                v.itemView.addInfo_imageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_white_24dp)
            }
        }

        //fetch add Info
        val refAddInfo = FirebaseDatabase.getInstance().getReference("/addInfo/$pid")
        refAddInfo.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val addInfo = p0.getValue(AddInfo::class.java)
                    v.itemView.kamera_textView.text = addInfo!!.kamera
                    v.itemView.lensa_textView.text = addInfo.lensa
                    v.itemView.lokasi_textView.text = addInfo.lokasi
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.add_info_detail
    }
}

//show comment title
class PostCommentTitle : Item<ViewHolder>() {
    override fun bind(p0: ViewHolder, p1: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.post_detail_comment_title
    }
}

class PostCommentRow(private val comment : Comment) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        //fetch user
        val refUser = FirebaseDatabase.getInstance().getReference("/users/${comment.uid}")
        refUser.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user: User? = p0.getValue(User::class.java)
                    viewHolder.itemView.comment_username_textView.text = user!!.name
                    val imageUser = viewHolder.itemView.comment_profile_imageView
                    val ro = RequestOptions().placeholder(R.drawable.baseline_person_white_24dp)
                    Glide.with(imageUser.context).applyDefaultRequestOptions(ro).load(user.userImageUrl).into(imageUser)
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("fetch user", "err : $p0")
            }
        })

        //set comment and date
        viewHolder.itemView.comment_textView.text = comment.comment
        viewHolder.itemView.comment_date_textView.text = comment.pd
    }

    override fun getLayout(): Int {
        return R.layout.comment_row
    }
}


