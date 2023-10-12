package com.loke.minidrawing

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {
    private var drawingView: Drawing? = null
    private var mImageButtonCurrentPaint:ImageButton? = null

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