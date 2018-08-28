package com.zhongjh.cameraviewsoundrecorder.album.model;

import android.content.Context;
import android.os.Bundle;

import com.zhongjh.cameraviewsoundrecorder.album.entity.Item;

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
     * 获取数据源长度
     * @return 数据源长度
     */
    public int count() {
        return mItems.size();
    }
}
