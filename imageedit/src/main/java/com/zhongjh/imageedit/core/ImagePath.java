package com.zhongjh.imageedit.core;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

/**
 *
 * @author felix
 * @date 2017/11/22 下午6:13
 */

public class ImagePath {

    protected Path path;

    private int color = Color.RED;

    private float width = BASE_MOSAIC_WIDTH;

    private ImageMode mode = ImageMode.DOODLE;

    /**
     * 画笔的宽度
     */
    public static final float BASE_DOODLE_WIDTH = 10f;

    public static final float BASE_MOSAIC_WIDTH = 72f;

    public ImagePath() {
        this(new Path());
    }

    public ImagePath(Path path) {
        this(path, ImageMode.DOODLE);
    }

    public ImagePath(Path path, ImageMode mode) {
        this(path, mode, Color.RED);
    }

    public ImagePath(Path path, ImageMode mode, int color) {
        this(path, mode, color, BASE_MOSAIC_WIDTH);
    }

    public ImagePath(Path path, ImageMode mode, int color, float width) {
        this.path = path;
        this.mode = mode;
        this.color = color;
        this.width = width;
        if (mode == ImageMode.MOSAIC) {
            path.setFillType(Path.FillType.EVEN_ODD);
        }
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public ImageMode getMode() {
        return mode;
    }

    public void setMode(ImageMode mode) {
        this.mode = mode;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    public void onDrawDoodle(Canvas canvas, Paint paint) {
        if (mode == ImageMode.DOODLE) {
            paint.setColor(color);
            paint.setStrokeWidth(BASE_DOODLE_WIDTH);
            // rewind
            canvas.drawPath(path, paint);
        }
    }

    public void onDrawMosaic(Canvas canvas, Paint paint) {
        if (mode == ImageMode.MOSAIC) {
            paint.setStrokeWidth(width);
            canvas.drawPath(path, paint);
        }
    }

    public void transform(Matrix matrix) {
        path.transform(matrix);
    }
}
