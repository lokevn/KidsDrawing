package com.loke.minidrawing

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    private var drawingView: Drawing? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawing_view)
        //drawingView?.setSizeforBrush(20f)

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
}