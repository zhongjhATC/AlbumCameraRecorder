package com.zhongjh.albumcamerarecorder.listener

import android.view.KeyEvent

/**
 * 处理回退的接口
 *
 * @author zhongjh
 */
interface HandleFragmentInterface {

    /**
     * 处理回退事件
     *
     * @return 是否回退，false为回退，true为本身消化了该事件
     */
    fun onBackPressed(): Boolean

    /**
     * 处理onKeyDown事件
     *
     * @return false为继续下个fragment检测，true为本身fragment消化了该事件
     */
    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean

}