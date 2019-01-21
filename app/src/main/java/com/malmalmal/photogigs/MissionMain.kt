package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.mission_main.*
import kotlinx.android.synthetic.main.mission_main_row.view.*


class MissionMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mission_main)

        showBottomBar()

        val adapter = GroupAdapter<ViewHolder>()

        missionMain_recyclerView.layoutManager = LinearLayoutManager(this)
        missionMain_recyclerView.addItemDecoration(CustomItemDecoration(0,20,0,0))
        missionMain_recyclerView.adapter = adapter

        fetchMission()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent(this, PostMain::class.java)
            startActivity(intent)
            return true;
        }
        return super.onKeyDown(keyCode, event)
    }

    fun fetchMission() {
        val adapter = GroupAdapter<ViewHolder>()
        val refMission = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/mission/en-US").orderByChild("sdate")
        refMission.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        val mission = it.getValue(Mission::class.java)
                        if (mission != null) {
                            adapter.add(MissionMainRow(mission))
                        }
                        missionMain_recyclerView.adapter = adapter
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    //show bottom bar view
    private fun showBottomBar() {
        missionMain_bottomBar.itemIconTintList = null
        missionMain_bottomBar.menu.getItem(2).setChecked(true)
        missionMain_bottomBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_home -> {
                    val intent = Intent(this, PostMain::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_news -> {
                    val intent = Intent(this, EventsMain::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_profile -> {
                    val intent = Intent(this, ProfileMain::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
    }


}


class MissionMainRow(val mission : Mission) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.title_textView.text = mission.mistitle
        viewHolder.itemView.sub_textView.text = mission.brief
        viewHolder.itemView.status_textView.text = mission.mstatus
        viewHolder.itemView.organizer_textView.text = mission.org

        val img = viewHolder.itemView.missionBanner_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(img.context).applyDefaultRequestOptions(ro).load(mission.img).into(img)

        val imgPP = viewHolder.itemView.pp_imageView
        val roPP = RequestOptions().placeholder(R.drawable.baseline_person_white_24dp)
        Glide.with(imgPP.context).applyDefaultRequestOptions(roPP).load(mission.imgPP).into(imgPP)

        val card = viewHolder.itemView.missionMain_card
        card.setOnClickListener {
            val intent = Intent(card.context, MissionDetail::class.java)
            intent.putExtra("MISSION", mission.id.toString())
            it.context.startActivity(intent)
        }
    }

    override fun getLayout(): Int {
        return R.layout.mission_main_row
    }
}






