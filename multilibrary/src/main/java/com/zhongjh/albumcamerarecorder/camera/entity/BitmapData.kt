package com.zhongjh.albumcamerarecorder.camera.entity

import com.zhongjh.albumcamerarecorder.album.entity.Album2


/**
 * 拍照制造出来的数据源
 *
 * @author zhongjh
 */
class BitmapData(
    /**
     * 临时id
     */
    var temporaryId: Long,
    /**
     * uri路径
     */
    var uri: String,
    /**
     * 真实路径
     */
    var absolutePath: String
) {

    /**
     * 用于 DiffUtil.Callback 进行判断
     */
    fun equalsBitmapData(bitmapData: BitmapData): Boolean {
        if (temporaryId != bitmapData.temporaryId) {
            return false
        }

        if (uri != bitmapData.uri) {
            return false
        }
        if (absolutePath != bitmapData.absolutePath) {
            return false
        }
        return true
    }


}
