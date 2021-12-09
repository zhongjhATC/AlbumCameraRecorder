package com.zhongjh.albumcamerarecorder.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.preview.AlbumPreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.recorder.db.RecordingItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.enums.MimeType;

import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.COLLECTION_IMAGE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.COLLECTION_VIDEO;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_COLLECTION_TYPE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_SELECTION;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_MULTIMEDIA_CHOICE;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_MULTIMEDIA_TYPES;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_RECORDING_ITEM;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION_PATH;

/**
 * 多媒体的设置 - Matisse
 *
 * @author zhongjh
 * @date 2018/9/28
 */
public final class MultiMediaSetting {

    private WeakReference<Activity> mContext;
    private WeakReference<Fragment> mFragment;

    private MultiMediaSetting() {

    }

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

    public static MultiMediaSetting init() {
        return new MultiMediaSetting();
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
     * 获取用户确认后的是否选择标记
     *
     * @param data 通过以下方法获取 onActivityResult
     * @return 用户确认后的是否选择标记
     */
    public static boolean obtainMultimediaChoice(Intent data) {
        return data.getBooleanExtra(EXTRA_MULTIMEDIA_CHOICE, false);
    }

    /**
     * 获取用户确认后的多媒体类型
     *
     * @param data 通过以下方法获取 onActivityResult
     * @return 用户确认后的多媒体类型
     */
    public static int obtainMultimediaType(Intent data) {
        return data.getIntExtra(EXTRA_MULTIMEDIA_TYPES, -1);
    }

    /**
     * 获取用户选择/拍照的媒体路径列表 {@link Uri}
     *
     * @param data 通过以下方法获取 onActivityResult
     * @return 用户选择/拍照的媒体路径列表. {@link Uri}
     */
    public static List<Uri> obtainResult(Intent data) {
        return data.getParcelableArrayListExtra(EXTRA_RESULT_SELECTION);
    }

    /**
     * 获取用户选择/拍照的媒体路径列表
     *
     * @param data 通过以下方法获取 onActivityResult
     * @return 用户选择/拍照的媒体路径列表.
     */
    public static List<String> obtainPathResult(Intent data) {
        return data.getStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH);
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

    /**
     * 调用打开图片、视频预览 - 主要用于配合九宫图
     *
     * @param activity 窗体
     * @param requestCode 请求码
     * @param list     数据源
     * @param position 当前数据的索引
     */
    public static void openPreviewData(Activity activity, int requestCode, ArrayList<? extends MultiMedia> list, int position) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(STATE_SELECTION, list);
        bundle.putInt(STATE_COLLECTION_TYPE, COLLECTION_IMAGE);

        Intent intent = new Intent(activity, AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, list.get(position));
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle);
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
        intent.putExtra(BasePreviewActivity.EXTRA_IS_ALLOW_REPEAT, true);
        intent.putExtra(BasePreviewActivity.IS_SELECTED_CHECK, false);
        GlobalSpec globalSpec = GlobalSpec.getInstance();
        activity.startActivityForResult(intent, requestCode);
        if (globalSpec.isCutscenes) {
            activity.overridePendingTransition(R.anim.activity_open, 0);
        }
    }

    /**
     * 调用打开图片预览 - 纯浏览不可操作
     *
     * @param activity 窗体
     * @param list     资源id数据源
     * @param position 当前数据的索引
     */
    public static void openPreviewResourceId(Activity activity, ArrayList<Integer> list, int position) {
        ArrayList<MultiMedia> multiMedias = new ArrayList<>();
        for (Integer item : list) {
            MultiMedia multiMedia = new MultiMedia();
            multiMedia.setDrawableId(item);
            multiMedias.add(multiMedia);
        }
        openPreview(activity, multiMedias, position);
    }

    /**
     * 调用打开图片预览 - 纯浏览不可操作
     *
     * @param activity 窗体
     * @param list     文件地址的数据源
     * @param position 当前数据的索引
     */
    public static void openPreviewPath(Activity activity, ArrayList<String> list, int position) {
        ArrayList<MultiMedia> multiMedias = new ArrayList<>();
        for (String item : list) {
            MultiMedia multiMedia = new MultiMedia();
            multiMedia.setUrl(item);
            multiMedias.add(multiMedia);
        }
        openPreview(activity, multiMedias, position);
    }

    /**
     * 提供给 {@link #openPreviewResourceId} 和 {@link #openPreviewPath} 共用的方法
     *
     * @param activity    窗体
     * @param multiMedias 数据源
     * @param position    当前数据的索引
     */
    private static void openPreview(Activity activity, ArrayList<MultiMedia> multiMedias, int position) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(STATE_SELECTION, multiMedias);
        bundle.putInt(STATE_COLLECTION_TYPE, COLLECTION_IMAGE);

        Intent intent = new Intent(activity, AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, multiMedias.get(position));
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle);
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
        intent.putExtra(BasePreviewActivity.EXTRA_IS_ALLOW_REPEAT, true);
        intent.putExtra(BasePreviewActivity.IS_SELECTED_CHECK, false);
        intent.putExtra(BasePreviewActivity.ENABLE_OPERATION, false);
        GlobalSpec globalSpec = GlobalSpec.getInstance();
        activity.startActivityForResult(intent, globalSpec.requestCode);
        if (globalSpec.isCutscenes) {
            activity.overridePendingTransition(R.anim.activity_open, 0);
        }
    }

}
