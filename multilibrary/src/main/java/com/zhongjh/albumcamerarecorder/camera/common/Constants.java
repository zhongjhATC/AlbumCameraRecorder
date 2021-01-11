package com.zhongjh.albumcamerarecorder.camera.common;

/**
 * 公共类
 * Created by zhongjh on 2018/8/7.
 */
public class Constants {

    public static final String ScreenWidth = "ScreenWidth";                 //屏幕宽度
    public static final String ScreenHeight = "ScreenHeight";               //屏幕高度

    public static final int STATE_PREVIEW = 0x01;      // 休闲状态 - 除了下面两个状态
    public static final int STATE_PICTURE = 0x02;      // 图片状态 - 拍照后，就修改成这个状态
    public static final int STATE_VIDEO = 0x03;        // 视频状态 - 录制视频后，播放当前视频，就修改成这个状态
    public static final int STATE_PICTURE_PREVIEW = 0x04;      // 图片休闲状态 - 拍照后，如果是多图情况，就修改成这个状态
    public static final int STATE_RECORDER = 0x05;      // 图片状态 - 拍照后，就修改成这个状态

    public static final int TYPE_RECORDER = 0x090; // 录像模式
    public static final int TYPE_CAPTURE = 0x091;  // 拍照模式

    public static final int TYPE_FLASH_AUTO = 0x101;                // 闪关灯自动
    public static final int TYPE_FLASH_ON = 0x102;                  // 闪关灯开启
    public static final int TYPE_FLASH_OFF = 0x103;                 // 闪关灯关闭

    public static final int BUTTON_STATE_ONLY_CLICK = 0x201;        //按钮只能点击
    public static final int BUTTON_STATE_ONLY_LONGCLICK = 0x202;     //按钮只能长按
    public static final int BUTTON_STATE_BOTH = 0x203;              //按钮点击或者长按两者都可以

    // 拍照浏览时候的类型
    public static final int TYPE_PICTURE = 0x001;   // 图片
    public static final int TYPE_VIDEO = 0x002;     // 视频
    public static final int TYPE_SHORT = 0x003;     // 短视频，无效的视频
    public static final int TYPE_DEFAULT = 0x004;   // 默认的

}
