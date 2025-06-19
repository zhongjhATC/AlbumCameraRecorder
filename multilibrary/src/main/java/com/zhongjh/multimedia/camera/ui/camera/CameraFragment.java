package com.zhongjh.multimedia.camera.ui.camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.multimedia.R;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraManage;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.multimedia.camera.widget.FocusView;
import com.zhongjh.multimedia.widget.ImageViewTouch;
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.multimedia.camera.widget.PhotoVideoLayout;
import com.zhongjh.multimedia.widget.childclickable.ChildClickableRelativeLayout;
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

        mViewHolder.previewView.getPreviewStreamState().observe(getMainActivity(), streamState -> {

        });
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
    public ImageViewTouch getSinglePhotoView() {
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

        final View rootView;
        final ChildClickableRelativeLayout rlMain;
        final ImageViewTouch imgPhoto;
        final ImageView imgFlash;
        final ImageView imgSwitch;
        public final PhotoVideoLayout pvLayout;
        final RecyclerView rlPhoto;
        final View vLine1;
        final View vLine2;
        final ImageView imgClose;
        final ConstraintLayout clMenu;
        public final PreviewView previewView;
        public final FocusView focusView;

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
