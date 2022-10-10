package com.zhongjh.albumcamerarecorder.settings

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.annotation.IntDef
import androidx.annotation.StyleRes
import com.zhongjh.albumcamerarecorder.MainActivity
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.album.engine.ImageEngine
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection
import com.zhongjh.albumcamerarecorder.listener.ImageCompressionInterface
import com.zhongjh.albumcamerarecorder.listener.OnResultCallbackListener
import com.zhongjh.albumcamerarecorder.preview.AlbumPreviewActivity
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec.cleanInstance
import com.zhongjh.albumcamerarecorder.settings.api.GlobalSettingApi
import com.zhongjh.common.coordinator.VideoCompressCoordinator
import com.zhongjh.common.entity.MultiMedia
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
class GlobalSetting
internal constructor(private val multiMediaSetting: MultiMediaSetting, mimeTypes: Set<MimeType>) :
    GlobalSettingApi {

    private val mGlobalSpec: GlobalSpec = cleanInstance

    @IntDef(value = [ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_USER, ActivityInfo.SCREEN_ORIENTATION_BEHIND, ActivityInfo.SCREEN_ORIENTATION_SENSOR, ActivityInfo.SCREEN_ORIENTATION_NOSENSOR, ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR, ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_FULL_USER, ActivityInfo.SCREEN_ORIENTATION_LOCKED])
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class ScreenOrientation

    override fun onDestroy() {
        mGlobalSpec.onResultCallbackListener = null
        if (mGlobalSpec.albumSetting != null) {
            mGlobalSpec.albumSetting!!.onDestroy()
        }
        if (mGlobalSpec.cameraSetting != null) {
            mGlobalSpec.cameraSetting!!.onDestroy()
        }
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
        alreadyImageCount: Int,
        alreadyVideoCount: Int,
        alreadyAudioCount: Int
    ): GlobalSetting {
        // 计算
        if (mGlobalSpec.maxSelectable != null) {
            mGlobalSpec.maxSelectable =
                mGlobalSpec.maxSelectable!! - (alreadyImageCount + alreadyVideoCount + alreadyAudioCount)
        }
        if (mGlobalSpec.maxImageSelectable != null) {
            mGlobalSpec.maxImageSelectable = mGlobalSpec.maxImageSelectable!! - alreadyImageCount
        }
        if (mGlobalSpec.maxVideoSelectable != null) {
            mGlobalSpec.maxVideoSelectable = mGlobalSpec.maxVideoSelectable!! - alreadyVideoCount
        }
        if (mGlobalSpec.maxAudioSelectable != null) {
            mGlobalSpec.maxAudioSelectable = mGlobalSpec.maxAudioSelectable!! - alreadyAudioCount
        }
        return this
    }

    override fun allStrategy(saveStrategy: SaveStrategy): GlobalSetting {
        mGlobalSpec.saveStrategy = saveStrategy
        return this
    }

    override fun pictureStrategy(saveStrategy: SaveStrategy): GlobalSetting {
        mGlobalSpec.pictureStrategy = saveStrategy
        return this
    }

    override fun videoStrategy(saveStrategy: SaveStrategy): GlobalSetting {
        mGlobalSpec.videoStrategy = saveStrategy
        return this
    }

    override fun audioStrategy(saveStrategy: SaveStrategy): GlobalSetting {
        mGlobalSpec.audioStrategy = saveStrategy
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

    override fun setOnImageCompressionInterface(listener: ImageCompressionInterface): GlobalSetting {
        mGlobalSpec.imageCompressionInterface = WeakReference(listener).get()
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
        list: ArrayList<out MultiMedia>, position: Int
    ) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(SelectedItemCollection.STATE_SELECTION, list)
        bundle.putInt(
            SelectedItemCollection.STATE_COLLECTION_TYPE,
            SelectedItemCollection.COLLECTION_IMAGE
        )
        val intent = Intent(activity, AlbumPreviewActivity::class.java)
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, list[position])
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle)
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false)
        intent.putExtra(BasePreviewActivity.EXTRA_IS_ALLOW_REPEAT, true)
        intent.putExtra(BasePreviewActivity.IS_SELECTED_CHECK, false)
        intent.putExtra(BasePreviewActivity.IS_EXTERNAL_USERS, true)
        intent.putExtra(BasePreviewActivity.IS_BY_PROGRESS_GRIDVIEW, true)
        activity.startActivityForResult(intent, requestCode)
        if (GlobalSpec.cutscenesEnabled) {
            activity.overridePendingTransition(R.anim.activity_open_zjh, 0)
        }
    }

    /**
     * 调用打开图片预览 - 纯浏览不可操作
     *
     * @param activity 窗体
     * @param list     资源id数据源
     * @param position 当前数据的索引
     */
    override fun openPreviewResourceId(activity: Activity, list: ArrayList<Int>, position: Int) {
        val multiMedias = ArrayList<MultiMedia>()
        for (item in list) {
            val multiMedia = MultiMedia()
            multiMedia.drawableId = item
            multiMedias.add(multiMedia)
        }
        openPreview(activity, multiMedias, position)
    }

    /**
     * 调用打开图片预览 - 纯浏览不可操作
     *
     * @param activity 窗体
     * @param list     文件地址的数据源
     * @param position 当前数据的索引
     */
    override fun openPreviewPath(activity: Activity, list: ArrayList<String>, position: Int) {
        val multiMedias = ArrayList<MultiMedia>()
        for (item in list) {
            val multiMedia = MultiMedia()
            multiMedia.url = item
            multiMedias.add(multiMedia)
        }
        openPreview(activity, multiMedias, position)
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

    companion object {
        /**
         * 提供给 [.openPreviewResourceId] 和 [.openPreviewPath] 共用的方法
         *
         * @param activity    窗体
         * @param multiMedias 数据源
         * @param position    当前数据的索引
         */
        private fun openPreview(
            activity: Activity,
            multiMedias: ArrayList<MultiMedia>,
            position: Int
        ) {
            val bundle = Bundle()
            bundle.putParcelableArrayList(SelectedItemCollection.STATE_SELECTION, multiMedias)
            bundle.putInt(
                SelectedItemCollection.STATE_COLLECTION_TYPE,
                SelectedItemCollection.COLLECTION_IMAGE
            )
            val intent = Intent(activity, AlbumPreviewActivity::class.java)
            intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, multiMedias[position])
            intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle)
            intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false)
            intent.putExtra(BasePreviewActivity.EXTRA_IS_ALLOW_REPEAT, true)
            intent.putExtra(BasePreviewActivity.IS_SELECTED_CHECK, false)
            intent.putExtra(BasePreviewActivity.ENABLE_OPERATION, false)
            intent.putExtra(BasePreviewActivity.IS_EXTERNAL_USERS, true)
            activity.startActivityForResult(intent, GlobalSpec.requestCode)
            if (GlobalSpec.cutscenesEnabled) {
                activity.overridePendingTransition(R.anim.activity_open_zjh, 0)
            }
        }
    }

    init {
        mGlobalSpec.setMimeTypeSet(mimeTypes)
    }
}