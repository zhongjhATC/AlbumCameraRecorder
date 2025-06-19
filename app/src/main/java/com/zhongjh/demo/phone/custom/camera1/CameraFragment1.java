package com.zhongjh.demo.phone.custom.camera1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraManage;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.multimedia.camera.widget.FocusView;
import com.zhongjh.multimedia.camera.widget.PhotoVideoLayout;
import com.zhongjh.multimedia.databinding.FragmentCameraZjhBinding;
import com.zhongjh.multimedia.widget.ImageViewTouch;
import com.zhongjh.multimedia.widget.childclickable.IChildClickableLayout;
import com.zhongjh.demo.R;
import com.zhongjh.demo.databinding.FragmentCamera1Binding;

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
public class CameraFragment1 extends BaseCameraFragment<CameraStateManager, CameraPictureManager, CameraVideoManager> {

    FragmentCamera1Binding mBinding;
    final CameraPictureManager cameraPicturePresenter = new CameraPictureManager(this);
    final CameraVideoManager cameraVideoPresenter = new CameraVideoManager(this);
    final CameraStateManager cameraStateManager = new CameraStateManager(this);

    public static CameraFragment1 newInstance() {
        return new CameraFragment1();
    }

    @Override
    public View setContentView(LayoutInflater inflater, ViewGroup container) {
        mBinding = FragmentCamera1Binding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        // 修改图片,两个调换过来，样式改变，功能不变
        mBinding.pvLayout.getViewHolder().btnConfirm.setFunctionImage(R.drawable.ic_baseline_keyboard_arrow_left_24,
                R.drawable.avd_done_to_stop, R.drawable.avd_stop_to_done);
        mBinding.pvLayout.getViewHolder().btnCancel.setFunctionImage(R.drawable.ic_baseline_done,
                R.drawable.avd_done_to_stop, R.drawable.avd_stop_to_done);

        // 修改副色调颜色
        mBinding.pvLayout.getViewHolder().btnConfirm.setPrimaryVariantColor(R.color.cpb_blue);

        // 定制样式 .确认按钮,修改主色调
        mBinding.pvLayout.getViewHolder().btnConfirm.setPrimaryColor(R.color.cpb_red);
        mBinding.pvLayout.getViewHolder().btnCancel.setPrimaryColor(R.color.cpb_red);
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
