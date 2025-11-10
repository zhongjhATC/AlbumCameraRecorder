package com.zhongjh.multimedia.album.ui.manager


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.album.ui.AlbumFragment.ViewHolder
import com.zhongjh.multimedia.album.ui.AlbumModel
import com.zhongjh.multimedia.album.ui.PermissionState
import com.zhongjh.multimedia.model.MainModel
import com.zhongjh.multimedia.utils.SettingsPermissionUtils

/**
 * 封装 tvAlbumPermission 控件的 UI 逻辑（富文本、点击事件、权限状态控制等）
 * 控件定义位置：AlbumFragment.ViewHolder 中的 tvAlbumPermission 字段（可通过 ViewHolder 类直接跳转）
 *
 * @param context 上下文
 * @param viewHolder 视图持有者
 * @param albumModel 相册模型
 * @param mainModel 主模型
 * @param appSettingsLauncher 应用设置启动器 （用于打开应用设置界面）
 */
class TvAlbumPermissionManager(
    private val context: Context, private val viewHolder: ViewHolder,
    private val albumModel: AlbumModel, private val mainModel: MainModel
) {

    // 新增：权限设置页启动器（由外部 Fragment 注册并传入）
    private lateinit var appSettingsLauncher: ActivityResultLauncher<Intent>

    /**
     * 标记是否有编辑权限，如果有编辑，那么在onResume中会处理
     */
    private var isEditPermission: Boolean = false

    /**
     * 初始化控件及启动器
     * @param launcher 由 Fragment 注册的权限设置页启动器
     */
    fun init(launcher: ActivityResultLauncher<Intent>) {
        // 接收启动器实例
        this.appSettingsLauncher = launcher
        setupRichText()
        observePermissionState()
    }

    /**
     * 处理 onResume 时的编辑权限逻辑（从 AlbumFragment 迁移）
     */
    fun onResume() {
        if (isEditPermission) {
            // 刷新相册数据（原 AlbumFragment 的逻辑）
            mainModel.loadAllAlbum()
            // 刷新顶部view
            observePermissionState()
            // 重置标记
            isEditPermission = false
        }
    }

    /**
     * 设置富文本（下划线+可点击文本）
     */
    private fun setupRichText() {
        val fullText = "您设置了仅访问部分多媒体【点击可修改访问权限】"
        val clickableText = "【点击可修改访问权限】"
        val startIndex = fullText.indexOf(clickableText)
        val endIndex = startIndex + clickableText.length

        val spannable = SpannableString(fullText).apply {
            // 添加下划线
            setSpan(UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            // 添加点击事件
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    launchAppSettings()
                }

                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.bgColor = Color.TRANSPARENT
                    // 从主题属性获取颜色
                    val ta = context.theme.obtainStyledAttributes(intArrayOf(R.attr.album_element_color))
                    ds.color = ta.getColor(0, Color.WHITE)
                    ta.recycle()
                }
            }, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        viewHolder.tvAlbumPermission.apply {
            text = spannable
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
    }

    /**
     * 监听权限状态，控制显示/隐藏
     */
    private fun observePermissionState() {
        val currentPermissionState = albumModel.isLimitedAccessPermission()
        viewHolder.tvAlbumPermission.visibility = if (currentPermissionState == PermissionState.LimitedAccess) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    /**
     * 启动应用权限设置页
     */
    private fun launchAppSettings() {
        val packageName = context.packageName
        val settingsIntent = SettingsPermissionUtils.createAppSettingsIntent(packageName)
        // 直接使用内部启动器
        appSettingsLauncher.launch(settingsIntent)
        isEditPermission = true
    }
}