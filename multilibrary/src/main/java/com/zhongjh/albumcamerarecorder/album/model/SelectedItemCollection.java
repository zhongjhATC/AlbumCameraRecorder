package com.zhongjh.albumcamerarecorder.album.model;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.IncapableCause;
import com.zhongjh.albumcamerarecorder.album.entity.Item;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.albumcamerarecorder.album.widget.CheckView;
import com.zhongjh.albumcamerarecorder.utils.PathUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 选择的数据源
 * Created by zhongjh on 2018/8/28.
 */
public class SelectedItemCollection {

    public static final String STATE_SELECTION = "state_selection"; // 数据源的标记
    public static final String STATE_COLLECTION_TYPE = "state_collection_type";

    /**
     * 空的数据类型
     */
    public static final int COLLECTION_UNDEFINED = 0x00;
    /**
     * 图像数据类型
     */
    public static final int COLLECTION_IMAGE = 0x01;
    /**
     * 视频数据类型
     */
    public static final int COLLECTION_VIDEO = 0x01 << 1;
    /**
     * 图像和视频混合类型
     */
    private static final int COLLECTION_MIXED = COLLECTION_IMAGE | COLLECTION_VIDEO;

    private final Context mContext;
    private Set<Item> mItems;       // 数据源
    private int mCollectionType = COLLECTION_UNDEFINED; // 类型

    public SelectedItemCollection(Context context) {
        mContext = context;
    }

    public void onCreate(Bundle bundle) {
        if (bundle == null) {
            mItems = new LinkedHashSet<>();
        } else {
            // 获取缓存的数据
            List<Item> saved = bundle.getParcelableArrayList(STATE_SELECTION);
            mItems = new LinkedHashSet<>(saved);
            mCollectionType = bundle.getInt(STATE_COLLECTION_TYPE, COLLECTION_UNDEFINED);
        }
    }

    /**
     * 缓存数据
     *
     * @param outState 缓存
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_SELECTION, new ArrayList<>(mItems));
        outState.putInt(STATE_COLLECTION_TYPE, mCollectionType);
    }

    /**
     * 将数据保存进Bundle并且返回
     *
     * @return Bundle
     */
    public Bundle getDataWithBundle() {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(STATE_SELECTION, new ArrayList<>(mItems));
        bundle.putInt(STATE_COLLECTION_TYPE, mCollectionType);
        return bundle;
    }

    /**
     * 将资源对象添加到已选中集合
     *
     * @param item 数据
     */
    public boolean add(Item item) {
        if (typeConflict(item)) {
            throw new IllegalArgumentException("Can't select images and videos at the same time.");
        }
        boolean added = mItems.add(item);
        // 如果只选中了图片Item， mCollectionType设置为COLLECTION_IMAGE
        // 如果只选中了图片影音资源，mCollectionType设置为COLLECTION_IMAGE
        // 如果两种都选择了，mCollectionType设置为COLLECTION_MIXED
        if (added) {
            // 如果是空的数据源
            if (mCollectionType == COLLECTION_UNDEFINED) {
                if (item.isImage()) {
                    // 如果是图片，就设置图片类型
                    mCollectionType = COLLECTION_IMAGE;
                } else if (item.isVideo()) {
                    // 如果是视频，就设置视频类型
                    mCollectionType = COLLECTION_VIDEO;
                }
            } else if (mCollectionType == COLLECTION_IMAGE) {
                // 如果当前是图片类型
                if (item.isVideo()) {
                    // 选择了视频，就设置混合模式
                    mCollectionType = COLLECTION_MIXED;
                }
            } else if (mCollectionType == COLLECTION_VIDEO) {
                // 如果当前是图片类型
                if (item.isImage()) {
                    // 选择了图片，就设置混合模式
                    mCollectionType = COLLECTION_MIXED;
                }
            }
        }
        return added;
    }

    /**
     * 删除数据源某项
     *
     * @param item 数据
     * @return 是否删除成功
     */
    public boolean remove(Item item) {
        boolean removed = mItems.remove(item);
        if (removed) {
            if (mItems.size() == 0) {
                // 如果删除后没有数据，设置当前类型为空
                mCollectionType = COLLECTION_UNDEFINED;
            } else {
                if (mCollectionType == COLLECTION_MIXED) {
                    currentMaxSelectable();
                }
            }
        }
        return removed;
    }


    /**
     * 重置数据源
     *
     * @param items          数据源
     * @param collectionType 类型
     */
    public void overwrite(ArrayList<Item> items, int collectionType) {
        if (items.size() == 0) {
            mCollectionType = COLLECTION_UNDEFINED;
        } else {
            mCollectionType = collectionType;
        }
        mItems.clear();
        mItems.addAll(items);
    }

    /**
     * 转换成list
     *
     * @return list<Item>
     */
    public List<Item> asList() {
        return new ArrayList<>(mItems);
    }

    /**
     * 获取uri的集合
     *
     * @return list<Uri>
     */
    public List<Uri> asListOfUri() {
        List<Uri> uris = new ArrayList<>();
        for (Item item : mItems) {
            uris.add(item.getUri());
        }
        return uris;
    }

    /**
     * 获取path的集合
     *
     * @return list<path>
     */
    public List<String> asListOfString() {
        List<String> paths = new ArrayList<>();
        for (Item item : mItems) {
            paths.add(PathUtils.getPath(mContext, item.getUri()));
        }
        return paths;
    }

    /**
     * 该item是否在选择中
     *
     * @param item 数据源
     * @return 返回是否选择
     */
    public boolean isSelected(Item item) {
        return mItems.contains(item);
    }

    /**
     * 验证当前item是否满足可以被选中的条件
     *
     * @param item 数据源
     * @return 弹窗
     */
    public IncapableCause isAcceptable(Item item) {
        // 检查是否超过最大设置数量
        if (maxSelectableReached()) {
            int maxSelectable = currentMaxSelectable();
            String cause;

            try {
                cause = mContext.getResources().getString(
                        R.string.error_over_count,
                        maxSelectable
                );
            } catch (Resources.NotFoundException e) {
                cause = mContext.getString(
                        R.string.error_over_count,
                        maxSelectable
                );
            } catch (NoClassDefFoundError e) {
                cause = mContext.getString(
                        R.string.error_over_count,
                        maxSelectable
                );
            }
            // 生成窗口
            return new IncapableCause(cause);
        } else if (typeConflict(item)) {
            // 判断选择资源(图片跟视频)是否类型冲突
            return new IncapableCause(mContext.getString(R.string.error_type_conflict));
        }

        // 过滤文件
        return PhotoMetadataUtils.isAcceptable(mContext, item);
    }

    /**
     * 当前数量 和 当前选择最大数量比较 是否相等
     *
     * @return boolean
     */
    public boolean maxSelectableReached() {
        return mItems.size() == currentMaxSelectable();
    }

    /**
     * 返回最多选择的数量
     *
     * @return 数量
     */
    private int currentMaxSelectable() {
        GlobalSpec spec = GlobalSpec.getInstance();
        int leastCount;
        if (mCollectionType == COLLECTION_IMAGE) {
            leastCount = spec.maxImageSelectable;
        } else if (mCollectionType == COLLECTION_VIDEO) {
            leastCount = spec.maxVideoSelectable;
        } else {
            // 视频+语音
            leastCount = spec.maxImageSelectable + spec.maxVideoSelectable;
        }
        return leastCount;
    }

    /**
     * 判断选择资源(图片跟视频)是否类型冲突
     * Determine whether there will be conflict media types. A user can only select images and videos at the same time
     * while {@link AlbumSpec#mediaTypeExclusive} is set to false.
     */
    private boolean typeConflict(Item item) {
        // 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
        return AlbumSpec.getInstance().mediaTypeExclusive
                && ((item.isImage() && (mCollectionType == COLLECTION_VIDEO || mCollectionType == COLLECTION_MIXED))
                || (item.isVideo() && (mCollectionType == COLLECTION_IMAGE || mCollectionType == COLLECTION_MIXED)));
    }

    /**
     * 获取数据源长度
     *
     * @return 数据源长度
     */
    public int count() {
        return mItems.size();
    }

    /**
     * 返回选择的num
     *
     * @param item 数据
     * @return 选择的索引，最终返回的选择了第几个
     */
    public int checkedNumOf(Item item) {
        // 获取选择的第几个
        int index = new ArrayList<>(mItems).indexOf(item);
        // 如果选择的为 -1 就是未选状态，否则选择基础数量+1
        return index == -1 ? CheckView.UNCHECKED : index + 1;
    }


}
