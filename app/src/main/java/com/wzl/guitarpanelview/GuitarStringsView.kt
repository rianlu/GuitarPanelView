package com.wzl.guitarpanelview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

/**
Created by FaceBlack
 */
/**
 * 吉他琴弦 View
 * @property mLinePaint Paint
 * @property screenWidth Float
 * @property screenHeight Float
 * @constructor
 */
class GuitarStringsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mLinePaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.light_green)
        isAntiAlias = true
    }

    private var mTextPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.light_green)
        isAntiAlias = true
        textSize = sp2px(context, 12f)
        typeface = Typeface.DEFAULT_BOLD
    }

    private var mCirclePaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.light_green)
        isAntiAlias = true
    }

    private var screenWidth = 0f
    private var screenHeight = 0f
    private var chordLetters = arrayOf("E", "B", "G", "D", "A", "E")

    // 和弦字母间距
    private val chordLetterHeight = dip2px(context, 16f)

    // 和弦字母与琴弦的间距
    private val letterStringsMargin = dip2px(context, 8f)

    // 圆点半径
    private val radius = dip2px(context, 4f)

    // 实际琴弦高度
    private var guitarStringsWidthArray = arrayOf(
        dip2px(context, 0.5f),
        dip2px(context, 1f),
        dip2px(context, 1.5f),
        dip2px(context, 2f),
        dip2px(context, 2.5f),
        dip2px(context, 3f)
    )
    private var textBounds = Rect()

    // 琴弦间距
    private var stringsMargin = chordLetterHeight * 2

    // 上下间距不能小于 GuitarFretBoardView 中的 (scaleHeight + borderWidth * 2) / 2
    // 不然会覆盖部分 UI
    // 顶部间距
    private val topMargin = dip2px(context, 7f)
    // 底部间距
    private val bottomMargin = topMargin

    init {
        initTypeArray(context, attrs)
//        setBackgroundColor(ContextCompat.getColor(context, R.color.dark_yellow))
    }

    private fun initTypeArray(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GuitarStringsView)
        if (typedArray.length() > 0) {
            try {
                stringsMargin = typedArray.getDimension(
                    R.styleable.GuitarStringsView_stringsMargin,
                    stringsMargin.toFloat()
                ).toInt()
//                if (stringsMargin < chordLetterHeight) {
//                    stringsMargin = chordLetterHeight
//                }
            } finally {
                typedArray.recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for (i in 0 until 6) {
            val textRealHeight = -textBounds.centerY() * 2
            mTextPaint.getTextBounds(chordLetters[i], 0, 1, textBounds)
            // 文字实际坐标原点为文字底部
            // 和弦字母纵坐标 = 顶部间距 + 累计琴弦间距 + 和弦字母固定高度 - (和弦字母固定高度 - 实际和弦字母高度) / 2
            val chordLetterY =
                topMargin + stringsMargin * i + chordLetterHeight - (chordLetterHeight - textRealHeight) / 2f
            canvas?.drawText(
                chordLetters[i],
                0f,
                chordLetterY,
                mTextPaint
            )
            // 绘制琴弦
            mLinePaint.strokeWidth = guitarStringsWidthArray[i].toFloat()
            // 琴弦纵坐标 = 顶部间距 + 累计琴弦间距 - 和弦字母固定高度的一半
            val stringsY =
                topMargin + stringsMargin * i + chordLetterHeight / 2f
            canvas?.drawLine(
                (letterStringsMargin + textBounds.centerX() * 2).toFloat(),
                stringsY,
                screenWidth,
                stringsY,
                mLinePaint
            )
            // 绘制右侧圆点
            // 圆点纵坐标 = 琴弦纵坐标
            canvas?.drawCircle(
                screenWidth - radius,
                stringsY,
                radius.toFloat(),
                mCirclePaint
            )
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
        // 总宽度为：顶部间距 + 和弦固定高度的一半 + (和弦字母固定高度 + 琴弦间距) * 5 + 和弦固定高度的一半 + 底部间距
        if (wSpecMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(
                wSpecSize,
                (topMargin + chordLetterHeight / 2f + stringsMargin * 5 + chordLetterHeight / 2f + bottomMargin).toInt()
            )
        }
    }

    private fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun sp2px(context: Context, spValue: Float): Float {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f)
    }
}