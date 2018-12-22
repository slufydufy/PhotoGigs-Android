package com.malmalmal.photogigs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.home.*
import kotlinx.android.synthetic.main.home_category_row.view.*

class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        showBottomBar()


        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(ArticleRow())
        adapter.add(PostsRow())
        adapter.add(ProfileRow())

        home_recyclerView.layoutManager = LinearLayoutManager(this)
        home_recyclerView.adapter = adapter

        checkUserLogin()

        floatingActionButton.setOnClickListener {
            val intent = Intent(this, PostAdd::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    fun checkUserLogin() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, MainLogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }
    }

    fun showBottomBar() {
        home_bottomNavigationView.itemIconTintList = null
        home_bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
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

        viewHolder.itemView.home_category_button.setOnClickListener {
            val intent = Intent(it.context, PostMain::class.java)

            it.context.startActivity(intent)
        }
    }

    override fun getLayout(): Int {

        return R.layout.home_category_row
    }
}

class ProfileRow : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.home_category_button.text = "profile"

        viewHolder.itemView.home_category_button.setOnClickListener {
            val intent = Intent(it.context, ProfileEdit::class.java)

            it.context.startActivity(intent)
        }
    }

    override fun getLayout(): Int {

        return R.layout.home_category_row
    }
}