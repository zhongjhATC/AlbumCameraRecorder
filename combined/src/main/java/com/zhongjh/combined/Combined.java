package com.zhongjh.combined;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.zhongjh.albumcamerarecorder.preview.PreviewFragment2;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.grid.apapter.PhotoAdapter;
import com.zhongjh.grid.entity.ProgressMedia;
import com.zhongjh.grid.listener.AbstractMaskProgressLayoutListener;
import com.zhongjh.grid.listener.MaskProgressLayoutListener;
import com.zhongjh.grid.widget.MaskProgressLayout;
import com.zhongjh.grid.widget.PlayProgressView;

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
            public void onAddDataSuccess(@NotNull List<ProgressMedia> progressMedia) {
            }

            @Override
            public void onItemAdd(@NotNull View view, @NotNull ProgressMedia progressMedia, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击Add
                globalSetting.alreadyCount(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
                globalSetting.forResult(requestCode);
                listener.onItemAdd(view, progressMedia, alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            }

            @Override
            public void onItemClick(@NotNull View view, @NotNull ProgressMedia progressMedia) {
                // 点击详情
                if (progressMedia.isImageOrGif() || progressMedia.isVideo()) {
                    // 预览
//                    globalSetting.openPreviewData(activity, requestCode,
//                            maskProgressLayout.getImagesAndVideos(),
//                            maskProgressLayout.getImagesAndVideos().indexOf(multiMediaView));
                }
                listener.onItemClick(view, progressMedia);
            }

            @Override
            public void onItemAudioStartUploading(@NonNull ProgressMedia progressMedia, @NonNull PlayProgressView playProgressView) {
                listener.onItemAudioStartUploading(progressMedia, playProgressView);
            }

            @Override
            public void onItemStartUploading(@NonNull ProgressMedia progressMedia, @NonNull PhotoAdapter.PhotoViewHolder viewHolder) {
                listener.onItemStartUploading(progressMedia, viewHolder);
            }

            @Override
            public void onItemClose(@NotNull ProgressMedia progressMedia) {
                listener.onItemClose(progressMedia);
            }

            @Override
            public void onItemAudioStartDownload(@NotNull View view, @NotNull String url) {
                listener.onItemAudioStartDownload(view, url);
            }

            @Override
            public boolean onItemVideoStartDownload(@NotNull View view, @NotNull ProgressMedia progressMedia) {
                return listener.onItemVideoStartDownload(view, progressMedia);
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
            if (data.getBooleanExtra(PreviewFragment2.EXTRA_RESULT_APPLY, false)) {
                // 获取选择的数据
                ArrayList<LocalMedia> selected = MultiMediaSetting.obtainLocalMediaResult(data);
                if (selected == null) {
                    return;
                }
                // 循环判断，如果不存在，则删除
                for (int i = this.maskProgressLayout.getImagesAndVideos().size() - 1; i >= 0; i--) {
                    int k = 0;
                    for (LocalMedia localMedia : selected) {
                        if (!this.maskProgressLayout.getImagesAndVideos().get(i).equals(localMedia)) {
                            k++;
                        }
                    }
                    if (k == selected.size()) {
                        // 所有都不符合，则删除
                        this.maskProgressLayout.removePosition(i);
                    }
                }
            } else {
                ArrayList<LocalMedia> result = MultiMediaSetting.obtainLocalMediaResult(data);
                this.maskProgressLayout.addLocalFileStartUpload(result);
            }
        }
    }

}
