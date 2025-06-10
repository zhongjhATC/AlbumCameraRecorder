package com.zhongjh.multimedia.album.entity

import com.zhongjh.common.entity.LocalMedia

/**
 * 封装多媒体文件集合的类
 *
 * @author zhongjh
 * @date 2023/7/26
 */
class MediaData(
    /**
     * 多媒体文件集合
     */
    var data: ArrayList<LocalMedia>,
    /**
     * 是否包含下一页
     */
    var isHasNextMore: Boolean
)