package com.zhongjh.multimedia.widget.clickorlongbutton

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.RectF
import android.os.Build
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.zhongjh.common.utils.DisplayMetricsUtils.dip2px
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.camera.listener.ClickOrLongListener
import java.lang.ref.WeakReference

/**
 * 点击或者长按的按钮
 *
 * @author zhongjh
 */
class ClickOrLongButton : View {
    /**
     * 倍数,控制该控件的大小
     */
    private var multiple = 1f

    /**
     * 录制时间
     */
    private var timeLimitInMils = 10000.0f

    /**
     * 当前录制位置集，计算时间后的百分比
     */
    private val mCurrentLocation = ArrayList<Float>()

    /**
     * 当前录制的节点，以360度为单位
     */
    private var mCurrentSumNumberDegrees = 0f

    /**
     * 当前录制的总共时间点
     */
    private var mCurrentSumTime = 0L

    /**
     * 动画的预备时间
     */
    private var mMinDurationAnimation = 1500

    /**
     * 当前状态的动画预备时间
     */
    private var mMinDurationAnimationCurrent = mMinDurationAnimation

    /**
     * 记录当前录制的总共多长的时间秒
     */
    private var mRecordedTime: Long = 0
    private var mBoundingBoxSize = 0
    private var mOutCircleWidth = 0
    private var mOuterCircleWidthInc = 0
    private var mInnerCircleRadius = 0f
    var isTouchable: Boolean = false
    private var isRecordable: Boolean = false

    private var centerCirclePaint = Paint()
    private var outBlackCirclePaint = Paint()
    private var outMostBlackCirclePaint = Paint()
    private var innerCircleRadiusToDraw = 0f

    /**
     * 外圈的画布
     */
    private lateinit var outMostCircleRect: RectF
    private var outBlackCircleRadius = 0f
    private var outMostBlackCircleRadius = 0f
    private var colorRoundBorder = 0
    private var colorRecord = 0
    private var colorWhiteP60 = 0
    private var startAngle270 = 0f
    private var percentInDegree = 0f
    private var centerX = 0f
    private var centerY = 0f

    /**
     * 按下去显示的进度外圈
     */
    private var processBarPaint = Paint()

    /**
     * 静止状态时的外圈
     */
    private var outMostWhiteCirclePaint = Paint()

    /**
     * 静止状态时的进度外圈
     */
    private var outProcessCirclePaint = Paint()
    private var translucentPaint = Paint()
    private var translucentCircleRadius = 0
    private var outMostCircleRadius = 0f
    private var innerCircleRadiusWhenRecord = 0f
    private var btnPressTime: Long = 0
    private var outBlackCircleRadiusInc = 0

    /**
     * 为了确保整个按钮的逻辑从按下-放开手都是流畅的，会用 按下+1，放开手+1，最后等于2的方式执行
     * 如果中间中断或者重置，那就直接减1，就说明中断流程
     */
    private var step = 0

    /**
     * 当前状态
     */
    private var recordState = 0

    /**
     * 按钮可执行的功能状态（点击,长按,两者,按钮点击即是长按模式）
     */
    private var mButtonState = 0

    /**
     * 是否允许动画,目前只针对视频录制,等视频通知我们开始，我们再开始动画
     */
    var isStartTicking: Boolean = true

    private val updateUITask = UpdateUITask(WeakReference(this))
    private var touchTimeHandler = TouchTimeHandler(Looper.getMainLooper(), updateUITask)

    /**
     * 判断是否超过预备时间就开始具体动画
     *
     * @param timeLapse 当前时间 - 点击的那一刻时间 = 点击后度过了多久
     * @param percent   当前百分比
     */
    private fun startAnimation(timeLapse: Long, percent: Float) {
        // isStartTicking是由CameraManage的视频录制监控来决定是否继续动画
        if (timeLapse >= mMinDurationAnimationCurrent && isStartTicking) {
            synchronized(this@ClickOrLongButton) {
                if (recordState == RECORD_NOT_STARTED) {
                    setRecordState(RECORD_STARTED)
                    mClickOrLongListener?.let { clickOrLongListener ->
                        clickOrLongListener.onLongClick()
                        // 如果禁止点击，那么就轮到长按触发actionDown
                        if (!mActionDown && mButtonState == BUTTON_STATE_ONLY_LONG_CLICK) {
                            // 如果禁止点击也不能触发该事件
                            clickOrLongListener.actionDown()
                            mActionDown = true
                        }
                    }

                }
            }
            if (!isRecordable) {
                return
            }
            centerCirclePaint.color = colorRecord
            outMostWhiteCirclePaint.color = colorRoundBorder
            percentInDegree = (360.0f * percent)
            if ((timeLapse - mMinDurationAnimationCurrent) >= mMinDurationAnimationCurrent) {
                setCurrentSumNumberDegrees(percentInDegree)
            }
            if (percent <= FULL_PROGRESS) {
                if (percent <= PROGRESS_LIM_TO_FINISH_STARTING_ANIM) {
                    val calPercent = percent / PROGRESS_LIM_TO_FINISH_STARTING_ANIM
                    val outIncDis = outBlackCircleRadiusInc * calPercent
                    val curOutCircleWidth = mOutCircleWidth + mOuterCircleWidthInc * calPercent
                    processBarPaint.strokeWidth = curOutCircleWidth
                    outProcessCirclePaint.strokeWidth = curOutCircleWidth
                    outMostWhiteCirclePaint.strokeWidth = curOutCircleWidth
                    outBlackCircleRadius = (outMostCircleRadius + outIncDis - curOutCircleWidth / 2.0f)
                    outMostBlackCircleRadius = (curOutCircleWidth / 2.0f + (outMostCircleRadius + outIncDis))
                    outMostCircleRect = RectF(
                        centerX - outMostCircleRadius - outIncDis,
                        centerY - outMostCircleRadius - outIncDis,
                        centerX + outMostCircleRadius + outIncDis,
                        centerY + outMostCircleRadius + outIncDis
                    )
                    translucentCircleRadius = (outIncDis + outMostCircleRadius).toInt()
                    innerCircleRadiusToDraw = calPercent * innerCircleRadiusWhenRecord
                }
                invalidateCustom()
            } else {
                step++
                refreshView()
            }
        }
    }

    constructor(paramContext: Context?) : super(paramContext) {
        init(null)
    }

    constructor(paramContext: Context?, paramAttributeSet: AttributeSet?) : super(paramContext, paramAttributeSet) {
        init(paramAttributeSet)
    }

    constructor(paramContext: Context?, paramAttributeSet: AttributeSet?, paramInt: Int) : super(paramContext, paramAttributeSet, paramInt) {
        init(paramAttributeSet)
    }

    private fun init(paramAttributeSet: AttributeSet?) {
        if (context == null || context.theme == null) {
            return
        }
        // 调取样式中的颜色
        val arrayRoundBorder = context.theme.obtainStyledAttributes(intArrayOf(R.attr.click_long_button_round_border))
        val arrayInnerCircleInOperation = context.theme.obtainStyledAttributes(intArrayOf(R.attr.click_long_button_inner_circle_in_operation))
        val arrayInnerCircleNoOperation = context.theme.obtainStyledAttributes(intArrayOf(R.attr.click_long_button_inner_circle_no_operation))
        val arrayClickOrLongButtonStyle = context.obtainStyledAttributes(paramAttributeSet, R.styleable.ClickOrLongButton)
        // 计算出倍数
        val size = arrayClickOrLongButtonStyle.getInt(R.styleable.ClickOrLongButton_size, 100)
        multiple = size.toFloat() / 100

        val defaultRoundBorderColor = ResourcesCompat.getColor(
            resources, R.color.click_long_button_round_border,
            context.theme
        )
        val defaultInnerCircleInOperationColor = ResourcesCompat.getColor(
            resources, R.color.click_long_button_inner_circle_in_operation,
            context.theme
        )
        val defaultInnerCircleNoOperationColor = ResourcesCompat.getColor(
            resources, R.color.click_long_button_inner_circle_no_operation,
            context.theme
        )

        isRecordable = true
        isTouchable = true
        // 整块
        mBoundingBoxSize = dip2px(100.0f * multiple)
        // 外线宽度
        mOutCircleWidth = dip2px(2.3f * multiple)
        mOuterCircleWidthInc = dip2px(4.3f * multiple)
        mInnerCircleRadius = dip2px(32.0f * multiple).toFloat()

        colorRecord = arrayInnerCircleInOperation.getColor(0, defaultInnerCircleInOperationColor)
        colorRoundBorder = arrayRoundBorder.getColor(0, defaultRoundBorderColor)
        colorWhiteP60 = arrayInnerCircleNoOperation.getColor(0, defaultInnerCircleNoOperationColor)

        initProcessBarPaint()
        initOutCircle()
        initCenterCircle()
        // 状态为两者都可以
        mButtonState = BUTTON_STATE_BOTH
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayRoundBorder.close()
            arrayInnerCircleInOperation.close()
            arrayInnerCircleNoOperation.close()
            arrayClickOrLongButtonStyle.close()
        } else {
            arrayRoundBorder.recycle()
            arrayInnerCircleInOperation.recycle()
            arrayInnerCircleNoOperation.recycle()
            arrayClickOrLongButtonStyle.recycle()
        }
    }

    /**
     * 初始化内圈操作中样式
     */
    private fun initProcessBarPaint() {
        processBarPaint = Paint()
        processBarPaint.color = colorRecord
        processBarPaint.isAntiAlias = true
        processBarPaint.strokeWidth = mOutCircleWidth.toFloat()
        processBarPaint.style = Paint.Style.STROKE
        processBarPaint.strokeCap = Cap.ROUND
    }

    /**
     * 初始化外圈样式
     */
    private fun initOutCircle() {
        outMostWhiteCirclePaint.color = colorRoundBorder
        outMostWhiteCirclePaint.isAntiAlias = true
        outMostWhiteCirclePaint.strokeWidth = mOutCircleWidth.toFloat()
        outMostWhiteCirclePaint.style = Paint.Style.STROKE

        outProcessCirclePaint = Paint()
        outProcessCirclePaint.color = colorRecord
        outProcessCirclePaint.isAntiAlias = true
        outProcessCirclePaint.strokeWidth = mOutCircleWidth.toFloat()
        outProcessCirclePaint.style = Paint.Style.STROKE
    }

    /**
     * 初始化内圈未操作中样式
     */
    private fun initCenterCircle() {
        val colorBlackP40 = ContextCompat.getColor(context, R.color.black_forty_percent)
        val colorBlackP80 = ContextCompat.getColor(context, R.color.black_eighty_percent)
        val colorTranslucent = ContextCompat.getColor(context, R.color.circle_shallow_translucent_bg)

        centerCirclePaint.color = colorWhiteP60
        centerCirclePaint.isAntiAlias = true
        centerCirclePaint.style = Paint.Style.FILL_AND_STROKE
        outBlackCirclePaint.color = colorBlackP40
        outBlackCirclePaint.isAntiAlias = true
        outBlackCirclePaint.style = Paint.Style.STROKE
        outBlackCirclePaint.strokeWidth = 1.0f
        outMostBlackCirclePaint.color = colorBlackP80
        outMostBlackCirclePaint.isAntiAlias = true
        outMostBlackCirclePaint.style = Paint.Style.STROKE
        outMostBlackCirclePaint.strokeWidth = 1.0f
        translucentPaint.color = colorTranslucent
        translucentPaint.isAntiAlias = true
        translucentPaint.style = Paint.Style.FILL_AND_STROKE
        centerX = (mBoundingBoxSize / 2f)
        centerY = (mBoundingBoxSize / 2f)
        outMostCircleRadius = dip2px(37.0f * multiple).toFloat()
        outBlackCircleRadiusInc = dip2px(7.0f * multiple)
        innerCircleRadiusWhenRecord = dip2px(35.0f * multiple).toFloat()
        innerCircleRadiusToDraw = mInnerCircleRadius
        outBlackCircleRadius = (outMostCircleRadius - mOutCircleWidth / 2.0f)
        outMostBlackCircleRadius = (outMostCircleRadius + mOutCircleWidth / 2.0f)
        startAngle270 = 270.0f
        percentInDegree = 0.0f
        outMostCircleRect = RectF(centerX - outMostCircleRadius, centerY - outMostCircleRadius, centerX + outMostCircleRadius, centerY + outMostCircleRadius)

    }

    /**
     * 销毁事件
     */
    fun onDestroy() {
        touchTimeHandler.clearMsg()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(mBoundingBoxSize, mBoundingBoxSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerX, centerY, translucentCircleRadius.toFloat(), translucentPaint)

        //center white-p40 circle  中心点+半径32，所以直接64就是内圈的宽高度了
        canvas.drawCircle(centerX, centerY, innerCircleRadiusToDraw, centerCirclePaint)

        // 静止状态时的外圈
        canvas.drawArc(outMostCircleRect, startAngle270, 360f, false, outMostWhiteCirclePaint)

        // 点击时的外圈进度
        canvas.drawArc(outMostCircleRect, startAngle270, percentInDegree, false, processBarPaint)

        // 静止状态时的外圈进度
        canvas.drawArc(outMostCircleRect, startAngle270, mCurrentSumNumberDegrees, false, outProcessCirclePaint)


        canvas.drawCircle(centerX, centerY, outBlackCircleRadius, outBlackCirclePaint)
        canvas.drawCircle(centerX, centerY, outMostBlackCircleRadius, outMostBlackCirclePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (mButtonState != BUTTON_STATE_CLICK_AND_HOLD) {
                if (mRecordedTime / timeLimitInMils >= FULL_PROGRESS) {
                    // 进度已满,不执行任何动作
                    return true
                }
                if (mCurrentSumTime / timeLimitInMils >= FULL_PROGRESS) {
                    // 进度已满,不执行任何动作
                    return true
                }
                // 判断是否禁用模式
                if (!isTouchable) {
                    mClickOrLongListener?.onBanClickTips()
                    return true
                }
                step = STEP_ACTION_DOWN
                // 是否支持长按
                mClickOrLongListener?.let {
                    val longClick = mButtonState == BUTTON_STATE_ONLY_LONG_CLICK || mButtonState == BUTTON_STATE_BOTH
                    if (longClick) {
                        startTicking()
                    }
                }
            }

            MotionEvent.ACTION_UP -> if (mButtonState == BUTTON_STATE_CLICK_AND_HOLD) {
                // 点击即长按模式
                if (recordState != RECORD_STARTED) {
                    mClickOrLongListener?.let { clickOrLongListener ->
                        // 判断是否禁用模式
                        if (!isTouchable) {
                            clickOrLongListener.onBanClickTips()
                            return true
                        }
                        // 未启动状态，即立刻启动长按动画
                        step = STEP_ACTION_DOWN
                        startTicking()
                        clickOrLongListener.onClickStopTips()
                    }
                } else {
                    // 已经启动状态，刷新view执行事件
                    step++
                    refreshView()
                }
            } else {
                // 其他模式
                step++
                refreshView()
            }

            MotionEvent.ACTION_MOVE -> if (mButtonState != BUTTON_STATE_CLICK_AND_HOLD) {
                if (mRecordedTime / timeLimitInMils >= FULL_PROGRESS) {
                    step++
                    refreshView()
                    return true
                }
            }

            else -> {}
        }
        return true
    }

    /**
     * 刷新view
     */
    private fun refreshView() {
        synchronized(this@ClickOrLongButton) {
            mClickOrLongListener?.let { clickOrLongListener ->
                if (recordState == RECORD_STARTED) {
                    if (step == STEP_ACTION_UP) {
                        // 回调录制结束
                        if (mRecordedTime / timeLimitInMils >= FULL_PROGRESS) {
                            clickOrLongListener.onLongClickFinish()
                        } else {
                            clickOrLongListener.onLongClickEnd(mRecordedTime)
                        }
                    }
                    setRecordState(RECORD_ENDED)
                } else if (recordState == RECORD_ENDED) {
                    // 回到初始状态
                    setRecordState(RECORD_NOT_STARTED)
                } else {
                    // 如果只支持长按事件则不触发
                    if (mButtonState != BUTTON_STATE_ONLY_LONG_CLICK && step == STEP_ACTION_UP) {
                        // 拍照
                        clickOrLongListener.onClick()
                    }
                }
            }
        }
        reset()
    }

    /**
     * 重置
     */
    fun reset() {
        resetCommon()
        mCurrentSumNumberDegrees = 0f
        mCurrentSumTime = 0L
        mCurrentLocation.clear()
        invalidateCustom()
    }

    /**
     * 中断当前操作
     */
    fun breakOff() {
        resetCommon()
        invalidateCustom()
    }

    private fun resetCommon() {
        step = STEP_NOT_TOUCH
        mActionDown = false
        touchTimeHandler.clearMsg()
        percentInDegree = 0.0f
        mRecordedTime = 0
        centerCirclePaint.color = colorWhiteP60
        outMostWhiteCirclePaint.color = colorRoundBorder
        innerCircleRadiusToDraw = mInnerCircleRadius
        outMostCircleRect = RectF(centerX - outMostCircleRadius, centerY - outMostCircleRadius, centerX + outMostCircleRadius, centerY + outMostCircleRadius)
        translucentCircleRadius = 0
        processBarPaint.strokeWidth = mOutCircleWidth.toFloat()
        outProcessCirclePaint.strokeWidth = mOutCircleWidth.toFloat()
        outMostWhiteCirclePaint.strokeWidth = mOutCircleWidth.toFloat()
        outBlackCircleRadius = (outMostCircleRadius - mOutCircleWidth / 2.0f)
        outMostBlackCircleRadius = (outMostCircleRadius + mOutCircleWidth / 2.0f)
    }

    private fun startTicking() {
        synchronized(this@ClickOrLongButton) {
            if (recordState != RECORD_NOT_STARTED) {
                setRecordState(RECORD_NOT_STARTED)
            }
        }
        btnPressTime = System.currentTimeMillis()
        touchTimeHandler.sendLoopMsg(0L, 16L)
    }

    /**
     * 数据设置成适合当前圆形
     *
     *
     * // 计算方式1：270至360是一个初始点，类似0-90
     * // 计算方式2: 所以如果是小于90点，就直接+270
     * // 计算方式3：如果大于等于90点，就直接-90
     *
     * @return numberDegrees
     */
    private fun getNumberDegrees(numberDegrees: Float): Float {
        return if (numberDegrees >= NINETY_DEGREES) {
            numberDegrees - 90
        } else {
            numberDegrees + 270
        }
    }


    private fun invalidateCustom() {
        invalidate()
    }

    /**
     * 按钮回调接口
     */
    private var mClickOrLongListener: ClickOrLongListener? = null

    /**
     * 判断是否已经调用过isActionDown,结束后重置此值
     */
    private var mActionDown = false

    private fun setCurrentSumNumberDegrees(value: Float) {
        mCurrentSumNumberDegrees = value
    }

    // region 对外方法
    /**
     * 设置最长录制时间
     *
     * @param duration 时间
     */
    fun setDuration(duration: Int) {
        timeLimitInMils = duration.toFloat()
    }

    /**
     * 长按准备时间
     * 长按达到duration时间后，才开启录制
     *
     * @param duration 时间
     */
    fun setReadinessDuration(duration: Int) {
        mMinDurationAnimation = duration
        mMinDurationAnimationCurrent = mMinDurationAnimation
    }

    /**
     * 设置当前已录制的时间，用于分段录制
     */
    fun setCurrentTime(currentTime: Long) {
        mCurrentLocation.clear()
        // 获取当前时间占比
        val percent = currentTime / timeLimitInMils
        // 根据360度，以这个占比计算是具体多少度
        val numberDegrees = percent * 360
        // 数据设置规范,适合当前圆形
        mCurrentLocation.add(getNumberDegrees(numberDegrees))
        setCurrentSumNumberDegrees(numberDegrees)
        mCurrentSumTime = currentTime
        mRecordedTime = currentTime
        invalidate()
    }

    /**
     * 设置回调接口
     *
     * @param clickOrLongListener 回调接口
     */
    fun setRecordingListener(clickOrLongListener: ClickOrLongListener?) {
        this.mClickOrLongListener = clickOrLongListener
    }

    /**
     * 设置按钮功能（点击和长按）
     *
     * @param buttonStateBoth 只能点击 [ClickOrLongButton.BUTTON_STATE_ONLY_CLICK]
     */
    fun setButtonFeatures(buttonStateBoth: Int) {
        this.mButtonState = buttonStateBoth
        if (buttonStateBoth == BUTTON_STATE_CLICK_AND_HOLD) {
            mMinDurationAnimationCurrent = 0
        }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        // 回到初始状态
        setRecordState(RECORD_NOT_STARTED)
        // 预备时间也恢复到初始设置时的时间
        mMinDurationAnimationCurrent = mMinDurationAnimation
    }

    private fun setRecordState(recordState: Int) {
        this.recordState = recordState
    } // endregion

    companion object {
        /**
         * 按钮只能点击
         */
        const val BUTTON_STATE_ONLY_CLICK: Int = 0x201

        /**
         * 按钮只能长按
         */
        const val BUTTON_STATE_ONLY_LONG_CLICK: Int = 0x202

        /**
         * 按钮点击或者长按两者都可以
         */
        const val BUTTON_STATE_BOTH: Int = 0x203

        /**
         * 按钮点击即是长按模式
         */
        const val BUTTON_STATE_CLICK_AND_HOLD: Int = 0x204

        /**
         * 满进度
         */
        private const val FULL_PROGRESS = 1f

        /**
         * 90度
         */
        private const val NINETY_DEGREES = 90

        /**
         * 未启动状态
         */
        private const val RECORD_NOT_STARTED = 0

        /**
         * 启动状态
         */
        private const val RECORD_STARTED = 1

        /**
         * 结束状态
         */
        private const val RECORD_ENDED = 2

        /**
         * 当前为未触摸状态
         *
         *
         * 为了确保整个按钮的逻辑是：按下 - 放开手，按下时+1，松手+1，最后等于2就是整个流程结束
         * 如果中间中断或者重置，那就直接减1，就说明中断流程
         */
        private const val STEP_NOT_TOUCH = 0

        /**
         * 当前为按下状态
         *
         *
         * 为了确保整个按钮的逻辑是：按下 - 放开手，按下时+1，松手+1，最后等于2就是整个流程结束
         * 如果中间中断或者重置，那就直接减1，就说明中断流程
         */
        private const val STEP_ACTION_DOWN = 1

        /**
         * 当前为松手状态
         *
         *
         * 为了确保整个按钮的逻辑是：按下 - 放开手，按下时+1，松手+1，最后等于2就是整个流程结束
         * 如果中间中断或者重置，那就直接减1，就说明中断流程
         */
        private const val STEP_ACTION_UP = 2

        private const val PROGRESS_LIM_TO_FINISH_STARTING_ANIM = 0.1f
    }

    private class UpdateUITask(private val weakButton: WeakReference<ClickOrLongButton>) : TouchTimeHandler.Task {
        override fun run() {
            val button = weakButton.get()
            button?.let {
                // 判断如果是 点击即长按 模式的情况下，判断进度是否>=100
                if (it.mButtonState == BUTTON_STATE_CLICK_AND_HOLD) {
                    if (it.mRecordedTime / it.timeLimitInMils >= FULL_PROGRESS) {
                        it.step++
                        it.refreshView()
                        return
                    }
                }
                if (it.mCurrentLocation.isNotEmpty()) {
                    // 当处于分段录制模式并且有分段数据的时候，关闭启动前奏
                    it.mMinDurationAnimationCurrent = 0
                }
                val timeLapse = System.currentTimeMillis() - it.btnPressTime
                it.mRecordedTime = (timeLapse - it.mMinDurationAnimationCurrent)
                it.mRecordedTime += it.mCurrentSumTime
                val percent = it.mRecordedTime / it.timeLimitInMils
                if (!it.mActionDown && timeLapse >= 1) {
                    it.mClickOrLongListener?.let { listener ->
                        val actionDown = it.mButtonState == BUTTON_STATE_ONLY_CLICK || it.mButtonState == BUTTON_STATE_BOTH
                        if (actionDown) {
                            listener.actionDown()
                            it.mActionDown = true
                        }
                    }
                }
                it.startAnimation(timeLapse, percent)
            }
        }
    }
}
