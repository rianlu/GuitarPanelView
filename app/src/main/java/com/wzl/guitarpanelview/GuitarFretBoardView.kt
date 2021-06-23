package com.wzl.guitarpanelview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import androidx.core.content.ContextCompat
import kotlin.random.Random

/**
Created by FaceBlack
 */
/**
 * 吉他指板 View
 */
class GuitarFretBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 完整吉他谱
    private var allNotes = mutableListOf<List<GuitarNote>>()

    private var textBounds = Rect()
    private val scaleHeight = dip2px(context, 12f)
    private val scaleRadius = dip2px(context, 10f)
    private val textLeftMargin = dip2px(context, 8f)
    private val borderWidth = dip2px(context, 1f)
    private val chordTextSize = sp2px(context, 12f)

    // 如果设置，这两个参数都需要传值
    private var startOffset = dip2px(context, 0f)

    // 时长 / px 比例
    private val ratio = 1f

    private var mTextPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.dark_green)
        isAntiAlias = true
        textSize = chordTextSize
        typeface = Typeface.DEFAULT_BOLD
    }

    private var mRectPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.light_green)
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private var mBorderPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.dark_green)
        isAntiAlias = true
        strokeWidth = +borderWidth
        style = Paint.Style.STROKE
    }

    private var screenWidth = 0f
    private var screenHeight = 0f

    private var mScroller: Scroller

    init {
        repeat(20) {
            allNotes.add(
                listOf(
                    GuitarNote(Random.nextInt(1, 4), Random.nextInt(1, 21), 500),
                    GuitarNote(Random.nextInt(1, 4), Random.nextInt(1, 21), 500),
                )
            )
            allNotes.add(
                listOf(
                    GuitarNote(Random.nextInt(4, 7), Random.nextInt(1, 21), 300),
                    GuitarNote(Random.nextInt(4, 7), Random.nextInt(1, 21), 300),
                )
            )
        }
        mScroller = Scroller(context, LinearInterpolator())
//        setBackgroundColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        var textX = startOffset
        // 之间的间距
        val scaleMargin = (screenHeight - scaleHeight) / 5
        if (canvas == null) return
        if (allNotes.isEmpty()) return
        allNotes.forEachIndexed { index, guitarNoteList ->
            // 设置画笔颜色
            when (guitarNoteList.first().noteState) {
                NoteState.NONE -> {
                    mRectPaint.color = ContextCompat.getColor(context, R.color.light_green)
                    mBorderPaint.color =
                        ContextCompat.getColor(context, R.color.dark_green)
                    mTextPaint.color = ContextCompat.getColor(context, R.color.dark_green)
                }
                NoteState.CORRECT -> {
                    mRectPaint.color =
                        ContextCompat.getColor(context, R.color.light_green)
                    mBorderPaint.color =
                        ContextCompat.getColor(context, R.color.dark_green)
                    mTextPaint.color = ContextCompat.getColor(context, R.color.dark_green)

                }
                NoteState.WRONG -> {
                    mRectPaint.color =
                        ContextCompat.getColor(context, R.color.light_green)
                    mBorderPaint.color =
                        ContextCompat.getColor(context, R.color.dark_green)
                    mTextPaint.color = ContextCompat.getColor(context, R.color.dark_green)
                }
            }
            guitarNoteList.forEach { guitarNote ->
                val scaleWidth = guitarNote.duration / ratio
                mTextPaint.getTextBounds(guitarNote.fretNumber.toString(), 0, 1, textBounds)
                // 需要加上边框宽度的一半
                val top = scaleMargin * (guitarNote.stringNumberTab - 1) + borderWidth / 2
                val bottom = scaleMargin * (guitarNote.stringNumberTab - 1) + scaleHeight
                canvas.drawRoundRect(
                    textX,
                    top,
                    (scaleWidth + textX),
                    bottom,
                    scaleRadius,
                    scaleRadius,
                    mRectPaint
                )
                // 绘制边框
                canvas.drawRoundRect(
                    textX + borderWidth / 2,
                    top,
                    (scaleWidth + textX) - borderWidth / 2,
                    bottom,
                    scaleRadius,
                    scaleRadius,
                    mBorderPaint
                )
                // 文本定位
                // 纵坐标 = 间距 + 文本自身高度的一半 + 所占宽度的一半 + 边框宽度的一半
                canvas.drawText(
                    guitarNote.fretNumber.toString(), textX + textLeftMargin,
                    scaleMargin * (guitarNote.stringNumberTab - 1) - textBounds.centerY() + (bottom - top) / 2 + borderWidth / 2,
                    mTextPaint
                )
            }
            textX += (guitarNoteList.map { it.duration }.maxOrNull() ?: 0 )/ ratio
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w.toFloat()
        screenHeight = h.toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val wSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        Log.d(
            GuitarFretBoardView::class.java.name,
            "wSpecMode: ${wSpecMode == MeasureSpec.UNSPECIFIED} ${wSpecMode == MeasureSpec.EXACTLY} ${wSpecMode == MeasureSpec.AT_MOST}"
        )
        Log.d(GuitarFretBoardView::class.java.name, "wSpecSize: $wSpecSize")
        Log.d(
            GuitarFretBoardView::class.java.name,
            "hSpecMode: ${hSpecMode == MeasureSpec.UNSPECIFIED} ${hSpecMode == MeasureSpec.EXACTLY} ${hSpecMode == MeasureSpec.AT_MOST}"
        )
        Log.d(GuitarFretBoardView::class.java.name, "hSpecSize: $hSpecSize")
        val realWidth = getTotalDuration()
        setMeasuredDimension(realWidth, hSpecSize)
    }

    fun setData(list: List<List<GuitarNote>>) {
        this.allNotes = list.toMutableList()
        invalidate()
    }

    private fun dip2px(context: Context, dpValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f)
    }

    private fun sp2px(context: Context, spValue: Float): Float {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f)
    }

    /**
     * 更新当前组音符状态
     * @param state Boolean
     */
    fun updateGuitarNoteState(state: NoteState, groupIndex: Int) {
        allNotes[groupIndex].first().noteState = state
        invalidate()
    }

    /**
     * 滚动到指定位置
     * @param dx Int
     * @param dy Int
     * @param duration Int // 默认为 250 ms
     */
    fun smoothScrollBy(dx: Int, dy: Int, duration: Int = -1) {
//        if (!mScroller.isFinished) return
        if (duration == -1) {
            mScroller.startScroll(mScroller.currX, mScroller.currY, (dx / ratio).toInt(), dy)
        } else {
            mScroller.startScroll(
                mScroller.currX, mScroller.currY, (dx / ratio).toInt(), dy,
                duration
            )
        }
        invalidate()
    }

    /**
     * 重置滚动
     */
    fun resetScroll() {
        allNotes.map { it.first().noteState = NoteState.NONE }
        // 重置滚动位置
        mScroller.finalX = 0
        invalidate()
    }

    /**
     * 获得滚动所需总时长
     * @return Int
     */
    fun getTotalDuration(): Int {
        return allNotes.map { it.map { it.duration }.maxOrNull() ?: 0 }.sum()
    }

    override fun computeScroll() {
//        Log.d("computeScroll", "value: ${mScroller.currX}")
        // 先判断mScroller滚currentTimeStamp动是否完成
        if (mScroller.computeScrollOffset()) {
            // 这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.currX, mScroller.currY)
            // 必须调用该方法，否则不一定能看到滚动效果
            postInvalidate()
        }
        super.computeScroll()
    }
}