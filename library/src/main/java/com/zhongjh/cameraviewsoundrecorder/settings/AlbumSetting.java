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
public final class AlbumSetting {

    private final MultiMedia mMultiMedia;
    private final AlbumSpec mAlbumSpec;

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
    AlbumSetting(MultiMedia multiMedia, @NonNull Set<MimeType> mimeTypes, boolean mediaTypeExclusive) {
        mMultiMedia = multiMedia;
        mAlbumSpec = AlbumSpec.getCleanInstance();
        mAlbumSpec.mimeTypeSet = mimeTypes;
        mAlbumSpec.mediaTypeExclusive = mediaTypeExclusive;
        mAlbumSpec.orientation = SCREEN_ORIENTATION_UNSPECIFIED;
    }

    /**
     * Whether to show only one media type if choosing medias are only images or videos.
     *
     * @param showSingleMediaType whether to show only one media type, either images or videos.
     * @return {@link AlbumSetting} for fluent API.
     * @see AlbumSpec#onlyShowImages()
     * @see AlbumSpec#onlyShowVideos()
     */
    public AlbumSetting showSingleMediaType(boolean showSingleMediaType) {
        mAlbumSpec.showSingleMediaType = showSingleMediaType;
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
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting theme(@StyleRes int themeId) {
        mAlbumSpec.themeId = themeId;
        return this;
    }

    /**
     * Show a auto-increased number or a check mark when user select media.
     *
     * @param countable true for a auto-increased number from 1, false for a check mark. Default
     *                  value is false.
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting countable(boolean countable) {
        mAlbumSpec.countable = countable;
        return this;
    }

    /**
     * Maximum selectable count.
     *
     * @param maxSelectable Maximum selectable count. Default value is 1.
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting maxSelectable(int maxSelectable) {
        if (maxSelectable < 1)
            throw new IllegalArgumentException("maxSelectable must be greater than or equal to one");
        if (mAlbumSpec.maxImageSelectable > 0 || mAlbumSpec.maxVideoSelectable > 0)
            throw new IllegalStateException("already set maxImageSelectable and maxVideoSelectable");
        mAlbumSpec.maxSelectable = maxSelectable;
        return this;
    }

    /**
     * Only useful when {@link AlbumSpec#mediaTypeExclusive} set true and you want to set different maximum
     * selectable files for image and video media types.
     *
     * @param maxImageSelectable Maximum selectable count for image.
     * @param maxVideoSelectable Maximum selectable count for video.
     * @return
     */
    public AlbumSetting maxSelectablePerMediaType(int maxImageSelectable, int maxVideoSelectable) {
        if (maxImageSelectable < 1 || maxVideoSelectable < 1)
            throw new IllegalArgumentException(("max selectable must be greater than or equal to one"));
        mAlbumSpec.maxSelectable = -1;
        mAlbumSpec.maxImageSelectable = maxImageSelectable;
        mAlbumSpec.maxVideoSelectable = maxVideoSelectable;
        return this;
    }

    /**
     * Add filter to filter each selecting item.
     *
     * @param filter {@link Filter}
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting addFilter(@NonNull Filter filter) {
        if (mAlbumSpec.filters == null) {
            mAlbumSpec.filters = new ArrayList<>();
        }
        if (filter == null) throw new IllegalArgumentException("filter cannot be null");
        mAlbumSpec.filters.add(filter);
        return this;
    }

    /**
     * Determines whether the photo capturing is enabled or not on the media grid view.
     * <p>
     * If this value is set true, photo capturing entry will appear only on All Media's page.
     *
     * @param enable Whether to enable capturing or not. Default value is false;
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting capture(boolean enable) {
        mAlbumSpec.capture = enable;
        return this;
    }

    /**
     * Show a original photo check options.Let users decide whether use original photo after select
     *
     * @param enable Whether to enable original photo or not
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting originalEnable(boolean enable) {
        mAlbumSpec.originalable = enable;
        return this;
    }

    /**
     * Maximum original size,the unit is MB. Only useful when {link@originalEnable} set true
     *
     * @param size Maximum original size. Default value is Integer.MAX_VALUE
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting maxOriginalSize(int size) {
        mAlbumSpec.originalMaxSize = size;
        return this;
    }

    /**
     * Capture strategy provided for the location to save photos including internal and external
     * storage and also a authority for {@link android.support.v4.content.FileProvider}.
     *
     * @param captureStrategy {@link CaptureStrategy}, needed only when capturing is enabled.
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting captureStrategy(CaptureStrategy captureStrategy) {
        mAlbumSpec.captureStrategy = captureStrategy;
        return this;
    }

    /**
     * Set the desired orientation of this activity.
     *
     * @param orientation An orientation constant as used in {@link ScreenOrientation}.
     *                    Default value is {@link android.content.pm.ActivityInfo#SCREEN_ORIENTATION_PORTRAIT}.
     * @return {@link AlbumSetting} for fluent API.
     * @see Activity#setRequestedOrientation(int)
     */
    public AlbumSetting restrictOrientation(@ScreenOrientation int orientation) {
        mAlbumSpec.orientation = orientation;
        return this;
    }

    /**
     * Set a fixed span count for the media grid. Same for different screen orientations.
     * <p>
     * This will be ignored when {@link #gridExpectedSize(int)} is set.
     *
     * @param spanCount Requested span count.
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting spanCount(int spanCount) {
        if (spanCount < 1) throw new IllegalArgumentException("spanCount cannot be less than 1");
        mAlbumSpec.spanCount = spanCount;
        return this;
    }

    /**
     * Set expected size for media grid to adapt to different screen sizes. This won't necessarily
     * be applied cause the media grid should fill the view container. The measured media grid's
     * size will be as close to this value as possible.
     *
     * @param size Expected media grid size in pixel.
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting gridExpectedSize(int size) {
        mAlbumSpec.gridExpectedSize = size;
        return this;
    }

    /**
     * Photo thumbnail's scale compared to the View's size. It should be a float value in (0.0,
     * 1.0].
     *
     * @param scale Thumbnail's scale in (0.0, 1.0]. Default value is 0.5.
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting thumbnailScale(float scale) {
        if (scale <= 0f || scale > 1f)
            throw new IllegalArgumentException("Thumbnail scale must be between (0.0, 1.0]");
        mAlbumSpec.thumbnailScale = scale;
        return this;
    }

    /**
     * Provide an image engine.
     * <p>
     * There are two built-in image engines:
     * 1. {@link GlideEngine}
     * 2. {@link PicassoEngine}
     * And you can implement your own image engine.
     *
     * @param imageEngine {@link ImageEngine}
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting imageEngine(ImageEngine imageEngine) {
        mAlbumSpec.imageEngine = imageEngine;
        return this;
    }

    /**
     * Set listener for callback immediately when user select or unselect something.
     * <p>
     * It's a redundant API with {@link MultiMedia#obtainResult(Intent)},
     * we only suggest you to use this API when you need to do something immediately.
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link AlbumSetting} for fluent API.
     */
    @NonNull
    public AlbumSetting setOnSelectedListener(@Nullable OnSelectedListener listener) {
        mAlbumSpec.onSelectedListener = listener;
        return this;
    }

    /**
     * Set listener for callback immediately when user check or uncheck original.
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link AlbumSetting} for fluent API.
     */
    public AlbumSetting setOnCheckedListener(@Nullable OnCheckedListener listener) {
        mAlbumSpec.onCheckedListener = listener;
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
