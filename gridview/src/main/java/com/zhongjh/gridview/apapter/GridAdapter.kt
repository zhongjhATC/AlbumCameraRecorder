package com.zhongjh.gridview.apapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sdsmdg.harjot.vectormaster.VectorMasterView
import com.zhongjh.common.entity.GridMedia
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.listener.OnMoreClickListener
import com.zhongjh.gridview.R
import com.zhongjh.gridview.engine.ImageEngine
import com.zhongjh.gridview.entity.PhotoAdapterEntity
import com.zhongjh.gridview.listener.GridViewListener
import com.zhongjh.gridview.provider.RoundViewOutlineProvider
import com.zhongjh.gridview.widget.MaskProgressView


/**
 * 九宫展示数据
 *
 * @author zhongjh
 * @date 2021/10/13
 *
 *
 * @param mContext            上下文
 * @param mGridLayoutManage 网格布局,用于动态计算，列数是外面动态设置的
 */
class GridAdapter(private val mContext: Context, private val mGridLayoutManage: GridLayoutManager, var photoAdapterEntity: PhotoAdapterEntity) : RecyclerView.Adapter<GridAdapter.PhotoViewHolder>() {

    companion object {
        val TAG: String = GridAdapter::class.java.simpleName
        const val PHOTO_ADAPTER_PROGRESS = "PHOTO_ADAPTER_PROGRESS"
        const val TIME_UNIT: Int = 1000
    }

    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)

    /**
     * 这是个用于添加的九宫格
     */
    private val mProgressMediaAdd = GridMedia()

    /**
     * 相关事件
     */
    var listener: GridViewListener? = null

    /**
     * 数据源（包括视频和图片）
     * 这是一个不可变的数据源
     */
    private var list: List<GridMedia> = emptyList()

    /**
     * 每次添加数据增长的id，用于在相同地址的情况下区分两张图等
     */
    private var mId: Long = 0

    /**
     * 图片数据数量
     */
    private var mImageCount = 0

    /**
     * 视频数据数量
     */
    private var mVideoCount = 0

    /**
     * 音频数据数量
     */
    private var mAudioCount = 0
    private var mItemHeight: Int = 0

    init {
        clearAll()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view: View = mInflater.inflate(R.layout.item_image_zjh, parent, false)
        val photoViewHolder = PhotoViewHolder(view)
        // 设置高度
        val params = photoViewHolder.itemView.layoutParams
        // 动态计算，设置item的宽高一致，总宽度-左右margin-左右padding / 总列数-item左右margin-item左右padding
        mItemHeight =
            mGridLayoutManage.width / mGridLayoutManage.spanCount - 2 * photoViewHolder.itemView.paddingLeft - 2 * (params as ViewGroup.MarginLayoutParams).leftMargin
        params.height = mItemHeight

        // 判断有没有自定义图片
        if (photoAdapterEntity.deleteImage != null) {
            // 使用自定义图片
            photoViewHolder.imgClose.visibility = View.VISIBLE
            photoViewHolder.imgClose.setImageDrawable(photoAdapterEntity.deleteImage)
            photoViewHolder.vClose = photoViewHolder.imgClose
        } else {
            // 使用自定义颜色
            photoViewHolder.vmvClose.visibility = View.VISIBLE
            val outline = photoViewHolder.vmvClose.getPathModelByName("close")
            outline.fillColor = photoAdapterEntity.deleteColor
            photoViewHolder.vClose = photoViewHolder.vmvClose
        }

        photoViewHolder.mpvImage.maskingColor = photoAdapterEntity.masking.maskingColor
        photoViewHolder.mpvImage.textSize = photoAdapterEntity.masking.maskingTextSize
        photoViewHolder.mpvImage.textColor = photoAdapterEntity.masking.maskingTextColor
        photoViewHolder.mpvImage.textString = photoAdapterEntity.masking.maskingTextContent

        // 设置圆角
        photoViewHolder.mpvImage.outlineProvider = RoundViewOutlineProvider(this.dip2px(8).toFloat())
        photoViewHolder.mpvImage.setClipToOutline(true)
        return photoViewHolder
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // 取消上传动画
        holder.mpvImage.reset()
        // 设置图片
        if (isShowAddItem(position)) {
            showAddItem(holder)
        } else {
            val gridMedia = list[position]
            // 根据类型做相关设置,视频/音频显示时间,图片隐藏
            if (gridMedia.isVideo() || gridMedia.isAudio()) {
                // 视频处理，判断是否显示播放按钮
                holder.tvVideoDuration.visibility = View.VISIBLE
                holder.tvVideoDuration.text = DateUtils.formatElapsedTime(gridMedia.duration / TIME_UNIT)
            } else if (gridMedia.isImageOrGif()) {
                holder.tvVideoDuration.visibility = View.GONE
            }

            if (gridMedia.isGif()) {
                holder.imgGif.visibility = View.VISIBLE
            } else {
                holder.imgGif.visibility = View.GONE
            }

            if (gridMedia.isAudio()) {
                photoAdapterEntity.imageEngine.loadResourceId(
                    mContext,
                    mItemHeight,
                    photoAdapterEntity.placeholder,
                    holder.mpvImage,
                    R.drawable.baseline_audio_file_24_zhongjh
                )
            } else {
                holder.loadImage(
                    mContext, photoAdapterEntity.imageEngine, photoAdapterEntity.placeholder, gridMedia, mItemHeight
                )
            }

            // 显示close
            if (photoAdapterEntity.isOperation) {
                holder.vClose.visibility = View.VISIBLE
                holder.vClose.setOnClickListener(object : OnMoreClickListener() {
                    override fun onListener(v: View) {
                        removePosition(gridMedia)
                    }
                })
            } else {
                holder.vClose.visibility = View.GONE
            }
            // 设置条目的点击事件
            holder.itemView.setOnClickListener(object : OnMoreClickListener() {
                override fun onListener(v: View) {
                    listener?.let {
                        // 点击
                        if (gridMedia.isImageOrGif()) {
                            // 如果是图片，直接跳转详情
                            it.onItemClick(v, gridMedia)
                        } else {
                            // 如果是视频，判断是否已经下载好（有path就是已经下载好了）
                            if (TextUtils.isEmpty(gridMedia.uri)) {
                                // 执行下载事件
                                val isContinue = it.onItemStartDownload(v, gridMedia, holder.adapterPosition)
                                if (isContinue) {
                                    // 点击事件
                                    it.onItemClick(v, gridMedia)
                                }
                            } else {
                                // 点击事件
                                it.onItemClick(v, gridMedia)
                            }
                        }
                    }
                }
            })

            // 是否上传
            if (gridMedia.isUploading) {
                // 设置该对象已经上传请求过了
                gridMedia.isUploading = false
                listener?.onItemStartUploading(gridMedia, holder)
            }
        }
    }

    /**
     * 示例： https://blog.csdn.net/a1064072510/article/details/82871034
     *
     * @param holder holder
     * @param position 索引
     * @param payloads   用于标识 刷新布局里面的那个具体控件
     */
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        val progressMedia = list[position]
        for (payload in payloads) {
            when (payload) {
                // 设置进度条
                PHOTO_ADAPTER_PROGRESS -> holder.mpvImage.setPercentage(progressMedia.progress)
            }
        }
    }

    override fun getItemCount(): Int {
        // 计算图片和视频的数量
        mImageCount = 0
        mVideoCount = 0
        mAudioCount = 0
        for (item in list) {
            if (item.isImageOrGif()) {
                mImageCount++
            } else if (item.isVideo()) {
                mVideoCount++
            } else if (item.isAudio()) {
                mAudioCount++
            }
        }
        // getItemCount() 动态返回 list.size 或 list.size + 1 是导致 ViewHolder 位置不一致 的核心原因
        return list.size
    }

    /**
     * 清空数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun clearAll() {
        val newData = ArrayList<GridMedia>()
        addAddItem(newData)
        val diffResult = DiffUtil.calculateDiff(GridCallback(list, newData))
        list = newData
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * 获取所有数据源，如果最后一个是add，则去掉它
     * @return 数据源
     */
    fun getData(): ArrayList<GridMedia> {
        // 判断最后一个有没有add
        if (lastIsAddItem(list)) {
            // 创建原列表的副本，然后删除副本的最后一个元素
            val newList = ArrayList(list)
            newList.removeLast()
            return newList
        }
        return ArrayList(list)
    }

    /**
     * 根据类型获取相关数据源
     * @return 数据源
     */
    fun getData(@MediaType mediaType: Int): ArrayList<GridMedia> {
        val dates = ArrayList<GridMedia>()
        when (mediaType) {
            MediaType.TYPE_PICTURE -> {
                for (multiMediaView in list) {
                    if (multiMediaView.isImageOrGif()) {
                        dates.add(multiMediaView)
                    }
                }
            }

            MediaType.TYPE_VIDEO -> {
                for (multiMediaView in list) {
                    if (multiMediaView.isVideo()) {
                        dates.add(multiMediaView)
                    }
                }
            }

            MediaType.TYPE_AUDIO -> {
                for (multiMediaView in list) {
                    if (multiMediaView.isAudio()) {
                        dates.add(multiMediaView)
                    }
                }
            }
        }
        return dates
    }

    /**
     * 添加视频、音频、图片数据
     *
     * @param gridMedia 数据集合
     */
    fun addData(gridMedia: List<GridMedia>, @MediaType mediaType: Int) {
        Log.d("$TAG Test", "addData")
        val position: Int = getNeedAddPosition(mediaType)
        for (item in gridMedia) {
            if (!item.isAdd) {
                item.id = mId++
            }
        }

        // 转换为可变列表并插入新数据
        val mutableList = list.toMutableList()
        mutableList.addAll(position, gridMedia)
        // 判断是否保留add数据
        if (!isShowAdd(mutableList) && lastIsAddItem(mutableList)) {
            mutableList.removeAt(mutableList.size - 1)
        }
        // 用DiffUtil计算差异并更新
        val diffResult = DiffUtil.calculateDiff(GridCallback(list, mutableList))
        list = mutableList
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * 覆盖视频、音频、图片数据
     * @param gridMediaImage 图片数据源
     * @param gridMediaVideo 视频数据源
     * @param gridMediaAudio 音频数据源
     */
    fun setData(gridMediaImage: List<GridMedia>?, gridMediaVideo: List<GridMedia>?, gridMediaAudio: List<GridMedia>?) {
        val newData = ArrayList<GridMedia>()
        gridMediaVideo?.let {
            newData.addAll(gridMediaVideo)
        }
        gridMediaAudio?.let {
            newData.addAll(gridMediaAudio)
        }
        gridMediaImage?.let {
            newData.addAll(gridMediaImage)
        }
        // 增加新的音频数据
        for (item in newData) {
            if (!item.isAdd) {
                item.id = mId++
            }
        }
        addAddItem(newData)
        // 1. 计算新旧数据差异（可在子线程执行）
        val diffResult = DiffUtil.calculateDiff(GridCallback(list, newData))
        // 2. 在主线程中，先用新数据替换旧数据
        list = newData
        // 3. 最后分发差异更新
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * 更新图片/视频数据
     */
    fun updateItem(gridMedia: GridMedia) {
        for (i in list.indices) {
            if (list[i].id == gridMedia.id) {
                notifyItemChanged(i)
            }
        }
    }

    /**
     * 删除某个数据
     *
     * @param position 索引
     */
    fun removePosition(position: Int) {
        val multiMediaView = list[position]
        // 根据索引获取相关view
        listener?.onItemClose(multiMediaView)
        val mutableList = list.toMutableList()
        mutableList.remove(multiMediaView)
        addAddItem(mutableList)
        // 1. 计算新旧数据差异（可在子线程执行）
        val diffResult = DiffUtil.calculateDiff(GridCallback(list, mutableList))
        // 2. 在主线程中，先用新数据替换旧数据
        list = mutableList
        // 3. 最后分发差异更新
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * 显示AddItem
     */
    private fun showAddItem(holder: PhotoViewHolder) {
        // 加载➕图
        if (photoAdapterEntity.addDrawable != null) {
            holder.mpvImage.setImageDrawable(photoAdapterEntity.addDrawable)
        } else {
            holder.mpvImage.setImageResource(R.drawable.selector_image_add_zhongjh)
        }
        // 隐藏close
        holder.vClose.visibility = View.GONE
        holder.vClose.setOnClickListener(null)
        holder.imgGif.visibility = View.GONE
        holder.tvVideoDuration.visibility = View.GONE
        // 设置条目的点击事件
        holder.itemView.setOnClickListener(object : OnMoreClickListener() {
            override fun onListener(v: View) {
                // 点击加载➕图
                listener?.onItemAdd(v, mProgressMediaAdd, mImageCount, mVideoCount, mAudioCount)
            }
        })
        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.visibility = View.VISIBLE
    }

    /**
     * 判断是否支持显示Add,满足下面两个条件
     * 1. 数量小于配置项maxMediaCount
     * 2. 九宫控件开启了允许操作
     */
    private fun isShowAdd(list: List<GridMedia>): Boolean {
        val size = if (list.isNotEmpty()) {
            if (lastIsAddItem(list)) {
                list.size - 1
            } else {
                list.size
            }
        } else {
            0
        }

        return size < photoAdapterEntity.maxMediaCount && photoAdapterEntity.isOperation
    }

    /**
     * 添加addItem
     *
     * @param gridMedias 需要添加item的数据源
     */
    private fun addAddItem(gridMedias: MutableList<GridMedia>) {
        // 判断支持操作并且没有＋数据，则加上＋
        if (isShowAdd(gridMedias) && !lastIsAddItem(gridMedias)) {
            val gridMedia = GridMedia()
            gridMedia.isAdd = true
            gridMedias.add(gridMedia)
        }
    }

    /**
     * 最后一个是否addItem
     */
    private fun lastIsAddItem(gridMedias: List<GridMedia>): Boolean {
        return gridMedias.lastOrNull()?.isAdd ?: false
    }

    /**
     * 根据对象删除某个数据
     *
     * @param gridMedia 集合里面的某个对象
     */
    private fun removePosition(gridMedia: GridMedia) {
        Log.d(TAG, "multiMediaView.path：" + gridMedia.uri)
        val position = list.indexOf(gridMedia)
        removePosition(position)
    }

    /**
     * 根据类型获取当前要添加的位置，新增的图片在最后一个，新增的视频在图片的前面
     *
     * @param mediaType 数据类型
     * @return 索引
     */
    private fun getNeedAddPosition(@MediaType mediaType: Int): Int {
        return when (mediaType) {
            MediaType.TYPE_PICTURE ->
                if (isShowAdd(list)) {
                    list.size - 1
                } else {
                    // 数据源的最后一个
                    list.size.coerceAtLeast(0)
                }
            MediaType.TYPE_VIDEO ->
                // 视频的最后一个,如果没有视频,则是0
                getVideoLeastPosition()
            MediaType.TYPE_AUDIO ->
                // 获取图片第一个索引
                getImageFirstPosition()
            else -> 0
        }
    }

    /**
     * 获取视频的最后一个,如果没有视频,则是0
     *
     * @return 索引
     */
    private fun getVideoLeastPosition(): Int {
        if (list.isNotEmpty()) {
            for (i in list.size - 1 downTo 0) {
                if (list[i].isVideo()) {
                    return i + 1
                }
            }
        }
        return 0
    }

    /**
     * 获取列表中第一个图片的索引
     * 如果没有图片,则是最后一个
     *
     * @return 索引
     */
    private fun getImageFirstPosition(): Int {
        if (list.isNotEmpty()) {
            for (i in list.indices) {
                if (list[i].isImageOrGif()) {
                    return i
                }
            }
        }
        return if (isShowAdd(list)) {
            list.size - 1
        } else {
            list.size
        }
    }

    /**
     * 判断该item是否Add
     *
     * @param position 索引
     * @return 是否Add
     */
    private fun isShowAddItem(position: Int): Boolean {
        return list[position].isAdd
    }

    /**
     * dp转px
     */
    @Suppress("SameParameterValue")
    private fun dip2px(dp: Int): Int {
        val density: Float = mContext.resources.displayMetrics.density
        return (dp * density + 0.5).toInt()
    }

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var vClose: View
        val mpvImage: MaskProgressView = itemView.findViewById(R.id.mpvImage)
        val imgGif: ImageView = itemView.findViewById(R.id.imgGif)
        val tvVideoDuration: TextView = itemView.findViewById(R.id.tvVideoDuration)
        val vmvClose: VectorMasterView = itemView.findViewById(R.id.vmvClose)
        val imgClose: ImageView = itemView.findViewById(R.id.imgClose)

        /**
         * 加载图片
         */
        internal fun loadImage(
            context: Context, imageEngine: ImageEngine, placeholder: Drawable, gridMedia: GridMedia, height: Int
        ) {
            // 加载图片
            if (!TextUtils.isEmpty(gridMedia.getAvailablePath())) {
                imageEngine.loadPath(context, height, placeholder, mpvImage, gridMedia.getAvailablePath())
            } else if (!TextUtils.isEmpty(gridMedia.url)) {
                imageEngine.loadUrl(context, height, placeholder, mpvImage, gridMedia.url!!)
            }
        }

        /**
         * 显示进度的view
         */
        fun setProgress(progress: Int) {
            mpvImage.setPercentage(progress)
        }

    }

}