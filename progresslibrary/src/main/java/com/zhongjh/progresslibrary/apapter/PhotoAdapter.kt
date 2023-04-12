package com.zhongjh.progresslibrary.apapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
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
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.listener.OnMoreClickListener
import com.zhongjh.progresslibrary.R
import com.zhongjh.progresslibrary.engine.ImageEngine
import com.zhongjh.progresslibrary.entity.MultiMediaView
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener
import com.zhongjh.progresslibrary.widget.MaskProgressLayout
import com.zhongjh.progresslibrary.widget.MaskProgressView
import java.io.File
import java.util.*

/**
 * 九宫展示数据
 *
 * @author zhongjh
 * @date 2021/10/13
 *
 *
 * @param mContext            上下文
 * @param mGridLayoutManage 网格布局,用于动态计算，列数是外面动态设置的
 * @param maskProgressLayout 父控件
 * @param imageEngine 兼容各种图片加载库
 * @param placeholder 占位图
 * @param isOperation        是否操作
 * @param maxMediaCount 最多显示多少个图片/视频/语音
 * @param maskingColor       有关遮罩层：颜色
 * @param maskingTextSize    有关遮罩层：文字大小
 * @param maskingTextColor   有关遮罩层：文字颜色
 * @param maskingTextContent 有关遮罩层：文字内容
 * @param addDrawable        添加的图片资源
 */
class PhotoAdapter(
    private val mContext: Context,
    private val mGridLayoutManage: GridLayoutManager,
    private val maskProgressLayout: MaskProgressLayout,
    private val imageEngine: ImageEngine,
    private val placeholder: Drawable,
    var isOperation: Boolean,
    var maxMediaCount: Int,
    private val maskingColor: Int,
    private val maskingTextSize: Int,
    private val maskingTextColor: Int,
    private val maskingTextContent: String,
    private val deleteColor: Int,
    private val deleteImage: Drawable?,
    private val addDrawable: Drawable?
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    companion object {
        val TAG: String = PhotoAdapter::class.java.simpleName
    }

    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)

    /**
     * 这是个用于添加的九宫格
     */
    private val mMultiMediaViewAdd = MultiMediaView()

    /**
     * 相关事件
     */
    var listener: MaskProgressLayoutListener? = null

    /**
     * 数据源（包括视频和图片）
     */
    private val list = ArrayList<MultiMediaView>()

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view: View = mInflater.inflate(R.layout.item_image_progresslibrary_zjh, parent, false)
        val photoViewHolder = PhotoViewHolder(view)
        // 设置高度
        val params = photoViewHolder.itemView.layoutParams
        // 动态计算，设置item的宽高一致，总宽度-左右margin-左右padding / 总列数-item左右margin-item左右padding
        mItemHeight = mGridLayoutManage.width / mGridLayoutManage.spanCount -
                2 * photoViewHolder.itemView.paddingLeft -
                2 * (params as ViewGroup.MarginLayoutParams).leftMargin
        params.height = mItemHeight

        // 判断有没有自定义图片
        if (deleteImage != null) {
            // 使用自定义图片
            photoViewHolder.imgClose.visibility = View.VISIBLE
            photoViewHolder.imgClose.setImageDrawable(deleteImage)
            photoViewHolder.vClose = photoViewHolder.imgClose
        } else {
            // 使用自定义颜色
            photoViewHolder.vmvClose.visibility = View.VISIBLE
            val outline = photoViewHolder.vmvClose.getPathModelByName("close")
            outline.fillColor = deleteColor
            photoViewHolder.vClose = photoViewHolder.vmvClose
        }

        photoViewHolder.mpvImage.maskingColor = maskingColor
        photoViewHolder.mpvImage.textSize = maskingTextSize
        photoViewHolder.mpvImage.textColor = maskingTextColor
        photoViewHolder.mpvImage.textString = maskingTextContent

        return photoViewHolder
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // 设置图片
        if (isShowAddItem(position)) {
            // 加载➕图
            if (addDrawable != null) {
                holder.mpvImage.setImageDrawable(addDrawable)
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
                    mMultiMediaViewAdd.maskProgressView = holder.mpvImage
                    mMultiMediaViewAdd.itemView = holder.itemView
                    // 点击加载➕图
                    listener?.onItemAdd(
                        v,
                        mMultiMediaViewAdd,
                        mImageCount,
                        mVideoCount,
                        maskProgressLayout.audioList.size
                    )
                }
            })
            holder.mpvImage.reset()
        } else {
            val multiMediaView = list[position]
            if (multiMediaView.isImageOrGif() || multiMediaView.isVideo()) {
                multiMediaView.maskProgressView = holder.mpvImage
                multiMediaView.itemView = holder.itemView
            }

            // 根据类型做相关设置
            if (multiMediaView.isVideo()) {
                // 视频处理，判断是否显示播放按钮
                holder.tvVideoDuration.visibility = View.VISIBLE
                holder.tvVideoDuration.text =
                    DateUtils.formatElapsedTime(multiMediaView.duration / 1000)
            } else if (multiMediaView.isImageOrGif()) {
                holder.tvVideoDuration.visibility = View.GONE
            }

            if (multiMediaView.isGif()) {
                holder.imgGif.visibility = View.VISIBLE
            } else {
                holder.imgGif.visibility = View.GONE
            }

            holder.loadImage(mContext, imageEngine, placeholder, multiMediaView, mItemHeight)

            // 显示close
            if (isOperation) {
                holder.vClose.visibility = View.VISIBLE
                holder.vClose.setOnClickListener(object : OnMoreClickListener() {
                    override fun onListener(v: View) {
                        removePosition(multiMediaView)
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
                        if (multiMediaView.isImageOrGif()) {
                            // 如果是图片，直接跳转详情
                            listener!!.onItemClick(v, multiMediaView)
                        } else {
                            // 如果是视频，判断是否已经下载好（有path就是已经下载好了）
                            if (TextUtils.isEmpty(multiMediaView.path) && multiMediaView.uri == null) {
                                // 执行下载事件
                                val isContinue =
                                    listener!!.onItemVideoStartDownload(v, multiMediaView)
                                if (isContinue) {
                                    // 点击事件
                                    listener!!.onItemClick(v, multiMediaView)
                                }
                            } else {
                                // 点击事件
                                listener!!.onItemClick(v, multiMediaView)
                            }
                        }
                    }
                }
            })

            // 是否上传
            if (multiMediaView.isUploading) {
                // 设置该对象已经上传请求过了
                multiMediaView.isUploading = false
                listener?.onItemStartUploading(multiMediaView)
            } else {
                // 取消上传动画
                holder.mpvImage.reset()
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
        return if (list.size + maskProgressLayout.audioList.size < maxMediaCount && isOperation) {
            list.size + 1
        } else {
            list.size
        }
    }

    /**
     * 清空数据
     */
    fun clearAll() {
        list.clear()
        notifyDataSetChanged()
    }

    /**
     * @return 获取所有数据源
     */
    fun getData(): ArrayList<MultiMediaView> {
        return list
    }

    /**
     * 获取图片的数据
     */
    fun getImageData(): ArrayList<MultiMediaView> {
        val imageDates = ArrayList<MultiMediaView>()
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
    fun getVideoData(): ArrayList<MultiMediaView> {
        val videoDates = ArrayList<MultiMediaView>()
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
     * @param multiMediaViews 数据集合
     */
    fun addImageData(multiMediaViews: List<MultiMediaView>) {
        Log.d("$TAG Test", "addImageData")
        val position: Int = getNeedAddPosition(MimeType.JPEG.mimeTypeName)
        for (item in multiMediaViews) {
            item.multiMediaId = mId++
        }
        list.addAll(position, multiMediaViews)
        // 刷新ui
        notifyItemRangeInserted(position, multiMediaViews.size)
        notifyItemRangeChanged(position, multiMediaViews.size)
        isRemoveAdd()
    }

    /**
     * 赋值图片数据
     */
    fun setImageData(multiMediaViews: List<MultiMediaView>) {
        Log.d("$TAG Test", "setImageData")
        // 删除当前所有图片
        for (i in list.indices.reversed()) {
            if (list[i].isImageOrGif()) {
                list.removeAt(i)
                notifyItemRemoved(i)
            }
        }
        // 增加新的图片数据
        val position = list.size - 1
        for (item in multiMediaViews) {
            item.multiMediaId = mId++
        }
        list.addAll(multiMediaViews)
        notifyItemRangeInserted(position, multiMediaViews.size)
        notifyItemRangeChanged(position, multiMediaViews.size)
        isRemoveAdd()
    }

    /**
     * 添加视频数据
     *
     * @param multiMediaViews 数据集合
     */
    fun addVideoData(multiMediaViews: List<MultiMediaView>) {
        Log.d("$TAG Test", "addVideoData")
        val position = getNeedAddPosition(MimeType.MP4.mimeTypeName)
        for (item in multiMediaViews) {
            item.multiMediaId = mId++
        }
        list.addAll(position, multiMediaViews)
        // 刷新ui
        notifyItemRangeInserted(position, multiMediaViews.size)
        notifyItemRangeChanged(position, multiMediaViews.size)
        isRemoveAdd()
    }

    /**
     * 赋值视频数据
     */
    fun setVideoData(multiMediaViews: List<MultiMediaView>) {
        Log.d("$TAG Test", "setVideoData")
        // 删除当前所有视频
        for (i in list.indices.reversed()) {
            if (list[i].isVideo()) {
                list.removeAt(i)
                notifyItemRemoved(i)
            }
        }

        // 增加新的视频数据
        for (item in multiMediaViews) {
            item.multiMediaId = mId++
        }
        list.addAll(0, multiMediaViews)
        notifyItemRangeInserted(0, multiMediaViews.size)
        notifyItemRangeChanged(0, multiMediaViews.size)
        isRemoveAdd()
    }

    /**
     * 删除某个数据
     *
     * @param position 索引
     */
    fun removePosition(position: Int) {
        val multiMediaView = list[position]
        listener?.onItemClose(multiMediaView.itemView, multiMediaView)
        list.remove(multiMediaView)
        multiMediaView.maskProgressView.reset()
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
    }

    /**
     * 删除某个数据
     *
     * @param multiMediaView 集合里面的某个对象
     */
    private fun removePosition(multiMediaView: MultiMediaView) {
        Log.d(TAG, "multiMediaView.path：" + multiMediaView.path)
        val position = list.indexOf(multiMediaView)
        removePosition(position)
    }

    /**
     * 判断是否删除Add Item
     */
    private fun isRemoveAdd() {
        // 判断是否等于最大数量,并且是可操作的才进行去掉add
        if (list.size + maskProgressLayout.audioList.size >= maxMediaCount
            && isOperation
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
        if (MimeType.isImageOrGif(mimeType)) {
            return list.size.coerceAtLeast(0)
        } else if (MimeType.isVideo(mimeType)) {
            // 获取图片第一个索引
            return getImageFirstPosition()
        }
        return 0
    }

    /**
     * 获取列表中第一个图片的索引
     *
     * @return 索引
     */
    private fun getImageFirstPosition(): Int {
        if (list.size <= 0) {
            return 0
        }
        for (item in list) {
            if (item.isImageOrGif()) {
                return list.indexOf(item)
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
            placeholder: Drawable, multiMediaView: MultiMediaView, height: Int
        ) {
            // 加载图片
            if (multiMediaView.uri != null) {
                imageEngine.loadThumbnail(
                    context, height, placeholder,
                    mpvImage, multiMediaView.uri!!
                )
            } else if (!TextUtils.isEmpty(multiMediaView.path)) {
                imageEngine.loadThumbnail(
                    context, height, placeholder,
                    mpvImage, Uri.fromFile(File(multiMediaView.path!!))
                )
            } else if (!TextUtils.isEmpty(multiMediaView.url)) {
                imageEngine.loadUrlThumbnail(
                    context, height, placeholder,
                    mpvImage, multiMediaView.url!!
                )
            }
        }

    }


}