package com.malmalmal.photogigs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.mission_main.*

class MissionMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mission_main)

        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(MissionMainRow())
        adapter.add(MissionMainRow())
        adapter.add(MissionMainRow())
        adapter.add(MissionMainRow())
        adapter.add(MissionMainRow())
        missionMain_recyclerView.layoutManager = LinearLayoutManager(this)
        missionMain_recyclerView.addItemDecoration(CustomItemDecoration(0,20,0,0))
        missionMain_recyclerView.adapter = adapter
    }



}


class MissionMainRow : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.mission_main_row
    }
}






