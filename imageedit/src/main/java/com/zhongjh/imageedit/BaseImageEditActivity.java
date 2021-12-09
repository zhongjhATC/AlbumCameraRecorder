package com.zhongjh.imageedit;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ViewSwitcher;

import com.zhongjh.imageedit.core.ImageMode;
import com.zhongjh.imageedit.core.ImageText;
import com.zhongjh.imageedit.view.ImageColorGroup;
import com.zhongjh.imageedit.view.ImageViewCustom;

import com.zhongjh.common.utils.StatusBarUtils;

/**
 * Created by felix on 2017/12/5 下午3:08.
 */
abstract class BaseImageEditActivity extends Activity implements View.OnClickListener,
        ImageTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener,
        DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    protected ImageViewCustom mImageViewCustom;

    private RadioGroup mModeGroup;

    private ImageColorGroup mColorGroup;

    private ImageTextEditDialog mTextDialog;

    private View mLayoutOpSub;

    private ViewSwitcher mOpSwitcher, mOpSubSwitcher;

    public static final int OP_HIDE = -1;

    public static final int OP_NORMAL = 0;

    public static final int OP_CLIP = 1;

    public static final int OP_SUB_DOODLE = 0;

    public static final int OP_SUB_MOSAIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtils.initStatusBar(BaseImageEditActivity.this);
        super.onCreate(savedInstanceState);
        Bitmap bitmap = getBitmap();
        if (bitmap != null) {
            setContentView(R.layout.image_edit_activity);
            initViews();
            mImageViewCustom.setImageBitmap(bitmap);
            onCreated();
        } else {
            finish();
        }
    }

    public void onCreated() {

    }

    private void initViews() {
        mImageViewCustom = findViewById(R.id.image_canvas);
        mModeGroup = findViewById(R.id.rg_modes);

        mOpSwitcher = findViewById(R.id.vs_op);
        mOpSubSwitcher = findViewById(R.id.vs_op_sub);

        mColorGroup = findViewById(R.id.cg_colors);
        mColorGroup.setOnCheckedChangeListener(this);

        mLayoutOpSub = findViewById(R.id.layout_op_sub);

        mImageViewCustom.addListener(() -> mColorGroup.clearCheck());
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.rb_doodle) {
            onModeClick(ImageMode.DOODLE);
        } else if (vid == R.id.btn_text) {
            onTextModeClick();
        } else if (vid == R.id.rb_mosaic) {
            onModeClick(ImageMode.MOSAIC);
        } else if (vid == R.id.btn_clip) {
            onModeClick(ImageMode.CLIP);
        } else if (vid == R.id.btn_undo) {
            onUndoClick();
        } else if (vid == R.id.ibtnDone) {
            onDoneClick();
        } else if (vid == R.id.ibtnBack) {
            onCancelClick();
        } else if (vid == R.id.ib_clip_cancel) {
            onCancelClipClick();
        } else if (vid == R.id.ib_clip_done) {
            onDoneClipClick();
        } else if (vid == R.id.tv_clip_reset) {
            onResetClipClick();
        } else if (vid == R.id.ib_clip_rotate) {
            onRotateClipClick();
        }
    }

    public void updateModeUi() {
        ImageMode mode = mImageViewCustom.getMode();
        switch (mode) {
            case DOODLE:
                mModeGroup.check(R.id.rb_doodle);
                setOpSubDisplay(OP_SUB_DOODLE);
                break;
            case MOSAIC:
                mModeGroup.check(R.id.rb_mosaic);
                setOpSubDisplay(OP_SUB_MOSAIC);
                break;
            case NONE:
                mModeGroup.clearCheck();
                setOpSubDisplay(OP_HIDE);
                break;
            default:
                break;
        }
    }

    public void onTextModeClick() {
        if (mTextDialog == null) {
            mTextDialog = new ImageTextEditDialog(this, this);
            mTextDialog.setOnShowListener(this);
            mTextDialog.setOnDismissListener(this);
        }
        mTextDialog.show();
    }

    @Override
    public final void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId != -1) {
            onColorChanged(mColorGroup.getCheckColor());
        }
    }

    public void setOpDisplay(int op) {
        if (op >= 0) {
            mOpSwitcher.setDisplayedChild(op);
        }
    }

    public void setOpSubDisplay(int opSub) {
        if (opSub < 0) {
            mLayoutOpSub.setVisibility(View.GONE);
        } else {
            mOpSubSwitcher.setDisplayedChild(opSub);
            mLayoutOpSub.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onShow(DialogInterface dialog) {
        mOpSwitcher.setVisibility(View.GONE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mOpSwitcher.setVisibility(View.VISIBLE);
    }

    /**
     * 获取数据源
     *
     * @return 返回数据源
     */
    public abstract Bitmap getBitmap();

    /**
     * 点击了模式事件
     *
     * @param mode 模式
     */
    public abstract void onModeClick(ImageMode mode);

    /**
     * 点击了撤销事件
     */
    public abstract void onUndoClick();

    /**
     * 点击了取消事件
     */
    public abstract void onCancelClick();

    /**
     * 点击了完成事件
     */
    public abstract void onDoneClick();

    /**
     * 裁剪：点击了取消事件
     */
    public abstract void onCancelClipClick();

    /**
     * 裁剪：点击了完成事件
     */
    public abstract void onDoneClipClick();

    /**
     * 裁剪：点击了重置事件
     */
    public abstract void onResetClipClick();

    /**
     * 裁剪：点击了旋转事件
     */
    public abstract void onRotateClipClick();

    /**
     * 改变颜色事件
     *
     * @param checkedColor 选择的颜色
     */
    public abstract void onColorChanged(int checkedColor);

    /**
     * 添加文本
     *
     * @param text 文本
     */
    @Override
    public abstract void onText(ImageText text);
}
