package com.wzl.guitarpanelview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

/**
Created by FaceBlack
 */
class ChordDiagramsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 和弦符号
    private var chordSymBol = "D"
    private var noteList = listOf<FrameNote>()
    private var startFretText: String? = null

    // 和弦符号文字占用高度
    private val chordTextHeight = dip2px(context, 24f)

    // 开始品位文字占用高度
    private val startFretTextHeight = dip2px(context, 16f)

    // 品柱宽度
    private val capoWidth = dip2px(context, 8f)

    private val topMargin = dip2px(context, 10f) + startFretTextHeight
    private val bottomMargin = dip2px(context, 10f)

    private var screenWidth = 0f
    private var screenHeight = 0f

    // 和弦文字边界
    private var chordTextBounds = Rect()

    // x / o 文字边界
    private var fretValueTextBounds = Rect()

    // 开始品位文字边界
    private var startFretTextBounds = Rect()

    // 和弦符号与 x / o 的间距
    private val chordTextFretValueMargin = dip2px(context, 8f)

    // x / o 与品柱的间距
    private val fretValueCapoMargin = dip2px(context, 8f)

    // 圆点半径
    private val circlePointRadius = dip2px(context, 6f)
    private var mCirclePaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.dark_green)
        isAntiAlias = true
    }

    private var mChordTextPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.dark_yellow)
        isAntiAlias = true
        textSize = sp2px(context, 24f)
    }

    private var mFretValueTextPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.dark_yellow)
        isAntiAlias = true
        textSize = sp2px(context, 16f)
    }

    // 网格线宽度
    private val borderWidth = dip2px(context, 2f)
    private var mBorderPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.dark_yellow)
        isAntiAlias = true
        strokeWidth = borderWidth
    }

    init {
        val tempNoteList = mutableListOf<FrameNote>()
        tempNoteList.add(FrameNote("4", "0"))
        tempNoteList.add(FrameNote("3", "2"))
        tempNoteList.add(FrameNote("2", "3"))
        tempNoteList.add(FrameNote("1", "2"))
        noteList = tempNoteList
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        if (noteList.isEmpty()) return
        checkOverLengthChord()
        drawChordText(chordSymBol, canvas)
        drawDiagram(canvas)
        drawStrings(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w.toFloat()
        screenHeight = h.toFloat()
    }

    /**
     * 检查是否有超出五品的和弦
     */
    private fun checkOverLengthChord() {
        // 每次重置
        startFretText = null
        // 超出五品的和弦的额外处理
        val overLength: Int = (noteList.mapNotNull { frameNote -> frameNote.fret }
            .maxOfOrNull { frameNote -> frameNote.toInt() } ?: 0) - 5
        if (overLength > 0) {
            noteList.forEach { frameNote ->
                val fret = frameNote.fret
                if (fret != null && fret != "0") {
                    frameNote.fret = (fret.toInt() - overLength).toString()
                }
            }
            startFretText = context.getString(R.string.start_fret_text, overLength)
            mFretValueTextPaint.getTextBounds(
                startFretText,
                0,
                startFretText!!.length,
                startFretTextBounds
            )
        }
    }

    /**
     * 绘制和弦符号
     * @param chordText String
     * @param canvas Canvas
     */
    private fun drawChordText(chordText: String, canvas: Canvas) {
        mChordTextPaint.getTextBounds(chordText, 0, chordText.length, chordTextBounds)
        canvas.drawText(
            chordText, (-chordTextBounds.left).toFloat(),
            (-chordTextBounds.top).toFloat() + topMargin - chordTextHeight / 2, mChordTextPaint
        )
    }

    /**
     * 绘制 x / o
     * @param canvas Canvas
     * @param stringIndex Int 当前琴弦索引
     * @param textX Float 文本开始横坐标
     * @param textY Float 文本开始纵坐标
     */
    private fun drawFretValue(canvas: Canvas, stringIndex: Int, textX: Float, textY: Float) {
        if (noteList.isNotEmpty()) {
            // 匹配当前琴弦是否有对应的 note
            val drawNoteList = noteList.filter { it.string == stringIndex.toString() }
            val drawText = when {
                drawNoteList.isEmpty() -> {
                    MUTE_FRET_VALUE
                }
                // 只会匹配到一个
                drawNoteList.first().fret == "0" -> {
                    OPEN_FRET_VALUE
                }
                else -> {
                    MUTE_FRET_VALUE
                }
            }
            // 根据 bounds 来校正文本坐标
            mFretValueTextPaint.getTextBounds(drawText, 0, 1, fretValueTextBounds)
            canvas.drawText(
                drawText,
                textX - fretValueTextBounds.centerX(),
                textY - fretValueTextBounds.centerY(),
                mFretValueTextPaint
            )
        }
    }

    private fun drawCirclePoint(
        canvas: Canvas,
        stringIndex: Int,
        pointY: Float,
        widthStep: Float,
        pointXList: List<Float>
    ) {
        if (noteList.isNotEmpty()) {
            // 匹配当前琴弦是否有对应的 note
            val drawNoteList = noteList.filter { it.string == stringIndex.toString() }
            if (drawNoteList.isNotEmpty()) {
                // 只会匹配到一个
                val fret = drawNoteList.first().fret ?: return
                if (fret != "0") {
                    canvas.drawCircle(
                        pointXList[fret.toInt() - 1] + widthStep / 2,
                        pointY,
                        circlePointRadius,
                        mCirclePaint
                    )
                }
            }
        }
    }

    /**
     * 绘制指板琴弦
     * @param canvas Canvas
     */
    private fun drawStrings(canvas: Canvas) {
        val finalTopMargin = topMargin + chordTextHeight
        mFretValueTextPaint.getTextBounds(MUTE_FRET_VALUE, 0, 1, fretValueTextBounds)
        // x / o 文本宽度
        val fretValueTextWidth = fretValueTextBounds.right - fretValueTextBounds.left
        // 针对于网格线
        val leftMargin =
            fretValueTextWidth + chordTextFretValueMargin + fretValueCapoMargin + capoWidth
        val totalHeight = screenHeight - finalTopMargin - bottomMargin
        // 高度间隔
        val heightStep = (totalHeight - borderWidth) / 5
        val totalWidth = screenWidth - leftMargin
        // 宽度间隔
        val widthStep = (totalWidth - borderWidth) / 5
        // 绘制品柱
        val capoLeft = fretValueTextWidth + chordTextFretValueMargin + fretValueCapoMargin
        mBorderPaint.color = ContextCompat.getColor(context, R.color.dark_yellow)
        canvas.drawRect(
            capoLeft,
            finalTopMargin,
            leftMargin, screenHeight - bottomMargin,
            mBorderPaint
        )
        // 判断是否需要绘制开始品位
        if (startFretText != null) {
            canvas.drawText(
                startFretText!!,
                leftMargin + startFretTextBounds.left,
                finalTopMargin - startFretTextHeight / 2,
                mFretValueTextPaint
            )
        }

        // 存储竖线横坐标，用于后面画圆点
        val pointXList = mutableListOf<Float>()
        // 绘制竖线
        for (i in 0 until 6) {
            // 隐藏最后一条竖线
            if (i == 5) {
                mBorderPaint.color = ContextCompat.getColor(context, R.color.transparent)
            } else {
                mBorderPaint.color = ContextCompat.getColor(context, R.color.dark_yellow)
            }
            val startX = leftMargin + widthStep * i + borderWidth / 2
            pointXList.add(startX)
            canvas.drawLine(
                startX,
                finalTopMargin,
                leftMargin + widthStep * i,
                screenHeight - bottomMargin,
                mBorderPaint
            )
        }
        // 绘制横线
        for (i in 0 until 6) {
            // 隐藏第一条和最后一条横线
            if (i == 0 || i == 5) {
                mBorderPaint.color = ContextCompat.getColor(context, R.color.transparent)
            } else {
                mBorderPaint.color = ContextCompat.getColor(context, R.color.dark_yellow)
            }
            val startY = finalTopMargin + heightStep * i + borderWidth / 2
            canvas.drawLine(
                leftMargin,
                // 排除线宽度的影响
                startY,
                screenWidth,
                startY,
                mBorderPaint
            )
            // 绘制 x / o
            drawFretValue(canvas, i + 1, chordTextFretValueMargin, startY)
            // 绘制圆点
            drawCirclePoint(canvas, i + 1, startY, widthStep, pointXList)
        }
    }

    fun reset() {
        this.noteList = listOf()
        invalidate()
    }

    /**
     * 设置数据
     * @param chordText String? 和弦符号
     * @param noteList List<FrameNote> 和弦值
     */
    fun setData(chordText: String?, noteList: List<FrameNote>) {
        this.chordSymBol = chordText ?: chordSymBol
        this.noteList = noteList
        invalidate()
    }

    private fun drawDiagram(canvas: Canvas) {
        val finalTopMargin = topMargin + chordTextHeight

        // 代码实现（API29 以上）
//        val outer =
//            RectF(0f, finalTopMargin, screenWidth, screenHeight - bottomMargin)
//        val outerRadii = floatArrayOf(0f, 0f, 50f, 50f, 50f, 50f, 0f, 0f)
//        val inner =
//            RectF(
//                rectBorderWidth,
//                finalTopMargin + rectBorderWidth,
//                screenWidth - rectBorderWidth,
//                screenHeight - bottomMargin - rectBorderWidth
//            )
//        val innerRadii = floatArrayOf(0f, 0f, 50f, 50f, 50f, 50f, 0f, 0f)
//        canvas.drawDoubleRoundRect(outer, outerRadii, inner, innerRadii, mRectBorderPaint)

        // x / o 文本宽度
        val fretValueTextWidth = fretValueTextBounds.right - fretValueTextBounds.left
        // 绘制品柱
        val capoLeft = fretValueTextWidth + chordTextFretValueMargin + fretValueCapoMargin
        // 针对于网格线
        val leftMargin =
            fretValueTextWidth + chordTextFretValueMargin + fretValueCapoMargin + capoWidth
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_chord_diagram, null)
        drawable?.setBounds(
            leftMargin.toInt(),
            (finalTopMargin).toInt(), screenWidth.toInt(),
            (screenHeight - bottomMargin).toInt()
        )
        drawable?.draw(canvas)
    }

    private fun dip2px(context: Context, dpValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f)
    }

    private fun sp2px(context: Context, spValue: Float): Float {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f)
    }
}

private const val MUTE_FRET_VALUE = "x"
private const val OPEN_FRET_VALUE = "o"