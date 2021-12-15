package com.zhongjh.progresslibrary.widget

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.common.entity.SaveStrategy
import com.zhongjh.common.enums.MultimediaTypes
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.ThreadUtils
import com.zhongjh.common.utils.ThreadUtils.BaseSimpleBaseTask
import com.zhongjh.progresslibrary.R
import com.zhongjh.progresslibrary.apapter.PhotoAdapter
import com.zhongjh.progresslibrary.api.MaskProgressApi
import com.zhongjh.progresslibrary.engine.ImageEngine
import com.zhongjh.progresslibrary.entity.MultiMediaView
import com.zhongjh.progresslibrary.entity.RecordingItem
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener
import java.util.*

/**
 * 这是返回（图片、视频、录音）等文件后，显示的Layout
 *
 * @author zhongjh
 * @date 2018/10/17
 * https://www.jianshu.com/p/191c41f63dc7
 */
class MaskProgressLayout : FrameLayout, MaskProgressApi {

    private lateinit var mPhotoAdapter: PhotoAdapter

    /**
     * 控件集合
     */
    private val mViewHolder: ViewHolder by lazy {
        ViewHolder(View.inflate(context, R.layout.layout_mask_progress, this))
    }

    /**
     * 图片加载方式
     */
    private var mImageEngine: ImageEngine? = null

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
    val audioList = ArrayList<MultiMediaView>()

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

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
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
        val attrsArray = intArrayOf(R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.colorAccent)
        val typedArray = context.obtainStyledAttributes(attrsArray)
        val colorPrimary = typedArray.getColor(0, defaultColor)

        // 获取自定义属性。
        val maskProgressLayoutStyle = context.obtainStyledAttributes(attrs, R.styleable.MaskProgressLayout)
        // 是否允许操作
        isOperation = maskProgressLayoutStyle.getBoolean(R.styleable.MaskProgressLayout_isOperation, true)
        // 一行多少列
        val columnNumber = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayout_columnNumber, 4)
        // 列与列之间多少间隔px单位
        val columnSpace = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayout_columnSpace, 10)
        // 获取默认图片
        var drawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayout_album_thumbnail_placeholder)
        // 获取添加图片
        val imageAddDrawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayout_imageAddDrawable)
        // 获取显示图片的类
        val imageEngineStr = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayout_imageEngine)
        // provider的authorities,用于提供给外部的file
        val authority = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayout_authority)
        val saveStrategy = SaveStrategy(true, authority, "")
        mMediaStoreCompat = MediaStoreCompat(context, saveStrategy)
        // 获取最多显示多少个方框
        val maxCount = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayout_maxCount, 5)
        val imageDeleteColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_imageDeleteColor, colorPrimary)
        val imageDeleteDrawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayout_imageDeleteDrawable)

        // region 音频
        // 音频，删除按钮的颜色
        audioDeleteColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_audioDeleteColor, colorPrimary)
        // 音频 文件的进度条颜色
        audioProgressColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_audioProgressColor, colorPrimary)
        // 音频 播放按钮的颜色
        audioPlayColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_audioPlayColor, colorPrimary)
        // endregion 音频

        // region 遮罩层相关属性

        val maskingColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_maskingColor, colorPrimary)
        val maskingTextSize = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayout_maskingTextSize, 12)

        val maskingTextColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_maskingTextColor, ContextCompat.getColor(context, R.color.thumbnail_placeholder))
        var maskingTextContent = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayout_maskingTextContent)

        // endregion 遮罩层相关属性

        if (imageEngineStr == null) {
            // 必须定义image_engine属性，指定某个显示图片类
            throw RuntimeException("The image_engine attribute must be defined to specify a class for displaying images")
        } else {
            val imageEngineClass: Class<*> //完整类名
            try {
                imageEngineClass = Class.forName(imageEngineStr)
                mImageEngine = imageEngineClass.newInstance() as ImageEngine
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (mImageEngine == null) {
                // image_engine找不到相关类
                throw RuntimeException("Image_engine could not find the related class")
            }
        }

        if (drawable == null) {
            drawable = ContextCompat.getDrawable(context, R.color.thumbnail_placeholder)
        }
        if (maskingTextContent == null) {
            maskingTextContent = ""
        }
        // 初始化九宫格的控件
        mViewHolder.rlGrid.layoutManager = GridLayoutManager(context, columnNumber)
        mPhotoAdapter = PhotoAdapter(context, (mViewHolder.rlGrid.layoutManager as GridLayoutManager?)!!, this,
                mImageEngine!!, drawable!!, isOperation, maxCount,
                maskingColor, maskingTextSize, maskingTextColor, maskingTextContent,
                imageDeleteColor, imageDeleteDrawable, imageAddDrawable)
        mViewHolder.rlGrid.adapter = mPhotoAdapter

        maskProgressLayoutStyle.recycle()
        typedArray.recycle()
    }

    override fun setAuthority(authority: String) {
        val saveStrategy = SaveStrategy(true, authority, "")
        mMediaStoreCompat.saveStrategy = saveStrategy
    }

    override fun addImagesUriStartUpload(uris: List<Uri>) {
        isAuthority()
        val multiMediaViews = ArrayList<MultiMediaView>()
        for (uri in uris) {
            val multiMediaView = MultiMediaView(MultimediaTypes.PICTURE)
            multiMediaView.uri = uri
            multiMediaView.isUploading = true
            multiMediaViews.add(multiMediaView)
        }
        mPhotoAdapter.addImageData(multiMediaViews)
    }

    override fun addImagesPathStartUpload(imagePaths: List<String>) {
        isAuthority()
        val multiMediaViews = ArrayList<MultiMediaView>()
        for (string in imagePaths) {
            val multiMediaView = MultiMediaView(MultimediaTypes.PICTURE)
            multiMediaView.path = string
            multiMediaView.uri = mMediaStoreCompat.getUri(string)
            multiMediaView.isUploading = true
            multiMediaViews.add(multiMediaView)
        }
        mPhotoAdapter.addImageData(multiMediaViews)
    }

    override fun setImageUrls(imagesUrls: List<String>) {
        val multiMediaViews = ArrayList<MultiMediaView>()
        for (string in imagesUrls) {
            val multiMediaView = MultiMediaView(MultimediaTypes.PICTURE)
            multiMediaView.url = string
            multiMediaViews.add(multiMediaView)
        }
        mPhotoAdapter.setImageData(multiMediaViews)
    }

    override fun addVideoStartUpload(videoUris: List<Uri>) {
        addVideo(videoUris, false, true)
    }

    override fun setVideoCover(multiMediaView: MultiMediaView, videoPath: String) {
        multiMediaView.path = videoPath
    }

    override fun setVideoUrls(videoUrls: List<String>) {
        val multiMediaViews = ArrayList<MultiMediaView>()
        for (i in videoUrls.indices) {
            val multiMediaView = MultiMediaView(MultimediaTypes.VIDEO)
            multiMediaView.isUploading = false
            multiMediaView.url = videoUrls[i]
            multiMediaViews.add(multiMediaView)
        }
        mPhotoAdapter.setVideoData(multiMediaViews)
    }

    override fun addAudioStartUpload(filePath: String, length: Int) {
        isAuthority()
        val multiMediaView = MultiMediaView(MultimediaTypes.AUDIO)
        multiMediaView.path = filePath
        multiMediaView.uri = mMediaStoreCompat.getUri(filePath)
        addAudioData(multiMediaView, filePath, length)
        // 检测添加多媒体上限
        mPhotoAdapter.notifyDataSetChanged()
    }

    override fun setAudioUrls(audioUrls: List<String>) {
        val multiMediaViews: ArrayList<MultiMediaView> = ArrayList()
        for (item in audioUrls) {
            val multiMediaView = MultiMediaView(MultimediaTypes.AUDIO)
            multiMediaView.url = item
            audioList.add(multiMediaView)
            multiMediaViews.add(multiMediaView)
        }
        createPlayProgressView(multiMediaViews, 0)
    }

    override fun setAudioCover(view: View, file: String) {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(file)

        // ms,时长
        val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val multiMediaView = MultiMediaView(MultimediaTypes.AUDIO)
        multiMediaView.path = file

        // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
        view.visibility = View.VISIBLE
        isShowRemoveRecorder()
        val recordingItem = RecordingItem()
        recordingItem.filePath = file
        recordingItem.length = duration!!.toInt()
        (view as PlayView).setData(recordingItem, audioProgressColor)
    }

    override fun reset() {
        audioList.clear()
        // 清空view
        mViewHolder.llContent.removeAllViews()

        // 清空数据和view
        mPhotoAdapter.clearAll()
    }

    override fun getImagesAndVideos(): ArrayList<MultiMediaView> {
        return mPhotoAdapter.getData()
    }

    override fun getImages(): ArrayList<MultiMediaView> {
        return mPhotoAdapter.getImageData()
    }

    override fun getVideos(): ArrayList<MultiMediaView> {
        return mPhotoAdapter.getVideoData()
    }

    override fun getAudios(): ArrayList<MultiMediaView> {
        return audioList
    }

    override fun onAudioClick(view: View) {
        (view as PlayView).mViewHolder.imgPlay.performClick()
    }

    override fun removePosition(position: Int) {
        mPhotoAdapter.removePosition(position)
    }

    override fun setOperation(isOperation: Boolean) {
        this.isOperation = isOperation
        mPhotoAdapter.isOperation = isOperation
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
    }

    /**
     * @return 最多显示多少个图片/视频/语音
     */
    fun getMaxMediaCount(): Int {
        return mPhotoAdapter.maxMediaCount
    }

    /**
     * 设置最多显示多少个图片/视频/语音
     */
    fun setMaxMediaCount(maxMediaCount: Int?, maxImageSelectable: Int?, maxVideoSelectable: Int?, maxAudioSelectable: Int?) {
        // 计算最终呈现的总数，这个总数决定是否还能点击添加
        val isMaxMediaCount = maxMediaCount != null &&
                (maxImageSelectable == null || maxVideoSelectable == null || maxAudioSelectable == null)
        if (isMaxMediaCount) {
            mPhotoAdapter.maxMediaCount = maxMediaCount!!
        } else {
            mPhotoAdapter.maxMediaCount = maxImageSelectable!! + maxVideoSelectable!! + maxAudioSelectable!!
        }
    }

    /**
     * 递归、有序的创建并且加入音频控件
     */
    private fun createPlayProgressView(audioMultiMediaViews: List<MultiMediaView>, position: Int) {
        if (position >= audioMultiMediaViews.size) {
            return
        }
        ThreadUtils.executeByIo(object : BaseSimpleBaseTask<PlayProgressView>() {
            override fun doInBackground(): PlayProgressView {
                val playProgressView: PlayProgressView
                playProgressView = newPlayProgressView(audioMultiMediaViews[position])
                // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
                playProgressView.mViewHolder.playView.visibility = View.VISIBLE
                // 隐藏上传进度
                playProgressView.mViewHolder.groupRecorderProgress.visibility = View.GONE
                isShowRemoveRecorder()

                // 设置数据源
                val recordingItem = RecordingItem()
                recordingItem.url = audioMultiMediaViews[position].url
                playProgressView.setData(recordingItem, audioProgressColor)
                return playProgressView
            }

            override fun onSuccess(result: PlayProgressView) {
                // 添加入view
                mViewHolder.llContent.addView(result)
                val newPosition = position + 1
                createPlayProgressView(audioMultiMediaViews, newPosition)
            }

        })
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
        val multiMediaViews = ArrayList<MultiMediaView>()
        for (i in videoUris.indices) {
            val multiMediaView = MultiMediaView(MultimediaTypes.VIDEO)
            multiMediaView.uri = videoUris[i]
            multiMediaView.isUploading = isUploading
            multiMediaViews.add(multiMediaView)
        }
        if (icClean) {
            mPhotoAdapter.setVideoData(multiMediaViews)
        } else {
            mPhotoAdapter.addVideoData(multiMediaViews)
        }
    }

    /**
     * 添加音频数据
     *
     * @param multiMediaView 数据
     * @param filePath       音频文件地址
     * @param length         音频文件长度
     */
    private fun addAudioData(multiMediaView: MultiMediaView, filePath: String, length: Int) {
        this.audioList.add(multiMediaView)
        if (audioList.size > 0) {
            // 显示音频的进度条
            this.maskProgressLayoutListener?.onItemStartUploading(multiMediaView)
        }
        val playProgressView = newPlayProgressView(multiMediaView)
        mViewHolder.llContent.addView(playProgressView)
        // 初始化播放控件
        val recordingItem = RecordingItem()
        recordingItem.filePath = filePath
        recordingItem.length = length
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
     * @param multiMediaView 这是携带view的实体控件
     * @return playProgressView
     */
    private fun newPlayProgressView(multiMediaView: MultiMediaView): PlayProgressView {
        val playProgressView = PlayProgressView(context)
        playProgressView.callback = object : PlayProgressView.Callback {
            override fun onRemoveRecorder() {
                if (audioList.size > 0) {
                    // 需要判断，防止是网址状态未提供实体数据的
                    maskProgressLayoutListener?.onItemClose(this@MaskProgressLayout, multiMediaView)
                }
                audioList.remove(multiMediaView)
                mPhotoAdapter.notifyDataSetChanged()
            }
        }
        playProgressView.initStyle(audioDeleteColor, audioProgressColor, audioPlayColor)
        multiMediaView.playProgressView = playProgressView
        playProgressView.setListener(maskProgressLayoutListener)
        return playProgressView
    }

    /**
     * 检测属性
     */
    private fun isAuthority() {
        if (mMediaStoreCompat.saveStrategy.authority == null) {
            // 必须定义authority属性，指定provider的authorities,用于提供给外部的file,否则Android7.0以上报错。也可以代码设置setAuthority
            throw java.lang.RuntimeException("You must define the authority attribute, which specifies the provider's authorities, to serve to external files. Otherwise, Android7.0 will report an error.You can also set setAuthority in code")
        }
    }

    class ViewHolder(rootView: View) {
        val rlGrid: RecyclerView = rootView.findViewById(R.id.rlGrid)
        val llContent: LinearLayout = rootView.findViewById(R.id.llContent)
    }

}