package com.zhongjh.albumcamerarecorder.camera.ui.presenter;

import static com.zhongjh.albumcamerarecorder.camera.constants.FlashModels.TYPE_FLASH_AUTO;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.otaliastudios.cameraview.controls.Flash;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.adapter.PhotoAdapter;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.camera.ui.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;
import com.zhongjh.albumcamerarecorder.camera.util.LogUtil;
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.ThreadUtils;
import com.zhongjh.imageedit.ImageEditActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * 这是专门负责图片的有关逻辑
 *
 * @author zhongjh
 * @date 2022/8/22
 */
public class BaseCameraPicturePresenter {

    public BaseCameraPicturePresenter(BaseCameraFragment<BaseCameraPicturePresenter> baseCameraFragment) {
        this.baseCameraFragment = baseCameraFragment;
    }

    protected BaseCameraFragment<BaseCameraPicturePresenter> baseCameraFragment;
    /**
     * 拍照的多图片集合适配器
     */
    private PhotoAdapter photoAdapter;
    /**
     * 照片File,用于后面能随时删除
     */
    private File photoFile;
    /**
     * 编辑后的照片
     */
    private File photoEditFile;
    /**
     * 照片Uri,作用于单图
     */
    private Uri singlePhotoUri;
    /**
     * 图片
     */
    private MediaStoreCompat mPictureMediaStoreCompat;
    /**
     * 延迟拍摄，用于打开闪光灯再拍摄
     */
    private final Handler cameraTakePictureHandler = new Handler(Looper.getMainLooper());
    private final Runnable cameraTakePictureRunnable = new Runnable() {
        @Override
        public void run() {
            if (baseCameraFragment.mCameraSpec.getEnableImageHighDefinition()) {
                baseCameraFragment.getCameraView().takePicture();
            } else {
                baseCameraFragment.getCameraView().takePictureSnapshot();
            }
        }
    };
    /**
     * 一个迁移图片的异步线程
     */
    public ThreadUtils.SimpleTask<ArrayList<LocalFile>> movePictureFileTask;

    /**
     * 初始化有关图片的配置数据
     */
    public void initData() {
        // 设置图片路径
        if (baseCameraFragment.mGlobalSpec.getPictureStrategy() != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mPictureMediaStoreCompat = new MediaStoreCompat(baseCameraFragment.mContext, baseCameraFragment.mGlobalSpec.getPictureStrategy());
        } else {
            // 否则使用全局的
            if (baseCameraFragment.mGlobalSpec.getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mPictureMediaStoreCompat = new MediaStoreCompat(baseCameraFragment.mContext, baseCameraFragment.mGlobalSpec.getSaveStrategy());
            }
        }
    }

    /**
     * 编辑图片事件
     */
    public void initPhotoEditListener() {
        baseCameraFragment.getPhotoVideoLayout().getViewHolder().rlEdit.setOnClickListener(view -> {
            Uri uri = (Uri) view.getTag();
            setPhotoEditFile(baseCameraFragment.mPictureMediaStoreCompat.createFile(0, true, "jpg"));

            Intent intent = new Intent();
            intent.setClass(getContext(), ImageEditActivity.class);
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SCREEN_ORIENTATION, mActivity.getRequestedOrientation());
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, uri);
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SAVE_PATH, mPhotoEditFile.getAbsolutePath());
            mImageEditActivityResult.launch(intent);
        });
    }

    /**
     * 拍照
     */
    public void takePhoto() {
        // 开启才能执行别的事件, 如果已经有分段视频，则不允许拍照了
        if (baseCameraFragment.getCameraView().isOpened() && baseCameraFragment.getVideoTimes().size() <= 0) {
            // 判断数量
            if (photoAdapter.getItemCount() < SelectableUtils.getImageMaxCount()) {
                // 设置不能点击，防止多次点击报错
                baseCameraFragment.getChildClickableLayout().setChildClickable(false);
                // 判断如果是自动闪光灯模式便开启闪光灯
                if (baseCameraFragment.getFlashModel() == TYPE_FLASH_AUTO) {
                    baseCameraFragment.getCameraView().setFlash(Flash.TORCH);
                    // 延迟1秒拍照
                    cameraTakePictureHandler.postDelayed(cameraTakePictureRunnable, 1000);
                } else {
                    cameraTakePictureRunnable.run();
                }
            } else {
                baseCameraFragment.getPhotoVideoLayout().setTipAlphaAnimation(baseCameraFragment.getResources().getString(R.string.z_multi_library_the_camera_limit_has_been_reached));
            }
        }
    }

    /**
     * 生命周期onDestroy
     *
     * @param isCommit 是否提交了数据,如果不是提交则要删除冗余文件
     */
    public void onDestroy(boolean isCommit) {
        LogUtil.i("BaseCameraPicturePresenter destroy");
        if (!isCommit) {
            if (getPhotoFile() != null) {
                // 删除图片
                FileUtil.deleteFile(getPhotoFile());
            }
            // 删除多个图片
            if (getPhotoAdapter().getListData() != null) {
                for (BitmapData bitmapData : getPhotoAdapter().getListData()) {
                    FileUtil.deleteFile(bitmapData.getPath());
                }
            }
        }
        cameraTakePictureHandler.removeCallbacks(cameraTakePictureRunnable);
        if (movePictureFileTask != null) {
            movePictureFileTask.cancel();
        }
    }

    public PhotoAdapter getPhotoAdapter() {
        return photoAdapter;
    }

    public void setPhotoAdapter(PhotoAdapter photoAdapter) {
        this.photoAdapter = photoAdapter;
    }

    public File getPhotoFile() {
        return photoFile;
    }

    public void setPhotoFile(File photoFile) {
        this.photoFile = photoFile;
    }

    public File getPhotoEditFile() {
        return photoEditFile;
    }

    public void setPhotoEditFile(File photoEditFile) {
        this.photoEditFile = photoEditFile;
    }

    public Uri getSinglePhotoUri() {
        return singlePhotoUri;
    }

    public void setSinglePhotoUri(Uri singlePhotoUri) {
        this.singlePhotoUri = singlePhotoUri;
    }
}
