package com.zhongjh.multimedia.utils

import android.content.Context
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.constants.ModuleTypes
import com.zhongjh.multimedia.settings.AlbumSpec.baseFilters
import com.zhongjh.multimedia.settings.GlobalSpec.getMimeTypeSet
import com.zhongjh.common.entity.IncapableCause
import com.zhongjh.common.entity.LocalMedia

object LocalMediaUtils {

    /**
     * 获取相同数据的索引
     *
     * @param item  当前数据
     * @return 索引
     */
    @JvmStatic
    fun checkedNumOf(list: List<LocalMedia>, item: LocalMedia): Int {
        var index = -1
        list.let {
            // 一般用于相册数据的获取索引
            for (i in it.indices) {
                if (it[i].path == item.path
                    && it[i].id == item.id
                ) {
                    index = i
                    break
                }
            }
        }
        // 如果选择的为 -1 就是未选状态，否则选择基础数量+1
        return if (index == -1) {
            Int.MIN_VALUE
        } else {
            index + 1
        }
    }

    /**
     * 获取相同数据的對象
     *
     * @param items 数据列表
     * @param item  当前数据
     * @return 索引
     */
    @JvmStatic
    fun checkedLocalMediaOf(items: List<LocalMedia>, item: LocalMedia): LocalMedia? {
        var localMedia: LocalMedia? = null
        for (i in items.indices) {
            if (items[i].path == item.path) {
                localMedia = items[i]
                break
            }
        }
        return localMedia
    }

    /**
     * 过滤文件
     *
     * @param context 上下文
     * @param item    数据源
     * @return 提示框
     */
    @JvmStatic
    fun isAcceptable(context: Context, item: LocalMedia): IncapableCause? {
        // 判断资源类型是否已设置可选
        if (!isSelectableType(item)) {
            return IncapableCause(context.getString(R.string.z_multi_library_error_file_type))
        }

        // 过滤不符合用户设定的资源 Filter提供抽象方法，由用户自行设置过滤规则
        if (baseFilters != null) {
            for (baseFilter in baseFilters!!) {
                val incapableCause = baseFilter.filter(context, item)
                if (incapableCause != null) {
                    return incapableCause
                }
            }
        }
        return null
    }

    /**
     * 判断资源类型是否已设置可选
     *
     * @param item    数据源
     * @return 是否
     */
    private fun isSelectableType(item: LocalMedia): Boolean {
        // 循环当前类型配置
        for (type in getMimeTypeSet(ModuleTypes.ALBUM)) {
            // 如果当前类型配置 相等 当前数据
            if (type.checkType(item.absolutePath)) {
                return true
            }
        }
        return false
    }

}