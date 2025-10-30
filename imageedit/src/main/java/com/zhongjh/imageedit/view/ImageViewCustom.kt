package com.zhongjh.imageedit.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.zhongjh.imageedit.core.ImageCustom
import com.zhongjh.imageedit.core.ImageMode
import com.zhongjh.imageedit.core.ImagePen
import com.zhongjh.imageedit.core.ImageText
import com.zhongjh.imageedit.core.anim.ImageHomingAnimator
import com.zhongjh.imageedit.core.homing.ImageHoming
import com.zhongjh.imageedit.core.sticker.ImageSticker
import com.zhongjh.imageedit.core.sticker.ImageStickerPortrait

/**
 * 用于辅助编辑图片界面的显示图片控件
 *
 *
 * ImageView
 * clip外不加入path
 *
 * @author zhongjh
 * @date 2017/11/14 下午6:43
 */
class ImageViewCustom(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : FrameLayout(context, attrs, defStyleAttr), Runnable, OnScaleGestureListener,
    AnimatorUpdateListener, ImageStickerPortrait.Callback, Animator.AnimatorListener {
    private var mPreMode = ImageMode.NONE

    private val mImage = ImageCustom()

    private var mGestureDetector: GestureDetector? = null

    private var mScaleGestureDetector: ScaleGestureDetector? = null

    private var mHomingAnimator: ImageHomingAnimator? = null

    private val mPen = Pen()

    private var mPointerCount = 0

    private val mDoodlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mMosaicPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    interface Listener {
        /**
         * 双手触控屏幕的时候会重置当前模式
         */
        fun resetModel()
    }

    private var listener: Listener? = null

    /**
     * 1. 无参构造函数（供代码创建 View 时使用）
     */
    constructor(context: Context) : this(context, null, 0)

    /**
     * 2. 带 AttributeSet 的构造函数（供 XML 布局解析时使用）
     */
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        // 涂鸦画刷
        mDoodlePaint.style = Paint.Style.STROKE
        mDoodlePaint.strokeWidth = ImagePen.BASE_DOODLE_WIDTH
        mDoodlePaint.color = Color.RED
        mDoodlePaint.setPathEffect(CornerPathEffect(ImagePen.BASE_DOODLE_WIDTH))
        mDoodlePaint.strokeCap = Paint.Cap.ROUND
        mDoodlePaint.strokeJoin = Paint.Join.ROUND

        // 马赛克画刷
        mMosaicPaint.style = Paint.Style.STROKE
        mMosaicPaint.strokeWidth = ImagePen.BASE_MOSAIC_WIDTH
        mMosaicPaint.color = Color.BLACK
        mMosaicPaint.setPathEffect(CornerPathEffect(ImagePen.BASE_MOSAIC_WIDTH))
        mMosaicPaint.strokeCap = Paint.Cap.ROUND
        mMosaicPaint.strokeJoin = Paint.Join.ROUND
    }

    init {
        initialize(context)
    }

    private fun initialize(context: Context) {
        mPen.mode = mImage.mode
        // 手势监听类
        mGestureDetector = GestureDetector(context, MoveAdapter())
        // 用于处理缩放的工具类
        mScaleGestureDetector = ScaleGestureDetector(context, this)
    }

    fun setImageBitmap(image: Bitmap?) {
        mImage.setBitmap(image!!)
        invalidate()
    }

    fun addListener(listener: Listener?) {
        this.listener = listener
    }

    val isHoming: Boolean
        /**
         * 是否真正修正归位
         */
        get() {
            Log.d(TAG, "isHoming")
            return (mHomingAnimator != null
                    && mHomingAnimator!!.isRunning)
        }

    /**
     * 矫正区域
     * 假设移动图片到某个区别或者放大缩小时，改方法用于变回原样
     */
    private fun onHoming() {
        Log.d(TAG, "onHoming")
        invalidate()
        stopHoming()
        startHoming(
            mImage.getStartHoming(scrollX.toFloat(), scrollY.toFloat()),
            mImage.getEndHoming(scrollX.toFloat(), scrollY.toFloat())
        )
    }

    /**
     * 开始了归位动画
     */
    private fun startHoming(sHoming: ImageHoming, eHoming: ImageHoming) {
        Log.d(TAG, "startHoming")
        if (mHomingAnimator == null) {
            mHomingAnimator = ImageHomingAnimator()
            mHomingAnimator!!.addUpdateListener(this)
            mHomingAnimator!!.addListener(this)
        }
        mHomingAnimator!!.setHomingValues(sHoming, eHoming)
        mHomingAnimator!!.start()
    }

    /**
     * 停止当前的矫正区域动画
     */
    private fun stopHoming() {
        Log.d(TAG, "stopHoming")
        if (mHomingAnimator != null) {
            mHomingAnimator!!.cancel()
        }
    }

    fun doRotate() {
        Log.d(TAG, "doRotate")
        if (!isHoming) {
            mImage.rotate(-90)
            onHoming()
        }
    }

    fun resetClip() {
        Log.d(TAG, "resetClip")
        mImage.resetClip()
        onHoming()
    }

    fun doClip() {
        Log.d(TAG, "doClip")
        mImage.clip(scrollX.toFloat(), scrollY.toFloat())
        mode = mPreMode
        onHoming()
    }

    fun cancelClip() {
        Log.d(TAG, "cancelClip")
        mImage.toBackupClip()
        mode = mPreMode
    }

    fun setPenColor(color: Int) {
        Log.d(TAG, "setPenColor")
        mPen.color = color
    }

    fun isDoodleEmpty() : Boolean {
        Log.d(TAG, "isDoodleEmpty")
        return mImage.isDoodleEmpty
    }

    fun undoDoodle() {
        Log.d(TAG, "undoDoodle")
        mImage.undoDoodle()
        invalidate()
    }

    fun isMosaicEmpty() : Boolean {
        Log.d(TAG, "isMosaicEmpty")
        return mImage.isMosaicEmpty
    }

    fun undoMosaic() {
        Log.d(TAG, "undoMosaic")
        mImage.undoMosaic()
        invalidate()
    }

    var mode: ImageMode?
        /**
         * 获取当前模式
         *
         * @return 模式
         */
        get() {
            Log.d(TAG, "getMode")
            return mImage.mode
        }
        /**
         * 设置模式
         *
         * @param mode 模式
         */
        set(mode) {
            Log.d(TAG, "setMode")
            // 保存现在的编辑模式
            mPreMode = mImage.mode

            // 设置新的编辑模式
            mImage.mode = mode!!
            mPen.mode = mode

            // 矫正区域
            onHoming()
        }

    /**
     * 重新在IMGView画图
     */
    override fun onDraw(canvas: Canvas) {
        Log.d(TAG, "onDraw")
        onDrawImages(canvas)
    }

    /**
     * 重新在IMGView画图
     */
    private fun onDrawImages(canvas: Canvas) {
        Log.d(TAG, "onDrawImages")
        canvas.save()

        // clip 中心旋转
        val clipFrame = mImage.clipFrame
        canvas.rotate(mImage.rotate, clipFrame.centerX(), clipFrame.centerY())

        // 图片
        mImage.onDrawImage(canvas)

        // 马赛克
        val isMosaic = !mImage.isMosaicEmpty || (mImage.mode == ImageMode.MOSAIC && !mPen.isEmpty())
        if (isMosaic) {
            val count = mImage.onDrawMosaicsPath(canvas)
            if (mImage.mode == ImageMode.MOSAIC && !mPen.isEmpty()) {
                mDoodlePaint.strokeWidth = ImagePen.BASE_MOSAIC_WIDTH
                canvas.save()
                val frame = mImage.clipFrame
                canvas.rotate(-mImage.rotate, frame.centerX(), frame.centerY())
                canvas.translate(scrollX.toFloat(), scrollY.toFloat())
                canvas.drawPath(mPen.path, mDoodlePaint)
                canvas.restore()
            }
            mImage.onDrawMosaic(canvas, count)
        }

        // 涂鸦
        mImage.onDrawDoodles(canvas)
        if (mImage.mode == ImageMode.DOODLE && !mPen.isEmpty()) {
            mDoodlePaint.color = mPen.color
            mDoodlePaint.strokeWidth = ImagePen.BASE_DOODLE_WIDTH * mImage.scale
            canvas.save()
            val frame = mImage.clipFrame
            canvas.rotate(-mImage.rotate, frame.centerX(), frame.centerY())
            canvas.translate(scrollX.toFloat(), scrollY.toFloat())
            canvas.drawPath(mPen.path, mDoodlePaint)
            canvas.restore()
        }

        // TODO
        if (mImage.isFreezing()) {
            // 文字贴片
            mImage.onDrawStickers(canvas)
        }

        mImage.onDrawShade(canvas)

        canvas.restore()

        // TODO
        if (!mImage.isFreezing()) {
            // 文字贴片
            mImage.onDrawStickerClip(canvas)
            mImage.onDrawStickers(canvas)
        }

        // 裁剪
        if (mImage.mode == ImageMode.CLIP) {
            canvas.save()
            canvas.translate(scrollX.toFloat(), scrollY.toFloat())
            mImage.onDrawClip(canvas)
            canvas.restore()
        }
    }

    fun saveBitmap(): Bitmap {
        Log.d(TAG, "saveBitmap")
        mImage.stickAll()

        val scale = 1f / mImage.scale

        val frame = RectF(mImage.clipFrame)

        // 旋转基画布
        val m = Matrix()
        m.setRotate(mImage.rotate, frame.centerX(), frame.centerY())
        m.mapRect(frame)

        // 缩放基画布
        m.setScale(scale, scale, frame.left, frame.top)
        m.mapRect(frame)

        val bitmap = Bitmap.createBitmap(
            Math.round(frame.width()),
            Math.round(frame.height()), Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        // 平移到基画布原点&缩放到原尺寸
        canvas.translate(-frame.left, -frame.top)
        canvas.scale(scale, scale, frame.left, frame.top)

        onDrawImages(canvas)

        return bitmap
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        Log.d(TAG, "onLayout")
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            mImage.onLayout((right - left).toFloat(), (bottom - top).toFloat())
        }
    }

    fun <V> addStickerView(stickerView: V?, params: LayoutParams?) where V : View?, V : ImageSticker? {
        Log.d(TAG, "addStickerView")
        if (stickerView != null) {
            addView(stickerView, params)

            stickerView.registerCallback(this)
            mImage.addSticker<V>(stickerView)
        }
    }

    fun addStickerText(text: ImageText?) {
        Log.d(TAG, "addStickerText")
        val textView = ImageStickerTextView(context)

        textView.text = text

        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        // Center of the drawing window.
        layoutParams.gravity = Gravity.CENTER

        textView.x = scrollX.toFloat()
        textView.y = scrollY.toFloat()

        addStickerView(textView, layoutParams)
    }

    /**
     * 处理点击分发事件
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        Log.d(TAG, "onInterceptTouchEvent")
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            return onInterceptTouch() || super.onInterceptTouchEvent(ev)
        }
        return super.onInterceptTouchEvent(ev)
    }

    /**
     * 处理可以直接中断当前伸缩，继续按照自己意愿伸缩，增强流畅度
     */
    private fun onInterceptTouch(): Boolean {
        Log.d(TAG, "onInterceptTouch")
        if (isHoming) {
            stopHoming()
            Log.d(TAG, "onInterceptTouch true stopHoming")
            return true
        } else if (mImage.mode == ImageMode.CLIP) {
            Log.d(TAG, "onInterceptTouch true IMGMode.CLIP")
            return true
        }
        Log.d(TAG, "onInterceptTouch false")
        return false
    }

    /**
     * 处理触屏事件，里面的延迟和取消延迟也是为了伸缩图片体验性提高
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent")
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN ->                 // 取消延迟
                removeCallbacks(this)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->                 // 取消或者离开触屏延迟1.2秒
                postDelayed(this, 1200)

            else -> {}
        }
        return onTouch(event)
    }

    /**
     * 处理触屏事件.详情
     */
    fun onTouch(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouch")

        if (isHoming) {
            // Homing
            return false
        }

        mPointerCount = event.pointerCount

        var handled = mScaleGestureDetector!!.onTouchEvent(event)

        val mode = mImage.mode

        if (mode == ImageMode.NONE || mode == ImageMode.CLIP) {
            handled = handled or onTouchNone(event)
        } else if (mPointerCount > 1) {
            onPathDone()
            handled = handled or onTouchNone(event)

            // 取消涂鸦或者别的模式
            listener!!.resetModel()
            this.mode = ImageMode.NONE
        } else {
            handled = handled or onTouchPath(event)
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> mImage.onTouchDown(event.x, event.y)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mImage.onTouchUp()
                onHoming()
            }

            else -> {}
        }
        return handled
    }


    private fun onTouchNone(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchNone")
        return mGestureDetector!!.onTouchEvent(event)
    }

    /**
     * 画笔线
     */
    private fun onTouchPath(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchPath")
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                onPathBegin(event)
                return true
            }

            MotionEvent.ACTION_MOVE -> return onPathMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> return mPen.isIdentity(event.getPointerId(0)) && onPathDone()
            else -> {}
        }
        return false
    }

    /**
     * 钢笔初始化
     */
    private fun onPathBegin(event: MotionEvent) {
        Log.d(TAG, "onPathBegin")
        mPen.reset(event.x, event.y)
        mPen.setIdentity(event.getPointerId(0))
    }

    /**
     * 画线
     */
    private fun onPathMove(event: MotionEvent): Boolean {
        Log.d(TAG, "onPathMove")
        if (mPen.isIdentity(event.getPointerId(0))) {
            mPen.lineTo(event.x, event.y)
            invalidate()
            return true
        }
        return false
    }

    /**
     * 画线完成
     */
    private fun onPathDone(): Boolean {
        Log.d(TAG, "onPathDone")
        if (mPen.isEmpty()) {
            return false
        }
        mImage.addPath(mPen.toPath(), scrollX.toFloat(), scrollY.toFloat())
        mPen.reset()
        invalidate()
        return true
    }

    override fun run() {
        Log.d(TAG, "run")
        // 稳定触发
        if (!onSteady()) {
            postDelayed(this, 500)
        }
    }

    fun onSteady(): Boolean {
        if (DEBUG) {
            Log.d(TAG, "onSteady: isHoming=" + isHoming)
        }
        if (!isHoming) {
            mImage.onSteady()
            onHoming()
            return true
        }
        return false
    }

    override fun onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow")
        super.onDetachedFromWindow()
        removeCallbacks(this)
        mImage.release()
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        Log.d(TAG, "onScale")
        if (mPointerCount > 1) {
            // 当图片本身大于20倍的时候并且缩放操作要放大的时候取消缩放。缩放大于20倍的时候，返回上一次的变形，防止裁剪因为高度不大于0而导致闪退
            if (mImage.scale > SCALE_MAX && detector.scaleFactor > 1) {
                return true
            }
            mImage.onScale(
                detector.scaleFactor,
                scrollX + detector.focusX,
                scrollY + detector.focusY
            )
            invalidate()
            return true
        }
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        Log.d(TAG, "onScaleBegin")
        return mPointerCount > 1
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        Log.d(TAG, "onScaleEnd")
        mImage.onScaleEnd()
    }

    /**
     * 标记着动画的更新
     */
    override fun onAnimationUpdate(animation: ValueAnimator) {
        Log.d(TAG, "onAnimationUpdate")
        mImage.onHoming(animation.animatedFraction)
        toApplyHoming(animation.animatedValue as ImageHoming)
    }

    /**
     * 设置图片的倍率、角度、图片位置,以下三种场景用到
     * 1. 动画进行时
     * 2. 动画结束后
     * 3. 移动、拉伸图片后
     */
    private fun toApplyHoming(homing: ImageHoming) {
        Log.d(
            TAG, "toApplyHoming " +
                    "homing.scale(" + homing.scale + ")homing.rotate(" + homing.rotate + ")homing.x(" + homing.x + ")homing.y" + homing.y + ")"
        )
        mImage.scale = homing.scale
        mImage.rotate = homing.rotate
        if (!onScrollTo(Math.round(homing.x), Math.round(homing.y))) {
            invalidate()
        }
    }

    /**
     * 移动自身
     */
    private fun onScrollTo(x: Int, y: Int): Boolean {
        Log.d(TAG, "onScrollTo")
        Log.d(TAG, "onScrollTo x$x")
        Log.d(TAG, "onScrollTo y$y")
        if (scrollX != x || scrollY != y) {
            scrollTo(x, y)
            return true
        }
        return false
    }

    override fun <V> onDismiss(stickerView: V) where V : View, V : ImageSticker {
        Log.d(TAG, "onDismiss")
        mImage.onDismiss(stickerView)
        invalidate()
    }

    override fun <V> onShowing(stickerView: V) where V : View, V : ImageSticker {
        Log.d(TAG, "onShowing")
        mImage.onShowing(stickerView)
        invalidate()
    }

    override fun <V> onRemove(stickerView: V): Boolean where V : View, V : ImageSticker {
        Log.d(TAG, "onRemove")
        mImage.onRemoveSticker(stickerView)
        stickerView.unregisterCallback(this)
        val parent = stickerView.parent
        if (parent != null) {
            (parent as ViewGroup).removeView(stickerView)
        }
        return true
    }

    /**
     * 标记着动画的开始
     */
    override fun onAnimationStart(animation: Animator) {
        Log.d(TAG, "onAnimationStart")
        mImage.onHomingStart()
    }

    /**
     * 标记着动画的结束
     */
    override fun onAnimationEnd(animation: Animator) {
        Log.d(TAG, "onAnimationEnd")
        if (mImage.onHomingEnd()) {
            toApplyHoming(mImage.clip(scrollX.toFloat(), scrollY.toFloat()))
        }
    }

    override fun onAnimationCancel(animation: Animator) {
        Log.d(TAG, "onAnimationCancel")
        mImage.onHomingCancel()
    }

    override fun onAnimationRepeat(animation: Animator) {
        Log.d(TAG, "onAnimationRepeat")
        // empty implementation.
    }

    private fun onScroll(dx: Float, dy: Float): Boolean {
        Log.d(TAG, "onScroll")
        Log.d("Scroll ScrollX", scaleX.toString() + "")
        Log.d("Scroll ScrollY", scrollY.toString() + "")
        val homing = mImage.onScroll(scrollX.toFloat(), scrollY.toFloat(), -dx, -dy)
        if (homing != null) {
            toApplyHoming(homing)
            return true
        }
        return onScrollTo(scrollX + Math.round(dx), scrollY + Math.round(dy))
    }

    private inner class MoveAdapter : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            return this@ImageViewCustom.onScroll(distanceX, distanceY)
        }

    }

    /**
     * 钢笔实体
     */
    private class Pen : ImagePen() {
        /**
         * event的身份证
         */
        private var identity = Int.MIN_VALUE

        fun reset() {
            path.reset()
            this.identity = Int.MIN_VALUE
        }

        fun reset(x: Float, y: Float) {
            path.reset()
            path.moveTo(x, y)
            this.identity = Int.MIN_VALUE
        }

        fun setIdentity(identity: Int) {
            this.identity = identity
        }

        fun isIdentity(identity: Int): Boolean {
            return this.identity == identity
        }

        fun lineTo(x: Float, y: Float) {
            path.lineTo(x, y)
        }

        fun isEmpty(): Boolean {
            return path.isEmpty
        }

        fun toPath(): ImagePen {
            return ImagePen(Path(this.path), mode, color, width)
        }
    }

    companion object {
        private const val TAG = "IMGView"
        private const val SCALE_MAX = 20

        private const val DEBUG = true
    }
}
