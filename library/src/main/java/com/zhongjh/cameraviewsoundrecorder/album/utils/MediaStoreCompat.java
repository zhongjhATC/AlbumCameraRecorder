package com.zhongjh.cameraviewsoundrecorder.album.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;

import java.lang.ref.WeakReference;

/**
 *
 * Created by zhongjh on 2018/8/23.
 */
public class MediaStoreCompat {

    private final WeakReference<Activity> mContext;
    private final WeakReference<Fragment> mFragment;

    public MediaStoreCompat(Activity activity) {
        mContext = new WeakReference<>(activity);
        mFragment = null;
    }

    public MediaStoreCompat(Activity activity, Fragment fragment) {
        mContext = new WeakReference<>(activity);
        mFragment = new WeakReference<>(fragment);
    }

    /**
     * 检查设备是否具有相机特性。
     *
     * @param context 检查相机特征的上下文。
     * @return 如果设备具有相机特性，则为真。否则为假。
     */
    public static boolean hasCameraFeature(Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

}
