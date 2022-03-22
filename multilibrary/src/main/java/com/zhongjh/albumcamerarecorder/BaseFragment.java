package com.zhongjh.albumcamerarecorder;

import android.view.KeyEvent;

import androidx.fragment.app.Fragment;

import com.zhongjh.albumcamerarecorder.listener.HandleFragmentInterface;
import com.zhongjh.albumcamerarecorder.utils.HandleBackUtil;

import org.jetbrains.annotations.NotNull;

/**
 * 录音、视频、音频的fragment继承于他
 * @author zhongjh
 */
public abstract class BaseFragment extends Fragment implements HandleFragmentInterface {

    @Override
    public boolean onBackPressed() {
        return HandleBackUtil.handleBackPress(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NotNull KeyEvent event) {
        return HandleBackUtil.handleBackPress(this);
    }


}
