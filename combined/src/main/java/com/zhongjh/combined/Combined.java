package com.zhongjh.combined;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.AbstractMaskProgressLayoutListener;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 协调多个控件之间代码，更加简化代码
 *
 * @author zhongjh
 * @date 2021/9/6
 */
public class Combined {

    Activity activity;
    int requestCode;
    MaskProgressLayout maskProgressLayout;

    /**
     * AlbumCameraRecorder和Mask控件合并
     *
     * @param activity           启动的activity
     * @param requestCode        请求打开AlbumCameraRecorder的Code
     * @param globalSetting      AlbumCameraRecorder
     * @param maskProgressLayout Mask控件
     * @param listener           事件
     */
    public Combined(Activity activity, int requestCode,
                    GlobalSetting globalSetting,
                    MaskProgressLayout maskProgressLayout,
                    AbstractMaskProgressLayoutListener listener) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.maskProgressLayout = maskProgressLayout;
        maskProgressLayout.setMaskProgressLayoutListener(new MaskProgressLayoutListener() {

            @Override
            public void onAddDataSuccess(@NotNull List<MultiMediaView> multiMediaViews) {
            }

            @Override
            public void onItemAdd(@NotNull View view, @NotNull MultiMediaView multiMediaView, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击Add
                globalSetting.alreadyCount(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
                globalSetting.forResult(requestCode);
                listener.onItemAdd(view, multiMediaView, alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            }

            @Override
            public void onItemClick(@NotNull View view, @NotNull MultiMediaView multiMediaView) {
                // 点击详情
                if (multiMediaView.isImageOrGif() || multiMediaView.isVideo()) {
                    // 预览
                    globalSetting.openPreviewData(activity, requestCode,
                            maskProgressLayout.getImagesAndVideos(),
                            maskProgressLayout.getImagesAndVideos().indexOf(multiMediaView));
                }
                listener.onItemClick(view, multiMediaView);
            }

            @Override
            public void onItemStartUploading(@NotNull MultiMediaView multiMediaView) {
                listener.onItemStartUploading(multiMediaView);
            }

            @Override
            public void onItemClose(@NotNull View view, @NotNull MultiMediaView multiMediaView) {
                listener.onItemClose(view, multiMediaView);
            }

            @Override
            public void onItemAudioStartDownload(@NotNull View view, @NotNull String url) {
                listener.onItemAudioStartDownload(view, url);
            }

            @Override
            public boolean onItemVideoStartDownload(@NotNull View view, @NotNull MultiMediaView multiMediaView) {
                return listener.onItemVideoStartDownload(view, multiMediaView);
            }
        });
    }

    /**
     * 封装Activity的onActivityResult
     *
     * @param requestCode 请求码
     * @param data        返回的数据
     */
    public void onActivityResult(int requestCode, Intent data) {
        if (this.requestCode == requestCode) {
            // 如果是在预览界面点击了确定
            if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                // 获取选择的数据
                ArrayList<MultiMedia> selected = MultiMediaSetting.obtainMultiMediaResult(data);
                if (selected == null) {
                    return;
                }
                // 循环判断，如果不存在，则删除
                for (int i = this.maskProgressLayout.getImagesAndVideos().size() - 1; i >= 0; i--) {
                    int k = 0;
                    for (MultiMedia multiMedia : selected) {
                        if (!this.maskProgressLayout.getImagesAndVideos().get(i).equals(multiMedia)) {
                            k++;
                        }
                    }
                    if (k == selected.size()) {
                        // 所有都不符合，则删除
                        this.maskProgressLayout.removePosition(i);
                    }
                }
            } else {
                List<LocalFile> result = MultiMediaSetting.obtainLocalFileResult(data);
                this.maskProgressLayout.addLocalFileStartUpload(result);
            }
        }
    }

}
