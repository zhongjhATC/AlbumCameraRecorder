package com.zhongjh.multimedia.album.entity

import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MimeType

/**
 * 专辑
 *
 * @author zhongjh
 * @date 2022/9/16
 */
class Album {
    /**
     * id 专辑id
     */
    var id: Long = 0

    /**
     * 专辑的封面图path
     */
    var firstImagePath: String? = ""

    /**
     * 专辑的封面类型
     */
    var firstMimeType: String? = ""

    /**
     * 专辑名称
     */
    var name: String? = ""

    /**
     * 当前专辑下总共多少个文件
     */
    var count: Int = 0

    /**
     * 专辑下的图片、视频等数据
     */
    var data: List<LocalMedia> = ArrayList()

    /**
     * 是否选择
     */
    var isChecked = false

    /**
     * 当前专辑选择了多少个图片、视频
     */
    var checkedCount: Int = 0

    /**
     * 类型
     */
    var type: Set<MimeType> = MimeType.ofAll()

    /**
     * 判断如果id = -1的话，就是查询全部的意思
     * @return 是否全部
     */
    val isAll: Boolean
        get() = ALBUM_ID_ALL == id
    val isEmpty: Boolean
        get() = count == 0

    /**
     * 用于 DiffUtil.Callback 进行判断
     */
    fun equalsAlbum(album: Album): Boolean {
        if (id != album.id) {
            return false
        }
        if (firstImagePath != album.firstImagePath) {
            return false
        }
        if (firstMimeType != album.firstMimeType) {
            return false
        }
        if (name != album.name) {
            return false
        }
        if (count != album.count) {
            return false
        }
        if (isChecked != album.isChecked) {
            return false
        }
        if (checkedCount != album.checkedCount) {
            return false
        }
        if (type != album.type) {
            return false
        }
        return true
    }

    companion object {

        const val ALBUM_ID_ALL: Long = -1
    }
}