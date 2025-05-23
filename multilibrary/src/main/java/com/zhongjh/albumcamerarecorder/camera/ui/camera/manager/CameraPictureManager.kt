package com.zhongjh.albumcamerarecorder.camera.ui.camera.manager

import android.app.Activity
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData
import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment
import com.zhongjh.albumcamerarecorder.camera.ui.camera.adapter.PhotoAdapter
import com.zhongjh.albumcamerarecorder.camera.ui.camera.adapter.PhotoAdapterListener
import com.zhongjh.albumcamerarecorder.camera.ui.camera.impl.ICameraPicture
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManager
import com.zhongjh.albumcamerarecorder.camera.util.LogUtil
import com.zhongjh.albumcamerarecorder.utils.FileMediaUtil
import com.zhongjh.albumcamerarecorder.utils.FileMediaUtil.createCacheFile
import com.zhongjh.albumcamerarecorder.utils.FileMediaUtil.getOutFile
import com.zhongjh.albumcamerarecorder.utils.MediaStoreUtils
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils.imageMaxCount
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.BitmapUtils.rotateImage
import com.zhongjh.common.utils.FileUtils
import com.zhongjh.common.utils.MediaUtils
import com.zhongjh.common.utils.ThreadUtils
import com.zhongjh.imageedit.ImageEditActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume


/**
 * 图片管理类
 * 这是专门负责图片的有关逻辑
 * 涉及到图片的拍照、编辑图片、提交图片、多图系列等等都是该类负责
 *
 * @author zhongjh
 * @date 2022/8/22
 */
open class CameraPictureManager(
    @JvmField protected var baseCameraFragment: BaseCameraFragment<out CameraStateManager?, out CameraPictureManager?, out CameraVideoManager?>
) : PhotoAdapterListener, ICameraPicture {
    /**
     * 从编辑图片界面回来
     */
    private var imageEditActivityResult: ActivityResultLauncher<Intent>? = null

    /**
     * 拍照的多图片集合适配器
     */
    private lateinit var photoAdapter: PhotoAdapter

    /**
     * 图片,单图或者多图都会加入该列表
     */
    private var bitmapDataList: MutableList<BitmapData> = ArrayList()

    /**
     * 照片File,用于后面能随时删除,作用于单图
     */
    private var photoFile: File? = null

    /**
     * 编辑后的照片
     */
    private var photoEditFile: File? = null

    /**
     * 一个迁移图片的异步线程
     */
    private var movePictureFileTask: ThreadUtils.SimpleTask<ArrayList<LocalMedia>>? = null

    /**
     * 初始化有关图片的配置数据
     */
    override fun initData() {
    }

    /**
     * 初始化多图适配器
     */
    override fun initMultiplePhotoAdapter() {
        // 初始化多图适配器，先判断是不是多图配置
        photoAdapter =
            PhotoAdapter(baseCameraFragment.mainActivity, baseCameraFragment.globalSpec, bitmapDataList, this)
        baseCameraFragment.recyclerViewPhoto?.let {
            if (imageMaxCount > 1) {
                it.layoutManager = LinearLayoutManager(
                    baseCameraFragment.myContext, RecyclerView.HORIZONTAL, false
                )
                it.adapter = photoAdapter
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }

    /**
     * 初始化Activity的有关图片回调
     */
    override fun initActivityResult() {
        // 从编辑图片界面回来
        imageEditActivityResult =
            baseCameraFragment.registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let {
                        // 编辑图片界面
                        refreshEditPhoto(
                            it.getIntExtra(ImageEditActivity.EXTRA_WIDTH, 0),
                            it.getIntExtra(ImageEditActivity.EXTRA_HEIGHT, 0)
                        )
                    } ?: let {
                        return@registerForActivityResult
                    }
                }
            }
    }

    /**
     * 编辑图片事件
     */
    override fun initPhotoEditListener() {
        baseCameraFragment.photoVideoLayout.viewHolder.rlEdit.setOnClickListener { view: View ->
            val uri = view.tag as Uri
            photoEditFile = createCacheFile(baseCameraFragment.myContext, MediaType.TYPE_PICTURE)
            val intent = Intent()
            intent.setClass(baseCameraFragment.myContext, ImageEditActivity::class.java)
            intent.putExtra(
                ImageEditActivity.EXTRA_IMAGE_SCREEN_ORIENTATION, baseCameraFragment.mainActivity.requestedOrientation
            )
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, uri)
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SAVE_PATH, photoEditFile?.absolutePath)
            imageEditActivityResult?.launch(intent)
        }
    }

    /**
     * 生命周期onDestroy
     *
     * @param isCommit 是否提交了数据,如果不是提交则要删除冗余文件
     */
    override fun onDestroy(isCommit: Boolean) {
        LogUtil.i("BaseCameraPicturePresenter destroy")
        if (!isCommit) {
            photoFile?.let {
                // 删除图片
                FileUtils.deleteFile(it)
            }
            // 删除多个图片
            for (bitmapData in photoAdapter.listData) {
                FileUtils.deleteFile(bitmapData.path)
            }
        }
        cancelMovePictureFileTask()
    }

    /**
     * 拍照
     */
    override fun takePhoto() {
        // 如果已经有视频，则不允许拍照了
        if ((baseCameraFragment.cameraVideoManager?.videoTime ?: 0) <= 0) {
            // 判断数量
            if (photoAdapter.itemCount < imageMaxCount) {
                // 设置不能点击，防止多次点击报错
                baseCameraFragment.childClickableLayout.setChildClickable(false)
                baseCameraFragment.cameraManage.takePictures()
            } else {
                baseCameraFragment.photoVideoLayout.setTipAlphaAnimation(baseCameraFragment.resources.getString(R.string.z_multi_library_the_camera_limit_has_been_reached))
            }
        }
    }

    /**
     * 添加入数据源
     *
     * @param path 文件路径
     */
    override fun addCaptureData(path: String) {
        // 初始化数据并且存储进file
        val file = File(path)
        val bitmapData = BitmapData(System.currentTimeMillis(), file.path, file.path)
        // 加速回收机制
        System.gc()
        // 判断是否多个图片
        if (imageMaxCount > 1) {
            // 添加入数据源
            bitmapDataList.add(bitmapData)
            // 更新最后一个添加
            photoAdapter.notifyItemInserted(photoAdapter.itemCount - 1)
            photoAdapter.notifyItemRangeChanged(photoAdapter.itemCount - 1, photoAdapter.itemCount)
            baseCameraFragment.showMultiplePicture()
        } else {
            bitmapDataList.add(bitmapData)
            photoFile = file
            baseCameraFragment.showSinglePicture(bitmapData, file, file.path)
        }

        if (bitmapDataList.isNotEmpty()) {
            // 母窗体禁止滑动
            baseCameraFragment.mainActivity.showHideTableLayout(false)
        }

        // 回调接口：添加图片后剩下的相关数据
        baseCameraFragment.cameraSpec.onCaptureListener?.add(this.bitmapDataList, bitmapDataList.size - 1)
    }

    /**
     * 刷新多个图片
     */
    override fun refreshMultiPhoto(bitmapDataList: ArrayList<BitmapData>) {
        this.bitmapDataList = bitmapDataList
        photoAdapter.listData = this.bitmapDataList
    }

    /**
     * 刷新编辑后的单图
     *
     * @param width  最新图片的宽度
     * @param height 最新图片的高度
     */
    override fun refreshEditPhoto(width: Int, height: Int) {
        // 删除旧图
        photoFile?.let {
            if (it.exists()) {
                val wasSuccessful = it.delete()
                if (!wasSuccessful) {
                    println("was not successful.")
                }
            }
        }

        // 用编辑后的图作为新的图片
        photoFile = photoEditFile

        photoFile?.let {
            // 重置mCaptureBitmaps
            val bitmapData = BitmapData(bitmapDataList[0].temporaryId, it.path, it.path)
            bitmapDataList.clear()
            bitmapDataList.add(bitmapData)

            // 这样可以重置大小
            baseCameraFragment.singlePhotoView.isZoomable = true
            baseCameraFragment.globalSpec.imageEngine.loadUriImage(
                baseCameraFragment.myContext, baseCameraFragment.singlePhotoView, it.path
            )
        }
    }

    /**
     * 返回迁移图片的线程
     *
     * @return 迁移图片的线程
     */
    override fun getMovePictureFileTask(): ThreadUtils.SimpleTask<ArrayList<LocalMedia>> {
        movePictureFileTask = object : ThreadUtils.SimpleTask<ArrayList<LocalMedia>>() {
            override fun doInBackground(): ArrayList<LocalMedia> {
                Log.d(TAG, "doInBackground")
                return movePictureFileTaskInBackground()
            }

            override fun onSuccess(newFiles: ArrayList<LocalMedia>) {
                Log.d(TAG, "onSuccess")
                baseCameraFragment.commitPictureSuccess(newFiles)
                // 恢复预览状态
                baseCameraFragment.cameraStateManager?.state = baseCameraFragment.cameraStateManager?.preview
            }

            override fun onFail(t: Throwable) {
                Log.d(TAG, "onFail")
                baseCameraFragment.commitFail(t)
            }
        }
        return movePictureFileTask as ThreadUtils.SimpleTask<ArrayList<LocalMedia>>
    }

    /**
     * 删除临时图片
     */
    override fun deletePhotoFile() {
        if (photoFile != null) {
            FileUtils.deleteFile(photoFile)
        }
    }

    /**
     * 清除数据源
     */
    override fun clearBitmapDataList() {
        bitmapDataList.clear()
    }

    /**
     * 停止迁移图片的线程运行
     */
    override fun cancelMovePictureFileTask() {
        movePictureFileTask?.cancel()
    }

    /**
     * 点击图片事件
     *
     * @param intent 点击后，封装相关数据进入该intent
     */
    override fun onPhotoAdapterClick(intent: Intent) {
        baseCameraFragment.openAlbumPreviewActivity(intent)
    }

    /**
     * 多图进行删除的时候
     *
     * @param bitmapData 数据
     * @param position   删除的索引
     */
    override fun onPhotoAdapterDelete(bitmapData: BitmapData, position: Int) {
        // 删除文件
        FileUtils.deleteFile(bitmapData.path)

        // 判断如果删除光图片的时候，母窗体启动滑动
        if (bitmapDataList.isEmpty()) {
            baseCameraFragment.mainActivity.showHideTableLayout(true)
        }
        baseCameraFragment.cameraSpec.onCaptureListener?.remove(this.bitmapDataList, position)

        // 当列表全部删掉隐藏列表框的UI
        if (bitmapDataList.isEmpty()) {
            baseCameraFragment.hideViewByMultipleZero()
        }
    }

    /**
     * 迁移图片的线程方法
     *
     *                 // AndroidQ才加入相册数据
     *                 var uri = movePictureFileQ(cacheFile)
     *                 newFile = cacheFile
     *                 // 获取相册数据
     *                 uri?.let {
     *                     localMedia = MediaStoreUtils.getMediaDataByUri(baseCameraFragment.myContext, it)
     *                 }
     *
     * @return 迁移后的数据
     */
    private fun movePictureFileTaskInBackground(): ArrayList<LocalMedia> {
        // 每次拷贝文件后记录，最后用于全部添加到相册，回调等操作
        val newFiles = ArrayList<LocalMedia>()
        // 将 缓存文件 拷贝到 配置目录
        for (item in bitmapDataList) {
            rotateImage(baseCameraFragment.myContext, item.path)
            val cacheFile = File(item.path)
            Log.d(TAG, "1. 拍照文件：" + cacheFile.absolutePath)
            var localMedia = LocalMedia()
            // 直接迁移到相册文件夹,刷新
            val cameraFile = getOutFile(baseCameraFragment.myContext, cacheFile.name, MediaType.TYPE_PICTURE)
            val isMove = FileUtils.move(cacheFile, cameraFile)
            // 需要处理的最终文件
            val newFile = if (isMove) {
                runBlocking {
                    localMedia = mediaScanFile(cameraFile.absolutePath)
                    Log.d(TAG, "2. 获取相册数据：" + localMedia.id)
                }
                cameraFile
            } else {
                cacheFile
            }
            // 压缩图片
            val compressionFile = baseCameraFragment.globalSpec.onImageCompressionListener?.compressionFile(
                baseCameraFragment.myContext, newFile
            ) ?: let {
                newFile
            }
            Log.d(TAG, "3. 压缩图片：" + compressionFile.absolutePath)
            localMedia.compressPath = compressionFile.absolutePath
            localMedia.sandboxPath =
                FileMediaUtil.getUri(baseCameraFragment.myContext, localMedia.compressPath.toString()).toString()
            localMedia.size = compressionFile.length()
            val mediaInfo = MediaUtils.getMediaInfo(
                baseCameraFragment.myContext, MediaType.TYPE_PICTURE, compressionFile.absolutePath
            )
            localMedia.width = mediaInfo.width
            localMedia.height = mediaInfo.height
            Log.d(TAG, "4. 补充属性")
            newFiles.add(localMedia)
        }
        // 执行完成
        return newFiles
    }

    /**
     * 扫描
     * 根据真实路径返回LocalMedia
     */
    private suspend fun mediaScanFile(path: String): LocalMedia = suspendCancellableCoroutine { ctn ->
        MediaScannerConnection.scanFile(
            baseCameraFragment.myContext, arrayOf(path), MimeType.ofImageArray()
        ) { path, _ ->
            // 相册刷新完成后的回调
            ctn.resume(MediaStoreUtils.getMediaDataByPath(baseCameraFragment.myContext, path))
        }
    }

    companion object {
        private const val TAG = "CameraPictureManager"
    }
}
