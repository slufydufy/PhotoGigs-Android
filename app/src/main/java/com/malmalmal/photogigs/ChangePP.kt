package com.malmalmal.photogigs

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.media.ExifInterface
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.change_pp.*
import java.io.*


class ChangePP : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_pp)


        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)

    }


    private var selectUri : Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectUri = data.data
//            val intent = Intent(this, ProfileEdit::class.java)
//            intent.putExtra("IMAGEURI", data.data.toString())
//            startActivity(intent)
//            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
//            Log.d("IMAGE","asli: ${bitmap.byteCount}")
//            val baos = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos)
//            val dataImg = baos.toByteArray()

            
            val uriPath = data.data.path
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
            val tempFilePath = getTempFilePath(uriPath)
            try {
                val out = FileOutputStream(tempFilePath)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 25, out)
                copyExif(uriPath, tempFilePath)
            } catch (ex : FileNotFoundException) {
                ex.printStackTrace()
            }
            val tempFile = File(tempFilePath)
            val byteFile = readFile(tempFile)
            val fis = ByteArrayInputStream(byteFile)
            val deleted = tempFile.delete()

//  ____          Glide.with(pp_imageView.context).load(byteFile).into(pp_imageView)


//        val inp = contentResolver.openInputStream(selectUri)
//            val exif = ExifInterface(inp)
//            Log.d("URI","asli: $exif")
//            Log.d("URI","asli: $exif")



            val filename = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/users/$filename").child("userImageUrl")
            val refImg = FirebaseStorage.getInstance().getReference("/profileImage/$filename")
//            refImg.putBytes(byteFile)
//                .addOnSuccessListener {
//                    //get image url from firebase
//                    refImg.downloadUrl.addOnSuccessListener {
////                        saveUserInfoToFirebase(it.toString())
//                        ref.setValue(it.toString())
//
//                        val intent = Intent(this, ProfileMain::class.java)
//                        startActivity(intent)
//                    }
//                }
        }
    }


//    fun copyExif(oldPath : String, newPath : String) {
//        val oldExif = ExifInterface(oldPath)
//        val exifOrientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION)
//
//        if (exifOrientation != null) {
//            val newExif = ExifInterface(newPath)
//            newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation)
//            newExif.saveAttributes()
//        }
//    }

    private fun getTempFilePath(filename: String): String {
        var filename = filename
        val temp = "_temp"
        val dot = filename.lastIndexOf(".")
        val ext = filename.substring(dot + 1)

        if (dot == -1 || !ext.matches("\\w+".toRegex())) {
            filename += temp
        } else {
            filename = filename.substring(0, dot) + temp + "." + ext
        }

        return filename
    }

    fun copyExif(originalPath: String, newPath: String) {

        val attributes = arrayOf(
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.TAG_SUBSEC_TIME,
            ExifInterface.TAG_WHITE_BALANCE
        )

        val oldExif = ExifInterface(originalPath)
        val newExif = ExifInterface(newPath)

        if (attributes.size > 0) {
            for (i in attributes.indices) {
                val value = oldExif.getAttribute(attributes[i])
                if (value != null)
                    newExif.setAttribute(attributes[i], value)
            }
            newExif.saveAttributes()
        }
    }

    fun readFile(file: File): ByteArray {
        // Open file
        val f = RandomAccessFile(file, "r")
        try {
            // Get and check length
            val longlength = f.length()
            val length = longlength.toInt()
            if (length.toLong() != longlength)
                throw IOException("File size >= 2 GB")
            // Read file and return data
            val data = ByteArray(length)
            f.readFully(data)
            return data
        } finally {
            f.close()
        }
    }

}


