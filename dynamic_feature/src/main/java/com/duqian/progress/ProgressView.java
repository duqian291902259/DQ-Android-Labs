package com.duqian.progress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import site.duqian.dynamic_feature.R;

/**
 * Des:圆角矩形进度条
 * <p>
 * Created by 杜小菜 on 2021/1/5-16:14
 * E-mail:duqian2010@gmail.com
 **/
public class ProgressView extends View {
    private static final String TAG = ProgressView.class.getSimpleName();
    public static final int PROGRESS_ROUND = 0;
    public static final int PROGRESS_RECTANGLE = 1;
    public static final int PROGRESS_OVAL = 2;
    public static final int PROGRESS_SQUARE = 3;
    private Context mContext;
    private Paint mPaintGray;
    private Path mPath = null;
    private final Path mDest = new Path();
    private Paint mPaint = null;
    private final PathMeasure mPathMeasure = new PathMeasure();
    private float mFloatPos = 0f;
    private float progress = 0f;
    private String text; // 进度条文本
    private int textColor = Color.BLACK; // 进度条文本字体颜色
    private int borderColor = Color.BLUE; //边界颜色
    private float textDimen = 0; // 进度条文本字体大小
    private TextPaint mTextPaint;
    private int progressShape = 0;
    private int strokeSize = 5;//描边宽度
    private float rounded = 8;
    private int centreHeight;
    private int centreWidth;
    private int smaller;
    private float textWidth;
    private float mVerticalY;
    RectF rect;
    private boolean isTextNotEmpty;
    private Paint.FontMetrics fontMetrics;

    public ProgressView(Context context) {
        super(context);
        this.mContext = context;
        init(null, 0);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(attrs, 0);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ProgressView, defStyle, 0);

        text = a.getString(
                R.styleable.ProgressView_textProgress);
        textColor = a.getColor(
                R.styleable.ProgressView_textColor,
                textColor);
        borderColor = a.getColor(
                R.styleable.ProgressView_borderColor,
                textColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        textDimen = a.getDimension(
                R.styleable.ProgressView_textDimension,
                textDimen);

        progress = a.getFloat(
                R.styleable.ProgressView_progress,
                progress);

        progressShape = a.getInteger(
                R.styleable.ProgressView_progressShape,
                progressShape);

        a.recycle();

        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(textColor);
//      mPaint.setColor(Color.parseColor("#65bafc"));

        mPaint.setStrokeWidth(strokeSize);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaintGray = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGray.setColor(borderColor);
        mPaintGray.setStrokeWidth(5);
        mPaintGray.setStyle(Paint.Style.STROKE);

        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        //字体绘制的位置控制
        fontMetrics = mTextPaint.getFontMetrics();
        if (progress != 0) {
            startAnim();
        }
        isTextNotEmpty = !TextUtils.isEmpty(text);
        if (isTextNotEmpty)
            invalidateTextPaint();

        rect = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取中心的x坐标
        centreWidth = getMeasuredWidth() / 2;
        //获取中心的y坐标
        centreHeight = getMeasuredHeight() / 2;
        //宽高中较小的一边
        smaller = centreWidth > centreHeight ? centreHeight * 2 : centreWidth * 2;
        switch (progressShape) {
            case PROGRESS_RECTANGLE:
            case PROGRESS_OVAL:
                rect.set(strokeSize, strokeSize, centreWidth * 2 - strokeSize, centreHeight * 2 - strokeSize);
                break;
            case PROGRESS_SQUARE:
                rect.set(centreWidth - smaller / 2 + strokeSize, centreHeight - smaller / 2 + strokeSize, centreWidth + smaller / 2 - strokeSize, centreHeight + smaller / 2 - strokeSize);
                break;
            default:
                break;
        }
    }

    private void invalidateTextPaint() {
        mTextPaint.setTextSize(textDimen);
        mTextPaint.setColor(textColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (progressShape) {
            case PROGRESS_ROUND:
                mPath.addCircle(centreWidth, centreHeight, smaller / 2 - strokeSize, Path.Direction.CW);
                if (isTextNotEmpty)//绘制文字
                    canvas.drawText(text, centreWidth - textWidth / 2 + strokeSize, mVerticalY, mTextPaint);
                break;
            case PROGRESS_RECTANGLE:
            case PROGRESS_SQUARE:
                mPath.addRoundRect(rect, smaller / rounded, smaller / rounded, Path.Direction.CW);
                if (isTextNotEmpty)
                    canvas.drawText(text, centreWidth - textWidth / 2 + strokeSize, mVerticalY, mTextPaint);
                break;
            case PROGRESS_OVAL:
                mPath.addOval(rect, Path.Direction.CW);
                if (isTextNotEmpty)
                    canvas.drawText(text, centreWidth - textWidth / 2 + strokeSize, mVerticalY, mTextPaint);
                break;
            default:
                break;
        }


        canvas.drawPath(mPath, mPaintGray);
//        mPath.addCircle(width/2,height/2,50, Path.Direction.CW);
        // 将mPath和mPathMeasure关联起来
        mPathMeasure.setPath(mPath, true);
        float mLength = mPathMeasure.getLength();

        // 每次重新绘制之前将mDest重置
        mDest.reset();
        mDest.lineTo(0, 0);

        float mStop = mFloatPos * mLength;
        // 截取mPath中从mStart起点到mStop终点的片段，到mDest里
        float mStart = 0f;
        mPathMeasure.getSegment(mStart, mStop, mDest, true);
        // 最终绘制的是截取之后的mDest
        canvas.drawPath(mDest, mPaint);

        // Draw the text.
    }

    private void startAnim() {
        if (progress > 1) {
            progress = progress / 100;
        }
        mFloatPos = progress;
        text = (int) (mFloatPos * 100) + "%";
        if (isTextNotEmpty) {
            //字体宽度
            textWidth = mTextPaint.measureText(text);
            mVerticalY = centreHeight + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        }
        invalidate();

        // 创建从0 到1的动画
        /*ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, progress);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFloatPos = (float) animation.getAnimatedValue();
                text = (int) (mFloatPos * 100) + "%";
                if (isTextNotEmpty) {
                    //字体宽度
                    textWidth = mTextPaint.measureText(text);
                    mVerticalY = centreHeight + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
                }
                // 不断刷新，重新调用onDraw方法
                invalidate();
            }
        });
        valueAnimator.setDuration(750);
        valueAnimator.start();*/
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        startAnim();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        startAnim();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        startAnim();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        startAnim();
    }

    public float getTextDimen() {
        return textDimen;
    }

    public void setTextDimen(float textDimen) {
        this.textDimen = textDimen;
        startAnim();
    }

    public int getProgressShape() {
        return progressShape;
    }

    public void setProgressShape(int progressShape) {
        this.progressShape = progressShape;
        startAnim();
    }

    public int getStrokeSize() {
        return strokeSize;
    }

    public void setStrokeSize(int strokeSize) {
        this.strokeSize = strokeSize;
        startAnim();
    }
}
