package com.zhongjh.imageedit.core.sticker;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zhongjh.imageedit.view.BaseImageStickerView;

/**
 * 贴纸调整功能的辅助类，实现View.OnTouchListener接口
 * 负责处理贴纸的缩放和旋转操作，通常用于贴纸的控制点交互
 * 
 * @author felix
 * @date 2017/11/15 下午5:44
 */
public class ImageStickerAdjustHelper implements View.OnTouchListener {

    /**
     * 日志标记，用于调试和日志输出
     */
    private static final String TAG = "IMGStickerAdjustHelper";

    /**
     * 要调整的视图对象
     */
    private final View mView;

    /**
     * 贴纸容器视图，通常是BaseImageStickerView或其子类
     */
    private final BaseImageStickerView mContainer;

    /**
     * 触摸点到中心点的距离，用于计算缩放比例
     */
    private double mRadius;
    
    /**
     * 触摸点相对于中心点的角度，用于计算旋转角度
     */
    private double mDegrees;

    /**
     * 变换矩阵，用于处理坐标变换
     */
    private final Matrix mMatrix = new Matrix();

    /**
     * 构造函数，初始化贴纸调整辅助类
     * 
     * @param container 贴纸容器视图，负责应用缩放和旋转操作
     * @param view 要调整的视图对象，通常是贴纸的控制点
     */
    public ImageStickerAdjustHelper(BaseImageStickerView container, View view) {
        mView = view;
        mContainer = container;
        // 设置触摸监听器
        mView.setOnTouchListener(this);
    }

    /**
     * 处理触摸事件，实现贴纸的缩放和旋转功能
     * 支持ACTION_DOWN和ACTION_MOVE事件
     * 
     * @param v 触发触摸事件的视图
     * @param event 触摸事件对象
     * @return 如果事件被处理则返回true，否则返回false
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float mCenterY;
        float pointX;
        float pointY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 获取触摸点在视图内的坐标
                float x = event.getX();
                float y = event.getY();
                
                // 初始化中心点坐标（这里设为0，0）
                float mCenterX = mCenterY = 0;
                
                // 计算触摸点相对于容器中心点的坐标
                pointX = mView.getX() + x - mContainer.getPivotX();
                pointY = mView.getY() + y - mContainer.getPivotY();
                
                // 记录调试信息
                Log.d(TAG, String.format("X=%f,Y=%f", pointX, pointY));
                
                // 计算触摸点到中心点的距离和角度
                mRadius = toLength(pointX, pointY);
                mDegrees = toDegrees(pointY, pointX);
                
                // 设置变换矩阵，用于后续计算
                mMatrix.setTranslate(pointX - x, pointY - y);
                
                // 记录调试信息
                Log.d(TAG, String.format("degrees=%f", toDegrees(pointY, pointX)));
                
                // 应用旋转变换
                mMatrix.postRotate((float) -toDegrees(pointY, pointX), mCenterX, mCenterY);
                
                // 返回true表示事件被处理
                return true;

            case MotionEvent.ACTION_MOVE:
                // 获取移动后的触摸点坐标
                float[] xy = {event.getX(), event.getY()};
                
                // 计算移动后触摸点相对于容器中心点的坐标
                pointX = mView.getX() + xy[0] - mContainer.getPivotX();
                pointY = mView.getY() + xy[1] - mContainer.getPivotY();
                
                // 记录调试信息
                Log.d(TAG, String.format("X=%f,Y=%f", pointX, pointY));
                
                // 计算新的距离和角度
                double radius = toLength(pointX, pointY);
                double degrees = toDegrees(pointY, pointX);
                
                // 计算缩放比例
                float scale = (float) (radius / mRadius);
                
                // 应用缩放
                mContainer.addScale(scale);
                
                // 记录调试信息
                Log.d(TAG, "    D   = " + (degrees - mDegrees));
                
                // 应用旋转
                mContainer.setRotation((float) (mContainer.getRotation() + degrees - mDegrees));
                
                // 更新当前半径，用于下一次计算
                mRadius = radius;
                
                // 返回true表示事件被处理
                return true;
            default:
                break;
        }
        // 默认返回false表示事件未被处理
        return false;
    }

    /**
     * 将笛卡尔坐标转换为角度
     * 使用Math.atan2计算弧度，然后转换为角度
     * 
     * @param y Y坐标
     * @param x X坐标
     * @return 角度值
     */
    private static double toDegrees(float y, float x) {
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * 计算点到原点的距离
     * 使用勾股定理计算距离
     * 
     * @param x X坐标
     * @param y Y坐标
     * @return 距离值
     */
    private static double toLength(float x, float y) {
        return Math.sqrt(x * x + y * y);
    }
}
