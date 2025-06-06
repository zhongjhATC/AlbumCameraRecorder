package com.zhongjh.gridview.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.entity.SaveStrategy
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.ThreadUtils.runOnUiThread
import com.zhongjh.gridview.R
import com.zhongjh.gridview.apapter.GridAdapter
import com.zhongjh.gridview.api.GridViewApi
import com.zhongjh.gridview.engine.ImageEngine
import com.zhongjh.common.entity.GridMedia
import com.zhongjh.gridview.entity.Masking
import com.zhongjh.gridview.entity.PhotoAdapterEntity
import com.zhongjh.gridview.listener.GridViewListener
import java.util.*


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
    val photoAdapterEntity = PhotoAdapterEntity()

    /**
     * 控件集合
     */
    private lateinit var mViewHolder: ViewHolder

    /**
     * 文件配置路径
     */
    private lateinit var mMediaStoreCompat: MediaStoreCompat

    /**
     * 是否允许操作
     */
    private var isOperation = true

    /**
     * 音频 文件的进度条颜色
     */
    private var audioProgressColor = 0

    /**
     * 音频 删除颜色
     */
    private var audioDeleteColor = 0

    /**
     * 音频 播放按钮的颜色
     */
    private var audioPlayColor = 0

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
            intArrayOf(R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.colorAccent)
        val typedArray = context.obtainStyledAttributes(attrsArray)
        val colorPrimary = typedArray.getColor(0, defaultColor)

        // 获取自定义属性。
        val maskProgressLayoutStyle =
            context.obtainStyledAttributes(attrs, R.styleable.GridView)
        // 是否允许操作
        photoAdapterEntity.isOperation =
            maskProgressLayoutStyle.getBoolean(R.styleable.GridView_isOperation, true)
        // 一行多少列
        val columnNumber = maskProgressLayoutStyle.getInteger(
            R.styleable.GridView_columnNumber,
            COLUMN_NUMBER
        )
        // 获取默认图片
        var drawable =
            maskProgressLayoutStyle.getDrawable(R.styleable.GridView_album_thumbnail_placeholder)
        // 获取添加图片
        photoAdapterEntity.addDrawable =
            maskProgressLayoutStyle.getDrawable(R.styleable.GridView_imageAddDrawable)
        // 获取显示图片的类
        val imageEngineStr =
            maskProgressLayoutStyle.getString(R.styleable.GridView_imageEngine)
        // provider的authorities,用于提供给外部的file
        val authority = maskProgressLayoutStyle.getString(R.styleable.GridView_authority)
        val saveStrategy = SaveStrategy(true, authority, "")
        mMediaStoreCompat = MediaStoreCompat(context, saveStrategy)
        // 获取最多显示多少个方框
        photoAdapterEntity.maxMediaCount =
            maskProgressLayoutStyle.getInteger(
                R.styleable.GridView_maxCount,
                MAX_MEDIA_COUNT
            )
        photoAdapterEntity.deleteColor = maskProgressLayoutStyle.getColor(
            R.styleable.GridView_imageDeleteColor,
            colorPrimary
        )
        photoAdapterEntity.deleteImage =
            maskProgressLayoutStyle.getDrawable(R.styleable.GridView_imageDeleteDrawable)
        initAudioProperty(maskProgressLayoutStyle, colorPrimary)
        photoAdapterEntity.masking = initMaskLayerProperty(maskProgressLayoutStyle, colorPrimary)
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

        maskProgressLayoutStyle.recycle()
        typedArray.recycle()
    }

    /**
     * 初始化音频属性
     */
    private fun initAudioProperty(maskProgressLayoutStyle: TypedArray, colorPrimary: Int) {
        // 音频，删除按钮的颜色
        audioDeleteColor = maskProgressLayoutStyle.getColor(
            R.styleable.GridView_audioDeleteColor,
            colorPrimary
        )
        // 音频 文件的进度条颜色
        audioProgressColor = maskProgressLayoutStyle.getColor(
            R.styleable.GridView_audioProgressColor,
            colorPrimary
        )
        // 音频 播放按钮的颜色
        audioPlayColor = maskProgressLayoutStyle.getColor(
            R.styleable.GridView_audioPlayColor,
            colorPrimary
        )
    }

    /**
     * 初始化遮罩层相关属性
     */
    private fun initMaskLayerProperty(
        maskProgressLayoutStyle: TypedArray,
        colorPrimary: Int
    ): Masking {
        val maskingColor = maskProgressLayoutStyle.getColor(
            R.styleable.GridView_maskingColor,
            colorPrimary
        )
        val maskingTextSize =
            maskProgressLayoutStyle.getInteger(
                R.styleable.GridView_maskingTextSize,
                MASKING_TEXT_SIZE
            )

        val maskingTextColor = maskProgressLayoutStyle.getColor(
            R.styleable.GridView_maskingTextColor,
            ContextCompat.getColor(context, R.color.z_thumbnail_placeholder)
        )
        var maskingTextContent =
            maskProgressLayoutStyle.getString(R.styleable.GridView_maskingTextContent)
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

    override fun setAuthority(authority: String) {
        val saveStrategy = SaveStrategy(true, authority, "")
        mMediaStoreCompat.saveStrategy = saveStrategy
    }

    override fun addLocalFileStartUpload(localMediaList: List<LocalMedia>) {
        isAuthority()
        // 新添加图片的
        val mediaImages = ArrayList<GridMedia>()
        // 新添加视频的
        val mediaVideos = ArrayList<GridMedia>()
        // 新添加音频的
        val mediaAudios = ArrayList<GridMedia>()
        for (localMedia in localMediaList) {
            val progressMedia = GridMedia(localMedia)
            progressMedia.isUploading = true
            // 处理音频
            if (progressMedia.isAudio()) {
                mediaAudios.add(progressMedia)
            }
            // 处理图片
            if (progressMedia.isImageOrGif()) {
                mediaImages.add(progressMedia)
            }
            // 处理视频
            if (progressMedia.isVideo()) {
                mediaVideos.add(progressMedia)
            }
        }
        // 先加音频再加图片视频,该顺序不能打乱,因为最终是需要mImagesAndVideoAdapter来判断是否需要+号
        mGridAdapter.addData(mediaAudios, MediaType.TYPE_AUDIO)
        mGridAdapter.addData(mediaImages, MediaType.TYPE_PICTURE)
        mGridAdapter.addData(mediaVideos, MediaType.TYPE_VIDEO)
        return
    }

    override fun addLocalMediaListStartUploadSingle(localMediaList: List<LocalMedia>) {
//        isAuthority()
//        // 新添加图片的
//        val multiMediaViewImages = ArrayList<MultiMediaView>()
//        // 新添加视频的
//        val multiMediaViewVideos = ArrayList<MultiMediaView>()
//        val maps: Map<Long, LocalMedia> = Maps.uniqueIndex(userList, object : Function<User?, Long?>() {
//            fun apply(user: User): Long? {
//                return user.getId()
//            }
//        })
//        // 循环判断，如果不存在，则删除
//        for (i in getImagesAndVideos().indices.reversed()) {
//            var k = 0
//            for (multiMedia in selected) {
//                if (getMaskProgressLayout().getImagesAndVideos().get(i) != multiMedia) {
//                    k++
//                }
//            }
//            if (k == selected.size) {
//                // 所有都不符合，则删除
//                getMaskProgressLayout().removePosition(i)
//            }
//        }
//
//
//        for (localMedia in localMediaList) {
//
//        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun notifyDataSetChanged() {
        mGridAdapter.notifyDataSetChanged()
    }

    override fun setImageUrls(imagesUrls: List<String>, isNotifyDataSetChanged: Boolean) {
        // 转换数据源
        val progressMedias = ArrayList<GridMedia>()
        for (string in imagesUrls) {
            val progressMedia = GridMedia(MimeType.JPEG.mimeTypeName)
            progressMedia.url = string
            progressMedias.add(progressMedia)
        }
        // 增加新的图片数据
        mGridAdapter.setImageData(progressMedias, isNotifyDataSetChanged)
    }

    override fun setVideoUrls(videoUrls: List<String>, isNotifyDataSetChanged: Boolean) {
        val gridMedia = ArrayList<GridMedia>()
        for (i in videoUrls.indices) {
            val progressMedia = GridMedia(MimeType.MP4.mimeTypeName)
            progressMedia.isUploading = false
            progressMedia.url = videoUrls[i]
            gridMedia.add(progressMedia)
        }
        mGridAdapter.setVideoData(gridMedia, isNotifyDataSetChanged)
    }

    override fun setAudioUrls(audioUrls: List<String>, isNotifyDataSetChanged: Boolean) {
        val gridMedia: ArrayList<GridMedia> = ArrayList()
        for (item in audioUrls) {
            val progressMedia = GridMedia(MimeType.AUDIO_MPEG.mimeTypeName)
            progressMedia.url = item
            gridMedia.add(progressMedia)
        }
        mGridAdapter.setAudioData(gridMedia, isNotifyDataSetChanged)
    }

    override fun setImagePaths(imagePaths: List<String>) {
    }

    override fun addVideoStartUpload(videoUris: List<Uri>) {
        addVideo(videoUris, icClean = false, isUploading = true)
    }

    override fun setDataCover(gridMedia: GridMedia, path: String) {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(path)
        // ms,时长
        val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            ?: -1
        gridMedia.absolutePath = path
        gridMedia.path = mMediaStoreCompat.getUri(path).toString()
        gridMedia.duration = duration
        gridMedia.mimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE).toString()
        mmr.release()
        mGridAdapter.updateItem(gridMedia)
    }

    override fun setVideoPaths(videoPaths: List<String>) {
    }

    override fun addAudioStartUpload(filePath: String, length: Long) {
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

    fun getPhotoViewHolder(position: Int): GridAdapter.PhotoViewHolder? {
        val holder: RecyclerView.ViewHolder? = mViewHolder.rlGrid.findViewHolderForAdapterPosition(position)
        holder?.let {
            return holder as GridAdapter.PhotoViewHolder
        }
        return null
    }

    override fun removePosition(position: Int) {
        mGridAdapter.removePosition(position)
    }

    override fun refreshPosition(position: Int) {
        mGridAdapter.notifyItemChanged(position)
    }

    override fun setOperation(isOperation: Boolean) {
//        this.isOperation = isOperation
//        mImagesAndVideoAdapter.photoAdapterEntity.isOperation = isOperation
//        // 添加音频后重置所有当前播放中的音频
//        for (i in 0 until mViewHolder.llContent.childCount) {
//            val item = mViewHolder.llContent.getChildAt(i) as PlayProgressView
//            item.isOperation = isOperation
//        }
//        isShowRemoveRecorder()
    }

    override fun onDestroy() {
        mGridAdapter.listener = null
//        for (i in 0 until mViewHolder.llContent.childCount) {
//            val item = mViewHolder.llContent.getChildAt(i) as PlayProgressView
//            item.mViewHolder.playView.onDestroy()
//            item.mViewHolder.playView.listener = null
//        }
//        this.displayMediaLayoutListener = null
//        if (mCreatePlayProgressViewTask != null) {
//            ThreadUtils.cancel(mCreatePlayProgressViewTask)
//        }
    }

    /**
     * @return 最多显示多少个图片/视频/语音
     */
    fun getMaxMediaCount(): Int {
        return mGridAdapter.photoAdapterEntity.maxMediaCount
    }

    /**
     * 设置最多显示多少个图片/视频/语音
     */
    fun setMaxMediaCount(
        maxMediaCount: Int?,
        maxImageSelectable: Int?,
        maxVideoSelectable: Int?,
        maxAudioSelectable: Int?
    ) {
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

    /**
     * 添加视频地址
     *
     * @param videoUris   视频列表
     * @param icClean     是否清除
     * @param isUploading 是否触发上传事件
     */
    private fun addVideo(videoUris: List<Uri>, icClean: Boolean, isUploading: Boolean) {
        isAuthority()
        val progressMedias = ArrayList<GridMedia>()
        for (i in videoUris.indices) {
            val progressMedia = GridMedia(MimeType.MP4.mimeTypeName)
            progressMedia.path = videoUris[i].toString()
            progressMedia.isUploading = isUploading
            progressMedias.add(progressMedia)
        }
        if (icClean) {
            mGridAdapter.setVideoData(progressMedias, true)
        } else {
            mGridAdapter.addData(progressMedias, MediaType.TYPE_VIDEO)
        }
    }

    /**
     * 检测属性
     */
    private fun isAuthority() {
        if (mMediaStoreCompat.saveStrategy.authority == null) {
            // 必须定义authority属性，指定provider的authorities,用于提供给外部的file,否则Android7.0以上报错。也可以代码设置setAuthority
            val stringBuilder = StringBuffer()
            stringBuilder.append("You must define the authority attribute,")
            stringBuilder.append("which specifies the provider's authorities,")
            stringBuilder.append("to serve to external files. Otherwise, ")
            stringBuilder.append("Android7.0 will report an error.You can also set setAuthority in code")
            throw java.lang.RuntimeException(stringBuilder.toString())
        }
    }

    class ViewHolder(rootView: View) {
        val rlGrid: RecyclerView = rootView.findViewById(R.id.rlImagesAndVideo)
    }

}