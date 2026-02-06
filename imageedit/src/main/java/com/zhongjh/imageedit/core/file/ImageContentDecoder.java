package com.zhongjh.imageedit.core.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;

/**
 * @author zhongjh
 */
public class ImageContentDecoder extends BaseImageDecoder {

    private static final String TAG = "ImageContentDecoder";

    private final Context mContext;

    /** @noinspection unused*/
    public ImageContentDecoder(Context context, Uri uri) {
        super(uri);
        mContext = context;
    }

    /** @noinspection unused*/
    @Override
    public Bitmap decode(BitmapFactory.Options options) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = mContext.getContentResolver().openFileDescriptor(getUri(), "r");
            FileDescriptor fileDescriptor = null;
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            }
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
            return image;
        } catch (Exception e) {
            Log.e(TAG, "decode" + e.getMessage());
        }
        return null;
    }

}
