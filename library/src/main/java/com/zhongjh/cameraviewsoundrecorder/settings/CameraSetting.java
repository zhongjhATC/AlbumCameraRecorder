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
    public CaptureStrategy captureStrategy; // 参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
    public boolean isMultiPicture = false;// 是否一次性可以拍摄多张图片
    public int pictureMaxNumber = 6; // 默认6张图片
    public int collectionType = COLLECTION_UNDEFINED; // 类型: 允许图片或者视频，跟知乎的选择相片共用模式
    public boolean isVideotape = false;// 是否增加录像模式，也就是长按模式

    private static final class InstanceHolder {
        private static final CameraSetting INSTANCE = new CameraSetting();
    }


}
