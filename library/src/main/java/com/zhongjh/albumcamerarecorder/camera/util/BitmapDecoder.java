//package com.zhongjh.albumcamerarecorder.camera.util;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
//import android.media.ExifInterface;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.zhongjh.albumcamerarecorder.camera.entity.BitmapSize;
//import com.zhongjh.albumcamerarecorder.camera.entity.ExifInfo;
//
//import java.io.File;
//import java.io.IOException;
//
///**
// * Created by zhongjh on 2019/1/2.
// */
//
//public class BitmapDecoder {
//
//    /**
//     * file转换成bitmap
//     *
//     * @param file
//     * @param maxSize
//     * @param config
//     * @return
//     */
//    public static Bitmap decodeSampledBitmapFromFile(File file, BitmapSize maxSize, Bitmap.Config config) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(file.getPath(), options);
//        options.inSampleSize = calculateInSampleSize(options, maxSize.getWidth(), maxSize.getHeight());
//        options.inJustDecodeBounds = false;
//        if (config != null) {
//            options.inPreferredConfig = config;
//        }
//        try {
//            return decodeSampledBitmap(BitmapFactory.decodeFile(file.getPath(), options), options, file.getPath());//是否需要翻转
//        } catch (Throwable e) {
//            Log.e("http", e.getMessage(), e);
//            return null;
//        }
//    }
//
//    public static int calculateInSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight) {
//        int height = options.outHeight;
//        int width = options.outWidth;
//        int inSampleSize = 1;
//
//        if (width > maxWidth || height > maxHeight) {
//            if (width > height) {
//                inSampleSize = Math.round((float) height / (float) maxHeight);
//            } else {
//                inSampleSize = Math.round((float) width / (float) maxWidth);
//            }
//
//            final float totalPixels = width * height;
//
//            final float maxTotalPixels = maxWidth * maxHeight * 2;
//
//            while (totalPixels / (inSampleSize * inSampleSize) > maxTotalPixels) {
//                inSampleSize++;
//            }
//        }
//        return inSampleSize;
//    }
//
//    public static Bitmap decodeSampledBitmap(Bitmap subsampledBitmap, BitmapFactory.Options options, String imageUri) throws IOException {
//        if (options != null && !TextUtils.isEmpty(options.outMimeType) && options.outMimeType.startsWith("image/")) {
//            ExifInfo exifInfo = defineExifOrientation(imageUri);
//            if (exifInfo != null && (exifInfo.flipHorizontal || exifInfo.rotation != 0)) {
//                return considerExactScaleAndOrientatiton(subsampledBitmap, exifInfo);
//            }
//        }
//        return subsampledBitmap;
//    }
//
//    public static Bitmap considerExactScaleAndOrientatiton(Bitmap subsampledBitmap, ExifInfo exifInfo) {
//        if (exifInfo != null) {
//            Matrix m = new Matrix();
//            // Flip bitmap if need
//            if (exifInfo.flipHorizontal) {
//                m.postScale(-1, 1);
//            }
//            // Rotate bitmap if need
//            if (exifInfo.rotation != 0) {
//                m.postRotate(exifInfo.rotation);
//            }
//            Bitmap finalBitmap = Bitmap.createBitmap(subsampledBitmap, 0, 0, subsampledBitmap.getWidth(), subsampledBitmap.getHeight(), m, true);
//            if (finalBitmap != subsampledBitmap) {
//                subsampledBitmap.recycle();
//            }
//            return finalBitmap;
//        }
//        return subsampledBitmap;
//    }
//
//    public static ExifInfo defineExifOrientation(String imageUri) {
//        int rotation = 0;
//        boolean flip = false;
//        try {
//            ExifInterface exif = new ExifInterface(imageUri);
//            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//            switch (exifOrientation) {
//                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
//                    flip = true;
//                case ExifInterface.ORIENTATION_NORMAL:
//                    rotation = 0;
//                    break;
//                case ExifInterface.ORIENTATION_TRANSVERSE:
//                    flip = true;
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    rotation = 90;
//                    break;
//                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
//                    flip = true;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    rotation = 180;
//                    break;
//                case ExifInterface.ORIENTATION_TRANSPOSE:
//                    flip = true;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    rotation = 270;
//                    break;
//            }
//        } catch (Exception e) {
//            Log.e("BitmapDecoder", "图片转换失败" + imageUri);
//        }
//        return new ExifInfo(rotation, flip);
//    }
//
//}
