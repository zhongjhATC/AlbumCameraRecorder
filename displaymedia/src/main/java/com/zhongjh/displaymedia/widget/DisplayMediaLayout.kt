package com.zhongjh.displaymedia.widget

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.entity.SaveStrategy
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.ThreadUtils.runOnUiThread
import com.zhongjh.displaymedia.R
import com.zhongjh.displaymedia.apapter.AudioAdapter
import com.zhongjh.displaymedia.apapter.ImagesAndVideoAdapter
import com.zhongjh.displaymedia.api.DisplayMediaApi
import com.zhongjh.displaymedia.engine.ImageEngine
import com.zhongjh.displaymedia.entity.DisplayMedia
import com.zhongjh.displaymedia.entity.Masking
import com.zhongjh.displaymedia.entity.PhotoAdapterEntity
import com.zhongjh.displaymedia.entity.VideoMedia
import com.zhongjh.displaymedia.listener.DisplayMediaLayoutListener
import java.io.File
import java.util.*


/**
 * 这是返回（图片、视频、录音）等文件后，显示的Layout
 *
 * @author zhongjh
 * @date 2018/10/17
 * https://www.jianshu.com/p/191c41f63dc7
 */
class DisplayMediaLayout : FrameLayout, DisplayMediaApi {

    companion object {
        const val COLUMN_NUMBER = 4
        const val MASKING_TEXT_SIZE = 12
        const val MAX_MEDIA_COUNT = 5
    }

    private lateinit var mImagesAndVideoAdapter: ImagesAndVideoAdapter
    private lateinit var mAudioAdapter: AudioAdapter

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
    var displayMediaLayoutListener: DisplayMediaLayoutListener? = null
        set(value) {
            field = value
            mImagesAndVideoAdapter.listener = value
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

        val view = inflate(context, R.layout.layout_display_media_zjh, this)
        mViewHolder = ViewHolder(view)

        // 初始化音频的控件
        mViewHolder.rlAudio.layoutManager = LinearLayoutManager(context)
        mAudioAdapter = AudioAdapter(context, audioDeleteColor, audioProgressColor, audioPlayColor)
        mAudioAdapter.callback = object : AudioAdapter.Callback {

            @SuppressLint("SetTextI18n")
            override fun onPlayProgress(position: Int, mediaPlayerCurrentPosition: Int) {
                val playViewHolder = mViewHolder.rlAudio.findViewHolderForAdapterPosition(position)
                playViewHolder?.let {
                    it as AudioAdapter.AudioHolder
                    // 设置当前播放进度
                    it.seekBar.progress = mediaPlayerCurrentPosition
                    it.tvCurrentProgress.text = mAudioAdapter.generateTime(mediaPlayerCurrentPosition.toLong()) + File.separator
                }
            }

        }
        mViewHolder.rlAudio.adapter = mAudioAdapter

        // 初始化九宫格的控件
        mViewHolder.rlGrid.layoutManager = GridLayoutManager(context, columnNumber)
        mImagesAndVideoAdapter = ImagesAndVideoAdapter(
            context,
            (mViewHolder.rlGrid.layoutManager as GridLayoutManager?)!!,
            mAudioAdapter.list,
            photoAdapterEntity
        )
        mViewHolder.rlGrid.adapter = mImagesAndVideoAdapter

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

    override fun setPercentage(multiMedia: DisplayMedia, percentage: Int) {
        multiMedia.progress = percentage
        if (multiMedia.isAudio()) {
            // 找出音频的 viewHolder 赋值
            runOnUiThread {
                val position = getAudios().indexOf(multiMedia)
                val videoHolder = mViewHolder.rlAudio.findViewHolderForAdapterPosition(position)
                videoHolder?.let {
                    it as AudioAdapter.AudioHolder
                    it.showProgress(percentage)
                }
            }
        } else {
            // 找出图片视频的 viewHolder 赋值
            runOnUiThread {
                val position = getImagesAndVideos().indexOf(multiMedia)
                val photoViewHolder = mViewHolder.rlGrid.findViewHolderForAdapterPosition(position)
                photoViewHolder?.let {
                    it as ImagesAndVideoAdapter.PhotoViewHolder
                    it.setProgress(percentage)
                }
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
        val progressMediaImages = ArrayList<DisplayMedia>()
        // 新添加视频的
        val progressMediaVideos = ArrayList<DisplayMedia>()
        // 新添加音频的
        val mediaAudios = ArrayList<DisplayMedia>()
        for (localMedia in localMediaList) {
            val progressMedia = DisplayMedia(localMedia)
            progressMedia.isUploading = true
            // 处理音频
            if (progressMedia.isAudio()) {
                progressMedia.videoMedia = VideoMedia()
                mediaAudios.add(progressMedia)
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
        mImagesAndVideoAdapter.addImageData(progressMediaImages)
        mImagesAndVideoAdapter.addVideoData(progressMediaVideos)
        mAudioAdapter.addAudioData(mediaAudios)
        // 检测添加多媒体上限
        mImagesAndVideoAdapter.notifyItemRangeChanged(0, mImagesAndVideoAdapter.getData().size - 1)
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

    override fun setImageUrls(imagesUrls: List<String>) {
        // 转换数据源
        val progressMedias = ArrayList<DisplayMedia>()
        for (string in imagesUrls) {
            val progressMedia = DisplayMedia(MimeType.JPEG.mimeTypeName)
            progressMedia.url = string
            progressMedias.add(progressMedia)
        }
        // 增加新的图片数据
        mImagesAndVideoAdapter.setImageData(progressMedias)
        displayMediaLayoutListener?.onAddDataSuccess(progressMedias)
    }

    override fun setImagePaths(imagePaths: List<String>) {
    }

    override fun addVideoStartUpload(videoUris: List<Uri>) {
        addVideo(videoUris, icClean = false, isUploading = true)
    }

    override fun setVideoCover(displayMedia: DisplayMedia, videoPath: String) {
        displayMedia.path = videoPath
    }

    override fun setVideoUrls(videoUrls: List<String>) {
        val displayMedias = ArrayList<DisplayMedia>()
        for (i in videoUrls.indices) {
            val progressMedia = DisplayMedia(MimeType.MP4.mimeTypeName)
            progressMedia.isUploading = false
            progressMedia.url = videoUrls[i]
            displayMedias.add(progressMedia)
        }
        mImagesAndVideoAdapter.setVideoData(displayMedias)
        displayMediaLayoutListener?.onAddDataSuccess(displayMedias)
    }

    override fun setVideoPaths(videoPaths: List<String>) {
    }

    override fun addAudioStartUpload(filePath: String, length: Long) {
    }

    override fun setAudioUrls(audioUrls: List<String>) {
        val displayMedias: ArrayList<DisplayMedia> = ArrayList()
        for (item in audioUrls) {
            val progressMedia = DisplayMedia(MimeType.AAC.mimeTypeName)
            progressMedia.url = item
            displayMedias.add(progressMedia)
        }
        mAudioAdapter.setAudioData(displayMedias)
        displayMediaLayoutListener?.onAddDataSuccess(displayMedias)
    }

    override fun setAudioCover(displayMedia: DisplayMedia, videoPath: String) {
        TODO("Not yet implemented")
    }

    override fun setAudioCover(view: View, file: String) {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(file)

        // ms,时长
        val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            ?: -1
        val progressMedia = DisplayMedia(MimeType.AAC.mimeTypeName)
        progressMedia.absolutePath = file
        progressMedia.path = mMediaStoreCompat.getUri(file).toString()
        progressMedia.duration = duration
        progressMedia.mimeType =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE).toString()

        // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
        view.visibility = View.VISIBLE
        isShowRemoveRecorder()
//        val recordingItem = RecordingItem()
//        recordingItem.path = file
//        recordingItem.duration = duration
//        (view as AudioView).setData(recordingItem, audioProgressColor)
    }

    override fun reset() {
        // 清空数据和view
        mAudioAdapter.clearAll()
        mImagesAndVideoAdapter.clearAll()
    }

    override fun getAllData(): ArrayList<DisplayMedia> {
        val list = ArrayList<DisplayMedia>()
        list.addAll(mImagesAndVideoAdapter.getData())
        list.addAll(mAudioAdapter.list)
        return list
    }

    override fun getImagesAndVideos(): ArrayList<DisplayMedia> {
        return mImagesAndVideoAdapter.getData()
    }

    override fun getImages(): ArrayList<DisplayMedia> {
        return mImagesAndVideoAdapter.getImageData()
    }

    override fun getVideos(): ArrayList<DisplayMedia> {
        return mImagesAndVideoAdapter.getVideoData()
    }

    override fun getAudios(): ArrayList<DisplayMedia> {
        return mAudioAdapter.list
    }

    override fun onAudioClick(view: View) {
//        (view as AudioView).mViewHolder.imgPlay.performClick()
    }

    fun getPhotoViewHolder(position: Int): ImagesAndVideoAdapter.PhotoViewHolder? {
        val holder: RecyclerView.ViewHolder? = mViewHolder.rlGrid.findViewHolderForAdapterPosition(position)
        holder?.let {
            return holder as ImagesAndVideoAdapter.PhotoViewHolder
        }
        return null
    }

    override fun removePosition(position: Int) {
        mImagesAndVideoAdapter.removePosition(position)
    }

    override fun refreshPosition(position: Int) {
        TODO("Not yet implemented")
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
        mImagesAndVideoAdapter.listener = null
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
        return mImagesAndVideoAdapter.photoAdapterEntity.maxMediaCount
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
            mImagesAndVideoAdapter.photoAdapterEntity.maxMediaCount = maxMediaCount!!
        } else {
            mImagesAndVideoAdapter.photoAdapterEntity.maxMediaCount =
                maxImageSelectable!! + maxVideoSelectable!! + maxAudioSelectable!!
        }
    }

    /**
     * 设置是否显示删除音频按钮
     */
    private fun isShowRemoveRecorder() {
//        for (i in 0 until mViewHolder.llContent.childCount) {
//            val item = mViewHolder.llContent.getChildAt(i) as PlayProgressView
//            item.isShowRemoveRecorder()
//        }
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
        val progressMedias = ArrayList<DisplayMedia>()
        for (i in videoUris.indices) {
            val progressMedia = DisplayMedia(MimeType.MP4.mimeTypeName)
            progressMedia.path = videoUris[i].toString()
            progressMedia.isUploading = isUploading
            progressMedias.add(progressMedia)
        }
        if (icClean) {
            mImagesAndVideoAdapter.setVideoData(progressMedias)
        } else {
            mImagesAndVideoAdapter.addVideoData(progressMedias)
        }
    }

    /**
     * 添加音频数据
     *
     * @param displayMedia 数据
     */
    private fun addAudioData(displayMedia: DisplayMedia) {
//        this.audioList.add(displayMedia)
//        val playProgressView = newPlayProgressView(displayMedia)
//        // 显示音频的进度条
//        this.displayMediaLayoutListener?.onItemAudioStartUploading(displayMedia, playProgressView)
//        mViewHolder.llContent.addView(playProgressView)
//        // 初始化播放控件
//        val recordingItem = RecordingItem()
//        recordingItem.path = displayMedia.path
//        recordingItem.duration = displayMedia.duration
//        playProgressView.setData(recordingItem, audioProgressColor)
//        // 添加音频后重置所有当前播放中的音频
//        for (i in 0 until mViewHolder.llContent.childCount) {
//            val item = mViewHolder.llContent.getChildAt(i) as PlayProgressView
//            item.reset()
//        }
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
        val rlAudio: RecyclerView = rootView.findViewById(R.id.rlAudio)
    }

}