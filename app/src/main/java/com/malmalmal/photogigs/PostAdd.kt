package com.malmalmal.photogigs


import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_add.*

class PostAdd : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_add)


        val uri = intent.getStringExtra("URIDRAW")
//        val uriuri = intent.
//
//        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
//        val bitmapDraw = BitmapDrawable(bitmap)



        Picasso.get().load(uri).into(post_add_imageView)


    }

}