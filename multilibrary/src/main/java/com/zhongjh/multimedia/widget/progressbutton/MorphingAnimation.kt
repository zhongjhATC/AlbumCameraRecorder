package com.zhongjh.multimedia.widget.progressbutton

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.widget.TextView

internal class MorphingAnimation(private val mView: TextView, private val mDrawable: StrokeGradientDrawable) {
    private var mListener: OnAnimationEndListener? = null

    private var mDuration = 0

    private var mFromWidth = 0
    private var mToWidth = 0

    private var mFromColor = 0
    private var mToColor = 0

    private var mFromStrokeColor = 0
    private var mToStrokeColor = 0

    private var mFromCornerRadius = 0f
    private var mToCornerRadius = 0f

    private var mPadding = 0f

    fun setDuration(duration: Int) {
        mDuration = duration
    }

    fun setListener(listener: OnAnimationEndListener?) {
        mListener = listener
    }

    fun setFromWidth(fromWidth: Int) {
        mFromWidth = fromWidth
    }

    fun setToWidth(toWidth: Int) {
        mToWidth = toWidth
    }

    fun setFromColor(fromColor: Int) {
        mFromColor = fromColor
    }

    fun setToColor(toColor: Int) {
        mToColor = toColor
    }

    fun setFromStrokeColor(fromStrokeColor: Int) {
        mFromStrokeColor = fromStrokeColor
    }

    fun setToStrokeColor(toStrokeColor: Int) {
        mToStrokeColor = toStrokeColor
    }

    fun setFromCornerRadius(fromCornerRadius: Float) {
        mFromCornerRadius = fromCornerRadius
    }

    fun setToCornerRadius(toCornerRadius: Float) {
        mToCornerRadius = toCornerRadius
    }

    fun setPadding(padding: Float) {
        mPadding = padding
    }

    fun start() {
        val widthAnimation = ValueAnimator.ofInt(mFromWidth, mToWidth)
        val gradientDrawable = mDrawable.gradientDrawable
        widthAnimation.addUpdateListener { animation: ValueAnimator ->
            val value = animation.animatedValue as Int
            val leftOffset: Int
            val rightOffset: Int
            val padding: Int

            if (mFromWidth > mToWidth) {
                leftOffset = (mFromWidth - value) / 2
                rightOffset = mFromWidth - leftOffset
                padding = (mPadding * animation.animatedFraction).toInt()
            } else {
                leftOffset = (mToWidth - value) / 2
                rightOffset = mToWidth - leftOffset
                padding = (mPadding - mPadding * animation.animatedFraction).toInt()
            }
            gradientDrawable
                .setBounds(leftOffset + padding, padding, rightOffset - padding, mView.height - padding)
        }

        val bgColorAnimation = ObjectAnimator.ofInt(gradientDrawable, "color", mFromColor, mToColor)
        bgColorAnimation.setEvaluator(ArgbEvaluator())

        val strokeColorAnimation =
            ObjectAnimator.ofInt(mDrawable, "strokeColor", mFromStrokeColor, mToStrokeColor)
        strokeColorAnimation.setEvaluator(ArgbEvaluator())

        val cornerAnimation =
            ObjectAnimator.ofFloat(gradientDrawable, "cornerRadius", mFromCornerRadius, mToCornerRadius)

        val animatorSet = AnimatorSet()
        animatorSet.setDuration(mDuration.toLong())
        animatorSet.playTogether(widthAnimation, bgColorAnimation, strokeColorAnimation, cornerAnimation)
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                mListener?.onAnimationEnd()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        animatorSet.start()
    }

    companion object {
        const val DURATION_NORMAL: Int = 400
        const val DURATION_INSTANT: Int = 1
    }
}