package com.zhongjh.albumcamerarecorder.listener;

/**
 * 处理回退的接口
 *
 * @author zhongjh
 */
public interface HandleBackInterface {

    /**
     * 处理回退事件
     *
     * @return 是否回退，false为回退，true为本身处理了其他事件
     */
    boolean onBackPressed();

}
