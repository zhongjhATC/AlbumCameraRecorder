package com.zhongjh.albumcamerarecorder.album.widget.albumspinner;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * 专辑下拉选项框的适配器
 *
 * @author zhongjh
 */
public class AlbumSpinnerAdapter extends RecyclerView.Adapter<AlbumSpinnerAdapter.ViewHolder> {

    private List<Album> albums = new ArrayList<>();
    /**
     * 默认图片
     */
    private final Drawable mPlaceholder;



    public void bindAlbums(List<Album> albums) {
        this.albums = albums;
        notifyItemRangeChanged(0, albums.size());
    }

    public List<Album> getFolderData() {
        return albums;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album_zjh, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Album album = albums.get(position);
        String name = album.getDisplayName();
        long imageNum = album.getCount();
//        boolean isChecked = album.isChecked();
//        int checkedNum = album.getCheckedNum();
//        holder.tvSign.setVisibility(checkedNum > 0 ? View.VISIBLE : View.INVISIBLE);
//        holder.itemView.setSelected(isChecked);
//        if (PictureSelectionConfig.uiStyle != null) {
//            if (PictureSelectionConfig.uiStyle.picture_album_backgroundStyle != 0) {
//                holder.itemView.setBackgroundResource(PictureSelectionConfig.uiStyle.picture_album_backgroundStyle);
//            }
//        } else if (PictureSelectionConfig.style != null) {
//            if (PictureSelectionConfig.style.pictureAlbumStyle != 0) {
//                holder.itemView.setBackgroundResource(PictureSelectionConfig.style.pictureAlbumStyle);
//            }
//        }

        GlobalSpec.INSTANCE.getImageEngine().loadThumbnail(holder.itemView.getContext(),
                holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.z_media_grid_size),
                mPlaceholder,
                holder.ivFirstImage, album.getCoverUri());

        if (PictureSelectionConfig.imageEngine != null) {
            PictureSelectionConfig.imageEngine.loadFolderImage(holder.itemView.getContext(),
                    imagePath, holder.ivFirstImage);
        }
        Context context = holder.itemView.getContext();
        String firstTitle = album.getOfAllType() != -1 ? album.getOfAllType() == PictureMimeType.ofAudio() ?
                context.getString(R.string.picture_all_audio)
                : context.getString(R.string.picture_camera_roll) : name;
        holder.tvFolderName.setText(context.getString(R.string.picture_camera_roll_num, firstTitle, imageNum));
        holder.itemView.setOnClickListener(view -> {
            if (onAlbumItemClickListener != null) {
                int size = albums.size();
                for (int i = 0; i < size; i++) {
                    Album item = albums.get(i);
                    item.setChecked(false);
                }
                album.setChecked(true);
                notifyItemRangeChanged(0, albums.size());
                onAlbumItemClickListener.onItemClick(position, album.isCameraFolder(), album.getBucketId(), album.getName(), album.getData());
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFirstImage;
        TextView tvFolderName, tvSign;

        public ViewHolder(View itemView) {
            super(itemView);
            ivFirstImage = itemView.findViewById(R.id.first_image);
            tvFolderName = itemView.findViewById(R.id.tv_folder_name);
            tvSign = itemView.findViewById(R.id.tv_sign);
            if (PictureSelectionConfig.uiStyle != null) {
                if (PictureSelectionConfig.uiStyle.picture_album_checkDotStyle != 0) {
                    tvSign.setBackgroundResource(PictureSelectionConfig.uiStyle.picture_album_checkDotStyle);
                }
                if (PictureSelectionConfig.uiStyle.picture_album_textColor != 0) {
                    tvFolderName.setTextColor(PictureSelectionConfig.uiStyle.picture_album_textColor);
                }
                if (PictureSelectionConfig.uiStyle.picture_album_textSize > 0) {
                    tvFolderName.setTextSize(PictureSelectionConfig.uiStyle.picture_album_textSize);
                }
            } else if (PictureSelectionConfig.style != null) {
                if (PictureSelectionConfig.style.pictureFolderCheckedDotStyle != 0) {
                    tvSign.setBackgroundResource(PictureSelectionConfig.style.pictureFolderCheckedDotStyle);
                }
                if (PictureSelectionConfig.style.folderTextColor != 0) {
                    tvFolderName.setTextColor(PictureSelectionConfig.style.folderTextColor);
                }
                if (PictureSelectionConfig.style.folderTextSize > 0) {
                    tvFolderName.setTextSize(PictureSelectionConfig.style.folderTextSize);
                }
            } else {
                Drawable folderCheckedDotDrawable = AttrsUtils.getTypeValueDrawable(itemView.getContext(), R.attr.picture_folder_checked_dot, R.drawable.picture_orange_oval);
                tvSign.setBackground(folderCheckedDotDrawable);
                int folderTextColor = AttrsUtils.getTypeValueColor(itemView.getContext(), R.attr.picture_folder_textColor);
                if (folderTextColor != 0) {
                    tvFolderName.setTextColor(folderTextColor);
                }
                float folderTextSize = AttrsUtils.getTypeValueSize(itemView.getContext(), R.attr.picture_folder_textSize);
                if (folderTextSize > 0) {
                    tvFolderName.setTextSize(TypedValue.COMPLEX_UNIT_PX, folderTextSize);
                }
            }
        }
    }

    private OnAlbumItemClickListener onAlbumItemClickListener;

    public void setOnAlbumItemClickListener(OnAlbumItemClickListener listener) {
        this.onAlbumItemClickListener = listener;
    }
}
