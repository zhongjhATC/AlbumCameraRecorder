package com.zhongjh.multimedia.camera.ui.camera.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.multimedia.R;
import com.zhongjh.multimedia.camera.entity.BitmapData;
import com.zhongjh.multimedia.preview.start.PreviewStartManager;
import com.zhongjh.multimedia.settings.GlobalSpec;
import com.zhongjh.common.listener.OnMoreClickListener;
import com.zhongjh.multimedia.camera.entity.BitmapData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 横向形式显示多个图片的
 *
 * @author zhongjh
 * @date 2021/10/9
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final String TAG = PhotoAdapter.class.getSimpleName();

    final Activity mActivity;
    final GlobalSpec mGlobalSpec;
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
        mGlobalSpec.getImageEngine().loadUriImage(mActivity, holder.imgPhoto, bitmapData.getAbsolutePath());
        // 点击图片
        int currentPosition = holder.getAbsoluteAdapterPosition();
        holder.itemView.setOnClickListener(new OnMoreClickListener() {
            @Override
            public void onListener(@NotNull View v) {
                onClickListener(currentPosition);
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
        // 计算新老数据集差异，将差异更新到Adapter
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PhotoCallback(this.mListData, listData));
        this.mListData = listData;
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * 点击事件
     *
     * @param position 索引
     */
    private void onClickListener(int position) {
        Intent intent = PreviewStartManager.INSTANCE.startPreviewActivityByCamera(mActivity, mListData, position);
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

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {

        final ImageView imgPhoto;
        final ImageView imgCancel;

        PhotoViewHolder(View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            imgCancel = itemView.findViewById(R.id.imgCancel);
        }
    }

}
