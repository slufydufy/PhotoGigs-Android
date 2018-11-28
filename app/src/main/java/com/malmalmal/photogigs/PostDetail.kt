package com.malmalmal.photogigs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.post_detail.*

class PostDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.post_detail)

        val imageUrl = intent.getStringExtra("IMAGE")
        Glide.with(this).load(imageUrl).into(detail_post_imageView)




    }
}