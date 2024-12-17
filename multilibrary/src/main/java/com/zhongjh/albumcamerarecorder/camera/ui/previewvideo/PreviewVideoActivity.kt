package com.zhongjh.albumcamerarecorder.camera.ui.previewvideo

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.databinding.ActivityPreviewVideoZjhBinding
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec.orientation
import com.zhongjh.albumcamerarecorder.utils.FileMediaUtil.createCompressFile
import com.zhongjh.albumcamerarecorder.utils.MediaStoreUtils
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.listener.VideoEditListener
import com.zhongjh.common.utils.StatusBarUtils.getStatusBarHeight
import com.zhongjh.common.utils.StatusBarUtils.initStatusBar
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume


/**
 * 一个预览合成分段录制的视频
 *
 * @author zhongjh
 */
class PreviewVideoActivity : AppCompatActivity() {

    private val mActivityPreviewVideoZjhBinding by lazy {
        ActivityPreviewVideoZjhBinding.inflate(layoutInflater)
    }

    /**
     * 该视频的相关参数
     */
    private var mLocalMedia: LocalMedia = LocalMedia()

    /**
     * 按钮事件运行中，因为该自定义控件如果通过setEnabled控制会导致动画不起效果，所以需要该变量控制按钮事件是否生效
     */
    private var mIsRun: Boolean = false

    /**
     * 拍摄配置
     */
    private var mGlobalSpec: GlobalSpec = GlobalSpec

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = orientation
        setTheme(mGlobalSpec.themeId)
        // 兼容沉倾状态栏
        initStatusBar(this@PreviewVideoActivity)
        super.onCreate(savedInstanceState)
        setContentView(mActivityPreviewVideoZjhBinding.root)
        intent.getStringExtra(PATH)?.let {
            mLocalMedia.absolutePath = it
            initView()
            initListener()
            initData()
        }
    }

    override fun finish() {
        // 关闭窗体动画显示
        this.overridePendingTransition(0, R.anim.activity_close_zjh)
        super.finish()
    }

    override fun onDestroy() {
        if (mGlobalSpec.isCompressEnable) {
            mGlobalSpec.videoCompressCoordinator?.onCompressDestroy(this@PreviewVideoActivity.javaClass)
            mGlobalSpec.videoCompressCoordinator = null
        }
        // 清除VideoView,防止内存泄漏
        mActivityPreviewVideoZjhBinding.vvPreview.stopPlayback()
        mActivityPreviewVideoZjhBinding.vvPreview.setOnCompletionListener(null)
        mActivityPreviewVideoZjhBinding.vvPreview.setOnPreparedListener(null)
        mActivityPreviewVideoZjhBinding.clMain.removeAllViews()
        super.onDestroy()
    }

    /**
     * 初始化View
     */
    private fun initView() {
        mActivityPreviewVideoZjhBinding.btnConfirm.isIndeterminateProgressMode = true
        // 兼容沉倾状态栏
        val statusBarHeight = getStatusBarHeight(this.applicationContext)
        mActivityPreviewVideoZjhBinding.clMenu.setPadding(
            mActivityPreviewVideoZjhBinding.clMenu.paddingLeft,
            statusBarHeight,
            mActivityPreviewVideoZjhBinding.clMenu.paddingRight,
            mActivityPreviewVideoZjhBinding.clMenu.paddingBottom
        )
        val layoutParams = mActivityPreviewVideoZjhBinding.clMenu.layoutParams
        layoutParams.height += statusBarHeight
    }

    private fun initListener() {
        mActivityPreviewVideoZjhBinding.btnConfirm.setOnClickListener {
            if (mIsRun) {
                return@setOnClickListener
            }
            mIsRun = true
            confirm()
        }
        mActivityPreviewVideoZjhBinding.imgClose.setOnClickListener { this@PreviewVideoActivity.finish() }
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        val file = File(mLocalMedia.absolutePath)
        Log.d(TAG, "exists:" + file.exists() + " length:" + file.length())
        playVideo(file)
    }

    /**
     * 播放视频,用于录制后，在是否确认的界面中，播放视频
     */
    private fun playVideo(file: File) {
        mActivityPreviewVideoZjhBinding.vvPreview.pause()
        // mediaController 是底部控制条
        val mediaController = MediaController(this@PreviewVideoActivity)
        mediaController.setAnchorView(mActivityPreviewVideoZjhBinding.vvPreview)
        mediaController.setMediaPlayer(mActivityPreviewVideoZjhBinding.vvPreview)
        mediaController.visibility = View.GONE
        mActivityPreviewVideoZjhBinding.vvPreview.setMediaController(mediaController)
        val uri = Uri.fromFile(file)
        mActivityPreviewVideoZjhBinding.vvPreview.setVideoURI(uri)
        // 这段代码需要放在更新视频文件后播放，不然会找不到文件。
        mActivityPreviewVideoZjhBinding.vvPreview.visibility = View.VISIBLE
        if (!mActivityPreviewVideoZjhBinding.vvPreview.isPlaying) {
            mActivityPreviewVideoZjhBinding.vvPreview.start()
        }
        mActivityPreviewVideoZjhBinding.vvPreview.setOnPreparedListener {
            // 获取相关参数
            mLocalMedia.duration = mActivityPreviewVideoZjhBinding.vvPreview.duration.toLong()
        }
        mActivityPreviewVideoZjhBinding.vvPreview.setOnCompletionListener {
            // 循环播放
            if (!mActivityPreviewVideoZjhBinding.vvPreview.isPlaying) {
                mActivityPreviewVideoZjhBinding.vvPreview.start()
            }
        }
    }

    /**
     * 提交
     */
    private fun confirm() {
        // 判断是否开启了视频压缩功能
        if (mGlobalSpec.isCompressEnable) {
            // 如果开启了直接压缩
            compress()
        } else {
            // 否则直接提交
            confirm(mLocalMedia.absolutePath, null)
            mIsRun = false
        }
    }

    /**
     * 压缩视频
     */
    private fun compress() {
        if (mGlobalSpec.videoCompressCoordinator != null) {
            // 获取文件名称
            val newFile = createCompressFile(applicationContext, mLocalMedia.absolutePath)
            // 压缩回调
            mGlobalSpec.videoCompressCoordinator?.setVideoCompressListener(
                this@PreviewVideoActivity.javaClass,
                object : VideoEditListener {
                    override fun onFinish() {
                        confirm(mLocalMedia.absolutePath, newFile.absolutePath)
                    }

                    override fun onProgress(progress: Int, progressTime: Long) {
                        mActivityPreviewVideoZjhBinding.btnConfirm.progress = progress
                    }

                    override fun onCancel() {
                    }

                    override fun onError(message: String) {
                        mIsRun = false
                    }
                })
            // 执行压缩
            if (mGlobalSpec.videoCompressCoordinator != null) {
                mGlobalSpec.videoCompressCoordinator?.compressRxJava(
                    this@PreviewVideoActivity.javaClass, mLocalMedia.absolutePath, newFile.path
                )
            }
        }
    }

    /**
     * 确定该视频
     */
    private fun confirm(absolutePath: String, compressPath: String?) {
        val intent = Intent()
        runBlocking {
            mLocalMedia = mediaScanFile(absolutePath)
        }
        mLocalMedia.compressPath = compressPath
        intent.putExtra(LOCAL_FILE, mLocalMedia)
        setResult(RESULT_OK, intent)
        this@PreviewVideoActivity.finish()
    }

    /**
     * 扫描
     * 根据真实路径返回LocalMedia
     */
    private suspend fun mediaScanFile(path: String): LocalMedia = suspendCancellableCoroutine { ctn ->
        MediaScannerConnection.scanFile(
            applicationContext, arrayOf(path), MimeType.ofVideoArray()
        ) { path, _ ->
            // 相册刷新完成后的回调
            ctn.resume(MediaStoreUtils.getMediaDataByPath(applicationContext, path))
        }
    }

    companion object {
        private val TAG: String = PreviewVideoActivity::class.java.simpleName

        const val LOCAL_FILE: String = "LOCAL_FILE"
        const val PATH: String = "PATH"

        /**
         * 打开activity
         *
         * @param fragment 打开者
         * @param path     视频地址
         */
        @JvmStatic
        fun startActivity(
            fragment: Fragment, previewVideoActivityResult: ActivityResultLauncher<Intent?>, path: String?
        ) {
            fragment.activity?.let {
                val intent = Intent()
                intent.putExtra(PATH, path)
                intent.setClass(it, PreviewVideoActivity::class.java)
                previewVideoActivityResult.launch(intent)
                fragment.activity?.overridePendingTransition(R.anim.activity_open_zjh, 0)
            }
        }
    }
}
