package com.zhongjh.multimedia.camera.ui.camera.manager

import android.app.Activity
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.BitmapUtils.rotateImage
import com.zhongjh.common.utils.FileUtils
import com.zhongjh.common.utils.MediaUtils
import com.zhongjh.common.utils.request
import com.zhongjh.imageedit.ImageEditActivity
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.camera.entity.BitmapData
import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.adapter.PhotoAdapter
import com.zhongjh.multimedia.camera.ui.camera.adapter.PhotoAdapterListener
import com.zhongjh.multimedia.camera.ui.camera.impl.ICameraPicture
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.camera.util.LogUtil
import com.zhongjh.multimedia.utils.FileMediaUtil.createCacheFile
import com.zhongjh.multimedia.utils.FileMediaUtil.getOutFile
import com.zhongjh.multimedia.utils.MediaStoreUtils
import com.zhongjh.multimedia.utils.SelectableUtils.imageMaxCount
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.lang.ref.WeakReference
import kotlin.coroutines.resume


/**
 * 相机图片管理类
 * 负责处理相机拍照、图片编辑、图片预览、多图管理等相机图片相关的核心逻辑
 * 实现了PhotoAdapterListener和ICameraPicture接口，提供图片数据的管理和操作功能
 *
 * @author zhongjh
 * @date 2022/8/22
 */
open class CameraPictureManager(baseCameraFragment: BaseCameraFragment<out CameraStateManager, out CameraPictureManager, out CameraVideoManager>) : PhotoAdapterListener, ICameraPicture {

    /**
     * 使用弱引用持有 CameraFragment
     * 避免在异步操作中导致的内存泄漏
     */
    val fragmentRef = WeakReference(baseCameraFragment)

    /**
     * 单图：从编辑图片界面回来的回调处理
     * 用于处理从ImageEditActivity返回的编辑结果
     */
    private var imageEditActivityResult: ActivityResultLauncher<Intent>? = null

    /**
     * 拍照的多图片集合适配器
     * 用于在多图模式下显示已拍摄的图片列表
     */
    private lateinit var photoAdapter: PhotoAdapter

    /**
     * 图片数据列表
     * 无论单图还是多图模式，所有拍摄的图片数据都会保存在此列表中
     * 存储BitmapData对象，包含图片的临时ID、URI和文件路径
     */
    private var bitmapDataList: MutableList<BitmapData> = ArrayList()

    /**
     * 当前照片文件
     * 主要用于单图模式下，存储当前正在编辑或预览的照片文件
     * 便于后续删除、编辑等操作
     */
    private var photoFile: File? = null

    /**
     * 编辑后的照片文件
     * 存储从ImageEditActivity返回的编辑结果文件
     */
    private var photoEditFile: File? = null

    /**
     * 迁移图片的异步任务
     * 负责将临时缓存的图片文件迁移到指定的存储目录
     */
    private var movePictureFileJob: Job? = null

    /**
     * 初始化多图适配器
     * 根据配置的最大图片数量决定是否显示多图列表
     * 如果maxCount>1，则初始化水平RecyclerView显示已拍摄的图片
     */
    override fun initMultiplePhotoAdapter() {
        fragmentRef.get()?.let { baseCameraFragment ->
            baseCameraFragment.mainActivity?.let { mainActivity ->
                // 初始化多图适配器，先判断是不是多图配置
                photoAdapter = PhotoAdapter(mainActivity, baseCameraFragment.globalSpec, bitmapDataList, this)
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
        }
    }

    /**
     * 初始化Activity的有关图片回调
     * 注册从图片编辑界面返回的结果处理器
     */
    override fun initActivityResult() {
        fragmentRef.get()?.let { baseCameraFragment ->
            // 从编辑图片界面回来
            imageEditActivityResult = baseCameraFragment.registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let {
                        // 编辑图片界面
                        refreshEditPhoto(
                            it.getIntExtra(ImageEditActivity.EXTRA_WIDTH, 0), it.getIntExtra(ImageEditActivity.EXTRA_HEIGHT, 0)
                        )
                    } ?: let {
                        return@registerForActivityResult
                    }
                }
            }
        }
    }

    /**
     * 初始化图片编辑事件监听器
     * 为编辑按钮设置点击事件，启动图片编辑Activity
     */
    override fun initPhotoEditListener() {
        fragmentRef.get()?.let { baseCameraFragment ->
            baseCameraFragment.photoVideoLayout.photoVideoLayoutViewHolder.rlEdit.setOnClickListener { view: View ->
                val uri = Uri.parse(view.tag.toString()).toString()
                photoEditFile = createCacheFile(baseCameraFragment.myContext, MediaType.TYPE_PICTURE)
                val intent = Intent()
                intent.setClass(baseCameraFragment.myContext, ImageEditActivity::class.java)
                intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SCREEN_ORIENTATION, baseCameraFragment.mainActivity?.requestedOrientation)
                intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, uri)
                intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SAVE_PATH, photoEditFile?.absolutePath)
                imageEditActivityResult?.launch(intent)
            }
        }

    }

    /**
     * 生命周期onDestroy
     * 清理资源，释放持有的引用
     *
     * @param isCommit 是否提交了数据,如果不是提交则要删除冗余的临时文件
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
                FileUtils.deleteFile(bitmapData.absolutePath)
            }
        }
        photoAdapter.release()
        cancelMovePictureFileTask()

        // 置空所有可能持有引用的对象
        imageEditActivityResult = null
        photoFile = null
        photoEditFile = null
    }

    /**
     * 执行拍照操作
     * 检查当前是否可以拍照（无视频正在录制、未达到最大拍摄数量）
     * 然后通过相机管理器执行实际的拍照
     */
    override fun takePhoto() {
        fragmentRef.get()?.let { baseCameraFragment ->
            // 如果已经有视频，则不允许拍照了
            if (baseCameraFragment.cameraVideoManager.videoTime <= 0) {
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
    }

    /**
     * 将拍摄的图片添加到数据源
     * 根据配置的最大图片数量决定是单图模式还是多图模式
     * 处理图片旋转，并更新UI显示
     *
     * @param path 拍摄的图片文件路径
     */
    override fun addCaptureData(path: String) {
        fragmentRef.get()?.let { baseCameraFragment ->
            rotateImage(baseCameraFragment.myContext, path)
            // 初始化数据并且存储进file
            val file = File(path)
            val uri = Uri.fromFile(file)
            val bitmapData = BitmapData(System.currentTimeMillis(), uri.toString(), file.path)
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
                baseCameraFragment.showSinglePicture(bitmapData, file, uri.toString())
            }

            if (bitmapDataList.isNotEmpty()) {
                // 母窗体禁止滑动
                baseCameraFragment.mainActivity?.showHideTableLayout(false)
            }

            // 回调接口：添加图片后剩下的相关数据
            baseCameraFragment.cameraSpec.onCaptureListener?.add(this.bitmapDataList, bitmapDataList.size - 1)
        }
    }

    /**
     * 刷新多图数据
     * 更新内部存储的图片数据列表，并通知适配器刷新UI
     *
     * @param bitmapDataList 新的图片数据列表
     */
    override fun refreshMultiPhoto(bitmapDataList: ArrayList<BitmapData>) {
        this.bitmapDataList = bitmapDataList
        photoAdapter.dispatchUpdatesTo(this.bitmapDataList)
    }

    /**
     * 刷新编辑后的单图
     * 删除原图，使用编辑后的图片替换，并更新UI显示
     *
     * @param width  编辑后图片的宽度
     * @param height 编辑后图片的高度
     */
    override fun refreshEditPhoto(width: Int, height: Int) {
        fragmentRef.get()?.let { baseCameraFragment ->
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
            // 重新赋值编辑后的图、新标签
            baseCameraFragment.photoVideoLayout.photoVideoLayoutViewHolder.rlEdit.tag = Uri.fromFile(photoFile).toString()

            photoFile?.let { photoFile ->
                // 重置mCaptureBitmaps
                val uri = Uri.fromFile(File(photoFile.path)).toString()
                val bitmapData = BitmapData(bitmapDataList[0].temporaryId, uri, photoFile.path)
                bitmapDataList.clear()
                bitmapDataList.add(bitmapData)

                // 这样可以重置大小
                baseCameraFragment.singlePhotoView.isZoomable = true
                baseCameraFragment.globalSpec.imageEngine.loadUriImage(
                    baseCameraFragment.myContext, baseCameraFragment.singlePhotoView, photoFile.path
                )
            }
        }
    }

    /**
     * 创建并启动图片迁移任务
     * 将临时缓存的图片文件迁移到指定目录，并添加到媒体库
     * 处理迁移成功或失败的回调
     */
    override fun newMovePictureFileTask() {
        movePictureFileJob = fragmentRef.get()?.lifecycleScope?.request {
            movePictureFileTaskInBackground()
        }?.onSuccess { data ->
            fragmentRef.get()?.let { baseCameraFragment ->
                Log.d(TAG, "onSuccess")
                baseCameraFragment.commitPictureSuccess(data)
                // 恢复预览状态
                baseCameraFragment.cameraStateManager.state = baseCameraFragment.cameraStateManager.preview
            }
        }?.onFail { error ->
            fragmentRef.get()?.let { baseCameraFragment ->
                // 打印堆栈日志
                Log.e(TAG, "getMovePictureFileTask")
                val stackTraceElements: Array<StackTraceElement> = error.stackTrace
                for (stackTraceElement in stackTraceElements) {
                    Log.e(TAG, stackTraceElement.toString())
                }
                baseCameraFragment.commitFail(error)
            }
        }?.launch()
    }

    /**
     * 删除临时图片文件
     * 删除当前持有的photoFile对象对应的文件
     */
    override fun deletePhotoFile() {
        photoFile?.let {
            FileUtils.deleteFile(photoFile)
        }
    }

    /**
     * 清除所有图片数据源
     * 清空bitmapDataList中的所有图片数据
     */
    override fun clearBitmapDataList() {
        bitmapDataList.clear()
    }

    /**
     * 取消图片迁移任务
     * 停止正在运行的movePictureFileJob任务
     */
    override fun cancelMovePictureFileTask() {
        movePictureFileJob?.cancel()
    }

    /**
     * 点击图片事件处理
     * 当用户点击多图适配器中的图片时调用
     *
     * @param intent 封装了图片信息的意图对象，用于启动预览界面
     */
    override fun onPhotoAdapterClick(intent: Intent) {
        fragmentRef.get()?.openAlbumPreviewActivity(intent)
    }

    /**
     * 删除多图适配器中的图片
     * 当用户在多图模式下删除图片时调用
     * 删除对应的文件和数据源，并处理相关的UI更新
     *
     * @param bitmapData 要删除的图片数据
     * @param position   要删除的图片在列表中的索引位置
     */
    override fun onPhotoAdapterDelete(bitmapData: BitmapData, position: Int) {
        fragmentRef.get()?.let { baseCameraFragment ->
            // 删除文件
            FileUtils.deleteFile(bitmapData.absolutePath)

            // 判断如果删除光图片的时候，母窗体启动滑动
            if (bitmapDataList.isEmpty()) {
                baseCameraFragment.mainActivity?.showHideTableLayout(true)
            }
            baseCameraFragment.cameraSpec.onCaptureListener?.remove(this.bitmapDataList, position)

            // 当列表全部删掉隐藏列表框的UI
            if (bitmapDataList.isEmpty()) {
                baseCameraFragment.hideViewByMultipleZero()
            }
        }

    }

    /**
     * 在后台执行图片迁移任务
     * 将临时缓存的图片文件迁移到指定的存储目录
     * 处理图片压缩、媒体库扫描等操作
     *
     * @return 迁移后生成的LocalMedia对象列表，包含完整的图片信息
     */
    private fun movePictureFileTaskInBackground(): ArrayList<LocalMedia> {
        // 每次拷贝文件后记录，最后用于全部添加到相册，回调等操作
        val newFiles = ArrayList<LocalMedia>()
        fragmentRef.get()?.let { baseCameraFragment ->
            // 将 缓存文件 拷贝到 配置目录
            for (item in bitmapDataList) {
                val cacheFile = File(item.absolutePath)
                Log.d(TAG, "1. 拍照文件：" + cacheFile.absolutePath)
                var localMedia = LocalMedia()
                // 直接迁移到相册文件夹,刷新
                val cameraFile = getOutFile(baseCameraFragment.myContext, cacheFile.name, MediaType.TYPE_PICTURE)
                val isMove = FileUtils.move(cacheFile, cameraFile)
                // 需要处理的最终文件
                val newFile = if (isMove) {
                    runBlocking {
                        localMedia = mediaScanFile(cameraFile.absolutePath)
                        Log.d(TAG, "2. 获取相册数据：" + localMedia.fileId)
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
                localMedia.size = compressionFile.length()
                val mediaInfo = MediaUtils.getMediaInfo(
                    baseCameraFragment.myContext, MediaType.TYPE_PICTURE, compressionFile.absolutePath
                )
                localMedia.width = mediaInfo.width
                localMedia.height = mediaInfo.height
                Log.d(TAG, "4. 补充属性")
                newFiles.add(localMedia)
            }
        }
        // 执行完成
        return newFiles
    }

    /**
     * 扫描媒体文件并获取媒体信息
     * 使用MediaScannerConnection扫描指定路径的文件，使其在系统相册中可见
     * 然后通过MediaStoreUtils获取完整的媒体信息
     *
     * @param path 要扫描的文件路径
     * @return 扫描后生成的LocalMedia对象，包含媒体文件的完整信息
     */
    private suspend fun mediaScanFile(path: String): LocalMedia = suspendCancellableCoroutine { ctn ->
        fragmentRef.get()?.let { baseCameraFragment ->
            MediaScannerConnection.scanFile(baseCameraFragment.myContext, arrayOf(path), MimeType.ofImageArray()) { path, _ ->
                // 相册刷新完成后的回调
                ctn.resume(MediaStoreUtils.getMediaDataByPath(baseCameraFragment.myContext, path))
            }
        }
    }

    companion object {
        private const val TAG = "CameraPictureManager"
    }
}
