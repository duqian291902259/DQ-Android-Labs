package site.duqian.test

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
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
    private var text: String? = null// 进度条文本
    private var textColor = Color.BLACK // 进度条文本字体颜色
    private var borderColor = Color.BLUE //边界颜色
    private var textDimen = 0f // 进度条文本字体大小
    private lateinit var mTextPaint: TextPaint
    private var progressShape = 0
    private var strokeSize = 5 //描边宽度
    private val rounded = 8f
    private var centreHeight = 0
    private var centreWidth = 0
    private var smaller = 0
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
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ProgressView, defStyle, 0
        )
        text = a.getString(
            R.styleable.ProgressView_textProgress
        )
        textColor = a.getColor(
            R.styleable.ProgressView_textColor, textColor
        )
        borderColor = a.getColor(
            R.styleable.ProgressView_borderColor, textColor
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
        mPaint.color = textColor
        mPaint.strokeWidth = strokeSize.toFloat()
        mPaint.style = Paint.Style.STROKE
        mPaintGray = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintGray.color = borderColor
        mPaintGray.strokeWidth = 5f
        mPaintGray.style = Paint.Style.STROKE
        mTextPaint = TextPaint()
        mTextPaint.flags = Paint.ANTI_ALIAS_FLAG
        mTextPaint.textAlign = Paint.Align.LEFT

        //字体绘制的位置控制
        fontMetrics = mTextPaint.fontMetrics
        if (mProgress != 0f) {
            startAnim()
        }
        isTextNotEmpty = !TextUtils.isEmpty(text)
        if (isTextNotEmpty) invalidateTextPaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //获取中心的x坐标
        centreWidth = measuredWidth / 2
        //获取中心的y坐标
        centreHeight = measuredHeight / 2
        //宽高中较小的一边
        smaller = if (centreWidth > centreHeight) centreHeight * 2 else centreWidth * 2
        when (progressShape) {
            PROGRESS_RECTANGLE, PROGRESS_OVAL ->
                rect[strokeSize.toFloat(), strokeSize.toFloat(), (centreWidth * 2 - strokeSize).toFloat()] =
                    (centreHeight * 2 - strokeSize).toFloat()
            PROGRESS_SQUARE -> rect[(centreWidth - smaller / 2 + strokeSize).toFloat(),
                    (centreHeight - smaller / 2 + strokeSize).toFloat(), (centreWidth + smaller / 2 - strokeSize).toFloat()] =
                (centreHeight + smaller / 2 - strokeSize).toFloat()
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
                    centreWidth.toFloat(),
                    centreHeight.toFloat(),
                    (smaller / 2 - strokeSize).toFloat(),
                    Path.Direction.CW
                )
                if (isTextNotEmpty) //绘制文字
                    canvas.drawText(
                        text!!,
                        centreWidth - textWidth / 2 + strokeSize,
                        mVerticalY,
                        mTextPaint
                    )
            }
            PROGRESS_RECTANGLE, PROGRESS_SQUARE -> {
                mPath.addRoundRect(
                    rect,
                    smaller / rounded,
                    smaller / rounded,
                    Path.Direction.CW
                )
                if (isTextNotEmpty) canvas.drawText(
                    text!!,
                    centreWidth - textWidth / 2 + strokeSize,
                    mVerticalY,
                    mTextPaint
                )
            }
            PROGRESS_OVAL -> {
                mPath.addOval(rect!!, Path.Direction.CW)
                if (isTextNotEmpty) canvas.drawText(
                    text!!,
                    centreWidth - textWidth / 2 + strokeSize,
                    mVerticalY,
                    mTextPaint
                )
            }
            else -> {
            }
        }
        canvas.drawPath(mPath, mPaintGray)
        //        mPath.addCircle(width/2,height/2,50, Path.Direction.CW);
        // 将mPath和mPathMeasure关联起来
        mPathMeasure.setPath(mPath, true)
        val mLength = mPathMeasure.length

        // 每次重新绘制之前将mDest重置
        mDest.reset()
        mDest.lineTo(0f, 0f)
        val mStop = mFloatPos * mLength
        // 截取mPath中从mStart起点到mStop终点的片段，到mDest里
        val mStart = 0f
        mPathMeasure.getSegment(mStart, mStop, mDest, true)
        // 最终绘制的是截取之后的mDest
        canvas.drawPath(mDest, mPaint)

        // Draw the text.
    }

    private fun startAnim() {
        if (mProgress > 1) {
            mProgress /= 100
        }
        mFloatPos = mProgress
        text = "${(mFloatPos * 100).toInt()} %"
        if (isTextNotEmpty) { //字体宽度
            textWidth = mTextPaint.measureText(text)
            fontMetrics?.apply {
                mVerticalY = centreHeight + (this.descent - this.ascent) / 2 - this.descent
            }
        }
        invalidate()
    }

    fun getProgress(): Float {
        return mProgress
    }

    fun setProgress(progress: Float) {
        this.mProgress = progress
        startAnim()
    }

    fun getProgressShape(): Int {
        return progressShape
    }

    fun setProgressShape(progressShape: Int) {
        this.progressShape = progressShape
        startAnim()
    }

    companion object {
        private val TAG = ProgressView::class.java.simpleName
        const val PROGRESS_ROUND = 0
        const val PROGRESS_RECTANGLE = 1
        const val PROGRESS_OVAL = 2
        const val PROGRESS_SQUARE = 3
    }
}