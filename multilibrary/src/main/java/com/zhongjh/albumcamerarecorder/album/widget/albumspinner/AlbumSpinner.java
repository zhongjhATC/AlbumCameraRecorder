package com.zhongjh.albumcamerarecorder.album.widget.albumspinner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.utils.AttrsUtils;
import com.zhongjh.common.utils.AnimUtils;
import com.zhongjh.common.utils.DisplayMetricsUtils;

import java.util.List;

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
    private final Drawable drawableUp;
    private final Drawable drawableDown;
    private final int maxHeight;
    private View rootViewBg;

    @SuppressLint("InflateParams")
    public AlbumSpinner(Context context) {
        this.context = context;
        this.window = LayoutInflater.from(context).inflate(R.layout.view_album_spinner_zjh, null);
        this.setContentView(window);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setAnimationStyle(R.style.AlbumSpinnerThemeStyle);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();

        // 获取上下箭头两个图片
        TypedArray typedArray = AttrsUtils.getTypedArray(context, R.attr.album_listPopupWindowStyle);
        if (typedArray.hasValue(R.styleable.ListPopupWindowStyle_album_arrow_up_icon)) {
            this.drawableUp = typedArray.getDrawable(R.styleable.ListPopupWindowStyle_album_arrow_up_icon);
        } else {
            this.drawableUp = ContextCompat.getDrawable(context, R.drawable.ic_round_keyboard_arrow_up_24);
        }
        if (typedArray.hasValue(R.styleable.ListPopupWindowStyle_album_arrow_down_icon)) {
            this.drawableDown = typedArray.getDrawable(R.styleable.ListPopupWindowStyle_album_arrow_down_icon);
        } else {
            this.drawableDown = ContextCompat.getDrawable(context, R.drawable.ic_round_keyboard_arrow_down_24);
        }
        this.maxHeight = (int) (DisplayMetricsUtils.getScreenHeight(context) * 0.6);
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
        lp.height = albums.size() > FOLDER_MAX_COUNT ? maxHeight : ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public List<Album> getAlbums() {
        return adapter.getAlbums();
    }

    public boolean isEmpty() {
        return adapter.getAlbums().size() == 0;
    }

    public Album getAlbum(int position) {
        return adapter.getAlbums().size() > 0
                && position < adapter.getAlbums().size() ? adapter.getAlbums().get(position) : null;
    }

    public void setArrowImageView(ImageView ivArrowView) {
        this.ivArrowView = ivArrowView;
    }

    @Override
    public void showAsDropDown(View anchor) {
        try {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                int[] location = new int[2];
                anchor.getLocationInWindow(location);
                showAtLocation(anchor, Gravity.NO_GRAVITY, 0, location[1] + anchor.getHeight());
            } else {
                super.showAsDropDown(anchor);
            }
            isDismiss = false;
            ivArrowView.setImageDrawable(drawableUp);
            AnimUtils.rotateArrow(ivArrowView, true);
            rootViewBg.animate()
                    .alpha(1)
                    .setDuration(250)
                    .setStartDelay(250).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        ivArrowView.setImageDrawable(drawableDown);
        AnimUtils.rotateArrow(ivArrowView, false);
        isDismiss = true;
        AlbumSpinner.super.dismiss();
        isDismiss = false;
    }

    /**
     * 设置选中状态
     */
    public void updateFolderCheckStatus(List<Album> result) {
        try {
            List<Album> albums = adapter.getAlbums();
            int size = albums.size();
            int resultSize = result.size();
            for (int i = 0; i < size; i++) {
                Album album = albums.get(i);
                album.setCheckedNum(0);
                for (int j = 0; j < resultSize; j++) {
                    Album media = result.get(j);
                    if (album.getDisplayName().equals(media.getDisplayName())
                            || "-1".equals(album.getId())) {
                        album.setCheckedNum(1);
                        break;
                    }
                }
            }
            adapter.bindAlbums(albums);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
