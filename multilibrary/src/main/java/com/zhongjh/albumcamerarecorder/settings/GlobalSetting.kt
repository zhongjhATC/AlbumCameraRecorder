package com.zhongjh.albumcamerarecorder.settings

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.annotation.IntDef
import androidx.annotation.StyleRes
import com.zhongjh.albumcamerarecorder.MainActivity
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.common.engine.ImageEngine
import com.zhongjh.albumcamerarecorder.listener.OnImageCompressionListener
import com.zhongjh.albumcamerarecorder.listener.OnLogListener
import com.zhongjh.albumcamerarecorder.listener.OnResultCallbackListener
import com.zhongjh.albumcamerarecorder.model.SelectedData.*
import com.zhongjh.albumcamerarecorder.preview.PreviewActivity
import com.zhongjh.albumcamerarecorder.preview.PreviewFragment2
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec.cleanInstance
import com.zhongjh.albumcamerarecorder.settings.api.GlobalSettingApi
import com.zhongjh.common.coordinator.VideoCompressCoordinator
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.entity.SaveStrategy
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
        mGlobalSpec.onResultCallbackListener = null
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

    override fun forResult(requestCode: Int) {
        mGlobalSpec.requestCode = requestCode
        // 回调监听设置null
        mGlobalSpec.onResultCallbackListener = null
        openMain(requestCode)
    }

    override fun forResult(listener: OnResultCallbackListener) {
        // 绑定回调监听
        mGlobalSpec.onResultCallbackListener = WeakReference(listener).get()
        openMain(null)
    }

    /**
     * 调用打开图片、视频预览 - 主要用于配合九宫图
     *
     * @param activity    窗体
     * @param requestCode 请求码
     * @param list        数据源
     * @param position    当前数据的索引
     */
    override fun openPreviewData(
        activity: Activity, requestCode: Int,
        list: ArrayList<LocalMedia>, position: Int
    ) {
        val intent = Intent(activity, PreviewActivity::class.java)
        intent.putExtra(PreviewFragment2.STATE_SELECTION, list)
        intent.putExtra(PreviewFragment2.EXTRA_ITEM, list[position])
        intent.putExtra(STATE_COLLECTION_TYPE, COLLECTION_IMAGE)
        intent.putExtra(PreviewFragment2.EXTRA_RESULT_ORIGINAL_ENABLE, false)
        intent.putExtra(PreviewFragment2.EXTRA_IS_ALLOW_REPEAT, true)
        intent.putExtra(PreviewFragment2.IS_SELECTED_CHECK, false)
        intent.putExtra(PreviewFragment2.IS_EXTERNAL_USERS, true)
        intent.putExtra(PreviewFragment2.EDIT_ENABLE, false)
        activity.startActivityForResult(intent, requestCode)
        if (GlobalSpec.cutscenesEnabled) {
            activity.overridePendingTransition(R.anim.activity_open_zjh, 0)
        }
    }

    /**
     * 调用打开图片预览 - 纯浏览不可操作
     *
     * @param activity 窗体
     * @param list     文件地址的数据源
     * @param position 当前数据的索引
     */
    override fun openPreviewPath(activity: Activity, list: ArrayList<String>, position: Int) {
//        openPreview(activity, list, position)
    }

    private fun openMain(requestCode: Int?) {
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
            if (requestCode != null) {
                fragment.startActivityForResult(intent, requestCode)
            } else {
                fragment.startActivity(intent)
            }
        } else {
            if (requestCode != null) {
                activity.startActivityForResult(intent, requestCode)
            } else {
                activity.startActivity(intent)
            }
            if (mGlobalSpec.cutscenesEnabled) {
                activity.overridePendingTransition(R.anim.activity_open_zjh, 0)
            }
        }
    }

//    companion object {
//        /**
//         * 提供给 [.openPreviewResourceId] 和 [.openPreviewPath] 共用的方法
//         *
//         * @param activity    窗体
//         * @param localMedias 数据源
//         * @param position    当前数据的索引
//         */
//        private fun openPreview(
//            activity: Activity,
//            localMedias: ArrayList<LocalMedia>,
//            position: Int
//        ) {
//            val bundle = Bundle()
//            bundle.putParcelableArrayList(STATE_SELECTION, localMedias)
//            bundle.putInt(STATE_COLLECTION_TYPE, COLLECTION_IMAGE)
//            val intent = Intent(activity, PreviewActivity::class.java)
//            intent.putExtra(PreviewFragment2.EXTRA_ITEM, localMedias[position])
//            intent.putExtra(PreviewFragment2.EXTRA_DEFAULT_BUNDLE, bundle)
//            intent.putExtra(PreviewFragment2.EXTRA_RESULT_ORIGINAL_ENABLE, false)
//            intent.putExtra(PreviewFragment2.EXTRA_IS_ALLOW_REPEAT, true)
//            intent.putExtra(PreviewFragment2.IS_SELECTED_CHECK, false)
//            intent.putExtra(PreviewFragment2.APPLY_ENABLE, false)
//            intent.putExtra(PreviewFragment2.SELECTED_ENABLE, false)
//            intent.putExtra(PreviewFragment2.IS_EXTERNAL_USERS, true)
//            activity.startActivityForResult(intent, GlobalSpec.requestCode)
//            if (GlobalSpec.cutscenesEnabled) {
//                activity.overridePendingTransition(R.anim.activity_open_zjh, 0)
//            }
//        }
//    }

    init {
        mGlobalSpec.setMimeTypeSet(mimeTypes)
    }
}