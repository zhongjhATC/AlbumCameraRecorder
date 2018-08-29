package com.zhongjh.cameraviewsoundrecorder.album;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.zhongjh.cameraviewsoundrecorder.R;

/**
 * 左上角的下拉框适配器
 * Created by zhongjh on 2018/8/29.
 */
public class AlbumsSpinnerAdapter extends CursorAdapter {

    private final Drawable mPlaceholder;

    public AlbumsSpinnerAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);

        TypedArray ta = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.album_thumbnail_placeholder});
        mPlaceholder = ta.getDrawable(0);
        ta.recycle();
    }

    public AlbumsSpinnerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        TypedArray ta = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.album_thumbnail_placeholder});
        mPlaceholder = ta.getDrawable(0);
        ta.recycle();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_matiss_list_zjh, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
