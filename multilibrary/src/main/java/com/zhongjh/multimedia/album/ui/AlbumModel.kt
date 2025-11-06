package com.zhongjh.multimedia.album.ui

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.lifecycle.AndroidViewModel
import com.zhongjh.common.enums.MimeType.Companion.ofImage
import com.zhongjh.common.enums.MimeType.Companion.ofVideo
import com.zhongjh.multimedia.constants.ModuleTypes
import com.zhongjh.multimedia.settings.GlobalSpec.getMimeTypeSet

/**
 * 权限状态密封类
 */
sealed class PermissionState {
    /**
     * 完全访问（所有媒体权限）
     */
    object FullAccess : PermissionState()

    /**
     * 有限访问（Android 14+ 部分媒体权限）
     */
    object LimitedAccess : PermissionState()
}

class AlbumModel(application: Application) : AndroidViewModel(application) {

    /**
     * 标记是否有编辑权限，如果有编辑，那么在onResume中会处理
     */
    var isEditPermission: Boolean = false

    /**
     * 是否有限访问的权限
     */
    fun isLimitedAccessPermission(): PermissionState {
        // 满足Android 14
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val permissions = addPermissionImagesAndVideo()
            for (permission in permissions) {
                // 有限访问允许
                if (checkSelfPermission(getApplication(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PermissionChecker.PERMISSION_GRANTED) {
                    if (permission == Manifest.permission.READ_MEDIA_IMAGES || permission == Manifest.permission.READ_MEDIA_VIDEO) {
                        return PermissionState.LimitedAccess
                    }
                }
            }
        }
        return PermissionState.FullAccess
    }

    /**
     * 根据配置来确定添加的权限类型 - 图片、视频
     */
    private fun addPermissionImagesAndVideo(): ArrayList<String> {
        val permissions = ArrayList<String>()
        if (getMimeTypeSet(ModuleTypes.ALBUM).containsAll(ofImage()) && getMimeTypeSet(ModuleTypes.ALBUM).containsAll(ofVideo())) {
            // 如果所有功能都支持视频图片，就请求视频图片权限
            if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else if (getMimeTypeSet(ModuleTypes.ALBUM).containsAll(ofImage())) {
            // 如果所有功能只支持图片，就只请求图片权限
            if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else if (getMimeTypeSet(ModuleTypes.ALBUM).containsAll(ofVideo())) {
            // 如果所有功能只支持视频，就只请求视频权限
            if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        }
        return permissions
    }

}