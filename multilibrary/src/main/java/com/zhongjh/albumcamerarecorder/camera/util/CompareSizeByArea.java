//package com.zhongjh.albumcamerarecorder.camera.util;
//
//import android.os.Build;
//import android.support.annotation.RequiresApi;
//import android.util.Size;
//
//
//public class CompareSizeByArea implements java.util.Comparator<Size> {
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public int compare(Size lhs, Size rhs) {
//        // We cast here to ensure the multiplications won't overflow
//        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
//                (long) rhs.getWidth() * rhs.getHeight());
//    }
//}
