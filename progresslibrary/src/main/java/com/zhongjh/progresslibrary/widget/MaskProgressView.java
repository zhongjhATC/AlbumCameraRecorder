package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.utils.DisplayMetricsUtils;

/**
 * 用于图片、视频的加载进度的view
 * Created by zhongjh on 2018/10/16.
 */
public class MaskProgressView extends android.support.v7.widget.AppCompatImageView {

    private static final String TAG = "MaskProgressView";

    private static final int MAX_PROGRESS = 100; //最大进度值

    private int width; // view 宽
    private int height; // view 高
    private Rect rect; // 遮罩矩形
    private Paint maskingPaint;     // 遮罩层画笔
    private Paint textPaint;        // 显示在遮罩层的字体画笔
    private int percentage = 0;     // 设置进度
    private int centerX;            // 字体的x位置
    private int centerY;            // 字体的y位置

    private int maskingColor = R.attr.colorPrimary; // 遮罩颜色，默认用主颜色
    private int textSize = DisplayMetricsUtils.dip2px(12);  // 显示在遮罩层的字体大小
    private int textColor = Color.BLACK; // 显示在遮罩层的字体颜色
    private String textString = "图片上传中";    // 加载中的文字

    public MaskProgressView(Context context) {
        this(context, null);
    }

    public MaskProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        maskingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        maskingPaint.setStyle(Paint.Style.FILL);
        maskingPaint.setColor(maskingColor);

        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);

        //测量文字的长度
        int textLength = (int) textPaint.measureText(textString);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        // 获取文字的高度
        int textHeight = (int) (fontMetrics.descent - fontMetrics.ascent);
        // 计算x轴居中的坐标
        centerX = (width - textLength) / 2;
        centerY = (int) ((height + textHeight) / 2 - fontMetrics.descent);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec));
    }

    /**
     * 每次绘画
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (percentage > 0 && percentage < MAX_PROGRESS) {
            // 设置顶部，假设高度70 * 0.1 / 100
            rect.top = height * percentage / MAX_PROGRESS;
            // 绘制图片遮罩
            if (percentage < MAX_PROGRESS) {
                canvas.drawText(textString, centerX, centerY, textPaint);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //获取view的宽高
        width = getWidth();
        height = getHeight();
    }


    /**
     * 测量宽高模式
     *
     * @param MeasureSpecSize 宽高度
     * @return 返回大小
     */
    private int measureSize(int MeasureSpecSize) {
        int size = 0;
        int[] ints = measureSpec(MeasureSpecSize);
        // 判断模式
        if (ints[0] == MeasureSpec.EXACTLY) {
            // 如果当前模式是 当前的尺寸就是当前View应该取的尺寸
            size = ints[1];
        } else {
            size = DisplayMetricsUtils.dip2px(70);
            if (ints[0] == MeasureSpec.AT_MOST) {
                // 如果当前模式是 当前尺寸是当前View能取的最大尺寸,就取最小的那个，70或者是最大尺寸
                size = Math.min(size, ints[1]);
            }
        }

        return size;
    }

    /**
     * 获取父布局传递给子布局的布局要求、大小
     *
     * @param measureSpec 包含 宽或高的信息，还有其他有关信息，比如模式等
     * @return 返回大小和模式的数组
     */
    private int[] measureSpec(int measureSpec) {
        int[] measure = new int[2];
        measure[0] = MeasureSpec.getMode(measureSpec);
        measure[1] = MeasureSpec.getSize(measureSpec);
        return measure;
    }

}
