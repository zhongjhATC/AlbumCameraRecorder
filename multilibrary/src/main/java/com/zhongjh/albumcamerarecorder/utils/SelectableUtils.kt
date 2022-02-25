package com.zhongjh.albumcamerarecorder.utils

import com.zhongjh.albumcamerarecorder.album.entity.SelectedCountMessage
import com.zhongjh.albumcamerarecorder.constants.ModuleTypes
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec
import com.zhongjh.common.enums.Constant.IMAGE
import com.zhongjh.common.enums.Constant.IMAGE_VIDEO
import com.zhongjh.common.enums.Constant.VIDEO
import com.zhongjh.common.enums.MimeType.Companion.ofVideo

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
object SelectableUtils {

    /**
     * 相册是否有效启动
     *
     * @return 是否有效
     */
    @JvmStatic
    fun albumValid(): Boolean {
        return if (GlobalSpec.instance.albumSetting != null) {
            if (GlobalSpec.instance.maxImageSelectable != null && GlobalSpec.instance.maxVideoSelectable != null) {
                GlobalSpec.instance.maxImageSelectable > 0 || GlobalSpec.instance.maxVideoSelectable > 0
            } else if (GlobalSpec.instance.maxImageSelectable != null && GlobalSpec.instance.maxImageSelectable > 0) {
                true
            } else if (GlobalSpec.instance.maxVideoSelectable != null && GlobalSpec.instance.maxVideoSelectable > 0) {
                true
            } else {
                GlobalSpec.instance.maxSelectable != null && GlobalSpec.instance.maxSelectable > 0
            }
        } else false
    }

    /**
     * 拍摄(拍照、录像)是否有效启动
     *
     * @return 是否有效
     */
    @JvmStatic
    fun cameraValid(): Boolean {
        return if (GlobalSpec.instance.cameraSetting != null) {
            if (GlobalSpec.instance.maxImageSelectable != null && GlobalSpec.instance.maxVideoSelectable != null) {
                GlobalSpec.instance.maxImageSelectable > 0 || GlobalSpec.instance.maxVideoSelectable > 0
            } else if (GlobalSpec.instance.maxImageSelectable != null && GlobalSpec.instance.maxImageSelectable > 0) {
                true
            } else if (GlobalSpec.instance.maxVideoSelectable != null && GlobalSpec.instance.maxVideoSelectable > 0) {
                true
            } else {
                GlobalSpec.instance.maxSelectable != null && GlobalSpec.instance.maxSelectable > 0
            }
        } else false
    }

    /**
     * 录像是否有效启动
     *
     * @return 是否有效
     */
    @JvmStatic
    fun videoValid(): Boolean {
        if (GlobalSpec.instance.cameraSetting != null) {
            if (GlobalSpec.instance.getMimeTypeSet(ModuleTypes.CAMERA)
                    .containsAll(ofVideo())
            ) {
                // 是否激活视频并且总数量大于1
                return GlobalSpec.instance.maxSelectable != null && GlobalSpec.instance.maxSelectable > 0
            }
        }
        return false
    }

    /**
     * 录音是否有效启动
     *
     * @return 是否有效
     */
    @JvmStatic
    fun recorderValid(): Boolean {
        return if (GlobalSpec.instance.recorderSetting != null) {
            if (GlobalSpec.instance.maxAudioSelectable != null) {
                GlobalSpec.instance.maxAudioSelectable > 0
            } else {
                GlobalSpec.instance.maxSelectable != null && GlobalSpec.instance.maxSelectable > 0
            }
        } else {
            false
        }
    }

    /**
     * 图片是否已经达到最大数量
     *
     * @param imageCount 当前图片数量
     * @param videoCount 当前视频数量
     * @return 是否达到最大数量
     */
    @JvmStatic
    fun isImageMaxCount(imageCount: Int, videoCount: Int): SelectedCountMessage {
        // 是否达到最大数量
        var isMaxCount: Boolean
        val selectedCountMessage = SelectedCountMessage()
        // 先判断最大值
        isMaxCount = isImageVideoMaxCount(imageCount, videoCount)
        if (!isMaxCount) {
            // 再判断本身
            if (GlobalSpec.instance.maxImageSelectable != null) {
                isMaxCount = imageCount == GlobalSpec.instance.maxImageSelectable
            } else if (GlobalSpec.instance.maxSelectable != null) {
                isMaxCount = imageCount == GlobalSpec.instance.maxSelectable
            }
            selectedCountMessage.type = IMAGE
            selectedCountMessage.maxCount = imageCount
        } else {
            selectedCountMessage.type = IMAGE_VIDEO
            selectedCountMessage.maxCount = imageCount + videoCount
        }
        selectedCountMessage.isMaxSelectableReached = isMaxCount
        return selectedCountMessage
    }

    /**
     * 视频是否已经达到最大数量
     *
     * @param videoCount 当前视频数量
     * @param imageCount 当前图片数量
     * @return 是否达到最大数量
     */
    @JvmStatic
    fun isVideoMaxCount(videoCount: Int, imageCount: Int): SelectedCountMessage {
        // 是否达到最大数量
        var isMaxCount: Boolean
        val selectedCountMessage = SelectedCountMessage()
        // 先判断最大值
        isMaxCount = isImageVideoMaxCount(imageCount, videoCount)
        if (!isMaxCount) {
            // 再判断本身
            if (GlobalSpec.instance.maxVideoSelectable != null) {
                isMaxCount = videoCount == GlobalSpec.instance.maxVideoSelectable
            } else if (GlobalSpec.instance.maxSelectable != null) {
                isMaxCount = videoCount == GlobalSpec.instance.maxSelectable
            }
            selectedCountMessage.type = VIDEO
            selectedCountMessage.maxCount = videoCount
        } else {
            selectedCountMessage.type = IMAGE_VIDEO
            selectedCountMessage.maxCount = imageCount + videoCount
        }
        selectedCountMessage.isMaxSelectableReached = isMaxCount
        return selectedCountMessage
    }

    /**
     * 图片+视频是否已经达到最大数量
     *
     * @param imageCount 当前图片数量
     * @param videoCount 当前视频数量
     * @return 是否达到最大数量
     */
    private fun isImageVideoMaxCount(imageCount: Int, videoCount: Int): Boolean {
        return if (GlobalSpec.instance.maxSelectable != null) {
            imageCount + videoCount == GlobalSpec.instance.maxSelectable
        } else {
            false
        }
    }

    /**
     * @return 返回最多能选择的图片+视频数量
     */
    @JvmStatic
    val imageVideoMaxCount: Int
        get() {
            if (GlobalSpec.instance.maxImageSelectable != null && GlobalSpec.instance.maxVideoSelectable != null) {
                return GlobalSpec.instance.maxImageSelectable + GlobalSpec.instance.maxVideoSelectable
            } else if (GlobalSpec.instance.maxSelectable != null) {
                return GlobalSpec.instance.maxSelectable
            } else if (GlobalSpec.instance.maxImageSelectable != null) {
                return GlobalSpec.instance.maxImageSelectable
            } else if (GlobalSpec.instance.maxVideoSelectable != null) {
                return GlobalSpec.instance.maxVideoSelectable
            }
            return 0
        }

    /**
     * 拍摄界面使用的场景
     *
     * @return 返回最多能选择的图片
     */
    @JvmStatic
    val imageMaxCount: Int
        get() = if (GlobalSpec.instance.maxImageSelectable != null) {
            GlobalSpec.instance.maxImageSelectable
        } else if (GlobalSpec.instance.maxSelectable != null) {
            GlobalSpec.instance.maxSelectable
        } else {
            0
        }

    /**
     * @return 返回最多能选择的视频数量
     */
    @JvmStatic
    val videoMaxCount: Int
        get() = if (GlobalSpec.instance.maxVideoSelectable != null) {
            GlobalSpec.instance.maxVideoSelectable
        } else if (GlobalSpec.instance.maxSelectable != null) {
            GlobalSpec.instance.maxSelectable
        } else {
            0
        }

    /**
     * @return 返回最多能选择的音频数量
     */
    @JvmStatic
    val audioMaxCount: Int
        get() = if (GlobalSpec.instance.maxAudioSelectable != null) {
            GlobalSpec.instance.maxAudioSelectable
        } else if (GlobalSpec.instance.maxSelectable != null) {
            GlobalSpec.instance.maxSelectable
        } else {
            0
        }

    /**
     * @return 返回图片/视频是否只剩下一个选择
     */
    @JvmStatic
    val singleImageVideo: Boolean
        get() {
            if (GlobalSpec.instance.maxImageSelectable != null && GlobalSpec.instance.maxVideoSelectable != null) {
                return GlobalSpec.instance.maxImageSelectable == 1 && GlobalSpec.instance.maxVideoSelectable == 1
            } else if (GlobalSpec.instance.maxImageSelectable != null) {
                return GlobalSpec.instance.maxImageSelectable == 1
            } else if (GlobalSpec.instance.maxVideoSelectable != null) {
                return GlobalSpec.instance.maxVideoSelectable == 1
            } else if (GlobalSpec.instance.maxSelectable != null) {
                return GlobalSpec.instance.maxSelectable == 1
            }
            return false
        }
}