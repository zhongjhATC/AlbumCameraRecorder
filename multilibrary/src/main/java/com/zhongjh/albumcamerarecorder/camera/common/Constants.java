package com.zhongjh.albumcamerarecorder.camera.common;

/**
 * 公共类
 *
 * @author zhongjh
 * @date 2018/8/7
 */
public class Constants {

    /**
     * 屏幕宽度
     */
    public static final String SCREEN_WIDTH = "ScreenWidth";
    /**
     * 屏幕高度
     */
    public static final String SCREEN_HEIGHT = "ScreenHeight";

    /**
     * 休闲状态 - 除了下面两个状态
     */
    public static final int STATE_PREVIEW = 0x01;
    /**
     * 图片状态 - 拍照后，就修改成这个状态
     */
    public static final int STATE_PICTURE = 0x02;
    /**
     * 视频状态 - 录制视频后，播放当前视频，就修改成这个状态
     */
    public static final int STATE_VIDEO = 0x03;
    /**
     * 图片休闲状态 - 拍照后，如果是多图情况，就修改成这个状态
     */
    public static final int STATE_PICTURE_PREVIEW = 0x04;
    /**
     * 图片状态 - 拍照后，就修改成这个状态
     */
    public static final int STATE_RECORDER = 0x05;
    /**
     * 视频状态 - 录制视频中，就修改成这个状态
     */
    public static final int STATE_VIDEO_IN = 0x06;

    /**
     * 闪关灯自动
     */
    public static final int TYPE_FLASH_AUTO = 0x101;
    /**
     * 闪关灯开启
     */
    public static final int TYPE_FLASH_ON = 0x102;
    /**
     * 闪关灯关闭
     */
    public static final int TYPE_FLASH_OFF = 0x103;

    /**
     * 按钮只能点击
     */
    public static final int BUTTON_STATE_ONLY_CLICK = 0x201;
    /**
     * 按钮只能长按
     */
    public static final int BUTTON_STATE_ONLY_LONG_CLICK = 0x202;
    /**
     * 按钮点击或者长按两者都可以
     */
    public static final int BUTTON_STATE_BOTH = 0x203;

    // region 拍照浏览时候的类型

    /**
     * 图片
     */
    public static final int TYPE_PICTURE = 0x001;
    /**
     * 视频
     */
    public static final int TYPE_VIDEO = 0x002;
    /**
     * 短视频，无效的视频
     */
    public static final int TYPE_SHORT = 0x003;
    /**
     * 默认的
     */
    public static final int TYPE_DEFAULT = 0x004;

    // endregion

}
