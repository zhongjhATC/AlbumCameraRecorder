package com.zhongjh.cameraviewsoundrecorder.camera.widget.cameralayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.ImageView;

import com.zhongjh.cameraviewsoundrecorder.camera.CameraCallback;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.ErrorListener;

/**
 * 摄像器的关联者
 * Created by zhongjh on 2018/8/14.
 */
public interface CameraContact {

    interface CameraPresenter {

        /**
         * 开始
         * @param surfaceHolder surfaceHolder是一个持有surface的抽象接口，可以控制surface的大小、格式、编辑、监听surface改变，一般通过SurfaceView实现
         * @param screenProp 高 / 宽 比例
         */
        void start(SurfaceHolder surfaceHolder, float screenProp);

        /**
         * 停止
         */
        void stop();

        void handleFocus(float x, float y, CameraCallback.FocusCallback callback);

        /**
         * 切换摄像模式 前摄像，后摄像头
         * @param surfaceHolder surfaceHolder是一个持有surface的抽象接口，可以控制surface的大小、格式、编辑、监听surface改变，一般通过SurfaceView实现
         * @param screenProp 高 / 宽 比例
         */
        void swtich(SurfaceHolder surfaceHolder, float screenProp);

        void restart();

        /**
         * 拍照
         */
        void capture();

        /**
         * 启动录像
         * @param surface 用来画图的地方
         * @param screenProp 高/宽 比例
         */
        void record(Surface surface, float screenProp);

        void stopRecord(boolean isShort, long time);

        /**
         * 取消
         * @param holder 用来画图的地方
         * @param screenProp 高/宽 比例
         */
        void cancle(SurfaceHolder holder, float screenProp);

        /**
         * 提交
         */
        void confirm();

        void zoom(float zoom, int type);

        /**
         * 设置当前闪关灯模式
         * @param mode 闪关灯
         */
        void flash(String mode);

        /**
         * 赋值Camera错误回调
         * @param errorLisenter 异常回调
         */
        void setErrorLinsenter(ErrorListener errorLisenter);

        /**
         * 打开camera
         */
        void doOpenCamera();

        /**
         * 销毁camera
         */
        void doDestroyCamera();

        /**
         * 注册方向传感器
         * @param mContext 上下文
         */
        void registerSensorManager(Context mContext);

        /**
         * 注销方向传感器
         * @param mContext 上下文
         */
        void unregisterSensorManager(Context mContext);

        /**
         * 设置摄像切换 和 闪关灯 控件
         * @param imgSwitch 摄像切换控件
         * @param imgFlash 闪关灯控件
         */
        void setImageViewSwitchAndFlash(ImageView imgSwitch, ImageView imgFlash);

        /**
         * 设置是否录制中
         * @param b 是否
         */
        void isPreview(boolean b);

        /**
         * 设置视频比特率
         * @param mediaQualityMiddle 比特率
         */
        void setMediaQuality(int mediaQualityMiddle);
    }

    /**
     * View的接口
     */
    interface CameraView {

        /**
         * 生命周期onResume
         */
        void onResume();

        /**
         * 生命周期onPause
         */
        void onPause();

        /**
         * 针对当前状态重新设置状态
         *
         * @param type
         */
        void resetState(int type);

        /**
         * 确认状态
         *
         * @param type
         */
        void confirmState(int type);

        /**
         * 显示图片
         *
         * @param bitmap bitmap
         * @param isVertical 是否铺满
         */
        void showPicture(Bitmap bitmap, boolean isVertical);

        /**
         * 播放视频,用于录制后，在是否确认的界面中，播放视频
         *
         * @param firstFrame @@
         * @param url 路径
         */
        void playVideo(Bitmap firstFrame, String url);

        /**
         * 停止播放视频
         */
        void stopVideo();

        /**
         * 设置提示
         *
         * @param tip 提示文本
         */
        void setTip(String tip);

        /**
         * 处理焦点，View界面显示绿色焦点框
         *
         * @param x 坐标x
         * @param y 坐标y
         * @return 是否在点击范围内
         */
        boolean handlerFoucs(float x, float y);

        /**
         * 获取当前view的状态
         * @return 状态
         */
        int getState();

        /**
         * 设置当前view的状态
         * @param state 状态
         */
        void setState(int state);

        /**
         * 返回比例
         * @return screenProp
         */
        float getScreenProp();

        /**
         * 返回 SurfaceHolder
         * @return SurfaceHolder
         */
        SurfaceHolder getSurfaceHolder();



        /**
         * 设置按钮支持的功能：
         * @param buttonStateBoth
         * {@link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#BUTTON_STATE_ONLY_CLICK 只能拍照
         * @link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#BUTTON_STATE_ONLY_LONGCLICK 只能录像
         * @link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#BUTTON_STATE_BOTH 两者皆可
         * }
         */
        void setFeatures(int buttonStateBoth);

        /**
         * 录制视频比特率
         * @param mediaQualityMiddle 比特率
         * {@link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#MEDIA_QUALITY_HIGH
         * @link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#MEDIA_QUALITY_MIDDLE
         * @link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#MEDIA_QUALITY_LOW
         * @link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#MEDIA_QUALITY_POOR
         * @link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#MEDIA_QUALITY_FUNNY
         * @link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#MEDIA_QUALITY_DESPAIR
         * @link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#MEDIA_QUALITY_SORRY
         * }
         */
        void setMediaQuality(int mediaQualityMiddle);
    }

}
