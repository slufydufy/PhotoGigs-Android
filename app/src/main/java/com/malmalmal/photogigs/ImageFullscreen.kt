package com.malmalmal.photogigs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.image_fullscreen.*

class ImageFullscreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_fullscreen)

        //enabled back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val imageUrl = intent.getStringExtra("IMAGE")

        Glide.with(this).load(imageUrl).into(full_screen_image)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}