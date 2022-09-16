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
 * @author felix
 * @date 2017/12/1 上午11:21
 */
public class ImageTextEditDialog extends Dialog implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    private EditText mEditText;

    private Callback mCallback;

    private ImageText mDefaultText;

    private ImageColorGroup mColorGroup;

    public ImageTextEditDialog(Context context, Callback callback) {
        super(context, R.style.ZImageTextDialog);
        setContentView(R.layout.image_text_dialog_zjh);
        mCallback = callback;
        Window window = getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mColorGroup = findViewById(R.id.cg_colors);
        mColorGroup.setOnCheckedChangeListener(this);
        mEditText = findViewById(R.id.et_text);

        findViewById(R.id.ibtnBack).setOnClickListener(this);
        findViewById(R.id.ibtnDone).setOnClickListener(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mCallback = null;
    }

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

    public void setText(ImageText text) {
        mDefaultText = text;
    }

    public void reset() {
        setText(new ImageText(null, Color.WHITE));
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.ibtnDone) {
            onDone();
        } else if (vid == R.id.ibtnBack) {
            dismiss();
        }
    }

    private void onDone() {
        String text = mEditText.getText().toString();
        if (!TextUtils.isEmpty(text) && mCallback != null) {
            mCallback.onText(new ImageText(text, mEditText.getCurrentTextColor()));
        }
        dismiss();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mEditText.setTextColor(mColorGroup.getCheckColor());
    }

    public interface Callback {

        /**
         * 点击完成后执行的该事件
         *
         * @param text dialog的text
         */
        void onText(ImageText text);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
}
