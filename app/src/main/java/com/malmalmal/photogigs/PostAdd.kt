package com.malmalmal.photogigs


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.post_add.*

class PostAdd : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_add)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        post_add_imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
//            val bitmapDraw = BitmapDrawable(bitmap)
            post_add_imageView.setImageBitmap(bitmap)
        }


    }

}