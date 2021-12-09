/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhongjh.albumcamerarecorder.album.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.Album;

import com.zhongjh.common.utils.ColorFilterUtil;

/**
 * 专辑下拉框控件
 * @author zhongjh
 */
public class AlbumsSpinner {

    private static final int MAX_SHOWN_COUNT = 6;
    private CursorAdapter mAdapter;
    private TextView mSelected;
    private final ListPopupWindow mListPopupWindow;
    private AdapterView.OnItemSelectedListener mOnItemSelectedListener;

    public AlbumsSpinner(@NonNull Context context) {
        // 实例化ListPopupWindow控件
        mListPopupWindow = new ListPopupWindow(context, null, R.attr.listPopupWindowStyle);
        mListPopupWindow.setModal(true);
        float density = context.getResources().getDisplayMetrics().density;
        mListPopupWindow.setContentWidth((int) (216 * density));
        mListPopupWindow.setHorizontalOffset((int) (16 * density));
        mListPopupWindow.setVerticalOffset((int) (-48 * density));

        // 点击事件
        mListPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            AlbumsSpinner.this.onItemSelected(parent.getContext(), position);
            if (mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onItemSelected(parent, view, position, id);
            }
        });
    }

    /**
     * 赋值事件
     *
     * @param listener 事件
     */
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    /**
     * 设置选择某一项
     *
     * @param context  上下文
     * @param position 索引
     */
    public void setSelection(Context context, int position) {
        mListPopupWindow.setSelection(position);
        onItemSelected(context, position);
    }

    /**
     * 点击事件
     *
     * @param context  上下文
     * @param position 索引
     */
    private void onItemSelected(Context context, int position) {
        mListPopupWindow.dismiss();
        // 获取数据源
        Cursor cursor = mAdapter.getCursor();
        // 选择该position
        cursor.moveToPosition(position);
        Album album = Album.valueOf(cursor);
        String displayName = album.getDisplayName(context);
        if (mSelected.getVisibility() == View.VISIBLE) {
            // 如果显示就赋值
            mSelected.setText(displayName);
        } else {
            // 否则先显示出来再赋值
            mSelected.setAlpha(0.0f);
            mSelected.setVisibility(View.VISIBLE);
            mSelected.setText(displayName);
            mSelected.animate().alpha(1.0f).setDuration(context.getResources().getInteger(
                    android.R.integer.config_longAnimTime)).start();

        }
    }

    /**
     * 设置适配器
     *
     * @param adapter 适配器
     */
    public void setAdapter(CursorAdapter adapter) {
        mListPopupWindow.setAdapter(adapter);
        mAdapter = adapter;
    }

    /**
     * 设置文本框,并且给该文本框设置点击等事件
     *
     * @param textView 文本框
     */
    @SuppressLint("ClickableViewAccessibility")
    public void setSelectedTextView(TextView textView) {
        mSelected = textView;

        // 设置下拉箭头的颜色跟album_element_color文本颜色一样
        Drawable[] drawables = mSelected.getCompoundDrawables();
        Drawable right = drawables[2];
        TypedArray ta = mSelected.getContext().getTheme().obtainStyledAttributes(
                new int[]{R.attr.album_element_color});
        int color = ta.getColor(0, 0);
        ta.recycle();
        // 使用设置的主题颜色对目标Drawable(这里是一个小箭头)进行SRC_IN模式合成 达到改变Drawable颜色的效果
        ColorFilterUtil.setColorFilterSrcIn(right,color);

        mSelected.setVisibility(View.GONE);
        mSelected.setOnClickListener(v -> {
            // 显示选择框
            int itemHeight = v.getResources().getDimensionPixelSize(R.dimen.album_item_height);
            mListPopupWindow.setHeight(
                    mAdapter.getCount() > MAX_SHOWN_COUNT ? itemHeight * MAX_SHOWN_COUNT
                            : itemHeight * mAdapter.getCount());
            mListPopupWindow.show();
        });
        // 设置textView向下拖拽可下拉ListPopupWindow
        mSelected.setOnTouchListener(mListPopupWindow.createDragToOpenListener(mSelected));
    }

    /**
     * 设置锚点view
     *
     * @param view view
     */
    public void setPopupAnchorView(View view) {
        mListPopupWindow.setAnchorView(view);
    }

}
