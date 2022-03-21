package com.zhongjh.albumcamerarecorder.album.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.CursorAdapter
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import com.zhongjh.albumcamerarecorder.album.entity.Album.Companion.valueOf
import com.zhongjh.common.utils.ColorFilterUtil.setColorFilterSrcIn

/**
 * 专辑下拉框控件
 * @author zhongjh
 */
class AlbumsSpinner(context: Context) {

    private lateinit var mAdapter: CursorAdapter
    private lateinit var mSelected: TextView
    private val mListPopupWindow: ListPopupWindow = ListPopupWindow(
        context,
        null,
        com.zhongjh.albumcamerarecorder.R.attr.listPopupWindowStyle
    )
    private lateinit var mOnItemSelectedListener: AdapterView.OnItemSelectedListener

    /**
     * 赋值事件
     *
     * @param listener 事件
     */
    fun setOnItemSelectedListener(listener: AdapterView.OnItemSelectedListener) {
        mOnItemSelectedListener = listener
    }

    /**
     * 设置选择某一项
     *
     * @param context  上下文
     * @param position 索引
     */
    fun setSelection(context: Context, position: Int) {
        mListPopupWindow.setSelection(position)
        onItemSelected(context, position)
    }

    /**
     * 点击事件
     *
     * @param context  上下文
     * @param position 索引
     */
    private fun onItemSelected(context: Context, position: Int) {
        mListPopupWindow.dismiss()
        // 获取数据源
        val cursor = mAdapter.cursor
        // 选择该position
        cursor.moveToPosition(position)
        val album = valueOf(cursor)
        val displayName = album.getDisplayName(context)
        if (mSelected.visibility == View.VISIBLE) {
            // 如果显示就赋值
            mSelected.text = displayName
        } else {
            // 否则先显示出来再赋值
            mSelected.alpha = 0.0f
            mSelected.visibility = View.VISIBLE
            mSelected.text = displayName
            mSelected.animate().alpha(1.0f).setDuration(
                context.resources.getInteger(
                    android.R.integer.config_longAnimTime
                ).toLong()
            ).start()
        }
    }

    /**
     * 设置适配器
     *
     * @param adapter 适配器
     */
    fun setAdapter(adapter: CursorAdapter) {
        mListPopupWindow.setAdapter(adapter)
        mAdapter = adapter
    }

    /**
     * 设置文本框,并且给该文本框设置点击等事件
     *
     * @param textView 文本框
     */
    @SuppressLint("ClickableViewAccessibility")
    fun setSelectedTextView(textView: TextView) {
        mSelected = textView

        // 设置下拉箭头的颜色跟album_element_color文本颜色一样
        val drawables = mSelected.compoundDrawables
        val right = drawables[2]
        val ta =
            mSelected.context.theme.obtainStyledAttributes(intArrayOf(com.zhongjh.albumcamerarecorder.R.attr.album_element_color))
        val color = ta.getColor(0, 0)
        ta.recycle()
        // 使用设置的主题颜色对目标Drawable(这里是一个小箭头)进行SRC_IN模式合成 达到改变Drawable颜色的效果
        setColorFilterSrcIn(right, color)
        mSelected.visibility = View.GONE
        mSelected.setOnClickListener { v: View ->
            // 显示选择框
            val itemHeight =
                v.resources.getDimensionPixelSize(com.zhongjh.albumcamerarecorder.R.dimen.album_item_height)
            mListPopupWindow.height =
                if (mAdapter.count > MAX_SHOWN_COUNT) itemHeight * MAX_SHOWN_COUNT else itemHeight * mAdapter.count
            mListPopupWindow.show()
        }
        // 设置textView向下拖拽可下拉ListPopupWindow
        mSelected.setOnTouchListener(mListPopupWindow.createDragToOpenListener(mSelected))
    }

    /**
     * 设置锚点view
     *
     * @param view view
     */
    fun setPopupAnchorView(view: View) {
        mListPopupWindow.anchorView = view
    }

    companion object {
        private const val MAX_SHOWN_COUNT = 6
    }

    init {
        // 实例化ListPopupWindow控件
        mListPopupWindow.isModal = true
        val density = context.resources.displayMetrics.density
        mListPopupWindow.setContentWidth((216 * density).toInt())
        mListPopupWindow.horizontalOffset = (16 * density).toInt()
        mListPopupWindow.verticalOffset = (-48 * density).toInt()

        // 点击事件
        mListPopupWindow.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
            onItemSelected(parent.context, position)
            mOnItemSelectedListener.onItemSelected(parent, view, position, id)
        }
    }
}