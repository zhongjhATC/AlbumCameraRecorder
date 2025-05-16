package com.zhongjh.displaymedia.apapter

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sdsmdg.harjot.vectormaster.VectorMasterView
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.listener.OnMoreClickListener
import com.zhongjh.displaymedia.R
import com.zhongjh.displaymedia.engine.ImageEngine
import com.zhongjh.displaymedia.entity.DisplayMedia
import com.zhongjh.displaymedia.entity.PhotoAdapterEntity
import com.zhongjh.displaymedia.listener.DisplayMediaLayoutListener
import com.zhongjh.displaymedia.widget.MaskProgressView

/**
 * 九宫展示数据
 *
 * @author zhongjh
 * @date 2021/10/13
 *
 *
 * @param mContext            上下文
 * @param mGridLayoutManage 网格布局,用于动态计算，列数是外面动态设置的
 * @param mAudioList 音频数据源
 */
class ImagesAndVideoAdapter(
    private val mContext: Context,
    private val mGridLayoutManage: GridLayoutManager,
    private val mAudioList: ArrayList<DisplayMedia>,
    var photoAdapterEntity: PhotoAdapterEntity
) : RecyclerView.Adapter<ImagesAndVideoAdapter.PhotoViewHolder>() {

    companion object {
        val TAG: String = ImagesAndVideoAdapter::class.java.simpleName
        const val PHOTO_ADAPTER_PROGRESS = "PHOTO_ADAPTER_PROGRESS"
        const val TIME_UNIT: Int = 1000
    }

    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)

    /**
     * 这是个用于添加的九宫格
     */
    private val mProgressMediaAdd = DisplayMedia()

    /**
     * 相关事件
     */
    var listener: DisplayMediaLayoutListener? = null

    /**
     * 数据源（包括视频和图片）
     */
    private val list = ArrayList<DisplayMedia>()

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
        mItemHeight = mGridLayoutManage.width / mGridLayoutManage.spanCount -
                2 * photoViewHolder.itemView.paddingLeft -
                2 * (params as ViewGroup.MarginLayoutParams).leftMargin
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

        return photoViewHolder
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // 取消上传动画
        holder.mpvImage.reset()
        // 设置图片
        if (isShowAddItem(position)) {
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
                    listener?.onItemAdd(
                        v,
                        mProgressMediaAdd,
                        mImageCount,
                        mVideoCount,
                        mAudioList.size
                    )
                }
            })
        } else {
            val displayMedia = list[position]
            // 根据类型做相关设置
            if (displayMedia.isVideo()) {
                // 视频处理，判断是否显示播放按钮
                holder.tvVideoDuration.visibility = View.VISIBLE
                holder.tvVideoDuration.text =
                    DateUtils.formatElapsedTime(displayMedia.duration / TIME_UNIT)
            } else if (displayMedia.isImageOrGif()) {
                holder.tvVideoDuration.visibility = View.GONE
            }

            if (displayMedia.isGif()) {
                holder.imgGif.visibility = View.VISIBLE
            } else {
                holder.imgGif.visibility = View.GONE
            }

            holder.loadImage(
                mContext,
                photoAdapterEntity.imageEngine,
                photoAdapterEntity.placeholder,
                displayMedia,
                mItemHeight
            )

            // 显示close
            if (photoAdapterEntity.isOperation) {
                holder.vClose.visibility = View.VISIBLE
                holder.vClose.setOnClickListener(object : OnMoreClickListener() {
                    override fun onListener(v: View) {
                        removePosition(displayMedia)
                    }
                })
            } else {
                holder.vClose.visibility = View.GONE
            }
            // 设置条目的点击事件
            holder.itemView.setOnClickListener(object : OnMoreClickListener() {
                override fun onListener(v: View) {
                    if (listener != null) {
                        // 点击
                        if (displayMedia.isImageOrGif()) {
                            // 如果是图片，直接跳转详情
                            listener!!.onItemClick(v, displayMedia)
                        } else {
                            // 如果是视频，判断是否已经下载好（有path就是已经下载好了）
                            if (TextUtils.isEmpty(displayMedia.path)) {
                                // 执行下载事件
                                val isContinue =
                                    listener!!.onItemVideoStartDownload(
                                        v,
                                        displayMedia,
                                        holder.adapterPosition
                                    )
                                if (isContinue) {
                                    // 点击事件
                                    listener!!.onItemClick(v, displayMedia)
                                }
                            } else {
                                // 点击事件
                                listener!!.onItemClick(v, displayMedia)
                            }
                        }
                    }
                }
            })

            // 是否上传
            if (displayMedia.isUploading) {
                // 设置该对象已经上传请求过了
                displayMedia.isUploading = false
                listener?.onItemStartUploading(displayMedia, holder)
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
        holder: PhotoViewHolder,
        position: Int,
        payloads: MutableList<Any>
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
        for (item in list) {
            if (item.isImageOrGif()) {
                mImageCount++
            } else if (item.isVideo()) {
                mVideoCount++
            }
        }
        // 数量如果小于最大值并且允许操作，才+1，这个+1是最后加个可操作的Add方框
        return if (list.size + mAudioList.size < photoAdapterEntity.maxMediaCount && photoAdapterEntity.isOperation) {
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
    fun getData(): ArrayList<DisplayMedia> {
        return list
    }

    /**
     * 获取图片的数据
     */
    fun getImageData(): ArrayList<DisplayMedia> {
        val imageDates = ArrayList<DisplayMedia>()
        for (multiMediaView in list) {
            if (multiMediaView.isImageOrGif()) {
                imageDates.add(multiMediaView)
            }
        }
        return imageDates
    }

    /**
     * 获取视频的数据
     */
    fun getVideoData(): ArrayList<DisplayMedia> {
        val videoDates = ArrayList<DisplayMedia>()
        for (multiMediaView in list) {
            if (multiMediaView.isVideo()) {
                videoDates.add(multiMediaView)
            }
        }
        return videoDates
    }

    /**
     * 添加图片数据
     *
     * @param displayMedia 数据集合
     */
    fun addImageData(displayMedia: List<DisplayMedia>) {
        Log.d("$TAG Test", "addImageData")
        val position: Int = getNeedAddPosition(MimeType.JPEG.mimeTypeName)
        for (item in displayMedia) {
            item.displayMediaId = mId++
        }
        list.addAll(position, displayMedia)
        // 刷新ui
        notifyItemRangeInserted(position, displayMedia.size)
        notifyItemRangeChanged(position, displayMedia.size)
        isRemoveAdd()
    }

    /**
     * 赋值图片数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setImageData(displayMedia: List<DisplayMedia>) {
        Log.d("$TAG Test", "setImageData")
        // 删除当前所有图片
        for (i in list.indices.reversed()) {
            if (list[i].isImageOrGif()) {
                list.removeAt(i)
            }
        }
        // 增加新的图片数据
        for (item in displayMedia) {
            item.displayMediaId = mId++
        }
        list.addAll(displayMedia)
        notifyDataSetChanged()
    }

    /**
     * 添加视频数据
     *
     * @param displayMediaList 数据集合
     */
    fun addVideoData(displayMediaList: List<DisplayMedia>) {
        Log.d("$TAG Test", "addVideoData")
        val position = getNeedAddPosition(MimeType.MP4.mimeTypeName)
        for (item in displayMediaList) {
            item.displayMediaId = mId++
        }
        list.addAll(position, displayMediaList)
        // 刷新ui
        notifyItemRangeInserted(position, displayMediaList.size)
        notifyItemRangeChanged(position, displayMediaList.size)
        isRemoveAdd()
    }

    /**
     * 更新图片/视频数据
     */
    fun updateItem(displayMedia: DisplayMedia) {
        for (i in 0 until list.size) {
            if (list[i].displayMediaId == displayMedia.displayMediaId) {
                notifyItemChanged(i)
            }
        }
    }

    /**
     * 赋值视频数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setVideoData(displayMedia: List<DisplayMedia>) {
        Log.d("$TAG Test", "setVideoData")
        // 删除当前所有视频
        for (i in list.indices.reversed()) {
            if (list[i].isVideo()) {
                list.removeAt(i)
            }
        }

        // 增加新的视频数据
        for (item in displayMedia) {
            item.displayMediaId = mId++
        }
        list.addAll(0, displayMedia)
        notifyDataSetChanged()
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
     * 根据对象删除某个数据
     *
     * @param displayMedia 集合里面的某个对象
     */
    private fun removePosition(displayMedia: DisplayMedia) {
        Log.d(TAG, "multiMediaView.path：" + displayMedia.path)
        val position = list.indexOf(displayMedia)
        removePosition(position)
    }

    /**
     * 判断是否删除Add Item
     */
    private fun isRemoveAdd() {
        // 判断是否等于最大数量,并且是可操作的才进行去掉add
        if (list.size + mAudioList.size >= photoAdapterEntity.maxMediaCount
            && photoAdapterEntity.isOperation
        ) {
            notifyItemRemoved(list.size)
            notifyItemRangeChanged(list.size, list.size)
        }
    }

    /**
     * 根据类型获取当前要添加的位置，新增的图片在最后一个，新增的视频在图片的前面
     *
     * @param mimeType 数据类型
     * @return 索引
     */
    private fun getNeedAddPosition(mimeType: String): Int {
        return when {
            MimeType.isImageOrGif(mimeType) -> {
                list.size.coerceAtLeast(0)
            }

            MimeType.isVideo(mimeType) -> {
                // 获取图片第一个索引
                getImageFirstPosition()
            }

            else -> {
                0
            }
        }
    }

    /**
     * 获取列表中第一个图片的索引
     *
     * @return 索引
     */
    private fun getImageFirstPosition(): Int {
        if (list.size > 0) {
            for (item in list) {
                if (item.isImageOrGif()) {
                    return list.indexOf(item)
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
            context: Context, imageEngine: ImageEngine,
            placeholder: Drawable, displayMedia: DisplayMedia, height: Int
        ) {
            // 加载图片
            if (!TextUtils.isEmpty(displayMedia.getAvailablePath())) {
                imageEngine.loadThumbnail(
                    context, height, placeholder,
                    mpvImage, displayMedia.getAvailablePath()!!
                )
            } else if (!TextUtils.isEmpty(displayMedia.url)) {
                imageEngine.loadUrlThumbnail(
                    context, height, placeholder,
                    mpvImage, displayMedia.url!!
                )
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