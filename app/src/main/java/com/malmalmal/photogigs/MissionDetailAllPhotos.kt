package com.malmalmal.photogigs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.mission_detail_allphotos_row.view.*
import kotlinx.android.synthetic.main.mission_list.*

class MissionDetailAllPhotos : AppCompatActivity() {

    var missId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mission_list)

        val mid = intent.getStringExtra("MISSION")
        missId = mid

        val adapter = GroupAdapter<ViewHolder>()

        missionList_recyclerView.layoutManager = LinearLayoutManager(this)
        missionList_recyclerView.addItemDecoration(CustomItemDecoration(0,20,0,0))
        missionList_recyclerView.adapter = adapter

        fetchMissionPost()

    }

    fun fetchMissionPost() {
        val adapter = GroupAdapter<ViewHolder>()
        val missAllPost = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/mission/en-US/$missId/posts")
        missAllPost.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val post = it.getValue(Post::class.java)
                    if (post != null) {
                        adapter.add(MissionDetailAllPhotosRow(post))
                    }
                    missionList_recyclerView.adapter = adapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


}

class MissionDetailAllPhotosRow(val post : Post) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val imgAllrow = viewHolder.itemView.missionAll_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(imgAllrow.context).applyDefaultRequestOptions(ro).load(post.imageUrl).into(imgAllrow)
    }

    override fun getLayout(): Int {
        return R.layout.mission_detail_allphotos_row
    }
}





