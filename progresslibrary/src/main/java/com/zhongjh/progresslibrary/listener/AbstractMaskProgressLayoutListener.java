package com.zhongjh.progresslibrary.listener;

import android.view.View;

import com.zhongjh.progresslibrary.entity.MultiMediaView;

/**
 * @author zhongjh
 * @date 2021/9/6
 */
public class AbstractMaskProgressLayoutListener implements MaskProgressLayoutListener {

    @Override
    public void onItemAdd(View view, MultiMediaView multiMediaView, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {

    }

    @Override
    public void onItemImage(View view, MultiMediaView multiMediaView) {

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

}
