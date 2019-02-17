package com.zhongjh.albumcamerarecorder.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.zhongjh.albumcamerarecorder.album.entity.Item;
import com.zhongjh.albumcamerarecorder.album.enums.MimeType;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.preview.AlbumPreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.SelectedPreviewActivity;
import com.zhongjh.albumcamerarecorder.recorder.db.RecordingItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.zhongjh.albumcamerarecorder.album.MatissFragment.REQUEST_CODE_PREVIEW;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.COLLECTION_IMAGE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_COLLECTION_TYPE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_SELECTION;
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
     *  由Fragment打开
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
     * 调用打开多个大图
     * @param activity 窗体
     * @param requestCode 请求码
     * @param list 需要显示的大图
     */
    public static void openPreviewImage(Activity activity,List<Uri> list) {
        // 转换成items
        ArrayList<Item> items = new ArrayList<>();
        for (Uri value : list) {
            Item item = new Item(value);
            items.add(item);
        }
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(STATE_SELECTION, items);
        bundle.putInt(STATE_COLLECTION_TYPE, COLLECTION_IMAGE);


        Intent intent = new Intent(activity, SelectedPreviewActivity.class);
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle);
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
        activity.startActivityForResult(intent, REQUEST_CODE_PREVIEW);
    }

//    public static void openPreviewSiginImage(Activity activity,int requestCode,Uri list) {
//        // 转换成items
//        ArrayList<Item> items = new ArrayList<>();
//        for (Uri value : list) {
//            Item item = new Item(value);
//            items.add(item);
//        }
//        Bundle bundle = new Bundle();
//        bundle.putParcelableArrayList(STATE_SELECTION, items);
//        bundle.putInt(STATE_COLLECTION_TYPE, COLLECTION_IMAGE);
//
//
//    Intent intent = new Intent(activity, AlbumPreviewActivity.class);
//        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);
//        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
//        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
//    startActivityForResult(intent, REQUEST_CODE_PREVIEW);
//    }




}
