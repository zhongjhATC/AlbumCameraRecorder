package com.zhongjh.cameraapp;

import android.app.Activity;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.album.filter.Filter;
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;
import com.zhongjh.cameraapp.databinding.ActivityMainBinding;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;

import java.util.ArrayList;
import java.util.Set;

import gaode.zhongjh.com.common.entity.SaveStrategy;
import gaode.zhongjh.com.common.enums.MimeType;
import gaode.zhongjh.com.common.enums.MultimediaTypes;

/**
 * 配置版
 */
public class MainActivity extends BaseActivity {

    ActivityMainBinding mBinding;

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // 以下为点击事件
        mBinding.mplImageList.setMaskProgressLayoutListener(new MaskProgressLayoutListener() {

            @Override
            public void onItemAdd(View view, MultiMediaView multiMediaView, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击添加
                boolean isOk = getPermissions(false);
                if (isOk)
                    openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            }

            @Override
            public void onItemImage(View view, MultiMediaView multiMediaView) {
                // 点击详情
                if (multiMediaView.getType() == MultimediaTypes.PICTURE) {
                    // 判断如果是图片类型就预览当前所有图片
                    MultiMediaSetting.openPreviewImage(MainActivity.this, (ArrayList) mBinding.mplImageList.getImages(), multiMediaView.getPosition());
                } else if (multiMediaView.getType() == MultimediaTypes.VIDEO) {
                    // 判断如果是视频类型就预览视频
                    MultiMediaSetting.openPreviewVideo(MainActivity.this, (ArrayList) mBinding.mplImageList.getVideos());
                }
            }

            @Override
            public void onItemStartUploading(MultiMediaView multiMediaView) {
                // 开始模拟上传 - 指刚添加后的。这里可以使用你自己的上传事件
                MyTask timer = new MyTask(multiMediaView);
                timers.put(multiMediaView, timer);
                timer.schedule();
            }

            @Override
            public void onItemClose(View view, MultiMediaView multiMediaView) {
                // 停止上传
                timers.get(multiMediaView).cancel();
                timers.remove(multiMediaView);
            }

            @Override
            public void onItemAudioStartDownload(String url) {

            }

            @Override
            public void onItemVideoStartDownload(String url) {

            }

        });
    }

    @Override
    protected MaskProgressLayout getMaskProgressLayout() {
        return mBinding.mplImageList;
    }

    /**
     * @param alreadyImageCount 已经存在显示的几张图片
     * @param alreadyVideoCount 已经存在显示的几个视频
     * @param alreadyAudioCount 已经存在显示的几个音频
     *                          打开窗体
     */
    @Override
    protected void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {

        // region 拍摄有关设置

        CameraSetting cameraSetting = new CameraSetting();
        Set<MimeType> mimeTypeCameras;
        if (mBinding.cbCameraImage.isChecked() && mBinding.cbCameraVideo.isChecked()) {
            mimeTypeCameras = MimeType.ofAll();
            cameraSetting.mimeTypeSet(mimeTypeCameras);// 支持的类型：图片，视频
        } else if (mBinding.cbCameraImage.isChecked()) {
            mimeTypeCameras = MimeType.ofVideo();
            cameraSetting.mimeTypeSet(mimeTypeCameras);// 支持的类型：图片，视频
        } else if (mBinding.cbCameraVideo.isChecked()) {
            mimeTypeCameras = MimeType.ofImage();
            cameraSetting.mimeTypeSet(mimeTypeCameras);// 支持的类型：图片，视频
        }
        cameraSetting.duration(Integer.parseInt(mBinding.etCameraDuration.getText().toString()));// 最长录制时间
        cameraSetting.minDuration(Integer.parseInt(mBinding.etMinCameraDuration.getText().toString()) * 1000);// 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制

        // endregion 拍摄有关设置

        //  region 相册
        AlbumSetting albumSetting = new AlbumSetting(true);
        Set<MimeType> mimeTypeAlbum;
        if (mBinding.cbAlbumImage.isChecked() && mBinding.cbAlbumVideo.isChecked()) {
            mimeTypeAlbum = MimeType.ofAll();
            albumSetting.mimeTypeSet(mimeTypeAlbum);// 支持的类型：图片，视频
        } else if (mBinding.cbAlbumImage.isChecked()) {
            mimeTypeAlbum = MimeType.ofVideo();
            albumSetting.mimeTypeSet(mimeTypeAlbum);// 支持的类型：图片，视频
        } else if (mBinding.cbAlbumVideo.isChecked()) {
            mimeTypeAlbum = MimeType.ofImage();
            albumSetting.mimeTypeSet(mimeTypeAlbum);// 支持的类型：图片，视频
        }

        albumSetting
                .showSingleMediaType(mBinding.cbShowSingleMediaTypeTrue.isChecked()) // 仅仅显示一个多媒体类型
                .countable(mBinding.cbCountableTrue.isChecked())// 是否显示多选图片的数字
                .addFilter(new GifSizeFilter(Integer.parseInt(mBinding.etAddFilterMinWidth.getText().toString()), Integer.parseInt(mBinding.etAddFilterMinHeight.getText().toString()), Integer.parseInt(mBinding.etMaxSizeInBytes.getText().toString()) * Filter.K * Filter.K))// 自定义过滤器
                .gridExpectedSize(dip2px(Integer.parseInt(mBinding.etGridExpectedSize.getText().toString())))// 九宫格大小 ,建议这样使用getResources().getDimensionPixelSize(R.dimen.grid_expected_size)
                .thumbnailScale(0.85f)// 图片缩放比例
                .setOnSelectedListener((uriList, pathList) -> {
                    // 每次选择的事件
                    Log.e("onSelected", "onSelected: pathList=" + pathList);
                })
                .originalEnable(mBinding.cbOriginalEnableTrue.isChecked())// 开启原图
                .maxOriginalSize(Integer.parseInt(mBinding.etMaxOriginalSize.getText().toString())) // 最大原图size,仅当originalEnable为true的时候才有效
                .setOnCheckedListener(isChecked -> {
                    // DO SOMETHING IMMEDIATELY HERE
                    Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                });

        // endregion 相册

        // region 录音机
        RecorderSetting recorderSetting = new RecorderSetting();
        // endregion 录音机

        //  全局
        Set<MimeType> mimeTypes = null;
        if (mBinding.rbAllAll.isChecked())
            mimeTypes = MimeType.ofAll();
        else if (mBinding.rbAllVideo.isChecked())
            mimeTypes = MimeType.ofVideo();
        else if (mBinding.rbAllImage.isChecked())
            mimeTypes = MimeType.ofImage();

        GlobalSetting globalSetting = MultiMediaSetting.from(MainActivity.this).choose(mimeTypes);
        globalSetting.defaultPosition(1);// 默认从第二个开始
        globalSetting.isCutscenes(mBinding.cbIsCutscenes.isChecked());// 启动过场动画，从下往上动画
        if (mBinding.cbAlbum.isChecked())
            // 开启相册功能
            globalSetting.albumSetting(albumSetting);
        if (mBinding.cbCamera.isChecked())
            // 开启拍摄功能
            globalSetting.cameraSetting(cameraSetting);
        if (mBinding.cbRecorder.isChecked())
            // 开启录音功能
            globalSetting.recorderSetting(recorderSetting);

        // 自定义失败信息
        globalSetting.setOnMainListener(errorMessage -> Toast.makeText(MainActivity.this.getApplicationContext(), "自定义失败信息：录音已经达到上限", Toast.LENGTH_LONG).show());

        // 自定义路径，如果其他子权限设置了路径，那么以子权限为准
        if (!TextUtils.isEmpty(mBinding.etAllFile.getText().toString()))
            globalSetting.allStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etAllFile.getText().toString()));// 设置路径和7.0保护路径等等，只影响录制拍照的路径，选择路径还是按照当前选择的路径
        if (!TextUtils.isEmpty(mBinding.etPictureFile.getText().toString()))
            globalSetting.pictureStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etPictureFile.getText().toString()));// 设置路径和7.0保护路径等等，只影响录制拍照的路径，选择路径还是按照当前选择的路径
        if (!TextUtils.isEmpty(mBinding.etAudioFile.getText().toString()))
            globalSetting.audioStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etAudioFile.getText().toString()));// 设置路径和7.0保护路径等等，只影响录制拍照的路径，选择路径还是按照当前选择的路径
        if (!TextUtils.isEmpty(mBinding.etVideoFile.getText().toString()))
            globalSetting.videoStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etVideoFile.getText().toString()));// 设置路径和7.0保护路径等等，只影响录制拍照的路径，选择路径还是按照当前选择的路径

        //                                            .imageEngine(new GlideEngine())  // for glide-V3
        globalSetting.imageEngine(new Glide4Engine())    // for glide-V4
                .maxSelectablePerMediaType(Integer.valueOf(mBinding.etAlbumCount.getText().toString()) - alreadyImageCount,
                        Integer.valueOf(mBinding.etVideoCount.getText().toString()) - alreadyVideoCount,
                        Integer.valueOf(mBinding.etAudioCount.getText().toString()) - alreadyAudioCount)// 最大10张图片或者最大1个视频
                .forResult(REQUEST_CODE_CHOOSE);


    }




}
