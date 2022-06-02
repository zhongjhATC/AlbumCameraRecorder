package com.zhongjh.albumcamerarecorder.camera.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.CameraFragment;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.preview.AlbumPreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.common.listener.OnMoreClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.COLLECTION_IMAGE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_COLLECTION_TYPE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_SELECTION;

/**
 * 横向形式显示多个图片的
 *
 * @author zhongjh
 * @date 2021/10/9
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final String TAG = PhotoAdapter.class.getSimpleName();

    Context mContext;
    CameraFragment mCameraFragment;
    GlobalSpec mGlobalSpec;
    List<BitmapData> mListData;

    /**
     * 记录当前删除事件的时间
     */
    private long mLastOperationTime;

    // region 回调监听事件

    private final PhotoAdapterListener mPhotoAdapterListener;

    // endregion

    public PhotoAdapter(Context context, CameraFragment fragment, GlobalSpec globalSpec,
                        List<BitmapData> listData, PhotoAdapterListener photoAdapterListener) {
        mContext = context;
        mCameraFragment = fragment;
        mGlobalSpec = globalSpec;
        this.mListData = listData;
        mPhotoAdapterListener = photoAdapterListener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_image_multilibrary_zjh, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        mGlobalSpec.getImageEngine().loadUriImage(mContext, holder.imgPhoto, mListData.get(position).getUri());
        // 点击图片
        holder.itemView.setOnClickListener(new OnMoreClickListener() {
            @Override
            public void onMoreClickListener(@NotNull View v) {
                onClickListener(position);
            }
        });
        holder.imgCancel.setOnClickListener(new OnMoreClickListener() {
            @Override
            public void onMoreClickListener(@NotNull View v) {
                removePosition(position);
            }
        });
    }

    public List<BitmapData> getListData() {
        return mListData;
    }

    public void setListData(List<BitmapData> listData) {
        this.mListData = listData;
        notifyDataSetChanged();
    }

    /**
     * 点击事件
     *
     * @param position 索引
     */
    private void onClickListener(int position) {
        if (isOperation()) {
            ArrayList<MultiMedia> items = new ArrayList<>();
            for (BitmapData item : mListData) {
                MultiMedia multiMedia = new MultiMedia();
                multiMedia.setId(mListData.indexOf(item));
                multiMedia.setUri(item.getUri());
                multiMedia.setPath(item.getPath());
                multiMedia.setMimeType(MimeType.JPEG.toString());
                multiMedia.setWidth(item.getWidth());
                multiMedia.setHeight(item.getHeight());
                items.add(multiMedia);
            }
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(STATE_SELECTION, items);
            bundle.putInt(STATE_COLLECTION_TYPE, COLLECTION_IMAGE);

            Intent intent = new Intent(mContext, AlbumPreviewActivity.class);

            // 获取目前点击的这个item
            MultiMedia item = new MultiMedia();
            item.setUri(mListData.get(position).getUri());
            item.setPath(mListData.get(position).getPath());
            item.setMimeType(MimeType.JPEG.toString());
            item.setWidth(mListData.get(position).getWidth());
            item.setHeight(mListData.get(position).getHeight());
            intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);

            intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle);
            intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
            intent.putExtra(BasePreviewActivity.EXTRA_IS_ALLOW_REPEAT, true);
            intent.putExtra(BasePreviewActivity.IS_SELECTED_LISTENER, false);
            intent.putExtra(BasePreviewActivity.IS_SELECTED_CHECK, false);
            mCameraFragment.mAlbumPreviewActivityResult.launch(intent);
            if (mGlobalSpec.getCutscenesEnabled()) {
                if (mCameraFragment.getActivity() != null) {
                    mCameraFragment.getActivity().overridePendingTransition(R.anim.activity_open_zjh, 0);
                }
            }
        }
    }

    /**
     * 根据索引删除view
     *
     * @param position 索引
     */
    public void removePosition(int position) {
        if (isOperation()) {
            mPhotoAdapterListener.onDelete(position);
            mListData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }
    }

    /**
     * 根据两次操作时间判断是否能进行下一个操作
     * 两次操作之间不能低于500毫秒，因为在我们快速删除的时候，会出现问题IndexOutOfIndexException或者删除错乱的问题，
     * 原因是notifyItemRangeChanged这个方法中开启了多线程，动画有250毫秒+120毫秒
     *
     * @return 是否能操作
     */
    public boolean isOperation() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        int deleteDelayTime = 500;
        if ((curClickTime - mLastOperationTime) >= deleteDelayTime) {
            flag = true;
        }
        mLastOperationTime = curClickTime;
        return flag;
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount");
        return mListData != null ? mListData.size() : 0;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPhoto;
        ImageView imgCancel;

        PhotoViewHolder(View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            imgCancel = itemView.findViewById(R.id.imgCancel);
        }
    }

}
