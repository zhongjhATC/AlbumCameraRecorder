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
 *
 * @author felix
 * @date 2017/11/14 下午7:27
 */
public class ImageStickerTextView extends BaseImageStickerView implements ImageTextEditDialog.Callback {

    private TextView mTextView;

    private ImageText mText;

    private ImageTextEditDialog mDialog;

    private static float mBaseTextSize = -1f;

    private static final int PADDING = 26;

    private static final float TEXT_SIZE_SP = 24f;

    public ImageStickerTextView(Context context) {
        this(context, null, 0);
    }

    public ImageStickerTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageStickerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onInitialize(Context context) {
        if (mBaseTextSize <= 0) {
            mBaseTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    TEXT_SIZE_SP, context.getResources().getDisplayMetrics());
        }
        super.onInitialize(context);
    }

    @Override
    public View onCreateContentView(Context context) {
        mTextView = new TextView(context);
        mTextView.setTextSize(mBaseTextSize);
        mTextView.setPadding(PADDING, PADDING, PADDING, PADDING);
        mTextView.setTextColor(Color.WHITE);

        return mTextView;
    }

    public void setText(ImageText text) {
        mText = text;
        if (mText != null && mTextView != null) {
            mTextView.setText(mText.getText());
            mTextView.setTextColor(mText.getColor());
        }
    }

    public ImageText getText() {
        return mText;
    }

    @Override
    public void onContentTap() {
        ImageTextEditDialog dialog = getDialog();
        dialog.setCallback(this);
        dialog.setText(mText);
        dialog.show();
    }

    private ImageTextEditDialog getDialog() {
        if (mDialog == null) {
            mDialog = new ImageTextEditDialog(getContext(), this);
        }
        return mDialog;
    }

    @Override
    public void onText(ImageText text) {
        mText = text;
        if (mText != null && mTextView != null) {
            mTextView.setText(mText.getText());
            mTextView.setTextColor(mText.getColor());
        }
    }
}