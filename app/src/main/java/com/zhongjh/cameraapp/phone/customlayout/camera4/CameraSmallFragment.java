package com.zhongjh.cameraapp.phone.customlayout.camera4;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.CameraManage;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.albumcamerarecorder.camera.widget.PhotoVideoLayout;
import com.zhongjh.albumcamerarecorder.widget.childclickable.IChildClickableLayout;
import com.zhongjh.cameraapp.databinding.FragmentCameraSmallBinding;

/**
 * 继承于BaseCameraFragment
 * 1. setContentView 实现自己想要的布局
 * 2. 实现相关view
 * 3. 使用默认的 BaseCameraPicturePresenter、BaseCameraVideoPresenter、CameraStateManagement
 * <p>
 * 该Fragment的例子让我们修改核心View，改成自己想要的样子
 * 使用 TODO 关键字可搜索相关自定义代码
 *
 * @author zhongjh
 * @date 2022/8/12
 */
public class CameraSmallFragment extends BaseCameraFragment<CameraStateManager, CameraPictureManager, CameraVideoManager> {

    FragmentCameraSmallBinding mBinding;
    CameraPictureManager cameraPicturePresenter = new CameraPictureManager(this);
    CameraVideoManager cameraVideoPresenter = new CameraVideoManager(this);
    CameraStateManager cameraStateManager = new CameraStateManager(this);

    public static CameraSmallFragment newInstance() {
        return new CameraSmallFragment();
    }

//    @Override
//    public View setContentView(LayoutInflater inflater, ViewGroup container) {
//        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera_small, container, false);
//        return mBinding.getRoot();
//    }

    @Override
    public View setContentView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        mBinding.pvLayout.getViewHolder().tvTip.setTextSize(10);
    }

    /**
     * TODO
     * 覆写该事件，赋值自定义按钮事件
     */
    @Override
    protected void initListener() {
        super.initListener();
    }

    @NonNull
    @Override
    public IChildClickableLayout getChildClickableLayout() {
        return mBinding.rlMain;
    }

    @Nullable
    @Override
    public View getTopView() {
        return mBinding.clMenu;
    }

    @Override
    public RecyclerView getRecyclerViewPhoto() {
        return mBinding.rlPhoto;
    }

    @Nullable
    @Override
    public View[] getMultiplePhotoView() {
        return new View[]{mBinding.vLine1, mBinding.vLine2};
    }

    @NonNull
    @Override
    public PhotoVideoLayout getPhotoVideoLayout() {
        return mBinding.pvLayout;
    }

    @NonNull
    @Override
    public com.zhongjh.albumcamerarecorder.widget.ImageViewTouch getSinglePhotoView() {
        return mBinding.imgPhoto;
    }

    @Nullable
    @Override
    public View getCloseView() {
        return mBinding.imgClose;
    }

    @Nullable
    @Override
    public ImageView getFlashView() {
        return mBinding.imgFlash;
    }

    @Nullable
    @Override
    public ImageView getSwitchView() {
        return mBinding.imgSwitch;
    }

    @NonNull
    @Override
    public CameraManage getCameraManage() {
        return null;
    }

    @NonNull
    @Override
    public CameraStateManager getCameraStateManager() {
        return cameraStateManager;
    }

    @NonNull
    @Override
    public CameraPictureManager getCameraPictureManager() {
        return cameraPicturePresenter;
    }

    @NonNull
    @Override
    public CameraVideoManager getCameraVideoManager() {
        return cameraVideoPresenter;
    }

}
