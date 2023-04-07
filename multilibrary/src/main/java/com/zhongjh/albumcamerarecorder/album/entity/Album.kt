package com.zhongjh.albumcamerarecorder.album.entity

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.album.loader.AlbumLoader
import kotlinx.android.parcel.Parcelize

/**
 * 专辑
 * @author zhihu
 */
@Parcelize
class Album internal constructor(
    var id: String,
    var coverUri: Uri,
    var displayName: String,
    var count: Long
) : Parcelable {

    /**
     *
     * 数量添加一个，目前是考虑如果有拍照功能，就数量+1
     */
    @Deprecated("作废，拍照已经独立出来", ReplaceWith("count++"))
    fun addCaptureCount() {
        count++
    }

    /**
     * 显示名称，可能返回“全部”
     * @return 返回名称
     */
    fun getDisplayName(context: Context): String {
        return if (isAll) {
            context.getString(R.string.z_multi_library_album_name_all)
        } else displayName
    }

    fun equalsAlbum(album: Album): Boolean {
        // coverUri
        if (coverUri != album.coverUri) {
            return false
        }
        // displayName
        if (displayName != album.displayName) {
            return false
        }
        // count
        if (count != album.count) {
            return false
        }
        // isChecked
        if (isChecked != album.isChecked) {
            return false
        }
        // checkedNum
        if (checkedNum != album.checkedNum) {
            return false
        }
        return true
    }

    /**
     * 判断如果id = -1的话，就是查询全部的意思
     * @return 是否全部
     */
    val isAll: Boolean
        get() = ALBUM_ID_ALL == id
    val isEmpty: Boolean
        get() = count == 0L

    /**
     * 是否进行了选择
     */
    var isChecked: Boolean = false

    /**
     * 当前专辑选择了多少个图片、视频
     */
    var checkedNum: Int = 0

    companion object {

        const val ALBUM_ID_ALL: String = (-1).toString()
        const val ALBUM_NAME_ALL = "All"

        /**
         * [Cursor] 构建一个新的实体 [Album]
         * 此方法不负责管理光标资源，如关闭、迭代等。
         */
        @JvmStatic
        fun valueOf(cursor: Cursor): Album {
            val column = cursor.getString(cursor.getColumnIndexOrThrow(AlbumLoader.COLUMN_URI))
            val bucketDisplayName =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))
            val count = cursor.getLong(cursor.getColumnIndexOrThrow(AlbumLoader.COLUMN_COUNT))
            return Album(
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_ID)),
                Uri.parse(column ?: ""),
                bucketDisplayName ?: "",
                count
            )
        }
    }
}