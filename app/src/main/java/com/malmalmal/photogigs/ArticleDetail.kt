package com.malmalmal.photogigs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.article_detail.*
import kotlinx.android.synthetic.main.article_detail_content_row.view.*
import kotlinx.android.synthetic.main.article_detail_image_row.view.*

class ArticleDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.article_detail)
        supportActionBar?.title = "Article Detail"

        val title = intent.getStringExtra("TITLE")
        val subtitle = intent.getStringExtra("SUBTITLE")
        val content = intent.getStringExtra("CONTENT")

        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(DetailImage(title, subtitle))
        adapter.add(DetailContent(content))

        articleDetail_recyclerView.layoutManager = LinearLayoutManager(this)
        articleDetail_recyclerView.adapter = adapter

    }

}

class DetailImage(val title : String, val subtitle : String) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.title_textView.text = title
        viewHolder.itemView.subtitle_textView.text = subtitle
    }

    override fun getLayout(): Int {

        return R.layout.article_detail_image_row
    }
}

class DetailContent(val content : String) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.content_textView.text = content
    }

    override fun getLayout(): Int {

        return R.layout.article_detail_content_row
    }
}
