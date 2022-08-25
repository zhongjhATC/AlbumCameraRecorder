package com.zhongjh.cameraapp.phone.customlayout.camera1;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.otaliastudios.cameraview.CameraView;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraPicturePresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraVideoPresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.widget.PhotoVideoLayout;
import com.zhongjh.albumcamerarecorder.widget.childclickable.ChildClickableRelativeLayout;
import com.zhongjh.albumcamerarecorder.widget.childclickable.IChildClickableLayout;
import com.zhongjh.cameraapp.R;

/**
 * 继承于BaseCameraFragment
 * 1. setContentView 实现自己想要的布局
 * 2. 实现相关view
 * 3. 使用默认的 BaseCameraPicturePresenter、BaseCameraVideoPresenter、CameraStateManagement
 *
 * 该Fragment的例子让我们增加几个view,添加我们想要的事件。
 * 使用 TODO 关键字可搜索相关自定义代码
 *
 * @author zhongjh
 * @date 2022/8/12
 */
public class CameraFragment1 extends BaseCameraFragment<CameraStateManagement, BaseCameraPicturePresenter, BaseCameraVideoPresenter> {

    ViewHolder mViewHolder;
    BaseCameraPicturePresenter cameraPicturePresenter = new BaseCameraPicturePresenter(this);
    BaseCameraVideoPresenter cameraVideoPresenter = new BaseCameraVideoPresenter(this);
    CameraStateManagement cameraStateManagement = new CameraStateManagement(this);

    public static CameraFragment1 newInstance() {
        return new CameraFragment1();
    }

    @Override
    public int setContentView() {
        return R.layout.fragment_camera1;
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        mViewHolder = new ViewHolder(view);
    }

    /**
     * TODO
     * 覆写该事件，赋值自定义按钮事件
     */
    @Override
    protected void initListener() {
        super.initListener();
        mViewHolder.btnCustom.setOnClickListener(v -> Toast.makeText(getMyContext(), "我是自定义的", Toast.LENGTH_SHORT).show());
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
    public CameraView getCameraView() {
        return mViewHolder.cameraView;
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
    public CameraStateManagement getCameraStateManagement() {
        return cameraStateManagement;
    }

    @NonNull
    @Override
    public BaseCameraPicturePresenter getCameraPicturePresenter() {
        return cameraPicturePresenter;
    }

    @NonNull
    @Override
    public BaseCameraVideoPresenter getCameraVideoPresenter() {
        return cameraVideoPresenter;
    }

    public static class ViewHolder {

        View rootView;
        ChildClickableRelativeLayout rlMain;
        com.zhongjh.albumcamerarecorder.widget.ImageViewTouch imgPhoto;
        ImageView imgFlash;
        ImageView imgSwitch;
        PhotoVideoLayout pvLayout;
        RecyclerView rlPhoto;
        View vLine1;
        View vLine2;
        ImageView imgClose;
        CameraView cameraView;
        ConstraintLayout clMenu;
        AppCompatButton btnCustom;

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
            this.cameraView = rootView.findViewById(R.id.cameraView);
            this.clMenu = rootView.findViewById(R.id.clMenu);
            this.btnCustom = rootView.findViewById(R.id.btnCustom);
        }
    }

}
