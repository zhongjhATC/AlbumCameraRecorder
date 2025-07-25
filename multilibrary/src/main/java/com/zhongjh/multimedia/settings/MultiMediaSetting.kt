package com.zhongjh.multimedia.settings

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.fragment.app.Fragment
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MimeType
import com.zhongjh.multimedia.model.SelectedData.Companion.STATE_SELECTION
import java.lang.ref.WeakReference

/**
 * 多媒体的设置 - Album
 *
 * @author zhongjh
 * @date 2018/9/28
 */
class MultiMediaSetting private constructor(activity: Activity, fragment: Fragment? = null) {

    private val mContext: WeakReference<Activity> = WeakReference(activity)
    private val mFragment: WeakReference<Fragment?> = WeakReference(fragment)

    val activity: Activity? = mContext.get()
    val fragment: Fragment?
        get() = mFragment.get()

    private constructor(fragment: Fragment) : this(fragment.requireActivity(), fragment)

    /**
     * 设置支持的类型
     *
     *
     * 未包含在集合中的类型仍将显示在网格中，但无法选择。
     *
     * @param mimeTypes 类型
     * @return [GlobalSetting] this
     * @see MimeType
     *
     * @see GlobalSetting
     */
    fun choose(mimeTypes: Set<MimeType>): GlobalSetting {
        return GlobalSetting(this, mimeTypes)
    }

    companion object {
        /**
         * 设置由Activity打开
         *
         * @param activity Activity instance.
         * @return this.
         */
        @JvmStatic
        fun from(activity: Activity): MultiMediaSetting {
            return MultiMediaSetting(activity)
        }

        /**
         * 由Fragment打开
         *
         *
         * 当用户完成选择时，将调用此方法： [Fragment.onActivityResult]
         *
         * @param fragment Fragment instance.
         * @return this.
         */
        @JvmStatic
        fun from(fragment: Fragment): MultiMediaSetting {
            return MultiMediaSetting(fragment)
        }

        /**
         * 获取用户确认后的MultiMedia多媒体数据
         *
         * @param data 通过onActivityResult方法获取
         * @return 用户通过直接调用预览界面然后返回的媒体数据. [LocalMedia]
         */
        @JvmStatic
        fun obtainLocalMediaResult(data: Intent): ArrayList<LocalMedia> {
            // 获取选择的数据
            val arrayList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableArrayListExtra(STATE_SELECTION, LocalMedia::class.java)
            } else {
                data.getParcelableArrayListExtra(STATE_SELECTION)
            }
            if (arrayList == null) {
                return ArrayList()
            }
            return arrayList
        }
    }
}