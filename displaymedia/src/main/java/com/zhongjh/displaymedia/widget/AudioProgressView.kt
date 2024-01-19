//package com.zhongjh.displaymedia.widget
//
//import android.app.Activity
//import android.content.Context
//import android.util.AttributeSet
//import android.view.View
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.constraintlayout.widget.Group
//import androidx.core.app.ActivityCompat
//import com.daimajia.numberprogressbar.NumberProgressBar
//import com.zhongjh.displaymedia.R
//import com.zhongjh.common.entity.RecordingItem
//import com.zhongjh.displaymedia.listener.DisplayMediaLayoutListener
//
///**
// * 这是包含播放音频的view 和 上传音频进度的view
// *
// * @author zhongjh
// * @date 2021/7/8
// */
//class AudioProgressView : ConstraintLayout {
//
//    /**
//     * 控件集合
//     */
//    lateinit var mViewHolder: ViewHolder
//
//    /**
//     * 是否允许操作(一般只用于展览作用)
//     */
//    var isOperation = true
//
//    var callback: Callback? = null
//
//    interface Callback {
//        /**
//         * 音频删除事件
//         */
//        fun onRemoveRecorder()
//    }
//
//    constructor(context: Context) : this(context, null)
//    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
//        initView()
//    }
//
//    /**
//     * 初始化view
//     */
//    private fun initView() {
//        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
//        setWillNotDraw(false)
//        addInit()
//        initListener()
//    }
//
//    /**
//     * 初始化样式
//     */
//    fun initStyle(audioDeleteColor: Int, audioProgressColor: Int, audioPlayColor: Int) {
//        // 设置上传音频等属性
//        mViewHolder.imgRemoveRecorder.setColorFilter(audioDeleteColor)
//        isShowRemoveRecorder()
//        mViewHolder.numberProgressBar.setProgressTextColor(audioProgressColor)
//        mViewHolder.numberProgressBar.reachedBarColor = audioProgressColor
//        mViewHolder.tvRecorderTip.setTextColor(audioProgressColor)
//
//        // 设置播放控件里面的播放按钮的颜色
//        mViewHolder.audioView.mViewHolder.imgPlay.setColorFilter(audioPlayColor)
//        mViewHolder.audioView.mViewHolder.tvCurrentProgress.setTextColor(audioProgressColor)
//        mViewHolder.audioView.mViewHolder.tvTotalProgress.setTextColor(audioProgressColor)
//    }
//
//    /**
//     * 重置
//     */
//    fun reset() {
//        mViewHolder.audioView.reset()
//    }
//
//    /**
//     * 赋值事件
//     *
//     * @param listener 事件
//     */
//    fun setListener(listener: DisplayMediaLayoutListener?) {
//        mViewHolder.audioView.listener = listener
//    }
//
//    /**
//     * 设置是否显示删除音频按钮
//     */
//    fun isShowRemoveRecorder() {
//        if (isOperation) {
//            // 如果是可操作的，就判断是否有音频数据
//            if (mViewHolder.audioView.visibility == View.VISIBLE
//                    || mViewHolder.groupRecorderProgress.visibility == View.VISIBLE) {
//                mViewHolder.imgRemoveRecorder.visibility = View.VISIBLE
//            } else {
//                mViewHolder.imgRemoveRecorder.visibility = View.GONE
//            }
//        } else {
//            mViewHolder.imgRemoveRecorder.visibility = View.GONE
//        }
//    }
//
//    /**
//     * 初始化相关数据
//     *
//     * @param recordingItem      音频数据源
//     * @param audioProgressColor 进度条颜色
//     */
//    fun setData(recordingItem: RecordingItem, audioProgressColor: Int) {
//        mViewHolder.audioView.setData(recordingItem, audioProgressColor)
//    }
//
//    /**
//     * 音频上传完成后
//     */
//    fun audioUploadCompleted() {
//        // 显示完成后的音频
//        mViewHolder.groupRecorderProgress.visibility = View.GONE
//        mViewHolder.audioView.visibility = View.VISIBLE
//        isShowRemoveRecorder()
//    }
//
//    /**
//     * 添加后的初始化
//     */
//    private fun addInit() {
//        val view = inflate(context, R.layout.item_audio_progress_zjh, this)
//        mViewHolder = ViewHolder(view)
//
//        // 显示上传中的音频
//        mViewHolder.groupRecorderProgress.visibility = View.VISIBLE
//        mViewHolder.audioView.visibility = View.GONE
//        isShowRemoveRecorder()
//    }
//
//    /**
//     * 初始化所有事件
//     */
//    private fun initListener() {
//        // 音频删除事件
//        mViewHolder.imgRemoveRecorder.setOnClickListener {
//            callback?.onRemoveRecorder()
//            // 隐藏音频相关控件
//            mViewHolder.groupRecorderProgress.visibility = View.GONE
//            mViewHolder.audioView.visibility = View.GONE
//            mViewHolder.imgRemoveRecorder.visibility = View.GONE
//            isShowRemoveRecorder()
//        }
//    }
//
//    class ViewHolder(rootView: View) {
//        val numberProgressBar: NumberProgressBar = rootView.findViewById(R.id.numberProgressBar)
//        val imgRemoveRecorder: ImageView = rootView.findViewById(R.id.imgRemoveRecorder)
//        val groupRecorderProgress: Group = rootView.findViewById(R.id.groupRecorderProgress)
//        val audioView: AudioView = rootView.findViewById(R.id.playView)
//        val tvRecorderTip: TextView = rootView.findViewById(R.id.tvRecorderTip)
//    }
//
//}