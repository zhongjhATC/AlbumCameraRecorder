package com.zhongjh.cameraviewsoundrecorder.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.zhongjh.cameraviewsoundrecorder.album.MatissFragment;
import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

import static com.zhongjh.cameraviewsoundrecorder.utils.Constant.EXTRA_RESULT_SELECTION;
import static com.zhongjh.cameraviewsoundrecorder.utils.Constant.EXTRA_RESULT_SELECTION_PATH;

/**
 * 多媒体的设置 - Matisse
 * Created by zhongjh on 2018/9/28.
 */
public final class MultiMedia {

    private final WeakReference<Activity> mContext;
    private final WeakReference<Fragment> mFragment;

    private MultiMedia(Activity activity) {
        this(activity, null);
    }

    private MultiMedia(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private MultiMedia(Activity activity, Fragment fragment) {
        mContext = new WeakReference<>(activity);
        mFragment = new WeakReference<>(fragment);
    }

    /**
     * Start Matisse from an Activity.
     * <p>
     * This Activity's {@link Activity#onActivityResult(int, int, Intent)} will be called when user
     * finishes selecting.
     *
     * @param activity Activity instance.
     * @return Matisse instance.
     */
    public static MultiMedia from(Activity activity) {
        return new MultiMedia(activity);
    }

    /**
     * Start Matisse from a Fragment.
     * <p>
     * This Fragment's {@link Fragment#onActivityResult(int, int, Intent)} will be called when user
     * finishes selecting.
     *
     * @param fragment Fragment instance.
     * @return Matisse instance.
     */
    public static MultiMedia from(Fragment fragment) {
        return new MultiMedia(fragment);
    }

    /**
     * Obtain user selected media' {@link Uri} list in the starting Activity or Fragment.
     *
     * @param data Intent passed by {@link Activity#onActivityResult(int, int, Intent)} or
     *             {@link Fragment#onActivityResult(int, int, Intent)}.
     * @return User selected media' {@link Uri} list.
     */
    public static List<Uri> obtainResult(Intent data) {
        return data.getParcelableArrayListExtra(EXTRA_RESULT_SELECTION);
    }

    /**
     * Obtain user selected media path list in the starting Activity or Fragment.
     *
     * @param data Intent passed by {@link Activity#onActivityResult(int, int, Intent)} or
     *             {@link Fragment#onActivityResult(int, int, Intent)}.
     * @return User selected media path list.
     */
    public static List<String> obtainPathResult(Intent data) {
        return data.getStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH);
    }

    /**
     * Obtain state whether user decide to use selected media in original
     *
     * @param data Intent passed by {@link Activity#onActivityResult(int, int, Intent)} or
     *             {@link Fragment#onActivityResult(int, int, Intent)}.
     * @return Whether use original photo
     */
    public static boolean obtainOriginalState(Intent data) {
        return data.getBooleanExtra(MatissFragment.EXTRA_RESULT_ORIGINAL_ENABLE, false);
    }

    /**
     * MIME types the selection constrains on.
     * <p>
     * Types not included in the set will still be shown in the grid but can't be chosen.
     *
     * @param mimeTypes MIME types set user can choose from.
     * @return {@link AlbumSetting} to build select specifications.
     * @see MimeType
     * @see AlbumSetting
     */
    public AlbumSetting choose(Set<MimeType> mimeTypes) {
        return this.choose(mimeTypes, true);
    }

    /**
     * MIME types the selection constrains on.
     * <p>
     * Types not included in the set will still be shown in the grid but can't be chosen.
     *
     * @param mimeTypes          MIME types set user can choose from.
     * @param mediaTypeExclusive Whether can choose images and videos at the same time during one single choosing
     *                           process. true corresponds to not being able to choose images and videos at the same
     *                           time, and false corresponds to being able to do this.
     * @return {@link AlbumSetting} to build select specifications.
     * @see MimeType
     * @see AlbumSetting
     */
    public AlbumSetting choose(Set<MimeType> mimeTypes, boolean mediaTypeExclusive) {
        return new AlbumSetting(this, mimeTypes, mediaTypeExclusive);
    }

    public CameraSetting chooseCamera(Set<MimeType> mimeTypes){
        return new CameraSetting(mimeTypes);
    }

    @Nullable
    Activity getActivity() {
        return mContext.get();
    }

    @Nullable
    Fragment getFragment() {
        return mFragment != null ? mFragment.get() : null;
    }

}
