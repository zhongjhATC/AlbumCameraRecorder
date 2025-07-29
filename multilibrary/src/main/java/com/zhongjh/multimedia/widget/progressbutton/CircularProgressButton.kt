package com.zhongjh.multimedia.widget.progressbutton

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.util.StateSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import com.zhongjh.multimedia.R


open class CircularProgressButton : AppCompatButton {
    private lateinit var mContext: Context

    private var background: StrokeGradientDrawable? = null
    private var mAnimatedDrawable: CircularAnimatedDrawable? = null
    private var mProgressDrawable: CircularProgressDrawable? = null

    private var mIdleColorState: ColorStateList? = null
    private var mCompleteColorState: ColorStateList? = null
    private var mErrorColorState: ColorStateList? = null

    private var mIdleStateDrawable = StateListDrawable()
    private var mCompleteStateDrawable = StateListDrawable()
    private var mErrorStateDrawable = StateListDrawable()

    private lateinit var mStateManager: StateManager
    private var mState = State.IDLE
    var idleText: String? = null
    var completeText: String? = null
    var errorText: String? = null
    private var mProgressText: String? = null

    private var mColorProgress = 0
    private var mColorIndicator = 0
    private var mColorIndicatorBackground = 0
    private var mIconComplete = 0
    private var mIconError = 0
    private var mStrokeWidth = 0
    private var mPaddingProgress = 0
    private var mCornerRadius = 0f
    var isIndeterminateProgressMode: Boolean = false
    private var mConfigurationChanged = false

    private enum class State {
        PROGRESS, IDLE, COMPLETE, ERROR
    }

    private var mMaxProgress = 0
    private var mProgress = 0

    private var mMorphingInProgress = false

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        mContext = context
        mStrokeWidth = getContext().resources.getDimension(R.dimen.z_cpb_stroke_width).toInt()

        initAttributes(context, attributeSet)

        mMaxProgress = 100
        mStateManager = StateManager(this)

        text = idleText

        initIdleStateDrawable()
        setBackgroundCompat(mIdleStateDrawable)
    }

    private fun initErrorStateDrawable() {
        val colorPressed = getPressedColor(mErrorColorState)

        val drawablePressed = createDrawable(colorPressed)

        drawablePressed?.let {
            mErrorStateDrawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePressed.gradientDrawable)
        }
        mErrorStateDrawable.addState(StateSet.WILD_CARD, background?.gradientDrawable)
    }

    private fun initCompleteStateDrawable() {
        val colorPressed = getPressedColor(mCompleteColorState)

        val drawablePressed = createDrawable(colorPressed)

        drawablePressed?.let {
            mCompleteStateDrawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePressed.gradientDrawable)
        }
        mCompleteStateDrawable.addState(StateSet.WILD_CARD, background?.gradientDrawable)
    }

    private fun initIdleStateDrawable() {
        val colorNormal = getNormalColor(mIdleColorState)
        val colorPressed = getPressedColor(mIdleColorState)
        val colorFocused = getFocusedColor(mIdleColorState)
        val colorDisabled = getDisabledColor(mIdleColorState)
        if (background == null) {
            background = createDrawable(colorNormal)
        }

        val drawableDisabled = createDrawable(colorDisabled)
        val drawableFocused = createDrawable(colorFocused)
        val drawablePressed = createDrawable(colorPressed)

        mIdleStateDrawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePressed?.gradientDrawable)
        mIdleStateDrawable.addState(intArrayOf(android.R.attr.state_focused), drawableFocused?.gradientDrawable)
        mIdleStateDrawable.addState(intArrayOf(-android.R.attr.state_enabled), drawableDisabled?.gradientDrawable)
        mIdleStateDrawable.addState(StateSet.WILD_CARD, background?.gradientDrawable)
    }

    private fun getNormalColor(colorStateList: ColorStateList?): Int {
        return colorStateList!!.getColorForState(intArrayOf(android.R.attr.state_enabled), 0)
    }

    private fun getPressedColor(colorStateList: ColorStateList?): Int {
        return colorStateList!!.getColorForState(intArrayOf(android.R.attr.state_pressed), 0)
    }

    private fun getFocusedColor(colorStateList: ColorStateList?): Int {
        return colorStateList!!.getColorForState(intArrayOf(android.R.attr.state_focused), 0)
    }

    private fun getDisabledColor(colorStateList: ColorStateList?): Int {
        return colorStateList!!.getColorForState(intArrayOf(-android.R.attr.state_enabled), 0)
    }

    private fun createDrawable(color: Int): StrokeGradientDrawable? {
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.cpb_background, mContext.theme)
        if (drawable != null) {
            val gradientDrawable = drawable.mutate() as GradientDrawable
            gradientDrawable.setColor(color)
            gradientDrawable.cornerRadius = mCornerRadius
            val strokeGradientDrawable = StrokeGradientDrawable(gradientDrawable)
            strokeGradientDrawable.strokeColor = color
            strokeGradientDrawable.strokeWidth = mStrokeWidth
            return strokeGradientDrawable
        }
        return null
    }

    override fun drawableStateChanged() {
        if (mState == State.COMPLETE) {
            initCompleteStateDrawable()
            setBackgroundCompat(mCompleteStateDrawable)
        } else if (mState == State.IDLE) {
            initIdleStateDrawable()
            setBackgroundCompat(mIdleStateDrawable)
        } else if (mState == State.ERROR) {
            initErrorStateDrawable()
            setBackgroundCompat(mErrorStateDrawable)
        }

        if (mState != State.PROGRESS) {
            super.drawableStateChanged()
        }
    }

    /**
     * 本来用这些代码取view的attr，但是因为代码动态设置背景颜色不生效，故只能该view直接取Activity的attr
     * int idleStateSelector = attr.getResourceId(R.styleable.CircularProgressButton_cpb_selectorIdle,
     * R.color.cpb_idle_state_selector_zjh);
     *
     *
     * int completeStateSelector = attr.getResourceId(R.styleable.CircularProgressButton_cpb_selectorComplete,
     * R.color.cpb_complete_state_selector_zjh);
     * mCompleteColorState = ResourcesCompat.getColorStateList(getResources(), completeStateSelector, mContext.getTheme());
     *
     *
     * int errorStateSelector = attr.getResourceId(R.styleable.CircularProgressButton_cpb_selectorError,
     * R.color.cpb_error_state_selector_zjh);
     * mErrorColorState = ResourcesCompat.getColorStateList(getResources(), errorStateSelector, mContext.getTheme());
     *
     *
     * mIconComplete = attr.getResourceId(R.styleable.CircularProgressButton_cpb_iconComplete, 0);
     * mIconError = attr.getResourceId(R.styleable.CircularProgressButton_cpb_iconError, 0);
     *
     *
     * mColorIndicatorBackground = attr.getColor(R.styleable.CircularProgressButton_cpb_colorIndicatorBackground, grey);
     *
     *
     * int white = getColor(R.color.cpb_white);
     * int grey = getColor(R.color.cpb_grey);
     *
     * @param context      上下文
     * @param attributeSet 属性
     */
    private fun initAttributes(context: Context, attributeSet: AttributeSet?) {
        val attr = getTypedArray(context, attributeSet, R.styleable.CircularProgressButton)

        try {
            // 由Activity主题提供的样式：提交按钮的文字的文本
            val confirmTextValue = mContext.theme.obtainStyledAttributes(intArrayOf(R.attr.preview_video_button_confirm_text_value))
            idleText = if (confirmTextValue.length() <= 0) {
                attr.getString(R.styleable.CircularProgressButton_cpb_textIdle)
            } else {
                confirmTextValue.getText(0).toString()
            }

            // 由Activity主题提供的样式：提交按钮的文字的颜色
            val confirmTextColor = mContext.theme.obtainStyledAttributes(intArrayOf(R.attr.preview_video_button_confirm_text_color))
            val confirmTextColorDefault = ResourcesCompat.getColor(resources, R.color.white, mContext.theme)
            setTextColor(confirmTextColor.getColor(0, confirmTextColorDefault))

            // 由Activity主题提供的样式：提交按钮的背景 - 深颜色
            val confirmBackgroundColor = mContext.theme.obtainStyledAttributes(intArrayOf(R.attr.preview_video_button_confirm_background_color))
            val confirmBackground = confirmBackgroundColor.getColorStateList(0)

            val confirmBackgroundDefault = ResourcesCompat.getColorStateList(resources, R.color.operation_background, mContext.theme)

            // 由Activity主题提供的样式：提交按钮的背景 - 浅颜色
            val confirmBackgroundProgress = mContext.theme.obtainStyledAttributes(intArrayOf(R.attr.preview_video_button_confirm_background_progress_color))
            val confirmBackgroundProgressDefault = ResourcesCompat.getColor(resources, R.color.white, mContext.theme)

            mIdleColorState = confirmBackground ?: confirmBackgroundDefault
            mCompleteColorState = confirmBackground ?: confirmBackgroundDefault
            mErrorColorState = confirmBackground ?: confirmBackgroundDefault

            // 由Activity主题提供的样式：提交按钮的完成时图标
            val confirmComplete = mContext.theme.obtainStyledAttributes(intArrayOf(R.attr.preview_video_button_confirm_icon_complete))
            mIconComplete = confirmComplete.getResourceId(0, R.drawable.ic_baseline_done)

            // 由Activity主题提供的样式：提交按钮的失败时图标
            val confirmError = mContext.theme.obtainStyledAttributes(intArrayOf(R.attr.preview_video_button_confirm_icon_error))
            mIconError = confirmError.getResourceId(0, R.drawable.ic_baseline_close_24)

            completeText = attr.getString(R.styleable.CircularProgressButton_cpb_textComplete)
            errorText = attr.getString(R.styleable.CircularProgressButton_cpb_textError)
            mProgressText = attr.getString(R.styleable.CircularProgressButton_cpb_textProgress)

            mCornerRadius = attr.getDimension(R.styleable.CircularProgressButton_cpb_cornerRadius, 0f)
            mPaddingProgress = attr.getDimensionPixelSize(R.styleable.CircularProgressButton_cpb_paddingProgress, 0)

            val blue = getColor(R.color.cpb_blue)

            mColorIndicator = attr.getColor(R.styleable.CircularProgressButton_cpb_colorIndicator, blue)
            // 进度时的内圆样式
            mColorProgress = confirmBackgroundProgress.getColor(0, confirmBackgroundProgressDefault)
            // 进度时的周边线样式
            mColorIndicatorBackground = getNormalColor(mIdleColorState)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                confirmTextValue.close()
            } else {
                confirmTextValue.recycle()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                confirmBackgroundColor.close()
            } else {
                confirmBackgroundColor.recycle()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                confirmBackgroundProgress.close()
            } else {
                confirmBackgroundProgress.recycle()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                confirmComplete.close()
            } else {
                confirmComplete.recycle()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                confirmError.close()
            } else {
                confirmError.recycle()
            }
        } finally {
            attr.recycle()
        }
    }

    private fun getColor(id: Int): Int {
        return ResourcesCompat.getColor(resources, id, mContext.theme)
    }

    private fun getTypedArray(context: Context, attributeSet: AttributeSet?, attr: IntArray): TypedArray {
        return context.obtainStyledAttributes(attributeSet, attr, 0, 0)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mProgress > 0 && mState == State.PROGRESS && !mMorphingInProgress) {
            if (isIndeterminateProgressMode) {
                drawIndeterminateProgress(canvas)
            } else {
                drawProgress(canvas)
            }
        }
    }

    private fun drawIndeterminateProgress(canvas: Canvas) {
        mAnimatedDrawable?.draw(canvas).let {
            val offset = (width - height) / 2
            mAnimatedDrawable = CircularAnimatedDrawable(mColorIndicator, mStrokeWidth.toFloat())
            val left = offset + mPaddingProgress
            val right = width - offset - mPaddingProgress
            val bottom = height - mPaddingProgress
            val top = mPaddingProgress
            mAnimatedDrawable.setBounds(left, top, right, bottom)
            mAnimatedDrawable.callback = this
            mAnimatedDrawable.start()
        }
    }

    private fun drawProgress(canvas: Canvas) {
        if (mProgressDrawable == null) {
            val offset = (width - height) / 2
            val size = height - mPaddingProgress * 2
            mProgressDrawable = CircularProgressDrawable(size, mStrokeWidth, mColorIndicator)
            val left = offset + mPaddingProgress
            mProgressDrawable!!.setBounds(left, mPaddingProgress, left, mPaddingProgress)
        }
        val sweepAngle = (360f / mMaxProgress) * mProgress
        mProgressDrawable!!.setSweepAngle(sweepAngle)
        mProgressDrawable!!.draw(canvas)
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return who === mAnimatedDrawable || super.verifyDrawable(who)
    }

    private fun createMorphing(): MorphingAnimation {
        mMorphingInProgress = true

        val animation = MorphingAnimation(this, background)
        animation.setFromCornerRadius(mCornerRadius)
        animation.setToCornerRadius(mCornerRadius)

        animation.setFromWidth(width)
        animation.setToWidth(width)

        if (mConfigurationChanged) {
            animation.setDuration(MorphingAnimation.DURATION_INSTANT)
        } else {
            animation.setDuration(MorphingAnimation.DURATION_NORMAL)
        }

        mConfigurationChanged = false

        return animation
    }

    private fun createProgressMorphing(fromCorner: Float, toCorner: Float, fromWidth: Int, toWidth: Int): MorphingAnimation {
        mMorphingInProgress = true

        val animation = MorphingAnimation(this, background)
        animation.setFromCornerRadius(fromCorner)
        animation.setToCornerRadius(toCorner)

        animation.setPadding(mPaddingProgress.toFloat())

        animation.setFromWidth(fromWidth)
        animation.setToWidth(toWidth)

        if (mConfigurationChanged) {
            animation.setDuration(MorphingAnimation.DURATION_INSTANT)
        } else {
            animation.setDuration(MorphingAnimation.DURATION_NORMAL)
        }

        mConfigurationChanged = false

        return animation
    }

    private fun morphToProgress() {
        width = width
        text = mProgressText

        val animation = createProgressMorphing(mCornerRadius, height.toFloat(), width, height)

        animation.setFromColor(getNormalColor(mIdleColorState))
        animation.setToColor(mColorProgress)

        animation.setFromStrokeColor(getNormalColor(mIdleColorState))
        animation.setToStrokeColor(mColorIndicatorBackground)

        animation.setListener(mProgressStateListener)

        animation.start()
    }

    private val mProgressStateListener = OnAnimationEndListener {
        mMorphingInProgress = false
        mState = State.PROGRESS
        mStateManager.checkState(this@CircularProgressButton)
    }

    private fun morphProgressToComplete() {
        val animation = createProgressMorphing(height.toFloat(), mCornerRadius, height, width)

        animation.setFromColor(mColorProgress)
        animation.setToColor(getNormalColor(mCompleteColorState))

        animation.setFromStrokeColor(mColorIndicator)
        animation.setToStrokeColor(getNormalColor(mCompleteColorState))

        animation.setListener(mCompleteStateListener)

        animation.start()
    }

    private fun morphIdleToComplete() {
        val animation = createMorphing()

        animation.setFromColor(getNormalColor(mIdleColorState))
        animation.setToColor(getNormalColor(mCompleteColorState))

        animation.setFromStrokeColor(getNormalColor(mIdleColorState))
        animation.setToStrokeColor(getNormalColor(mCompleteColorState))

        animation.setListener(mCompleteStateListener)

        animation.start()
    }

    private val mCompleteStateListener: OnAnimationEndListener = object : OnAnimationEndListener {
        override fun onAnimationEnd() {
            if (mIconComplete != 0) {
                text = null
                setIcon(mIconComplete)
            } else {
                setText(this@CircularProgressButton.completeText)
            }
            mMorphingInProgress = false
            mState = State.COMPLETE

            mStateManager.checkState(this@CircularProgressButton)
        }
    }

    private fun morphCompleteToIdle() {
        val animation = createMorphing()

        animation.setFromColor(getNormalColor(mCompleteColorState))
        animation.setToColor(getNormalColor(mIdleColorState))

        animation.setFromStrokeColor(getNormalColor(mCompleteColorState))
        animation.setToStrokeColor(getNormalColor(mIdleColorState))

        animation.setListener(mIdleStateListener)

        animation.start()
    }

    private fun morphErrorToIdle() {
        val animation = createMorphing()

        animation.setFromColor(getNormalColor(mErrorColorState))
        animation.setToColor(getNormalColor(mIdleColorState))

        animation.setFromStrokeColor(getNormalColor(mErrorColorState))
        animation.setToStrokeColor(getNormalColor(mIdleColorState))

        animation.setListener(mIdleStateListener)

        animation.start()
    }

    private val mIdleStateListener: OnAnimationEndListener = object : OnAnimationEndListener {
        override fun onAnimationEnd() {
            removeIcon()
            setText(this@CircularProgressButton.idleText)
            mMorphingInProgress = false
            mState = State.IDLE

            mStateManager.checkState(this@CircularProgressButton)
        }
    }

    private fun morphIdleToError() {
        val animation = createMorphing()

        animation.setFromColor(getNormalColor(mIdleColorState))
        animation.setToColor(getNormalColor(mErrorColorState))

        animation.setFromStrokeColor(getNormalColor(mIdleColorState))
        animation.setToStrokeColor(getNormalColor(mErrorColorState))

        animation.setListener(mErrorStateListener)

        animation.start()
    }

    private fun morphProgressToError() {
        val animation = createProgressMorphing(height.toFloat(), mCornerRadius, height, width)

        animation.setFromColor(mColorProgress)
        animation.setToColor(getNormalColor(mErrorColorState))

        animation.setFromStrokeColor(mColorIndicator)
        animation.setToStrokeColor(getNormalColor(mErrorColorState))
        animation.setListener(mErrorStateListener)

        animation.start()
    }

    private val mErrorStateListener: OnAnimationEndListener = object : OnAnimationEndListener {
        override fun onAnimationEnd() {
            if (mIconError != 0) {
                text = null
                setIcon(mIconError)
            } else {
                setText(this@CircularProgressButton.errorText)
            }
            mMorphingInProgress = false
            mState = State.ERROR

            mStateManager.checkState(this@CircularProgressButton)
        }
    }

    private fun morphProgressToIdle() {
        val animation = createProgressMorphing(height.toFloat(), mCornerRadius, height, width)

        animation.setFromColor(mColorProgress)
        animation.setToColor(getNormalColor(mIdleColorState))

        animation.setFromStrokeColor(mColorIndicator)
        animation.setToStrokeColor(getNormalColor(mIdleColorState))
        animation.setListener {
            removeIcon()
            text = idleText
            mMorphingInProgress = false
            mState = State.IDLE
            mStateManager.checkState(this@CircularProgressButton)
        }

        animation.start()
    }

    private fun setIcon(icon: Int) {
        val drawable = ResourcesCompat.getDrawable(resources, icon, mContext.theme)
        if (drawable != null) {
            val padding = (width / 2) - (drawable.intrinsicWidth / 2)
            setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
            setPadding(padding, 0, 0, 0)
        }
    }

    protected fun removeIcon() {
        setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        setPadding(0, 0, 0, 0)
    }

    /**
     * Set the View's background. Masks the API changes made in Jelly Bean.
     */
    fun setBackgroundCompat(drawable: Drawable?) {
        setBackground(drawable)
    }

    var progress: Int
        get() = mProgress
        set(progress) {
            mProgress = progress

            if (mMorphingInProgress || width == 0) {
                return
            }

            mStateManager.saveProgress(this)

            if (mProgress >= mMaxProgress) {
                if (mState == State.PROGRESS) {
                    morphProgressToComplete()
                } else if (mState == State.IDLE) {
                    morphIdleToComplete()
                }
            } else if (mProgress > IDLE_STATE_PROGRESS) {
                if (mState == State.IDLE) {
                    morphToProgress()
                } else if (mState == State.PROGRESS) {
                    invalidate()
                }
            } else if (mProgress == ERROR_STATE_PROGRESS) {
                if (mState == State.PROGRESS) {
                    morphProgressToError()
                } else if (mState == State.IDLE) {
                    morphIdleToError()
                }
            } else if (mProgress == IDLE_STATE_PROGRESS) {
                if (mState == State.COMPLETE) {
                    morphCompleteToIdle()
                } else if (mState == State.PROGRESS) {
                    morphProgressToIdle()
                } else if (mState == State.ERROR) {
                    morphErrorToIdle()
                }
            }
        }

    override fun setBackgroundColor(color: Int) {
        background!!.gradientDrawable.setColor(color)
    }

    fun setStrokeColor(color: Int) {
        background!!.strokeColor = color
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            progress = mProgress
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.mProgress = mProgress
        savedState.mIndeterminateProgressMode = isIndeterminateProgressMode
        savedState.mConfigurationChanged = true

        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            val savedState = state
            mProgress = savedState.mProgress
            isIndeterminateProgressMode = savedState.mIndeterminateProgressMode
            mConfigurationChanged = savedState.mConfigurationChanged
            super.onRestoreInstanceState(savedState.superState)
            progress = mProgress
        } else {
            super.onRestoreInstanceState(state)
        }
    }


    internal class SavedState : BaseSavedState {
        var mIndeterminateProgressMode: Boolean = false
        var mConfigurationChanged: Boolean = false
        var mProgress: Int = 0

        constructor(parcel: Parcelable?) : super(parcel)

        private constructor(parcel: Parcel) : super(parcel) {
            mProgress = parcel.readInt()
            mIndeterminateProgressMode = parcel.readInt() == 1
            mConfigurationChanged = parcel.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(mProgress)
            out.writeInt(if (mIndeterminateProgressMode) 1 else 0)
            out.writeInt(if (mConfigurationChanged) 1 else 0)
        }

        companion object CREATOR : Creator<SavedState> {

            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        const val IDLE_STATE_PROGRESS: Int = 0
        const val ERROR_STATE_PROGRESS: Int = -1
    }
}
