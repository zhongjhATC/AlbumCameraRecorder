package com.zhongjh.imageedit.core.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;

public class IMGContentDecoder extends IMGDecoder {

    private Context mContext;

    public IMGContentDecoder(Context context, Uri uri) {
        super(uri);
        mContext = context;
    }

    @Override
    public Bitmap decode(BitmapFactory.Options options) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    mContext.getContentResolver().openFileDescriptor(getUri(), "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
