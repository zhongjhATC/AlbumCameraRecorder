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

    /**
     * 图片
     */
    public static final int TYPE_PICTURE = 0x001;
    /**
     * 视频
     */
    public static final int TYPE_VIDEO = 0x002;


}
