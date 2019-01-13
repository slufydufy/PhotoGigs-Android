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
import java.text.SimpleDateFormat

class EventsMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_main)

        showBottomBar()

        val adapter = GroupAdapter<ViewHolder>()
        val ll = LinearLayoutManager(this)
        //reverse post order
        ll.reverseLayout = true
        ll.stackFromEnd = true
        newsMain_recyclerView.layoutManager = ll
        newsMain_recyclerView.addItemDecoration(CustomItemDecoration(0,5,0,0))
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
        val refEvent = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/eventArtikel/en-US").orderByChild("sdate")
            refEvent.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach {
                        val event = it.getValue(Eventm::class.java)
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
                    val intent = Intent(this, MissionMain::class.java)
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

class EventsMainRow(val eventm : Eventm) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        //parse and format post date
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val date = sdf.parse(eventm.sdate)
        val dateform = SimpleDateFormat("dd")
        val monthform = SimpleDateFormat("MMMM")
        val day = dateform.format(date)
        val month = monthform.format(date)

        //set value
        viewHolder.itemView.pdate_textView.text = day
        viewHolder.itemView.month_textView.text = month
        viewHolder.itemView.eventTitle_textView.text = eventm.etitle
        viewHolder.itemView.lokasi_textView.text = eventm.lok

//        val img = viewHolder.itemView.banner_imageView
//        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
//        Glide.with(img.context).applyDefaultRequestOptions(ro).load(eventm.img).into(img)

        val card = viewHolder.itemView.event_row_card
        card.setOnClickListener {
            val intent = Intent(card.context, EventDetail::class.java)
            intent.putExtra("EVENT", eventm.id.toString())
            it.context.startActivity(intent)
        }
    }

    override fun getLayout(): Int {
        return R.layout.event_main_row
    }
}

