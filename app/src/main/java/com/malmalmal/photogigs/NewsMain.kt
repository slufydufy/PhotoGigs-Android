package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.news_main.*
import kotlinx.android.synthetic.main.news_main_row.view.*

class NewsMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_main)

        showBottomBar()

        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(NewsMainRow())
        adapter.add(NewsMainRow())

        newsMain_recyclerView.layoutManager = LinearLayoutManager(this)
        newsMain_recyclerView.adapter = adapter


    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent(this, PostMain::class.java)
            startActivity(intent)
            return true;
        }
        return super.onKeyDown(keyCode, event)
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

class NewsMainRow : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val adapter = GroupAdapter<ViewHolder>()
        val rv = viewHolder.itemView.newsMain_row_pp_recyclerView
        rv.layoutManager = LinearLayoutManager(rv.context, LinearLayoutManager.HORIZONTAL, false)
        rv.addItemDecoration(CustomItemDecoration(0,0,8,8))
        adapter.add(PpOnly())
        adapter.add(PpOnly())
        adapter.add(PpOnly())
        rv.adapter = adapter
    }

    override fun getLayout(): Int {
        return R.layout.news_main_row
    }
}

class PpOnly : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.pp_only
    }
}