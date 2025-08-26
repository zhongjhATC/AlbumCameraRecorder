package com.zhongjh.imageedit.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.Log
import com.zhongjh.imageedit.core.clip.ImageClip
import com.zhongjh.imageedit.core.clip.ImageClipWindow
import com.zhongjh.imageedit.core.homing.ImageHoming
import com.zhongjh.imageedit.core.sticker.ImageSticker
import com.zhongjh.imageedit.core.util.ImageUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 图像处理核心类，负责图像的显示、编辑、裁剪、涂鸦和马赛克等操作
 * 该类是图像编辑器的核心实现，提供了图像变换、贴纸管理、涂鸦绘制、马赛克效果等功能
 *
 * @author felix
 * @date 2017/11/21 下午10:03
 */
class ImageCustom {
    /**
     * 原始图像对象，存储正在编辑的主图像
     * 所有编辑操作的基础图像数据源
     */
    private var image: Bitmap?

    /**
     * 马赛克图像对象，用于存储应用马赛克效果后的图像
     * 是原始图像经过缩放处理后的低分辨率版本，用于实现马赛克效果
     */
    private var mosaicImage: Bitmap? = null

    /**
     * 表示完整图像的边框矩形
     */
    private val frame = RectF()

    /**
     * 表示当前裁剪区域的边框矩形（显示的图片区域）
     */
    private val mClipFrame = RectF()

    /**
     * 临时裁剪边框，用于临时存储裁剪区域信息
     */
    private val mTempClipFrame = RectF()

    /**
     * 裁剪模式前状态备份
     */
    private val mBackupClipFrame = RectF()

    /**
     * 裁剪模式前的旋转角度备份，用于保存进入裁剪模式前的旋转状态
     */
    private var mBackupClipRotate = 0f

    /**
     * 当前图像旋转角度
     */
    private var mRotate = 0f

    /**
     * 动画过渡的目标旋转角度
     */
    private var mTargetRotate = 0f

    /**
     * 是否请求基础适配，标记是否需要将图像调整到基础适配状态
     */
    private var isRequestToBaseFitting = false

    /**
     * 动画是否已取消的标记
     */
    private var isAnimCanceled = false

    /**
     * 裁剪模式时当前触摸锚点，标识用户正在拖动的裁剪框锚点
     */
    private var mAnchor: ImageClip.Anchor? = null

    /**
     * 是否处于稳定状态，标识图像是否处于可编辑状态
     */
    private var isSteady = true

    /**
     * 阴影路径，用于绘制裁剪区域外的阴影效果
     */
    private val mShade = Path()

    /**
     * 裁剪窗口对象，负责处理裁剪相关功能
     */
    private val mClipWin = ImageClipWindow()

    /**
     * 当前图像编辑器的工作模式，默认为无特殊模式
     * 定义了当前图像编辑器的工作模式，如裁剪、涂鸦、马赛克、贴纸等
     */
    private var mMode = ImageMode.NONE

    /**
     * 标记图像是否处于冻结状态（不可编辑）
     */
    private var isFreezing = false

    /**
     * 表示编辑器可视窗口的矩形区域，无Scroll
     */
    private val mWindow = RectF()

    /**
     * 标记图像是否已经完成初始归位
     */
    private var isInitialHoming = false

    /**
     * 当前选中的贴纸对象（前景贴纸）
     */
    private var mForeSticker: ImageSticker? = null

    /**
     * 未被选中的贴纸集合（背景贴纸）
     */
    private val mBackStickers: MutableList<ImageSticker> = ArrayList()

    /**
     * 存储所有涂鸦路径的集合
     */
    private val mDoodles: MutableList<ImagePen> = ArrayList()

    /**
     * 存储所有马赛克路径的集合
     */
    private val mMosaics: MutableList<ImagePen> = ArrayList()

    /**
     * 通用绘制画笔，用于绘制涂鸦和其他图形
     */
    private val mPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    /**
     * 专门用于绘制马赛克效果的画笔
     */
    private var mMosaicPaint: Paint? = null

    /**
     * 用于绘制裁剪区域外阴影的画笔
     */
    private var mShadePaint: Paint? = null

    /**
     * 矩阵变换对象，用于处理图像的各种平移、缩放、旋转等变换
     */
    private val mMatrix = Matrix()

    init {
        mShade.fillType = Path.FillType.WINDING
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = ImagePen.BASE_DOODLE_WIDTH
        mPaint.setColor(Color.RED)
        mPaint.setPathEffect(CornerPathEffect(ImagePen.BASE_DOODLE_WIDTH))
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeJoin = Paint.Join.ROUND
    }

    /**
     * 构造函数，初始化ImageCustom对象
     * 设置默认图像，并根据初始编辑模式初始化相应的资源
     */
    init {
        Log.d(TAG, "IMGImage")
        // 设置默认图像为初始编辑对象
        image = DEFAULT_IMAGE

        if (mMode == ImageMode.CLIP) {
            // 如果初始模式为裁剪，初始化阴影画笔
            initShadePaint()
        }
    }

    /**
     * 设置要编辑的位图
     *
     * @param bitmap 要设置的位图对象
     */
    fun setBitmap(bitmap: Bitmap?) {
        Log.d(TAG, "setBitmap")
        if (bitmap == null || bitmap.isRecycled) {
            return
        }

        this.image = bitmap

        // 清空马赛克图层
        if (mosaicImage != null) {
            mosaicImage!!.recycle()
        }
        this.mosaicImage = null

        makeMosaicBitmap()

        onImageChanged()
    }

    var mode: ImageMode
        /**
         * 获取当前的编辑模式
         *
         * @return 当前的编辑模式枚举值
         */
        get() {
            Log.d(TAG, "getMode")
            return mMode
        }
        /**
         * 设置图像编辑模式
         *
         * @param mode 要设置的编辑模式枚举值
         */
        set(mode) {
            Log.d(TAG, "setMode")

            if (this.mMode == mode) {
                return
            }

            moveToBackground(mForeSticker)

            if (mode == ImageMode.CLIP) {
                setFreezing(true)
            }

            this.mMode = mode

            if (mMode == ImageMode.CLIP) {
                // 初始化Shade 画刷

                initShadePaint()

                // 备份裁剪前Clip 区域
                mBackupClipRotate = rotate
                mBackupClipFrame.set(mClipFrame)

                val scale = 1 / scale
                mMatrix.setTranslate(-frame.left, -frame.top)
                mMatrix.postScale(scale, scale)
                mMatrix.mapRect(mBackupClipFrame)

                // 重置裁剪区域
                mClipWin.reset(mClipFrame, targetRotate)
            } else {
                if (mMode == ImageMode.MOSAIC) {
                    makeMosaicBitmap()
                }

                mClipWin.isClipping = false
            }
        }

    /**
     * 旋转所有贴纸
     *
     * @param rotate 旋转角度
     */
    private fun rotateStickers(rotate: Float) {
        Log.d(TAG, "rotateStickers")
        mMatrix.setRotate(rotate, mClipFrame.centerX(), mClipFrame.centerY())
        for (sticker in mBackStickers) {
            mMatrix.mapRect(sticker.frame)
            sticker.rotation = sticker.rotation + rotate
            sticker.x = sticker.frame.centerX() - sticker.pivotX
            sticker.y = sticker.frame.centerY() - sticker.pivotY
        }
    }

    /**
     * 初始化阴影画笔
     * 创建并配置用于绘制裁剪区域外阴影效果的画笔
     * 设置为抗锯齿、半透明黑色、填充样式
     * 只在需要时创建画笔实例，避免不必要的资源消耗
     */
    private fun initShadePaint() {
        Log.d(TAG, "initShadePaint")
        if (mShadePaint == null) {
            // 创建带抗锯齿的画笔
            mShadePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            // 设置为半透明黑色
            mShadePaint!!.color = COLOR_SHADE
            // 设置为填充样式
            mShadePaint!!.style = Paint.Style.FILL
        }
    }

    val isMosaicEmpty: Boolean
        /**
         * 检查马赛克路径是否为空
         *
         * @return 如果没有马赛克路径则返回true，否则返回false
         */
        get() {
            Log.d(TAG, "isMosaicEmpty")
            return mMosaics.isEmpty()
        }

    val isDoodleEmpty: Boolean
        /**
         * 检查涂鸦路径是否为空
         *
         * @return 如果没有涂鸦路径则返回true，否则返回false
         */
        get() {
            Log.d(TAG, "isDoodleEmpty")
            return mDoodles.isEmpty()
        }

    /**
     * 撤销上一步涂鸦操作
     */
    fun undoDoodle() {
        Log.d(TAG, "undoDoodle")
        if (!mDoodles.isEmpty()) {
            mDoodles.removeAt(mDoodles.size - 1)
        }
    }

    /**
     * 撤销上一步马赛克操作
     */
    fun undoMosaic() {
        Log.d(TAG, "undoMosaic")
        if (!mMosaics.isEmpty()) {
            mMosaics.removeAt(mMosaics.size - 1)
        }
    }

    val clipFrame: RectF
        /**
         * 获取裁剪区域的边框
         *
         * @return 裁剪区域的RectF对象
         */
        get() {
            Log.d(TAG, "getClipFrame")
            return mClipFrame
        }

    /**
     * 裁剪区域旋转回原始角度后形成新的裁剪区域，旋转中心发生变化，
     * 因此需要将视图窗口平移到新的旋转中心位置。
     *
     * @param scrollX X轴滚动偏移量
     * @param scrollY Y轴滚动偏移量
     * @return 包含新的滚动位置、缩放和旋转信息的ImageHoming对象
     */
    fun clip(scrollX: Float, scrollY: Float): ImageHoming {
        Log.d(TAG, "clip")
        val frame = mClipWin.getOffsetFrame(scrollX, scrollY)

        mMatrix.setRotate(-rotate, mClipFrame.centerX(), mClipFrame.centerY())
        mMatrix.mapRect(mClipFrame, frame)

        return ImageHoming(
            scrollX + (mClipFrame.centerX() - frame.centerX()),
            scrollY + (mClipFrame.centerY() - frame.centerY()),
            scale, rotate
        )
    }

    /**
     * 恢复到裁剪前的状态
     */
    fun toBackupClip() {
        Log.d(TAG, "toBackupClip")
        mMatrix.setScale(scale, scale)
        mMatrix.postTranslate(frame.left, frame.top)
        mMatrix.mapRect(mClipFrame, mBackupClipFrame)
        targetRotate = mBackupClipRotate
        isRequestToBaseFitting = true
    }

    /**
     * 重置裁剪区域
     */
    fun resetClip() {
        Log.d(TAG, "resetClip")
        // TODO 就近旋转
        targetRotate = rotate - rotate % 360
        mClipFrame.set(frame)
        mClipWin.reset(mClipFrame, targetRotate)
    }

    /**
     * 创建马赛克图和马赛克画笔
     * 该方法会创建一个低分辨率的图像版本用于实现马赛克效果，并初始化相应的绘制画笔
     * 马赛克效果的原理是使用低分辨率图像覆盖在原图像上，通过绘制放大后的低分辨率图像产生马赛克效果
     */
    private fun makeMosaicBitmap() {
        Log.d(TAG, "makeMosaicBitmap")
        if (mosaicImage != null || image == null) {
            return  // 如果马赛克图像已存在或原图为空，则直接返回
        }

        if (mMode == ImageMode.MOSAIC) {
            // 计算马赛克图像的尺寸（原图宽高的1/64）
            var w = Math.round(image!!.width / 64f)
            var h = Math.round(image!!.height / 64f)

            // 确保马赛克图像尺寸不小于8x8像素
            w = max(w.toDouble(), 8.0).toInt()
            h = max(h.toDouble(), 8.0).toInt()

            // 初始化马赛克画笔
            if (mMosaicPaint == null) {
                mMosaicPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                // 禁用位图过滤，增强马赛克效果
                mMosaicPaint!!.isFilterBitmap = false
                // 设置混合模式
                mMosaicPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
            }

            // 创建低分辨率的马赛克图像
            mosaicImage = Bitmap.createScaledBitmap(image!!, w, h, false)
        }
    }

    /**
     * 当图像发生变化时调用的方法
     */
    private fun onImageChanged() {
        Log.d(TAG, "onImageChanged")
        isInitialHoming = false
        onLayout(mWindow.width(), mWindow.height())

        if (mMode == ImageMode.CLIP) {
            mClipWin.reset(mClipFrame, targetRotate)
        }
    }

    /**
     * 执行裁剪区域的归位操作
     *
     * @return 归位操作是否成功
     * @noinspection UnusedReturnValue
     */
    fun onClipHoming(): Boolean {
        Log.d(TAG, "onClipHoming")
        return mClipWin.homing()
    }

    /**
     * 获取起始归位信息
     *
     * @param scrollX X轴滚动偏移量
     * @param scrollY Y轴滚动偏移量
     * @return 包含滚动位置、缩放和旋转信息的ImageHoming对象
     */
    fun getStartHoming(scrollX: Float, scrollY: Float): ImageHoming {
        Log.d(TAG, "getStartHoming: scrollX(" + scrollX + ") scrollY(" + scrollY + ") getScale(" + scale + ") getRotate(" + rotate + ")")
        return ImageHoming(scrollX, scrollY, scale, rotate)
    }

    /**
     * 获取结束归位信息
     *
     * @param scrollX X轴滚动偏移量
     * @param scrollY Y轴滚动偏移量
     * @return 包含最终滚动位置、缩放和旋转信息的ImageHoming对象
     */
    fun getEndHoming(scrollX: Float, scrollY: Float): ImageHoming {
        val homing = ImageHoming(scrollX, scrollY, scale, targetRotate)
        Log.d(TAG, "getEndHoming: homing.x(" + homing.x + ") homing.y(" + homing.y + ") homing.scale(" + homing.scale + ") homing.rotate(" + homing.rotate + ")")
        if (mMode == ImageMode.CLIP) {
            val frame = RectF(mClipWin.targetFrame)
            frame.offset(scrollX, scrollY)
            if (mClipWin.isResetting) {
                val clipFrame = RectF()
                mMatrix.setRotate(targetRotate, mClipFrame.centerX(), mClipFrame.centerY())
                mMatrix.mapRect(clipFrame, mClipFrame)

                homing.rConcat(ImageUtils.fill(frame, clipFrame))
            } else {
                val cFrame = RectF()

                // cFrame要是一个暂时clipFrame
                if (mClipWin.isHoming) {
                    mMatrix.setRotate(targetRotate - rotate, mClipFrame.centerX(), mClipFrame.centerY())
                    mMatrix.mapRect(cFrame, mClipWin.getOffsetFrame(scrollX, scrollY))

                    homing.rConcat(ImageUtils.fitHoming(frame, cFrame, mClipFrame.centerX(), mClipFrame.centerY()))
                } else {
                    mMatrix.setRotate(targetRotate, mClipFrame.centerX(), mClipFrame.centerY())
                    mMatrix.mapRect(cFrame, this.frame)
                    homing.rConcat(ImageUtils.fillHoming(frame, cFrame, mClipFrame.centerX(), mClipFrame.centerY()))
                }
            }
        } else {
            val clipFrame = RectF()
            mMatrix.setRotate(targetRotate, mClipFrame.centerX(), mClipFrame.centerY())
            mMatrix.mapRect(clipFrame, mClipFrame)

            val win = RectF(mWindow)
            win.offset(scrollX, scrollY)
            homing.rConcat(ImageUtils.fitHoming(win, clipFrame, isRequestToBaseFitting))
            isRequestToBaseFitting = false
        }

        Log.d(TAG, "getEndHoming: homing.x(" + homing.x + ") homing.y(" + homing.y + ") homing.scale(" + homing.scale + ") homing.rotate(" + homing.rotate + ")")
        return homing
    }

    /**
     * 添加贴纸到图片上
     *
     * @param sticker 要添加的贴纸对象
     * @param <S>     贴纸的类型，必须是ImageSticker的子类
    </S> */
    fun <S : ImageSticker?> addSticker(sticker: S?) {
        Log.d(TAG, "addSticker")
        if (sticker != null) {
            moveToForeground(sticker)
        }
    }

    /**
     * 添加路径到图片上（涂鸦或马赛克）
     * 该方法将用户绘制的路径转换为图像坐标系中的路径，并根据路径类型（涂鸦或马赛克）添加到相应的集合中
     * 转换过程详解：
     * 1. M.setTranslate(sx, sy) - 将矩阵平移到与视图的XY轴对齐（使用getScrollX()和getScrollY()值）
     * 2. M.postTranslate(-mFrame.left, -mFrame.top) - 调整坐标系统，确保绘制不会超出图像边界
     * 3. M.postScale(scale, scale) - 应用缩放变换，确保路径与图像的缩放比例一致
     *
     * @param path 要添加的路径对象（ImagePen类型），包含绘制路径的所有信息（点序列、颜色、宽度等）
     * @param sx X轴滚动偏移量
     * @param sy Y轴滚动偏移量
     */
    fun addPath(path: ImagePen?, sx: Float, sy: Float) {
        if (path == null) {
            return
        }

        val scale = 1f / scale
        Log.d(TAG, "addPath getScale()" + this.scale)
        Log.d(TAG, "addPath scale$scale")
        mMatrix.setTranslate(sx, sy)
        Log.d(TAG, "addPath sx$sx")
        Log.d(TAG, "addPath sy$sy")
        mMatrix.postRotate(-rotate, mClipFrame.centerX(), mClipFrame.centerY())
        Log.d(TAG, "addPath -getRotate()" + -rotate)
        Log.d(TAG, "addPath mClipFrame.centerX()" + mClipFrame.centerX())
        Log.d(TAG, "addPath mClipFrame.centerY()" + mClipFrame.centerY())
        mMatrix.postTranslate(-frame.left, -frame.top)
        Log.d(TAG, "addPath -mFrame.left" + -frame.left)
        Log.d(TAG, "addPath -mFrame.top" + -frame.top)
        mMatrix.postScale(scale, scale)
        Log.d(TAG, "addPath scale$scale")
        // 矩阵变换
        path.transform(mMatrix)

        when (path.mode) {
            ImageMode.DOODLE -> mDoodles.add(path) // 涂鸦模式下添加到涂鸦路径集合
            ImageMode.MOSAIC -> {
                path.width = path.width * scale
                mMosaics.add(path) // 马赛克模式下添加到马赛克路径集合
            }

            else -> {}
        }
    }

    /**
     * 将贴纸移到前景（选中状态）
     *
     * @param sticker 要移动的贴纸对象
     */
    private fun moveToForeground(sticker: ImageSticker?) {
        Log.d(TAG, "moveToForeground")
        if (sticker == null) {
            return
        }

        moveToBackground(mForeSticker)

        if (sticker.isShowing) {
            mForeSticker = sticker
            // 从BackStickers中移除
            mBackStickers.remove(sticker)
        } else {
            sticker.show()
        }
    }

    /**
     * 将贴纸移到背景（未选中状态）
     *
     * @param sticker 要移动的贴纸对象
     */
    private fun moveToBackground(sticker: ImageSticker?) {
        Log.d(TAG, "moveToBackground")
        if (sticker == null) {
            return
        }

        if (!sticker.isShowing) {
            // 加入BackStickers中
            if (!mBackStickers.contains(sticker)) {
                mBackStickers.add(sticker)
            }

            if (mForeSticker === sticker) {
                mForeSticker = null
            }
        } else {
            sticker.dismiss()
        }
    }

    /**
     * 将所有贴纸固定（取消选中状态）
     */
    fun stickAll() {
        Log.d(TAG, "stickAll")
        moveToBackground(mForeSticker)
    }

    /**
     * 当贴纸被隐藏时调用
     *
     * @param sticker 被隐藏的贴纸对象
     */
    fun onDismiss(sticker: ImageSticker?) {
        Log.d(TAG, "onDismiss")
        moveToBackground(sticker)
    }

    /**
     * 当贴纸显示时调用
     *
     * @param sticker 显示的贴纸对象
     */
    fun onShowing(sticker: ImageSticker) {
        Log.d(TAG, "onShowing")
        if (mForeSticker !== sticker) {
            moveToForeground(sticker)
        }
    }

    /**
     * 移除贴纸
     *
     * @param sticker 要移除的贴纸对象
     */
    fun onRemoveSticker(sticker: ImageSticker) {
        Log.d(TAG, "onRemoveSticker")
        if (mForeSticker === sticker) {
            mForeSticker = null
        } else {
            mBackStickers.remove(sticker)
        }
    }

    /**
     * 布局图像和视图
     *
     * @param width  视图宽度
     * @param height 视图高度
     */
    fun onLayout(width: Float, height: Float) {
        Log.d(TAG, "onLayout")
        if (width == 0f || height == 0f) {
            return
        }

        mWindow[0f, 0f, width] = height

        if (!isInitialHoming) {
            onInitialHoming(width, height)
        } else {
            // Pivot to fit window.
            mMatrix.setTranslate(mWindow.centerX() - mClipFrame.centerX(), mWindow.centerY() - mClipFrame.centerY())
            mMatrix.mapRect(frame)
            mMatrix.mapRect(mClipFrame)
        }

        mClipWin.setClipWinSize(width, height)
    }

    /**
     * 执行初始归位操作
     *
     * @param width  视图宽度
     * @param height 视图高度
     */
    private fun onInitialHoming(width: Float, height: Float) {
        Log.d(TAG, "onInitialHoming")
        frame[0f, 0f, image!!.width.toFloat()] = image!!.height.toFloat()
        mClipFrame.set(frame)
        mClipWin.setClipWinSize(width, height)

        if (mClipFrame.isEmpty) {
            return
        }

        toBaseHoming()

        isInitialHoming = true
        onInitialHomingDone()
    }

    /**
     * 执行基础归位操作，将图像缩放到适合窗口的大小
     * 此方法是图像编辑初始化时的关键步骤，负责计算合适的缩放比例，确保图像能够完全显示在编辑窗口内
     * 归位过程会计算窗口和裁剪框的宽高比，选择最小的缩放比例来保证图像完全可见
     */
    private fun toBaseHoming() {
        Log.d(TAG, "toBaseHoming")
        if (mClipFrame.isEmpty) {
            // 裁剪框为空，无法执行归位操作（通常是位图无效导致）
            return
        }

        // 计算合适的缩放比例：选择窗口宽度与裁剪框宽度的比值和窗口高度与裁剪框高度的比值中的较小值
        // 这样可以确保整个裁剪框内容都能显示在窗口内
        val scale = min(
            (mWindow.width() / mClipFrame.width()).toDouble(),
            (mWindow.height() / mClipFrame.height()).toDouble()
        ).toFloat()

        // Scale to fit window.
        mMatrix.setScale(scale, scale, mClipFrame.centerX(), mClipFrame.centerY())
        mMatrix.postTranslate(mWindow.centerX() - mClipFrame.centerX(), mWindow.centerY() - mClipFrame.centerY())
        mMatrix.mapRect(frame)
        mMatrix.mapRect(mClipFrame)
    }

    /**
     * 当初始归位完成时调用
     */
    private fun onInitialHomingDone() {
        Log.d(TAG, "onInitialHomingDone")
        if (mMode == ImageMode.CLIP) {
            mClipWin.reset(mClipFrame, targetRotate)
        }
    }

    /**
     * 绘制图像到画布上
     * 该方法负责将主图像绘制到指定的画布上，并根据当前编辑模式进行相应的裁剪
     *
     * @param canvas 目标画布对象
     */
    fun onDrawImage(canvas: Canvas) {
        Log.d(TAG, "onDrawImage")

        // 根据当前模式确定裁剪区域：如果正在裁剪，则使用完整图像边框；否则使用裁剪边框
        canvas.clipRect(if (mClipWin.isClipping) frame else mClipFrame)

        // 绘制主图像到指定区域
        canvas.drawBitmap(image!!, null, frame, null)

        if (DEBUG) {
            // 调试模式下，绘制边框以便于查看
            mPaint!!.color = Color.RED
            mPaint.strokeWidth = 6f
            canvas.drawRect(frame, mPaint) // 绘制完整图像边框
            canvas.drawRect(mClipFrame, mPaint) // 绘制裁剪区域边框
        }
    }

    /**
     * 绘制马赛克路径
     * 该方法在画布上保存一个新的图层，并在该图层上绘制所有马赛克路径
     *
     * @param canvas 目标画布对象
     * @return 图层计数，用于后续恢复画布状态
     */
    fun onDrawMosaicsPath(canvas: Canvas): Int {
        Log.d(TAG, "onDrawMosaicsPath")
        // 保存当前图层，创建一个新的图层用于绘制马赛克
        val layerCount = canvas.saveLayer(frame, null)

        if (!isMosaicEmpty) { // 如果存在马赛克路径
            canvas.save() // 保存当前画布状态
            val scale = scale // 获取当前图像缩放比例
            canvas.translate(frame.left, frame.top) // 平移到图像位置
            canvas.scale(scale, scale) // 应用缩放变换
            // 绘制所有马赛克路径
            for (path in mMosaics) {
                path.onDrawMosaic(canvas, mPaint)
            }
            canvas.restore() // 恢复画布状态
        }

        return layerCount // 返回图层计数，供后续恢复使用
    }

    /**
     * 绘制马赛克效果
     * 该方法使用之前保存的图层和马赛克图像，实现马赛克效果的最终呈现
     *
     * @param canvas 目标画布对象
     * @param layerCount 之前保存的图层计数
     */
    fun onDrawMosaic(canvas: Canvas, layerCount: Int) {
        Log.d(TAG, "onDrawMosaic")
        // 在保存的图层上绘制马赛克图像，利用混合模式实现马赛克效果
        canvas.drawBitmap(mosaicImage!!, null, frame, mMosaicPaint)
        // 恢复到之前保存的图层状态
        canvas.restoreToCount(layerCount)
    }

    /**
     * 绘制所有涂鸦到画布上
     *
     * @param canvas 目标画布对象
     */
    fun onDrawDoodles(canvas: Canvas) {
        Log.d(TAG, "onDrawDoodles")
        if (!isDoodleEmpty) {
            canvas.save()
            val scale = scale
            canvas.translate(frame.left, frame.top)
            canvas.scale(scale, scale)
            for (path in mDoodles) {
                path.onDrawDoodle(canvas, mPaint)
            }
            canvas.restore()
        }
    }

    /**
     * 绘制贴纸裁剪区域
     * 该方法在贴纸绘制前设置裁剪区域，确保贴纸只绘制在图像可见区域内
     * 当图像发生旋转时，需要对裁剪区域进行相应的旋转变换，以保持正确的显示效果
     *
     * @param canvas 目标画布对象
     */
    fun onDrawStickerClip(canvas: Canvas) {
        Log.d(TAG, "onDrawStickerClip")
        // 设置旋转矩阵，以裁剪框中心为旋转中心
        mMatrix.setRotate(rotate, mClipFrame.centerX(), mClipFrame.centerY())
        // 根据当前是否处于裁剪模式，选择不同的裁剪区域并应用旋转变换
        mMatrix.mapRect(mTempClipFrame, if (mClipWin.isClipping) frame else mClipFrame)
        // 设置画布裁剪区域，确保贴纸只绘制在指定区域内
        canvas.clipRect(mTempClipFrame)
    }

    /**
     * 绘制所有贴纸到画布上
     *
     * @param canvas 目标画布对象
     */
    fun onDrawStickers(canvas: Canvas) {
        Log.d(TAG, "onDrawStickers")
        if (mBackStickers.isEmpty()) {
            return
        }
        canvas.save()
        for (sticker in mBackStickers) {
            if (!sticker.isShowing) {
                val tPivotX = sticker.x + sticker.pivotX
                val tPivotY = sticker.y + sticker.pivotY

                canvas.save()
                mMatrix.setTranslate(sticker.x, sticker.y)
                mMatrix.postScale(sticker.scale, sticker.scale, tPivotX, tPivotY)
                mMatrix.postRotate(sticker.rotation, tPivotX, tPivotY)

                canvas.concat(mMatrix)
                sticker.onSticker(canvas)
                canvas.restore()
            }
        }
        canvas.restore()
    }

    /**
     * 绘制裁剪区域的阴影
     * 该方法在裁剪模式下且图像处于稳定状态时，在裁剪区域外绘制半透明阴影效果，突出显示裁剪区域
     * 阴影实现通过创建一个大矩形（覆盖整个图像）减去裁剪区域矩形的复合路径来实现
     *
     * @param canvas 目标画布对象
     */
    fun onDrawShade(canvas: Canvas) {
        Log.d(TAG, "onDrawShade")
        // 仅在裁剪模式且图像处于稳定状态时绘制阴影
        if (mMode == ImageMode.CLIP && isSteady) {
            mShade.reset() // 重置阴影路径
            // 添加一个稍大于图像边界的矩形（顺时针方向）
            mShade.addRect(frame.left - 2, frame.top - 2, frame.right + 2, frame.bottom + 2, Path.Direction.CW)
            // 添加裁剪区域矩形（逆时针方向），与前一个矩形形成差集
            mShade.addRect(mClipFrame, Path.Direction.CCW)
            // 使用阴影画笔绘制差集区域，形成阴影效果
            canvas.drawPath(mShade, mShadePaint!!)
        }
    }

    /**
     * 绘制裁剪窗口
     *
     * @param canvas 目标画布对象
     */
    fun onDrawClip(canvas: Canvas?) {
        Log.d(TAG, "onDrawClip")
        if (mMode == ImageMode.CLIP) {
            mClipWin.onDraw(canvas)
        }
    }

    /**
     * 处理触摸按下事件
     * 当用户触摸屏幕时调用，标记图像进入非稳定状态，并处理裁剪模式下的锚点选择
     *
     * @param x 触摸点的X坐标
     * @param y 触摸点的Y坐标
     */
    fun onTouchDown(x: Float, y: Float) {
        Log.d(TAG, "onTouchDown")
        isSteady = false // 标记图像进入非稳定状态
        moveToBackground(mForeSticker) // 取消当前选中的贴纸
        if (mMode == ImageMode.CLIP) {
            mAnchor = mClipWin.getAnchor(x, y) // 在裁剪模式下，确定用户触摸的是哪个锚点
        }
    }

    /**
     * 处理触摸抬起事件
     * 当用户手指离开屏幕时调用，清除锚点选择
     */
    fun onTouchUp() {
        Log.d(TAG, "onTouchUp")
        if (mAnchor != null) {
            mAnchor = null // 清除锚点选择
        }
    }

    /**
     * 当图像进入稳定状态时调用
     */
    fun onSteady() {
        Log.d(TAG, "onSteady")
        isSteady = true
        onClipHoming()
        mClipWin.isShowShade = true
    }

    /**
     * 处理滚动事件
     * 该方法处理用户的滚动操作，根据当前编辑模式执行相应的处理逻辑
     *
     * @param scrollX X轴滚动偏移量
     * @param scrollY Y轴滚动偏移量
     * @param dx      X轴滚动增量
     * @param dy      Y轴滚动增量
     * @return 包含滚动后位置信息的ImageHoming对象，如果不需要滚动则返回null
     */
    fun onScroll(scrollX: Float, scrollY: Float, dx: Float, dy: Float): ImageHoming? {
        Log.d(TAG, "onScroll")
        if (mMode == ImageMode.CLIP) {
            mClipWin.isShowShade = false // 滚动时隐藏阴影效果
            if (mAnchor != null) { // 如果用户正在拖动裁剪框的锚点
                mClipWin.onScroll(mAnchor, dx, dy) // 处理锚点的滚动

                // 计算旋转后的裁剪区域
                val clipFrame = RectF()
                mMatrix.setRotate(rotate, mClipFrame.centerX(), mClipFrame.centerY())
                mMatrix.mapRect(clipFrame, frame)

                // 获取偏移后的裁剪窗口并计算归位信息
                val frame = mClipWin.getOffsetFrame(scrollX, scrollY)
                val homing = ImageHoming(scrollX, scrollY, scale, targetRotate)
                homing.rConcat(ImageUtils.fillHoming(frame, clipFrame, mClipFrame.centerX(), mClipFrame.centerY()))
                return homing
            }
        }
        return null
    }

    var targetRotate: Float
        /**
         * 获取目标旋转角度
         *
         * @return 目标旋转角度
         */
        get() {
            Log.d(TAG, "getTargetRotate")
            return mTargetRotate
        }
        /**
         * 设置目标旋转角度
         *
         * @param targetRotate 目标旋转角度
         */
        set(targetRotate) {
            Log.d(TAG, "setTargetRotate")
            this.mTargetRotate = targetRotate
        }

    /**
     * 在当前基础上旋转图像
     *
     * @param rotate 旋转角度增量
     */
    fun rotate(rotate: Int) {
        Log.d(TAG, "rotate")
        mTargetRotate = (Math.round((mRotate + rotate) / 90f) * 90).toFloat()
        mClipWin.reset(mClipFrame, targetRotate)
    }

    var rotate: Float
        /**
         * 获取当前旋转角度
         *
         * @return 当前旋转角度
         */
        get() {
            Log.d(TAG, "getRotate")
            return mRotate
        }
        /**
         * 设置当前旋转角度
         *
         * @param rotate 当前旋转角度
         */
        set(rotate) {
            Log.d(TAG, "setRotate")
            mRotate = rotate
        }

    var scale: Float
        /**
         * 获取当前缩放比例
         *
         * @return 当前缩放比例，计算公式为：view缩放后的宽度 / 图片固定宽度
         */
        get() {
            Log.d(TAG, "getScale")
            return frame.width() / image!!.width
        }
        /**
         * 设置缩放比例（以裁剪框中心为焦点）
         *
         * @param scale 目标缩放比例
         */
        set(scale) {
            Log.d(TAG, "setScale")
            setScale(scale, mClipFrame.centerX(), mClipFrame.centerY())
        }

    /**
     * 设置缩放比例（以指定焦点为中心）
     *
     * @param scale  目标缩放比例
     * @param focusX 缩放焦点的X坐标
     * @param focusY 缩放焦点的Y坐标
     */
    fun setScale(scale: Float, focusX: Float, focusY: Float) {
        Log.d(TAG, "setScale")
        onScale(scale / this.scale, focusX, focusY)
    }

    /**
     * 执行缩放操作
     * 该方法实现图像的缩放变换，同时也会对贴纸等附加元素应用相同的缩放效果
     *
     * @param factor 缩放因子
     * @param focusX 缩放焦点的X坐标
     * @param focusY 缩放焦点的Y坐标
     */
    fun onScale(factor: Float, focusX: Float, focusY: Float) {
        var factor = factor
        Log.d(TAG, "onScale")

        if (abs(factor.toDouble()) == abs(SCALE_MAX.toDouble())) {
            return  // 达到最大缩放限制，停止缩放
        }

        // 当图像尺寸接近最大或最小值时，减小缩放因子以避免过度缩放
        if (max(mClipFrame.width().toDouble(), mClipFrame.height().toDouble()) >= MAX_SIZE
            || min(mClipFrame.width().toDouble(), mClipFrame.height().toDouble()) <= MIN_SIZE
        ) {
            factor += (1 - factor) / 2
        }

        // 创建缩放变换矩阵并应用到图像边框和裁剪区域
        mMatrix.setScale(factor, factor, focusX, focusY)
        mMatrix.mapRect(frame)
        mMatrix.mapRect(mClipFrame)

        // 对所有贴纸应用相同的缩放变换
        for (sticker in mBackStickers) {
            mMatrix.mapRect(sticker.frame) // 变换贴纸边框
            val tPivotX = sticker.x + sticker.pivotX
            val tPivotY = sticker.y + sticker.pivotY
            sticker.addScale(factor) // 更新贴纸自身的缩放因子
            // 调整贴纸位置以保持相对于图像的正确位置
            sticker.x = sticker.x + sticker.frame.centerX() - tPivotX
            sticker.y = sticker.y + sticker.frame.centerY() - tPivotY
        }
    }

    /**
     * 缩放操作结束时调用
     */
    fun onScaleEnd() {
        Log.d(TAG, "onScaleEnd")
    }

    /**
     * 归位动画开始时调用
     */
    fun onHomingStart() {
        Log.d(TAG, "onHomingStart")
        isAnimCanceled = false
    }

    /**
     * 执行归位动画的每一步
     *
     * @param fraction 动画进度，范围为0到1
     */
    fun onHoming(fraction: Float) {
        Log.d(TAG, "onHoming")
        mClipWin.homing(fraction)
    }

    /**
     * 归位动画结束时调用
     *
     *
     * 该方法在归位动画完成后被调用，根据当前编辑模式执行不同的操作：
     * - 裁剪模式下：完成裁剪操作，设置裁剪窗口状态
     * - 其他模式下：如果之前冻结了图像，则解冻图像
     *
     * @return 如果成功进行了裁剪操作则返回true，否则返回false
     */
    fun onHomingEnd(): Boolean {
        Log.d(TAG, "onHomingEnd")
        if (mMode == ImageMode.CLIP) {
            // 裁剪模式下，检查动画是否被取消
            val clip = !isAnimCanceled

            // 更新裁剪窗口状态：结束归位动画、开启裁剪模式、取消重置状态
            mClipWin.isHoming = false
            mClipWin.isClipping = true
            mClipWin.isResetting = false

            return clip
        } else {
            // 非裁剪模式下，如果图像处于冻结状态且动画未被取消，则解冻图像
            if (isFreezing && !isAnimCanceled) {
                setFreezing(false)
            }
        }
        return false
    }

    /**
     * 检查图像是否处于冻结状态
     * 冻结状态下，贴纸会保持水平方向，不受图像旋转的影响
     *
     * @return 如果图像处于冻结状态则返回true，否则返回false
     */
    fun isFreezing(): Boolean {
        Log.d(TAG, "isFreezing")
        return isFreezing
    }

    /**
     * 设置图像是否冻结
     *
     *
     * 冻结状态用于在图像旋转时保持贴纸方向不变，当图像解冻时，贴纸会被旋转到与图像匹配的角度
     *
     * @param freezing 是否冻结图像
     */
    private fun setFreezing(freezing: Boolean) {
        Log.d(TAG, "setFreezing")
        if (freezing != isFreezing) {
            // 根据冻结状态调整贴纸旋转角度：
            // - 冻结时：旋转贴纸以抵消当前图像旋转角度，保持贴纸水平显示
            // - 解冻时：旋转贴纸到目标旋转角度，使其与图像方向一致
            rotateStickers(if (freezing) -rotate else targetRotate)
            isFreezing = freezing
        }
    }

    /**
     * 取消归位动画
     */
    fun onHomingCancel() {
        Log.d(TAG, "onHomingCancel")
        isAnimCanceled = true
        Log.d(TAG, "Homing cancel")
    }

    /**
     * 释放资源
     * 该方法负责释放图像编辑器使用的主要资源，特别是位图资源，防止内存泄漏
     */
    fun release() {
        Log.d(TAG, "release")
        // 释放主图像资源（如果存在且未被回收）
        if (image != null && !image!!.isRecycled) {
            image!!.recycle()
        }
    }

    /**
     * 对象被回收时调用的方法，释放默认图像资源
     * 在对象被垃圾回收器回收之前调用，确保所有资源都被正确释放
     *
     * @throws Throwable 抛出任何可能的异常
     */
    fun onDestroy() {
        Log.d(TAG, "finalize")
        // 释放静态的默认图像资源
        DEFAULT_IMAGE?.recycle()
    }

    companion object {
        // 定义ImageCustom类，作为图像编辑器的核心实现类
        /**
         * 日志标签常量，用于标识该类的日志输出
         */
        private const val TAG = "IMGImage"

        /**
         * 最大缩放比例限制，用于限制图像的最大放大倍数
         */
        private const val SCALE_MAX = 1f

        /**
         * 最小尺寸限制，用于限制图像的最小显示尺寸
         */
        private const val MIN_SIZE = 500

        /**
         * 最大尺寸限制，用于限制图像的最大处理尺寸
         */
        private const val MAX_SIZE = 10000

        /**
         * 调试模式标志，用于控制调试信息的输出
         */
        private const val DEBUG = false

        /**
         * 默认图像对象
         * 当没有提供图像时使用的占位图像
         * 是一个静态对象，确保在任何ImageCustom实例创建之前初始化
         */
        private var DEFAULT_IMAGE: Bitmap? = null

        /**
         * 阴影颜色值
         * 用于绘制裁剪区域外的半透明黑色阴影
         * 0xCC000000表示透明度为80%的黑色
         */
        private const val COLOR_SHADE = -0x34000000

        /**
         * 静态初始化块，初始化默认图像对象
         * 当没有提供图像时，创建一个100x100像素的ARGB_8888格式位图作为默认图像
         * 确保在任何ImageCustom实例创建之前初始化
         */
        init {
            DEFAULT_IMAGE = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
    }
}
