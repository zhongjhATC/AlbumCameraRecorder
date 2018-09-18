package com.zhongjh.cameraviewsoundrecorder.album.model;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.zhongjh.cameraviewsoundrecorder.album.entity.Item;
import com.zhongjh.cameraviewsoundrecorder.album.entity.SelectionSpec;
import com.zhongjh.cameraviewsoundrecorder.album.widget.CheckView;
import com.zhongjh.cameraviewsoundrecorder.utils.PathUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * Created by zhongjh on 2018/8/28.
 */
public class SelectedItemCollection {

    public static final String STATE_SELECTION = "state_selection"; // 数据源的标记
    public static final String STATE_COLLECTION_TYPE = "state_collection_type";

    /**
     * Empty collection
     */
    public static final int COLLECTION_UNDEFINED = 0x00;
    /**
     * Collection only with images
     */
    public static final int COLLECTION_IMAGE = 0x01;
    /**
     * Collection only with videos
     */
    public static final int COLLECTION_VIDEO = 0x01 << 1;

    private final Context mContext;
    private Set<Item> mItems;       // 数据源
    private int mCollectionType = COLLECTION_UNDEFINED; // 类型

    public SelectedItemCollection(Context context) {
        mContext = context;
    }

    public void onCreate(Bundle bundle) {
        if (bundle == null){
            mItems = new LinkedHashSet<>();
        }else{
            // 获取缓存的数据
            List<Item> saved = bundle.getParcelableArrayList(STATE_SELECTION);
            mItems = new LinkedHashSet<>(saved);
            mCollectionType = bundle.getInt(STATE_COLLECTION_TYPE, COLLECTION_UNDEFINED);
        }
    }

    /**
     * 缓存数据
     * @param outState 缓存
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_SELECTION, new ArrayList<>(mItems));
        outState.putInt(STATE_COLLECTION_TYPE, mCollectionType);
    }

    /**
     * 将数据保存进Bundle并且返回
     * @return Bundle
     */
    public Bundle getDataWithBundle() {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(STATE_SELECTION, new ArrayList<>(mItems));
        bundle.putInt(STATE_COLLECTION_TYPE, mCollectionType);
        return bundle;
    }

    /**
     * 添加数据
     * @param item 数据
     */
    public void add(Item item) {
        if (typeConflict(item)) {
            throw new IllegalArgumentException("Can't select images and videos at the same time.");
        }
    }


    /**
     * 重置数据源
     * @param items 数据源
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
     * @return list<Item>
     */
    public List<Item> asList() {
        return new ArrayList<>(mItems);
    }

    /**
     * 获取uri的集合
     * @return list<Uri>
     */
    public List<Uri> asListOfUri() {
        List<Uri> uris = new ArrayList<>();
        for (Item item : mItems) {
            uris.add(item.getContentUri());
        }
        return uris;
    }

    /**
     * 获取path的集合
     * @return list<path>
     */
    public List<String> asListOfString() {
        List<String> paths = new ArrayList<>();
        for (Item item : mItems) {
            paths.add(PathUtils.getPath(mContext, item.getContentUri()));
        }
        return paths;
    }

    /**
     * 该item是否在选择中
     * @param item 数据源
     * @return 返回是否选择
     */
    public boolean isSelected(Item item) {
        return mItems.contains(item);
    }

    /**
     * 当前数量 和 当前选择最大数量比较 是否相等
     * @return boolean
     */
    public boolean maxSelectableReached() {
        return mItems.size() == currentMaxSelectable();
    }

    // depends
    private int currentMaxSelectable() {
        SelectionSpec spec = SelectionSpec.getInstance();
        if (spec.maxSelectable > 0) {
            // 返回最大选择数量
            return spec.maxSelectable;
        } else if (mCollectionType == COLLECTION_IMAGE) {
            // 如果是图片类型，则返回最大图片选择数量
            return spec.maxImageSelectable;
        } else if (mCollectionType == COLLECTION_VIDEO) {
            // 如果是视频类型，则返回最大视频选择数量
            return spec.maxVideoSelectable;
        } else {
            // 返回最大选择数量
            return spec.maxSelectable;
        }
    }

    /**
     * 获取数据源长度
     * @return 数据源长度
     */
    public int count() {
        return mItems.size();
    }

    /**
     * 返回选择的num
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
