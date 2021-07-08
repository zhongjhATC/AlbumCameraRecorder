package gaode.zhongjh.com.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;

/**
 * @author zhongjh
 */
public class StatusBarUtils {

    public static void initStatusBar(Activity activity) {
        // 设置状态栏为透明并且为全屏模式
        int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            window.setAttributes(attributes);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            Window window = activity.getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.flags |= flagTranslucentStatus;
            window.setAttributes(attributes);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }


    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

}
