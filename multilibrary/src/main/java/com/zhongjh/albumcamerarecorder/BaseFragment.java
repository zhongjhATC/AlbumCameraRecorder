package com.zhongjh.albumcamerarecorder;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.zhongjh.albumcamerarecorder.listener.HandleBackInterface;
import com.zhongjh.albumcamerarecorder.utils.HandleBackUtil;

/**
 * 录音、视频、音频的fragment继承于他
 */
public abstract class BaseFragment extends Fragment implements HandleBackInterface {

    @Override
    public boolean onBackPressed() {
        return HandleBackUtil.handleBackPress(this);
    }

}
