package com.zhongjh.imageedit.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.zhongjh.imageedit.ImageTextEditDialog
import com.zhongjh.imageedit.core.ImageText

/**
 * 图像文本贴纸视图组件，用于在图像上显示和编辑文本贴纸
 * 支持文本内容、颜色的设置和编辑
 *
 * @param context 上下文对象
 * @param attrs XML属性集合
 * @param defStyleAttr 默认样式属性
 *
 * @author zhongjh
 * @date 2025/10/28
 */
class ImageStickerTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    BaseImageStickerView(context, attrs, defStyleAttr), ImageTextEditDialog.Callback {

    /**
     * 用于显示文本内容的TextView组件
     */
    private var mTextView: TextView? = null

    /**
     * 存储文本内容和样式的ImageText对象
     */
    private var mText: ImageText? = null

    /**
     * 用于编辑文本内容和样式的对话框（懒加载初始化）
     */
    private val mDialog: ImageTextEditDialog by lazy {
        ImageTextEditDialog(context, this)
    }

    /**
     * 初始化视图组件
     * 计算基础文本大小并调用父类初始化方法
     *
     * @param context 上下文对象
     */
    override fun onInitialize(context: Context) {
        if (mBaseTextSize <= 0) {
            // 将sp单位转换为像素单位
            mBaseTextSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                TEXT_SIZE_SP,
                context.resources.displayMetrics
            )
        }
        super.onInitialize(context)
    }

    /**
     * 创建内容视图
     * 初始化文本显示组件并设置默认样式
     *
     * @param context 上下文对象
     * @return 创建的文本视图
     */
    override fun onCreateContentView(context: Context): View {
        return TextView(context).apply {
            textSize = mBaseTextSize
            setPadding(PADDING, PADDING, PADDING, PADDING)
            setTextColor(Color.WHITE)
            mTextView = this // 赋值给成员变量
        }
    }

    /**
     * 文本内容和样式的属性访问器
     */
    var text: ImageText?
        get() = mText
        set(value) {
            mText = value
            mTextView?.let { textView ->
                value?.let {
                    textView.text = it.text
                    textView.setTextColor(it.color)
                }
            }
        }

    /**
     * 处理内容点击事件
     * 当用户点击文本贴纸时，显示文本编辑对话框
     */
    override fun onContentTap() {
        mDialog.apply {
            setCallback(this@ImageStickerTextView)
            setText(mText)
            show()
        }
    }

    /**
     * 文本编辑完成回调（实现ImageTextEditDialog.Callback接口）
     *
     * @param text 编辑后的文本内容和样式
     */
    override fun onText(text: ImageText) {
        mText = text
        mTextView?.let {
            it.text = text.text
            it.setTextColor(text.color)
        }
    }

    // region 贴纸属性控制（实现ImageSticker接口）
    override var stickerRotation: Float
        get() = rotation
        set(value) {
            rotation = value
        }
    override val stickerPivotX: Float
        get() = pivotX
    override val stickerPivotY: Float
        get() = pivotY
    override var stickerX: Float
        get() = x
        set(value) {
            x = value
        }
    override var stickerY: Float
        get() = y
        set(value) {
            y = value
        }
    // endregion

    companion object {
        /**
         * 基础文本大小，以像素为单位
         */
        private var mBaseTextSize = -1f

        /**
         * 文本视图的内边距，单位为像素
         */
        private const val PADDING = 26

        /**
         * 文本的基础字体大小，单位为sp
         */
        private const val TEXT_SIZE_SP = 24f
    }
}