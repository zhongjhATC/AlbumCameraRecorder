package com.zhongjh.imageedit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.zhongjh.imageedit.core.ImageMode;
import com.zhongjh.imageedit.core.ImageText;
import com.zhongjh.imageedit.core.file.BaseImageDecoder;
import com.zhongjh.imageedit.core.file.ImageAssetFileDecoder;
import com.zhongjh.imageedit.core.file.ImageContentDecoder;
import com.zhongjh.imageedit.core.file.ImageFileDecoder;
import com.zhongjh.imageedit.core.util.BitmapLoadUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author felix
 * @date 2017/11/14 下午2:26
 */
public class ImageEditActivity extends BaseImageEditActivity {

    private static final int MAX_WIDTH = 1024;

    private static final int MAX_HEIGHT = 1024;

    public static final String EXTRA_IMAGE_URI = "IMAGE_URI";

    public static final String EXTRA_IMAGE_SAVE_PATH = "IMAGE_SAVE_PATH";

    public static final String EXTRA_IMAGE_SCREEN_ORIENTATION = "EXTRA_SCREEN_ORIENTATION";

    public static final String EXTRA_WIDTH = "EXTRA_WIDTH";
    public static final String EXTRA_HEIGHT = "EXTRA_HEIGHT";

    @Override
    public void onCreated() {

    }

    @Override
    public Bitmap getBitmap() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }

        Uri uri = intent.getParcelableExtra(EXTRA_IMAGE_URI);
        if (uri == null) {
            return null;
        }

        BaseImageDecoder decoder = null;
        switch (uri.getScheme()) {
            case "asset":
                decoder = new ImageAssetFileDecoder(this, uri);
                break;
            case "file":
                decoder = new ImageFileDecoder(uri);
                break;
            case "content":
                decoder = new ImageContentDecoder(this, uri);
                break;
            default:
                break;
        }

        if (decoder == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;

        decoder.decode(options);
        // 按照成倍的缩小
        int maxBitmapSize = BitmapLoadUtils.calculateMaxBitmapSize(getApplicationContext());
        options.inSampleSize = BitmapLoadUtils.calculateInSampleSize(options, maxBitmapSize, maxBitmapSize);

        options.inJustDecodeBounds = false;

        return decoder.decode(options);
    }

    @Override
    public void onText(ImageText text) {
        mImageViewCustom.addStickerText(text);
    }

    @Override
    public void onModeClick(ImageMode mode) {
        ImageMode cm = mImageViewCustom.getMode();
        if (cm == mode) {
            mode = ImageMode.NONE;
        }
        mImageViewCustom.setMode(mode);
        updateModeUi();

        if (mode == ImageMode.CLIP) {
            setOpDisplay(OP_CLIP);
        }
    }

    @Override
    public void onUndoClick() {
        ImageMode mode = mImageViewCustom.getMode();
        if (mode == ImageMode.DOODLE) {
            mImageViewCustom.undoDoodle();
        } else if (mode == ImageMode.MOSAIC) {
            mImageViewCustom.undoMosaic();
        }
    }

    @Override
    public void onCancelClick() {
        finish();
    }

    @Override
    public void onDoneClick() {
        String path = getIntent().getStringExtra(EXTRA_IMAGE_SAVE_PATH);
        if (!TextUtils.isEmpty(path)) {
            Bitmap bitmap = mImageViewCustom.saveBitmap();
            if (bitmap != null) {
                FileOutputStream fout = null;
                try {
                    fout = new FileOutputStream(path);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fout);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (fout != null) {
                        try {
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Intent intent = new Intent();
                intent.putExtra(EXTRA_WIDTH, bitmap.getWidth());
                intent.putExtra(EXTRA_HEIGHT, bitmap.getHeight());
                setResult(RESULT_OK, intent);
                finish();
                return;
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onCancelClipClick() {
        mImageViewCustom.cancelClip();
        setOpDisplay(mImageViewCustom.getMode() == ImageMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onDoneClipClick() {
        mImageViewCustom.doClip();
        setOpDisplay(mImageViewCustom.getMode() == ImageMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onResetClipClick() {
        mImageViewCustom.resetClip();
    }

    @Override
    public void onRotateClipClick() {
        mImageViewCustom.doRotate();
    }

    @Override
    public void onColorChanged(int checkedColor) {
        if (mImageViewCustom.getMode() != ImageMode.DOODLE) {
            onModeClick(ImageMode.DOODLE);
        }
        mImageViewCustom.setPenColor(checkedColor);
    }
}
