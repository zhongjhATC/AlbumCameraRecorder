package com.zhongjh.combined;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.zhongjh.albumcamerarecorder.preview.PreviewFragment2;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.displaymedia.apapter.AudioAdapter;
import com.zhongjh.displaymedia.apapter.ImagesAndVideoAdapter;
import com.zhongjh.displaymedia.entity.DisplayMedia;
import com.zhongjh.displaymedia.listener.AbstractDisplayMediaLayoutListener;
import com.zhongjh.displaymedia.listener.DisplayMediaLayoutListener;
import com.zhongjh.displaymedia.widget.DisplayMediaLayout;

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
    DisplayMediaLayout maskProgressLayout;

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
                    DisplayMediaLayout maskProgressLayout,
                    AbstractDisplayMediaLayoutListener listener) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.maskProgressLayout = maskProgressLayout;
        maskProgressLayout.setDisplayMediaLayoutListener(new DisplayMediaLayoutListener() {

            @Override
            public void onItemAudioStartDownload(@NonNull AudioAdapter.VideoHolder holder, @NonNull String url) {
                listener.onItemAudioStartDownload(holder, url);
            }

            @Override
            public void onItemAudioStartUploading(@NonNull DisplayMedia displayMedia, @NonNull AudioAdapter.VideoHolder viewHolder) {
                listener.onItemAudioStartUploading(displayMedia, viewHolder);
            }

            @Override
            public void onAddDataSuccess(@NotNull List<DisplayMedia> displayMedia) {
            }

            @Override
            public void onItemAdd(@NotNull View view, @NotNull DisplayMedia displayMedia, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击Add
                globalSetting.alreadyCount(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
                globalSetting.forResult(requestCode);
                listener.onItemAdd(view, displayMedia, alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            }

            @Override
            public void onItemClick(@NotNull View view, @NotNull DisplayMedia displayMedia) {
                // 点击详情
                if (displayMedia.isImageOrGif() || displayMedia.isVideo()) {
                    // 预览
//                    globalSetting.openPreviewData(activity, requestCode,
//                            maskProgressLayout.getImagesAndVideos(),
//                            maskProgressLayout.getImagesAndVideos().indexOf(multiMediaView));
                }
                listener.onItemClick(view, displayMedia);
            }

            @Override
            public void onItemStartUploading(@NonNull DisplayMedia displayMedia, @NonNull ImagesAndVideoAdapter.PhotoViewHolder viewHolder) {
                listener.onItemStartUploading(displayMedia, viewHolder);
            }

            @Override
            public void onItemClose(@NotNull DisplayMedia displayMedia) {
                listener.onItemClose(displayMedia);
            }

            @Override
            public boolean onItemVideoStartDownload(@NotNull View view, @NotNull DisplayMedia displayMedia) {
                return listener.onItemVideoStartDownload(view, displayMedia);
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
