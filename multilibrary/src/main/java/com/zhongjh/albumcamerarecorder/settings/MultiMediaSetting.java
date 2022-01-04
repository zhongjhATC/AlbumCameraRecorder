package com.zhongjh.albumcamerarecorder.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.entity.RecordingItem;
import com.zhongjh.common.enums.MimeType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;

import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_RECORDING_ITEM;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION_LOCAL_FILE;

/**
 * 多媒体的设置 - Matisse
 *
 * @author zhongjh
 * @date 2018/9/28
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
     * 设置由Activity打开
     *
     * @param activity Activity instance.
     * @return this.
     */
    public static MultiMediaSetting from(Activity activity) {
        return new MultiMediaSetting(activity);
    }

    /**
     * 由Fragment打开
     * <p>
     * 当用户完成选择时，将调用此方法： {@link Fragment#onActivityResult(int, int, Intent)}
     *
     * @param fragment Fragment instance.
     * @return this.
     */
    public static MultiMediaSetting from(Fragment fragment) {
        return new MultiMediaSetting(fragment);
    }

    /**
     * 获取用户确认后的LocalFile多媒体数据
     *
     * @param data 通过onActivityResult方法获取
     * @return 用户选择/拍照的媒体数据. {@link LocalFile}
     */
    public static ArrayList<LocalFile> obtainLocalFileResult(Intent data) {
        return data.getParcelableExtra(EXTRA_RESULT_SELECTION_LOCAL_FILE);
    }

    /**
     * 获取用户确认后的MultiMedia多媒体数据
     *
     * @param data 通过onActivityResult方法获取
     * @return 用户通过直接调用预览界面然后返回的媒体数据. {@link MultiMedia}
     */
    public static ArrayList<MultiMedia> obtainMultiMediaResult(Intent data) {
        // 请求的预览界面
        Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
        // 获取选择的数据
        return resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
    }

    /**
     * 获取用户录音的数据
     *
     * @param data 通过以下方法获取 onActivityResult
     * @return 用户录音的数据
     */
    public static RecordingItem obtainRecordingItemResult(Intent data) {
        return data.getParcelableExtra(EXTRA_RESULT_RECORDING_ITEM);
    }

    /**
     * 设置支持的类型
     * <p>
     * 未包含在集合中的类型仍将显示在网格中，但无法选择。
     *
     * @param mimeTypes 类型
     * @return {@link GlobalSetting} this
     * @see MimeType
     * @see GlobalSetting
     */
    public GlobalSetting choose(Set<MimeType> mimeTypes) {
        return new GlobalSetting(this, mimeTypes);
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
