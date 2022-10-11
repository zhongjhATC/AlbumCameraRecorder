package com.zhongjh.albumcamerarecorder.album.base

import android.database.Cursor
import android.provider.MediaStore
import androidx.recyclerview.widget.RecyclerView

/**
 * 父类
 *
 * @author zhongjh
 * @param <VH>
</VH> */
abstract class BaseRecyclerViewCursorAdapter<VH : RecyclerView.ViewHolder?>  :
    RecyclerView.Adapter<VH>() {
    protected var cursor: Cursor? = null
        private set
    private var mRowIdColumn = 0

    /**
     * 绑定数据源
     *
     * @param holder 控件
     * @param cursor 加载完成的游标
     */
    protected abstract fun onBindViewHolder(holder: VH, cursor: Cursor?)
    override fun onBindViewHolder(holder: VH, position: Int) {
        check(isDataValid(cursor)) { "Cannot bind view holder when cursor is in invalid state." }
        check(cursor!!.moveToPosition(position)) {
            ("Could not move cursor to position " + position
                    + " when trying to bind view holder")
        }
        onBindViewHolder(holder, cursor)
    }

    override fun getItemViewType(position: Int): Int {
        check(cursor!!.moveToPosition(position)) {
            ("Could not move cursor to position " + position
                    + " when trying to get item view type.")
        }
        return getItemViewType(position, cursor)
    }

    /**
     * 返回类型
     * @param position 索引
     * @param cursor   游标
     * @return 返回类型
     */
    protected abstract fun getItemViewType(position: Int, cursor: Cursor?): Int
    override fun getItemCount(): Int {
        return if (isDataValid(cursor)) {
            cursor!!.count
        } else {
            0
        }
    }

    override fun getItemId(position: Int): Long {
        check(isDataValid(cursor)) { "Cannot lookup item id when cursor is in invalid state." }
        check(cursor!!.moveToPosition(position)) {
            ("Could not move cursor to position " + position
                    + " when trying to get an item id")
        }
        return cursor!!.getLong(mRowIdColumn)
    }

    /**
     * 填充数据源
     *
     * @param newCursor 数据源
     */
    fun swapCursor(newCursor: Cursor?) {
        if (newCursor === cursor) {
            return
        }
        if (newCursor != null) {
            cursor = newCursor
            mRowIdColumn = cursor!!.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            notifyDataSetChanged()
        } else {
            notifyItemRangeRemoved(0, itemCount)
            cursor = null
            mRowIdColumn = -1
        }
    }

    private fun isDataValid(cursor: Cursor?): Boolean {
        return cursor != null && !cursor.isClosed
    }

    init {
    }
}