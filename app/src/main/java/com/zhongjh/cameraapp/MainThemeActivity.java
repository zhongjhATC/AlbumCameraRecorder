package com.zhongjh.cameraapp;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.album.filter.Filter;
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;
import com.zhongjh.cameraapp.databinding.ActivityMainThemeBinding;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;

import java.util.ArrayList;

import gaode.zhongjh.com.common.entity.SaveStrategy;
import gaode.zhongjh.com.common.enums.MimeType;
import gaode.zhongjh.com.common.enums.MultimediaTypes;

/**
 * 多种样式版
 */
public class MainThemeActivity extends BaseActivity {

    ActivityMainThemeBinding mBinding;

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainThemeActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_theme);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_theme);

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
                    MultiMediaSetting.openPreviewImage(MainThemeActivity.this, (ArrayList) mBinding.mplImageList.getImages(), multiMediaView.getPosition());
                } else if (multiMediaView.getType() == MultimediaTypes.VIDEO) {
                    // 判断如果是视频类型就预览视频
                    MultiMediaSetting.openPreviewVideo(MainThemeActivity.this, (ArrayList) mBinding.mplImageList.getVideos());
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

    @Override
    protected void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        cameraSetting.mimeTypeSet(MimeType.ofAll());// 支持的类型：图片，视频
        // 相册
        AlbumSetting albumSetting = new AlbumSetting(true)
                .mimeTypeSet(MimeType.ofAll())// 支持的类型：图片，视频
                .countable(true)// 是否显示多选图片的数字
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))// 自定义过滤器
                .originalEnable(true)// 开启原图
                .maxOriginalSize(10); // 最大原图size,仅当originalEnable为true的时候才有效

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();
        // 全局
        GlobalSetting globalSetting = MultiMediaSetting.from(MainThemeActivity.this).choose(MimeType.ofAll());

        // 样式选择
        if (mBinding.rbBlue.isChecked())
            globalSetting.theme(R.style.AppTheme_Blue);
        if (mBinding.rbBlack.isChecked())
            globalSetting.theme(R.style.AppTheme_Dracula);


        globalSetting.albumSetting(albumSetting);
        globalSetting.cameraSetting(cameraSetting);
        globalSetting.recorderSetting(recorderSetting);
        globalSetting
                .setOnMainListener(errorMessage -> Toast.makeText(MainThemeActivity.this.getApplicationContext(), "自定义失败信息：录音已经达到上限", Toast.LENGTH_LONG).show())
                .allStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/test"))// 设置路径和7.0保护路径等等
                .imageEngine(new Glide4Engine())    // for glide-V4
                .maxSelectablePerMediaType(5 - alreadyImageCount, 1 - alreadyVideoCount, 1 - alreadyAudioCount)// 最大10张图片或者最大1个视频
                .forResult(REQUEST_CODE_CHOOSE);
    }

}
