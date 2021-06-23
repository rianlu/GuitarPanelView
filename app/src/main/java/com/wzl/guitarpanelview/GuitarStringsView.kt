package com.wzl.guitarpanelview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
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
        color = ContextCompat.getColor(context, R.color.white)
        isAntiAlias = true
    }

    private var mTextPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        isAntiAlias = true
        textSize = sp2px(context, 16f)
        typeface = Typeface.DEFAULT_BOLD
    }

    private var mCirclePaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        isAntiAlias = true
    }

    private var screenWidth = 0f
    private var screenHeight = 0f
    private var chordLetters = arrayOf("E", "B", "G", "D", "A", "E")

    // 和弦字母间距（顶部、底部间距为琴弦的一半）
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
    // 因为每个字母实际绘画出来高度并不一致
    // 所以琴弦间距用于设置和弦字母间距，确保琴弦能和字母对齐
    private var stringsMargin = chordLetterHeight

    init {
        initTypeArray(context, attrs)
        setBackgroundColor(ContextCompat.getColor(context, R.color.dark_yellow))
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
        // 用于累计之前和弦字母的高度（字母实际高度不一定一样）
        var preTextHeight = 0f
        for (i in 0 until 6) {
            val textRealHeight = -textBounds.centerY() * 2
            mTextPaint.getTextBounds(chordLetters[i], 0, 1, textBounds)
            // 文字实际坐标原点为文字底部
            // 和弦字母纵坐标 = 顶部间距 + 和弦字母自身实际高度 + 差值 + 琴弦间距 + 累计和弦字母实际高度
            // 差值的意思就是和弦字母实际高度与我们设置的固定高度的差值
            // 即字母到顶部的距离 = 顶部间距 + 和弦字母自身实际高度 + (固定高度 - 和弦字母自身实际高度) / 2
            val chordLetterY =
                stringsMargin / 2 + textRealHeight + (chordLetterHeight - textRealHeight) / 2 + stringsMargin * i + preTextHeight
            canvas?.drawText(
                chordLetters[i],
                0f,
                chordLetterY,
                mTextPaint
            )
            // 绘制琴弦
            // 琴弦纵坐标 = 和弦字母纵坐标 - 和弦字母实际高度的一半
            mLinePaint.strokeWidth = guitarStringsWidthArray[i].toFloat()
            val stringsY = chordLetterY + textBounds.centerY()
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
            // 累计和弦字母高度
            preTextHeight += chordLetterHeight
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
        if (wSpecMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(
                wSpecSize,
                ((stringsMargin + chordLetterHeight) * 6)
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