package com.zhongjh.cameraapp.phone.customlayout.camera3;

import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManager;

/**
 * 主要演示 BaseCameraPicturePresenter
 * 可以看父类能提供什么方法给你覆写，更详细的注释等请看父类
 *
 * @author zhongjh
 * @date 2022/8/25
 */
public class CameraPictureManagerCustom extends CameraPictureManager {

    public CameraPictureManagerCustom(BaseCameraFragment<? extends CameraStateManager, ? extends CameraPictureManager, ? extends CameraVideoManager> baseCameraFragment) {
        super(baseCameraFragment);
    }

    @Override
    public void takePhoto() {
        super.takePhoto();
//        TextView watermark = ((ViewGroup) baseCameraFragment.getCameraManage()).findViewById(R.id.tvWatermark);
//        watermark.setText("自定义水印");
        Toast.makeText(baseCameraFragment.getMyContext(), "自定义水印", Toast.LENGTH_SHORT).show();
    }
}
