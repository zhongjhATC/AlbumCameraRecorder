package com.zhongjh.imageedit

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.ViewSwitcher
import com.zhongjh.common.utils.StatusBarUtils
import com.zhongjh.imageedit.core.ImageMode
import com.zhongjh.imageedit.core.ImageText
import com.zhongjh.imageedit.view.ImageColorGroup
import com.zhongjh.imageedit.view.ImageViewCustom

/**
 * Created by felix on 2017/12/5 下午3:08.
 */
internal abstract class BaseImageEditActivity1 : Activity(), View.OnClickListener, ImageTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener, DialogInterface.OnShowListener, DialogInterface.OnDismissListener {
    protected var mImageViewCustom: ImageViewCustom? = null
    private var mModeGroup: RadioGroup? = null
    private var mColorGroup: ImageColorGroup? = null
    private var mTextDialog: ImageTextEditDialog? = null
    private var mLayoutOpSub: View? = null
    private var mOpSwitcher: ViewSwitcher? = null
    private var mOpSubSwitcher: ViewSwitcher? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarUtils.initStatusBar(this@BaseImageEditActivity)
        super.onCreate(savedInstanceState)
        val bitmap = bitmap
        if (bitmap != null) {
            setContentView(R.layout.image_edit_activity)
            initViews()
            mImageViewCustom.setImageBitmap(bitmap)
            onCreated()
        } else {
            finish()
        }
    }

    fun onCreated() {}
    private fun initViews() {
        mImageViewCustom = findViewById(R.id.image_canvas)
        mModeGroup = findViewById(R.id.rg_modes)
        mOpSwitcher = findViewById(R.id.vs_op)
        mOpSubSwitcher = findViewById(R.id.vs_op_sub)
        mColorGroup = findViewById(R.id.cg_colors)
        mColorGroup.setOnCheckedChangeListener(this)
        mLayoutOpSub = findViewById(R.id.layout_op_sub)
        mImageViewCustom.addListener({ mColorGroup.clearCheck() })
    }

    override fun onClick(v: View) {
        val vid = v.id
        if (vid == R.id.rb_doodle) {
            onModeClick(ImageMode.DOODLE)
        } else if (vid == R.id.btn_text) {
            onTextModeClick()
        } else if (vid == R.id.rb_mosaic) {
            onModeClick(ImageMode.MOSAIC)
        } else if (vid == R.id.btn_clip) {
            onModeClick(ImageMode.CLIP)
        } else if (vid == R.id.btn_undo) {
            onUndoClick()
        } else if (vid == R.id.ibtnDone) {
            onDoneClick()
        } else if (vid == R.id.ibtnBack) {
            onCancelClick()
        } else if (vid == R.id.ib_clip_cancel) {
            onCancelClipClick()
        } else if (vid == R.id.ib_clip_done) {
            onDoneClipClick()
        } else if (vid == R.id.tv_clip_reset) {
            onResetClipClick()
        } else if (vid == R.id.ib_clip_rotate) {
            onRotateClipClick()
        }
    }

    fun updateModeUi() {
        val mode: ImageMode = mImageViewCustom.getMode()
        when (mode) {
            DOODLE -> {
                mModeGroup!!.check(R.id.rb_doodle)
                setOpSubDisplay(OP_SUB_DOODLE)
            }
            MOSAIC -> {
                mModeGroup!!.check(R.id.rb_mosaic)
                setOpSubDisplay(OP_SUB_MOSAIC)
            }
            NONE -> {
                mModeGroup!!.clearCheck()
                setOpSubDisplay(OP_HIDE)
            }
            else -> {
            }
        }
    }

    fun onTextModeClick() {
        if (mTextDialog == null) {
            mTextDialog = ImageTextEditDialog(this, this)
            mTextDialog.setOnShowListener(this)
            mTextDialog.setOnDismissListener(this)
        }
        mTextDialog.show()
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        if (checkedId != -1) {
            onColorChanged(mColorGroup.getCheckColor())
        }
    }

    fun setOpDisplay(op: Int) {
        if (op >= 0) {
            mOpSwitcher!!.displayedChild = op
        }
    }

    fun setOpSubDisplay(opSub: Int) {
        if (opSub < 0) {
            mLayoutOpSub!!.visibility = View.GONE
        } else {
            mOpSubSwitcher!!.displayedChild = opSub
            mLayoutOpSub!!.visibility = View.VISIBLE
        }
    }

    override fun onShow(dialog: DialogInterface) {
        mOpSwitcher!!.visibility = View.GONE
    }

    override fun onDismiss(dialog: DialogInterface) {
        mOpSwitcher!!.visibility = View.VISIBLE
    }

    /**
     * 获取数据源
     *
     * @return 返回数据源
     */
    abstract val bitmap: Bitmap?

    /**
     * 点击了模式事件
     *
     * @param mode 模式
     */
    abstract fun onModeClick(mode: ImageMode?)

    /**
     * 点击了撤销事件
     */
    abstract fun onUndoClick()

    /**
     * 点击了取消事件
     */
    abstract fun onCancelClick()

    /**
     * 点击了完成事件
     */
    abstract fun onDoneClick()

    /**
     * 裁剪：点击了取消事件
     */
    abstract fun onCancelClipClick()

    /**
     * 裁剪：点击了完成事件
     */
    abstract fun onDoneClipClick()

    /**
     * 裁剪：点击了重置事件
     */
    abstract fun onResetClipClick()

    /**
     * 裁剪：点击了旋转事件
     */
    abstract fun onRotateClipClick()

    /**
     * 改变颜色事件
     *
     * @param checkedColor 选择的颜色
     */
    abstract fun onColorChanged(checkedColor: Int)

    /**
     * 添加文本
     *
     * @param text 文本
     */
    abstract fun onText(text: ImageText?)

    companion object {
        const val OP_HIDE = -1
        const val OP_NORMAL = 0
        const val OP_CLIP = 1
        const val OP_SUB_DOODLE = 0
        const val OP_SUB_MOSAIC = 1
    }
}