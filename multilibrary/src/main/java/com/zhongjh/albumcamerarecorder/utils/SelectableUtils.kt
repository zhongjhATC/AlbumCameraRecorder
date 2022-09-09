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
     * 公共配置中的最大可选图片或者最大可选视频是否为 null
     * 如果为 null 就是可选无限，最大值受 maxSelectable 限制
     * 如果不为 null 就判断是否 > 0，如果 < 0 就不开启相册
     *
     * @return 是否有效
     */
    @JvmStatic
    fun albumValid(): Boolean {
        return if (GlobalSpec.albumSetting != null) {
            if (GlobalSpec.maxImageSelectable != null && GlobalSpec.maxVideoSelectable != null) {
                GlobalSpec.maxImageSelectable!! > 0 || GlobalSpec.maxVideoSelectable!! > 0
            } else if (GlobalSpec.maxImageSelectable != null && GlobalSpec.maxImageSelectable!! > 0) {
                true
            } else if (GlobalSpec.maxVideoSelectable != null && GlobalSpec.maxVideoSelectable!! > 0) {
                true
            } else {
                GlobalSpec.maxSelectable != null && GlobalSpec.maxSelectable!! > 0
            }
        } else false
    }

    /**
     * 拍摄(拍照、录像)是否有效启动
     *
     * 公共配置中的最大可选图片或者最大可选视频是否为 null
     * 如果为 null 就是可选无限，最大值受 maxSelectable 限制
     * 如果不为 null 就判断是否 > 0，如果 < 0 就不开启拍摄
     *
     * @return 是否有效
     */
    @JvmStatic
    fun cameraValid(): Boolean {
        return if (GlobalSpec.cameraSetting != null) {
            if (GlobalSpec.maxImageSelectable != null && GlobalSpec.maxVideoSelectable != null) {
                GlobalSpec.maxImageSelectable!! > 0 || GlobalSpec.maxVideoSelectable!! > 0
            } else if (GlobalSpec.maxImageSelectable != null && GlobalSpec.maxImageSelectable!! > 0) {
                true
            } else if (GlobalSpec.maxVideoSelectable != null && GlobalSpec.maxVideoSelectable!! > 0) {
                true
            } else {
                GlobalSpec.maxSelectable != null && GlobalSpec.maxSelectable!! > 0
            }
        } else false
    }

    /**
     * 录音是否有效启动
     * 判断启动了录音并且可选音频 > 0
     *
     * @return 是否有效
     */
    @JvmStatic
    fun recorderValid(): Boolean {
        return if (GlobalSpec.recorderSetting != null) {
            if (GlobalSpec.maxAudioSelectable != null) {
                GlobalSpec.maxAudioSelectable!! > 0
            } else {
                GlobalSpec.maxSelectable != null && GlobalSpec.maxSelectable!! > 0
            }
        } else {
            false
        }
    }

    /**
     * 录像是否有效启动
     *
     * 判断启动了录像并且可选视频 > 0
     *
     * @return 是否有效
     */
    @JvmStatic
    fun videoValid(): Boolean {
        if (GlobalSpec.cameraSetting != null) {
            if (GlobalSpec.getMimeTypeSet(ModuleTypes.CAMERA)
                    .containsAll(ofVideo())
            ) {
                // 是否激活视频并且总数量大于1
                if (GlobalSpec.maxSelectable != null) {
                    return GlobalSpec.maxSelectable!! > 0
                } else if (GlobalSpec.maxVideoSelectable != null) {
                    return GlobalSpec.maxVideoSelectable!! > 0
                }
            }
        }
        return false
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
            if (GlobalSpec.maxImageSelectable != null) {
                isMaxCount = imageCount == GlobalSpec.maxImageSelectable
            } else if (GlobalSpec.maxSelectable != null) {
                isMaxCount = imageCount == GlobalSpec.maxSelectable
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
            if (GlobalSpec.maxVideoSelectable != null) {
                isMaxCount = videoCount == GlobalSpec.maxVideoSelectable
            } else if (GlobalSpec.maxSelectable != null) {
                isMaxCount = videoCount == GlobalSpec.maxSelectable
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
        return if (GlobalSpec.maxSelectable != null) {
            imageCount + videoCount == GlobalSpec.maxSelectable
        } else {
            false
        }
    }

    /**
     * 如果 maxImageSelectable 和 maxVideoSelectable 都不为null，那么返回他们的数值和
     * 如果上述数值都为null，就以 maxSelectable 为准
     * 如果 maxSelectable 也为null
     * 那么依次判断 maxImageSelectable 和 maxVideoSelectable 的数值
     * 如果全部为 null 就直接返回0
     *
     * @return 返回最多能选择的图片+视频数量
     */
    @JvmStatic
    val imageVideoMaxCount: Int
        get() {
            if (GlobalSpec.maxImageSelectable != null && GlobalSpec.maxVideoSelectable != null) {
                return GlobalSpec.maxImageSelectable!! + GlobalSpec.maxVideoSelectable!!
            } else if (GlobalSpec.maxSelectable != null) {
                return GlobalSpec.maxSelectable!!
            } else if (GlobalSpec.maxImageSelectable != null) {
                return GlobalSpec.maxImageSelectable!!
            } else if (GlobalSpec.maxVideoSelectable != null) {
                return GlobalSpec.maxVideoSelectable!!
            }
            return 0
        }

    /**
     * 拍摄界面使用的场景
     * 优先：如果 maxImageSelectable 不为null就返回 maxImageSelectable
     * 次级：如果 maxSelectable 不为null则返回 maxSelectable
     * 否则为0
     *
     * @return 返回最多能选择的图片
     */
    @JvmStatic
    val imageMaxCount: Int
        get() = GlobalSpec.maxImageSelectable ?: GlobalSpec.maxSelectable ?: 0

    /**
     * 优先：如果 maxVideoSelectable 不为null就返回 maxVideoSelectable
     * 次级：如果 maxSelectable 不为null则返回 maxSelectable
     * 否则为0
     *
     * @return 返回最多能选择的视频数量
     */
    @JvmStatic
    val videoMaxCount: Int
        get() = GlobalSpec.maxVideoSelectable ?: GlobalSpec.maxSelectable ?: 0

    /**
     * 优先：如果 maxAudioSelectable 不为null就返回 maxAudioSelectable
     * 次级：如果 maxSelectable 不为null则返回 maxSelectable
     * 否则为0
     * @return 返回最多能选择的音频数量
     */
    @JvmStatic
    val audioMaxCount: Int
        get() = GlobalSpec.maxAudioSelectable ?: GlobalSpec.maxSelectable ?: 0


    /**
     * @return 返回图片/视频是否只剩下一个选择
     */
    @JvmStatic
    val singleImageVideo: Boolean
        get() {
            if (GlobalSpec.maxImageSelectable != null && GlobalSpec.maxVideoSelectable != null) {
                return GlobalSpec.maxImageSelectable == 1 && GlobalSpec.maxVideoSelectable == 1
            } else if (GlobalSpec.maxImageSelectable != null) {
                return GlobalSpec.maxImageSelectable == 1
            } else if (GlobalSpec.maxVideoSelectable != null) {
                return GlobalSpec.maxVideoSelectable == 1
            } else if (GlobalSpec.maxSelectable != null) {
                return GlobalSpec.maxSelectable == 1
            }
            return false
        }
}