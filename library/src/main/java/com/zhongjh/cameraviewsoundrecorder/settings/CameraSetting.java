package com.zhongjh.cameraviewsoundrecorder.settings;

import com.zhongjh.cameraviewsoundrecorder.camera.listener.CaptureListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.CloseListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.ErrorListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.OperaeCameraListener;
import com.zhongjh.cameraviewsoundrecorder.camera.widget.cameralayout.CameraContact;

import static com.zhongjh.cameraviewsoundrecorder.album.model.SelectedItemCollection.COLLECTION_UNDEFINED;

public class CameraSetting {

    private CameraSetting() {
    }

    public static CameraSetting getInstance() {
        return InstanceHolder.INSTANCE;
    }

    // region 属性

    public String videoPath;            // 保存视频文件的路径
    public String photoPath;            // 保存图片文件的路径
    private boolean mIsMultiPicture = false;// 是否一次性可以拍摄多张图片
    private int PictureMaxNumber = 6; // 默认6张图片
    private int mCollectionType = COLLECTION_UNDEFINED; // 类型: 允许图片或者视频，跟知乎的选择相片共用模式

    public boolean isVideotape = false;// 是否增加录像模式，也就是长按模式

    // 回调监听
    private ErrorListener mErrorLisenter;
    private CloseListener mCloseListener;           // 退出当前Activity的按钮监听
    private ClickOrLongListener mClickOrLongListener; // 按钮的监听
    private OperaeCameraListener mOperaeCameraListener;         // 确认跟返回的监听
    private CaptureListener mCaptureListener;       // 拍摄后操作图片的事件


    // 赋值Camera错误回调
    public void setErrorLisenter(ErrorListener errorLisenter) {
        this.mErrorLisenter = errorLisenter;
        mCameraPresenter.setErrorLinsenter(errorLisenter);
    }

    // 退出当前Activity的按钮监听
    public void setCloseListener(CloseListener closeListener) {
        this.mCloseListener = closeListener;
    }

    // 核心按钮事件
    public void setPhotoVideoListener(ClickOrLongListener clickOrLongListener) {
        this.mClickOrLongListener = clickOrLongListener;
    }

    // 确认跟返回的监听
    public void setOperaeCameraListener(OperaeCameraListener operaeCameraListener) {
        this.mOperaeCameraListener = operaeCameraListener;
    }

    // 拍摄后操作图片的事件
    public void setCaptureListener(CaptureListener captureListener) {
        this.mCaptureListener = captureListener;
    }

    /**
     * 设置是否一次性拍摄多张图片
     *
     * @param b 是否
     */
    public void isMultiPicture(boolean b) {
        this.mIsMultiPicture = b;
    }


    /**
     * 设置类型 允许图片或者视频，跟知乎的选择相片共用模式
     *
     * @param mCollectionType 类型
     */
    public void setCollectionType(int mCollectionType) {
        this.mCollectionType = mCollectionType;
    }

    /**
     * 如果 {@link CameraContact.CameraView#isMultiPicture } 生效，那么该方法才能生效
     * @param i 允许最多多少张图片
     */
    public void setPictureMaxNumber(int i) {
        mCameraPresenter.setPictureMaxNumber(i);
    }

    /**
     * 设置视频保存路径
     * @param saveVideoPath 路径文本
     */
    public void setSaveVideoPath(String saveVideoPath){

    }

    // endregion

    private static final class InstanceHolder {
        private static final CameraSetting INSTANCE = new CameraSetting();
    }



}
