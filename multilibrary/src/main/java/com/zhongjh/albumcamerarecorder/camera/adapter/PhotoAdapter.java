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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.CameraLayout;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.preview.AlbumPreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.common.enums.MultimediaTypes;

import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.COLLECTION_IMAGE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_COLLECTION_TYPE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_SELECTION;
import static com.zhongjh.albumcamerarecorder.constants.Constant.REQUEST_CODE_PREVIEW_CAMRRA;

/**
 * 横向形式显示多个图片的
 *
 * @author zhongjh
 * @date 2021/10/9
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final String TAG = PhotoAdapter.class.getSimpleName();

    Context mContext;
    Fragment mFragment;
    GlobalSpec mGlobalSpec;
    List<BitmapData> mListData;

    // region 回调监听事件

    private PhotoAdapterListener mPhotoAdapterListener;

    // endregion

    public PhotoAdapter(Context context, Fragment fragment, GlobalSpec globalSpec,
                        List<BitmapData> listData, PhotoAdapterListener photoAdapterListener) {
        mContext = context;
        mFragment = fragment;
        mGlobalSpec = globalSpec;
        this.mListData = listData;
        mPhotoAdapterListener = photoAdapterListener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_image_zjh, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        mGlobalSpec.imageEngine.loadUriImage(mContext, holder.imgPhoto, mListData.get(position).getUri());
        // 点击图片
        holder.itemView.setOnClickListener(v -> onClickListener(position));
        holder.imgCancel.setOnClickListener(v -> removePosition(position));
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
     * @param position 索引
     */
    private void onClickListener(int position) {
        ArrayList<MultiMedia> items = new ArrayList<>();
        for (BitmapData item : mListData) {
            MultiMedia multiMedia = new MultiMedia();
            multiMedia.setUri(item.getUri());
            multiMedia.setPath(item.getPath());
            multiMedia.setType(MultimediaTypes.PICTURE);
            multiMedia.setMimeType(MimeType.JPEG.toString());
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
        item.setType(MultimediaTypes.PICTURE);
        item.setMimeType(MimeType.JPEG.toString());
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);

        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle);
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
        intent.putExtra(BasePreviewActivity.EXTRA_IS_ALLOW_REPEAT, true);
        intent.putExtra(BasePreviewActivity.IS_SELECTED_LISTENER, false);
        intent.putExtra(BasePreviewActivity.IS_SELECTED_CHECK, false);
        intent.putExtra(BasePreviewActivity.IS_ALBUM_URI, false);
        mFragment.startActivityForResult(intent, REQUEST_CODE_PREVIEW_CAMRRA);
        if (mGlobalSpec.isCutscenes) {
            if (mFragment.getActivity() != null) {
                mFragment.getActivity().overridePendingTransition(R.anim.activity_open, 0);
            }
        }
    }

    /**
     * 根据索引删除view
     *
     * @param position 索引
     */
    public void removePosition(int position) {
        mPhotoAdapterListener.onDelete(position);
        mListData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,getItemCount());
    }

    @Override
    public int getItemCount() {
        Log.d(TAG,"getItemCount");
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
