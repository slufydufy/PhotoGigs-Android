package com.malmalmal.photogigs

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.util.DataUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.profile_edit.*
import java.io.*
import java.net.URI


@Suppress("DEPRECATION")
class ProfileEdit : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_edit)
        profile_progressBar.visibility = View.INVISIBLE

        //pick profile image
        profile_imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        //sign out btn action
        signout_button.setOnClickListener {
            signOutDialog()
        }

        fetchUser()
    }

    //sign Out Dialog
    private fun signOutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sign Out")
        builder.setMessage("Apakah anda ingin keluar ?")
        builder.setCancelable(true)
        builder.setPositiveButton(
            "Ya"
        ) { dialog, which ->
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainLogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        builder.setNeutralButton(
            "Tidak"
        ) {dialog, which ->
            return@setNeutralButton
        }

        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

    //fetch user info
    var profileImageData : String? = ""
    var aboutInfo : String? = ""
    var selectedPhoto : Bitmap? = null

    private fun fetchUser() {
        val uuid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    name_textView.setText(user!!.name)
                    aboutInfo = user.about
                    if (user.about.isEmpty()) {
                        about_textView.setText("")
                    } else {about_textView.setText(user.about)}
                    profileImageData = user.userImageUrl
                    val ro = RequestOptions().placeholder(R.drawable.user123)
                    Glide.with(profile_imageView.context).applyDefaultRequestOptions(ro).load(profileImageData).into(profile_imageView)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("fetch user", "err : $p0")
            }

        })
    }

    //set picked image to image profile
    private var selectedPhotoUri : Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data

            val file = File(data.data.path)
            decode(file)
            val iStream = contentResolver.openInputStream(data.data)
            var selectedImage = BitmapFactory.decodeStream(iStream)
            selectedImage = getResizedBitmap(selectedImage, 150)
//
//            val okokok = data.data
//
//            val column = arrayOf(MediaStore.Images.Media.ORIENTATION)
//            val cursor = this.contentResolver.query(okokok, column, null, null)
//            if (cursor != null) {
//                cursor.moveToFirst()
//                val colIn = cursor.getColumnIndex((column[0]))
//                val orien = cursor.getInt(colIn)
//                Log.d("EXIF", "nih: $orien")
//                Log.d("EXIF", "nih: $orien")

            val oldEx = ExifInterface(data.data.path)
            val exData = oldEx.getAttribute(ExifInterface.TAG_ORIENTATION)
            Log.d("EXIFDATA", "nih: $exData")
            Log.d("EXIFDATA", "nih: $exData")
            Glide.with(view.context).load(selectedImage).into(profile_imageView)

        }


//            Glide.with(view.context).load(selectedImage).into(profile_imageView)




//        }
    }

    fun decode(file : File) : Bitmap? {
        try {
            val bitmapOption = BitmapFactory.Options()
            bitmapOption.inJustDecodeBounds = true
            BitmapFactory.decodeStream(FileInputStream(file), null, bitmapOption)
            val displaymetric = applicationContext.resources.displayMetrics

            val scaleW = bitmapOption.outWidth / displaymetric.widthPixels
            val scaleH = bitmapOption.outHeight / displaymetric.heightPixels
            val scale = Math.max(scaleW, scaleH)

            val bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inSampleSize = scale
            selectedPhoto = BitmapFactory.decodeStream(FileInputStream(file), null, bitmapOptions)
            try {
                val exif = ExifInterface(file.absolutePath)
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                if (rotation > 0) {
                    selectedPhoto = convertBitmapToCorrectOrientation(selectedPhoto!!, rotation)
                }
                Glide.with(view.context).load(selectedPhoto).into(profile_imageView)
            } catch (e : IOException) {
                e.printStackTrace()
            }
            return selectedPhoto!!
        } catch (filenotfound : FileNotFoundException) {
            filenotfound.printStackTrace()
        }
        return null
    }

    fun convertBitmapToCorrectOrientation(photo : Bitmap, rotation : Int ) : Bitmap {
        var width = photo.width
        var height = photo.height

        var matrix = Matrix()
        matrix.preRotate(rotation.toFloat())

        return Bitmap.createBitmap(photo, 0, 0, width, height, matrix, false)
    }

    //enable save button action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //save menu button action
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.menu_save_profile -> {
                if (selectedPhotoUri == null) {

                    saveUserInfoToFirebase(profileImageData!!)
                } else {

                    profile_progressBar.visibility = View.VISIBLE
//                    val filename = UUID.randomUUID().toString()
//                    val iStream = contentResolver.openInputStream(i)
//                    var selectedImage = BitmapFactory.decodeStream(iStream)
//                    selectedImage = getResizedBitmap(selectedImage, 150)




                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
                    Log.d("IMAGE","asli: ${bitmap.byteCount}")
                    val baos = ByteArrayOutputStream()
//
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos)
                    val data = baos.toByteArray()
                    //val decode = BitmapFactory.decodeStream(ByteArrayInputStream(data))
 //                   Log.d("IMAGE","kompres: ${decode.byteCount}")
////                    val e = ExifInterface()

                    val filename = FirebaseAuth.getInstance().uid
                    val ref = FirebaseStorage.getInstance().getReference("/profileImage/$filename")

                    ref.putFile(selectedPhotoUri!!)
                        .addOnSuccessListener {

                            //get image url from firebase
                            ref.downloadUrl.addOnSuccessListener {
                                saveUserInfoToFirebase(it.toString())
                            }
                        }

                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //save change to firebase
    private fun saveUserInfoToFirebase(piu : String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        if (name_textView.text.isEmpty()) {
            Toast.makeText(this, "username, harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (about_textView.text.isEmpty()) {
            aboutInfo = ""
        } else {
            aboutInfo = about_textView.text.toString()
        }

        val user = User(uid, name_textView.text.toString(), piu, aboutInfo!!)
        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, ProfileMain::class.java)
                startActivity(intent)
                profile_progressBar.visibility = View.INVISIBLE
            }

            .addOnFailureListener {
                Log.d("profile save", "error : $it")
            }
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }


}
