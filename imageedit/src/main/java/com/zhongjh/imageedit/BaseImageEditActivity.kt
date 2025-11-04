package com.zhongjh.imageedit

import android.app.Activity
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.ViewSwitcher
import com.zhongjh.common.utils.StatusBarUtils.initStatusBar
import com.zhongjh.imageedit.core.ImageMode
import com.zhongjh.imageedit.core.ImageText
import com.zhongjh.imageedit.view.ImageColorGroup
import com.zhongjh.imageedit.view.ImageViewCustom
import java.lang.ref.WeakReference

/**
 * 图像编辑的基础活动类
 * 提供图像编辑的通用界面和功能，是所有图像编辑活动的基类
 * 实现了视图点击、文本编辑、颜色选择等常见交互逻辑
 * 定义了编辑模式切换、撤销、完成、裁剪等核心操作的抽象方法
 *
 * @author zhongjh
 * @date 2025/11/04
 */
internal abstract class BaseImageEditActivity : Activity(), View.OnClickListener, ImageTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener, OnShowListener, DialogInterface.OnDismissListener {
    /**
     * 自定义图像视图，用于显示和编辑图像
     * 负责处理图像的各种编辑操作，如涂鸦、马赛克、裁剪等
     */
    protected val mImageViewCustom: ImageViewCustom by lazy {
        findViewById(R.id.image_canvas)
    }

    /**
     * 模式选择组，用于切换不同的编辑模式（涂鸦、马赛克等）
     * 包含涂鸦模式和马赛克模式两个选项按钮
     */
    private val mModeGroup: RadioGroup by lazy {
        findViewById(R.id.rg_modes)
    }

    /**
     * 颜色选择组，提供多种预设颜色选项
     * 主要用于涂鸦模式下选择画笔颜色
     */
    private val mColorGroup: ImageColorGroup by lazy {
        findViewById(R.id.cg_colors)
    }

    /**
     * 文本编辑对话框，用于添加和编辑图像上的文本
     * 提供文本输入和颜色选择功能
     */
    private val mTextDialog: ImageTextEditDialog by lazy {
        val textDialog = ImageTextEditDialog(this, this)
        textDialog.setOnShowListener(this)
        textDialog.setOnDismissListener(this)
        textDialog
    }

    /**
     * 子操作布局容器，包含涂鸦和马赛克等具体操作的界面元素
     * 根据当前选中的编辑模式显示相应的子操作界面
     */
    private val mLayoutOpSub: View by lazy {
        findViewById(R.id.layout_op_sub)
    }

    /**
     * 主操作切换器，用于在不同的主操作界面（普通、裁剪）之间切换
     * 控制显示普通编辑界面或裁剪操作界面
     */
    private val mOpSwitcher: ViewSwitcher by lazy {
        findViewById(R.id.vs_op)
    }

    /**
     * 子操作切换器，用于在不同的子操作界面（涂鸦、马赛克）之间切换
     * 在子操作布局容器内部切换显示不同编辑模式的具体操作界面
     */
    private val mOpSubSwitcher: ViewSwitcher by lazy {
        findViewById(R.id.vs_op_sub)
    }

    /**
     * 活动创建时的初始化方法
     * 设置屏幕方向，初始化状态栏，获取图像数据并设置到视图中
     * 如果无法获取图像，则直接结束活动
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // 设置屏幕方向，默认为竖屏
        requestedOrientation = intent.getIntExtra(ImageEditActivity.EXTRA_IMAGE_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        // 初始化状态栏
        initStatusBar(this@BaseImageEditActivity)
        super.onCreate(savedInstanceState)
        // 获取要编辑的图像
        val bitmap = bitmap
        if (bitmap != null) {
            // 设置布局和初始化视图
            setContentView(R.layout.image_edit_activity_zjh)
            initViews()
            // 将图像设置到自定义图像视图中
            mImageViewCustom.setImageBitmap(bitmap)
        } else {
            // 如果图像为空，直接结束活动
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 关闭 Dialog 并释放引用
        if (mTextDialog.isShowing) {
            mTextDialog.dismiss()
        }
        // 移除回调引用
        mTextDialog.setCallback(null)
        mTextDialog.setOnShowListener(null)
        mTextDialog.setOnDismissListener(null)
    }

    /**
     * 初始化视图组件
     * 获取各个UI组件的引用，并设置相应的监听器
     */
    private fun initViews() {
        // 获取颜色选择组，并设置监听器
        mColorGroup.setOnCheckedChangeListener(WeakCheckedChangeListener(WeakReference(this)))

        // 为图像视图添加监听器，在编辑操作时清除颜色选择
        mImageViewCustom.addListener(WeakImageListener(WeakReference(this)))
    }

    /**
     * 处理视图点击事件
     * 根据点击的视图ID执行相应的操作
     *
     * @param v 被点击的视图对象
     */
    override fun onClick(v: View) {
        val vid = v.id
        // 根据视图ID执行相应操作
        when (vid) {
            R.id.rb_doodle -> {
                // 切换到涂鸦模式
                onModeClick(ImageMode.DOODLE)
            }

            R.id.btn_text -> {
                // 打开文本编辑模式
                onTextModeClick()
            }

            R.id.rb_mosaic -> {
                // 切换到马赛克模式
                onModeClick(ImageMode.MOSAIC)
            }

            R.id.btn_clip -> {
                // 切换到裁剪模式
                onModeClick(ImageMode.CLIP)
            }

            R.id.btn_undo -> {
                // 执行撤销操作
                onUndoClick()
            }

            R.id.iBtnDone -> {
                // 执行完成操作
                onDoneClick()
            }

            R.id.iBtnBack -> {
                // 执行取消操作
                onCancelClick()
            }

            R.id.ib_clip_cancel -> {
                // 取消裁剪操作
                onCancelClipClick()
            }

            R.id.ib_clip_done -> {
                // 完成裁剪操作
                onDoneClipClick()
            }

            R.id.tv_clip_reset -> {
                // 重置裁剪区域
                onResetClipClick()
            }

            R.id.ib_clip_rotate -> {
                // 旋转裁剪区域
                onRotateClipClick()
            }
        }
    }

    /**
     * 更新模式UI显示
     * 根据当前的编辑模式更新UI状态，包括模式选择按钮和子操作界面
     */
    fun updateModeUi() {
        val mode = mImageViewCustom.mode
        when (mode) {
            ImageMode.DOODLE -> {
                // 选中涂鸦模式按钮，显示涂鸦子操作界面
                mModeGroup.check(R.id.rb_doodle)
                setOpSubDisplay(OP_SUB_DOODLE)
            }

            ImageMode.MOSAIC -> {
                // 选中马赛克模式按钮，显示马赛克子操作界面
                mModeGroup.check(R.id.rb_mosaic)
                setOpSubDisplay(OP_SUB_MOSAIC)
            }

            ImageMode.NONE -> {
                // 清除所有模式选中状态，隐藏子操作界面
                mModeGroup.clearCheck()
                setOpSubDisplay(OP_HIDE)
            }

            else -> {}
        }
    }

    /**
     * 处理文本模式点击事件
     * 显示文本编辑对话框，用于添加或编辑图像上的文本
     * 如果对话框不存在则创建新的，否则重用已存在的对话框
     */
    private fun onTextModeClick() {
        // 重新设置回调
        mTextDialog.setCallback(this)
        // 显示文本编辑对话框
        mTextDialog.show()
    }

    /**
     * 处理颜色选择变化事件
     * 当颜色选择组中的选中项发生变化时调用
     *
     * @param group 颜色选择组
     * @param checkedId 被选中的颜色ID
     */
    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        // 只有当有颜色被选中时才处理颜色变化
        if (checkedId != -1) {
            onColorChanged(mColorGroup.checkColor)
        }
    }

    /**
     * 设置主操作界面的显示状态
     * 根据指定的操作类型切换主操作界面
     *
     * @param op 操作类型，可以是OP_NORMAL（普通模式）或OP_CLIP（裁剪模式）
     */
    fun setOpDisplay(op: Int) {
        // 只有当操作类型有效时才进行切换
        if (op >= 0) {
            mOpSwitcher.displayedChild = op
        }
    }

    /**
     * 设置子操作界面的显示状态
     * 根据指定的子操作类型切换子操作界面，或隐藏子操作界面
     *
     * @param opSub 子操作类型，可以是OP_HIDE（隐藏）、OP_SUB_DOODLE（涂鸦模式）或OP_SUB_MOSAIC（马赛克模式）
     */
    private fun setOpSubDisplay(opSub: Int) {
        if (opSub < 0) {
            // 隐藏子操作界面
            mLayoutOpSub.visibility = View.GONE
        } else {
            // 切换到指定的子操作界面并显示
            mOpSubSwitcher.displayedChild = opSub
            mLayoutOpSub.visibility = View.VISIBLE
        }
    }

    /**
     * 当对话框显示时调用
     * 隐藏主操作界面，避免与对话框重叠
     *
     * @param dialog 显示的对话框
     */
    override fun onShow(dialog: DialogInterface) {
        // 隐藏主操作界面
        mOpSwitcher.visibility = View.GONE
    }

    /**
     * 当对话框消失时调用
     * 恢复显示主操作界面
     *
     * @param dialog 消失的对话框
     */
    override fun onDismiss(dialog: DialogInterface) {
        // 恢复显示主操作界面
        mOpSwitcher.visibility = View.VISIBLE
    }

    /**
     * 获取要编辑的图像数据源
     * 子类必须实现此方法来提供要编辑的图像
     *
     * @return 要编辑的图像位图，如果无法获取则返回null
     */
    abstract val bitmap: Bitmap?

    /**
     * 处理模式点击事件
     * 当用户点击不同的编辑模式按钮时调用
     *
     * @param mode 被点击的图像编辑模式
     */
    abstract fun onModeClick(mode: ImageMode?)

    /**
     * 处理撤销操作点击事件
     * 当用户点击撤销按钮时调用，用于撤销上一步操作
     */
    abstract fun onUndoClick()

    /**
     * 处理取消操作点击事件
     * 当用户点击取消按钮时调用，通常用于放弃所有更改并退出编辑
     */
    abstract fun onCancelClick()

    /**
     * 处理完成操作点击事件
     * 当用户点击完成按钮时调用，通常用于保存编辑结果并退出编辑
     */
    abstract fun onDoneClick()

    /**
     * 处理取消裁剪操作点击事件
     * 当用户在裁剪模式下点击取消按钮时调用，放弃裁剪操作
     */
    abstract fun onCancelClipClick()

    /**
     * 处理完成裁剪操作点击事件
     * 当用户在裁剪模式下点击完成按钮时调用，执行裁剪并应用更改
     */
    abstract fun onDoneClipClick()

    /**
     * 处理重置裁剪区域点击事件
     * 当用户在裁剪模式下点击重置按钮时调用，将裁剪区域重置为初始状态
     */
    abstract fun onResetClipClick()

    /**
     * 处理旋转裁剪区域点击事件
     * 当用户在裁剪模式下点击旋转按钮时调用，旋转裁剪区域
     */
    abstract fun onRotateClipClick()

    /**
     * 处理颜色选择变化事件
     * 当用户选择不同的颜色时调用，通常用于更新画笔颜色
     *
     * @param checkedColor 选中的颜色值
     */
    abstract fun onColorChanged(checkedColor: Int)

    /**
     * 处理添加文本操作
     * 当用户在文本编辑对话框中确认添加文本时调用
     *
     * @param text 要添加到图像上的文本对象
     */
    abstract override fun onText(text: ImageText)

    companion object {
        /**
         * 操作显示状态：隐藏
         */
        const val OP_HIDE: Int = -1

        /**
         * 操作显示状态：普通模式
         */
        const val OP_NORMAL: Int = 0

        /**
         * 操作显示状态：裁剪模式
         */
        const val OP_CLIP: Int = 1

        /**
         * 子操作显示状态：涂鸦模式
         */
        const val OP_SUB_DOODLE: Int = 0

        /**
         * 子操作显示状态：马赛克模式
         */
        const val OP_SUB_MOSAIC: Int = 1
    }

    /**
     * 静态内部类，弱引用包装 OnCheckedChangeListener
     */
    private class WeakCheckedChangeListener(private val activityRef: WeakReference<BaseImageEditActivity>) : RadioGroup.OnCheckedChangeListener {
        override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
            // 仅当 Activity 未被回收时，才转发回调
            activityRef.get()?.onCheckedChanged(group, checkedId)
        }
    }

    /**
     * 静态内部类，弱引用包装 ImageViewCustom.Listener
     */
    private class WeakImageListener(private val activityRef: WeakReference<BaseImageEditActivity>) : ImageViewCustom.Listener {
        override fun resetModel() {
            // 仅当 Activity 未被回收时执行逻辑
            activityRef.get()?.mColorGroup?.clearCheck()
        }
    }
}
