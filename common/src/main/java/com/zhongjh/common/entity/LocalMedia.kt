package com.zhongjh.common.entity;

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.widget.ImageView
import com.zhongjh.common.engine.ImageEngine
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.BitmapUtils
import com.zhongjh.common.utils.MediaUtils
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.io.File

/**
 * 多媒体文件
 * 该实体分别有多个不同的path
 * getAvailablePath：是必定可用的地址，如果对地址没有太苛刻的时候可以使用它，具体逻辑可以看该方法(比如支持压缩的话，该方法返回压缩路径)。
 * compressPath: 压缩后的路径，如果开启压缩配置后，将最终原图或者编辑后的图片进行压缩，然后赋值该属性
 * editorPath: 如果该图片裁剪或者编辑过，那么该属性会有值。
 * sandboxPath：沙盒路径，是配合 FileProvider 后形成的路径，未压缩、未编辑前的，即是原图
 * path：初始的uri路径，未压缩、未编辑前的，即是原图
 * absolutePath： 初始的真实路径，未压缩、未编辑前的，即是原图
 *
 * @author zhongjh
 * @date 2023/7/26
 *
 *
 */
@Parcelize
open class LocalMedia() : Parcelable, BaseMedia {

    /**
     * 文件id
     */
    var id: Long = 0

    /**
     * 压缩后的路径，如果开启压缩配置后，最终原图或者将编辑后的图片进行压缩，然后赋值该属性
     */
    var compressPath: String? = null

    /**
     * 如果该图片裁剪或者编辑过，那么该属性会有值。
     */
    var editorPath: String? = null

    /**
     * 沙盒路径，是配合 FileProvider 后形成的路径，未压缩、未编辑前的，即是原图
     */
    var sandboxPath: String? = null

    /**
     * 初始的uri路径，未压缩、未编辑前的，即是原图
     */
    var path: String = ""

    /**
     * 初始的真实路径，未压缩、未编辑前的，即是原图
     */
    var absolutePath: String = ""

    /**
     * 视频的持续时间
     */
    var duration: Long = 0

    /**
     * 角度
     */
    var orientation: Int = 0

    /**
     * 如果是被选中
     */
    var isChecked: Boolean = false

    /**
     * 是否裁剪的
     */
    var isCut: Boolean = false

    /**
     * 列表中的索引
     */
    var position: Int = 0

    /**
     * 媒体资源类型
     */
    var mimeType: String? = ""

    /**
     * 以AvailablePath为准
     * 图像或视频宽度
     * 如果出现0，开发人员需要额外处理
     */
    var width: Int = 0

    /**
     * 以AvailablePath为准
     * 图像或视频宽度
     * 如果出现0，开发人员需要额外处理
     */
    var height: Int = 0

    /**
     * 裁剪图片的宽度
     */
    var cropImageWidth: Int = 0

    /**
     * 裁剪图片的高度
     */
    var cropImageHeight: Int = 0

    /**
     * 裁剪比例X
     */
    var cropOffsetX: Int = 0

    /**
     * 裁剪比例Y
     */
    var cropOffsetY: Int = 0

    /**
     * 裁剪纵横比
     */
    var cropResultAspectRatio: Float = 0F

    /**
     * 文件大小
     */
    var size: Long = 0

    /**
     * 是否显示原始图像
     */
    var isOriginal: Boolean = false

    /**
     * 以AbsolutePath为准
     * 文件名称
     */
    var fileName: String? = ""

    /**
     * 以AbsolutePath为准
     * 父文件夹名称
     */
    var parentFolderName: String? = ""

    /**
     * 以AbsolutePath为准
     * 专辑ID
     */
    var bucketId: Long = -1

    /**
     * 以AbsolutePath为准
     * 文件创建时间
     */
    var dateAddedTime: Long = 0

    companion object : Parceler<LocalMedia> {

        override fun LocalMedia.write(parcel: Parcel, flags: Int) {
            parcel.writeLong(id)
            parcel.writeString(compressPath)
            parcel.writeString(editorPath)
            parcel.writeString(sandboxPath)
            parcel.writeString(path)
            parcel.writeString(absolutePath)
            parcel.writeLong(duration)
            parcel.writeInt(orientation)
            parcel.writeByte(if (isChecked) 1 else 0)
            parcel.writeByte(if (isCut) 1 else 0)
            parcel.writeInt(position)
            parcel.writeString(mimeType)
            parcel.writeInt(width)
            parcel.writeInt(height)
            parcel.writeInt(cropImageWidth)
            parcel.writeInt(cropImageHeight)
            parcel.writeInt(cropOffsetX)
            parcel.writeFloat(cropResultAspectRatio)
            parcel.writeLong(size)
            parcel.writeByte(if (isOriginal) 1 else 0)
            parcel.writeString(fileName)
            parcel.writeString(parentFolderName)
            parcel.writeLong(bucketId)
            parcel.writeLong(dateAddedTime)
        }

        override fun create(parcel: Parcel): LocalMedia {
            return LocalMedia(parcel)
        }

        /**
         * 构造LocalMedia
         *
         * @param id               资源id
         * @param path             资源路径
         * @param absolutePath     资源真实路径
         * @param fileName         文件名
         * @param parentFolderName 文件所在相册目录名称
         * @param duration         视频/音频时长
         * @param orientation 角度
         * @param mimeType         资源类型
         * @param width            资源宽
         * @param height           资源高
         * @param size             资源大小
         * @param bucketId         文件目录id
         * @param dateAdded  资源添加时间
         * @return
         */
        @JvmStatic
        fun parseLocalMedia(
            id: Long,
            path: String,
            absolutePath: String,
            fileName: String,
            parentFolderName: String,
            duration: Long,
            orientation: Int,
            mimeType: String,
            width: Int,
            height: Int,
            size: Long,
            bucketId: Long,
            dateAdded: Long
        ): LocalMedia {
            val localMedia = LocalMedia()
            localMedia.id = id
            localMedia.path = path
            localMedia.absolutePath = absolutePath
            localMedia.fileName = fileName
            localMedia.parentFolderName = parentFolderName
            localMedia.orientation = orientation
            localMedia.duration = duration
            localMedia.mimeType = mimeType
            localMedia.width = width
            localMedia.height = height
            localMedia.size = size
            localMedia.bucketId = bucketId
            localMedia.dateAddedTime = dateAdded
            return localMedia
        }
    }

    /**
     * 主要用于 parcel
     */
    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        compressPath = parcel.readString()
        editorPath = parcel.readString()
        sandboxPath = parcel.readString()
        val parcelPath = parcel.readString()
        path = parcelPath.let { parcelPath } ?: let { "" }
        val parcelAbsolutePath = parcel.readString()
        absolutePath = parcelAbsolutePath.let { parcelAbsolutePath } ?: let { "" }
        duration = parcel.readLong()
        orientation = parcel.readInt()
        isChecked = parcel.readByte() != 0.toByte()
        isCut = parcel.readByte() != 0.toByte()
        position = parcel.readInt()
        val parcelMimeType = parcel.readString()
        mimeType = parcelMimeType.let { parcelMimeType } ?: let { "" }
        width = parcel.readInt()
        height = parcel.readInt()
        cropImageWidth = parcel.readInt()
        cropImageHeight = parcel.readInt()
        cropOffsetX = parcel.readInt()
        cropResultAspectRatio = parcel.readFloat()
        size = parcel.readLong()
        isOriginal = parcel.readByte() != 0.toByte()
        val parcelFileName = parcel.readString()
        fileName = parcelFileName.let { parcelFileName } ?: let { "" }
        val parcelParentFolderName = parcel.readString()
        parentFolderName = parcelParentFolderName.let { parcelParentFolderName } ?: let { "" }
        bucketId = parcel.readLong()
        dateAddedTime = parcel.readLong()
    }

    /**
     * 赋值一个新的path，借由这个新的path，修改相关参数
     */
    constructor(
        context: Context, localMedia: LocalMedia, compressionFile: File, isCompress: Boolean
    ) : this() {
        updateFile(context, localMedia, compressionFile, isCompress)
    }

    /**
     * 从 LocalMedia 赋值到另外一个新的 LocalMedia
     * 之所以这样做是因为 Parcelable 如果使用的是看似父类其实是子类就会出问题
     */
    constructor(localMedia: LocalMedia) : this() {
        copyLocalMedia(localMedia)
    }

    /**
     * 用于 DiffUtil.Callback 进行判断
     */
    fun equalsLocalMedia(localMedia: LocalMedia): Boolean {
        if (id != localMedia.id) {
            return false
        }
        if (compressPath != localMedia.compressPath) {
            return false
        }
        if (editorPath != localMedia.editorPath) {
            return false
        }
        if (sandboxPath != localMedia.sandboxPath) {
            return false
        }
        if (path != localMedia.path) {
            return false
        }
        if (duration != localMedia.duration) {
            return false
        }
        if (isChecked != localMedia.isChecked) {
            return false
        }
        if (isCut != localMedia.isCut) {
            return false
        }
        if (position != localMedia.position) {
            return false
        }
        if (mimeType != localMedia.mimeType) {
            return false
        }
        if (width != localMedia.width) {
            return false
        }
        if (height != localMedia.height) {
            return false
        }
        if (cropImageWidth != localMedia.cropImageWidth) {
            return false
        }
        if (cropImageHeight != localMedia.cropImageHeight) {
            return false
        }
        if (cropOffsetX != localMedia.cropOffsetX) {
            return false
        }
        if (cropOffsetY != localMedia.cropOffsetY) {
            return false
        }
        if (cropResultAspectRatio != localMedia.cropResultAspectRatio) {
            return false
        }
        if (size != localMedia.size) {
            return false
        }
        if (isOriginal != localMedia.isOriginal) {
            return false
        }
        if (fileName != localMedia.fileName) {
            return false
        }
        if (parentFolderName != localMedia.parentFolderName) {
            return false
        }
        if (bucketId != localMedia.bucketId) {
            return false
        }
        if (dateAddedTime != localMedia.dateAddedTime) {
            return false
        }
        return true
    }

    /**
     * 返回大类
     */
    fun getMediaType(): Int {
        if (isVideo()) {
            return MediaType.TYPE_VIDEO
        }
        if (isAudio()) {
            return MediaType.TYPE_AUDIO
        }
        return MediaType.TYPE_PICTURE
    }

    /**
     * 不包含gif
     */
    fun isImage(): Boolean {
        return mimeType == MimeType.JPEG.toString() || mimeType == MimeType.PNG.toString() || mimeType == MimeType.BMP.toString() || mimeType == MimeType.WEBP.toString()
    }

    /**
     * 单纯gif
     */
    fun isGif(): Boolean {
        return mimeType == MimeType.GIF.toString()
    }

    /**
     * 包含gif
     */
    fun isImageOrGif(): Boolean {
        return mimeType == MimeType.JPEG.toString() || mimeType == MimeType.PNG.toString() || mimeType == MimeType.GIF.toString() || mimeType == MimeType.BMP.toString() || mimeType == MimeType.WEBP.toString()
    }

    /**
     * 是否音频
     */
    fun isAudio(): Boolean {
        return mimeType == MimeType.AAC.toString() || mimeType == MimeType.AUDIO_MPEG.toString()
    }

    /**
     * 是否视频
     */
    override fun isVideo(): Boolean {
        return mimeType == MimeType.MPEG.toString() || mimeType == MimeType.MP4.toString() || mimeType == MimeType.QUICKTIME.toString() || mimeType == MimeType.THREEGPP.toString() || mimeType == MimeType.THREEGPP2.toString() || mimeType == MimeType.MKV.toString() || mimeType == MimeType.WEBM.toString() || mimeType == MimeType.TS.toString() || mimeType == MimeType.AVI.toString()
    }

    /**
     * 加载图片
     */
    override fun loadImage(context: Context, imageEngine: ImageEngine, imageView: ImageView) {
        val size = getRealSizeFromMedia(this)
        val mediaComputeSize = BitmapUtils.getComputeImageSize(size[0], size[1])
        val width = mediaComputeSize[0]
        val height = mediaComputeSize[1]
        imageEngine.loadUrlImage(context, width, height, imageView, editorPath ?: path)
    }

    /**
     * getAvailablePath：是必定可用的地址，如果对地址没有太苛刻的时候可以使用它，具体逻辑可以看该方法(比如支持压缩的话，该方法返回压缩路径)。
     * compressPath: 压缩后的路径，如果开启压缩配置后，最终原图或者将编辑后的图片进行压缩，然后赋值该属性
     * editorPath: 如果该图片裁剪或者编辑过，那么该属性会有值。
     * sandboxPath：沙盒路径，是配合 FileProvider 后形成的路径，未压缩、未编辑前的，即是原图
     * path：初始的真实路径，未压缩、未编辑前的，即是原图
     *
     * @return 是必定可用的地址
     */
    fun getAvailablePath(): String? {
        if (compressPath != null) {
            return compressPath
        } else if (editorPath != null) {
            return editorPath
        }
        return path
    }

    /**
     * 场景：在相册预览等界面迁移图片到配置文件夹处，重新生成新的地址
     * 修改新的file
     *
     * @param context 上下文
     * @param localMedia 实体
     * @param compressionFile 压缩文件
     * @param isCompress 是否压缩
     */
    private fun updateFile(
        context: Context, localMedia: LocalMedia, compressionFile: File, isCompress: Boolean
    ) {
        copyLocalMedia(localMedia)
        compressPath = compressionFile.absolutePath
        size = compressionFile.length()
        if (isImageOrGif()) {
            val imageWidthAndHeight: IntArray =
                MediaUtils.getImageWidthAndHeight(compressionFile.absolutePath)
            width = imageWidthAndHeight[0]
            height = imageWidthAndHeight[1]
        } else if (isVideo()) {
            // 有些手机视频拍照没有宽高的
            if (localMedia.width == 0) {
                val mediaExtraInfo = MediaUtils.getVideoSize(context, compressionFile.absolutePath)
                width = mediaExtraInfo.width
                height = mediaExtraInfo.height
                duration = mediaExtraInfo.duration
            } else {
                width = localMedia.width
                height = localMedia.height
            }
        }
    }

    /**
     * 深度拷贝
     */
    private fun copyLocalMedia(localMedia: LocalMedia) {
        id = localMedia.id
        compressPath = localMedia.compressPath
        editorPath = localMedia.editorPath
        sandboxPath = localMedia.sandboxPath
        path = localMedia.path
        absolutePath = localMedia.absolutePath
        duration = localMedia.duration
        orientation = localMedia.orientation
        isChecked = localMedia.isChecked
        isCut = localMedia.isCut
        position = localMedia.position
        mimeType = localMedia.mimeType
        width = localMedia.width
        height = localMedia.height
        cropImageWidth = localMedia.cropImageWidth
        cropImageHeight = localMedia.cropImageHeight
        cropOffsetX = localMedia.cropOffsetX
        cropOffsetY = localMedia.cropOffsetY
        cropResultAspectRatio = localMedia.cropResultAspectRatio
        size = localMedia.size
        isOriginal = localMedia.isOriginal
        fileName = localMedia.fileName
        parentFolderName = localMedia.parentFolderName
        bucketId = localMedia.bucketId
        dateAddedTime = localMedia.dateAddedTime
    }

    private fun getRealSizeFromMedia(localMedia: LocalMedia): IntArray {
        return intArrayOf(localMedia.width, localMedia.height)
    }

}
