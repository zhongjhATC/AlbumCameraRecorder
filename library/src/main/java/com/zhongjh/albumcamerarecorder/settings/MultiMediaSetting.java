package com.zhongjh.albumcamerarecorder.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.zhongjh.albumcamerarecorder.album.MatissFragment;
import com.zhongjh.albumcamerarecorder.album.enums.MimeType;
import com.zhongjh.albumcamerarecorder.recorder.db.RecordingItem;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_MULTIMEDIA_TYPES;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_RECORDING_ITEM;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_SELECTION;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_SELECTION_PATH;

/**
 * 多媒体的设置 - Matisse
 * Created by zhongjh on 2018/9/28.
 */
public final class MultiMediaSetting {

    private final WeakReference<Activity> mContext;
    private final WeakReference<Fragment> mFragment;

    private MultiMediaSetting(Activity activity) {
        this(activity, null);
    }

    private MultiMediaSetting(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private MultiMediaSetting(Activity activity, Fragment fragment) {
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
    public static MultiMediaSetting from(Activity activity) {
        return new MultiMediaSetting(activity);
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
    public static MultiMediaSetting from(Fragment fragment) {
        return new MultiMediaSetting(fragment);
    }

    /**
     * 获取用户确认后的多媒体类型
     *
     * @param data 通过以下方法获取
     *             {@link Activity#onActivityResult(int, int, Intent)} 或者
     *             {@link Fragment#onActivityResult(int, int, Intent)}.
     * @return 用户确认后的多媒体类型
     */
    public static int obtainMultimediaType(Intent data) {
        return data.getIntExtra(EXTRA_MULTIMEDIA_TYPES, -1);
    }

    /**
     * 获取用户选择/拍照的媒体路径列表 {@link Uri}
     *
     * @param data 通过以下方法获取
     *             {@link Activity#onActivityResult(int, int, Intent)} 或者
     *             {@link Fragment#onActivityResult(int, int, Intent)}.
     * @return 用户选择/拍照的媒体路径列表. {@link Uri}
     */
    public static List<Uri> obtainResult(Intent data) {
        return data.getParcelableArrayListExtra(EXTRA_RESULT_SELECTION);
    }

    /**
     * 获取用户选择/拍照的媒体路径列表
     *
     * @param data 通过以下方法获取
     *             {@link Fragment#onActivityResult(int, int, Intent)} 或者
     *             {@link Activity#onActivityResult(int, int, Intent)}
     * @return 用户选择/拍照的媒体路径列表.
     */
    public static List<String> obtainPathResult(Intent data) {
        return data.getStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH);
    }

    /**
     * 获取用户录音的数据
     *
     * @param data 通过以下方法获取
     *             {@link Fragment#onActivityResult(int, int, Intent)} 或者
     *             {@link Activity#onActivityResult(int, int, Intent)}
     * @return 用户录音的数据
     */
    public static RecordingItem obtainRecordingItemResult(Intent data) {
        return data.getParcelableExtra(EXTRA_RESULT_RECORDING_ITEM);
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
     * @return {@link GlobalSetting} to build select specifications.
     * @see MimeType
     * @see GlobalSetting
     */
    public GlobalSetting choose(Set<MimeType> mimeTypes) {
        return new GlobalSetting(this, mimeTypes);
    }

//    /**
//     *
//     * @param mimeTypes
//     * @param mediaTypeExclusive Whether can choose images and videos at the same time during one single choosing
//     *                           process. true corresponds to not being able to choose images and videos at the same
//     *                           time, and false corresponds to being able to do this.
//     * @return
//     */
//    public AlbumSetting chooseAlbum(Set<MimeType> mimeTypes, boolean mediaTypeExclusive) {
//        return new AlbumSetting(mimeTypes, mediaTypeExclusive);
//    }


    @Nullable
    Activity getActivity() {
        return mContext.get();
    }

    @Nullable
    Fragment getFragment() {
        return mFragment != null ? mFragment.get() : null;
    }

}
