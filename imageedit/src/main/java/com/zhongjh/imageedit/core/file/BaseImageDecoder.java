package com.zhongjh.imageedit.core.file;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

/**
 *
 * @author felix
 * @date 2017/12/26 下午2:54
 */
public abstract class BaseImageDecoder {

    private Uri uri;

    public BaseImageDecoder(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Bitmap decode() {
        return decode(null);
    }

    /**
     * 解码
     * @param options 配置
     * @return bitmap
     */
    public abstract Bitmap decode(BitmapFactory.Options options);

}
