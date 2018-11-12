package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.home.*
import kotlinx.android.synthetic.main.home_category_row.view.*

class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(ArticleRow())
        adapter.add(PostsRow())
        adapter.add(ProfileRow())

        home_recyclerView.layoutManager = LinearLayoutManager(this)
        home_recyclerView.adapter = adapter

    }

}

class ArticleRow : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.home_category_button.text = "article"

        viewHolder.itemView.home_category_button.setOnClickListener {
            val intent = Intent(it.context, ArticleMain::class.java)

            it.context.startActivity(intent)
        }
    }

    override fun getLayout(): Int {

        return R.layout.home_category_row
    }
}

class PostsRow : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.home_category_button.text = "posts"


    }

    override fun getLayout(): Int {

        return R.layout.home_category_row
    }
}

class ProfileRow : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.home_category_button.text = "profile"
    }

    override fun getLayout(): Int {

        return R.layout.home_category_row
    }
}