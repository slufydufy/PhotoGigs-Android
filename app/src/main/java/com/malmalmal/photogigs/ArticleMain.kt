package com.malmalmal.photogigs

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.article_main_row.view.*
import kotlinx.android.synthetic.main.home.*

class ArticleMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = GroupAdapter<ViewHolder>()

        article_card_recyclerView.layoutManager = LinearLayoutManager(this)
        article_card_recyclerView.adapter = adapter

        fetchArticle()

        floatingActionButtonArticle.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data

            val intent = Intent(this, PostAdd::class.java)
            intent.putExtra("URIDRAW", intent)
            startActivity(intent)


        }


    }

    private fun fetchArticle() {

        val adapter = GroupAdapter<ViewHolder>()
        val ref = FirebaseDatabase.getInstance().getReference("/flamelink/environments/production/content/artikel/en-US")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d("FETCH ARTICLES", it.toString())
                    val article = it.getValue(Article::class.java)
                    if (article != null) {
                        adapter.add(ImageRow(article))
                    }
                }
                article_card_recyclerView.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }
}

class ImageRow(val article : Article) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val image = viewHolder.itemView.article_card_imageView
        Picasso.get().load(article.urlGambar).into(image)
        viewHolder.itemView.title_card_textView.text = article.judul
        viewHolder.itemView.subTitle_textView.text = article.deskripsi

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(it.context, ArticleDetail::class.java)
            intent.putExtra("TITLE", article.judul)
            intent.putExtra("SUBTITLE", article.deskripsi)
            intent.putExtra("IMAGE", article.urlGambar)
            intent.putExtra("CONTENT", article.content)

            it.context.startActivity(intent)
        }


    }

    override fun getLayout(): Int {

        return  R.layout.article_main_row
    }
}

class Article(val judul : String, val deskripsi : String, val urlGambar : String, val content : String) {

    constructor() : this("", "","", "")
}
