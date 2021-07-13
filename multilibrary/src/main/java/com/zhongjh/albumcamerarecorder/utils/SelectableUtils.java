package com.zhongjh.albumcamerarecorder.utils;

import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;

/**
 * 这是一个判断能选择xx个图片、视频、音频的判断逻辑封装
 * @author zhongjh
 * @date 2021/7/13
 */
public class SelectableUtils {

    /**
     * 相册是否有效启动
     * @return 是否有效
     */
    public static boolean albumValid() {
        if (GlobalSpec.getInstance().albumSetting != null) {
            return (GlobalSpec.getInstance().maxSelectable != null && GlobalSpec.getInstance().maxSelectable > 0) ||
                    (GlobalSpec.getInstance().maxImageSelectable > 0 || GlobalSpec.getInstance().maxVideoSelectable > 0);
        } else {
            return false;
        }
    }

    /**
     * 拍摄是否有效启动
     * @return 是否有效
     */
    public static boolean cameraValid() {
        if (GlobalSpec.getInstance().cameraSetting != null) {
            return (GlobalSpec.getInstance().maxSelectable != null && GlobalSpec.getInstance().maxSelectable > 0) ||
                    (GlobalSpec.getInstance().maxImageSelectable > 0 || GlobalSpec.getInstance().maxVideoSelectable > 0);
        } else {
            return false;
        }
    }

    /**
     * 录音是否有效启动
     * @return 是否有效
     */
    public static boolean recorderValid() {
        if (GlobalSpec.getInstance().recorderSetting != null) {
            return (GlobalSpec.getInstance().maxSelectable != null && GlobalSpec.getInstance().maxSelectable > 0) ||
                    (GlobalSpec.getInstance().maxAudioSelectable > 0);
        } else {
            return false;
        }
    }

    /**
     * 图片是否已经达到最大数量
     *
     * @param imageCount 当前图片数量
     * @return 是否达到最大数量
     */
    public static boolean isImageMaxCount(int imageCount) {
        if (GlobalSpec.getInstance().maxSelectable != null) {
            return imageCount == GlobalSpec.getInstance().maxSelectable;
        } else {
            return imageCount == GlobalSpec.getInstance().maxImageSelectable;
        }
    }

    /**
     * 视频是否已经达到最大数量
     *
     * @param videoCount 当前视频数量
     * @return 是否达到最大数量
     */
    public static boolean isVideoMaxCount(int videoCount) {
        if (GlobalSpec.getInstance().maxSelectable != null) {
            return videoCount == GlobalSpec.getInstance().maxSelectable;
        } else {
            return videoCount == GlobalSpec.getInstance().maxVideoSelectable;
        }
    }

    /**
     * @return 返回最多能选择的图片+视频数量
     */
    public static int getImageVideoMaxCount() {
        if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        } else {
            return GlobalSpec.getInstance().maxImageSelectable + GlobalSpec.getInstance().maxVideoSelectable;
        }
    }

    /**
     * @return 返回最多能选择的图片
     */
    public static int getImageMaxCount() {
        if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        } else {
            return GlobalSpec.getInstance().maxImageSelectable;
        }
    }

    /**
     * @return 返回最多能选择的视频数量
     */
    public static int getVideoMaxCount() {
        if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        } else {
            return GlobalSpec.getInstance().maxVideoSelectable;
        }
    }

    /**
     * @return 返回最多能选择的音频数量
     */
    public static int getAudioMaxCount() {
        if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        } else {
            return GlobalSpec.getInstance().maxAudioSelectable;
        }
    }

    /**
     * @return 返回图片/视频是否只剩下一个选择
     */
    public static boolean getSingleImageVideo() {
        if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable == 1;
        } else {
            return GlobalSpec.getInstance().maxImageSelectable == 1 && GlobalSpec.getInstance().maxVideoSelectable == 1;
        }
    }


}
