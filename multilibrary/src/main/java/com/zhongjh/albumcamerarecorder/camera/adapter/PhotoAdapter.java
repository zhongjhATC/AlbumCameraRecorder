package com.zhongjh.albumcamerarecorder.camera.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;

import java.util.List;

/**
 * 横向形式显示多个图片的
 *
 * @author zhongjh
 * @date 2021/10/9
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    Context mContext;
    GlobalSpec mGlobalSpec;
    List<BitmapData> mListData;

    // region 回调监听事件

    private PhotoAdapterListener mPhotoAdapterListener;

    // endregion

    public PhotoAdapter(Context context, GlobalSpec globalSpec,
                        List<BitmapData> listData, PhotoAdapterListener photoAdapterListener) {
        mContext = context;
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
     * 根据索引删除view
     * @param position 索引
     */
    public void removePosition(int position) {
        mListData.remove(position);
        mPhotoAdapterListener.onDelete(mListData.get(position));
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
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
