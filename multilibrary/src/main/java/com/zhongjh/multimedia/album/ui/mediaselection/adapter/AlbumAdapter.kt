package com.zhongjh.multimedia.album.ui.mediaselection.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.common.entity.IncapableCause.Companion.handleCause
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.utils.LogUtil
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.album.entity.Album
import com.zhongjh.multimedia.album.entity.Album.Companion.ALBUM_ID_ALL
import com.zhongjh.multimedia.album.entity.RefreshMediaData
import com.zhongjh.multimedia.album.ui.mediaselection.adapter.widget.MediaGrid
import com.zhongjh.multimedia.album.widget.CheckView
import com.zhongjh.multimedia.constants.AlbumTypes
import com.zhongjh.multimedia.model.SelectedModel
import com.zhongjh.multimedia.settings.AlbumSpec


/**
 * 相册适配器
 *
 * @author zhongjh
 */
class AlbumAdapter(
    private val mSelectedModel: SelectedModel, private val placeholder: Drawable?, imageResize: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    MediaGrid.OnMediaGridClickListener {
    private val tag: String = this@AlbumAdapter.javaClass.simpleName

    private val mAlbumSpec = AlbumSpec
    private var data: List<LocalMedia> = ArrayList()
    private var mCheckStateListener: CheckStateListener? = null
    private var mOnMediaClickListener: OnMediaClickListener? = null
    private val mImageResize: Int

    /**
     * 专辑id,-1是全部
     */
    private var mBucketId: Long = ALBUM_ID_ALL

    /**
     * 缓存：fileId -> 选中序号/-1未选中，解决onBind线性遍历性能问题
     */
    private var fileIdCheckMap: Map<Long, Int> = emptyMap()

    init {
        LogUtil.d("onSaveInstanceState", mSelectedModel.getSelectedData().selectedItems.size.toString() + " AlbumMediaAdapter")
        mImageResize = imageResize
        // 初始化选中映射缓存
        refreshSelectCache()
    }

    /**
     * 刷新选中状态映射缓存，仅选中变更时调用，不全局刷新列表
     */
    private fun refreshSelectCache() {
        // 遍历 selectedItems 列表里每一个 SelectedItem,然后生成Map<Long, Int>,it.media.fileId作为key,it.order作为value
        fileIdCheckMap = mSelectedModel.getSelectedData().selectedItems.associate {
            it.media.fileId to it.order
        }
    }

    /**
     * 重新赋值数据
     *
     * @param bucketId 专辑id,-1是全部
     * @param refreshMediaData 数据源和比较数据工具类
     */
    fun setReloadPageMediaData(bucketId: Long, refreshMediaData: RefreshMediaData) {
        this@AlbumAdapter.data = refreshMediaData.data
        this@AlbumAdapter.mBucketId = bucketId
        refreshMediaData.diffResult.dispatchUpdatesTo(this@AlbumAdapter)
        // 刷新选中映射
        refreshSelectCache()
    }

    /**
     * 赋值合并新数据后的总数据，并刷新下拉后的数据
     */
    fun notifyDataInserted(data: List<LocalMedia>, startPosition: Int) {
        this@AlbumAdapter.data = data
        notifyItemInserted(startPosition)
        // 刷新选中映射
        refreshSelectCache()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isDisplayCamera() && position == 0) {
            // 第一个位置返回拍照类型
            AlbumTypes.CAMERA
        } else {
            // 其他位置返回图片/视频
            AlbumTypes.FILE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == AlbumTypes.CAMERA) {
            // 拍照的item
            CameraViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.media_grid_item_camera_zjh, parent, false))
        } else {
            // 相片的item
            MediaViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.media_grid_item_zjh, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        LogUtil.d("onSaveInstanceState", fileIdCheckMap.size.toString() + " onBindViewHolder")
        if (getItemViewType(position) == AlbumTypes.CAMERA) {
            holder.itemView.setOnClickListener {
                // 打开添加功能
                mOnMediaClickListener?.onOpenAddClick()
            }
        } else {
            // 相片的item
            val mediaViewHolder = holder as MediaViewHolder

            // 按钮占了位置，图片要 -1
            val realPos = if (isDisplayCamera()) {
                position - 1
            } else {
                position
            }
            val item = data[realPos]
            LogUtil.d(tag, "position: $position")
            if (position == 0) {
                LogUtil.d(tag, "path: " + item.uri)
            }
            // 传递相关的值
            mediaViewHolder.mMediaGrid.preBindMedia(MediaGrid.PreBindInfo(mImageResize, placeholder!!, mAlbumSpec.countable, holder))

            mediaViewHolder.mMediaGrid.bindMedia(item, position)
            mediaViewHolder.mMediaGrid.setOnMediaGridClickListener(this)
            setCheckStatus(item, mediaViewHolder.mMediaGrid)
        }
    }

    override fun getItemCount(): Int {
        // 如果显示拍照按钮，总数 = 图片数量 + 1
        return if (isDisplayCamera()) {
            data.size + 1
        } else {
            data.size
        }
    }

    override fun getItemId(position: Int): Long {
        // 相机按钮返回固定ID，不读取数据
        if (isDisplayCamera() && position == 0) {
            return -1000L
        }
        // 需要返回id，否则不会重复调用onBindViewHolder，因为设置了mAdapter.setHasStableIds(true)
        val realPos = if (isDisplayCamera()) position - 1 else position
        return data[realPos].fileId
    }

    fun getItem(position: Int): LocalMedia {
        val realPos = if (isDisplayCamera()) position - 1 else position
        return data[realPos]
    }

    /**
     * 设置当前选择状态
     *
     * @param item      数据
     * @param mediaGrid holder
     */
    private fun setCheckStatus(item: LocalMedia, mediaGrid: MediaGrid) {
        LogUtil.d("onSaveInstanceState", fileIdCheckMap.size.toString() + " setCheckStatus")
        // 是否多选时,显示数字 - true:选择数字,false:不显示数字
        if (mAlbumSpec.countable) {
            val checkedNum = fileIdCheckMap[item.fileId] ?: CheckView.UNCHECKED
            if (checkedNum != CheckView.UNCHECKED) {
                // 设置启用,设置数量
                setCheckEnabled(mediaGrid, true)
                mediaGrid.setCheckedNum(checkedNum)
            } else {
                // 未选中统一传UNCHECKED常量，避免0导致崩溃
                val isMax = mSelectedModel.getSelectedData().maxSelectableReached()
                setCheckEnabled(mediaGrid, !isMax)
                mediaGrid.setCheckedNum(CheckView.UNCHECKED)
            }
        } else {
            // 如果被选中了，就设置选择
            val selected = fileIdCheckMap.containsKey(item.fileId)
            if (selected) {
                setCheckEnabled(mediaGrid,true)
                mediaGrid.setChecked(true)
            } else {
                // 判断当前数量 和 当前选择最大数量比较 是否相等，相等就设置为false，否则true
                val isMax = mSelectedModel.getSelectedData().maxSelectableReached()
                setCheckEnabled(mediaGrid, !isMax)
                mediaGrid.setChecked(false)
            }
        }
    }

    override fun onThumbnailClicked(imageView: ImageView, item: LocalMedia, holder: RecyclerView.ViewHolder) {
        mOnMediaClickListener?.onMediaClick(null, imageView, item, holder.bindingAdapterPosition)
    }

    override fun onCheckViewClicked(imageView: ImageView, item: LocalMedia, context: Context, position: Int) {
        LogUtil.d("onSaveInstanceState", mSelectedModel.getSelectedData().selectedItems.size.toString() + " onCheckViewClicked")
        // onCheckViewClicked 末尾
        val selectData = mSelectedModel.getSelectedData()
        // 选择之前，是否达到上限
        val beforeMax = selectData.maxSelectableReached()
        // 是否多选模式,显示数字
        if (mAlbumSpec.countable) {
            // 获取当前选择的第几个
            val checkedNum = fileIdCheckMap[item.fileId] ?: CheckView.UNCHECKED
            if (checkedNum == CheckView.UNCHECKED) {
                // 如果当前数据是未选状态
                if (assertAddSelection(context, item)) {
                    // 动画
                    val animation = AnimationUtils.loadAnimation(context, R.anim.album_item_anim_select)
                    imageView.startAnimation(animation)
                    // 添加选择了当前数据
                    mSelectedModel.addSelectedData(item, position)
                }
            } else {
                // 删除当前选择
                mSelectedModel.removeSelectedData(item, position)
            }
        } else {
            // 不是多选模式
            if (selectData.isSelected(item)) {
                // 如果当前已经被选中，再次选择就是取消了
                mSelectedModel.removeSelectedData(item, position)
            } else {
                if (assertAddSelection(context, item)) {
                    // 动画
                    val animation = AnimationUtils.loadAnimation(context, R.anim.album_item_anim_select)
                    imageView.startAnimation(animation)
                    // 添加选择了当前数据
                    mSelectedModel.addSelectedData(item, position)
                }
            }
        }
        // 选择之后，是否达到上限
        val afterMax = selectData.maxSelectableReached()

        // 选择之前和之后是否一样,不一样
        if (beforeMax != afterMax) {
            mCheckStateListener?.onNeedRefreshVisible()
            mCheckStateListener?.onUpdate()
        } else {
            notifyItemChanged(position)
            mCheckStateListener?.onUpdate()
        }
    }

    /**
     * 刷新数据
     */
    fun notifyCheckStateChanged() {
        refreshSelectCache()
        // 不使用全量刷新，通知页面刷新屏幕可见条目
        mCheckStateListener?.onNeedRefreshVisible()
        mCheckStateListener?.onUpdate()
    }

    /**
     * 注册选择事件
     *
     * @param listener 事件
     */
    fun registerCheckStateListener(listener: CheckStateListener?) {
        mCheckStateListener = listener
    }

    /**
     * 注销选择事件
     */
    fun unregisterCheckStateListener() {
        mCheckStateListener = null
    }

    /**
     * 注册图片点击事件
     *
     * @param listener 事件
     */
    fun registerOnMediaClickListener(listener: OnMediaClickListener?) {
        mOnMediaClickListener = listener
    }

    /**
     * 注销图片点击事件
     */
    fun unregisterOnMediaClickListener() {
        mOnMediaClickListener = null
    }

    /**
     * 验证当前item是否满足可以被选中的条件
     *
     * @param context 上下文
     * @param item    数据源
     */
    private fun assertAddSelection(context: Context, item: LocalMedia): Boolean {
        val cause = mSelectedModel.getSelectedData().isAcceptable(item)
        handleCause(context, cause)
        return cause == null
    }

    /**
     * 封装配置是否启动才设置是否可选中
     */
    private fun setCheckEnabled(mediaGrid: MediaGrid, enable: Boolean) {
        if (mAlbumSpec.selectedEnable) {
            mediaGrid.setCheckEnabled(enable)
        }
    }

    /**
     * 返回是否显示相机
     */
    private fun isDisplayCamera(): Boolean {
        return mAlbumSpec.isDisplayCamera && mBucketId == ALBUM_ID_ALL
    }

    interface CheckStateListener {
        /**
         * 选择选项后更新事件
         */
        fun onUpdate()

        /**
         * 通知外部需要刷新可见区间
         */
        fun onNeedRefreshVisible()
    }

    interface OnMediaClickListener {
        /**
         * 图片/视频 点击事件
         *
         * @param album           相册集合
         * @param imageView       图片View
         * @param item            选项
         * @param adapterPosition 索引
         * @noinspection unused
         */
        fun onMediaClick(album: Album?, imageView: ImageView?, item: LocalMedia?, adapterPosition: Int)

        /**
         * 打开添加功能
         */
        fun onOpenAddClick()
    }

    private class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mMediaGrid: MediaGrid = itemView as MediaGrid
    }

    private class CameraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
