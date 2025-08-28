package com.zhongjh.imageedit;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.zhongjh.imageedit.core.ImageText;
import com.zhongjh.imageedit.view.ImageColorGroup;

/**
 * 图像文本编辑对话框，用于在图像编辑过程中添加和编辑文本内容
 * 提供文本输入和颜色选择功能，是图像编辑器中文本添加功能的核心交互组件
 * 用户可以通过此对话框输入自定义文本并选择文本颜色
 * 
 * @author felix
 * @date 2017/12/1 上午11:21
 */
public class ImageTextEditDialog extends Dialog implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    /**
     * 文本输入框组件，用于用户输入文本内容
     */
    private EditText mEditText;

    /**
     * 回调接口，用于将编辑完成的文本内容传递给调用方
     */
    private Callback mCallback;

    /**
     * 默认文本对象，用于设置初始显示的文本内容和颜色
     */
    private ImageText mDefaultText;

    /**
     * 颜色选择组组件，提供多种文本颜色选项供用户选择
     */
    private ImageColorGroup mColorGroup;

    /**
     * 构造函数，创建图像文本编辑对话框实例
     * 
     * @param context 上下文对象，通常是调用该对话框的Activity
     * @param callback 回调接口，用于在用户完成编辑后接收文本内容
     */
    public ImageTextEditDialog(Context context, Callback callback) {
        super(context, R.style.ZImageTextDialog);
        setContentView(R.layout.image_text_dialog_zjh);
        mCallback = callback;
        Window window = getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
    }

    /**
     * 对话框创建时调用的方法，初始化界面组件和设置事件监听器
     * 绑定XML布局中的各个UI元素，并设置点击事件监听器
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mColorGroup = findViewById(R.id.cg_colors);
        mColorGroup.setOnCheckedChangeListener(this);
        mEditText = findViewById(R.id.et_text);

        findViewById(R.id.iBtnBack).setOnClickListener(this);
        findViewById(R.id.iBtnDone).setOnClickListener(this);
    }

    /**
     * 关闭对话框的方法
     * 在关闭对话框的同时释放回调接口引用，避免内存泄漏
     */
    @Override
    public void dismiss() {
        super.dismiss();
        mCallback = null;
    }

    /**
     * 对话框显示时调用的方法
     * 根据mDefaultText设置初始文本内容和颜色，或者设置为空
     * 自动选中颜色选择组中与当前文本颜色匹配的选项
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mDefaultText != null) {
            mEditText.setText(mDefaultText.getText());
            mEditText.setTextColor(mDefaultText.getColor());
            if (!mDefaultText.isEmpty()) {
                mEditText.setSelection(mEditText.length());
            }
            mDefaultText = null;
        } else {
            mEditText.setText("");
        }
        mColorGroup.setCheckColor(mEditText.getCurrentTextColor());
    }

    /**
     * 设置默认显示的文本内容和颜色
     * 
     * @param text 包含文本内容和颜色的ImageText对象
     */
    public void setText(ImageText text) {
        mDefaultText = text;
    }

    /**
     * 重置对话框状态到默认值
     * 将文本设为空，颜色设为白色，用于新建文本操作
     */
    public void reset() {
        setText(new ImageText(null, Color.WHITE));
    }

    /**
     * 处理视图点击事件的方法
     * 根据点击的视图ID执行不同的操作（完成编辑或取消操作）
     * 
     * @param v 被点击的视图对象
     */
    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.iBtnDone) {
            onDone();
        } else if (vid == R.id.iBtnBack) {
            dismiss();
        }
    }

    /**
     * 处理完成编辑操作的方法
     * 获取用户输入的文本内容，创建ImageText对象并通过回调接口传递给调用方
     * 最后关闭对话框
     */
    private void onDone() {
        String text = mEditText.getText().toString();
        if (!TextUtils.isEmpty(text) && mCallback != null) {
            mCallback.onText(new ImageText(text, mEditText.getCurrentTextColor()));
        }
        dismiss();
    }

    /**
     * 处理颜色选择变化事件的方法
     * 当用户选择不同的颜色时，更新文本输入框中的文本颜色
     * 
     * @param group 颜色选择组
     * @param checkedId 被选中的颜色按钮ID
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mEditText.setTextColor(mColorGroup.getCheckColor());
    }

    /**
     * 文本编辑回调接口
     * 用于在用户完成文本编辑后将编辑结果通知给调用方
     */
    public interface Callback {

        /**
         * 点击完成按钮后执行的回调方法
         * 将用户编辑好的文本内容和颜色通过ImageText对象传递给调用方
         * 
         * @param text 包含文本内容和颜色信息的ImageText对象
         */
        void onText(ImageText text);
    }

    /**
     * 设置回调接口，用于传递编辑完成的文本内容
     * 
     * @param callback 实现Callback接口的对象
     */
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
}
