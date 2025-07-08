package com.zhongjh.multimedia.settings

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.StyleRes
import com.zhongjh.multimedia.R
import com.zhongjh.common.engine.ImageEngine
import com.zhongjh.common.engine.impl.GlideEngine
import com.zhongjh.multimedia.constants.ModuleTypes
import com.zhongjh.multimedia.listener.OnImageCompressionListener
import com.zhongjh.multimedia.listener.OnLogListener
import com.zhongjh.common.coordinator.VideoCompressCoordinator
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
    var hasInit = false

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
     * 压缩图片路径
     */
    var compressImagePath: String? = null

    /**
     * 压缩视频路径
     */
    var compressVidePath: String? = null

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
     * 设置图片编辑后是否加入相册功能，默认加入
     */
    var isAddAlbumByEdit = true

    /**
     * 压缩图片接口
     */
    var onImageCompressionListener: OnImageCompressionListener? = null

    /**
     * 日志接口
     * 虽然功能都捕获了相关异常，但是一般开发都是需要记录为何报错，可以让下次修复
     */
    var onLogListener: OnLogListener? = null

    /**
     * 视频压缩功能
     */
    var videoCompressCoordinator: VideoCompressCoordinator? = null

    /**
     * 用于启动执行ActivityResultContract过程的先前准备好的调用的启动器
     */
    var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    fun needOrientationRestriction(): Boolean {
        return orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    /**
     * 传递子模块的类型，如果子模块有类型限制，则优先子模块的限制，如果没有，才轮到公共模块的限制
     * @return 根据指定功能模块返回 mime 的类型，MimeType.allOf()
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
        compressImagePath = null
        compressVidePath = null
        hasInit = true
        imageEngine = GlideEngine()
        cutscenesEnabled = true
        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        imageEditEnabled = true
        onImageCompressionListener = null
        onLogListener = null
        activityResultLauncher = null
    }
}