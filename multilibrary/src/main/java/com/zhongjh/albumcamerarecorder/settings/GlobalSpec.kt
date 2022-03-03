package com.zhongjh.albumcamerarecorder.settings

import android.content.pm.ActivityInfo
import androidx.annotation.StyleRes
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.album.engine.ImageEngine
import com.zhongjh.albumcamerarecorder.album.engine.impl.GlideEngine
import com.zhongjh.albumcamerarecorder.constants.ModuleTypes
import com.zhongjh.albumcamerarecorder.listener.ImageCompressionInterface
import com.zhongjh.albumcamerarecorder.listener.OnResultCallbackListener
import com.zhongjh.common.coordinator.VideoCompressCoordinator
import com.zhongjh.common.entity.SaveStrategy
import com.zhongjh.common.enums.MimeType

/**
 * 设置的一些属性,别的界面也根据这个来进行动态改变
 *
 * @author zhongjh
 * @date 2018/8/23
 */
object GlobalSpec {

    /**
     * 相册的设置
     */
    var albumSetting: AlbumSetting? = null

    /**
     * 拍摄的设置
     */
    var cameraSetting: CameraSetting? = null

    /**
     * 录音的设置
     */
    var recorderSetting: RecorderSetting? = null

    /**
     * 选择 mime 的类型，MimeType.ofAll()
     */
    private var mimeTypeSet = MimeType.ofAll()

    /**
     * 是否通过正规方式进来
     */
    var hasInited = false

    /**
     * 默认从第几个开始
     */
    var defaultPosition = 0

    /**
     * 样式
     */
    @StyleRes
    var themeId = 0

    /**
     * 最大选择数量，如果设置为null，那么能选择的总数量就是 maxImageSelectable+maxVideoSelectable+maxAudioSelectable 的总数
     */
    var maxSelectable: Int? = null

    /**
     * 最大图片选择数量
     */
    var maxImageSelectable: Int? = null

    /**
     * 最大视频选择数量
     */
    var maxVideoSelectable: Int? = null

    /**
     * 最大音频选择数量
     */
    var maxAudioSelectable: Int? = null

    /**
     * 拍照\录像\存储的保存路径 参数1 true表示在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
     */
    var saveStrategy: SaveStrategy? = null

    /**
     * 图片保存路径 参数1 true表示在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
     */
    var pictureStrategy: SaveStrategy? = null

    /**
     * 视频保存路径 参数1 true表示在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
     */
    var videoStrategy: SaveStrategy? = null

    /**
     * 音频保存路径 参数1 true表示在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
     */
    var audioStrategy: SaveStrategy? = null

    lateinit var imageEngine: ImageEngine

    /**
     * 是否启动过场动画，只包括开始打开界面和关闭界面的过场动画
     */
    var cutscenesEnabled = false

    /**
     * 横竖屏设置,默认强制竖屏
     */
    var orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    /**
     * 图片是否开启编辑功能，涉及功能：预览、拍照
     */
    var imageEditEnabled = false

    /**
     * 压缩图片接口
     */
    var imageCompressionInterface: ImageCompressionInterface? = null

    /**
     * 视频压缩功能
     */
    var videoCompressCoordinator: VideoCompressCoordinator? = null

    /**
     * 请求界面的Code
     */
    var requestCode = 0

    /**
     * 回调监听
     */
    var onResultCallbackListener: OnResultCallbackListener? = null

    fun needOrientationRestriction(): Boolean {
        return orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    /**
     * @return 返回 mime 的类型，MimeType.allOf()
     */
    fun getMimeTypeSet(@ModuleTypes moduleTypes: Int): Set<MimeType> {
        // 优先取各自的类型，如果没设置则取公共的
        when (moduleTypes) {
            ModuleTypes.ALBUM ->
                return AlbumSpec.mimeTypeSet ?: this.mimeTypeSet
            ModuleTypes.CAMERA ->
                return CameraSpec.mimeTypeSet ?: this.mimeTypeSet
            ModuleTypes.RECORDER -> {
            }
            else -> {
            }
        }
        return mimeTypeSet
    }

    fun setMimeTypeSet(mimeTypeSet: Set<MimeType>) {
        this.mimeTypeSet = mimeTypeSet
    }

    /**
     * @return 是否开启了视频压缩功能
     */
    val isCompressEnable: Boolean
        get() = videoCompressCoordinator != null

    val cleanInstance = GlobalSpec
        get() {
            val globalSpec: GlobalSpec = field
            globalSpec.reset()
            return globalSpec
        }

    /**
     * 重置
     */
    private fun reset() {
        albumSetting = null
        cameraSetting = null
        recorderSetting = null
        mimeTypeSet = MimeType.ofAll()
        defaultPosition = 0
        themeId = R.style.AppTheme_Blue
        maxSelectable = null
        maxImageSelectable = null
        maxVideoSelectable = null
        maxAudioSelectable = null
        saveStrategy = null
        pictureStrategy = null
        videoStrategy = null
        audioStrategy = null
        hasInited = true
        imageEngine = GlideEngine()
        cutscenesEnabled = true
        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        imageEditEnabled = true
        imageCompressionInterface = null
        requestCode = 0
    }
}