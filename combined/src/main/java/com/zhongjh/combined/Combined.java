package com.zhongjh.combined;

import android.app.Activity;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;

import java.util.ArrayList;

import gaode.zhongjh.com.common.enums.MultimediaTypes;

/**
 * 协调多个控件之间代码，更加简化代码
 *
 * @author zhongjh
 * @date 2021/9/6
 */
public class Combined {

    /**
     * AlbumCameraRecorder和Mask控件合并
     *
     * @param activity           启动的activity
     * @param globalSetting      AlbumCameraRecorder
     * @param maskProgressLayout Mask控件
     */
    public static void combinedAlbumCameraRecorderAndMask(Activity activity, GlobalSetting globalSetting, MaskProgressLayout maskProgressLayout) {
        maskProgressLayout.setMaskProgressLayoutListener(new MaskProgressLayoutListener() {
            @Override
            public void onItemAdd(View view, MultiMediaView multiMediaView, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                globalSetting.forResult(1);
            }

            @Override
            public void onItemImage(View view, MultiMediaView multiMediaView) {
                // 点击详情
                if (multiMediaView.getType() == MultimediaTypes.PICTURE) {
                    // 判断如果是图片类型就预览当前所有图片
                    MultiMediaSetting.openPreviewImage(activity, (ArrayList) maskProgressLayout.getImages(), multiMediaView.getPosition());
                } else if (multiMediaView.getType() == MultimediaTypes.VIDEO) {
                    // 判断如果是视频类型就预览视频
                    MultiMediaSetting.openPreviewVideo(activity, (ArrayList) maskProgressLayout.getVideos(), multiMediaView.getPosition());
                }
            }

            @Override
            public void onItemStartUploading(MultiMediaView multiMediaView) {

            }

            @Override
            public void onItemClose(View view, MultiMediaView multiMediaView) {

            }

            @Override
            public void onItemAudioStartDownload(View view, String url) {

            }

            @Override
            public void onItemVideoStartDownload(String url) {

            }
        });
    }

    private void dialog(Activity activity) {
        // 构造器
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // 设置标题
        builder.setTitle("提示");
        // 设置内容
        builder.setMessage("是否确认退出?");
        // 设置确定按钮
        builder.setPositiveButton("好的", (dialog, which) -> {
            // 关闭dialog
            dialog.dismiss();
        });
        // 设置取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.dismiss();
        });
        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }
}
