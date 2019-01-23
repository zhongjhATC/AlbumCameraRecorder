package com.zhongjh.albumcamerarecorder.camera.util;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.zhongjh.albumcamerarecorder.camera.common.Constants;

/**
 * 获取手机分辨率的宽高，先从缓存获取，如果缓存都没有，则重新计算，并且保存
 * Created by zhongjh on 2017/10/25.
 */
public class DisplayMetricsSPUtils {

    private static final String CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCES = "cameraviewsoundrecorderSharedPreferences";

    /**
     * 获取屏幕分辨率-高
     *
     * @param context 上下文
     * @return 高
     */
    public static int getScreenHeight(Context context) {
        SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(context, CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCES);
        if (sharedPreferencesUtil.getInt(Constants.ScreenHeight, 0) == 0) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm == null)
                return 0;
            Point size = new Point();
            wm.getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;
            sharedPreferencesUtil.putInt(Constants.ScreenHeight, screenHeight);
            sharedPreferencesUtil.putInt(Constants.ScreenWidth, screenWidth);
        }
        return sharedPreferencesUtil.getInt(Constants.ScreenHeight, 0);
    }

    /**
     * 获取屏幕分辨率- 宽
     *
     * @param context 上下文
     * @return 宽
     */
    public static int getScreenWidth(Context context) {
        SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(context, CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCES);
        if (sharedPreferencesUtil.getInt(Constants.ScreenWidth, 0) == 0) {
//            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            Point size = new Point();
//            wm.getDefaultDisplay().getSize(size);
//            int screenWidth = size.x;
//            int screenHeight = size.y;
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;
            sharedPreferencesUtil.putInt(Constants.ScreenHeight, screenHeight);
            sharedPreferencesUtil.putInt(Constants.ScreenWidth, screenWidth);
        }
        return sharedPreferencesUtil.getInt(Constants.ScreenWidth, 0);
    }

}
