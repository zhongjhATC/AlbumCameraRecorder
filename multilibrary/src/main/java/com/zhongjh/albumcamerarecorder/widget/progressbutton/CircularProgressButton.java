package com.zhongjh.albumcamerarecorder.widget.progressbutton;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.StateSet;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;

import com.zhongjh.albumcamerarecorder.R;

public class CircularProgressButton extends AppCompatButton {

    public static final int IDLE_STATE_PROGRESS = 0;
    public static final int ERROR_STATE_PROGRESS = -1;

    private Context mContext;

    private StrokeGradientDrawable background;
    private CircularAnimatedDrawable mAnimatedDrawable;
    private CircularProgressDrawable mProgressDrawable;

    private ColorStateList mIdleColorState;
    private ColorStateList mCompleteColorState;
    private ColorStateList mErrorColorState;

    private StateListDrawable mIdleStateDrawable;
    private StateListDrawable mCompleteStateDrawable;
    private StateListDrawable mErrorStateDrawable;

    private StateManager mStateManager;
    private State mState;
    private String mIdleText;
    private String mCompleteText;
    private String mErrorText;
    private String mProgressText;

    private int mColorProgress;
    private int mColorIndicator;
    private int mColorIndicatorBackground;
    private int mIconComplete;
    private int mIconError;
    private int mStrokeWidth;
    private int mPaddingProgress;
    private float mCornerRadius;
    private boolean mIndeterminateProgressMode;
    private boolean mConfigurationChanged;

    private enum State {
        PROGRESS, IDLE, COMPLETE, ERROR
    }

    private int mMaxProgress;
    private int mProgress;

    private boolean mMorphingInProgress;

    public CircularProgressButton(Context context) {
        super(context);
        init(context, null);
    }

    public CircularProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularProgressButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mContext = context;
        mStrokeWidth = (int) getContext().getResources().getDimension(R.dimen.z_cpb_stroke_width);

        initAttributes(context, attributeSet);

        mMaxProgress = 100;
        mState = State.IDLE;
        mStateManager = new StateManager(this);

        setText(mIdleText);

        initIdleStateDrawable();
        setBackgroundCompat(mIdleStateDrawable);
    }

    private void initErrorStateDrawable() {
        int colorPressed = getPressedColor(mErrorColorState);

        StrokeGradientDrawable drawablePressed = createDrawable(colorPressed);
        mErrorStateDrawable = new StateListDrawable();

        if (drawablePressed != null) {
            mErrorStateDrawable.addState(new int[]{android.R.attr.state_pressed}, drawablePressed.getGradientDrawable());
        }
        mErrorStateDrawable.addState(StateSet.WILD_CARD, background.getGradientDrawable());
    }

    private void initCompleteStateDrawable() {
        int colorPressed = getPressedColor(mCompleteColorState);

        StrokeGradientDrawable drawablePressed = createDrawable(colorPressed);
        mCompleteStateDrawable = new StateListDrawable();

        if (drawablePressed != null) {
            mCompleteStateDrawable.addState(new int[]{android.R.attr.state_pressed}, drawablePressed.getGradientDrawable());
        }
        mCompleteStateDrawable.addState(StateSet.WILD_CARD, background.getGradientDrawable());
    }

    private void initIdleStateDrawable() {
        int colorNormal = getNormalColor(mIdleColorState);
        int colorPressed = getPressedColor(mIdleColorState);
        int colorFocused = getFocusedColor(mIdleColorState);
        int colorDisabled = getDisabledColor(mIdleColorState);
        if (background == null) {
            background = createDrawable(colorNormal);
        }

        StrokeGradientDrawable drawableDisabled = createDrawable(colorDisabled);
        StrokeGradientDrawable drawableFocused = createDrawable(colorFocused);
        StrokeGradientDrawable drawablePressed = createDrawable(colorPressed);
        mIdleStateDrawable = new StateListDrawable();

        if (drawablePressed != null) {
            mIdleStateDrawable.addState(new int[]{android.R.attr.state_pressed}, drawablePressed.getGradientDrawable());
        }
        if (drawableFocused != null) {
            mIdleStateDrawable.addState(new int[]{android.R.attr.state_focused}, drawableFocused.getGradientDrawable());
        }
        if (drawableDisabled != null) {
            mIdleStateDrawable.addState(new int[]{-android.R.attr.state_enabled}, drawableDisabled.getGradientDrawable());
        }
        mIdleStateDrawable.addState(StateSet.WILD_CARD, background.getGradientDrawable());
    }

    private int getNormalColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{android.R.attr.state_enabled}, 0);
    }

    private int getPressedColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{android.R.attr.state_pressed}, 0);
    }

    private int getFocusedColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{android.R.attr.state_focused}, 0);
    }

    private int getDisabledColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{-android.R.attr.state_enabled}, 0);
    }

    private StrokeGradientDrawable createDrawable(int color) {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.cpb_background, mContext.getTheme());
        if (drawable != null) {
            GradientDrawable gradientDrawable = (GradientDrawable) drawable.mutate();
            gradientDrawable.setColor(color);
            gradientDrawable.setCornerRadius(mCornerRadius);
            StrokeGradientDrawable strokeGradientDrawable = new StrokeGradientDrawable(gradientDrawable);
            strokeGradientDrawable.setStrokeColor(color);
            strokeGradientDrawable.setStrokeWidth(mStrokeWidth);
            return strokeGradientDrawable;
        }
        return null;
    }

    @Override
    protected void drawableStateChanged() {
        if (mState == State.COMPLETE) {
            initCompleteStateDrawable();
            setBackgroundCompat(mCompleteStateDrawable);
        } else if (mState == State.IDLE) {
            initIdleStateDrawable();
            setBackgroundCompat(mIdleStateDrawable);
        } else if (mState == State.ERROR) {
            initErrorStateDrawable();
            setBackgroundCompat(mErrorStateDrawable);
        }

        if (mState != State.PROGRESS) {
            super.drawableStateChanged();
        }
    }

    /**
     * 本来用这些代码取view的attr，但是因为代码动态设置背景颜色不生效，故只能该view直接取Activity的attr
     * int idleStateSelector = attr.getResourceId(R.styleable.CircularProgressButton_cpb_selectorIdle,
     * R.color.cpb_idle_state_selector_zjh);
     * <p>
     * int completeStateSelector = attr.getResourceId(R.styleable.CircularProgressButton_cpb_selectorComplete,
     * R.color.cpb_complete_state_selector_zjh);
     * mCompleteColorState = ResourcesCompat.getColorStateList(getResources(), completeStateSelector, mContext.getTheme());
     * <p>
     * int errorStateSelector = attr.getResourceId(R.styleable.CircularProgressButton_cpb_selectorError,
     * R.color.cpb_error_state_selector_zjh);
     * mErrorColorState = ResourcesCompat.getColorStateList(getResources(), errorStateSelector, mContext.getTheme());
     * <p>
     * mIconComplete = attr.getResourceId(R.styleable.CircularProgressButton_cpb_iconComplete, 0);
     * mIconError = attr.getResourceId(R.styleable.CircularProgressButton_cpb_iconError, 0);
     * <p>
     * mColorIndicatorBackground = attr.getColor(R.styleable.CircularProgressButton_cpb_colorIndicatorBackground, grey);
     * <p>
     * int white = getColor(R.color.cpb_white);
     * int grey = getColor(R.color.cpb_grey);
     *
     * @param context      上下文
     * @param attributeSet 属性
     */
    private void initAttributes(Context context, AttributeSet attributeSet) {
        TypedArray attr = getTypedArray(context, attributeSet, R.styleable.CircularProgressButton);
        if (attr == null) {
            return;
        }

        try {
            // 由Activity主题提供的样式：提交按钮的文字的颜色
            TypedArray confirmTextColor = mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.preview_video_button_confirm_text_color});
            int confirmTextColorDefault = ResourcesCompat.getColor(getResources(), R.color.white, mContext.getTheme());
            setTextColor(confirmTextColor.getColor(0, confirmTextColorDefault));

            // 由Activity主题提供的样式：提交按钮的背景 - 深颜色
            ColorStateList confirmBackground = mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.preview_video_button_confirm_background_color}).getColorStateList(0);
            ColorStateList confirmBackgroundDefault = ResourcesCompat.getColorStateList(getResources(), R.color.operation_background, mContext.getTheme());

            // 由Activity主题提供的样式：提交按钮的背景 - 浅颜色
            TypedArray confirmBackgroundProgress = mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.preview_video_button_confirm_background_progress_color});
            int confirmBackgroundProgressDefault = ResourcesCompat.getColor(getResources(), R.color.white, mContext.getTheme());

            mIdleColorState = confirmBackground != null ? confirmBackground : confirmBackgroundDefault;
            mCompleteColorState = confirmBackground != null ? confirmBackground : confirmBackgroundDefault;
            mErrorColorState = confirmBackground != null ? confirmBackground : confirmBackgroundDefault;

            // 由Activity主题提供的样式：提交按钮的完成时图标
            TypedArray confirmComplete = mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.preview_video_button_confirm_icon_complete});
            mIconComplete = confirmComplete.getResourceId(0, R.drawable.ic_baseline_done);

            // 由Activity主题提供的样式：提交按钮的失败时图标
            TypedArray confirmError = mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.preview_video_button_confirm_icon_error});
            mIconError = confirmError.getResourceId(0, R.drawable.ic_baseline_close_24);

            mIdleText = attr.getString(R.styleable.CircularProgressButton_cpb_textIdle);
            mCompleteText = attr.getString(R.styleable.CircularProgressButton_cpb_textComplete);
            mErrorText = attr.getString(R.styleable.CircularProgressButton_cpb_textError);
            mProgressText = attr.getString(R.styleable.CircularProgressButton_cpb_textProgress);

            mCornerRadius = attr.getDimension(R.styleable.CircularProgressButton_cpb_cornerRadius, 0);
            mPaddingProgress = attr.getDimensionPixelSize(R.styleable.CircularProgressButton_cpb_paddingProgress, 0);

            int blue = getColor(R.color.cpb_blue);

            mColorIndicator = attr.getColor(R.styleable.CircularProgressButton_cpb_colorIndicator, blue);
            // 进度时的内圆样式
            mColorProgress = confirmBackgroundProgress.getColor(0, confirmBackgroundProgressDefault);
            // 进度时的周边线样式
            mColorIndicatorBackground = getNormalColor(mIdleColorState);
        } finally {
            attr.recycle();
        }
    }

    protected int getColor(int id) {
        return ResourcesCompat.getColor(getResources(), id, mContext.getTheme());
    }

    protected TypedArray getTypedArray(Context context, AttributeSet attributeSet, int[] attr) {
        return context.obtainStyledAttributes(attributeSet, attr, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mProgress > 0 && mState == State.PROGRESS && !mMorphingInProgress) {
            if (mIndeterminateProgressMode) {
                drawIndeterminateProgress(canvas);
            } else {
                drawProgress(canvas);
            }
        }
    }

    private void drawIndeterminateProgress(Canvas canvas) {
        if (mAnimatedDrawable == null) {
            int offset = (getWidth() - getHeight()) / 2;
            mAnimatedDrawable = new CircularAnimatedDrawable(mColorIndicator, mStrokeWidth);
            int left = offset + mPaddingProgress;
            int right = getWidth() - offset - mPaddingProgress;
            int bottom = getHeight() - mPaddingProgress;
            int top = mPaddingProgress;
            mAnimatedDrawable.setBounds(left, top, right, bottom);
            mAnimatedDrawable.setCallback(this);
            mAnimatedDrawable.start();
        } else {
            mAnimatedDrawable.draw(canvas);
        }
    }

    private void drawProgress(Canvas canvas) {
        if (mProgressDrawable == null) {
            int offset = (getWidth() - getHeight()) / 2;
            int size = getHeight() - mPaddingProgress * 2;
            mProgressDrawable = new CircularProgressDrawable(size, mStrokeWidth, mColorIndicator);
            int left = offset + mPaddingProgress;
            mProgressDrawable.setBounds(left, mPaddingProgress, left, mPaddingProgress);
        }
        float sweepAngle = (360f / mMaxProgress) * mProgress;
        mProgressDrawable.setSweepAngle(sweepAngle);
        mProgressDrawable.draw(canvas);
    }

    public boolean isIndeterminateProgressMode() {
        return mIndeterminateProgressMode;
    }

    public void setIndeterminateProgressMode(boolean indeterminateProgressMode) {
        this.mIndeterminateProgressMode = indeterminateProgressMode;
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mAnimatedDrawable || super.verifyDrawable(who);
    }

    private MorphingAnimation createMorphing() {
        mMorphingInProgress = true;

        MorphingAnimation animation = new MorphingAnimation(this, background);
        animation.setFromCornerRadius(mCornerRadius);
        animation.setToCornerRadius(mCornerRadius);

        animation.setFromWidth(getWidth());
        animation.setToWidth(getWidth());

        if (mConfigurationChanged) {
            animation.setDuration(MorphingAnimation.DURATION_INSTANT);
        } else {
            animation.setDuration(MorphingAnimation.DURATION_NORMAL);
        }

        mConfigurationChanged = false;

        return animation;
    }

    private MorphingAnimation createProgressMorphing(float fromCorner, float toCorner, int fromWidth, int toWidth) {
        mMorphingInProgress = true;

        MorphingAnimation animation = new MorphingAnimation(this, background);
        animation.setFromCornerRadius(fromCorner);
        animation.setToCornerRadius(toCorner);

        animation.setPadding(mPaddingProgress);

        animation.setFromWidth(fromWidth);
        animation.setToWidth(toWidth);

        if (mConfigurationChanged) {
            animation.setDuration(MorphingAnimation.DURATION_INSTANT);
        } else {
            animation.setDuration(MorphingAnimation.DURATION_NORMAL);
        }

        mConfigurationChanged = false;

        return animation;
    }

    private void morphToProgress() {
        setWidth(getWidth());
        setText(mProgressText);

        MorphingAnimation animation = createProgressMorphing(mCornerRadius, getHeight(), getWidth(), getHeight());

        animation.setFromColor(getNormalColor(mIdleColorState));
        animation.setToColor(mColorProgress);

        animation.setFromStrokeColor(getNormalColor(mIdleColorState));
        animation.setToStrokeColor(mColorIndicatorBackground);

        animation.setListener(mProgressStateListener);

        animation.start();
    }

    private final OnAnimationEndListener mProgressStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            mMorphingInProgress = false;
            mState = State.PROGRESS;

            mStateManager.checkState(CircularProgressButton.this);
        }
    };

    private void morphProgressToComplete() {
        MorphingAnimation animation = createProgressMorphing(getHeight(), mCornerRadius, getHeight(), getWidth());

        animation.setFromColor(mColorProgress);
        animation.setToColor(getNormalColor(mCompleteColorState));

        animation.setFromStrokeColor(mColorIndicator);
        animation.setToStrokeColor(getNormalColor(mCompleteColorState));

        animation.setListener(mCompleteStateListener);

        animation.start();

    }

    private void morphIdleToComplete() {
        MorphingAnimation animation = createMorphing();

        animation.setFromColor(getNormalColor(mIdleColorState));
        animation.setToColor(getNormalColor(mCompleteColorState));

        animation.setFromStrokeColor(getNormalColor(mIdleColorState));
        animation.setToStrokeColor(getNormalColor(mCompleteColorState));

        animation.setListener(mCompleteStateListener);

        animation.start();

    }

    private final OnAnimationEndListener mCompleteStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            if (mIconComplete != 0) {
                setText(null);
                setIcon(mIconComplete);
            } else {
                setText(mCompleteText);
            }
            mMorphingInProgress = false;
            mState = State.COMPLETE;

            mStateManager.checkState(CircularProgressButton.this);
        }
    };

    private void morphCompleteToIdle() {
        MorphingAnimation animation = createMorphing();

        animation.setFromColor(getNormalColor(mCompleteColorState));
        animation.setToColor(getNormalColor(mIdleColorState));

        animation.setFromStrokeColor(getNormalColor(mCompleteColorState));
        animation.setToStrokeColor(getNormalColor(mIdleColorState));

        animation.setListener(mIdleStateListener);

        animation.start();

    }

    private void morphErrorToIdle() {
        MorphingAnimation animation = createMorphing();

        animation.setFromColor(getNormalColor(mErrorColorState));
        animation.setToColor(getNormalColor(mIdleColorState));

        animation.setFromStrokeColor(getNormalColor(mErrorColorState));
        animation.setToStrokeColor(getNormalColor(mIdleColorState));

        animation.setListener(mIdleStateListener);

        animation.start();

    }

    private final OnAnimationEndListener mIdleStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            removeIcon();
            setText(mIdleText);
            mMorphingInProgress = false;
            mState = State.IDLE;

            mStateManager.checkState(CircularProgressButton.this);
        }
    };

    private void morphIdleToError() {
        MorphingAnimation animation = createMorphing();

        animation.setFromColor(getNormalColor(mIdleColorState));
        animation.setToColor(getNormalColor(mErrorColorState));

        animation.setFromStrokeColor(getNormalColor(mIdleColorState));
        animation.setToStrokeColor(getNormalColor(mErrorColorState));

        animation.setListener(mErrorStateListener);

        animation.start();

    }

    private void morphProgressToError() {
        MorphingAnimation animation = createProgressMorphing(getHeight(), mCornerRadius, getHeight(), getWidth());

        animation.setFromColor(mColorProgress);
        animation.setToColor(getNormalColor(mErrorColorState));

        animation.setFromStrokeColor(mColorIndicator);
        animation.setToStrokeColor(getNormalColor(mErrorColorState));
        animation.setListener(mErrorStateListener);

        animation.start();
    }

    private final OnAnimationEndListener mErrorStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            if (mIconError != 0) {
                setText(null);
                setIcon(mIconError);
            } else {
                setText(mErrorText);
            }
            mMorphingInProgress = false;
            mState = State.ERROR;

            mStateManager.checkState(CircularProgressButton.this);
        }
    };

    private void morphProgressToIdle() {
        MorphingAnimation animation = createProgressMorphing(getHeight(), mCornerRadius, getHeight(), getWidth());

        animation.setFromColor(mColorProgress);
        animation.setToColor(getNormalColor(mIdleColorState));

        animation.setFromStrokeColor(mColorIndicator);
        animation.setToStrokeColor(getNormalColor(mIdleColorState));
        animation.setListener(() -> {
            removeIcon();
            setText(mIdleText);
            mMorphingInProgress = false;
            mState = State.IDLE;

            mStateManager.checkState(CircularProgressButton.this);
        });

        animation.start();
    }

    private void setIcon(int icon) {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), icon, mContext.getTheme());
        if (drawable != null) {
            int padding = (getWidth() / 2) - (drawable.getIntrinsicWidth() / 2);
            setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
            setPadding(padding, 0, 0, 0);
        }
    }

    protected void removeIcon() {
        setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        setPadding(0, 0, 0, 0);
    }

    /**
     * Set the View's background. Masks the API changes made in Jelly Bean.
     */
    public void setBackgroundCompat(Drawable drawable) {
        setBackground(drawable);
    }

    public void setProgress(int progress) {
        mProgress = progress;

        if (mMorphingInProgress || getWidth() == 0) {
            return;
        }

        mStateManager.saveProgress(this);

        if (mProgress >= mMaxProgress) {
            if (mState == State.PROGRESS) {
                morphProgressToComplete();
            } else if (mState == State.IDLE) {
                morphIdleToComplete();
            }
        } else if (mProgress > IDLE_STATE_PROGRESS) {
            if (mState == State.IDLE) {
                morphToProgress();
            } else if (mState == State.PROGRESS) {
                invalidate();
            }
        } else if (mProgress == ERROR_STATE_PROGRESS) {
            if (mState == State.PROGRESS) {
                morphProgressToError();
            } else if (mState == State.IDLE) {
                morphIdleToError();
            }
        } else if (mProgress == IDLE_STATE_PROGRESS) {
            if (mState == State.COMPLETE) {
                morphCompleteToIdle();
            } else if (mState == State.PROGRESS) {
                morphProgressToIdle();
            } else if (mState == State.ERROR) {
                morphErrorToIdle();
            }
        }
    }

    public int getProgress() {
        return mProgress;
    }

    @Override
    public void setBackgroundColor(int color) {
        background.getGradientDrawable().setColor(color);
    }

    public void setStrokeColor(int color) {
        background.setStrokeColor(color);
    }

    public String getIdleText() {
        return mIdleText;
    }

    public String getCompleteText() {
        return mCompleteText;
    }

    public String getErrorText() {
        return mErrorText;
    }

    public void setIdleText(String text) {
        mIdleText = text;
    }

    public void setCompleteText(String text) {
        mCompleteText = text;
    }

    public void setErrorText(String text) {
        mErrorText = text;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            setProgress(mProgress);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mProgress = mProgress;
        savedState.mIndeterminateProgressMode = mIndeterminateProgressMode;
        savedState.mConfigurationChanged = true;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mProgress = savedState.mProgress;
            mIndeterminateProgressMode = savedState.mIndeterminateProgressMode;
            mConfigurationChanged = savedState.mConfigurationChanged;
            super.onRestoreInstanceState(savedState.getSuperState());
            setProgress(mProgress);
        } else {
            super.onRestoreInstanceState(state);
        }
    }


    static class SavedState extends BaseSavedState {

        private boolean mIndeterminateProgressMode;
        private boolean mConfigurationChanged;
        private int mProgress;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mProgress = in.readInt();
            mIndeterminateProgressMode = in.readInt() == 1;
            mConfigurationChanged = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mProgress);
            out.writeInt(mIndeterminateProgressMode ? 1 : 0);
            out.writeInt(mConfigurationChanged ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
