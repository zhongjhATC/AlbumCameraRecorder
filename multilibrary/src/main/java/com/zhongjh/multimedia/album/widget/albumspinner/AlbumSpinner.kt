package com.zhongjh.multimedia.album.widget.albumspinner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.common.utils.AnimUtils;
import com.zhongjh.common.utils.DisplayMetricsUtils;
import com.zhongjh.multimedia.R;
import com.zhongjh.multimedia.album.entity.Album;
import com.zhongjh.multimedia.album.entity.AlbumSpinnerStyle;
import com.zhongjh.multimedia.utils.AttrsUtils;

import java.util.List;
import java.util.Objects;

/**
 * 专辑选项控件
 *
 * @author zhongjh
 * @date 2022/9/21
 */
public class AlbumSpinner extends PopupWindow {
    private static final int FOLDER_MAX_COUNT = 8;

    private final Context context;
    private final View window;
    private RecyclerView mRecyclerView;
    private AlbumSpinnerAdapter adapter;
    private boolean isDismiss = false;
    private ImageView ivArrowView;
    private TextView tvAlbumTitle;
    private View rootViewBg;
    private final AlbumSpinnerStyle albumSpinnerStyle;

    @SuppressLint("InflateParams")
    public AlbumSpinner(Context context, AlbumSpinnerStyle albumSpinnerStyle) {
        this.context = context;
        this.albumSpinnerStyle = albumSpinnerStyle;
        this.window = LayoutInflater.from(context).inflate(R.layout.view_album_spinner_zjh, null);
        this.setContentView(window);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setAnimationStyle(R.style.AlbumSpinnerThemeStyle);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();
        initView();
    }

    public void initView() {
        rootViewBg = window.findViewById(R.id.rootViewBg);
        adapter = new AlbumSpinnerAdapter();
        mRecyclerView = window.findViewById(R.id.folder_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(adapter);
        rootViewBg.setOnClickListener(v -> dismiss());
    }

    public void bindFolder(List<Album> albums) {
        adapter.bindAlbums(albums);
        ViewGroup.LayoutParams lp = mRecyclerView.getLayoutParams();
        lp.height = albums.size() > FOLDER_MAX_COUNT ? albumSpinnerStyle.getMaxHeight() : ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public List<Album> getAlbums() {
        return adapter.getAlbums();
    }

    public boolean isEmpty() {
        return adapter.getAlbums().isEmpty();
    }

    public Album getAlbum(int position) {
        return !adapter.getAlbums().isEmpty() && position < adapter.getAlbums().size() ? adapter.getAlbums().get(position) : null;
    }

    public void setArrowImageView(ImageView ivArrowView) {
        this.ivArrowView = ivArrowView;
        this.ivArrowView.setImageDrawable(albumSpinnerStyle.getDrawableDown());
        this.ivArrowView.setOnClickListener(v -> albumSpinnerOnClick());
    }

    public void setTitleTextView(TextView tvAlbumTitle) {
        this.tvAlbumTitle = tvAlbumTitle;
        this.tvAlbumTitle.setOnClickListener(v -> albumSpinnerOnClick());
    }

    @Override
    public void showAsDropDown(View anchor) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            int[] location = new int[2];
            anchor.getLocationInWindow(location);
            showAtLocation(anchor, Gravity.NO_GRAVITY, 0, location[1] + anchor.getHeight());
        } else {
            super.showAsDropDown(anchor);
        }
        isDismiss = false;
        ivArrowView.setImageDrawable(albumSpinnerStyle.getDrawableUp());
        AnimUtils.rotateArrow(ivArrowView, true);
        rootViewBg.animate()
                .alpha(1)
                .setDuration(250)
                .setStartDelay(250).start();
    }

    public void setOnAlbumItemClickListener(OnAlbumItemClickListener listener) {
        adapter.setOnAlbumItemClickListener(listener);
    }

    @Override
    public void dismiss() {
        if (isDismiss) {
            return;
        }
        rootViewBg.animate()
                .alpha(0)
                .setDuration(50)
                .start();
        ivArrowView.setImageDrawable(albumSpinnerStyle.getDrawableDown());
        AnimUtils.rotateArrow(ivArrowView, false);
        isDismiss = true;
        AlbumSpinner.super.dismiss();
        isDismiss = false;
    }

    /**
     * 设置选中状态 - 红色圆点
     */
    public void updateFolderCheckStatus(List<Album> result) {
        List<Album> albums = adapter.getAlbums();
        int size = albums.size();
        int resultSize = result.size();
        for (int i = 0; i < size; i++) {
            Album album = albums.get(i);
            album.setCheckedCount(0);
            if (null != album.getName()) {
                for (int j = 0; j < resultSize; j++) {
                    Album media = result.get(j);
                    if (album.getName().equals(media.getName())
                            || -1 == album.getId()) {
                        album.setCheckedCount(1);
                        break;
                    }
                }
            }
        }
        adapter.bindAlbums(albums);
    }

    /**
     * 设置选中状态
     */
    public void updateCheckStatus(List<Album> selects) {
        List<Album> albums = adapter.getAlbums();
        for (Album album : albums) {
            for (Album select : selects) {
                if (Objects.equals(select.getName(), album.getName())) {
                    album.setChecked(true);
                    break;
                }
            }
        }
        adapter.bindAlbums(albums);
    }

    /**
     * 自动绑定相关View显示本身
     */
    public void albumSpinnerOnClick() {
        if (this.isShowing()) {
            this.dismiss();
        } else {
            this.showAsDropDown(tvAlbumTitle);
        }
    }
}
