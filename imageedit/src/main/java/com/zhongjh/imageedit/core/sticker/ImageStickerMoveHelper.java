package com.zhongjh.imageedit.core.sticker;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;

/**
 * 贴纸移动功能的辅助类，处理贴纸视图的拖拽移动操作
 * 负责将用户的触摸事件转换为贴纸的位置变化，考虑了旋转角度的影响
 * 
 * @author felix
 * @date 2017/11/17 下午6:08
 */
public class ImageStickerMoveHelper {

    /**
     * 日志标记，用于调试和日志输出
     */
    private static final String TAG = "IMGStickerMoveHelper";

    /**
     * 要移动的视图对象
     */
    private final View mView;

    /**
     * 记录触摸按下时的X坐标
     */
    private float mX;
    
    /**
     * 记录触摸按下时的Y坐标
     */
    private float mY;

    /**
     * 静态变换矩阵，用于处理考虑旋转角度的坐标变换
     */
    private static final Matrix M = new Matrix();

    /**
     * 构造函数，初始化贴纸移动辅助类
     * 
     * @param view 要移动的视图对象
     */
    public ImageStickerMoveHelper(View view) {
        mView = view;
    }

    /**
     * 处理触摸事件，实现贴纸的拖拽移动
     * 支持ACTION_DOWN和ACTION_MOVE事件，考虑了视图的旋转角度
     * 
     * @param v 触发触摸事件的视图
     * @param event 触摸事件对象
     * @return 如果事件被处理则返回true，否则返回false
     */
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 记录触摸按下时的坐标
                mX = event.getX();
                mY = event.getY();
                // 重置变换矩阵并设置旋转角度（与视图的旋转角度一致）
                M.reset();
                M.setRotate(v.getRotation());
                // 返回true表示事件被处理
                return true;
            case MotionEvent.ACTION_MOVE:
                // 计算触摸点的移动距离
                float[] dxy = {event.getX() - mX, event.getY() - mY};
                // 将移动距离应用变换矩阵，考虑视图的旋转角度
                M.mapPoints(dxy);
                // 更新视图的平移位置
                v.setTranslationX(mView.getTranslationX() + dxy[0]);
                v.setTranslationY(mView.getTranslationY() + dxy[1]);
                // 返回true表示事件被处理
                return true;
            default:
                break;
        }
        // 默认返回false表示事件未被处理
        return false;
    }
}
