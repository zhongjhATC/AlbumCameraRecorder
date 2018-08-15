package com.zhongjh.cameraviewsoundrecorder.camera;

import android.graphics.Bitmap;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.zhongjh.cameraviewsoundrecorder.camera.other.CameraCallback;
import com.zhongjh.cameraviewsoundrecorder.listener.ErrorListener;

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

        void foucs(float x, float y, CameraCallback.FocusCallback callback);

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

        void cancle(SurfaceHolder holder, float screenProp);

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
    }

    /**
     * View的接口
     */
    interface CameraView {

        /**
         * 重新设置状态
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
         * @param tip
         */
        void setTip(String tip);

        /**
         * 启动预览回调
         */
        void startPreviewCallback();

        /**
         * 看看干嘛的@
         *
         * @param x
         * @param y
         * @return
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

    }

}
