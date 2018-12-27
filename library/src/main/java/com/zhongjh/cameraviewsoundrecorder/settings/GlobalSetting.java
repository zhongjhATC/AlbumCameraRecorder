package com.zhongjh.cameraviewsoundrecorder.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;

import com.zhongjh.cameraviewsoundrecorder.MainActivity;
import com.zhongjh.cameraviewsoundrecorder.album.engine.ImageEngine;
import com.zhongjh.cameraviewsoundrecorder.album.engine.impl.GlideEngine;
import com.zhongjh.cameraviewsoundrecorder.album.engine.impl.PicassoEngine;
import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;
import com.zhongjh.cameraviewsoundrecorder.album.filter.Filter;
import com.zhongjh.cameraviewsoundrecorder.album.listener.OnCheckedListener;
import com.zhongjh.cameraviewsoundrecorder.album.listener.OnSelectedListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Set;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_BEHIND;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;

/**
 * 用于构建媒体具体设置 API。
 * Created by zhongjh on 2018/9/28.
 */
public final class GlobalSetting {

    private final MultiMedia mMultiMedia;
    private final GlobalSpec mGlobalSpec;

    // www.代替枚举的@IntDef用法
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @IntDef({
            SCREEN_ORIENTATION_UNSPECIFIED,
            SCREEN_ORIENTATION_LANDSCAPE,
            SCREEN_ORIENTATION_PORTRAIT,
            SCREEN_ORIENTATION_USER,
            SCREEN_ORIENTATION_BEHIND,
            SCREEN_ORIENTATION_SENSOR,
            SCREEN_ORIENTATION_NOSENSOR,
            SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
            SCREEN_ORIENTATION_SENSOR_PORTRAIT,
            SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
            SCREEN_ORIENTATION_REVERSE_PORTRAIT,
            SCREEN_ORIENTATION_FULL_SENSOR,
            SCREEN_ORIENTATION_USER_LANDSCAPE,
            SCREEN_ORIENTATION_USER_PORTRAIT,
            SCREEN_ORIENTATION_FULL_USER,
            SCREEN_ORIENTATION_LOCKED
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ScreenOrientation {
    }

    /**
     * Constructs a new specification builder on the context.
     *
     * @param multiMedia   a requester context wrapper.
     * @param mimeTypes MIME type set to select.
     */
    GlobalSetting(MultiMedia multiMedia, @NonNull Set<MimeType> mimeTypes) {
        mMultiMedia = multiMedia;
        mGlobalSpec = GlobalSpec.getCleanInstance();
        mGlobalSpec.mimeTypeSet = mimeTypes;
        mGlobalSpec.orientation = SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public GlobalSetting cameraSetting(CameraSetting cameraSetting){
        mGlobalSpec.cameraSetting = cameraSetting;
        return this;
    }

    /**
     * Theme for media selecting Activity.
     * <p>
     * There are two built-in themes:
     * 1. com.zhihu.matisse.R.style.Matisse_Zhihu;
     * 2. com.zhihu.matisse.R.style.Matisse_Dracula
     * you can define a custom theme derived from the above ones or other themes.
     *
     * @param themeId theme resource id. Default value is com.zhihu.matisse.R.style.Matisse_Zhihu.
     * @return {@link GlobalSetting} for fluent API.
     */
    public GlobalSetting theme(@StyleRes int themeId) {
        mGlobalSpec.themeId = themeId;
        return this;
    }

    /**
     * Maximum selectable count.
     *
     * @param maxSelectable Maximum selectable count. Default value is 1.
     * @return {@link GlobalSetting} for fluent API.
     */
    public GlobalSetting maxSelectable(int maxSelectable) {
        if (maxSelectable < 1)
            throw new IllegalArgumentException("maxSelectable must be greater than or equal to one");
        mGlobalSpec.maxSelectable = maxSelectable;
        return this;
    }

    /**
     * Capture strategy provided for the location to save photos including internal and external
     * storage and also a authority for {@link android.support.v4.content.FileProvider}.
     *
     * @param captureStrategy {@link CaptureStrategy}, needed only when capturing is enabled.
     * @return {@link GlobalSetting} for fluent API.
     */
    public GlobalSetting captureStrategy(CaptureStrategy captureStrategy) {
        mGlobalSpec.captureStrategy = captureStrategy;
        return this;
    }

    /**
     * Set the desired orientation of this activity.
     *
     * @param orientation An orientation constant as used in {@link ScreenOrientation}.
     *                    Default value is {@link android.content.pm.ActivityInfo#SCREEN_ORIENTATION_PORTRAIT}.
     * @return {@link GlobalSetting} for fluent API.
     * @see Activity#setRequestedOrientation(int)
     */
    public GlobalSetting restrictOrientation(@ScreenOrientation int orientation) {
        mGlobalSpec.orientation = orientation;
        return this;
    }

    /**
     * Start to select media and wait for result.
     *
     * @param requestCode Identity of the request Activity or Fragment.
     */
    public void forResult(int requestCode) {
        Activity activity = mMultiMedia.getActivity();
        if (activity == null) {
            return;
        }

        Intent intent = new Intent(activity, MainActivity.class);

        Fragment fragment = mMultiMedia.getFragment();
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

}
