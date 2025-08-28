package com.zhongjh.imageedit.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.zhongjh.imageedit.ImageTextEditDialog;
import com.zhongjh.imageedit.core.ImageText;

/**
 * 图像文本贴纸视图组件，用于在图像上显示和编辑文本贴纸
 * 支持文本内容、颜色的设置和编辑
 * 
 * @author felix
 * @date 2017/11/14 下午7:27
 */
public class ImageStickerTextView extends BaseImageStickerView implements ImageTextEditDialog.Callback {

    /**
     * 用于显示文本内容的TextView组件
     */
    private TextView mTextView;

    /**
     * 存储文本内容和样式的ImageText对象
     */
    private ImageText mText;

    /**
     * 用于编辑文本内容和样式的对话框
     */
    private ImageTextEditDialog mDialog;

    /**
     * 基础文本大小，以像素为单位
     */
    private static float mBaseTextSize = -1f;

    /**
     * 文本视图的内边距，单位为像素
     */
    private static final int PADDING = 26;

    /**
     * 文本的基础字体大小，单位为sp
     */
    private static final float TEXT_SIZE_SP = 24f;

    /**
     * 构造函数，创建图像文本贴纸视图
     * 
     * @param context 上下文对象
     */
    public ImageStickerTextView(Context context) {
        this(context, null, 0);
    }

    /**
     * 构造函数，创建图像文本贴纸视图
     * 
     * @param context 上下文对象
     * @param attrs XML属性集合
     */
    public ImageStickerTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造函数，创建图像文本贴纸视图
     * 
     * @param context 上下文对象
     * @param attrs XML属性集合
     * @param defStyleAttr 默认样式属性
     */
    public ImageStickerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化视图组件
     * 计算基础文本大小并调用父类初始化方法
     * 
     * @param context 上下文对象
     */
    @Override
    public void onInitialize(Context context) {
        if (mBaseTextSize <= 0) {
            // 将sp单位转换为像素单位
            mBaseTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 
                    TEXT_SIZE_SP, context.getResources().getDisplayMetrics());
        }
        // 调用父类的初始化方法
        super.onInitialize(context);
    }

    /**
     * 创建内容视图
     * 初始化文本显示组件并设置默认样式
     * 
     * @param context 上下文对象
     * @return 创建的文本视图
     */
    @Override
    public View onCreateContentView(Context context) {
        mTextView = new TextView(context);
        // 设置文本大小
        mTextView.setTextSize(mBaseTextSize);
        // 设置内边距
        mTextView.setPadding(PADDING, PADDING, PADDING, PADDING);
        // 设置默认文本颜色
        mTextView.setTextColor(Color.WHITE);

        return mTextView;
    }

    /**
     * 设置文本内容和样式
     * 
     * @param text 包含文本内容和样式的ImageText对象
     */
    public void setText(ImageText text) {
        mText = text;
        if (mText != null && mTextView != null) {
            // 设置文本内容
            mTextView.setText(mText.getText());
            // 设置文本颜色
            mTextView.setTextColor(mText.getColor());
        }
    }

    /**
     * 获取当前文本内容和样式
     * 
     * @return 包含文本内容和样式的ImageText对象
     */
    public ImageText getText() {
        return mText;
    }

    /**
     * 处理内容点击事件
     * 当用户点击文本贴纸时，显示文本编辑对话框
     */
    @Override
    public void onContentTap() {
        // 获取或创建文本编辑对话框
        ImageTextEditDialog dialog = getDialog();
        // 设置回调接口
        dialog.setCallback(this);
        // 设置当前文本内容
        dialog.setText(mText);
        // 显示对话框
        dialog.show();
    }

    /**
     * 获取文本编辑对话框实例
     * 使用懒加载模式创建对话框
     * 
     * @return 文本编辑对话框实例
     */
    private ImageTextEditDialog getDialog() {
        if (mDialog == null) {
            // 创建新的文本编辑对话框
            mDialog = new ImageTextEditDialog(getContext(), this);
        }
        return mDialog;
    }

    /**
     * 实现ImageTextEditDialog.Callback接口的方法
     * 当文本编辑完成时被调用，更新文本内容和样式
     * 
     * @param text 编辑后的文本内容和样式
     */
    @Override
    public void onText(ImageText text) {
        // 更新文本对象
        mText = text;
        if (mText != null && mTextView != null) {
            // 更新文本内容
            mTextView.setText(mText.getText());
            // 更新文本颜色
            mTextView.setTextColor(mText.getColor());
        }
    }
}