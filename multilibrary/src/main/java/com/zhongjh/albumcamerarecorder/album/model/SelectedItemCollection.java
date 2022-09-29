package com.zhongjh.albumcamerarecorder.album.model;

import static com.zhongjh.common.enums.Constant.IMAGE;
import static com.zhongjh.common.enums.Constant.IMAGE_VIDEO;
import static com.zhongjh.common.enums.Constant.VIDEO;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.SelectedCountMessage;
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils;
import com.zhongjh.common.entity.IncapableCause;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 选择的数据源
 *
 * @author zhongjh
 * @date 2018/8/28
 */
public class SelectedItemCollection {

    /**
     * 数据源的标记
     */
    public static final String STATE_SELECTION = "state_selection";
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
    /**
     * 选择数据源
     */
    private ArrayList<MultiMedia> mItems;
    /**
     * 当前选择的所有类型，列表如果包含了图片和视频，就会变成混合类型
     */
    private int mCollectionType = COLLECTION_UNDEFINED;
    /**
     * 当前选择的视频数量
     */
    private int mSelectedVideoCount;
    /**
     * 当前选择的图片数量
     */
    private int mSelectedImageCount;

    public SelectedItemCollection(Context context) {
        mContext = context;
    }

    /**
     * @param bundle        数据源
     * @param isAllowRepeat 是否允许重复
     */
    public void onCreate(Bundle bundle, boolean isAllowRepeat) {
        if (bundle == null) {
            mItems = new ArrayList<>();
        } else {
            // 获取缓存的数据
            List<MultiMedia> saved = bundle.getParcelableArrayList(STATE_SELECTION);
            if (saved != null) {
                Log.d("onSaveInstanceState",saved.size() + " onCreate");
                if (isAllowRepeat) {
                    mItems = new ArrayList<>();
                    mItems.addAll(saved);
                } else {
                    mItems = new ArrayList<>(saved);
                }
            }

            mCollectionType = bundle.getInt(STATE_COLLECTION_TYPE, COLLECTION_UNDEFINED);
        }
        Log.d("onSaveInstanceState",mItems.size() + " onCreate2");
    }

    /**
     * 缓存数据
     *
     * @param outState 缓存
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_SELECTION, mItems);
        Log.d("onSaveInstanceState",mItems.size() + " onSaveInstanceState");
        outState.putInt(STATE_COLLECTION_TYPE, mCollectionType);
    }

    /**
     * 将数据保存进Bundle并且返回
     *
     * @return Bundle
     */
    public Bundle getDataWithBundle() {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(STATE_SELECTION, mItems);
        bundle.putInt(STATE_COLLECTION_TYPE, mCollectionType);
        Log.d("onSaveInstanceState",mItems.size() + " getDataWithBundle");
        return bundle;
    }

    /**
     * 将资源对象添加到已选中集合
     *
     * @param item 数据
     */
    public boolean add(MultiMedia item) {
        Log.d("onSaveInstanceState",mItems.size() + " add");
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
    public boolean remove(MultiMedia item) {
        boolean removed;
        MultiMedia multiMedia = MultiMedia.checkedMultiMediaOf(mItems, item);
        removed = mItems.remove(multiMedia);
        if (removed) {
            if (mItems.size() == 0) {
                // 如果删除后没有数据，设置当前类型为空
                mCollectionType = COLLECTION_UNDEFINED;
            } else {
                if (mCollectionType == COLLECTION_MIXED) {
                    currentMaxSelectable();
                    Log.d("currentMaxSelectable", "currentMaxSelectable");
                }
            }
        }
        Log.d("onSaveInstanceState",mItems.size() + " remove");
        return removed;
    }

    /**
     * 重置数据源
     *
     * @param items          数据源
     * @param collectionType 类型
     */
    public void overwrite(ArrayList<MultiMedia> items, int collectionType) {
        if (items.size() == 0) {
            mCollectionType = COLLECTION_UNDEFINED;
        } else {
            mCollectionType = collectionType;
        }
        mItems.clear();
        mItems.addAll(items);
        Log.d("onSaveInstanceState",mItems.size() + " overwrite");
    }

    /**
     * 转换成list
     *
     * @return list<Item>
     */
    public List<MultiMedia> asList() {
        return mItems;
    }

    /**
     * 获取LocalFile的集合
     *
     * @return list<LocalFile>
     */
    public ArrayList<LocalFile> asListOfLocalFile() {
        ArrayList<LocalFile> localFiles = new ArrayList<>();
        for (MultiMedia item : mItems) {
            updateMultiMediaPath(item);
            LocalFile localFile = new LocalFile(item);
            localFiles.add(localFile);
        }
        Log.d("onSaveInstanceState",mItems.size() + " asListOfLocalFile");
        return localFiles;
    }

    /**
     * 更新path
     */
    public void updatePath() {
        for (MultiMedia item : mItems) {
            updateMultiMediaPath(item);
        }
        Log.d("onSaveInstanceState",mItems.size() + " updatePath");
    }

    /**
     * 更新 MultiMedia 的path
     * 如果为 null 则更新真实地址
     *
     * @param multiMedia multiMedia
     */
    private void updateMultiMediaPath(MultiMedia multiMedia) {
        multiMedia.analysesUriSetPathAndOriginalPath(mContext);
        Log.d("onSaveInstanceState",mItems.size() + " updateMultiMediaPath");
    }

    /**
     * 该item是否在选择中
     *
     * @param item 数据源
     * @return 返回是否选择
     */
    public boolean isSelected(MultiMedia item) {
        return mItems.contains(item);
    }

    /**
     * 验证当前item是否满足可以被选中的条件
     *
     * @param item 数据item
     * @return 弹窗
     */
    public IncapableCause isAcceptable(MultiMedia item) {
        Log.d("onSaveInstanceState",mItems.size() + " isAcceptable");
        boolean maxSelectableReached = false;
        int maxSelectable = 0;
        String type = "";
        SelectedCountMessage selectedCountMessage;
        // 判断是否混合视频图片模式
        if (!AlbumSpec.INSTANCE.getMediaTypeExclusive()) {
            // 混合检查
            getSelectCount();
            if (Objects.requireNonNull(item.getMimeType()).startsWith(IMAGE)) {
                selectedCountMessage = SelectableUtils.isImageMaxCount(mSelectedImageCount, mSelectedVideoCount);
                if (selectedCountMessage.isMaxSelectableReached()) {
                    maxSelectableReached = true;
                    maxSelectable = selectedCountMessage.getMaxCount();
                    type = selectedCountMessage.getType();
                }
            } else if (item.getMimeType().startsWith(VIDEO)) {
                selectedCountMessage = SelectableUtils.isVideoMaxCount(mSelectedVideoCount, mSelectedImageCount);
                if (selectedCountMessage.isMaxSelectableReached()) {
                    maxSelectableReached = true;
                    maxSelectable = selectedCountMessage.getMaxCount();
                    type = selectedCountMessage.getType();
                }
            }
            return newIncapableCause(item, maxSelectableReached, maxSelectable, true, type);
        } else {
            // 非混合模式
            maxSelectableReached = maxSelectableReached();
            maxSelectable = currentMaxSelectable();
            return newIncapableCause(item, maxSelectableReached, maxSelectable, false, null);
        }

    }

    /**
     * 验证当前item是否满足可以被选中的条件
     *
     * @param item                 数据item
     * @param maxSelectableReached 是否已经选择最大值
     * @param maxSelectable        选择的最大数量
     * @param isMashup             提示是否提示
     * @param type                 类型
     * @return 弹窗
     */
    public IncapableCause newIncapableCause(MultiMedia item, boolean maxSelectableReached, int maxSelectable, boolean isMashup, String type) {
        // 检查是否超过最大设置数量
        if (maxSelectableReached) {
            String cause;
            try {
                cause = getCause(maxSelectable, isMashup, type);
            } catch (Resources.NotFoundException | NoClassDefFoundError e) {
                cause = getCause(maxSelectable, isMashup, type);
            }
            // 生成窗口
            return new IncapableCause(cause);
        } else if (typeConflict(item)) {
            // 判断选择资源(图片跟视频)是否类型冲突
            return new IncapableCause(mContext.getString(R.string.z_multi_library_error_type_conflict));
        }
        // 过滤文件
        return PhotoMetadataUtils.isAcceptable(mContext, item);
    }

    /**
     * 根据相关参数构造文本消息
     *
     * @param maxSelectable 选择的最大数量
     * @param isMashup      提示是否提示
     * @param type          类型
     * @return 文本消息
     */
    private String getCause(int maxSelectable, boolean isMashup, String type) {
        String cause = "";
        if (isMashup) {
            switch (type) {
                case IMAGE_VIDEO:
                    cause = mContext.getResources().getString(
                            R.string.z_multi_library_error_over_count,
                            maxSelectable
                    );
                    break;
                case IMAGE:
                    cause = mContext.getResources().getString(
                            R.string.z_multi_library_error_over_count_image,
                            maxSelectable
                    );
                    break;
                case VIDEO:
                    cause = mContext.getResources().getString(
                            R.string.z_multi_library_error_over_count_video,
                            maxSelectable
                    );
                    break;
                default:
                    break;
            }
        } else {
            cause = mContext.getResources().getString(
                    R.string.z_multi_library_error_over_count,
                    maxSelectable
            );
        }
        return cause;
    }

    /**
     * 当前数量 和 当前选择最大数量比较 是否相等
     *
     * @return boolean
     */
    public boolean maxSelectableReached() {
        Log.d("onSaveInstanceState",mItems.size() + " maxSelectableReached");
        return mItems.size() == currentMaxSelectable();
    }

    /**
     * 获取当前选择了图片、视频数量
     */
    private void getSelectCount() {
        mSelectedImageCount = 0;
        mSelectedVideoCount = 0;
        for (MultiMedia multiMedia : mItems) {
            if (multiMedia.getMimeType().startsWith("image")) {
                mSelectedImageCount++;
            } else if (multiMedia.getMimeType().startsWith("video")) {
                mSelectedVideoCount++;
            }
        }
        Log.d("onSaveInstanceState",mItems.size() + " getSelectCount");
    }

    /**
     * 返回最多选择的数量
     *
     * @return 数量
     */
    private int currentMaxSelectable() {
        int leastCount;
        // 判断是否能同时选择视频和图片
        if (!AlbumSpec.INSTANCE.getMediaTypeExclusive()) {
            // 返回视频+图片
            leastCount = SelectableUtils.getImageVideoMaxCount();
        } else {
            if (mCollectionType == COLLECTION_IMAGE) {
                leastCount = SelectableUtils.getImageMaxCount();
            } else if (mCollectionType == COLLECTION_VIDEO) {
                leastCount = SelectableUtils.getVideoMaxCount();
            } else {
                // 返回视频+图片
                leastCount = SelectableUtils.getImageVideoMaxCount();
            }

        }

        Log.d("onSaveInstanceState",mItems.size() + " currentMaxSelectable");
        return leastCount;
    }

    /**
     * 判断选择资源(图片跟视频)是否类型冲突
     * Determine whether there will be conflict media types. A user can only select images and videos at the same time
     * while {@link AlbumSpec#getMediaTypeExclusive} is set to false.
     */
    private boolean typeConflict(MultiMedia item) {
        // 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
        return AlbumSpec.INSTANCE.getMediaTypeExclusive()
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
    public int checkedNumOf(MultiMedia item) {
        return MultiMedia.checkedNumOf(new ArrayList<>(mItems), item);
    }

}
