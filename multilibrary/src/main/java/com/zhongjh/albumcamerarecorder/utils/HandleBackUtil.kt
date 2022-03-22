package com.zhongjh.albumcamerarecorder.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.zhongjh.albumcamerarecorder.listener.HandleFragmentInterface

/**
 * 处理fragment的回退事件
 * @author zhongjh
 */
object HandleBackUtil {

    @JvmStatic
    fun handleBackPress(fragment: Fragment): Boolean {
        return handleBackPress(fragment.childFragmentManager)
    }

    @JvmStatic
    fun handleBackPress(fragmentActivity: FragmentActivity): Boolean {
        return handleBackPress(fragmentActivity.supportFragmentManager)
    }

    /**
     * 将back事件分发给 FragmentManager 中管理的子Fragment，如果该 FragmentManager 中的所有Fragment都
     * 没有处理back事件，则尝试 FragmentManager.popBackStack()
     *
     * @return 如果处理了back键则返回 **true**
     * @see .handleBackPress
     */
    private fun handleBackPress(fragmentManager: FragmentManager): Boolean {
        val fragments = fragmentManager.fragments
        for (i in fragments.indices.reversed()) {
            val child = fragments[i]
            if (isFragmentBackHandled(child)) {
                return true
            }
        }
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            return true
        }
        return false
    }

    /**
     * 判断Fragment是否处理了Back键
     *
     * @return 如果处理了back键则返回 **true**
     */
    private fun isFragmentBackHandled(fragment: Fragment): Boolean {
        return (fragment.isVisible
                && fragment.userVisibleHint // for ViewPager
                && fragment is HandleFragmentInterface
                && (fragment as HandleFragmentInterface).onBackPressed())
    }
}