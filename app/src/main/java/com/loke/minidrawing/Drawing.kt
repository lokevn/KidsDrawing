package com.loke.minidrawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class Drawing(context:Context, attrs: AttributeSet): View(context, attrs) {
    private var mDrawPath: CustomePath? = null
    private var mCanvasBitmap:Bitmap? = null
    private var mDrawPaint:Paint? = null
    private var mCanvasPaint:Paint? = null
    private var mBrushSize:Float = 0.0f
    private var color = Color.BLUE
    private var canvas:Canvas? = null
    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomePath(color, mBrushSize)
        mDrawPaint?.color = color
        mDrawPaint?.style = Paint.Style.STROKE
        mDrawPaint?.strokeJoin = Paint.Join.ROUND
        mDrawPaint?.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 20.0f
    }

    internal inner class CustomePath(var color:Int, var brushThickness:Float):Path() {

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!,0.0f, 0.0f, mCanvasPaint)
        mDrawPaint?.strokeWidth = mDrawPath!!.brushThickness
        mDrawPaint?.color = mDrawPath!!.color
        canvas?.drawPath(mDrawPath!!, mDrawPaint!!)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.reset()
                if (touchX != null && touchY != null) {
                    mDrawPath!!.moveTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchY != null && touchX != null) {
                    mDrawPath!!.lineTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_UP -> {
                mDrawPath = CustomePath(color, mBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }
}
