package com.zhongjh.cameraapp.phone.customlayout.camera2;

import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraPicturePresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraVideoPresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManagement;

/**
 * 主要演示 BaseCameraPicturePresenter
 * 可以看父类能提供什么方法给你覆写，更详细的注释等请看父类
 *
 * @author zhongjh
 * @date 2022/8/25
 */
public class CameraPicturePresenter extends BaseCameraPicturePresenter {

    public CameraPicturePresenter(BaseCameraFragment<? extends CameraStateManagement, ? extends BaseCameraPicturePresenter, ? extends BaseCameraVideoPresenter> baseCameraFragment) {
        super(baseCameraFragment);
    }

    @Override
    public void takePhoto() {
        super.takePhoto();
        Toast.makeText(baseCameraFragment.getMyContext(), "拍照时触发自定义事件！", Toast.LENGTH_SHORT).show();
    }
}
