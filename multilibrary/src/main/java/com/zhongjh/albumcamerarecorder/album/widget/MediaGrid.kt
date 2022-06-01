package com.zhongjh.albumcamerarecorder.album.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec.imageEngine
import com.zhongjh.common.entity.MultiMedia

/**
 * @author zhongjh
 */
class MediaGrid : SquareFrameLayout, View.OnClickListener {

    private lateinit var mThumbnail: ImageView

    /**
     * 选择控件
     */
    private lateinit var mCheckView: CheckView

    /**
     * gif标志图片
     */
    private lateinit var mGifTag: ImageView

    /**
     * 文本的时长（类似指视频的时长）
     */
    private lateinit var mVideoDuration: TextView

    /**
     * 值
     */
    private lateinit var mMedia: MultiMedia

    /**
     * 控件 和一些别的变量
     */
    private lateinit var mPreBindInfo: PreBindInfo

    /**
     * 事件
     */
    private lateinit var mListener: OnMediaGridClickListener

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.media_grid_content_zjh, this, true)
        mThumbnail = findViewById(R.id.media_thumbnail)
        mCheckView = findViewById(R.id.checkView)
        mGifTag = findViewById(R.id.gif)
        mVideoDuration = findViewById(R.id.video_duration)
        mThumbnail.setOnClickListener(this)
        mCheckView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view === mThumbnail) {
            // 图片的点击事件
            mListener.onThumbnailClicked(mThumbnail, mMedia, mPreBindInfo.mViewHolder)
        } else if (view === mCheckView) {
            // 勾选的点击事件
            mListener.onCheckViewClicked(mCheckView, mMedia, mPreBindInfo.mViewHolder)
        }
    }

    fun preBindMedia(info: PreBindInfo) {
        mPreBindInfo = info
    }

    /**
     * 绑定值
     *
     * @param item 值
     */
    fun bindMedia(item: MultiMedia) {
        mMedia = item
        setGifTag()
        initCheckView()
        setImage()
        setVideoDuration()
    }

    /**
     * 根据gif判断是否显示gif标志
     */
    private fun setGifTag() {
        mGifTag.visibility =
            if (mMedia.isGif()) VISIBLE else GONE
    }

    /**
     * 设置是否多选
     */
    private fun initCheckView() {
        mCheckView.setCountable(mPreBindInfo.mCheckViewCountable)
    }

    /**
     * 设置是否启用
     *
     * @param enabled 启用
     */
    fun setCheckEnabled(enabled: Boolean) {
        mCheckView.isEnabled = enabled
    }

    /**
     * 设置当前选择的第几个
     *
     * @param checkedNum 数量
     */
    fun setCheckedNum(checkedNum: Int) {
        mCheckView.setCheckedNum(checkedNum)
    }

    /**
     * 设置当前的单选框为选择
     *
     * @param checked 是否选择
     */
    fun setChecked(checked: Boolean) {
        mCheckView.setChecked(checked)
    }

    /**
     * 设置图片或者gif图片
     */
    private fun setImage() {
        if (mMedia.uri == null) {
            return
        }
        if (mMedia.isGif()) {
            imageEngine.loadGifThumbnail(
                context, mPreBindInfo.mResize,
                mPreBindInfo.mPlaceholder, mThumbnail, mMedia.uri!!
            )
        } else {
            imageEngine.loadThumbnail(
                context, mPreBindInfo.mResize,
                mPreBindInfo.mPlaceholder, mThumbnail, mMedia.uri!!
            )
        }
    }

    /**
     * 设置文本的时长（类似指视频的时长）
     */
    private fun setVideoDuration() {
        if (mMedia.isVideo()) {
            mVideoDuration.visibility = VISIBLE
            mVideoDuration.text =
                DateUtils.formatElapsedTime(mMedia.duration / 1000)
        } else {
            mVideoDuration.visibility = GONE
        }
    }

    fun setOnMediaGridClickListener(listener: OnMediaGridClickListener) {
        mListener = listener
    }

    interface OnMediaGridClickListener {
        /**
         * 点击事件
         *
         * @param thumbnail 图片控件
         * @param item      数据
         * @param holder    控件
         */
        fun onThumbnailClicked(
            thumbnail: ImageView?,
            item: MultiMedia?,
            holder: RecyclerView.ViewHolder?
        )

        /**
         * 选择事件
         *
         * @param checkView 选择控件
         * @param item      数据
         * @param holder    控件
         */
        fun onCheckViewClicked(
            checkView: CheckView?,
            item: MultiMedia?,
            holder: RecyclerView.ViewHolder?
        )
    }

    class PreBindInfo(
        var mResize: Int, var mPlaceholder: Drawable, var mCheckViewCountable: Boolean,
        var mViewHolder: RecyclerView.ViewHolder
    )
}