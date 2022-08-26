package com.zhongjh.cameraapp.phone.customlayout.camera1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.otaliastudios.cameraview.CameraView;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraPicturePresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraVideoPresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.widget.PhotoVideoLayout;
import com.zhongjh.albumcamerarecorder.widget.childclickable.IChildClickableLayout;
import com.zhongjh.cameraapp.R;
import com.zhongjh.cameraapp.databinding.FragmentCamera1Binding;

/**
 * 继承于BaseCameraFragment
 * 1. setContentView 实现自己想要的布局
 * 2. 实现相关view
 * 3. 使用默认的 BaseCameraPicturePresenter、BaseCameraVideoPresenter、CameraStateManagement
 * <p>
 * 该Fragment的例子让我们增加几个view,添加我们想要的事件。
 * 使用 TODO 关键字可搜索相关自定义代码
 *
 * @author zhongjh
 * @date 2022/8/12
 */
public class CameraFragment1 extends BaseCameraFragment<CameraStateManagement, BaseCameraPicturePresenter, BaseCameraVideoPresenter> {

    FragmentCamera1Binding mBinding;
    BaseCameraPicturePresenter cameraPicturePresenter = new BaseCameraPicturePresenter(this);
    BaseCameraVideoPresenter cameraVideoPresenter = new BaseCameraVideoPresenter(this);
    CameraStateManagement cameraStateManagement = new CameraStateManagement(this);

    public static CameraFragment1 newInstance() {
        return new CameraFragment1();
    }

    @Override
    public View setContentView(LayoutInflater inflater, ViewGroup container) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera1,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
    }

    /**
     * TODO
     * 覆写该事件，赋值自定义按钮事件
     */
    @Override
    protected void initListener() {
        super.initListener();
        mBinding.btnCustom.setOnClickListener(v -> Toast.makeText(getMyContext(), "我是自定义的", Toast.LENGTH_SHORT).show());
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
    public CameraView getCameraView() {
        return mBinding.cameraView;
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

}
