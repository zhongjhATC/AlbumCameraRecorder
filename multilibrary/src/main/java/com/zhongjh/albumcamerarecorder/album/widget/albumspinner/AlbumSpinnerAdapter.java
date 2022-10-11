package com.zhongjh.albumcamerarecorder.album.widget.albumspinner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.AttrsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 专辑下拉选项框的适配器
 *
 * @author zhongjh
 */
public class AlbumSpinnerAdapter extends RecyclerView.Adapter<AlbumSpinnerAdapter.ViewHolder> {

    private List<Album> albums = new ArrayList<>();

    public void bindAlbums(List<Album> albums) {
        List<Album> oldAlbums = this.albums;
        this.albums = albums;
        // 计算新老数据集差异，将差异更新到Adapter
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new AlbumCallback(oldAlbums, albums));
        diffResult.dispatchUpdatesTo(this);
    }

    public List<Album> getAlbums() {
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
        String name = album.getDisplayName(holder.itemView.getContext());
        long imageNum = album.getCount();
        boolean isChecked = album.isChecked();
        int checkedNum = album.getCheckedNum();
        holder.tvSign.setVisibility(checkedNum > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.itemView.setSelected(isChecked);
        GlobalSpec.INSTANCE.getImageEngine().loadThumbnail(holder.itemView.getContext(),
                holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.z_media_grid_size),
                holder.placeholder,
                holder.imgFirst, album.getCoverUri());
        Context context = holder.itemView.getContext();
        holder.tvName.setText(context.getString(R.string.z_multi_library_album_num, name, imageNum));
        holder.itemView.setOnClickListener(view -> {
            if (onAlbumItemClickListener != null) {
                int size = albums.size();
                for (int i = 0; i < size; i++) {
                    Album item = albums.get(i);
                    item.setChecked(false);
                }
                album.setChecked(true);
                notifyItemRangeChanged(0, albums.size(), 0);
                onAlbumItemClickListener.onItemClick(position, album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFirst;
        TextView tvName, tvSign;
        /**
         * 默认图片
         */
        private final Drawable placeholder;

        public ViewHolder(View itemView) {
            super(itemView);
            imgFirst = itemView.findViewById(R.id.imgFirst);
            tvName = itemView.findViewById(R.id.tvName);
            tvSign = itemView.findViewById(R.id.tvSign);

            TypedArray ta = itemView.getContext().getTheme().obtainStyledAttributes(
                    new int[]{R.attr.album_thumbnail_placeholder});
            placeholder = ta.getDrawable(0);
            ta.recycle();

            // 获取下拉框的样式集合
            TypedValue typedValue = new TypedValue();
            itemView.getContext().getTheme().resolveAttribute(R.attr.album_listPopupWindowStyle, typedValue, true);

            // item背景
            Drawable backgroundStyle = AttrsUtils.getTypeValueDrawable(itemView.getContext(), typedValue.resourceId,
                    R.attr.album_backgroundStyle, R.drawable.spinner_item_select_bg_white);
            itemView.setBackground(backgroundStyle);

            // 该专辑里面有图片被选择时
            Drawable folderCheckedDotDrawable = AttrsUtils.getTypeValueDrawable(itemView.getContext(), typedValue.resourceId,
                    R.attr.album_checkDotStyle, R.drawable.ic_orange_oval);
            tvSign.setBackground(folderCheckedDotDrawable);

            // 专辑字体颜色
            int folderTextColor = AttrsUtils.getTypeValueColor(itemView.getContext(), typedValue.resourceId,
                    R.attr.album_textColor);
            if (folderTextColor != 0) {
                tvName.setTextColor(folderTextColor);
            }

            // 专辑字体大小
            int folderTextSize = AttrsUtils.getTypeValueSizeForInt(itemView.getContext(), typedValue.resourceId,
                    R.attr.album_textSize);
            if (folderTextSize != 0) {
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, folderTextSize);
            }
        }
    }

    private OnAlbumItemClickListener onAlbumItemClickListener;

    public void setOnAlbumItemClickListener(OnAlbumItemClickListener listener) {
        this.onAlbumItemClickListener = listener;
    }
}
