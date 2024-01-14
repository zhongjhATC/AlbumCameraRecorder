package com.zhongjh.grid.widget

import android.content.Context
import android.content.res.TypedArray
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.entity.RecordingItem
import com.zhongjh.common.entity.SaveStrategy
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.ThreadUtils
import com.zhongjh.common.utils.ThreadUtils.SimpleTask
import com.zhongjh.common.utils.ThreadUtils.runOnUiThread
import com.zhongjh.grid.R
import com.zhongjh.grid.apapter.PhotoAdapter
import com.zhongjh.grid.apapter.PhotoAdapter.Companion.PHOTO_ADAPTER_PROGRESS
import com.zhongjh.grid.api.GridApi
import com.zhongjh.grid.engine.ImageEngine
import com.zhongjh.grid.entity.Masking
import com.zhongjh.grid.entity.PhotoAdapterEntity
import com.zhongjh.grid.entity.GridMedia
import com.zhongjh.grid.listener.MaskProgressLayoutListener
import java.util.*

/**
 * 这是返回（图片、视频、录音）等文件后，显示的Layout
 *
 * @author zhongjh
 * @date 2018/10/17
 * https://www.jianshu.com/p/191c41f63dc7
 */
class GridLayout : FrameLayout, GridApi {

    companion object {
        const val COLUMN_NUMBER = 4
        const val MASKING_TEXT_SIZE = 12
        const val MAX_MEDIA_COUNT = 5
    }

    private lateinit var mPhotoAdapter: PhotoAdapter

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
     * 音频数据
     */
    val audioList = ArrayList<GridMedia>()

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
     * 点击事件(这里只针对音频)
     */
    var maskProgressLayoutListener: MaskProgressLayoutListener? = null
        set(value) {
            field = value
            mPhotoAdapter.listener = value
        }

    /**
     * 创建view的异步线程
     */
    var mCreatePlayProgressViewTask: SimpleTask<PlayProgressView>? = null

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
            context.obtainStyledAttributes(attrs, R.styleable.MaskProgressLayout)
        // 是否允许操作
        photoAdapterEntity.isOperation =
            maskProgressLayoutStyle.getBoolean(R.styleable.MaskProgressLayout_isOperation, true)
        // 一行多少列
        val columnNumber = maskProgressLayoutStyle.getInteger(
            R.styleable.MaskProgressLayout_columnNumber,
            COLUMN_NUMBER
        )
        // 获取默认图片
        var drawable =
            maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayout_album_thumbnail_placeholder)
        // 获取添加图片
        photoAdapterEntity.addDrawable =
            maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayout_imageAddDrawable)
        // 获取显示图片的类
        val imageEngineStr =
            maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayout_imageEngine)
        // provider的authorities,用于提供给外部的file
        val authority = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayout_authority)
        val saveStrategy = SaveStrategy(true, authority, "")
        mMediaStoreCompat = MediaStoreCompat(context, saveStrategy)
        // 获取最多显示多少个方框
        photoAdapterEntity.maxMediaCount =
            maskProgressLayoutStyle.getInteger(
                R.styleable.MaskProgressLayout_maxCount,
                MAX_MEDIA_COUNT
            )
        photoAdapterEntity.deleteColor = maskProgressLayoutStyle.getColor(
            R.styleable.MaskProgressLayout_imageDeleteColor,
            colorPrimary
        )
        photoAdapterEntity.deleteImage =
            maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayout_imageDeleteDrawable)
        initAudioProperty(maskProgressLayoutStyle, colorPrimary)
        photoAdapterEntity.masking = initMaskLayerProperty(maskProgressLayoutStyle, colorPrimary)
        initException(imageEngineStr)
        if (drawable == null) {
            drawable = ContextCompat.getDrawable(context, R.color.z_thumbnail_placeholder)
        }
        photoAdapterEntity.placeholder = drawable!!

        val view = inflate(context, R.layout.layout_mask_progress_zjh, this)
        mViewHolder = ViewHolder(view)

        // 初始化九宫格的控件
        mViewHolder.rlGrid.layoutManager = GridLayoutManager(context, columnNumber)

        mPhotoAdapter = PhotoAdapter(
            context,
            (mViewHolder.rlGrid.layoutManager as GridLayoutManager?)!!,
            this,
            photoAdapterEntity
        )
        mViewHolder.rlGrid.adapter = mPhotoAdapter

        maskProgressLayoutStyle.recycle()
        typedArray.recycle()
    }

    /**
     * 初始化音频属性
     */
    private fun initAudioProperty(maskProgressLayoutStyle: TypedArray, colorPrimary: Int) {
        // 音频，删除按钮的颜色
        audioDeleteColor = maskProgressLayoutStyle.getColor(
            R.styleable.MaskProgressLayout_audioDeleteColor,
            colorPrimary
        )
        // 音频 文件的进度条颜色
        audioProgressColor = maskProgressLayoutStyle.getColor(
            R.styleable.MaskProgressLayout_audioProgressColor,
            colorPrimary
        )
        // 音频 播放按钮的颜色
        audioPlayColor = maskProgressLayoutStyle.getColor(
            R.styleable.MaskProgressLayout_audioPlayColor,
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
            R.styleable.MaskProgressLayout_maskingColor,
            colorPrimary
        )
        val maskingTextSize =
            maskProgressLayoutStyle.getInteger(
                R.styleable.MaskProgressLayout_maskingTextSize,
                MASKING_TEXT_SIZE
            )

        val maskingTextColor = maskProgressLayoutStyle.getColor(
            R.styleable.MaskProgressLayout_maskingTextColor,
            ContextCompat.getColor(context, R.color.z_thumbnail_placeholder)
        )
        var maskingTextContent =
            maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayout_maskingTextContent)
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
            throw
            NullPointerException("The image_engine attribute must be defined to specify a class for displaying images")
        } else {
            // 完整类名
            val imageEngineClass: Class<*> = Class.forName(imageEngineStr)
            photoAdapterEntity.imageEngine = imageEngineClass.newInstance() as ImageEngine
        }
    }

    override fun setPercentage(multiMedia: GridMedia, percentage: Int) {
        multiMedia.progress = percentage
        val position = getData().indexOf(multiMedia)
        runOnUiThread {
            mPhotoAdapter.notifyItemChanged(position, PHOTO_ADAPTER_PROGRESS)
        }
    }

    override fun setAuthority(authority: String) {
        val saveStrategy = SaveStrategy(true, authority, "")
        mMediaStoreCompat.saveStrategy = saveStrategy
    }

    override fun addLocalFileStartUpload(localMediaList: List<LocalMedia>) {
        isAuthority()
        // 新添加图片的
        val progressMediaImages = ArrayList<GridMedia>()
        // 新添加视频的
        val progressMediaVideos = ArrayList<GridMedia>()
        for (localMedia in localMediaList) {
            val progressMedia = GridMedia(localMedia)
            progressMedia.isUploading = true
            // 直接处理音频
            if (progressMedia.isAudio()) {
                addAudioData(progressMedia)
                mPhotoAdapter.notifyItemInserted(mPhotoAdapter.getData().size - 1)
                return
            }
            // 处理图片
            if (progressMedia.isImageOrGif()) {
                progressMediaImages.add(progressMedia)
            }
            // 处理视频
            if (progressMedia.isVideo()) {
                progressMediaVideos.add(progressMedia)
            }
        }
        mPhotoAdapter.addImageData(progressMediaImages)
        mPhotoAdapter.addVideoData(progressMediaVideos)
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

    override fun setImageUrls(imagesUrls: List<String>) {
        // 转换数据源
        val progressMedias = ArrayList<GridMedia>()
        for (string in imagesUrls) {
            val progressMedia = GridMedia(MimeType.JPEG.mimeTypeName)
            progressMedia.url = string
            progressMedias.add(progressMedia)
        }
        // 赋值创建UI
        // 删除当前所有图片
        // 增加新的图片数据
        mPhotoAdapter.setImageData(progressMedias)
        maskProgressLayoutListener?.onAddDataSuccess(progressMedias)
    }

    override fun addVideoStartUpload(videoUris: List<Uri>) {
        addVideo(videoUris, icClean = false, isUploading = true)
    }

    override fun setVideoCover(gridMedia: GridMedia, videoPath: String) {
        gridMedia.path = videoPath
    }

    override fun setVideoUrls(videoUrls: List<String>) {
        val progressMedias = ArrayList<GridMedia>()
        for (i in videoUrls.indices) {
            val progressMedia = GridMedia(MimeType.MP4.mimeTypeName)
            progressMedia.isUploading = false
            progressMedia.url = videoUrls[i]
            progressMedias.add(progressMedia)
        }
        mPhotoAdapter.setVideoData(progressMedias)
        maskProgressLayoutListener?.onAddDataSuccess(progressMedias)
    }

    override fun setAudioUrls(audioUrls: List<String>) {
        val gridMedia: ArrayList<GridMedia> = ArrayList()
        for (item in audioUrls) {
            val progressMedia = GridMedia(MimeType.AAC.mimeTypeName)
            progressMedia.url = item
            audioList.add(progressMedia)
            gridMedia.add(progressMedia)
        }
        createPlayProgressView(gridMedia, 0)
    }

    override fun setAudioCover(view: View, file: String) {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(file)

        // ms,时长
        val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            ?: -1
        val progressMedia = GridMedia(MimeType.AAC.mimeTypeName)
        progressMedia.absolutePath = file
        progressMedia.path = mMediaStoreCompat.getUri(file).toString()
        progressMedia.duration = duration
        progressMedia.mimeType =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE).toString()

        // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
        view.visibility = View.VISIBLE
        isShowRemoveRecorder()
        val recordingItem = RecordingItem()
        recordingItem.path = file
        recordingItem.duration = duration
        (view as PlayView).setData(recordingItem, audioProgressColor)
    }

    override fun reset() {
        audioList.clear()
        // 清空view
        mViewHolder.llContent.removeAllViews()

        // 清空数据和view
        mPhotoAdapter.clearAll()
    }

    override fun getData(): ArrayList<LocalMedia> {
        return mPhotoAdapter.getDataByLocalMedia()
    }

    override fun getImagesAndVideos(): ArrayList<GridMedia> {
        return mPhotoAdapter.getData()
    }

    override fun getImages(): ArrayList<GridMedia> {
        return mPhotoAdapter.getImageData()
    }

    override fun getVideos(): ArrayList<GridMedia> {
        return mPhotoAdapter.getVideoData()
    }

    override fun getAudios(): ArrayList<GridMedia> {
        return audioList
    }

    override fun onAudioClick(view: View) {
        (view as PlayView).mViewHolder.imgPlay.performClick()
    }

    fun getPhotoViewHolder(position: Int): PhotoAdapter.PhotoViewHolder? {
        val holder: RecyclerView.ViewHolder? = mViewHolder.rlGrid.findViewHolderForAdapterPosition(position)
        holder?.let {
            return holder as PhotoAdapter.PhotoViewHolder
        }
        return null
    }

    override fun removePosition(position: Int) {
        mPhotoAdapter.removePosition(position)
    }

    override fun setOperation(isOperation: Boolean) {
        this.isOperation = isOperation
        mPhotoAdapter.photoAdapterEntity.isOperation = isOperation
        // 添加音频后重置所有当前播放中的音频
        for (i in 0 until mViewHolder.llContent.childCount) {
            val item = mViewHolder.llContent.getChildAt(i) as PlayProgressView
            item.isOperation = isOperation
        }
        isShowRemoveRecorder()
    }

    override fun onDestroy() {
        mPhotoAdapter.listener = null
        for (i in 0 until mViewHolder.llContent.childCount) {
            val item = mViewHolder.llContent.getChildAt(i) as PlayProgressView
            item.mViewHolder.playView.onDestroy()
            item.mViewHolder.playView.listener = null
        }
        this.maskProgressLayoutListener = null
        if (mCreatePlayProgressViewTask != null) {
            ThreadUtils.cancel(mCreatePlayProgressViewTask)
        }
    }

    /**
     * @return 最多显示多少个图片/视频/语音
     */
    fun getMaxMediaCount(): Int {
        return mPhotoAdapter.photoAdapterEntity.maxMediaCount
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
            mPhotoAdapter.photoAdapterEntity.maxMediaCount = maxMediaCount!!
        } else {
            mPhotoAdapter.photoAdapterEntity.maxMediaCount =
                maxImageSelectable!! + maxVideoSelectable!! + maxAudioSelectable!!
        }
    }

    /**
     * 递归、有序的创建并且加入音频控件
     */
    private fun createPlayProgressView(audioGridMedia: List<GridMedia>, position: Int) {
        if (position >= audioGridMedia.size) {
            // 加载完毕
            maskProgressLayoutListener?.onAddDataSuccess(audioGridMedia)
            return
        }
        ThreadUtils.executeByIo(getCreatePlayProgressViewTask(audioGridMedia, position))
    }

    /**
     * 创建音频控件的线程
     */
    private fun getCreatePlayProgressViewTask(
        audioGridMedia: List<GridMedia>,
        position: Int
    ): SimpleTask<PlayProgressView> {
        mCreatePlayProgressViewTask = object : SimpleTask<PlayProgressView>() {
            override fun doInBackground(): PlayProgressView {
                val playProgressView: PlayProgressView =
                    newPlayProgressView(audioGridMedia[position])
                // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
                playProgressView.mViewHolder.playView.visibility = View.VISIBLE
                // 隐藏上传进度
                playProgressView.mViewHolder.groupRecorderProgress.visibility = View.GONE
                isShowRemoveRecorder()

                // 设置数据源
                val recordingItem = RecordingItem()
                recordingItem.url = audioGridMedia[position].url
                playProgressView.setData(recordingItem, audioProgressColor)
                return playProgressView
            }

            override fun onSuccess(result: PlayProgressView) {
                // 添加入view
                mViewHolder.llContent.addView(result)
                val newPosition = position + 1
                createPlayProgressView(audioGridMedia, newPosition)
            }
        }
        return mCreatePlayProgressViewTask as SimpleTask<PlayProgressView>
    }


    /**
     * 设置是否显示删除音频按钮
     */
    private fun isShowRemoveRecorder() {
        for (i in 0 until mViewHolder.llContent.childCount) {
            val item = mViewHolder.llContent.getChildAt(i) as PlayProgressView
            item.isShowRemoveRecorder()
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
            mPhotoAdapter.setVideoData(progressMedias)
        } else {
            mPhotoAdapter.addVideoData(progressMedias)
        }
    }

    /**
     * 添加音频数据
     *
     * @param gridMedia 数据
     */
    private fun addAudioData(gridMedia: GridMedia) {
        this.audioList.add(gridMedia)
        val playProgressView = newPlayProgressView(gridMedia)
        // 显示音频的进度条
        this.maskProgressLayoutListener?.onItemAudioStartUploading(gridMedia, playProgressView)
        mViewHolder.llContent.addView(playProgressView)
        // 初始化播放控件
        val recordingItem = RecordingItem()
        recordingItem.path = gridMedia.path
        recordingItem.duration = gridMedia.duration
        playProgressView.setData(recordingItem, audioProgressColor)
        // 添加音频后重置所有当前播放中的音频
        for (i in 0 until mViewHolder.llContent.childCount) {
            val item = mViewHolder.llContent.getChildAt(i) as PlayProgressView
            item.reset()
        }
    }

    /**
     * 创建一个新的playProgressView
     *
     * @param gridMedia 这是携带view的实体控件
     * @return playProgressView
     */
    private fun newPlayProgressView(gridMedia: GridMedia): PlayProgressView {
        val playProgressView = PlayProgressView(context)
        playProgressView.callback = object : PlayProgressView.Callback {
            override fun onRemoveRecorder() {
                if (audioList.size > 0) {
                    // 需要判断，防止是网址状态未提供实体数据的
                    maskProgressLayoutListener?.onItemClose(gridMedia)
                }
                audioList.remove(gridMedia)
            }
        }
        playProgressView.initStyle(audioDeleteColor, audioProgressColor, audioPlayColor)
        playProgressView.setListener(maskProgressLayoutListener)
        return playProgressView
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
        val rlGrid: RecyclerView = rootView.findViewById(R.id.rlGrid)
        val llContent: LinearLayout = rootView.findViewById(R.id.llContent)
    }

}