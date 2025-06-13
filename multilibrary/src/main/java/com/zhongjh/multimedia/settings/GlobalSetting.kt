package com.zhongjh.multimedia.settings

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.IntDef
import androidx.annotation.StyleRes
import com.zhongjh.multimedia.MainActivity
import com.zhongjh.multimedia.R
import com.zhongjh.common.engine.ImageEngine
import com.zhongjh.multimedia.listener.OnImageCompressionListener
import com.zhongjh.multimedia.listener.OnLogListener
import com.zhongjh.multimedia.preview.PreviewActivity
import com.zhongjh.multimedia.preview.enum.PreviewType
import com.zhongjh.multimedia.preview.start.PreviewSetting
import com.zhongjh.multimedia.settings.GlobalSpec.cleanInstance
import com.zhongjh.multimedia.settings.api.GlobalSettingApi
import com.zhongjh.common.coordinator.VideoCompressCoordinator
import com.zhongjh.common.entity.GridMedia
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MimeType
import java.lang.ref.WeakReference
import java.util.*

/**
 * 用于构建媒体具体公共设置 API。
 *
 * @author zhongjh
 * @date 2018/9/28
 *
 * 在上下文中构造新的规范生成器。
 *
 * @param multiMediaSetting 在 requester context wrapper.
 * @param mimeTypes         设置为选择的 [MimeType] 类型
 */
class GlobalSetting internal constructor(
    private val multiMediaSetting: MultiMediaSetting,
    mimeTypes: Set<MimeType>
) : GlobalSettingApi {

    private val mGlobalSpec: GlobalSpec = cleanInstance

    @IntDef(value = [ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_USER, ActivityInfo.SCREEN_ORIENTATION_BEHIND, ActivityInfo.SCREEN_ORIENTATION_SENSOR, ActivityInfo.SCREEN_ORIENTATION_NOSENSOR, ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR, ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_FULL_USER, ActivityInfo.SCREEN_ORIENTATION_LOCKED])
    @Retention(AnnotationRetention.SOURCE)
    annotation class ScreenOrientation

    override fun onDestroy() {
        mGlobalSpec.activityResultLauncher = null
        if (mGlobalSpec.albumSetting != null) {
            mGlobalSpec.albumSetting!!.onDestroy()
        }
        if (mGlobalSpec.cameraSetting != null) {
            mGlobalSpec.cameraSetting!!.onDestroy()
        }
        mGlobalSpec.onLogListener = null
    }

    override fun albumSetting(albumSetting: AlbumSetting): GlobalSetting {
        mGlobalSpec.albumSetting = albumSetting
        return this
    }

    override fun cameraSetting(cameraSetting: CameraSetting): GlobalSetting {
        mGlobalSpec.cameraSetting = cameraSetting
        return this
    }

    override fun recorderSetting(recorderSetting: RecorderSetting): GlobalSetting {
        mGlobalSpec.recorderSetting = recorderSetting
        return this
    }

    override fun theme(@StyleRes themeId: Int): GlobalSetting {
        mGlobalSpec.themeId = themeId
        return this
    }

    override fun defaultPosition(position: Int): GlobalSetting {
        mGlobalSpec.defaultPosition = position
        return this
    }

    override fun maxSelectablePerMediaType(
        maxSelectable: Int?,
        maxImageSelectable: Int?,
        maxVideoSelectable: Int?,
        maxAudioSelectable: Int?,
        alreadyImageCount: Int,
        alreadyVideoCount: Int,
        alreadyAudioCount: Int
    ): GlobalSetting {
        check(!(maxSelectable == null && maxImageSelectable == null)) { "maxSelectablePerMediaType 方法中如果 maxSelectable 为null，那么 maxImageSelectable 必须是0或者0以上数值" }
        check(!(maxSelectable == null && maxVideoSelectable == null)) { "maxSelectablePerMediaType 方法中如果 maxSelectable 为null，那么 maxVideoSelectable 必须是0或者0以上数值" }
        check(!(maxSelectable == null && maxAudioSelectable == null)) { "maxSelectablePerMediaType 方法中如果 maxSelectable 为null，那么 maxAudioSelectable 必须是0或者0以上数值" }
        check(!(maxSelectable != null && maxImageSelectable != null && maxImageSelectable > maxSelectable)) { "maxSelectable 必须比 maxImageSelectable 大" }
        check(!(maxSelectable != null && maxVideoSelectable != null && maxVideoSelectable > maxSelectable)) { "maxSelectable 必须比 maxVideoSelectable 大" }
        check(!(maxSelectable != null && maxAudioSelectable != null && maxAudioSelectable > maxSelectable)) { "maxSelectable 必须比 maxAudioSelectable 大" }

        // 计算
        if (maxSelectable != null) {
            mGlobalSpec.maxSelectable =
                maxSelectable - (alreadyImageCount + alreadyVideoCount + alreadyAudioCount)
        }
        if (maxImageSelectable != null) {
            mGlobalSpec.maxImageSelectable = maxImageSelectable - alreadyImageCount
        } else {
            mGlobalSpec.maxImageSelectable = null
        }
        if (maxVideoSelectable != null) {
            mGlobalSpec.maxVideoSelectable = maxVideoSelectable - alreadyVideoCount
        } else {
            mGlobalSpec.maxVideoSelectable = null
        }
        if (maxAudioSelectable != null) {
            mGlobalSpec.maxAudioSelectable = maxAudioSelectable - alreadyAudioCount
        } else {
            mGlobalSpec.maxAudioSelectable = null
        }
        return this
    }

    override fun alreadyCount(
        maxSelectable: Int?,
        maxImageSelectable: Int?,
        maxVideoSelectable: Int?,
        maxAudioSelectable: Int?,
        alreadyImageCount: Int,
        alreadyVideoCount: Int,
        alreadyAudioCount: Int
    ): GlobalSetting {
        // 计算
        if (maxSelectable != null) {
            mGlobalSpec.maxSelectable =
                maxSelectable - (alreadyImageCount + alreadyVideoCount + alreadyAudioCount)
        }
        if (maxImageSelectable != null) {
            mGlobalSpec.maxImageSelectable = maxImageSelectable - alreadyImageCount
        }
        if (maxVideoSelectable != null) {
            mGlobalSpec.maxVideoSelectable = maxVideoSelectable - alreadyVideoCount
        }
        if (maxAudioSelectable != null) {
            mGlobalSpec.maxAudioSelectable = maxAudioSelectable - alreadyAudioCount
        }
        return this
    }

    override fun compressImagePath(compressImagePath: String): GlobalSetting {
        mGlobalSpec.compressImagePath = compressImagePath
        return this
    }

    override fun compressVidePath(compressVidePath: String): GlobalSetting {
        mGlobalSpec.compressVidePath = compressVidePath
        return this
    }

    override fun imageEngine(imageEngine: ImageEngine): GlobalSetting {
        mGlobalSpec.imageEngine = imageEngine
        return this
    }

    override fun isCutscenes(isCutscenes: Boolean): GlobalSetting {
        mGlobalSpec.cutscenesEnabled = isCutscenes
        return this
    }

    override fun setRequestedOrientation(@ScreenOrientation requestedOrientation: Int): GlobalSetting {
        mGlobalSpec.orientation = requestedOrientation
        return this
    }

    override fun isImageEdit(isImageEdit: Boolean): GlobalSetting {
        mGlobalSpec.imageEditEnabled = isImageEdit
        return this
    }

    override fun isAddAlbum(byEdit: Boolean): GlobalSetting {
        mGlobalSpec.isAddAlbumByEdit = byEdit
        return this
    }

    override fun setOnImageCompressionListener(listener: OnImageCompressionListener): GlobalSetting {
        mGlobalSpec.onImageCompressionListener = WeakReference(listener).get()
        return this
    }

    override fun setOnLogListener(listener: OnLogListener): GlobalSetting {
        mGlobalSpec.onLogListener = WeakReference(listener).get()
        return this
    }

    override fun videoCompress(videoCompressManager: VideoCompressCoordinator): GlobalSetting {
        mGlobalSpec.videoCompressCoordinator = videoCompressManager
        return this
    }

    override fun forResult(activityResultLauncher: ActivityResultLauncher<Intent>) {
        mGlobalSpec.activityResultLauncher = activityResultLauncher
        openMain(activityResultLauncher)
    }

    /**
     * 调用打开图片、视频预览 - 主要用于配合九宫图
     *
     * @param activity    窗体
     * @param activityResultLauncher 请求器
     * @param list        数据源
     * @param position    当前数据的索引
     * @param isApply 是否有同意操作,支持删除数据源
     */
    override fun openPreviewData(activity: Activity, activityResultLauncher: ActivityResultLauncher<Intent>, list: ArrayList<GridMedia>, position: Int, isApply: Boolean) {
        val intent = Intent(activity, PreviewActivity::class.java)

        // 深度拷贝
        val localMedias = ArrayList<LocalMedia>()
        for (i in list.indices) {
            val localMedia = LocalMedia(list[i])
            localMedias.add(localMedia)
        }

        PreviewSetting(PreviewType.GRID)
            .setLocalMediaArrayList(localMedias)
            .setCurrentPosition(position)
            .isApply(isApply)
            .isOriginal(false)
            .isSelectedCheck(false)
            .isEdit(false)
            .setIntent(intent)
        activityResultLauncher.launch(intent)
        if (GlobalSpec.cutscenesEnabled) {
            activity.overridePendingTransition(R.anim.activity_open_zjh, 0)
        }
    }

    private fun openMain(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val activity = multiMediaSetting.activity ?: return
        // 数量
        var numItems = 0
        // 根据相关配置做相应的初始化
        if (mGlobalSpec.albumSetting != null) {
            numItems++
        }
        if (mGlobalSpec.cameraSetting != null) {
            numItems++
        }
        if (mGlobalSpec.recorderSetting != null) {
            numItems++
        }
        // 如果numItems一个都没，则抛出异常
        check(numItems > 0) { activity.resources.getString(R.string.z_one_of_these_three_albumSetting_camerasSetting_and_recordDerSetting_must_be_set) }
        val intent = Intent(activity, MainActivity::class.java)
        val fragment = multiMediaSetting.fragment
        if (fragment != null) {
            activityResultLauncher.launch(intent)
        } else {
            activityResultLauncher.launch(intent)
            if (mGlobalSpec.cutscenesEnabled) {
                activity.overridePendingTransition(R.anim.activity_open_zjh, 0)
            }
        }
    }

    init {
        mGlobalSpec.setMimeTypeSet(mimeTypes)
    }
}