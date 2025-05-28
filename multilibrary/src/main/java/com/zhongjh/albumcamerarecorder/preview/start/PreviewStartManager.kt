package com.zhongjh.albumcamerarecorder.preview.start

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.zhongjh.albumcamerarecorder.MainActivity
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData
import com.zhongjh.albumcamerarecorder.model.SelectedData.STATE_SELECTION
import com.zhongjh.albumcamerarecorder.preview.PreviewActivity
import com.zhongjh.albumcamerarecorder.preview.PreviewFragment
import com.zhongjh.albumcamerarecorder.preview.enum.PreviewType
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MimeType


/**
 *
 * 因为打开预览界面的方式很多种，该类单独抽取出来做处理
 */
object PreviewStartManager {
    /**
     * 相册界面 打开tPreviewActivity
     */
    @JvmStatic
    fun startPreviewActivityByAlbum(activity: Activity, cutscenesEnabled: Boolean, previewActivityResult: ActivityResultLauncher<Intent>, localMedias: ArrayList<LocalMedia>) {
        val intent = Intent(activity, PreviewActivity::class.java)
        // 支持所有功能
        PreviewSetting(PreviewType.ALBUM_ACTIVITY)
            .setData(localMedias)
            .setIntent(intent)
        previewActivityResult.launch(intent)
        if (cutscenesEnabled) {
            activity.overridePendingTransition(R.anim.activity_open_zjh, 0)
        }
    }

    /**
     * 相册界面 打开PreviewFragment
     */
    @JvmStatic
    fun startPreviewFragmentByAlbum(mainActivity: MainActivity) {
        // 隐藏底部控件
        mainActivity.showHideTableLayoutAnimator(false)
        val fragment: Fragment = PreviewFragment()
        val bundle = Bundle()
        // 支持所有功能
        PreviewSetting(PreviewType.ALBUM_FRAGMENT)
            .setBundle(bundle)
        fragment.arguments = bundle
        mainActivity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, PreviewFragment::class.java.simpleName)
            .addToBackStack(PreviewFragment::class.java.simpleName)
            .commitAllowingStateLoss()
    }

    /**
     * 拍摄界面打开tPreviewActivity
     */
    fun startPreviewActivityByCamera(activity: Activity, bitmapDataArray: List<BitmapData>, bitmapData: BitmapData): Intent {
        val items = ArrayList<LocalMedia>()
        for (item in bitmapDataArray) {
            val localMedia = LocalMedia()
            localMedia.id = item.temporaryId
            localMedia.absolutePath = item.absolutePath
            localMedia.path = item.path
            localMedia.mimeType = MimeType.JPEG.toString()
            items.add(localMedia)
        }

        val intent = Intent(activity, PreviewActivity::class.java)

        // 获取目前点击的这个item
        val item = LocalMedia()
        item.absolutePath = bitmapData.absolutePath
        item.path = bitmapData.path
        item.mimeType = MimeType.JPEG.toString()

        // 不支持原图、不支持选择时进行检查功能
        PreviewSetting(PreviewType.CAMERA)
            .setData(items)
            .setCurrentItem(item)
            .isOriginal(false)
            .isSelectedCheck(false)
            .setIntent(intent)
        return intent
    }
}