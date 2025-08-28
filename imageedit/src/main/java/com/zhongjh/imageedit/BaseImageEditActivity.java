package com.zhongjh.imageedit;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static com.zhongjh.imageedit.ImageEditActivity.EXTRA_IMAGE_SCREEN_ORIENTATION;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ViewSwitcher;

import com.zhongjh.common.utils.StatusBarUtils;
import com.zhongjh.imageedit.core.ImageMode;
import com.zhongjh.imageedit.core.ImageText;
import com.zhongjh.imageedit.view.ImageColorGroup;
import com.zhongjh.imageedit.view.ImageViewCustom;

/**
 * 图像编辑的基础活动类
 * 提供图像编辑的通用界面和功能，是所有图像编辑活动的基类
 * 实现了视图点击、文本编辑、颜色选择等常见交互逻辑
 * 定义了编辑模式切换、撤销、完成、裁剪等核心操作的抽象方法
 * 
 * @author felix
 * @date 2017/12/5 下午3:08
 */
abstract class BaseImageEditActivity extends Activity implements View.OnClickListener,
        ImageTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener,
        DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    /**
     * 自定义图像视图，用于显示和编辑图像
     * 负责处理图像的各种编辑操作，如涂鸦、马赛克、裁剪等
     */
    protected ImageViewCustom mImageViewCustom;

    /**
     * 模式选择组，用于切换不同的编辑模式（涂鸦、马赛克等）
     * 包含涂鸦模式和马赛克模式两个选项按钮
     */
    private RadioGroup mModeGroup;

    /**
     * 颜色选择组，提供多种预设颜色选项
     * 主要用于涂鸦模式下选择画笔颜色
     */
    private ImageColorGroup mColorGroup;

    /**
     * 文本编辑对话框，用于添加和编辑图像上的文本
     * 提供文本输入和颜色选择功能
     */
    private ImageTextEditDialog mTextDialog;

    /**
     * 子操作布局容器，包含涂鸦和马赛克等具体操作的界面元素
     * 根据当前选中的编辑模式显示相应的子操作界面
     */
    private View mLayoutOpSub;

    /**
     * 主操作切换器，用于在不同的主操作界面（普通、裁剪）之间切换
     * 控制显示普通编辑界面或裁剪操作界面
     */
    private ViewSwitcher mOpSwitcher;
    
    /**
     * 子操作切换器，用于在不同的子操作界面（涂鸦、马赛克）之间切换
     * 在子操作布局容器内部切换显示不同编辑模式的具体操作界面
     */
    private ViewSwitcher mOpSubSwitcher;

    /**
     * 操作显示状态：隐藏
     */
    public static final int OP_HIDE = -1;

    /**
     * 操作显示状态：普通模式
     */
    public static final int OP_NORMAL = 0;

    /**
     * 操作显示状态：裁剪模式
     */
    public static final int OP_CLIP = 1;

    /**
     * 子操作显示状态：涂鸦模式
     */
    public static final int OP_SUB_DOODLE = 0;

    /**
     * 子操作显示状态：马赛克模式
     */
    public static final int OP_SUB_MOSAIC = 1;


    /**
     * 活动创建时的初始化方法
     * 设置屏幕方向，初始化状态栏，获取图像数据并设置到视图中
     * 如果无法获取图像，则直接结束活动
     * 
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置屏幕方向，默认为竖屏
        setRequestedOrientation(getIntent().getIntExtra(EXTRA_IMAGE_SCREEN_ORIENTATION, SCREEN_ORIENTATION_PORTRAIT));
        // 初始化状态栏
        StatusBarUtils.initStatusBar(BaseImageEditActivity.this);
        super.onCreate(savedInstanceState);
        // 获取要编辑的图像
        Bitmap bitmap = getBitmap();
        if (bitmap != null) {
            // 设置布局和初始化视图
            setContentView(R.layout.image_edit_activity_zjh);
            initViews();
            // 将图像设置到自定义图像视图中
            mImageViewCustom.setImageBitmap(bitmap);
        } else {
            // 如果图像为空，直接结束活动
            finish();
        }
    }

    /**
     * 初始化视图组件
     * 获取各个UI组件的引用，并设置相应的监听器
     */
    private void initViews() {
        // 获取自定义图像视图
        mImageViewCustom = findViewById(R.id.image_canvas);
        // 获取模式选择组
        mModeGroup = findViewById(R.id.rg_modes);

        // 获取操作切换器
        mOpSwitcher = findViewById(R.id.vs_op);
        mOpSubSwitcher = findViewById(R.id.vs_op_sub);

        // 获取颜色选择组，并设置监听器
        mColorGroup = findViewById(R.id.cg_colors);
        mColorGroup.setOnCheckedChangeListener(this);

        // 获取子操作布局
        mLayoutOpSub = findViewById(R.id.layout_op_sub);

        // 为图像视图添加监听器，在编辑操作时清除颜色选择
        mImageViewCustom.addListener(() -> mColorGroup.clearCheck());
    }

    /**
     * 处理视图点击事件
     * 根据点击的视图ID执行相应的操作
     * 
     * @param v 被点击的视图对象
     */
    @Override
    public void onClick(View v) {
        int vid = v.getId();
        // 根据视图ID执行相应操作
        if (vid == R.id.rb_doodle) {
            // 切换到涂鸦模式
            onModeClick(ImageMode.DOODLE);
        } else if (vid == R.id.btn_text) {
            // 打开文本编辑模式
            onTextModeClick();
        } else if (vid == R.id.rb_mosaic) {
            // 切换到马赛克模式
            onModeClick(ImageMode.MOSAIC);
        } else if (vid == R.id.btn_clip) {
            // 切换到裁剪模式
            onModeClick(ImageMode.CLIP);
        } else if (vid == R.id.btn_undo) {
            // 执行撤销操作
            onUndoClick();
        } else if (vid == R.id.iBtnDone) {
            // 执行完成操作
            onDoneClick();
        } else if (vid == R.id.iBtnBack) {
            // 执行取消操作
            onCancelClick();
        } else if (vid == R.id.ib_clip_cancel) {
            // 取消裁剪操作
            onCancelClipClick();
        } else if (vid == R.id.ib_clip_done) {
            // 完成裁剪操作
            onDoneClipClick();
        } else if (vid == R.id.tv_clip_reset) {
            // 重置裁剪区域
            onResetClipClick();
        } else if (vid == R.id.ib_clip_rotate) {
            // 旋转裁剪区域
            onRotateClipClick();
        }
    }

    /**
     * 更新模式UI显示
     * 根据当前的编辑模式更新UI状态，包括模式选择按钮和子操作界面
     */
    public void updateModeUi() {
        ImageMode mode = mImageViewCustom.getMode();
        // 根据当前模式更新UI状态
        switch (mode) {
            case DOODLE:
                // 选中涂鸦模式按钮，显示涂鸦子操作界面
                mModeGroup.check(R.id.rb_doodle);
                setOpSubDisplay(OP_SUB_DOODLE);
                break;
            case MOSAIC:
                // 选中马赛克模式按钮，显示马赛克子操作界面
                mModeGroup.check(R.id.rb_mosaic);
                setOpSubDisplay(OP_SUB_MOSAIC);
                break;
            case NONE:
                // 清除所有模式选中状态，隐藏子操作界面
                mModeGroup.clearCheck();
                setOpSubDisplay(OP_HIDE);
                break;
            default:
                break;
        }
    }

    /**
     * 处理文本模式点击事件
     * 显示文本编辑对话框，用于添加或编辑图像上的文本
     * 如果对话框不存在则创建新的，否则重用已存在的对话框
     */
    public void onTextModeClick() {
        if (mTextDialog == null) {
            // 创建新的文本编辑对话框并设置监听器
            mTextDialog = new ImageTextEditDialog(this, this);
            mTextDialog.setOnShowListener(this);
            mTextDialog.setOnDismissListener(this);
        } else {
            // 重用已存在的对话框，设置回调
            mTextDialog.setCallback(this);
        }
        // 显示文本编辑对话框
        mTextDialog.show();
    }

    /**
     * 处理颜色选择变化事件
     * 当颜色选择组中的选中项发生变化时调用
     * 
     * @param group 颜色选择组
     * @param checkedId 被选中的颜色ID
     */
    @Override
    public final void onCheckedChanged(RadioGroup group, int checkedId) {
        // 只有当有颜色被选中时才处理颜色变化
        if (checkedId != -1) {
            onColorChanged(mColorGroup.getCheckColor());
        }
    }

    /**
     * 设置主操作界面的显示状态
     * 根据指定的操作类型切换主操作界面
     * 
     * @param op 操作类型，可以是OP_NORMAL（普通模式）或OP_CLIP（裁剪模式）
     */
    public void setOpDisplay(int op) {
        // 只有当操作类型有效时才进行切换
        if (op >= 0) {
            mOpSwitcher.setDisplayedChild(op);
        }
    }

    /**
     * 设置子操作界面的显示状态
     * 根据指定的子操作类型切换子操作界面，或隐藏子操作界面
     * 
     * @param opSub 子操作类型，可以是OP_HIDE（隐藏）、OP_SUB_DOODLE（涂鸦模式）或OP_SUB_MOSAIC（马赛克模式）
     */
    public void setOpSubDisplay(int opSub) {
        if (opSub < 0) {
            // 隐藏子操作界面
            mLayoutOpSub.setVisibility(View.GONE);
        } else {
            // 切换到指定的子操作界面并显示
            mOpSubSwitcher.setDisplayedChild(opSub);
            mLayoutOpSub.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 当对话框显示时调用
     * 隐藏主操作界面，避免与对话框重叠
     * 
     * @param dialog 显示的对话框
     */
    @Override
    public void onShow(DialogInterface dialog) {
        // 隐藏主操作界面
        mOpSwitcher.setVisibility(View.GONE);
    }

    /**
     * 当对话框消失时调用
     * 恢复显示主操作界面
     * 
     * @param dialog 消失的对话框
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        // 恢复显示主操作界面
        mOpSwitcher.setVisibility(View.VISIBLE);
    }

    /**
     * 获取要编辑的图像数据源
     * 子类必须实现此方法来提供要编辑的图像
     * 
     * @return 要编辑的图像位图，如果无法获取则返回null
     */
    public abstract Bitmap getBitmap();

    /**
     * 处理模式点击事件
     * 当用户点击不同的编辑模式按钮时调用
     * 
     * @param mode 被点击的图像编辑模式
     */
    public abstract void onModeClick(ImageMode mode);

    /**
     * 处理撤销操作点击事件
     * 当用户点击撤销按钮时调用，用于撤销上一步操作
     */
    public abstract void onUndoClick();

    /**
     * 处理取消操作点击事件
     * 当用户点击取消按钮时调用，通常用于放弃所有更改并退出编辑
     */
    public abstract void onCancelClick();

    /**
     * 处理完成操作点击事件
     * 当用户点击完成按钮时调用，通常用于保存编辑结果并退出编辑
     */
    public abstract void onDoneClick();

    /**
     * 处理取消裁剪操作点击事件
     * 当用户在裁剪模式下点击取消按钮时调用，放弃裁剪操作
     */
    public abstract void onCancelClipClick();

    /**
     * 处理完成裁剪操作点击事件
     * 当用户在裁剪模式下点击完成按钮时调用，执行裁剪并应用更改
     */
    public abstract void onDoneClipClick();

    /**
     * 处理重置裁剪区域点击事件
     * 当用户在裁剪模式下点击重置按钮时调用，将裁剪区域重置为初始状态
     */
    public abstract void onResetClipClick();

    /**
     * 处理旋转裁剪区域点击事件
     * 当用户在裁剪模式下点击旋转按钮时调用，旋转裁剪区域
     */
    public abstract void onRotateClipClick();

    /**
     * 处理颜色选择变化事件
     * 当用户选择不同的颜色时调用，通常用于更新画笔颜色
     * 
     * @param checkedColor 选中的颜色值
     */
    public abstract void onColorChanged(int checkedColor);

    /**
     * 处理添加文本操作
     * 当用户在文本编辑对话框中确认添加文本时调用
     * 
     * @param text 要添加到图像上的文本对象
     */
    @Override
    public abstract void onText(ImageText text);
}
