package com.zhongjh.multimedia

import android.view.KeyEvent
import androidx.fragment.app.Fragment
import com.zhongjh.multimedia.listener.HandleFragmentInterface
import com.zhongjh.multimedia.utils.HandleBackUtil.handleBackPress

/**
 * 录音、视频、音频的fragment继承于他
 * @author zhongjh
 */
abstract class BaseFragment : Fragment(), HandleFragmentInterface {
    override fun onBackPressed(): Boolean {
        return handleBackPress(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return handleBackPress(this)
    }
}
