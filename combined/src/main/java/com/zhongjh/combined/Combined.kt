package com.zhongjh.combined

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.zhongjh.multimedia.settings.GlobalSetting
import com.zhongjh.multimedia.settings.GlobalSpec
import com.zhongjh.multimedia.settings.MultiMediaSetting.Companion.obtainLocalMediaResult
import com.zhongjh.common.entity.GridMedia
import com.zhongjh.gridview.apapter.GridAdapter
import com.zhongjh.gridview.listener.AbstractGridViewListener
import com.zhongjh.gridview.listener.GridViewListener
import com.zhongjh.gridview.widget.GridView

/**
 * 协调多个控件之间代码，更加简化代码
 *
 * @param activity           启动的activity
 * @param globalSetting      AlbumCameraRecorder
 * @param gridView Mask控件
 * @param listener           事件
 *
 * @author zhongjh
 * @date 2021/9/6
 */
open class Combined(
    var activity: AppCompatActivity,
    globalSetting: GlobalSetting,
    private var gridView: GridView,
    listener: AbstractGridViewListener
) {

    /**
     * 九宫格的回调
     */
    protected var requestLauncherGrid: ActivityResultLauncher<Intent> = activity.registerForActivityResult(StartActivityForResult(),
        ActivityResultCallback { result: ActivityResult ->
            if (result.resultCode != Activity.RESULT_OK) {
                return@ActivityResultCallback
            }

            result.data?.let { resultData ->
                // 获取选择的数据
                val selected = obtainLocalMediaResult(resultData)

                // 循环判断，如果不存在，则删除
                for (i in gridView.getAllData().indices.reversed()) {
                    var k = 0
                    for (localMedia in selected) {
                        if (!gridView.getAllData()[i].equalsLocalMedia(localMedia)) {
                            k++
                        }
                    }
                    if (k == selected.size) {
                        // 所有都不符合，则删除
                        gridView.removePosition(i)
                    }
                }
            }
        })

    /**
     * AlbumCameraRecorder的回调
     */
    protected var requestLauncherACR: ActivityResultLauncher<Intent> = activity.registerForActivityResult(StartActivityForResult(),
        ActivityResultCallback { result: ActivityResult ->
            if (result.resultCode != Activity.RESULT_OK) {
                return@ActivityResultCallback
            }
            result.data?.let { resultData -> // 获取选择的数据
                val data = obtainLocalMediaResult(resultData)
                gridView.addLocalFileStartUpload(data)
            }
        })

    /**
     * 最大选择数量，如果设置为null，那么能选择的总数量就是 maxImageSelectable+maxVideoSelectable+maxAudioSelectable 的总数.
     * 最大值初始化时都先缓存下来,避免globalSetting.alreadyCount多次改变了总值
     */
    private var maxSelectable: Int? = null

    /**
     * 最大图片选择数量
     * 最大值初始化时都先缓存下来,避免globalSetting.alreadyCount多次改变了总值
     */
    private var maxImageSelectable: Int? = null

    /**
     * 最大视频选择数量
     * 最大值初始化时都先缓存下来,避免globalSetting.alreadyCount多次改变了总值
     */
    private var maxVideoSelectable: Int? = null

    /**
     * 最大音频选择数量
     * 最大值初始化时都先缓存下来,避免globalSetting.alreadyCount多次改变了总值
     */
    private var maxAudioSelectable: Int? = null

    /**
     * AlbumCameraRecorder和Mask控件合并
     */
    init {
        maxSelectable = GlobalSpec.maxSelectable
        maxImageSelectable = GlobalSpec.maxImageSelectable
        maxVideoSelectable = GlobalSpec.maxVideoSelectable
        maxAudioSelectable = GlobalSpec.maxAudioSelectable
        gridView.gridViewListener = object : GridViewListener {
            override fun onItemStartDownload(view: View, gridMedia: GridMedia, position: Int): Boolean {
                return listener.onItemStartDownload(view, gridMedia, position)
            }

            override fun onItemStartUploading(
                gridMedia: GridMedia, viewHolder: GridAdapter.PhotoViewHolder
            ) {
                listener.onItemStartUploading(gridMedia, viewHolder)
            }

            override fun onItemAdd(
                view: View,
                gridMedia: GridMedia,
                alreadyImageCount: Int,
                alreadyVideoCount: Int,
                alreadyAudioCount: Int
            ) {
                // 点击Add
                globalSetting.alreadyCount(
                    maxSelectable, maxImageSelectable, maxVideoSelectable, maxAudioSelectable,
                    alreadyImageCount, alreadyVideoCount, alreadyAudioCount
                )
                globalSetting.forResult(requestLauncherACR)
                listener.onItemAdd(view, gridMedia, alreadyImageCount, alreadyVideoCount, alreadyAudioCount)
            }

            override fun onItemClick(view: View, gridMedia: GridMedia) {
                // 预览
                globalSetting.openPreviewData(activity, requestLauncherGrid, gridView.getAllData(), gridView.getAllData().indexOf(gridMedia), gridView.isOperation())
                listener.onItemClick(view, gridMedia)
            }

            override fun onItemClose(gridMedia: GridMedia) {
                listener.onItemClose(gridMedia)
            }
        }
    }
}
