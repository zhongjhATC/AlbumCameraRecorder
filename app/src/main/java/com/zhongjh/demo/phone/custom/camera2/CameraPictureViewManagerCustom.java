package com.zhongjh.demo.phone.custom.camera2;

import android.widget.Toast;

import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureViewManager;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoViewManager;
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager;

/**
 * 主要演示 BaseCameraPicturePresenter
 * 可以看父类能提供什么方法给你覆写，更详细的注释等请看父类
 *
 * @author zhongjh
 * @date 2022/8/25
 */
public class CameraPictureViewManagerCustom extends CameraPictureViewManager {

    public CameraPictureViewManagerCustom(BaseCameraFragment<? extends CameraStateManager, ? extends CameraPictureViewManager, ? extends CameraVideoViewManager> baseCameraFragment) {
        super(baseCameraFragment);
    }

    @Override
    public void takePhoto() {
        super.takePhoto();
        if (getFragmentRef().get() != null) {
            Toast.makeText(getFragmentRef().get().getMyContext(), "拍照时触发自定义事件！", Toast.LENGTH_SHORT).show();
        }
    }
}
