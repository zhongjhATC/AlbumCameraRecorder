package com.zhongjh.albumcamerarecorder.album.entity

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Parcelable
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

    /**
     * 判断如果id = -1的话，就是查询全部的意思
     * @return 是否全部
     */
    val isAll: Boolean
        get() = ALBUM_ID_ALL == id
    val isEmpty: Boolean
        get() = count == 0L

    companion object {

        const val ALBUM_ID_ALL: String = (-1).toString()
        const val ALBUM_NAME_ALL = "All"

        /**
         * [Cursor] 构建一个新的实体 [Album]
         * 此方法不负责管理光标资源，如关闭、迭代等。
         */
        @JvmStatic
        fun valueOf(cursor: Cursor): Album {
            val column = cursor.getString(cursor.getColumnIndex(AlbumLoader.COLUMN_URI))
            return Album(
                cursor.getString(cursor.getColumnIndex("bucket_id")),
                Uri.parse(column ?: ""),
                cursor.getString(cursor.getColumnIndex("bucket_display_name")),
                cursor.getLong(cursor.getColumnIndex(AlbumLoader.COLUMN_COUNT))
            )
        }
    }
}