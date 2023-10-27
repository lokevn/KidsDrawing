package com.loke.minidrawing

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private var drawingView: Drawing? = null
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
                    //Toast.makeText(this, "Permission granted for {$perName}",Toast.LENGTH_SHORT).show()
                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
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
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) &&
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                AlertDialog.Builder(this).setTitle("Paint requires Storage&Location Permision")
                    .setMessage("Paint not function properly without Storage&Camera access")
                    .setPositiveButton("Cancel"){
                    dialog, which-> dialog.dismiss()
                }.create().show()
            }
            else {
                permResultLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA))
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

}