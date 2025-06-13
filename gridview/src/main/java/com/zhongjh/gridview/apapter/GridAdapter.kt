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
import com.zhongjh.common.entity.LocalMedia
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
     */
    private val list = ArrayList<GridMedia>()

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

    override fun getItemViewType(position: Int): Int {
        return 0
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
                            if (TextUtils.isEmpty(gridMedia.path)) {
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
    override fun onBindViewHolder(
        holder: PhotoViewHolder, position: Int, payloads: MutableList<Any>
    ) {
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
        // 数量如果小于最大值并且允许操作，才+1，这个+1是最后加个可操作的Add方框
        return if (list.size < photoAdapterEntity.maxMediaCount && photoAdapterEntity.isOperation) {
            list.size + 1
        } else {
            list.size
        }
    }

    /**
     * 清空数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun clearAll() {
        list.clear()
        notifyDataSetChanged()
    }

    /**
     * @return 获取所有数据源
     */
    fun getDataByLocalMedia(): ArrayList<LocalMedia> {
        val data = ArrayList<LocalMedia>()
        for (multiMediaView in list) {
            data.add(multiMediaView)
        }
        return data
    }

    /**
     * @return 获取所有数据源
     */
    fun getData(): ArrayList<GridMedia> {
        return list
    }

    /**
     * @return 获取所有数据源
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
            item.id = mId++
        }
        list.addAll(position, gridMedia)
        // 刷新ui
        notifyItemRangeInserted(position, gridMedia.size)
        notifyItemRangeChanged(position, gridMedia.size)
        isRemoveAdd()
    }

    /**
     * 覆盖视频、音频、图片数据
     * @param gridMediaImage 图片数据源
     * @param gridMediaVideo 视频数据源
     * @param gridMediaAudio 音频数据源
     */
    fun setData(gridMediaImage: List<GridMedia>?, gridMediaVideo: List<GridMedia>?, gridMediaAudio: List<GridMedia>?) {
        // clone 旧数据
        val oldData = ArrayList<GridMedia>()
        for (i in list.indices.reversed()) {
            val gridMedia = GridMedia()
            gridMedia.copyGridMedia(list[i])
            oldData.add(gridMedia)
        }
        // 根据不同数据源赋值
        gridMediaImage?.let {
            setImageData(gridMediaImage)
        }
        gridMediaVideo?.let {
            setVideoData(gridMediaVideo)
        }
        gridMediaAudio?.let {
            setAudioData(gridMediaAudio)
        }
        dispatchUpdatesTo(oldData)
    }

    /**
     * 代替notifyDataSetChanged
     * @param oldData 旧数据
     */
    private fun dispatchUpdatesTo(oldData: List<GridMedia>) {
        // 计算新老数据集差异，将差异更新到Adapter
        val diffResult = DiffUtil.calculateDiff(GridCallback(oldData, list))
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * 更新图片/视频数据
     */
    fun updateItem(gridMedia: GridMedia) {
        for (i in 0 until list.size) {
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
        list.remove(multiMediaView)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size - position)
    }

    /**
     * 赋值图片数据
     *
     * @param gridMedia 数据源
     */
    private fun setImageData(gridMedia: List<GridMedia>) {
        Log.d("$TAG Test", "setImageData")
        // 删除当前所有图片
        for (i in list.indices.reversed()) {
            if (list[i].isImageOrGif()) {
                list.removeAt(i)
            }
        }
        // 增加新的图片数据
        for (item in gridMedia) {
            item.id = mId++
        }
        list.addAll(gridMedia)
    }

    /**
     * 赋值视频数据
     *
     * @param gridMedia 数据源
     */
    private fun setVideoData(gridMedia: List<GridMedia>) {
        Log.d("$TAG Test", "setVideoData")
        // 删除当前所有视频
        for (i in list.indices.reversed()) {
            if (list[i].isVideo()) {
                list.removeAt(i)
            }
        }

        // 增加新的视频数据
        for (item in gridMedia) {
            item.id = mId++
        }
        list.addAll(0, gridMedia)
    }

    /**
     * 赋值音频数据
     *
     * @param gridMedia 数据源
     */
    private fun setAudioData(gridMedia: List<GridMedia>) {
        Log.d("$TAG Test", "setAudioData")
        // 删除当前所有音频
        for (i in list.indices.reversed()) {
            if (list[i].isAudio()) {
                list.removeAt(i)
            }
        }

        // 增加新的音频数据
        for (item in gridMedia) {
            item.id = mId++
        }
        // 在视频的最后一个位置添加音频
        list.addAll(getVideoLeastPosition(), gridMedia)
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
    }

    /**
     * 根据对象删除某个数据
     *
     * @param gridMedia 集合里面的某个对象
     */
    private fun removePosition(gridMedia: GridMedia) {
        Log.d(TAG, "multiMediaView.path：" + gridMedia.path)
        val position = list.indexOf(gridMedia)
        removePosition(position)
    }

    /**
     * 判断是否删除Add Item
     */
    private fun isRemoveAdd() {
        // 判断是否等于最大数量,并且是可操作的才进行去掉add
        if (list.size >= photoAdapterEntity.maxMediaCount && photoAdapterEntity.isOperation) {
            notifyItemRemoved(list.size)
            notifyItemRangeChanged(list.size, list.size)
        }
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
                // 数据源的最后一个
                list.size.coerceAtLeast(0)

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
        if (list.size > 0) {
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
     *
     * @return 索引
     */
    private fun getImageFirstPosition(): Int {
        if (list.size > 0) {
            for (i in 0 until list.size) {
                if (list[i].isImageOrGif()) {
                    return i
                }
            }
        }
        return 0
    }

    /**
     * 判断该item是否Add
     *
     * @param position 索引
     * @return 是否Add
     */
    private fun isShowAddItem(position: Int): Boolean {
        val size = list.size
        return position == size
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