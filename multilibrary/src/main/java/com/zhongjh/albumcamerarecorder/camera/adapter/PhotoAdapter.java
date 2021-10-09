package com.zhongjh.albumcamerarecorder.camera.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;

import java.util.List;

/**
 * 横向形式显示多个图片的
 *
 * @author zhongjh
 * @date 2021/10/9
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    Context mContext;

    public PhotoAdapter(Context context, List<String> listData) {
        mContext = context;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_image_zjh, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
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
