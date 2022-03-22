package com.zhongjh.albumcamerarecorder.utils

import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.zhongjh.albumcamerarecorder.listener.HandleFragmentInterface

/**
 *
 * @author zhongjh
 * @date 2022/3/22
 */
object HandleOnKeyUtil {

    @JvmStatic
    fun handleOnKey(fragment: Fragment, keyCode: Int, event: KeyEvent): Boolean {
        return handleOnKey(fragment.childFragmentManager, keyCode, event)
    }

    @JvmStatic
    fun handleOnKey(fragmentActivity: FragmentActivity, keyCode: Int, event: KeyEvent): Boolean {
        return handleOnKey(fragmentActivity.supportFragmentManager, keyCode, event)
    }

    private fun handleOnKey(fragmentManager: FragmentManager, keyCode: Int, event: KeyEvent)
            : Boolean {
        val fragments = fragmentManager.fragments
        for (i in fragments.indices.reversed()) {
            val child = fragments[i]
            if (isFragmentOnKeyHandled(child, keyCode, event)) {
                return true
            }
        }
        return false
    }

    /**
     * 判断Fragment是否处理了onKeyDown事件
     *
     * @return 如果处理了 onKeyDown事件 则返回 **true**
     */
    private fun isFragmentOnKeyHandled(fragment: Fragment, keyCode: Int, event: KeyEvent): Boolean {
        return (fragment.isVisible
                && fragment.userVisibleHint // for ViewPager
                && fragment is HandleFragmentInterface
                && (fragment as HandleFragmentInterface).onKeyDown(keyCode, event))
    }


}