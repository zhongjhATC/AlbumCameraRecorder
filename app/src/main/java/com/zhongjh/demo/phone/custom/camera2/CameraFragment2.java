package com.zhongjh.demo.phone.custom.camera2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.demo.R;
import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureViewManager;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoViewManager;
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.multimedia.camera.widget.FocusView;
import com.zhongjh.multimedia.camera.widget.PhotoVideoLayout;
import com.zhongjh.multimedia.widget.ImageViewTouch;
import com.zhongjh.multimedia.widget.childclickable.ChildClickableRelativeLayout;
import com.zhongjh.multimedia.widget.childclickable.IChildClickableLayout;

/**
 * 继承于BaseCameraFragment
 * 主要演示 BaseCameraPicturePresenter
 * <p>
 * 使用 TODO 关键字可搜索相关自定义代码
 *
 * @author zhongjh
 * @date 2022/8/12
 */
public class CameraFragment2 extends BaseCameraFragment<CameraStateManager, CameraPictureViewManager, CameraVideoViewManager> {

    ViewHolder mViewHolder;
    final CameraPictureViewManagerCustom cameraPicturePresenter = new CameraPictureViewManagerCustom(this);
    final CameraVideoViewManager cameraVideoPresenter = new CameraVideoViewManager(this);
    final CameraStateManager cameraStateManager = new CameraStateManager(this);

    public static CameraFragment2 newInstance() {
        return new CameraFragment2();
    }

    @Override
    public View setContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(com.zhongjh.multimedia.R.layout.fragment_camera_zjh, container, false);
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        mViewHolder = new ViewHolder(view);
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

    @NonNull
    @Override
    public PreviewView getPreviewView() {
        return mViewHolder.previewView;
    }

    @NonNull
    @Override
    public FocusView getFocusView() {
        return mViewHolder.focusView;
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
    public CameraStateManager getCameraStateManager() {
        return cameraStateManager;
    }

    @NonNull
    @Override
    public CameraPictureViewManager getCameraPictureViewManager() {
        return cameraPicturePresenter;
    }

    @NonNull
    @Override
    public CameraVideoViewManager getCameraVideoViewManager() {
        return cameraVideoPresenter;
    }

    public static class ViewHolder {

        final View rootView;
        final ChildClickableRelativeLayout rlMain;
        final PreviewView previewView;
        final FocusView focusView;
        final ImageViewTouch imgPhoto;
        final ImageView imgFlash;
        final ImageView imgSwitch;
        final PhotoVideoLayout pvLayout;
        final RecyclerView rlPhoto;
        final View vLine1;
        final View vLine2;
        final ImageView imgClose;
        final ConstraintLayout clMenu;
        final AppCompatButton btnCustom;

        ViewHolder(View rootView) {
            this.rootView = rootView;
            this.rlMain = rootView.findViewById(R.id.rlMain);
            this.previewView = rootView.findViewById(R.id.previewView);
            this.focusView = rootView.findViewById(R.id.focusView);
            this.imgPhoto = rootView.findViewById(R.id.imgPhoto);
            this.imgFlash = rootView.findViewById(R.id.imgFlash);
            this.imgSwitch = rootView.findViewById(R.id.imgSwitch);
            this.pvLayout = rootView.findViewById(R.id.pvLayout);
            this.rlPhoto = rootView.findViewById(R.id.rlPhoto);
            this.vLine1 = rootView.findViewById(R.id.vLine1);
            this.vLine2 = rootView.findViewById(R.id.vLine2);
            this.imgClose = rootView.findViewById(R.id.imgClose);
            this.clMenu = rootView.findViewById(R.id.clMenu);
            this.btnCustom = rootView.findViewById(R.id.btnCustom);
        }
    }

}
