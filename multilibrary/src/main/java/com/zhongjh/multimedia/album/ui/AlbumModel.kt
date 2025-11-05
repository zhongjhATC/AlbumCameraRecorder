package com.zhongjh.multimedia.album.ui

import android.Manifest
import android.app.Application
import android.os.Build
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
     * 是否有限访问的权限
     */
    private fun isLimitedAccessPermission(): PermissionState {
        // 满足Android 14
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // 有限访问允许
            if (checkSelfPermission(getApplication(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PermissionChecker.PERMISSION_GRANTED) {
                return PermissionState.LimitedAccess
            }
        }
        return PermissionState.FullAccess
    }


}