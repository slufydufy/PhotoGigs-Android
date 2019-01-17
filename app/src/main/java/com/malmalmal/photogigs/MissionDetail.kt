package com.malmalmal.photogigs

import android.content.Intent
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
import kotlinx.android.synthetic.main.mission_detail.*
import kotlinx.android.synthetic.main.mission_detail_desc.view.*
import kotlinx.android.synthetic.main.mission_detail_prize.view.*
import kotlinx.android.synthetic.main.mission_detail_top.view.*

class MissionDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mission_detail)

        val mid = intent.getStringExtra("MISSION")

        val adapter = GroupAdapter<ViewHolder>()

        val refMission = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/mission/en-US/$mid")
        refMission.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val mission = p0.getValue(Mission::class.java)
                    adapter.add(MissionDetailTop(mission!!))
                    adapter.add(MissionDetailDesc(mission))
                    adapter.add(MissionDetailPrize(mission))
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        missionDetail_recyclerView.layoutManager = LinearLayoutManager(this)
        missionDetail_recyclerView.adapter = adapter

        mission_fab.setOnClickListener {
            val intent = Intent(this, MissionList::class.java)
            it.context.startActivity(intent)
        }
    }



}

class MissionDetailTop(val mission : Mission) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        val img = viewHolder.itemView.banner_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(img.context).applyDefaultRequestOptions(ro).load(mission.img).into(img)

        val imgPP = viewHolder.itemView.orgPP_imageView
        val roPP = RequestOptions().placeholder(R.drawable.baseline_person_white_24dp)
        Glide.with(imgPP.context).applyDefaultRequestOptions(roPP).load(mission.imgPP).into(imgPP)

        viewHolder.itemView.org_textView.text = mission.org
        viewHolder.itemView.title_textView.text = mission.mistitle
        viewHolder.itemView.status_textView.text = "Active"
    }

    override fun getLayout(): Int {
        return R.layout.mission_detail_top
    }
}

class MissionDetailDesc(val mission : Mission) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.desc_textView.text = mission.desc
    }

    override fun getLayout(): Int {
        return R.layout.mission_detail_desc
    }
}

class MissionDetailPrize(val mission : Mission) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.prize_textView.text = mission.prize
    }

    override fun getLayout(): Int {
        return R.layout.mission_detail_prize
    }
}