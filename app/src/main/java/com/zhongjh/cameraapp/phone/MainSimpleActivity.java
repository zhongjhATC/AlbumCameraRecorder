package com.zhongjh.cameraapp.phone;

import android.app.Activity;
import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.album.filter.BaseFilter;
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;
import com.zhongjh.cameraapp.BaseActivity;
import com.zhongjh.cameraapp.configuration.GifSizeFilter;
import com.zhongjh.cameraapp.configuration.Glide4Engine;
import com.zhongjh.cameraapp.R;
import com.zhongjh.cameraapp.databinding.ActivityMainSimpleBinding;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;

import java.util.ArrayList;

import com.zhongjh.common.entity.SaveStrategy;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.common.enums.MultimediaTypes;

/**
 * 简单版
 *
 * @author zhongjh
 */
public class MainSimpleActivity extends BaseActivity {

    ActivityMainSimpleBinding mBinding;
    private final String TAG = MainSimpleActivity.this.getClass().getSimpleName();

    GlobalSetting mGlobalSetting;

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainSimpleActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_simple);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_simple);

        // 以下为点击事件
        mBinding.mplImageList.setMaskProgressLayoutListener(new MaskProgressLayoutListener() {

            @Override
            public void onItemAdd(View view, MultiMediaView multiMediaView, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击添加
                boolean isOk = getPermissions(false);
                if (isOk) {
                    openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
                }
            }

            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public void onItemClick(View view, MultiMediaView multiMediaView) {
                // 点击详情
                if (multiMediaView.getType() == MultimediaTypes.PICTURE || multiMediaView.getType() == MultimediaTypes.VIDEO) {
                    MultiMediaSetting.openPreviewData(MainSimpleActivity.this, REQUEST_CODE_CHOOSE,
                            mBinding.mplImageList.getImagesAndVideos(),
                            mBinding.mplImageList.getImagesAndVideos().indexOf(multiMediaView));
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
                MyTask myTask = timers.get(multiMediaView);
                if (myTask != null) {
                    myTask.cancel();
                    timers.remove(multiMediaView);
                }
            }

            @Override
            public void onItemAudioStartDownload(View view, String url) {

            }

            @Override
            public boolean onItemVideoStartDownload(View view, MultiMediaView multiMediaView) {
                return false;
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGlobalSetting != null) {
            mGlobalSetting.onDestroy();
        }
    }

    @Override
    protected MaskProgressLayout getMaskProgressLayout() {
        return mBinding.mplImageList;
    }

    @Override
    protected void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        // 支持的类型：图片，视频
        cameraSetting.mimeTypeSet(MimeType.ofAll());

        // 相册
        AlbumSetting albumSetting = new AlbumSetting(false)
                // 支持的类型：图片，视频
                .mimeTypeSet(MimeType.ofAll())
                // 是否显示多选图片的数字
                .countable(true)
                // 自定义过滤器
                .addFilter(new GifSizeFilter(320, 320, 5 * BaseFilter.K * BaseFilter.K))
                // 开启原图
                .originalEnable(true)
                // 最大原图size,仅当originalEnable为true的时候才有效
                .maxOriginalSize(10);

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();

        // 全局
        mGlobalSetting = MultiMediaSetting.from(MainSimpleActivity.this).choose(MimeType.ofAll());

        if (mBinding.cbAlbum.isChecked()) {
            // 开启相册功能
            mGlobalSetting.albumSetting(albumSetting);
        }
        if (mBinding.cbCamera.isChecked()) {
            // 开启拍摄功能
            mGlobalSetting.cameraSetting(cameraSetting);
        }
        if (mBinding.cbRecorder.isChecked()) {
            // 开启录音功能
            mGlobalSetting.recorderSetting(recorderSetting);
        }

        mGlobalSetting
                .setOnMainListener(errorMessage -> {
                    Log.d(TAG, errorMessage);
                    Toast.makeText(MainSimpleActivity.this.getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                })
                // 设置路径和7.0保护路径等等
                .allStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "aabb"))
                // for glide-V4
                .imageEngine(new Glide4Engine())
                // 最大5张图片、最大3个视频、最大1个音频
                .maxSelectablePerMediaType(null,
                        5,
                        3,
                        3,
                        alreadyImageCount,
                        alreadyVideoCount,
                        alreadyAudioCount)
                .forResult(REQUEST_CODE_CHOOSE);
    }

}
