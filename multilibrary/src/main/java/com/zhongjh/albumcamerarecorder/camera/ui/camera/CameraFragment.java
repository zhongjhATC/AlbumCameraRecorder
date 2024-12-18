package com.zhongjh.albumcamerarecorder.camera.ui.camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraManage;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.albumcamerarecorder.camera.widget.FocusView;
import com.zhongjh.albumcamerarecorder.camera.widget.PhotoVideoLayout;
import com.zhongjh.albumcamerarecorder.widget.childclickable.ChildClickableRelativeLayout;
import com.zhongjh.albumcamerarecorder.widget.childclickable.IChildClickableLayout;

/**
 * 继承于BaseCameraFragment
 *
 * @author zhongjh
 * @date 2022/8/12
 */
public class CameraFragment extends BaseCameraFragment<CameraStateManager, CameraPictureManager, CameraVideoManager> {

    CameraPictureManager cameraPicturePresenter = new CameraPictureManager(this);
    CameraVideoManager cameraVideoPresenter = new CameraVideoManager(this);
    CameraStateManager cameraStateManager = new CameraStateManager(this);
    ViewHolder mViewHolder;
    CameraManage cameraManage;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View setContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_camera_zjh, container, false);
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        mViewHolder = new ViewHolder(view);
        cameraManage = new CameraManage(getMainActivity(), mViewHolder, this);
    }

    @NonNull
    @Override
    public IChildClickableLayout getChildClickableLayout() {
        return mViewHolder.rlMain;
    }

    @Nullable
    @Override
    public View getTopView() {
        return mViewHolder.clMenu;
    }

    @Override
    public RecyclerView getRecyclerViewPhoto() {
        return mViewHolder.rlPhoto;
    }

    @Nullable
    @Override
    public View[] getMultiplePhotoView() {
        return new View[]{mViewHolder.vLine1, mViewHolder.vLine2};
    }

    @NonNull
    @Override
    public PhotoVideoLayout getPhotoVideoLayout() {
        return mViewHolder.pvLayout;
    }

    @NonNull
    @Override
    public com.zhongjh.albumcamerarecorder.widget.ImageViewTouch getSinglePhotoView() {
        return mViewHolder.imgPhoto;
    }

    @Nullable
    @Override
    public View getCloseView() {
        return mViewHolder.imgClose;
    }

    @Nullable
    @Override
    public ImageView getFlashView() {
        return mViewHolder.imgFlash;
    }

    @Nullable
    @Override
    public ImageView getSwitchView() {
        return mViewHolder.imgSwitch;
    }

    @NonNull
    @Override
    public CameraManage getCameraManage() {
        return cameraManage;
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

    public static class ViewHolder {

        View rootView;
        ChildClickableRelativeLayout rlMain;
        com.zhongjh.albumcamerarecorder.widget.ImageViewTouch imgPhoto;
        ImageView imgFlash;
        ImageView imgSwitch;
        public PhotoVideoLayout pvLayout;
        RecyclerView rlPhoto;
        View vLine1;
        View vLine2;
        ImageView imgClose;
        ConstraintLayout clMenu;
        public PreviewView previewView;
        public FocusView focusView;

        ViewHolder(View rootView) {
            this.rootView = rootView;
            this.rlMain = rootView.findViewById(R.id.rlMain);
            this.imgPhoto = rootView.findViewById(R.id.imgPhoto);
            this.imgFlash = rootView.findViewById(R.id.imgFlash);
            this.imgSwitch = rootView.findViewById(R.id.imgSwitch);
            this.pvLayout = rootView.findViewById(R.id.pvLayout);
            this.rlPhoto = rootView.findViewById(R.id.rlPhoto);
            this.vLine1 = rootView.findViewById(R.id.vLine1);
            this.vLine2 = rootView.findViewById(R.id.vLine2);
            this.imgClose = rootView.findViewById(R.id.imgClose);
            this.clMenu = rootView.findViewById(R.id.clMenu);
            this.previewView = rootView.findViewById(R.id.previewView);
            this.focusView = rootView.findViewById(R.id.focusView);
        }
    }

}
