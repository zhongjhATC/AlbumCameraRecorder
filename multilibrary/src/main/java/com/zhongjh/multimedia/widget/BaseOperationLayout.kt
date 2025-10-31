package com.zhongjh.multimedia.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.zhongjh.circularprogressview.CircularProgress
import com.zhongjh.circularprogressview.CircularProgressListener
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.camera.listener.ClickOrLongListener
import com.zhongjh.multimedia.widget.clickorlongbutton.ClickOrLongButton
import java.lang.ref.WeakReference

/**
 * 集成开始功能按钮、确认按钮、取消按钮的布局
 * [点击或者长按的按钮 ][ClickOrLongButton]
 * [操作按钮(取消和确认) ][com.zhongjh.circularprogressview.CircularProgress]
 *
 * @author zhongjh
 * @date 2018/8/7
 */
abstract class BaseOperationLayout : FrameLayout {

    /**
     * @param context 上下文对象
     * @param attrs XML属性集合
     * @param defStyleAttr 默认样式属性
     */
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)

    // region 回调事件监听

    /**
     * 点击或长按监听
     */
    private var clickOrLongListener: ClickOrLongListener? = null

    /**
     * 点击或长按监听结束后的 确认取消事件监控
     */
    private var operateListener: OperateListener? = null

    /**
     * 操作按钮的Listener
     * @noinspection unused
     */
    interface OperateListener {
        /**
         * 确认前的事件，一般用于请求权限
         *
         *
         * return false则是做其他操作
         * return true则是无其他操作继续下一步
         */
        fun beforeConfirm(): Boolean

        /**
         * 取消
         */
        fun cancel()

        /**
         * 开始进度操作，目前只用于分段录制
         */
        fun startProgress()

        /**
         * 取消进度操作，目前只用于分段录制
         */
        fun stopProgress()

        /**
         * 进度完成
         */
        fun doneProgress()
    }

    fun setPhotoVideoListener(clickOrLongListener: ClickOrLongListener) {
        this.clickOrLongListener = clickOrLongListener
    }

    fun setOperateListener(mOperateListener: OperateListener) {
        this.operateListener = mOperateListener
    }

    // endregion
    /**
     * 控件集合
     */
    protected val viewHolder by lazy {
        newViewHolder()
    }

    /**
     * 是否第一次
     */
    private var isFirst = true

    /**
     * 按钮左右分开移动动画
     */
    private var animatorConfirm: ObjectAnimator? = null
    private var animatorCancel: ObjectAnimator? = null
    private var animatorStartTxtTip: ObjectAnimator? = null
    private var animatorSetTxtTip: ObjectAnimator? = null

    /**
     * 创建
     *
     * @return ViewHolder
     */
    protected abstract fun newViewHolder(): BaseViewHolder

    init {
        initView()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 获取宽的模式
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        // 获取高的模式
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        // 获取宽的尺寸
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        // 获取高的尺寸
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        heightSize /= 3
        animatorConfirm = ObjectAnimator.ofFloat(viewHolder.btnConfirm, "translationX", -widthSize / 4f, 0f)
        animatorCancel = ObjectAnimator.ofFloat(viewHolder.btnCancel, "translationX", widthSize / 4f, 0f)

        setMeasuredDimension(widthSize, heightSize)
        // 传递新创建的宽高给子控件
        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, widthMode), MeasureSpec.makeMeasureSpec(heightSize, heightMode))
    }

    /**
     * 初始化view
     */
    private fun initView() {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false)

        // 默认隐藏
        viewHolder.btnCancel.visibility = GONE
        viewHolder.btnConfirm.visibility = GONE

        // 定制样式 .确认按钮,修改主色调
        viewHolder.btnConfirm.setPrimaryColor(R.color.operation_background)
        // 修改成铺满样式
        viewHolder.btnConfirm.setFullStyle(true)
        // 修改图片
        viewHolder.btnConfirm.setFunctionImage(
            R.drawable.ic_baseline_done, R.drawable.avd_done_to_stop, R.drawable.avd_stop_to_done
        )
        // 修改进度颜色
        viewHolder.btnConfirm.setFullProgressColor(R.color.click_button_inner_circle_no_operation_interval)

        // 定制样式 .取消按钮 修改主色调
        viewHolder.btnCancel.setPrimaryColor(R.color.operation_background)
        // 修改成铺满样式
        viewHolder.btnCancel.setFullStyle(true)
        // 修改图片
        viewHolder.btnCancel.setFunctionImage(
            R.drawable.ic_baseline_keyboard_arrow_left_24, R.drawable.avd_done_to_stop, R.drawable.avd_stop_to_done
        )
        // 取消进度模式
        viewHolder.btnCancel.setProgressMode(false)

        initListener()
    }

    /**
     * 初始化事件
     */
    protected fun initListener() {
        btnClickOrLongListener()
        btnCancelListener()
        btnConfirmListener()
    }

    /**
     * btnClickOrLong事件
     */
    private fun btnClickOrLongListener() {
        viewHolder.btnClickOrLong.setRecordingListener(ClickOrLongListenerImpl(WeakReference(this)))
    }

    /**
     * 返回事件
     */
    private fun btnCancelListener() {
        viewHolder.btnCancel.setCircularProgressListener(CircularProgressListenerImpl(WeakReference(this)))
    }

    /**
     * 提交事件
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun btnConfirmListener() {
        // 用于点击前请求权限
        viewHolder.btnConfirm.setOnTouchListener { _: View?, motionEvent: MotionEvent ->
            operateListener?.let { operateListener ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    return@setOnTouchListener !operateListener.beforeConfirm()
                }
            }
            false
        }
        viewHolder.btnConfirm.setCircularProgressListener(object : CircularProgressListener {
            override fun onStart() {
                operateListener?.let { operateListener ->
                    operateListener.startProgress()
                    startTipAlphaAnimation()
                }
            }

            override fun onDone() {
                operateListener?.doneProgress()
            }

            override fun onStop() {
                operateListener?.stopProgress()
            }

            override fun onClickByGeneralMode() {
            }

            override fun onClickByProgressMode() {
            }
        })
    }

    /**
     * 隐藏中间的核心操作按钮
     */
    fun hideBtnClickOrLong() {
        viewHolder.btnClickOrLong.visibility = INVISIBLE
    }

    /**
     * 点击长按结果后的动画
     * 显示左右两边的按钮
     *
     * @param showCancel 是否显示取消按钮
     */
    open fun startShowLeftRightButtonsAnimator(showCancel: Boolean) {
        // 显示提交和取消按钮
        viewHolder.btnConfirm.visibility = VISIBLE
        if (showCancel) {
            viewHolder.btnCancel.visibility = VISIBLE
        }
        // 动画未结束前不能让它们点击
        viewHolder.btnConfirm.isClickable = false
        viewHolder.btnCancel.isClickable = false

        // 显示动画
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animatorCancel, animatorConfirm)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                // 动画结束使得按钮可点击
                viewHolder.btnConfirm.isClickable = true
                viewHolder.btnCancel.isClickable = true
            }
        })
        animatorSet.setDuration(300)
        animatorSet.start()
    }

    /**
     * 多图片拍照后显示的右侧按钮
     */
    fun startOperationBtnAnimatorMulti() {
        // 如果本身隐藏的，就显示出来
        if (viewHolder.btnConfirm.visibility == GONE) {
            // 显示提交按钮
            viewHolder.btnConfirm.visibility = VISIBLE
            // 动画未结束前不能让它们点击
            viewHolder.btnConfirm.isClickable = false

            // 显示动画
            val set = AnimatorSet()
            set.playTogether(animatorConfirm)
            set.addListener(AnimatorListenerAdapterImpl(WeakReference(this)))
            set.setDuration(200)
            set.start()
        }
    }

    /**
     * 销毁，防止内存泄漏
     */
    fun onDestroy() {
        animatorConfirm?.cancel()
        animatorCancel?.cancel()
        animatorStartTxtTip?.cancel()
        animatorSetTxtTip?.cancel()
        viewHolder.btnClickOrLong.onDestroy()
        viewHolder.btnConfirm.onDestroy()
        viewHolder.btnCancel.onDestroy()
        clickOrLongListener = null
        operateListener = null
    }

    private class ClickOrLongListenerImpl(private val outer: WeakReference<BaseOperationLayout>) : ClickOrLongListener {
        override fun actionDown() {
            outer.get()?.clickOrLongListener?.actionDown()
        }

        override fun onClick() {
            outer.get()?.clickOrLongListener?.onClick()
        }

        override fun onLongClick() {
            outer.get()?.clickOrLongListener?.onLongClick()
            outer.get()?.startTipAlphaAnimation()
        }

        override fun onLongClickEnd(time: Long) {
            outer.get()?.clickOrLongListener?.onLongClickEnd(time)
            outer.get()?.startTipAlphaAnimation()
        }

        override fun onLongClickFinish() {
            outer.get()?.clickOrLongListener?.onLongClickFinish()
        }

        override fun onLongClickError() {
            outer.get()?.clickOrLongListener?.onLongClickError()
        }

        override fun onBanClickTips() {
            outer.get()?.clickOrLongListener?.onBanClickTips()
        }

        override fun onClickStopTips() {
            outer.get()?.clickOrLongListener?.onClickStopTips()
        }
    }

    private class CircularProgressListenerImpl(private val outer: WeakReference<BaseOperationLayout>) : CircularProgressListener {
        override fun onStart() {
        }

        override fun onDone() {
        }

        override fun onStop() {
        }

        override fun onClickByGeneralMode() {
            outer.get()?.operateListener?.cancel()
            outer.get()?.startTipAlphaAnimation()
        }

        override fun onClickByProgressMode() {
        }
    }

    private class AnimatorListenerAdapterImpl(private val outer: WeakReference<BaseOperationLayout>) : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            outer.get()?.viewHolder?.btnConfirm?.isClickable = true
            outer.get()?.viewHolder?.btnCancel?.isClickable = true
            animation.removeAllListeners()
        }
    }

    // region 对外提供的api

    /**
     * 设置提示文本
     *
     * @param tip 提示文本
     */
    fun setTip(tip: String?) {
        viewHolder.tvTip.text = tip
    }

    /**
     * 提示文本框 - 浮现渐现动画
     */
    fun startTipAlphaAnimation() {
        if (isFirst) {
            animatorStartTxtTip = ObjectAnimator.ofFloat(viewHolder.tvTip, "alpha", 1f, 0f)
            animatorStartTxtTip?.setDuration(500)
            animatorStartTxtTip?.start()
            isFirst = false
        }
    }

    /**
     * 提示文本框 - 浮现渐现动画，显示新的文字
     *
     * @param tip 提示文字
     */
    fun setTipAlphaAnimation(tip: String?) {
        viewHolder.tvTip.text = tip
        animatorSetTxtTip?.cancel()
        animatorSetTxtTip = ObjectAnimator.ofFloat(viewHolder.tvTip, "alpha", 0f, 1f, 1f, 0f)
        animatorSetTxtTip?.setDuration(2500)
        animatorSetTxtTip?.start()
    }

    /**
     * 设置按钮 最长长按时间
     *
     * @param duration 时间秒
     */
    fun setDuration(duration: Int) {
        viewHolder.btnClickOrLong.setDuration(duration)
    }

    /**
     * 长按准备时间
     * 长按达到duration时间后，才开启录制
     *
     * @param duration 时间毫秒
     */
    fun setReadinessDuration(duration: Int) {
        viewHolder.btnClickOrLong.setReadinessDuration(duration)
    }

    /**
     * 重置本身全部
     */
    open fun reset() {
        viewHolder.btnClickOrLong.resetState()
        viewHolder.btnClickOrLong.reset()
        // 隐藏第二层的view
        viewHolder.btnCancel.visibility = GONE
        viewHolder.btnCancel.reset()
        viewHolder.btnConfirm.visibility = GONE
        viewHolder.btnConfirm.reset()
        // 显示第一层的view
        viewHolder.btnClickOrLong.visibility = VISIBLE
    }

    /**
     * 设置按钮支持的功能：
     *
     * @param buttonStateBoth [ClickOrLongButton.BUTTON_STATE_ONLY_CLICK]
     */
    fun setButtonFeatures(buttonStateBoth: Int) {
        viewHolder.btnClickOrLong.setButtonFeatures(buttonStateBoth)
    }

    /**
     * 设置是否可点击
     */
    override fun setEnabled(enabled: Boolean) {
        viewHolder.btnClickOrLong.isTouchable = enabled
        viewHolder.btnConfirm.isEnabled = enabled
        viewHolder.btnCancel.isEnabled = enabled
    }

    /**
     * 设置提交按钮是否可点击
     *
     * @param enabled 是否可点击
     */
    fun setConfirmEnable(enabled: Boolean) {
        viewHolder.btnConfirm.isEnabled = enabled
    }

    /**
     * 设置中间按钮是否可点击
     *
     * @param enabled 是否可点击
     */
    fun setClickOrLongEnable(enabled: Boolean) {
        viewHolder.btnClickOrLong.isTouchable = enabled
    }

    /**
     * 赋值当前视频录制时间
     */
    fun setData(videoTime: Long) {
        viewHolder.btnClickOrLong.setCurrentTime(videoTime)
    }

    /**
     * 刷新点击长按按钮
     */
    fun invalidateClickOrLongButton() {
        viewHolder.btnClickOrLong.invalidate()
    }

    var progressMode: Boolean
        /**
         * @return 获取当前是否进度模式
         */
        get() = viewHolder.btnConfirm.mIsProgress
        /**
         * 是否启用进度模式
         */
        set(isProgress) {
            viewHolder.btnConfirm.setProgressMode(isProgress)
        }

    /**
     * 重置btnConfirm
     */
    fun resetConfirm() {
        viewHolder.btnConfirm.reset()
    }

    /**
     * 提交按钮
     */
    fun btnConfirmPerformClick() {
        viewHolder.btnConfirm.performClick()
    }

    // 在 BaseOperationLayout 类中添加以下方法
    fun getBtnClickOrLong(): ClickOrLongButton {
        return viewHolder.btnClickOrLong
    }

    open class BaseViewHolder(rootView: View) {
        val btnCancel: CircularProgress = rootView.findViewById(R.id.btnCancel)
        val btnConfirm: CircularProgress = rootView.findViewById(R.id.btnConfirm)
        val btnClickOrLong: ClickOrLongButton = rootView.findViewById(R.id.btnClickOrLong)
        val tvTip: TextView = rootView.findViewById(R.id.tvTip)
    }

    // endregion
}
