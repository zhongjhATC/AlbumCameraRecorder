package com.zhongjh.albumcamerarecorder.sharedanimation

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 用于在相册界面中的RecyclerView的item 过渡到 预览界面的ViewPager2的item 时，所需要的相关参数
 */
object RecycleItemViewParams {

    private val viewParams: MutableList<ViewParams> = ArrayList()

    /**
     * 添加 列表的每一个参数 到 viewParams
     */
    @JvmStatic
    fun add(viewGroup: ViewGroup, statusBarHeight: Int) {
        val views: MutableList<View?> = ArrayList()
        // 判断只支持 RecyclerView 和 ListView
        val childCount: Int = when (viewGroup) {
            is RecyclerView -> {
                viewGroup.childCount
            }
            is ListView -> {
                viewGroup.childCount
            }
            else -> {
                throw IllegalArgumentException(
                    viewGroup.javaClass.name
                            + " Must be " + RecyclerView::class.java + " or " + ListView::class.java
                )
            }
        }
        // 把列表的控件添加进去
        for (i in 0 until childCount) {
            val view = viewGroup.getChildAt(i) ?: continue
            views.add(view)
        }
        // 获取第一个、最后一个的可视position，列表总数
        val firstPos: Int
        var lastPos: Int
        val totalCount: Int
        if (viewGroup is RecyclerView) {
            val layoutManager = viewGroup.layoutManager as GridLayoutManager? ?: return
            totalCount = layoutManager.itemCount
            firstPos = layoutManager.findFirstVisibleItemPosition()
            lastPos = layoutManager.findLastVisibleItemPosition()
        } else {
            val listAdapter = (viewGroup as ListView).adapter ?: return
            totalCount = listAdapter.count
            firstPos = viewGroup.firstVisiblePosition
            lastPos = viewGroup.lastVisiblePosition
        }
        lastPos =
            if (lastPos > totalCount) {
                totalCount - 1
            } else {
                lastPos
            }
        fillPlaceHolder(views, totalCount, firstPos, lastPos)
        viewParams.clear()
        // 添加宽高左右标点参数到 viewParams
        for (i in views.indices) {
            val view = views[i]
            val viewParam = ViewParams()
            if (view == null) {
                viewParam.left = 0
                viewParam.top = 0
                viewParam.width = 0
                viewParam.height = 0
                Log.d("RecycleItemViewParams","00")
            } else {
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                viewParam.left = location[0]
                viewParam.top = location[1] - statusBarHeight
                viewParam.width = view.width
                viewParam.height = view.height
                Log.d("RecycleItemViewParams","left: ${viewParam.left} viewParam.top: ${viewParam.top} viewParam.width: ${viewParam.width} viewParam.height: ${viewParam.height}")
            }
            viewParams.add(viewParam)
        }
    }

    /**
     * 获取viewParams缓存中的某个索引下数据
     */
    fun getItem(position: Int): ViewParams? {
        return if (viewParams.size > position) {
            viewParams[position]
        } else {
            null
        }
    }

    private fun fillPlaceHolder(
        originImageList: MutableList<View?>,
        totalCount: Int,
        firstPos: Int,
        lastPos: Int,
    ) {
        if (firstPos > 0) {
            for (i in firstPos downTo 1) {
                originImageList.add(0, null)
            }
        }
        if (lastPos < totalCount) {
            for (i in totalCount - 1 - lastPos downTo 1) {
                originImageList.add(null)
            }
        }
    }

}