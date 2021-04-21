package com.magtonic.magtoniccargoinout.ui.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.magtonic.magtoniccargoinout.MainActivity.Companion.penColor
import com.magtonic.magtoniccargoinout.MainActivity.Companion.penWidth


import java.util.*
import kotlin.math.abs

class PaintBoard : View {
    private val touchTolerance = 4f //thumb width


    private val mTAG = PaintBoard::class.java.name
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private var paint: Paint
    private var eraser: Paint
    private var mBitmapPaint: Paint? = null
    var bitmap: Bitmap
    private var mCanvas: Canvas

    private var startX:Float = 0f
    private var startY:Float = 0f

    private var mPath: ColorPath? = null

    //private var paths: ArrayList<ColorPath>? = null
    //private var undoPaths: ArrayList<ColorPath>? = null

    private var stack: Stack<ColorPath>? = null

    init {
        // bitmap
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Canvas
        mCanvas = Canvas(bitmap)
        mCanvas.drawColor(Color.WHITE)

        mBitmapPaint = Paint(Paint.DITHER_FLAG)

        // Paint
        paint = Paint()
        paint.isDither = true
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 10f
        paint.isAntiAlias = true

        // Eraser
        eraser = Paint()
        eraser.isDither = true
        eraser.color = Color.WHITE
        eraser.style = Paint.Style.STROKE
        eraser.strokeJoin = Paint.Join.ROUND
        eraser.strokeCap = Paint.Cap.ROUND
        eraser.strokeWidth = 50f
        eraser.isAntiAlias = true

        //path
        mPath = ColorPath()
        //paths = ArrayList()
        //undoPaths = ArrayList()

        stack = Stack()
    }



    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.d(mTAG, "--->onDraw")

        //canvas!!.drawBitmap(bitmap, 0f, 0f, mBitmapPaint)

        /*if (isEraser) {
            canvas!!.drawPoint(eraser.strokeWidth, eraser.strokeWidth, eraser)
        } else {
            Log.e(mTAG, "paths size = ${paths!!.size}")

            for (i in 0 until paths!!.size) {

                val path = paths!![i]
                Log.e(mTAG, "draw path: $path")
                canvas!!.drawPath(path, paint)
            }

            canvas!!.drawPath(mPath as Path, paint)
        }
        Log.e(mTAG, "paths size = ${paths!!.size}")*/

        /*Log.e(mTAG, "paths size = ${paths!!.size}")

        for (i in 0 until paths!!.size) {

            val path = paths!![i]
            paint.color = path.pColor
            paint.strokeWidth = path.pStrokeWidth
            Log.e(mTAG, "draw path: $path")
            canvas!!.drawPath(path, paint)
        }*/

        //stack
        Log.d(mTAG, "stack size = ${stack!!.size}")
        for (i in 0 until stack!!.size) {
            val path = stack!![i]
            paint.color = path.pColor
            paint.strokeWidth = path.pStrokeWidth
            Log.d(mTAG, "draw path: $path")
            canvas!!.drawPath(path, paint)
        }

        paint.color = penColor
        paint.strokeWidth = penWidth

        canvas!!.drawPath(mPath as Path, paint)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN -> {

                Log.d(mTAG, "ACTION_DOWN ${paint.color}")

                mPath!!.pColor = penColor
                mPath!!.pStrokeWidth = penWidth


                mPath!!.reset()
                mPath!!.moveTo(event.x, event.y)
                startX = event.x
                startY = event.y

                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                Log.d(mTAG, "ACTION_MOVE ${paint.color}")



                val stopX = event.x
                val stopY = event.y
                val dx = abs(stopX - startX)
                val dy = abs(stopY - startY)
                Log.d(mTAG, "dx = $dx, dy = $dy")


                /*if (isEraser) {
                    mCanvas.drawLine(startX, startY, stopX, stopY, eraser)
                } else { //pen
                    mCanvas.drawLine(startX, startY, stopX, stopY, paint)
                }*/

                if (dx > touchTolerance || dy > touchTolerance) {
                    mPath!!.quadTo(startX, startY, (stopX+startX)/2, (stopY+startY)/2)
                }
                //mPath!!.lineTo(startX, startY)
                val tempPath = mPath
                //if (isEraser)
                //    mCanvas.drawPath(tempPath as Path, eraser)
                //else
                mCanvas.drawPath(tempPath as Path, paint)

                startX = event.x
                startY = event.y


                // call onDraw
                invalidate()

            }

            MotionEvent.ACTION_UP -> {
                Log.d(mTAG, "ACTION_UP ${paint.color}")

                mPath!!.lineTo(startX, startY)
                mCanvas.drawPath(mPath as Path, paint)
                //paths!!.add(mPath as ColorPath)
                stack!!.push(mPath as ColorPath)
                mPath = ColorPath()
                //mPath!!.reset()
                //mPath!!.lineTo(startX, startY)


                /*mPath!!.lineTo(startX, startY)
                val tempPath = mPath
                if (isEraser)
                    mCanvas.drawPath(tempPath as Path, eraser)
                else
                    mCanvas.drawPath(tempPath as Path, paint)
                val newPAth = mPath
                paths!!.add(newPAth as Path)
                mPath!!.reset()*/

                invalidate()
            }
            else -> {

            }



        }

        return true
    }



    fun clear() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas.setBitmap(bitmap)
        mCanvas.drawColor(Color.WHITE)

        //paths!!.clear()
        //undoPaths!!.clear()

        stack!!.clear()

        invalidate()
    }

    fun undo() {
        /*if (paths!!.size > 0) {
            Log.e(mTAG, "undo ${paths!!.size}")
            //mCanvas.drawColor(Color.WHITE)
            //mCanvas.save()

            undoPaths!!.add(paths!!.removeAt(paths!!.size - 1))

            invalidate()
        }*/

        if (stack!!.size > 0) {
            Log.e(mTAG, "undo ${stack!!.size}")
            stack!!.pop()

            invalidate()
        }
    }
}