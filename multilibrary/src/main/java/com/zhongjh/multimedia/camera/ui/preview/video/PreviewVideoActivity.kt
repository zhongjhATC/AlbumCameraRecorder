package com.zhongjh.multimedia.camera.ui.preview.video

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.listener.VideoEditListener
import com.zhongjh.common.utils.FileUtils
import com.zhongjh.common.utils.StatusBarUtils.initStatusBar
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.databinding.ActivityPreviewVideoZjhBinding
import com.zhongjh.multimedia.settings.GlobalSpec
import com.zhongjh.multimedia.settings.GlobalSpec.orientation
import com.zhongjh.multimedia.utils.FileMediaUtil
import com.zhongjh.multimedia.utils.FileMediaUtil.createCompressFile
import com.zhongjh.multimedia.utils.MediaStoreUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.lang.ref.WeakReference
import kotlin.coroutines.resume


/**
 * 一个预览视频
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
    private var isCompressing: Boolean = false

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
        mLocalMedia.uri = intent.getStringExtra(URI) ?: ""
        mLocalMedia.absolutePath = intent.getStringExtra(PATH) ?: ""
        initView()
        initListener()
        initData()
    }

    override fun finish() {
        // 关闭窗体动画显示
        this.overridePendingTransition(0, R.anim.activity_close_zjh)
        super.finish()
    }

    override fun onDestroy() {
        mGlobalSpec.videoCompressCoordinator?.let {
            it.onCompressDestroy(this@PreviewVideoActivity.javaClass)
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
        if (intent.getBooleanExtra(IS_COMPRESS, false)) {
            mActivityPreviewVideoZjhBinding.btnConfirm.isIndeterminateProgressMode = true
        } else {
            mActivityPreviewVideoZjhBinding.btnConfirm.visibility = View.GONE
        }
        // 兼容沉倾状态栏
        ViewCompat.setOnApplyWindowInsetsListener(mActivityPreviewVideoZjhBinding.clMenu) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(
                mActivityPreviewVideoZjhBinding.clMenu.paddingLeft,
                statusBars.top,
                mActivityPreviewVideoZjhBinding.clMenu.paddingRight,
                mActivityPreviewVideoZjhBinding.clMenu.paddingBottom
            )
            val layoutParams = mActivityPreviewVideoZjhBinding.clMenu.layoutParams
            layoutParams.height += statusBars.top
            insets
        }
    }

    private fun initListener() {
        mActivityPreviewVideoZjhBinding.btnConfirm.setOnClickListener {
            if (isCompressing) {
                return@setOnClickListener
            }
            isCompressing = true
            confirm()
        }
        mActivityPreviewVideoZjhBinding.imgClose.setOnClickListener { this@PreviewVideoActivity.finish() }
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        playVideo(mLocalMedia.uri)
    }

    /**
     * 播放视频,用于录制后，在是否确认的界面中，播放视频
     */
    private fun playVideo(uri: String) {
        mActivityPreviewVideoZjhBinding.vvPreview.pause()
        // mediaController 是底部控制条
        val mediaController = MediaController(this@PreviewVideoActivity)
        mediaController.setAnchorView(mActivityPreviewVideoZjhBinding.vvPreview)
        mediaController.setMediaPlayer(mActivityPreviewVideoZjhBinding.vvPreview)
        mActivityPreviewVideoZjhBinding.vvPreview.setMediaController(mediaController)
        mActivityPreviewVideoZjhBinding.vvPreview.setVideoURI(uri.toUri())
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
        mGlobalSpec.videoCompressCoordinator?.let {
            // 如果开启了直接压缩
            val absolutePath = FileMediaUtil.prepareCompressFile(applicationContext, mLocalMedia.uri, File(mLocalMedia.absolutePath)).absolutePath
            Log.d(TAG, "confirm: absolutePath = $absolutePath")
            compress(absolutePath)
        } ?: let {
            // 否则直接提交
            confirm(mLocalMedia.absolutePath, null)
            isCompressing = false
        }
    }

    /**
     * 压缩视频
     */
    private fun compress(absolutePath: String) {
        mGlobalSpec.videoCompressCoordinator?.let { videoCompressCoordinator ->
            // 获取文件名称
            val newFile = createCompressFile(applicationContext, absolutePath)
            // 弱引用持有Activity
            val weakActivity = WeakReference(this)
            // 压缩回调
            videoCompressCoordinator.setVideoCompressListener(
                this@PreviewVideoActivity.javaClass,
                object : VideoEditListener {
                    override fun onFinish() {
                        weakActivity.get()?.let { activity ->
                            if (!activity.isFinishing && !activity.isDestroyed) {
                                activity.confirm(mLocalMedia.absolutePath, newFile.absolutePath)
                            }
                        }
                    }

                    override fun onProgress(progress: Int, progressTime: Long) {
                        weakActivity.get()?.runOnUiThread {
                            mActivityPreviewVideoZjhBinding.btnConfirm.progress = progress
                        }
                    }

                    override fun onCancel() {
                        isCompressing = false
                    }

                    override fun onError(message: String) {
                        isCompressing = false
                    }
                })
            // 执行压缩
            videoCompressCoordinator.compressRxJava(
                this@PreviewVideoActivity.javaClass, absolutePath, newFile.path
            )
        }
    }

    /**
     * 确定该视频
     */
    private fun confirm(absolutePath: String, compressPath: String?) {
        val intent = Intent()
        // 设置压缩路径
        mLocalMedia.compressPath = compressPath
        // 设置类型
        val suffix = absolutePath.substring(absolutePath.lastIndexOf("."))
        mLocalMedia.mimeType = MimeType.getMimeType(suffix)
        intent.putExtra(LOCAL_FILE, mLocalMedia)
        setResult(RESULT_OK, intent)
        this@PreviewVideoActivity.finish()
    }

    companion object {
        private val TAG: String = PreviewVideoActivity::class.java.simpleName

        const val LOCAL_FILE: String = "LOCAL_FILE"
        const val PATH: String = "PATH"
        const val URI: String = "URI"
        const val IS_COMPRESS: String = "isCompress"

        /**
         * 打开activity
         *
         * @param fragment 打开者
         * @param path     视频地址
         * @param isCompress 是否需要压缩
         */
        @JvmStatic
        fun startActivity(
            fragment: Fragment,
            previewVideoActivityResult: ActivityResultLauncher<Intent>,
            path: String, uri: String,
            isCompress: Boolean
        ) {
            fragment.activity?.let {
                val intent = Intent()
                intent.putExtra(PATH, path)
                intent.putExtra(URI, uri)
                intent.putExtra(IS_COMPRESS, isCompress)
                intent.setClass(it, PreviewVideoActivity::class.java)
                previewVideoActivityResult.launch(intent)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    fragment.activity?.overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        0,
                        R.anim.activity_open_zjh
                    )
                } else {
                    fragment.activity?.overridePendingTransition(R.anim.activity_open_zjh, 0)
                }
            }
        }
    }
}
