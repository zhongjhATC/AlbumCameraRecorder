package com.zhongjh.albumcamerarecorder.settings

import com.zhongjh.albumcamerarecorder.album.filter.BaseFilter
import com.zhongjh.albumcamerarecorder.album.listener.OnCheckedListener
import com.zhongjh.albumcamerarecorder.album.listener.OnSelectedListener
import com.zhongjh.albumcamerarecorder.settings.api.AlbumSettingApi
import com.zhongjh.common.enums.MimeType
import java.lang.ref.WeakReference
import java.util.*

/**
 * 相册设置
 *
 * @author zhongjh
 * @date 2018/12/27
 *
 * @param mediaTypeExclusive 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
 */
class AlbumSetting(mediaTypeExclusive: Boolean) : AlbumSettingApi {

    /**
     * 每次使用配置前都重新清除配置
     */
    private val mAlbumSpec: AlbumSpec = AlbumSpec.cleanInstance

    /**
     * 销毁事件
     */
    fun onDestroy() {
        mAlbumSpec.onSelectedListener = null
        mAlbumSpec.onCheckedListener = null
    }

    override fun mimeTypeSet(mimeTypes: Set<MimeType>): AlbumSetting {
        mAlbumSpec.mimeTypeSet = mimeTypes
        return this
    }

    override fun showSingleMediaType(showSingleMediaType: Boolean): AlbumSetting {
        mAlbumSpec.showSingleMediaType = showSingleMediaType
        return this
    }

    override fun countable(countable: Boolean): AlbumSetting {
        mAlbumSpec.countable = countable
        return this
    }

    override fun addFilter(baseFilter: BaseFilter): AlbumSetting {
        if (mAlbumSpec.baseFilters == null) {
            mAlbumSpec.baseFilters = ArrayList()
        }
        mAlbumSpec.baseFilters?.add(baseFilter)
        return this
    }

    override fun originalEnable(enable: Boolean): AlbumSetting {
        mAlbumSpec.originalEnable = enable
        return this
    }

    override fun maxOriginalSize(size: Int): AlbumSetting {
        mAlbumSpec.originalMaxSize = size
        return this
    }

    override fun spanCount(spanCount: Int): AlbumSetting {
        require(spanCount >= 1) { "spanCount cannot be less than 1" }
        mAlbumSpec.spanCount = spanCount
        return this
    }

    override fun gridExpectedSize(size: Int): AlbumSetting {
        mAlbumSpec.gridExpectedSize = size
        return this
    }

    override fun thumbnailScale(scale: Float): AlbumSetting {
        require(!(scale <= SCALE_ZERO || scale > SCALE_ONE)) { "Thumbnail scale must be between (0.0, 1.0)" }
        mAlbumSpec.thumbnailScale = scale
        return this
    }

    override fun setOnSelectedListener(listener: OnSelectedListener): AlbumSetting {
        mAlbumSpec.onSelectedListener = WeakReference(listener).get()
        return this
    }

    override fun setOnCheckedListener(listener: OnCheckedListener): AlbumSetting {
        mAlbumSpec.onCheckedListener = WeakReference(listener).get()
        return this
    }

    override fun slidingHiddenEnable(enable: Boolean): AlbumSetting {
        mAlbumSpec.slidingHiddenEnable = enable
        return this
    }

    companion object {
        private const val SCALE_ZERO = 0f
        private const val SCALE_ONE = 1f
    }

    init {
        mAlbumSpec.mediaTypeExclusive = mediaTypeExclusive
    }
}