package com.loke.minidrawing

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    private var drawingView: Drawing? = null
    private var customProgressDialog:Dialog? = null
    private var mGalleryButton:ImageButton? = null
    private var mImageButtonCurrentPaint:ImageButton? = null
    private var openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if(result.resultCode == RESULT_OK && result.data != null) {
                val ivFrame = findViewById<ImageView>(R.id.iv_background)
                ivFrame.setImageURI(result.data!!.data)
            }
        }
    private val permResultLauncher:ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
            permissions.entries.forEach {
                val perName = it.key
                val isGranted = it.value
                if(isGranted) {
                    Toast.makeText(this, "Permission granted for {$perName}",Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this, "permission denied for {$perName}}", Toast.LENGTH_SHORT).show()
                }
            }

        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawing_view)
        //drawingView?.setSizeforBrush(20f)

        val linearLayoutPaintColors = findViewById<LinearLayout>(R.id.paint_color_layout)
        mImageButtonCurrentPaint = linearLayoutPaintColors[0] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )
        drawingView?.setColorforBrush(mImageButtonCurrentPaint!!.tag.toString())
        val btnBrushSelect = findViewById<ImageButton>(R.id.brush_selector)
        btnBrushSelect.setOnClickListener() {
            showBrushSizeDialog()
        }

        mGalleryButton = findViewById(R.id.bạckground_selector)
        mGalleryButton?.setOnClickListener() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                AlertDialog.Builder(this).setTitle("Paint requires Storage&Location Permision")
                    .setMessage("Paint not function properly without Storage&Camera access")
                    .setNegativeButton("Cancel"){
                        dialog, which-> dialog.dismiss()
                    }.setPositiveButton("Yes"){
                        dialog, which->
                        dialog.dismiss()
                        permResultLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                    }.create().show()
            }
            else {
                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(pickIntent)

            }
            //weSnackbar.make(it, "you have clicked Gallery", Snackbar.LENGTH_LONG).show()
        }

        val btnUndo = findViewById<ImageButton>(R.id.undo_button)
        btnUndo.setOnClickListener() {
            drawingView?.undoDrawPath()
        }
        val btnRedo = findViewById<ImageButton>(R.id.redo_button)
        btnRedo.setOnClickListener() {
            drawingView?.redoDrawPath()
        }

        val btnSave = findViewById<ImageButton>(R.id.save_buton)
        btnSave.setOnClickListener() {
            if (isReadStorageAllowed()) {
                showProgressDialog()
                val frame = findViewById<FrameLayout>(R.id.drawing_view_frame)
                lifecycleScope.launch {
                    saveBitmapFile(createBitmapFromView(frame))
                }
            }
            else {
                AlertDialog.Builder(this).setTitle("Paint requires Storage&Location Permision")
                    .setMessage("Paint could not save images without Storage&Camera access")
                    .setNegativeButton("Cancel"){
                            dialog, which-> dialog.dismiss()
                    }.setPositiveButton("Yes"){
                            dialog, which->
                        dialog.dismiss()
                        permResultLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                    }.create().show()
            }
        }
    }

    private fun showBrushSizeDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.diaglog_brush_size)
        brushDialog.setTitle("Brush Size:")
        val btnSmallStroke = brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        btnSmallStroke.setOnClickListener() {
            drawingView?.setSizeforBrush(5f)
            brushDialog.dismiss()
        }
        val btnMediumStroke = brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        btnMediumStroke.setOnClickListener() {
            drawingView?.setSizeforBrush(10f)
            brushDialog.dismiss()
        }
        val btnLargeStroke = brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)
        btnLargeStroke.setOnClickListener() {
            drawingView?.setSizeforBrush(20f)
            brushDialog.dismiss()
        }
        brushDialog.show()

    }

    fun paintClicked(view:View) {
        //Toast.makeText(this, "click paint", Toast.LENGTH_SHORT).show()
        if (view != mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColorforBrush(colorTag)
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet)
            )
            mImageButtonCurrentPaint = imageButton
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )
        }
    }

    private fun createBitmapFromView(view: View) :Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        }
        else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private suspend fun saveBitmapFile(bitmap:Bitmap?):String {
        var result = ""
        withContext(Dispatchers.IO) {
            if (bitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
                    val f = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absoluteFile.toString() + File.separator
                            + "MiniDrawing_" + System.currentTimeMillis()/1000 + ".jpg")
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath
                    runOnUiThread{
                        cancelProgoressDiag()
                        if (result.isNotEmpty()) {
                            Toast.makeText(this@MainActivity,
                                "successfully saved file $result",
                                Toast.LENGTH_SHORT
                            ).show()
                            AlertDialog.Builder(this@MainActivity).setTitle("Share with your friends")
                                .setMessage("Share your new amazing job with your friends")
                                .setIcon(R.drawable.ic_share)
                                .setNegativeButton("Cancel"){
                                        dialog, which-> dialog.dismiss()
                                }.setPositiveButton("Yes"){
                                        dialog, which->
                                    dialog.dismiss()
                                    shareContent(result)
                                }.create().show()

                        }
                    }
                }
                catch (e:Exception) {
                    runOnUiThread {
                        cancelProgoressDiag()
                        Toast.makeText(this@MainActivity, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        return  result
    }

    private fun isReadStorageAllowed():Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun showProgressDialog() {
        customProgressDialog = Dialog(this)
        customProgressDialog?.setContentView(R.layout.custom_progress_dialog)
        customProgressDialog?.show()
    }

    private fun cancelProgoressDiag() {
        if (customProgressDialog != null) {
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }

    private fun shareContent(contentPath:String) {
        val requestFile = File(contentPath)
        try {
            val fileUri: Uri? = FileProvider.getUriForFile(
                this@MainActivity,
                "com.loke.minidrawing.fileprovider",
                requestFile)
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            shareIntent.type = "image/jpeg"
            startActivity(Intent.createChooser(shareIntent, "Share"))
        } catch (e: Exception) {
            Log.e("File Selector",
                "The selected file can't be shared; Error: ${e.toString()}")
            null
        }
    }
}