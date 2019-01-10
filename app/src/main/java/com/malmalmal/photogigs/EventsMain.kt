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
import kotlinx.android.synthetic.main.event_main.*
import kotlinx.android.synthetic.main.event_main_row.view.*

class EventsMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_main)

        showBottomBar()

        val adapter = GroupAdapter<ViewHolder>()


        newsMain_recyclerView.layoutManager = LinearLayoutManager(this)
        newsMain_recyclerView.addItemDecoration(CustomItemDecoration(0,20,0,0))
        newsMain_recyclerView.adapter = adapter

        fetchEvent()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent(this, PostMain::class.java)
            startActivity(intent)
            return true;
        }
        return super.onKeyDown(keyCode, event)
    }

    fun fetchEvent() {
        val adapter = GroupAdapter<ViewHolder>()
        val refEvent = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/eventArtikel/en-US")
            refEvent.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach {
                        val event = it.getValue(Event::class.java)
                        if (event != null) {
                            adapter.add(EventsMainRow(event))
                        }
                        newsMain_recyclerView.adapter = adapter

                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
    }


    //show bottom bar view
    private fun showBottomBar() {
        newsMain_bottomNavigation.itemIconTintList = null
        newsMain_bottomNavigation.menu.getItem(1).setChecked(true)
        newsMain_bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_home -> {
                    val intent = Intent(this, PostMain::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_article -> {
                    val intent = Intent(this, ArticleMain::class.java)
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

class EventsMainRow(val event : Event) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        //set main image
        val mainImg = viewHolder.itemView.main_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(mainImg.context).applyDefaultRequestOptions(ro).load(event.img).into(mainImg)

        viewHolder.itemView.pdate_textView.text = event.sdate
        viewHolder.itemView.eventTitle_textView.text = event.etitle

        //create pp recyclerView
        val adapter = GroupAdapter<ViewHolder>()
        val rv = viewHolder.itemView.newsMain_row_pp_recyclerView
        rv.layoutManager = LinearLayoutManager(rv.context, LinearLayoutManager.HORIZONTAL, false)
        rv.addItemDecoration(CustomItemDecoration(0,0,8,8))
        adapter.add(PpOnly())
        adapter.add(PpOnly())
        adapter.add(PpOnly())
        rv.adapter = adapter
        val img = viewHolder.itemView.main_imageView

    }

    override fun getLayout(): Int {
        return R.layout.event_main_row
    }
}

class PpOnly : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.pp_only
    }
}