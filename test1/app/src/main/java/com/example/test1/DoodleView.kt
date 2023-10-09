package com.example.test1
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DoodleView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val path = Path()
    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val erasingPaint = Paint().apply {
        color = Color.TRANSPARENT
        strokeWidth = 10f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var drawingMode: Boolean = true

    private lateinit var drawingBitmap: Bitmap
    private lateinit var drawingCanvas: Canvas


    init {
        // Set the background to null
        setBackgroundColor(Color.TRANSPARENT)
        // Rest of your code...
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawingCanvas = Canvas(drawingBitmap)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                // Handle the completion of drawing or erasing
                if (drawingMode) {
                    drawPath(path)
                } else {
                    erasePath(path)
                }
                path.reset()
            }
        }

        invalidate() // Redraw the view

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(drawingBitmap, 0f, 0f, null)
        canvas.drawPath(path, paint)
    }

    private fun drawPath(path: Path) {
        // Draw the path onto the drawing canvas
        drawingCanvas.drawPath(path, paint)
    }

    private fun erasePath(path: Path) {
        // Erase the path from the drawing canvas
        drawingCanvas.drawPath(path, erasingPaint)
    }

    fun setDrawingMode(drawing: Boolean) {
        drawingMode = drawing
    }
    fun clearDrawing() {
        drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }
    fun getDrawingData(): Bitmap {
        return drawingBitmap
    }
}
