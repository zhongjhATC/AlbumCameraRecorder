package com.zhongjh.progresslibrary.listener;

import android.view.View;

import com.zhongjh.progresslibrary.entity.MultiMediaView;

/**
 * "抽象接口"
 * @author zhongjh
 * @date 2021/9/7
 */
public class AbstractMaskProgressLayoutListener implements MaskProgressLayoutListener {
    @Override
    public void onItemAdd(View view, MultiMediaView multiMediaView, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {

    }

    @Override
    public void onItemClick(View view, MultiMediaView multiMediaView) {

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
    public boolean onItemVideoStartDownload(View view, MultiMediaView multiMediaView) {
        return false;
    }

}
