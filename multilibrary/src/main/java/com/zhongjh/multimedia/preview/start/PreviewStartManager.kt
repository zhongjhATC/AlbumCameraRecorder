package com.zhongjh.multimedia.preview.start

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity.OVERRIDE_TRANSITION_OPEN
import androidx.fragment.app.Fragment
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MimeType
import com.zhongjh.multimedia.MainActivity
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.camera.entity.BitmapData
import com.zhongjh.multimedia.preview.PreviewActivity
import com.zhongjh.multimedia.preview.PreviewFragment
import com.zhongjh.multimedia.preview.enum.PreviewType


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
            .setLocalMediaArrayList(localMedias)
            .setIntent(intent)
        previewActivityResult.launch(intent)
        if (cutscenesEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                activity.overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, R.anim.activity_open_zjh)
            } else {
                activity.overridePendingTransition(R.anim.activity_open_zjh, 0)
            }
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
    fun startPreviewActivityByCamera(activity: Activity, bitmapDataArray: List<BitmapData>, position: Int): Intent {
        val items = ArrayList<LocalMedia>()
        for (item in bitmapDataArray) {
            val localMedia = LocalMedia()
            localMedia.fileId = item.temporaryId
            localMedia.absolutePath = item.absolutePath
            localMedia.path = item.uri
            localMedia.mimeType = MimeType.JPEG.toString()
            items.add(localMedia)
        }

        val intent = Intent(activity, PreviewActivity::class.java)

        // 不支持原图、不支持选择时进行检查功能
        PreviewSetting(PreviewType.CAMERA)
            .setLocalMediaArrayList(items)
            .setCurrentPosition(position)
            .isOriginal(false)
            .isSelectedCheck(false)
            .setIntent(intent)
        return intent
    }
}