package com.zhongjh.combined;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.documentfile.provider.DocumentFile;

import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.recorder.db.RecordingItem;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.AbstractMaskProgressLayoutListener;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;

import java.util.ArrayList;
import java.util.List;

import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.enums.MultimediaTypes;

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
                    GlobalSetting globalSetting, MaskProgressLayout maskProgressLayout,
                    AbstractMaskProgressLayoutListener listener) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.maskProgressLayout = maskProgressLayout;
        maskProgressLayout.setMaskProgressLayoutListener(new MaskProgressLayoutListener() {

            @Override
            public void onItemAdd(View view, MultiMediaView multiMediaView, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击Add
                globalSetting.alreadyCount(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
                globalSetting.forResult(requestCode);
                listener.onItemAdd(view, multiMediaView, alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            }

            @Override
            public void onItemClick(View view, MultiMediaView multiMediaView) {
                // 点击详情
                if (multiMediaView.getType() == MultimediaTypes.PICTURE || multiMediaView.getType() == MultimediaTypes.VIDEO) {
                    // 预览
                    MultiMediaSetting.openPreviewData(activity, requestCode,
                            maskProgressLayout.getImagesAndVideos(),
                            maskProgressLayout.getImagesAndVideos().indexOf(multiMediaView));
                }
                listener.onItemClick(view, multiMediaView);
            }

            @Override
            public void onItemStartUploading(MultiMediaView multiMediaView) {
                listener.onItemStartUploading(multiMediaView);
            }

            @Override
            public void onItemClose(View view, MultiMediaView multiMediaView) {
                listener.onItemClose(view, multiMediaView);
            }

            @Override
            public void onItemAudioStartDownload(View view, String url) {
                listener.onItemAudioStartDownload(view, url);
            }

            @Override
            public boolean onItemVideoStartDownload(View view, MultiMediaView multiMediaView) {
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
                // 请求的预览界面
                Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
                // 获取选择的数据
                ArrayList<MultiMedia> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
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
                return;
            }
            // 获取类型，根据类型设置不同的事情
            switch (MultiMediaSetting.obtainMultimediaType(data)) {
                case MultimediaTypes.PICTURE:
                    // 图片，自动AndroidQ版本以后，使用除了本身app的文件，最好是用uri方式控制
                    List<Uri> path = MultiMediaSetting.obtainResult(data);
                    this.maskProgressLayout.addImagesUriStartUpload(path);
                    break;
                case MultimediaTypes.VIDEO:
                    // 录像
                    List<Uri> videoUris = MultiMediaSetting.obtainResult(data);
                    this.maskProgressLayout.addVideoStartUpload(videoUris);
                    break;
                case MultimediaTypes.AUDIO:
                    // 语音
                    RecordingItem recordingItem = MultiMediaSetting.obtainRecordingItemResult(data);
                    this.maskProgressLayout.addAudioStartUpload(recordingItem.getFilePath(), recordingItem.getLength());
                    break;
                case MultimediaTypes.BLEND:
                    // 混合类型，意思是图片可能跟录像在一起.
                    List<Uri> blends = MultiMediaSetting.obtainResult(data);
                    List<Uri> images = new ArrayList<>();
                    List<Uri> videos = new ArrayList<>();
                    // 循环判断类型
                    for (Uri uri : blends) {
                        DocumentFile documentFile = DocumentFile.fromSingleUri(this.activity.getApplication(), uri);
                        if (documentFile != null && documentFile.getType() != null) {
                            if (documentFile.getType().startsWith("image")) {
                                images.add(uri);
                            } else if (documentFile.getType().startsWith("video")) {
                                videos.add(uri);
                            }
                        }
                    }
                    // 分别上传图片和视频
                    this.maskProgressLayout.addImagesUriStartUpload(images);
                    this.maskProgressLayout.addVideoStartUpload(videos);
                    break;
                default:
                    break;
            }
        }
    }

}
