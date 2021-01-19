package site.duqian.test

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View

/**
 * Des:圆角矩形进度条
 *
 *
 * Created by 杜小菜 on 2021/1/5-16:14
 * E-mail:duqian2010@gmail.com
 */
class ProgressView : View {
    private var mContext: Context
    private lateinit var mPaintGray: Paint
    private lateinit var mPath: Path
    private val mDest = Path()
    private lateinit var mPaint: Paint
    private val mPathMeasure = PathMeasure()
    private var mFloatPos = 0f
    private var mProgress = 0f
    private var textProgress: String? = null// 进度条文本
    private var textColor = Color.WHITE // 进度条文本字体颜色
    private var firstPbColor = Color.GRAY //进度条底层颜色
    private var secondPbColor = Color.WHITE // 进度条上层颜色
    private var textDimen = 20f // 进度条文本字体大小
    private lateinit var mTextPaint: TextPaint
    private var progressShape = 0
    private var strokeSize = 5f //描边宽度
    private val rounded = 8f
    private var centreHeight = 0f
    private var centreWidth = 0f
    private var smaller = 0f
    private var textWidth = 0f
    private var rect: RectF = RectF()
    private var isTextNotEmpty = false
    private var fontMetrics: Paint.FontMetrics? = null
    private var mVerticalY = 0f

    constructor(context: Context) : super(context) {
        mContext = context
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        mContext = context
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ProgressView, defStyle, 0
        )
        textProgress = a.getString(
            R.styleable.ProgressView_textProgress
        )
        textColor = a.getColor(
            R.styleable.ProgressView_textColor, textColor
        )
        firstPbColor = a.getColor(
            R.styleable.ProgressView_firstPbColor, firstPbColor
        )
        secondPbColor = a.getColor(
            R.styleable.ProgressView_secondPbColor, secondPbColor
        )
        strokeSize = a.getDimension(
            R.styleable.ProgressView_strokeSize, strokeSize
        )
        textDimen = a.getDimension(
            R.styleable.ProgressView_textDimension, textDimen
        )
        mProgress = a.getFloat(
            R.styleable.ProgressView_progress, mProgress
        )
        progressShape = a.getInteger(
            R.styleable.ProgressView_progressShape, progressShape
        )
        a.recycle()
        mPath = Path()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.color = secondPbColor
        mPaint.strokeWidth = strokeSize
        mPaint.style = Paint.Style.STROKE

        mPaintGray = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintGray.color = firstPbColor
        mPaintGray.strokeWidth = strokeSize
        mPaintGray.style = Paint.Style.STROKE

        mTextPaint = TextPaint()
        mTextPaint.flags = Paint.ANTI_ALIAS_FLAG
        mTextPaint.textAlign = Paint.Align.LEFT

        //字体绘制的位置控制
        fontMetrics = mTextPaint.fontMetrics
        if (mProgress != 0f) {
            updateProgressAndText()
        }
        isTextNotEmpty = !TextUtils.isEmpty(textProgress)
        //if (isTextNotEmpty)
        invalidateTextPaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //获取中心的x坐标
        centreWidth = measuredWidth / 2.0f
        //获取中心的y坐标
        centreHeight = measuredHeight / 2.0f
        //宽高中较小的一边
        smaller = if (centreWidth > centreHeight) centreHeight * 2.0f else centreWidth * 2.0f
        when (progressShape) {
            PROGRESS_RECTANGLE, PROGRESS_OVAL -> {
                rect.set(
                    strokeSize,
                    strokeSize,
                    centreWidth * 2 - strokeSize,
                    centreHeight * 2 - strokeSize
                )
            }

            PROGRESS_SQUARE -> {
                rect.set(
                    centreWidth - smaller / 2 + strokeSize,
                    centreHeight - smaller / 2 + strokeSize,
                    centreWidth + smaller / 2 - strokeSize,
                    centreHeight + smaller / 2 - strokeSize
                )
            }

            PROGRESS_ROUND_RECTANGLE -> {
                rect.set(
                    strokeSize,
                    strokeSize,
                    centreWidth * 2 - strokeSize,
                    centreHeight * 2 - strokeSize
                )
            }
            else -> {
            }
        }
    }

    private fun invalidateTextPaint() {
        mTextPaint.textSize = textDimen
        mTextPaint.color = textColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (progressShape) {
            PROGRESS_ROUND -> {
                mPath.addCircle(
                    centreWidth,
                    centreHeight,
                    smaller / 2 - strokeSize,
                    Path.Direction.CW
                )
            }
            PROGRESS_RECTANGLE, PROGRESS_SQUARE -> {
                mPath.addRoundRect(
                    rect,
                    smaller / rounded,
                    smaller / rounded,
                    Path.Direction.CW
                )
            }
            PROGRESS_OVAL -> {
                mPath.addOval(rect, Path.Direction.CW)
            }
            PROGRESS_ROUND_RECTANGLE -> {
                mPath.addRoundRect(
                    rect,
                    smaller / 1.0f,
                    smaller / 1.0f,
                    Path.Direction.CW
                )
            }
            else -> {
            }
        }

        if (isTextNotEmpty) {
            canvas.drawText(
                textProgress!!,
                centreWidth - textWidth / 2 + strokeSize,
                mVerticalY,
                mTextPaint
            )
        }
        canvas.drawPath(mPath, mPaintGray)

        //将mPath和mPathMeasure关联起来
        mPathMeasure.setPath(mPath, true)
        val mLength = mPathMeasure.length

        //重置
        mDest.reset()
        mDest.lineTo(0f, 0f)
        //val isEnd = mFloatPos > 1 - PROGRESS_START
        val mStart = 0f//if (isEnd) 0f else measuredWidth / 1.8f // 调整起始绘制的位置
        val mStop =
            mFloatPos * mLength //if (isEnd) (mFloatPos - 1) * mLength else (mFloatPos - PROGRESS_START) * mLength

        // 截取
        mPathMeasure.getSegment(mStart, mStop, mDest, true)
        canvas.drawPath(mDest, mPaint)
    }

    private fun updateProgressAndText() {
        if (mProgress > 1) {
            mProgress /= 100
        }
        mFloatPos = mProgress
        textProgress = "${(mFloatPos * 100).toInt()} %"

        //实际起始位置 mFloatPos = mProgress + PROGRESS_START

        isTextNotEmpty = true
        //Log.d("dq-pb-$TAG", "" + textProgress)
        textWidth = mTextPaint.measureText(textProgress)
        fontMetrics?.apply {
            mVerticalY = centreHeight + (this.descent - this.ascent) / 2 - this.descent
        }
        invalidate()
    }

    fun setProgress(progress: Float) {
        this.mProgress = progress
        updateProgressAndText()
    }

    companion object {
        private val TAG = ProgressView::class.java.simpleName
        const val PROGRESS_START = 0.3f
        const val PROGRESS_ROUND = 0
        const val PROGRESS_RECTANGLE = 1
        const val PROGRESS_OVAL = 2
        const val PROGRESS_SQUARE = 3
        const val PROGRESS_ROUND_RECTANGLE = 4
    }
}