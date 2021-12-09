package com.zhongjh.albumcamerarecorder.utils;

import com.zhongjh.albumcamerarecorder.album.entity.SelectedCountMessage;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;

import static com.zhongjh.common.enums.Constant.IMAGE;
import static com.zhongjh.common.enums.Constant.IMAGE_VIDEO;
import static com.zhongjh.common.enums.Constant.VIDEO;

/**
 * 这是一个判断能选择xx个图片、视频、音频的判断逻辑封装
 * 根据当前设置值来呈现相应的功能：
 * 1： maxSelectable有值maxImageSelectable无值，可选择的图片上限和所有数据的上限总和以maxSelectable为标准
 * 2： maxSelectable无值maxImageSelectable有值，可选择的图片上限以maxImageSelectable为准，其他例如视频音频也是以各自的上限为准
 * 3： maxSelectable有值maxImageSelectable有值，可选择的图片上限以maxImageSelectable为准，但是最终总和数据以maxSelectable为标准
 *
 * @author zhongjh
 * @date 2021/7/13
 */
public class SelectableUtils {

    /**
     * 相册是否有效启动
     *
     * @return 是否有效
     */
    public static boolean albumValid() {
        if (GlobalSpec.getInstance().albumSetting != null) {
            if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxVideoSelectable != null) {
                return GlobalSpec.getInstance().maxImageSelectable > 0 || GlobalSpec.getInstance().maxVideoSelectable > 0;
            } else if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxImageSelectable > 0) {
                return true;
            } else if (GlobalSpec.getInstance().maxVideoSelectable != null && GlobalSpec.getInstance().maxVideoSelectable > 0) {
                return true;
            } else {
                return GlobalSpec.getInstance().maxSelectable != null && GlobalSpec.getInstance().maxSelectable > 0;
            }
        }
        return false;
    }

    /**
     * 拍摄是否有效启动
     *
     * @return 是否有效
     */
    public static boolean cameraValid() {
        if (GlobalSpec.getInstance().cameraSetting != null) {
            if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxVideoSelectable != null) {
                return GlobalSpec.getInstance().maxImageSelectable > 0 || GlobalSpec.getInstance().maxVideoSelectable > 0;
            } else if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxImageSelectable > 0) {
                return true;
            } else if (GlobalSpec.getInstance().maxVideoSelectable != null && GlobalSpec.getInstance().maxVideoSelectable > 0) {
                return true;
            } else {
                return GlobalSpec.getInstance().maxSelectable != null && GlobalSpec.getInstance().maxSelectable > 0;
            }
        }
        return false;
    }

    /**
     * 录音是否有效启动
     *
     * @return 是否有效
     */
    public static boolean recorderValid() {
        if (GlobalSpec.getInstance().recorderSetting != null) {
            if (GlobalSpec.getInstance().maxAudioSelectable != null) {
                return GlobalSpec.getInstance().maxAudioSelectable > 0;
            } else {
                return GlobalSpec.getInstance().maxSelectable != null && GlobalSpec.getInstance().maxSelectable > 0;
            }
        } else {
            return false;
        }
    }

    /**
     * 图片是否已经达到最大数量
     *
     * @param imageCount 当前图片数量
     * @param videoCount 当前视频数量
     * @return 是否达到最大数量
     */
    public static SelectedCountMessage isImageMaxCount(int imageCount, int videoCount) {
        // 是否达到最大数量
        boolean isMaxCount;
        SelectedCountMessage selectedCountMessage = new SelectedCountMessage();
        // 先判断最大值
        isMaxCount = isImageVideoMaxCount(imageCount,videoCount);
        if (!isMaxCount) {
            // 再判断本身
            if (GlobalSpec.getInstance().maxImageSelectable != null) {
                isMaxCount = imageCount == GlobalSpec.getInstance().maxImageSelectable;
            } else if (GlobalSpec.getInstance().maxSelectable != null) {
                isMaxCount = imageCount == GlobalSpec.getInstance().maxSelectable;
            }
            selectedCountMessage.setType(IMAGE);
            selectedCountMessage.setMaxCount(imageCount);
        } else {
            selectedCountMessage.setType(IMAGE_VIDEO);
            selectedCountMessage.setMaxCount(imageCount+videoCount);
        }
        selectedCountMessage.setMaxSelectableReached(isMaxCount);
        return selectedCountMessage;
    }

    /**
     * 视频是否已经达到最大数量
     *
     * @param videoCount 当前视频数量
     * @param imageCount 当前图片数量
     * @return 是否达到最大数量
     */
    public static SelectedCountMessage isVideoMaxCount(int videoCount, int imageCount) {
        // 是否达到最大数量
        boolean isMaxCount;
        SelectedCountMessage selectedCountMessage = new SelectedCountMessage();
        // 先判断最大值
        isMaxCount = isImageVideoMaxCount(imageCount,videoCount);
        if (!isMaxCount) {
            // 再判断本身
            if (GlobalSpec.getInstance().maxVideoSelectable != null) {
                isMaxCount = videoCount == GlobalSpec.getInstance().maxVideoSelectable;
            } else if (GlobalSpec.getInstance().maxSelectable != null) {
                isMaxCount = videoCount == GlobalSpec.getInstance().maxSelectable;
            }
            selectedCountMessage.setType(VIDEO);
            selectedCountMessage.setMaxCount(videoCount);
        } else {
            selectedCountMessage.setType(IMAGE_VIDEO);
            selectedCountMessage.setMaxCount(imageCount+videoCount);
        }
        selectedCountMessage.setMaxSelectableReached(isMaxCount);
        return selectedCountMessage;
    }

    /**
     * 图片+视频是否已经达到最大数量
     *
     * @param imageCount 当前图片数量
     * @param videoCount 当前视频数量
     * @return 是否达到最大数量
     */
    private static boolean isImageVideoMaxCount(int imageCount, int videoCount) {
        if (GlobalSpec.getInstance().maxSelectable != null) {
            return (imageCount+videoCount) == GlobalSpec.getInstance().maxSelectable;
        }
        return false;
    }

    /**
     * @return 返回最多能选择的图片+视频数量
     */
    public static int getImageVideoMaxCount() {
        if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxVideoSelectable != null) {
            return GlobalSpec.getInstance().maxImageSelectable + GlobalSpec.getInstance().maxVideoSelectable;
        } else if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        } else if (GlobalSpec.getInstance().maxImageSelectable != null) {
            return GlobalSpec.getInstance().maxImageSelectable;
        } else if (GlobalSpec.getInstance().maxVideoSelectable != null) {
            return GlobalSpec.getInstance().maxVideoSelectable;
        }
        return 0;
    }

    /**
     * 拍摄界面使用的场景
     * @return 返回最多能选择的图片
     */
    public static int getImageMaxCount() {
        if (GlobalSpec.getInstance().maxImageSelectable != null) {
            return GlobalSpec.getInstance().maxImageSelectable;
        } else if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        } else {
            return 0;
        }
    }

    /**
     * @return 返回最多能选择的视频数量
     */
    public static int getVideoMaxCount() {
        if (GlobalSpec.getInstance().maxVideoSelectable != null) {
            return GlobalSpec.getInstance().maxVideoSelectable;
        } else if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        } else {
            return 0;
        }
    }

    /**
     * @return 返回最多能选择的音频数量
     */
    public static int getAudioMaxCount() {
        if (GlobalSpec.getInstance().maxAudioSelectable != null) {
            return GlobalSpec.getInstance().maxAudioSelectable;
        } else if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        } else {
            return 0;
        }
    }

    /**
     * @return 返回图片/视频是否只剩下一个选择
     */
    public static boolean getSingleImageVideo() {
        if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxVideoSelectable != null) {
            return GlobalSpec.getInstance().maxImageSelectable == 1 && GlobalSpec.getInstance().maxVideoSelectable == 1;
        } else if (GlobalSpec.getInstance().maxImageSelectable != null) {
            return GlobalSpec.getInstance().maxImageSelectable == 1;
        } else if (GlobalSpec.getInstance().maxVideoSelectable != null) {
            return GlobalSpec.getInstance().maxVideoSelectable == 1;
        } else if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable == 1;
        }
        return false;
    }

}
