package com.malmalmal.photogigs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.profile_edit.*
import java.io.*


class ProfileEdit : AppCompatActivity() {

//    var finalUri : Uri? = null

    var byteArrayData : ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_edit)
        profile_progressBar.visibility = View.INVISIBLE


        //pick profile image
        profile_edit_ppImage.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = "image/*"
//            startActivityForResult(intent, 0)
            val intent = Intent(this, ChangePP::class.java)
            startActivity(intent)
        }

        fetchUser()
    }

    //enable save button action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //fetch user info
    var profileImageData : String? = ""
    var aboutInfo : String? = ""

    private fun fetchUser() {
        val uuid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uuid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    userName_editText.setText(user!!.name)
                    aboutInfo = user.about
                    if (user.about.isEmpty()) {
                        about_editText.setText("")
                    } else {about_editText.setText(user.about)}

                    profileImageData = user.userImageUrl
                    val ro = RequestOptions().placeholder(R.drawable.user123)
                    Glide.with(profile_edit_ppImage.context).applyDefaultRequestOptions(ro).load(profileImageData).into(profile_edit_ppImage)
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

//            processingImage(getAbsolutePath(data.data!!), 100, 70)

            Glide.with(profile_edit_ppImage.context).load(selectedPhotoUri).into(profile_edit_ppImage)

        }
    }

    //save menu button action
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.menu_save -> {
                if (selectedPhotoUri == null) {
                    saveUserInfoToFirebase(profileImageData!!)
                } else {

                    profile_progressBar.visibility = View.VISIBLE
//                   val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
//                    Log.d("IMAGE","asli: ${bitmap.byteCount}")
//                    val baos = ByteArrayOutputStream()
//
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos)
//                    val data = baos.toByteArray()

                    val filename = FirebaseAuth.getInstance().uid
                    val ref = FirebaseStorage.getInstance().getReference("/profileImage/$filename")
                    ref.putBytes(byteArrayData!!)
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
        if (userName_editText.text.isEmpty()) {
            Toast.makeText(this, "username, harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (about_editText.text.isEmpty()) {
            aboutInfo = ""
        }
        else {
            aboutInfo = about_editText.text.toString()
        }

        val user = User(uid, userName_editText.text.toString(), piu, aboutInfo!!)
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

//    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
//        var width = image.width
//        var height = image.height
//
//        val bitmapRatio = width.toFloat() / height.toFloat()
//        if (bitmapRatio > 1) {
//            width = maxSize
//            height = (width / bitmapRatio).toInt()
//        } else {
//            height = maxSize
//            width = (height * bitmapRatio).toInt()
//        }
//        return Bitmap.createScaledBitmap(image, width, height, true)
//    }
//
//    fun rotateImageDegree(path: String?) : Int {
//        var degree : Int = 0
//        try {
//            var exitInterface = ExifInterface(path)
//            var orientation = exitInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
//            when (orientation) {
//                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
//                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
//                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
//            }
//        } catch (e : IOException) {
//            e.printStackTrace()
//            return degree
//        }
//
//        return degree
//    }
//
//    fun rotateImageBitmap(bitmap : Bitmap, degress : Int, destroySource : Boolean) : Bitmap {
//        if (degress == 0) {
//            return bitmap
//        }
//        val matrix = Matrix()
//        matrix.setRotate(degress.toFloat(), (bitmap.width/2).toFloat(), (bitmap.height/2).toFloat())
//        val bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//        if (null != bitmap && destroySource) {
//            bitmap.recycle()
//        }
//        return bmp
//    }
//
//    fun compressMethodAndSave(image : Bitmap, targetFile: File, exifInterface: ExifInterface?) {
//        var stream = FileOutputStream(targetFile)
//        val size = compressBitmapToStream(image, stream)
//        if (size == 0) return
//        val afterSize = targetFile.length()
//    }
//
//
//    @SuppressLint("ObsoleteSdkInt")
//    fun getSize(image : Bitmap) : Int{
//        var size = 0
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            size = image.allocationByteCount
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
//            size = image.byteCount
//        } else {
//            size = image.rowBytes * image.height
//        }
//        return size
//    }
//
//    fun getQuality(size : Int) : Int {
//        val mb = size.shr(20)
//        val kb = size.shl(10)
//        return when {
//            mb > 70 -> 17
//            mb > 50 -> 20
//            mb > 40 -> 25
//            mb > 20 -> 40
//            mb > 10 -> 50
//            mb > 3  -> 60
//            mb >= 2 -> 70
//            kb > 1800 -> 75
//            kb > 1500 -> 80
//            kb > 1000 -> 85
//            kb > 500 -> 90
//            kb > 100 -> 95
//            else -> 100
//        }
//    }
//
//    fun compressBitmapToStream(image : Bitmap, stream: OutputStream) : Int {
//        if (image == null || stream == null) return 0
//        val compressFormat = Bitmap.CompressFormat.JPEG
//        val size = getSize(image)
//        val quality = getQuality(size)
//
//        var startTime = System.currentTimeMillis()
//        image.compress(compressFormat, quality, stream)
//        image.recycle()
//        return size
//    }
//
//    fun compressImage(originalFile : File, targetFile : File?, stream : OutputStream,
//                      ifDeleted : Boolean, rotateDegree : Int, keepExif : Boolean) {
//        if (!originalFile.isFile || !originalFile.exists() || originalFile.length() < 10) {
//            return
//        }
//
//        var bitmap : Bitmap
//        val options = BitmapFactory.Options()
//        options.inJustDecodeBounds = true
//        BitmapFactory.decodeFile(originalFile.absolutePath)
//        val imageHeight = options.outHeight
//        val imageWidth = options.outWidth
//        val longEdge = Math.max(imageHeight, imageWidth)
//        val pixelCount = (imageWidth * imageHeight) shr 20
//
//        var size = originalFile.length()
//        when {
//            pixelCount >= 3 -> {
//                val compressRatio = longEdge / 1280f
//                var compressRationInt : Int= Math.round(compressRatio)
//                if (compressRationInt % 2 != 0 && compressRationInt != 1) {
//                    compressRationInt++
//                }
//
//                val bitmapOptions = BitmapFactory.Options()
//                bitmapOptions.inSampleSize = compressRationInt
//                bitmapOptions.inJustDecodeBounds = false
//                bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565
//                bitmap = BitmapFactory.decodeFile(originalFile.absolutePath, bitmapOptions)
//            }
//            imageHeight * imageWidth > 2073600 -> {
//                val bitmapOptions2 = BitmapFactory.Options()
//                bitmapOptions2.inSampleSize = 1
//                bitmapOptions2.inJustDecodeBounds = false
//                bitmapOptions2.inPreferredConfig = Bitmap.Config.RGB_565
//                bitmap = BitmapFactory.decodeFile(originalFile.absolutePath, bitmapOptions2)
//            }
//            else ->  bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
//        }
//
//        if (rotateDegree != 0) {
//            bitmap = rotateImageBitmap(bitmap, rotateDegree, true)
//        }
//        var exif : ExifInterface? = null
//        if (keepExif) exif = ExifInterface(originalFile.absolutePath)
//        if (targetFile != null) compressMethodAndSave(bitmap, targetFile, exif)
//        if (stream != null) compressBitmapToStream(bitmap, stream)
//        if (ifDeleted) originalFile.delete()
//        System.gc()
//    }
//
//    fun uriToImageFile(uri : Uri) : File? {
//        val filePathToColumn = arrayOf(MediaStore.Images.Media.DATA)
//        val cursor = contentResolver.query(uri, filePathToColumn, null, null, null)
//        if (cursor != null) {
//            cursor.moveToFirst()
//            if (cursor.moveToNext()) {
//                val columnIndex = cursor.getColumnIndex(filePathToColumn[0])
//                val filePath = cursor.getString(columnIndex)
//                cursor.close()
//                return File(filePath)
//            }
//            cursor.close()
//        }
//        return null
//    }
//
//    fun processingImage(filePath : String?, sampleSize : Int, quality : Int) {
//        val file = File(filePath)
//        if (file.exists()) {
//            try {
//                val baos = ByteArrayOutputStream()
//                val rotateDegree: Int = rotateImageDegree(filePath)
//                compressImage(file, null, baos, false, 0, true)
//                byteArrayData = baos.toByteArray()
//            } catch (ex: FileNotFoundException) {
//                ex.printStackTrace()
//            }
//        }
//            // Put data to server using in here
//    }
//
//    fun getAbsolutePath(uri : Uri) : String {
//        var filePath = ""
//        var cursor = contentResolver.query(uri, null, null, null, null)
//        if (cursor == null) {
//            filePath = uri.path
//        } else {
//            cursor.moveToFirst()
//            var index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
//            var result = cursor.getString(index)
//            cursor.close()
//            return result;
//        }
//        return ""
//    }

}

