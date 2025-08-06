package com.zhongjh.gridview.widget

import android.content.Context
import android.content.res.TypedArray
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.common.entity.GridMedia
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.ThreadUtils.runOnUiThread
import com.zhongjh.gridview.R
import com.zhongjh.gridview.apapter.GridAdapter
import com.zhongjh.gridview.api.GridViewApi
import com.zhongjh.gridview.engine.ImageEngine
import com.zhongjh.gridview.entity.Masking
import com.zhongjh.gridview.entity.PhotoAdapterEntity
import com.zhongjh.gridview.listener.GridViewListener


/**
 * 这是返回（图片、视频、录音）等文件后，显示的Layout
 *
 * @author zhongjh
 * @date 2018/10/17
 * https://www.jianshu.com/p/191c41f63dc7
 */
class GridView : FrameLayout, GridViewApi {

    companion object {
        const val COLUMN_NUMBER = 4
        const val MASKING_TEXT_SIZE = 12
        const val MAX_MEDIA_COUNT = 5
    }

    private lateinit var mGridAdapter: GridAdapter

    /**
     * 一些样式的属性
     */
    private val photoAdapterEntity = PhotoAdapterEntity()

    /**
     * 控件集合
     */
    private lateinit var mViewHolder: ViewHolder

    /**
     * 点击事件
     */
    var gridViewListener: GridViewListener? = null
        set(value) {
            field = value
            mGridAdapter.listener = value
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(attrs!!)
    }

    /**
     * 初始化view
     */
    private fun initView(attrs: AttributeSet) {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false)
        // 获取系统颜色
        val defaultColor = -0x1000000
        val attrsArray =
            intArrayOf(androidx.appcompat.R.attr.colorPrimary, androidx.appcompat.R.attr.colorPrimaryDark, androidx.appcompat.R.attr.colorAccent)
        val typedArray = context.obtainStyledAttributes(attrsArray)
        val colorPrimary = typedArray.getColor(0, defaultColor)

        // 获取自定义属性。
        val gridViewStyle =
            context.obtainStyledAttributes(attrs, R.styleable.GridView)
        // 是否允许操作
        photoAdapterEntity.isOperation =
            gridViewStyle.getBoolean(R.styleable.GridView_isOperation, true)
        // 一行多少列
        val columnNumber = gridViewStyle.getInteger(
            R.styleable.GridView_columnNumber,
            COLUMN_NUMBER
        )
        // 获取默认图片
        var drawable =
            gridViewStyle.getDrawable(R.styleable.GridView_placeholder)
        // 获取添加图片
        photoAdapterEntity.addDrawable =
            gridViewStyle.getDrawable(R.styleable.GridView_imageAddDrawable)
        // 获取显示图片的类
        val imageEngineStr =
            gridViewStyle.getString(R.styleable.GridView_imageEngine)
        // 获取最多显示多少个方框
        photoAdapterEntity.maxMediaCount =
            gridViewStyle.getInteger(
                R.styleable.GridView_maxCount,
                MAX_MEDIA_COUNT
            )
        photoAdapterEntity.deleteColor = gridViewStyle.getColor(
            R.styleable.GridView_imageDeleteColor,
            colorPrimary
        )
        photoAdapterEntity.deleteImage =
            gridViewStyle.getDrawable(R.styleable.GridView_imageDeleteDrawable)
        photoAdapterEntity.masking = initMaskLayerProperty(gridViewStyle, colorPrimary)
        initException(imageEngineStr)
        if (drawable == null) {
            drawable = ContextCompat.getDrawable(context, R.color.z_thumbnail_placeholder)
        }
        photoAdapterEntity.placeholder = drawable!!

        val view = inflate(context, R.layout.layout_grid_media_zjh, this)
        mViewHolder = ViewHolder(view)

        // 初始化九宫格的控件
        mViewHolder.rlGrid.layoutManager = GridLayoutManager(context, columnNumber)
        mGridAdapter = GridAdapter(
            context,
            (mViewHolder.rlGrid.layoutManager as GridLayoutManager?)!!,
            photoAdapterEntity
        )
        mViewHolder.rlGrid.adapter = mGridAdapter

        gridViewStyle.recycle()
        typedArray.recycle()
    }

    /**
     * 初始化遮罩层相关属性
     */
    private fun initMaskLayerProperty(
        gridViewStyle: TypedArray,
        colorPrimary: Int
    ): Masking {
        val maskingColor = gridViewStyle.getColor(
            R.styleable.GridView_maskingColor,
            colorPrimary
        )
        val maskingTextSize =
            gridViewStyle.getInteger(
                R.styleable.GridView_maskingTextSize,
                MASKING_TEXT_SIZE
            )

        val maskingTextColor = gridViewStyle.getColor(
            R.styleable.GridView_maskingTextColor,
            ContextCompat.getColor(context, R.color.z_thumbnail_placeholder)
        )
        var maskingTextContent =
            gridViewStyle.getString(R.styleable.GridView_maskingTextContent)
        if (maskingTextContent == null) {
            maskingTextContent = ""
        }
        return Masking(maskingColor, maskingTextSize, maskingTextColor, maskingTextContent)
    }

    /**
     * 处理异常
     */
    private fun initException(imageEngineStr: String?) {
        if (imageEngineStr == null) {
            // 必须定义image_engine属性，指定某个显示图片类
            throw NullPointerException("The image_engine attribute must be defined to specify a class for displaying images")
        } else {
            // 完整类名
            val imageEngineClass: Class<*> = Class.forName(imageEngineStr)
            photoAdapterEntity.imageEngine = imageEngineClass.newInstance() as ImageEngine
        }
    }

    override fun setPercentage(multiMedia: GridMedia, percentage: Int) {
        multiMedia.progress = percentage
        // 找出图片视频音频的 viewHolder 赋值
        runOnUiThread {
            val position = getAllData().indexOf(multiMedia)
            val photoViewHolder = mViewHolder.rlGrid.findViewHolderForAdapterPosition(position)
            photoViewHolder?.let {
                it as GridAdapter.PhotoViewHolder
                it.setProgress(percentage)
            }
        }
    }

    override fun addLocalFileStartUpload(localMediaList: List<LocalMedia>) {
        // 新添加图片的
        val mediaImages = ArrayList<GridMedia>()
        // 新添加视频的
        val mediaVideos = ArrayList<GridMedia>()
        // 新添加音频的
        val mediaAudios = ArrayList<GridMedia>()
        for (localMedia in localMediaList) {
            val gridMedia = GridMedia(localMedia)
            gridMedia.isUploading = true
            // 处理音频
            if (gridMedia.isAudio()) {
                mediaAudios.add(gridMedia)
            }
            // 处理图片
            if (gridMedia.isImageOrGif()) {
                mediaImages.add(gridMedia)
            }
            // 处理视频
            if (gridMedia.isVideo()) {
                mediaVideos.add(gridMedia)
            }
        }
        // 先加音频再加图片视频,该顺序不能打乱,因为最终是需要mImagesAndVideoAdapter来判断是否需要+号
        mGridAdapter.addData(mediaAudios, MediaType.TYPE_AUDIO)
        mGridAdapter.addData(mediaImages, MediaType.TYPE_PICTURE)
        mGridAdapter.addData(mediaVideos, MediaType.TYPE_VIDEO)
        return
    }

    override fun setUrls(imagesUrls: List<String>, videoUrls: List<String>, audioUrls: List<String>) {
        // 转换数据源
        val gridMediaImage = ArrayList<GridMedia>()
        for (string in imagesUrls) {
            val progressMedia = GridMedia(MimeType.JPEG.mimeTypeName)
            progressMedia.url = string
            gridMediaImage.add(progressMedia)
        }
        val gridMediaVideo = ArrayList<GridMedia>()
        for (i in videoUrls.indices) {
            val progressMedia = GridMedia(MimeType.MP4.mimeTypeName)
            progressMedia.isUploading = false
            progressMedia.url = videoUrls[i]
            gridMediaVideo.add(progressMedia)
        }
        val gridMediaAudio: ArrayList<GridMedia> = ArrayList()
        for (item in audioUrls) {
            val progressMedia = GridMedia(MimeType.AUDIO_MPEG.mimeTypeName)
            progressMedia.url = item
            gridMediaAudio.add(progressMedia)
        }
        mGridAdapter.setData(gridMediaImage, gridMediaVideo, gridMediaAudio)

    }

    override fun setData(gridMediaArrayList: List<GridMedia>) {
        // 新添加图片的
        val mediaImages = ArrayList<GridMedia>()
        // 新添加视频的
        val mediaVideos = ArrayList<GridMedia>()
        // 新添加音频的
        val mediaAudios = ArrayList<GridMedia>()
        for (gridMedia in gridMediaArrayList) {
            // 处理图片
            if (gridMedia.isImageOrGif()) {
                mediaImages.add(gridMedia)
            }
            // 处理视频
            if (gridMedia.isVideo()) {
                mediaVideos.add(gridMedia)
            }
            // 处理音频
            if (gridMedia.isAudio()) {
                mediaAudios.add(gridMedia)
            }
        }
        mGridAdapter.setData(mediaImages, mediaVideos, mediaAudios)
    }

    override fun setItemCover(gridMedia: GridMedia, path: String) {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(path)
        // ms,时长
        val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            ?: -1
        gridMedia.absolutePath = path
        gridMedia.uri = MediaStoreCompat.getUri(context, path).toString()
        gridMedia.duration = duration
        gridMedia.mimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE).toString()
        mmr.release()
        mGridAdapter.updateItem(gridMedia)
    }

    override fun reset() {
        // 清空数据和view
        mGridAdapter.clearAll()
    }

    override fun getAllData(): ArrayList<GridMedia> {
        return mGridAdapter.getData()
    }

    override fun getImages(): ArrayList<GridMedia> {
        return mGridAdapter.getData(MediaType.TYPE_PICTURE)
    }

    override fun getVideos(): ArrayList<GridMedia> {
        return mGridAdapter.getData(MediaType.TYPE_VIDEO)
    }

    override fun getAudios(): ArrayList<GridMedia> {
        return mGridAdapter.getData(MediaType.TYPE_AUDIO)
    }

    override fun removePosition(position: Int) {
        mGridAdapter.removePosition(position)
    }

    override fun refreshPosition(position: Int) {
        mGridAdapter.notifyItemChanged(position)
    }

    override fun setOperation(isOperation: Boolean) {
        photoAdapterEntity.isOperation = isOperation
        mGridAdapter.photoAdapterEntity.isOperation = isOperation
    }

    override fun isOperation(): Boolean {
        return photoAdapterEntity.isOperation
    }

    override fun onDestroy() {
        mGridAdapter.listener = null
    }

    /**
     * 设置最多显示多少个图片/视频/语音
     */
    fun setMaxMediaCount(maxMediaCount: Int?, maxImageSelectable: Int?, maxVideoSelectable: Int?, maxAudioSelectable: Int?) {
        check(!(maxMediaCount == null && maxImageSelectable == null)) { "setMaxMediaCount 方法中如果 maxMediaCount 为null，那么 maxImageSelectable 必须是0或者0以上数值" }
        check(!(maxMediaCount == null && maxVideoSelectable == null)) { "setMaxMediaCount 方法中如果 maxMediaCount 为null，那么 maxVideoSelectable 必须是0或者0以上数值" }
        check(!(maxMediaCount == null && maxAudioSelectable == null)) { "setMaxMediaCount 方法中如果 maxMediaCount 为null，那么 maxAudioSelectable 必须是0或者0以上数值" }
        check(!(maxMediaCount != null && maxImageSelectable != null && maxImageSelectable > maxMediaCount)) { "maxMediaCount 必须比 maxImageSelectable 大" }
        check(!(maxMediaCount != null && maxVideoSelectable != null && maxVideoSelectable > maxMediaCount)) { "maxMediaCount 必须比 maxVideoSelectable 大" }
        check(!(maxMediaCount != null && maxAudioSelectable != null && maxAudioSelectable > maxMediaCount)) { "maxMediaCount 必须比 maxAudioSelectable 大" }
        // 计算最终呈现的总数，这个总数决定是否还能点击添加
        val isMaxMediaCount = maxMediaCount != null &&
                (maxImageSelectable == null || maxVideoSelectable == null || maxAudioSelectable == null)
        if (isMaxMediaCount) {
            mGridAdapter.photoAdapterEntity.maxMediaCount = maxMediaCount!!
        } else {
            mGridAdapter.photoAdapterEntity.maxMediaCount =
                maxImageSelectable!! + maxVideoSelectable!! + maxAudioSelectable!!
        }
    }

    class ViewHolder(rootView: View) {
        val rlGrid: RecyclerView = rootView.findViewById(R.id.rlImagesAndVideo)
    }

}