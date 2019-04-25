package com.zhongjh.albumcamerarecorder.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import gaode.zhongjh.com.common.entity.MultiMedia;
import gaode.zhongjh.com.common.enums.MimeType;

import com.zhongjh.albumcamerarecorder.preview.AlbumPreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.recorder.db.RecordingItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.COLLECTION_IMAGE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.COLLECTION_VIDEO;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_COLLECTION_TYPE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_SELECTION;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_MULTIMEDIA_CHOICE;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_MULTIMEDIA_TYPES;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_RECORDING_ITEM;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_SELECTION;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_SELECTION_PATH;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.REQUEST_CODE_PREVIEW;

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
     * 由Activity打开
     * <p>
     * 当用户完成选择时，将调用此方法：{@link Activity#onActivityResult(int, int, Intent)}
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
     * 获取用户确认后的是否选择标记
     * @param data 通过以下方法获取
     *             {@link Activity#onActivityResult(int, int, Intent)} 或者
     *             {@link Fragment#onActivityResult(int, int, Intent)}.
     * @return 用户确认后的是否选择标记
     */
    public static boolean obtainMultimediaChoice(Intent data){
        return data.getBooleanExtra(EXTRA_MULTIMEDIA_CHOICE, false);
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

    /**
     * 调用打开图片
     *
     * @param activity 窗体
     * @param list     数据源
     * @param position 当前数据的索引
     */
    public static void openPreviewImage(Activity activity, ArrayList<MultiMedia> list, int position) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(STATE_SELECTION, list);
        bundle.putInt(STATE_COLLECTION_TYPE, COLLECTION_IMAGE);

        Intent intent = new Intent(activity, AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, list.get(position));
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle);
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
        intent.putExtra(BasePreviewActivity.EXTRA_IS_ALLOW_REPEAT, true);
        intent.putExtra(BasePreviewActivity.IS_SELECTED_CHECK,false);
        activity.startActivityForResult(intent, REQUEST_CODE_PREVIEW);
    }

    /**
     * 调用打开单个视频
     *
     * @param activity 窗体
     * @param list     需要显示的大图
     */
    public static void openPreviewVideo(Activity activity, ArrayList<MultiMedia> list) {
        // 转换成items
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(STATE_SELECTION, list);
        bundle.putInt(STATE_COLLECTION_TYPE, COLLECTION_VIDEO);

        Intent intent = new Intent(activity, AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, list.get(0));
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle);
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
        intent.putExtra(BasePreviewActivity.ENABLE_OPERATION, false);
        intent.putExtra(BasePreviewActivity.IS_SELECTED_CHECK,false);
        activity.startActivityForResult(intent, REQUEST_CODE_PREVIEW);

    }



}
