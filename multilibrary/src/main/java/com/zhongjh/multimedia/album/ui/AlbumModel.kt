package com.zhongjh.multimedia.album.ui

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
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

    fun isLimitedAccessPermission(): PermissionState {
        // Android14以下不存在部分媒体权限
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            return PermissionState.FullAccess
//        }
        val ctx = getApplication<Application>()
        val mimeSet = getMimeTypeSet(ModuleTypes.ALBUM)
        val supportImage = mimeSet.containsAll(ofImage())
        val supportVideo = mimeSet.containsAll(ofVideo())

        // 判断是否已经拿到全部需要的完整媒体权限
        var hasFullMedia = true
        if (supportImage) {
            // hasFullMedia为true并且图片是全部访问,设置hasFullMedia为true,继续下一个检查
            hasFullMedia = hasFullMedia && ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        }
        if (supportVideo) {
            // hasFullMedia为true并且视频是全部访问
            hasFullMedia = hasFullMedia && ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        }

        // 已有全部权限 → 正常模式
        if (hasFullMedia) {
            return PermissionState.FullAccess
        }

        // 没有完整权限，再进一步判断
        val hasPartial = ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
        return if (hasPartial) {
            // 用户选择【部分照片】→ LimitedAccess（展示提示条）
            PermissionState.LimitedAccess
        } else {
            // 用户直接拒绝相册权限，不展示这条提示，返回 FullAccess
            PermissionState.FullAccess
        }
    }

}