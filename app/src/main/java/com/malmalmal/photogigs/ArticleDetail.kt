package com.malmalmal.photogigs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.article_detail.*
import kotlinx.android.synthetic.main.article_detail_content_row.view.*
import kotlinx.android.synthetic.main.article_detail_image_row.view.*
import kotlinx.android.synthetic.main.article_main_row.view.*

class ArticleDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.article_detail)


        val title = intent.getStringExtra("TITLE")
        val subtitle = intent.getStringExtra("SUBTITLE")
        val image = intent.getStringExtra("IMAGE")
        val content = intent.getStringExtra("CONTENT")
        supportActionBar?.title = title

        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(DetailImage(title, subtitle, image))
        adapter.add(DetailContent(content))

        articleDetail_recyclerView.layoutManager = LinearLayoutManager(this)
        articleDetail_recyclerView.adapter = adapter


    }

}

class DetailImage(val title : String, val subtitle : String, val image : String) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val imageView = viewHolder.itemView.article_detail_imageView
        Picasso.get().load(image).into(imageView)
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
