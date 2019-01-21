package com.malmalmal.photogigs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.mission_detail.*
import kotlinx.android.synthetic.main.mission_detail_desc.view.*
import kotlinx.android.synthetic.main.mission_detail_inswin.view.*
import kotlinx.android.synthetic.main.mission_detail_prize.view.*
import kotlinx.android.synthetic.main.mission_detail_top.view.*
import kotlinx.android.synthetic.main.profile_main_user_gallery.view.*

class MissionDetail : AppCompatActivity() {

    var isOpen = false
    var missId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mission_detail)

        newPost_textView.visibility = View.INVISIBLE
        oldPost_textView.visibility = View.INVISIBLE

        val mid = intent.getStringExtra("MISSION")
        missId = mid


        val adapter = GroupAdapter<ViewHolder>()

        val refMission = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/mission/en-US/$mid")
        refMission.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val mission = p0.getValue(Mission::class.java)
                    adapter.add(MissionDetailTop(mission!!))
                    adapter.add(MissionDetailDesc(mission))
                    adapter.add(MissionDetailPrize(mission))
                    adapter.add(MissionDetailInsWin(mission))

                    if (mission.inswin == "Completed" || mission.inswin == "Waiting for result") {
                        mission_fab.hide()
                        newPost_Fab.hide()
                        oldPost_Fab.hide()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        missionDetail_recyclerView.layoutManager = LinearLayoutManager(this)
        missionDetail_recyclerView.adapter = adapter

        mission_fab.setOnClickListener {
            if (!isOpen) {
                showFabMenu()
            } else {
                closeFabMenu()
            }
        }

        newPost_Fab.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        newPost_textView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        oldPost_Fab.setOnClickListener {
            val intent = Intent(this, MissionList::class.java)
            intent.putExtra("MISSION", mid)
            it.context.startActivity(intent)
        }

        oldPost_textView.setOnClickListener {
            val intent = Intent(this, MissionList::class.java)
            intent.putExtra("MISSION", mid)
            it.context.startActivity(intent)
        }

    }

    override fun onBackPressed() {
        if(!isOpen){
            super.onBackPressed()
        }else{
            closeFabMenu()
        }
    }

    //    choose image to post
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            val intent = Intent(this, MissionNewPost::class.java)
            intent.putExtra("IMAGE", data.data.toString())
            intent.putExtra("MISSION", missId)
            startActivity(intent)
        }
    }

    fun showFabMenu() {
        isOpen = true
        newPost_Fab.animate().translationY(-resources.getDimension((R.dimen.s55)))
        newPost_textView.animate().translationY(-resources.getDimension((R.dimen.s55)))
        oldPost_Fab.animate().translationY(-resources.getDimension((R.dimen.s105)))
        oldPost_textView.animate().translationY(-resources.getDimension((R.dimen.s105)))
        newPost_textView.visibility = View.VISIBLE
        oldPost_textView.visibility = View.VISIBLE
    }

    fun closeFabMenu() {
        isOpen = false
        newPost_Fab.animate().translationY(0f)
        newPost_textView.animate().translationY(0f)
        oldPost_Fab.animate().translationY(0f)
        oldPost_textView.animate().translationY(0f)
        newPost_textView.visibility = View.INVISIBLE
        oldPost_textView.visibility = View.INVISIBLE
    }

}

class MissionDetailTop(val mission : Mission) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        //fetch banner image
        val img = viewHolder.itemView.missDetImg_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(img.context).applyDefaultRequestOptions(ro).load(mission.img).into(img)
        //fetch pp image
        val imgPP = viewHolder.itemView.orgPP_imageView
        val roPP = RequestOptions().placeholder(R.drawable.baseline_person_white_24dp)
        Glide.with(imgPP.context).applyDefaultRequestOptions(roPP).load(mission.imgPP).into(imgPP)
        //fetch info
        viewHolder.itemView.org_textView.text = mission.org
        viewHolder.itemView.title_textView.text = mission.mistitle
        viewHolder.itemView.status_textView.text = mission.mstatus
        //fetch total photos submitted
        val missRef = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/mission/en-US/${mission.id}/posts")
        missRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val total = p0.childrenCount.toString()
                viewHolder.itemView.participant_textView.text = total + " Photos"
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        //view photos submitted
        val allText = viewHolder.itemView.participant_textView
        allText.setOnClickListener {
            val intent = Intent(allText.context, MissionDetailAllPhotos::class.java)
            it.context.startActivity(intent)
        }
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

class MissionDetailInsWin(val mission : Mission) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.insWin_textView.text = mission.inswin

        val adapter = GroupAdapter<ViewHolder>()
        val insWinRecycler = viewHolder.itemView.inswin_recyclerView
        insWinRecycler.layoutManager = LinearLayoutManager(insWinRecycler.context)
        var missRef = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/mission/en-US/${mission.id}/posts").limitToFirst(5)
        if (mission.inswin == "Winner" || mission.inswin == "Waiting for result") {
            missRef = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/mission/en-US/${mission.id}/winner")
        }
        missRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val post = it.getValue(Post::class.java)
                    if (post != null) {
                        adapter.add(MissionDetailInsWinGallery(post))
                    }
                    insWinRecycler.adapter = adapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    override fun getLayout(): Int {
        return R.layout.mission_detail_inswin
    }
}

class MissionDetailInsWinGallery(val post : Post) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val img = viewHolder.itemView.gallery_imageView
        val ro = RequestOptions().placeholder(R.drawable.placeholder1)
        Glide.with(img.context).applyDefaultRequestOptions(ro).load(post.imageUrl).into(img)
    }

    override fun getLayout(): Int {
        return R.layout.profile_main_user_gallery
    }
}