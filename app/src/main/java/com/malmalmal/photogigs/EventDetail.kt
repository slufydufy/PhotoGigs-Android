package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.article_detail_content_row.view.*
import kotlinx.android.synthetic.main.event_detail.*
import kotlinx.android.synthetic.main.event_detail_top.view.*
import kotlinx.android.synthetic.main.event_main_row.view.*
import java.text.SimpleDateFormat

class EventDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_detail)

        val eid = intent.getStringExtra("EVENT")

        val adapter = GroupAdapter<ViewHolder>()

        val refEvent = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/eventArtikel/en-US/$eid")
        refEvent.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val event = p0.getValue(Eventm::class.java)
                    adapter.add(EventDetailTop(event!!))
                    adapter.add(EventDetailInfo(event))
                    adapter.add(EventDetailContent(event))
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        eventDetail_recyclerView.layoutManager = LinearLayoutManager(this)
        eventDetail_recyclerView.addItemDecoration(CustomItemDecoration(0,20,0,0))
        eventDetail_recyclerView.adapter = adapter

    }
}

class EventDetailTop(val eventm : Eventm) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val img = viewHolder.itemView.eventDetailTop_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(img.context).applyDefaultRequestOptions(ro).load(eventm.img).into(img)

        img.setOnClickListener {
            val intent = Intent(it.context, ImageFullscreen::class.java)
            intent.putExtra("IMAGE", eventm.img)
            it.context.startActivity(intent)
        }
    }

    override fun getLayout(): Int {
        return R.layout.event_detail_top
    }
}

class EventDetailInfo(val eventm : Eventm) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

//        viewHolder.itemView.banner_imageView.visibility = View.INVISIBLE

        //parse and format post date
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val date = sdf.parse(eventm.sdate)
        val dateform = SimpleDateFormat("dd")
        val monthform = SimpleDateFormat("MMMM")
        val day = dateform.format(date)
        val month = monthform.format(date)

        viewHolder.itemView.pdate_textView.text = day
        viewHolder.itemView.month_textView.text = month
        viewHolder.itemView.eventTitle_textView.text = eventm.etitle
        viewHolder.itemView.lokasi_textView.text = eventm.lok
    }

    override fun getLayout(): Int {
        return R.layout.event_main_row
    }
}

class EventDetailContent(val eventm : Eventm) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView7.setText(Html.fromHtml(eventm.contents))
    }

    override fun getLayout(): Int {
        return R.layout.article_detail_content_row
    }
}