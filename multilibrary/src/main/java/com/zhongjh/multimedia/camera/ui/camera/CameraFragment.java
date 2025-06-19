package com.zhongjh.multimedia.camera.ui.camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.multimedia.camera.widget.FocusView;
import com.zhongjh.multimedia.databinding.FragmentCameraZjhBinding;
import com.zhongjh.multimedia.widget.ImageViewTouch;
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.multimedia.camera.widget.PhotoVideoLayout;
import com.zhongjh.multimedia.widget.childclickable.IChildClickableLayout;

/**
 * 继承于BaseCameraFragment
 *
 * @author zhongjh
 * @date 2022/8/12
 */
public class CameraFragment extends BaseCameraFragment<CameraStateManager, CameraPictureManager, CameraVideoManager> {

    final CameraPictureManager cameraPicturePresenter = new CameraPictureManager(this);
    final CameraVideoManager cameraVideoPresenter = new CameraVideoManager(this);
    final CameraStateManager cameraStateManager = new CameraStateManager(this);
    FragmentCameraZjhBinding mBinding;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View setContentView(LayoutInflater inflater, ViewGroup container) {
        mBinding = FragmentCameraZjhBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        mBinding.previewView.getPreviewStreamState().observe(getMainActivity(), streamState -> {

        });
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

    @NonNull
    @Override
    public PreviewView getPreviewView() {
        return mBinding.previewView;
    }

    @NonNull
    @Override
    public FocusView getFocusView() {
        return mBinding.focusView;
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
    public ImageViewTouch getSinglePhotoView() {
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
