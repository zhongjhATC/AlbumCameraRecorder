package com.zhongjh.multimedia.album.ui.mediaselection.adapter.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * 线
 * @author zhongjh
 */
class MediaGridInset(private val mSpanCount: Int, private val mSpacing: Int, private val mIncludeEdge: Boolean) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        // item position
        val position = parent.getChildAdapterPosition(view)
        // item column
        val column = position % mSpanCount
        if (mIncludeEdge) {
            // spacing - column * ((1f / spanCount) * spacing)
            outRect.left = mSpacing - column * mSpacing / mSpanCount
            // (column + 1) * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * mSpacing / mSpanCount

            // top edge
            if (position < mSpanCount) {
                outRect.top = mSpacing
            }
            // item bottom
            outRect.bottom = mSpacing
        } else {
            // column * ((1f / spanCount) * spacing)
            outRect.left = column * mSpacing / mSpanCount
            // spacing - (column + 1) * ((1f / spanCount) * spacing)
            outRect.right = mSpacing - (column + 1) * mSpacing / mSpanCount
            if (position >= mSpanCount) {
                // item top
                outRect.top = mSpacing
            }
        }
    }

}