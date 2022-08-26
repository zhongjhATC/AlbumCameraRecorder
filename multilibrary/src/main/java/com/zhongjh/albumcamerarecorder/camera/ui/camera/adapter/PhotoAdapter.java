package com.zhongjh.albumcamerarecorder.camera.ui.camera.adapter;

import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.COLLECTION_IMAGE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_COLLECTION_TYPE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_SELECTION;

import android.app.Activity;
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

/**
 * 横向形式显示多个图片的
 *
 * @author zhongjh
 * @date 2021/10/9
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final String TAG = PhotoAdapter.class.getSimpleName();

    Activity mActivity;
    GlobalSpec mGlobalSpec;
    List<BitmapData> mListData;

    // region 回调监听事件

    private final PhotoAdapterListener mPhotoAdapterListener;

    // endregion

    public PhotoAdapter(Activity activity, GlobalSpec globalSpec,
                        List<BitmapData> listData, PhotoAdapterListener photoAdapterListener) {
        mActivity = activity;
        mGlobalSpec = globalSpec;
        this.mListData = listData;
        mPhotoAdapterListener = photoAdapterListener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_image_multilibrary_zjh, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        BitmapData bitmapData = mListData.get(position);
        mGlobalSpec.getImageEngine().loadUriImage(mActivity, holder.imgPhoto, bitmapData.getUri());
        // 点击图片
        holder.itemView.setOnClickListener(new OnMoreClickListener() {
            @Override
            public void onListener(@NotNull View v) {
                onClickListener(bitmapData);
            }
        });
        holder.imgCancel.setOnClickListener(new OnMoreClickListener() {
            @Override
            public void onListener(@NotNull View v) {
                removePosition(bitmapData);
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
     * @param bitmapData 数据
     */
    private void onClickListener(BitmapData bitmapData) {
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

        Intent intent = new Intent(mActivity, AlbumPreviewActivity.class);

        // 获取目前点击的这个item
        MultiMedia item = new MultiMedia();
        item.setUri(bitmapData.getUri());
        item.setPath(bitmapData.getPath());
        item.setMimeType(MimeType.JPEG.toString());
        item.setWidth(bitmapData.getWidth());
        item.setHeight(bitmapData.getHeight());
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);

        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle);
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
        intent.putExtra(BasePreviewActivity.EXTRA_IS_ALLOW_REPEAT, true);
        intent.putExtra(BasePreviewActivity.IS_SELECTED_LISTENER, false);
        intent.putExtra(BasePreviewActivity.IS_SELECTED_CHECK, false);
        mPhotoAdapterListener.onPhotoAdapterClick(intent);
    }

    /**
     * 根据索引删除view
     *
     * @param bitmapData 数据
     */
    public void removePosition(BitmapData bitmapData) {
        int position = mListData.indexOf(bitmapData);
        Log.d(TAG, "removePosition " + position);
        mListData.remove(bitmapData);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mListData.size());
        mPhotoAdapterListener.onPhotoAdapterDelete(bitmapData, position);
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
