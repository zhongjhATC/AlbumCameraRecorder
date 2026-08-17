package com.zhongjh.multimedia.camera.ui.camera.motion

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Xml
import androidx.core.graphics.scale
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import com.zhongjh.common.utils.LogUtil
import com.zhongjh.multimedia.camera.ui.camera.motion.colorspace.HdrToneMapper
import com.zhongjh.multimedia.camera.ui.camera.motion.model.CapturedFrame
import com.zhongjh.multimedia.camera.ui.camera.motion.model.EncodedAudio
import com.zhongjh.multimedia.camera.ui.camera.motion.model.EncodedAudioPacket
import com.zhongjh.multimedia.camera.ui.camera.motion.model.ExportConfig
import com.zhongjh.multimedia.camera.ui.camera.motion.model.ExportDirectory
import com.zhongjh.multimedia.camera.ui.camera.motion.model.ExportFormat
import com.zhongjh.multimedia.camera.ui.camera.motion.model.ExportResult
import com.zhongjh.multimedia.camera.ui.camera.motion.model.PcmAudio
import com.zhongjh.multimedia.camera.ui.camera.motion.model.VideoClipResult
import com.zhongjh.multimedia.camera.ui.camera.motion.model.VideoMetadata
import com.zhongjh.multimedia.camera.ui.camera.motion.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.StringWriter
import java.nio.ByteBuffer
import kotlin.coroutines.cancellation.CancellationException

/**
 * 帧导出核心执行器 FrameExporter
 * 核心能力：将视频截取的帧画面导出为静态图片 / Google 标准动态照片(Motion Photo)
 *
 * 支持完整能力清单：
 * 1. 静态图片导出：JPEG / PNG / WebP 三种格式，可自定义压缩质量、最大分辨率缩放
 * 2. 动态照片导出：遵循 Google Motion Photo 规范（JPEG主图尾部拼接MP4短视频片段）
 * 3. HDR 色彩空间映射：HDR画面自动转SDR色域，兼容普通相册查看
 * 4. 完整EXIF元数据持久化：拍摄时间、曝光参数、设备信息、GPS等写入图片
 * 5. 双存储写入策略：系统MediaStore图库 或 SAF自定义授权文件夹（作用域存储兼容Android10+）
 * 6. 音频自动转码：MP4不兼容音频（LPCM/AC3/FLAC/Opus/MP3）自动转AAC嵌入视频片段
 * 7. 协程异步执行、异常捕获、Bitmap内存自动回收、导出失败文件自动清理
 *
 * 依赖关联类：
 * [HdrToneMapper] HDR转SDR色调映射工具
 * [MetadataWriter] EXIF元数据写入封装
 * [DateTimeUtils] 时间字符串转EXIF标准格式、时间戳解析
 * [LogUtil] 统一日志打印工具
 * 数据模型：[CapturedFrame] 捕获帧信息、[ExportConfig] 导出配置、[ExportResult] 导出结果封装
 *
 */
class FrameExporter(private val context: Context) {

    companion object {
        /** XMP 标准Adobe命名空间固定URI，用于MotionPhoto写入XMP元数据 */
        private const val XMP_NAMESPACE_URI = "http://ns.adobe.com/xap/1.0/\u0000"

        /** 未指定自定义文件名时的默认文件前缀 */
        private const val DEFAULT_CUSTOM_FILENAME = "Motion"

        // ===================== AAC音频转码全局常量 =====================
        /** AAC单声道比特率 96kbps，双声道自动翻倍 */
        private const val AAC_BIT_RATE_PER_CHANNEL = 96_000

        /** MediaCodec dequeue 阻塞超时时间（微秒）10ms */
        private const val CODEC_DEQUEUE_TIMEOUT_US = 10_000L

        /** 解码器/编码器连续空闲最大轮次，超过判定卡死终止（约6秒） */
        private const val MAX_CODEC_IDLE_ROUNDS = 600

        /** 音频拷贝单次缓冲区大小 64KB */
        private const val AUDIO_COPY_CHUNK_BYTES = 64 * 1024

        /** PCM原始音频读取缓冲区大小 256KB */
        private const val PCM_READ_BUFFER_BYTES = 256 * 1024

        /** PCM内存缓存最大上限 15MB，防止超大音频OOM */
        private const val MAX_PCM_BUFFER_BYTES = 15 * 1024 * 1024
    }

    /**
     * 协程异步导出单张静态帧图片
     * 完整链路：硬件Bitmap转软件Bitmap → HDR色调映射 → 分辨率缩放 → 文件写入 → EXIF元数据写入 → 内存回收
     * @param bitmap 原始帧Bitmap画面
     * @param frame 捕获帧元数据封装对象（包含色彩空间、拍摄时间、原始视频信息）
     * @param config 导出全局配置（格式、质量、最大分辨率、是否保留元数据、导出目录）
     * @param customExportTreeUri 用户自定义目录根Uri，为空则写入系统MediaStore图库
     * @return ExportResult 导出成功/失败结果封装
     */
    suspend fun exportStaticFrame(bitmap: Bitmap, frame: CapturedFrame, config: ExportConfig, customExportTreeUri: Uri? = null): ExportResult = withContext(Dispatchers.IO) {
        // 声明过程中产生的临时Bitmap，最终统一回收释放内存
        var softBitmap: Bitmap? = null
        var processedBitmap: Bitmap? = null
        var finalBitmap: Bitmap? = null
        var outputUri: Uri? = null
        try {
            // 步骤1：如果是HARDWARE则转为ARGB_8888
            softBitmap = ensureSoftwareBitmap(bitmap)

            // 步骤2：如果是HDR图像，则转SDR色域映射，普通相册才能正常显示
            processedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                HdrToneMapper.process(bitmap = softBitmap, colorSpaceInfo = frame.colorSpace)
            } else {
                softBitmap
            }

            // 步骤3：根据配置最大边长缩放图片，限制输出分辨率
            finalBitmap = scaleBitmap(processedBitmap, config.maxResolution)
            val resultWidth = finalBitmap.width
            val resultHeight = finalBitmap.height

            // 步骤4：生成过滤非法字符的合规文件名
            val fileName = generateFileName(frame, config)

            // 步骤5：将原始拍摄时间字符串转为MediaStore标准毫秒时间戳
            val dateTakenMs = frame.metadata.dateTime?.let { DateTimeUtils.parseToMillis(it) }

            // 步骤6：分支逻辑 - 用户自定义目录根Uri / 系统图库MediaStore
            val savedUri = if (customExportTreeUri != null) {
                saveToCustomDirectory(finalBitmap, fileName, config, customExportTreeUri)
            } else {
                saveToMediaStore(finalBitmap, fileName, config, dateTakenMs)
            }
            outputUri = savedUri

            // 步骤7：配置开关控制是否写入完整EXIF拍摄元数据
            var metadataPreserved = false
            if (config.preserveMetadata) {
                metadataPreserved = writeMetadataToUri(savedUri, frame.metadata)
            }

            ExportResult.Success(
                outputPath = savedUri.toString(),
                width = resultWidth,
                height = resultHeight,
                fileSizeBytes = getFileSize(savedUri),
                format = config.format,
                isMotionPhoto = false,
                metadataPreserved = metadataPreserved
            )
        } catch (e: CancellationException) {
            // 协程被手动取消中断导出，删掉半截生成的残缺文件，避免残留无效图片
            outputUri?.let { cleanupFailedOutput(it) }
            // 继续向上抛出取消异常，让上层协程感知到取消状态
            throw e
        } catch (e: OutOfMemoryError) {
            // 内存溢出，一般是图片分辨率过大占用内存超标
            ExportResult.Error("Image too large to process. Try reducing resolution or using JPEG format.", e)
        } catch (e: SecurityException) {
            // 存储权限被拒绝，SAF授权失效、文件读写权限不足都会触发
            ExportResult.Error("Storage permission denied. Please grant storage access.", e)
        } catch (e: Exception) {
            // 其余所有未知异常统一兜底封装错误信息返回
            ExportResult.Error("Export failed: ${e.message ?: e.javaClass.simpleName}", e)
        } finally {
            // 无论导出成功/失败/取消，最后统一回收所有临时Bitmap，释放图片内存 - 多重判断作用：只回收副本Bitmap，原始输入bitmap不回收，防止外部调用方图片被误销毁
            if (finalBitmap !== processedBitmap && finalBitmap !== bitmap) {
                finalBitmap?.recycle()
            }
            if (processedBitmap !== bitmap && processedBitmap !== softBitmap) {
                processedBitmap?.recycle()
            }
            if (softBitmap !== bitmap) {
                softBitmap?.recycle()
            }
        }
    }

    /**
     * 导出Google标准动态照片(Motion Photo)
     * 标准封装结构：JPEG静态主图 + APP1段XMP GCamera标识元数据 + 文件末尾拼接MP4短视频片段
     * 系统相册识别规则：解析图片内部XMP标记，长按图片自动播放尾部附加的短视频
     * 重要执行顺序硬性约束：必须先写入EXIF信息，再注入Motion Photo专属XMP数据；
     * 顺序颠倒会被ExifInterface覆盖XMP字段，导致相册无法识别为动态照片
     * @param videoUri 原视频文件Uri，用来抽取关键帧前后的短视频片段
     * @param bitmap 选中的静态封面关键帧原图
     * @param frame 帧完整元数据包：包含拍摄时间戳、色彩空间、视频基础参数、旋转角度等
     * @param config 导出配置：动态视频前后时长、是否静音、最大分辨率、是否保留原图EXIF等
     * @param customExportTreeUri SAF用户自定义文件夹授权Uri；传null则直接存入系统MediaStore图库
     * @return ExportResult 导出结果实体，audioDropped字段标记音频是否因转码异常被丢弃
     */
    suspend fun exportMotionPhoto(videoUri: Uri, bitmap: Bitmap, frame: CapturedFrame, config: ExportConfig, customExportTreeUri: Uri? = null): ExportResult = withContext(Dispatchers.IO) {
        // 声明过程中产生的临时Bitmap，最终统一回收释放内存
        var softBitmap: Bitmap? = null
        var processedBitmap: Bitmap? = null
        var finalBitmap: Bitmap? = null
        // 记录最终生成文件Uri，异常时用来删除残缺文件
        var outputUri: Uri? = null


        try {
            val effectiveConfig = config
            // 步骤1：如果是HARDWARE则转为ARGB_8888
            softBitmap = ensureSoftwareBitmap(bitmap)

            // 步骤2：如果是HDR图像，则转SDR色域映射，普通相册才能正常显示
            processedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                HdrToneMapper.process(bitmap = softBitmap, colorSpaceInfo = frame.colorSpace)
            } else {
                softBitmap
            }

            // 步骤3：根据配置最大边长缩放图片，限制输出分辨率
            finalBitmap = scaleBitmap(processedBitmap, config.maxResolution)
            val resultWidth = finalBitmap.width
            val resultHeight = finalBitmap.height

            // 步骤4：生成过滤非法字符、动态照片标识的合规文件名
            val fileName = generateFileName(frame, effectiveConfig, isMotion = true)

            // 步骤5：动态照片规范强制要求主载体为JPEG格式，先把封面帧压缩成JPEG字节数组
            val jpegBytes = compressBitmapToBytes(finalBitmap, Bitmap.CompressFormat.JPEG, config.quality)

            // 步骤6：判断是否写入图片EXIF拍摄元数据（必须在拼接视频、写入XMP之前执行）。最终返回给exifJpegBytes和metadataPreserved
            val (exifJpegBytes, metadataPreserved) = if (config.preserveMetadata) {
                writeExifToJpegBytes(jpegBytes, frame.metadata)
            } else {
                Pair(jpegBytes, false)
            }

            // 前后时长转微秒单位,1_000_000就是1000000,只是分隔符更容易观看而已
            val beforeDurationUs = (config.motionDurationBeforeS * 1_000_000).toLong()
            val afterDurationUs = (config.motionDurationAfterS * 1_000_000).toLong()

            // 抽取关键帧前后MP4片段，自动对齐前向关键帧保证MP4可播放
            val clipResult = extractVideoClip(
                videoUri = videoUri,
                centerTimestampUs = frame.timestampUs,
                beforeDurationUs = beforeDurationUs,
                afterDurationUs = afterDurationUs,
                muteAudio = config.muteAudio,
                videoDurationUs = frame.metadata.durationMs * 1000L,
                rotation = frame.metadata.rotation
            )
            val videoClipFile = clipResult.file

            try {
                // 校验临时MP4有效性
                if (!videoClipFile.exists() || videoClipFile.length() == 0L || clipResult.videoSamplesWritten == 0) {
                    return@withContext ExportResult.Error(
                        "Failed to extract video clip. The video format may not be supported for motion photo export."
                    )
                }

                // 计算帧在剪辑内播放锚点时间戳
                val presentationTimestampUs = (frame.timestampUs - clipResult.actualStartUs).coerceAtLeast(0L)

                // 插入MotionPhoto专属XMP元数据到JPEG头部
                val xmpJpegBytes = injectMotionPhotoXmp(
                    jpegBytes = exifJpegBytes, videoLength = videoClipFile.length(), presentationTimestampUs = presentationTimestampUs
                )

                // 二进制拼接JPEG+MP4写入目标存储
                val dateTakenMs = frame.metadata.dateTime?.let { DateTimeUtils.parseToMillis(it) }
                val savedUri = if (customExportTreeUri != null) {
                    saveMotionPhotoToCustomDirectory(xmpJpegBytes, videoClipFile, fileName, customExportTreeUri)
                } else {
                    saveMotionPhotoToMediaStore(xmpJpegBytes, videoClipFile, fileName, effectiveConfig, dateTakenMs)
                }
                outputUri = savedUri

                // 封装成功返回结果，标记各项导出状态
                ExportResult.Success(
                    outputPath = savedUri.toString(),
                    width = resultWidth,
                    height = resultHeight,
                    fileSizeBytes = getFileSize(savedUri),
                    format = effectiveConfig.format,
                    isMotionPhoto = true,
                    metadataPreserved = metadataPreserved,
                    requestedFormat = null,
                    // 音频丢弃标记：未开启静音但有音轨，最终却没有封装进视频时为true
                    audioDropped = !config.muteAudio && clipResult.hasAudioTrack && !clipResult.audioIncluded
                )
            } finally {
                videoClipFile.delete()
            }
        } catch (e: CancellationException) {
            outputUri?.let { cleanupFailedOutput(it) }
            throw e
        } catch (e: OutOfMemoryError) {
            ExportResult.Error("Image too large to process. Try reducing resolution or using JPEG format.", e)
        } catch (e: SecurityException) {
            ExportResult.Error("Storage permission denied. Please grant storage access.", e)
        } catch (e: Exception) {
            ExportResult.Error("Motion photo export failed: ${e.message ?: e.javaClass.simpleName}", e)
        } finally {
            if (finalBitmap !== processedBitmap && finalBitmap !== bitmap) {
                finalBitmap?.recycle()
            }
            if (processedBitmap !== bitmap && processedBitmap !== softBitmap) {
                processedBitmap?.recycle()
            }
            if (softBitmap !== bitmap) {
                softBitmap?.recycle()
            }
        }
    }

    /**
     * 将GPU硬件HARDWARE Bitmap转为App堆内存ARGB_8888软件位图
     * 兼容适配：Bitmap.Config.HARDWARE仅API26(O)才存在，低版本直接跳过判断，无编译报错
     * HARDWARE位图限制：像素存显存，不支持compress压缩、Canvas绘制、像素读写，导出图片会崩溃
     * @param bitmap 原始帧Bitmap（硬解预览帧大概率为Hardware类型）
     * @return 可安全压缩导出的标准软件ARGB_8888位图
     * @throws IllegalStateException 硬件位图拷贝失败抛出异常
     */
    private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
            return bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: throw IllegalStateException("Failed to convert hardware bitmap to software bitmap")
        }
        return bitmap
    }

    /**
     * 根据配置的最大边长限制缩放Bitmap
     * 业务场景：导出图片时限制图片宽高上限，避免超大原图导致OOM、文件过大、相册加载卡顿
     * 缩放规则：
     * 1. 取原图宽、高中的最长边作为基准进行等比例缩小，严格保持原图宽高比，不会拉伸变形
     * 2. 仅当原图最长边 > maxResolution 时才执行缩放；原图尺寸更小/相等直接返回原图，无内存拷贝开销
     * 3. 缩放开启双线性过滤(第三个参数true)，缩放后画面更平滑，避免锯齿、模糊失真
     * 4. 宽高计算后强制最小为1，防止极端0尺寸Bitmap崩溃
     * @param bitmap 原始输入位图（经过硬件转换、HDR处理后的内存位图）
     * @param maxResolution 导出配置允许的图片最大边长；传null代表不做缩放，原图直接使用
     * @return 缩放后的新Bitmap对象；无需缩放时返回原bitmap引用（不创建新对象节省内存）
     */
    private fun scaleBitmap(bitmap: Bitmap, maxResolution: Int?): Bitmap {
        // 配置未限制最大分辨率，直接返回原图，跳过缩放逻辑
        if (maxResolution == null) {
            return bitmap
        }

        // 获取图片长边（宽、高中更大的值），以长边为缩放基准保证等比例不变形
        val maxSide = maxOf(bitmap.width, bitmap.height)

        // 原图长边小于等于限制值，尺寸达标无需缩放，直接复用原图
        if (maxSide <= maxResolution) {
            return bitmap
        }

        // 计算缩放比例：目标最大边长 / 当前最长边.使用Float浮点运算，防止整数除法丢失精度导致尺寸偏差
        val scaleRate = maxResolution.toFloat() / maxSide

        // 按比例计算新宽高，coerceAtLeast(1)兜底，避免极小图缩放后宽/高为0引发创建Bitmap崩溃
        val w = (bitmap.width * scaleRate).toInt().coerceAtLeast(1)
        val h = (bitmap.height * scaleRate).toInt().coerceAtLeast(1)

        // filter = true 开启双线性插值滤波，缩放图像抗锯齿，画面更细腻；false为邻近插值，锯齿严重
        return bitmap.scale(w, h, true)
    }

    /**
     * 生成导出文件的文件名
     * 规则：优先使用用户自定义文件名；没有自定义就用默认前缀 IMG / MVIMG
     * @param frame 当前帧对象，携带帧的微秒时间戳
     * @param config 导出配置，里面存放用户填写的自定义文件名、导出格式
     * @param isMotion 是否是Google动态照片；动态照片后缀固定用jpeg，前缀变成MVIMG
     * @return 处理完非法字符的最终文件名，例如 IMG_1755000000000_1754999999.jpg
     */
    private fun generateFileName(frame: CapturedFrame, config: ExportConfig, isMotion: Boolean = false): String {
        // 判断文件后缀：动态照片强制用jpeg后缀；普通图片使用配置里设置的格式后缀
        val extension = if (isMotion) ExportFormat.JPEG.extension else config.format.extension

        // System.currentTimeMillis()：当前手机系统的现实时间（毫秒），用于文件名，方便区分本次保存操作
        val timestamp = System.currentTimeMillis()

        // frame.timestampUs：视频内部帧的原始微秒时间戳，除以1000转为毫秒
        val timeStr = frame.timestampUs / 1000

        // 如果用户填写了自定义文件名，并且不是空白字符串，走自定义名字逻辑
        config.customFileName?.takeIf { it.isNotBlank() }?.let { custom ->
            // sanitizeFileName：过滤掉文件名不允许的特殊非法字符
            // substringBeforeLast('.')：把用户输入的名字后面自带的后缀删掉，避免出现 xxx.jpg.jpg
            // ifBlank：过滤完字符为空，就使用兜底默认文件名
            val baseName = sanitizeFileName(custom).substringBeforeLast('.').ifBlank { DEFAULT_CUSTOM_FILENAME }
            // 拼接：自定义名字_系统当前时间_帧原始时间.后缀
            return "${baseName}_${timestamp}_${timeStr}.${extension}"
        }

        // 没有自定义文件名：动态照片前缀 MVIMG，普通图片前缀 IMG
        val prefix = if (isMotion) "MVIMG" else "IMG"
        // 拼接默认文件名：前缀_系统当前时间_帧原始时间.后缀
        return "${prefix}_${timestamp}_${timeStr}.${extension}"
    }

    /**
     * 清洗文件名，移除所有系统文件系统不支持的非法字符
     * 适配Android MediaStore / SAF DocumentFile / Windows/Mac/Linux全平台文件名规范
     * 过滤范围：
     * 1. 0~31 ASCII不可见控制字符（换行、制表、空字符等）统一替换为下划线
     * 2. Windows/Android禁止路径符号：\ / : * ? " < > | 全部替换下划线
     * 3. 连续多个点号... 替换为单个下划线，避免隐藏文件/后缀错乱
     * 4. 首尾清理：删除下划线、点、空格，防止目录识别异常
     * 5. 清洗后为空字符串时，返回默认文件名兜底
     * @param name 用户原始自定义文件名（可能带特殊符号、空格、点）
     * @return 完全合规、可直接用于创建文件的干净文件名
     */
    internal fun sanitizeFileName(name: String): String {
        return name
            // 替换所有ASCII 0~31不可见控制字符为下划线
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "_")
            // 替换文件系统禁止的特殊路径符号
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            // 连续多个英文句号统一替换单个下划线（防止...xxx隐藏文件）
            .replace(Regex("\\.\\.+"), "_")
            // 裁剪首尾多余：下划线、点、空格
            .trim { it == '_' || it == '.' || it.isWhitespace() }
            // 清洗完为空字符串（全是非法字符），返回默认文件名兜底
            .ifBlank { DEFAULT_CUSTOM_FILENAME }
    }

    /**
     * 把图片保存到用户手动选的自定义文件夹
     * @param bitmap 要保存的图片位图
     * @param fileName 处理好的合规文件名
     * @param config 导出配置，包含图片格式、画质
     * @param treeUri 用户授权文件夹的地址
     * @return 保存后文件的Uri地址
     */
    private fun saveToCustomDirectory(bitmap: Bitmap, fileName: String, config: ExportConfig, treeUri: Uri): Uri {
        // 根据传入的文件夹地址，拿到文件夹操作对象
        val tree = DocumentFile.fromTreeUri(context, treeUri) ?: throw IOException("Failed to access custom directory — permission may have been revoked")
        // 在文件夹里新建对应类型的图片文件
        val doc = tree.createFile(config.format.mimeType, fileName) ?: throw IOException("Failed to create file in custom directory")
        val uri = doc.uri
        try {
            val outputStream = context.contentResolver.openOutputStream(uri) ?: throw IOException("Failed to open output stream for custom directory")
            // 打开文件写入流，把图片压缩后写入文件
            outputStream.use { os ->
                val fmt = toCompressFormat(config.quality)
                // 压缩图片写入，失败直接抛异常
                if (!bitmap.compress(fmt, config.quality, os)) {
                    throw IOException("Failed to compress bitmap (format: ${config.format}, config: ${bitmap.config})")
                }
            }
            // 保存成功，返回文件地址
            return uri
        } catch (e: Exception) {
            // 保存出错，删掉刚才创建的空文件，避免残留垃圾文件
            try {
                doc.delete()
            } catch (deleteException: Exception) {
                LogUtil.e("FrameExporter", "Failed to delete file after export failure", deleteException)
            }
            throw e
        }
    }

    /**
     * 将图片保存到系统相册（MediaStore公共图库）
     * 适配新旧安卓版本存储规则：Android10及以上使用分区存储IS_PENDING标记，旧系统直接写文件路径
     * 写入失败会自动删除相册里残留空文件，不会产生垃圾数据
     * @param bitmap 需要保存的图片画布
     * @param fileName 清洗好的合规文件名（不含特殊符号）
     * @param config 导出配置，包含图片格式、画质、相册文件夹路径
     * @param dateTakenMs 图片拍摄时间戳（毫秒），为空则不记录拍摄时间
     * @return 相册中该图片的资源Uri，外部可用来读取/编辑图片
     */
    private fun saveToMediaStore(bitmap: Bitmap, fileName: String, config: ExportConfig, dateTakenMs: Long?): Uri {
        // 构建相册文件信息容器，存放文件名、格式、存储路径、拍摄时间等信息
        val contentValues = ContentValues().apply {
            // 设置文件展示名（相册列表显示的名字）
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            // 设置图片MIME类型（jpg/png/webp）
            put(MediaStore.Images.Media.MIME_TYPE, config.format.mimeType)
            // 有拍摄时间就存入相册的拍摄时间字段
            dateTakenMs?.let { put(MediaStore.Images.Media.DATE_TAKEN, it) }
            // Android10(Q)及新版本：分区存储规则，使用相对路径+待处理标记
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 配置图片存放的相册子文件夹（DCIM/自定义目录）
                put(MediaStore.Images.Media.RELATIVE_PATH, config.exportDirectory.relativePath)
                // IS_PENDING=1 标记文件还没写完，相册暂时不扫描展示
                put(MediaStore.Images.Media.IS_PENDING, 1)
            } else {
                // Android9及以下旧版本：直接拼接完整本地文件路径
                put(MediaStore.Images.Media.DATA, resolveLegacyOutputFile(fileName, config.exportDirectory).absolutePath)
            }
        }

        // 向系统相册数据库插入一条图片记录，占位
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: throw IOException("相册创建图片记录失败，可能存储空间已满")

        try {
            val outputStream = context.contentResolver.openOutputStream(uri) ?: throw IOException("Failed to open output stream for MediaStore entry")
            // 打开文件写入流，把图片压缩写入相册文件
            outputStream.use { os ->
                // 根据配置获取对应的图片压缩格式
                val fmt = toCompressFormat(config.quality)
                // 压缩图片写入流，写入失败抛出异常
                if (!bitmap.compress(fmt, config.quality, os)) {
                    throw IOException("图片压缩写入相册失败")
                }
            }
            // 安卓10+ 写完文件，取消待处理标记，相册就能正常刷新显示这张图
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }
            return uri
        } catch (e: Exception) {
            // 写入过程报错，删除相册里这条空白记录，避免残留无效图片
            try {
                context.contentResolver.delete(uri, null, null)
            } catch (deleteException: Exception) {
                LogUtil.e("FrameExporter", "Failed to delete URI after export failure", deleteException)
            }
            throw e
        }
    }

    /**
     * 适配安卓10以下旧系统，拼接图片完整本地文件路径
     * 旧系统不用SAF、不用MediaStore相对路径，直接生成真实File对象
     * 逻辑：根据配置的目录前缀，区分DCIM/电影/图片三大系统文件夹，再拼接子目录和文件名
     * @param name 图片文件名
     * @param dir 导出目录配置，包含相对路径
     * @return 可直接写入的本地文件对象
     */
    private fun resolveLegacyOutputFile(name: String, dir: ExportDirectory): File {
        // 获取配置里的相对路径，类似 "motion/Shot"
        val rel = dir.relativePath
        // 截取第一层根文件夹（DCIM / MOVIES / PICTURES）
        val rootSeg = rel.substringBefore('/')
        // 截取根目录后面的子文件夹路径
        val sub = rel.substringAfter('/', "")

        // 判断根目录类型，拿到系统公共存储根路径
        val rootDir = when (rootSeg.uppercase()) {
            "DCIM" -> Environment.DIRECTORY_DCIM
            "MOVIES" -> Environment.DIRECTORY_MOVIES
            else -> Environment.DIRECTORY_PICTURES
        }
        // 获取系统根文件夹真实路径
        val base = Environment.getExternalStoragePublicDirectory(rootDir)
        // 拼接完整目录：根目录 + 自定义子文件夹
        val target = if (sub.isBlank()) {
            base
        } else {
            File(base, sub)
        }
        if (!target.exists() && !target.mkdirs()) {
            throw IOException("Failed to create output directory: ${target.absolutePath}")
        }
        // 最终拼接文件名，返回完整文件对象
        return File(target, name)
    }

    /**
     * 给已保存的图片写入EXIF拍摄信息（时间、相机、GPS等元数据）
     * @param uri 图片文件资源地址
     * @param metadata 视频/帧携带的完整拍摄元数据
     * @return 写入成功返回true；出现异常（无权限/文件损坏）返回false，不中断导出流程
     */
    private fun writeMetadataToUri(uri: Uri, metadata: VideoMetadata): Boolean {
        return try {
            // 以读写模式打开图片文件描述符，通过文件句柄操作EXIF，避免文件流冲突
            context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                // 基于文件句柄创建EXIF操作对象
                val exif = ExifInterface(pfd.fileDescriptor)
                // 调用封装工具写入所有拍摄信息
                MetadataWriter.writeExifData(exif, metadata)
                // 持久保存修改后的EXIF数据到图片文件
                exif.saveAttributes()
            }
            true
        } catch (e: Exception) {
            // 写入元数据失败仅打印日志，不抛出异常，保证图片本体导出成功
            LogUtil.e("FrameExporter", "Write exif fail", e)
            false
        }
    }

    /**
     * 根据文件的content Uri获取文件占用大小（单位：字节）
     * 读取异常、文件不存在、无读取权限时直接返回0
     * @param uri 文件资源Uri
     * @return 文件字节大小，失败返回0
     */
    private fun getFileSize(uri: Uri): Long {
        return try {
            // 只读方式打开文件句柄，直接读取系统记录的文件体积
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (_: Exception) {
            // 任意异常兜底返回0，避免流程崩溃
            0L
        }
    }

    /**
     * 导出失败时清理已创建的残留空文件
     * 版本兼容说明：
     * Android 11（API30/Android R）及以上：直接用 contentResolver.delete 删除 Uri 最简洁
     * Android 10及更低版本：优先通过 DocumentFile 删除，兜底再用 contentResolver.delete
     * 捕获所有异常只打日志，不影响上层导出流程
     * @param uri 需要删除的图片文件资源地址
     */
    private fun cleanupFailedOutput(uri: Uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.contentResolver.delete(uri, null, null)
            } else {
                // 低版本先尝试 DocumentFile 方式删除，失败再走系统删除兜底
                val docFile = DocumentFile.fromSingleUri(context, uri)
                docFile?.delete() ?: context.contentResolver.delete(uri, null, null)
            }
        } catch (e: Exception) {
            // 删除失败仅记录日志，不抛出异常中断业务
            LogUtil.e("FrameExporter", "Clean file fail", e)
        }
    }

    /**
     * 把Bitmap图片压缩成字节数组
     * 性能优化点：根据图片尺寸预估占用大小，提前给输出流分配足够缓冲区，减少内存频繁扩容复制开销
     * 不预分配（默认写法）：
     * 盒子一开始只给很小空间，装一点图片数据满了 → 系统自动新建一个更大盒子 → 把旧盒子全部内容复制过去 → 销毁旧盒子。
     * 大图要反复复制十几次，损耗性能、产生大量内存碎片。
     * 咱们现在的写法：
     * 提前算好大概需要多大的盒子，一次性建好。压缩过程只往里填数据，零扩容、零拷贝，效率更高。
     *
     *
     * PNG按 2 字节/像素预估体积，其他格式按 1 字节/像素预估，同时限制最小8KB、最大Int上限防止内存溢出
     * @param bitmap 需要压缩的原图
     * @param format 压缩格式：JPEG / PNG / WEBP
     * @param quality 压缩画质，取值范围 0~100，数值越大画质越高、文件越大
     * @return 压缩后的图片二进制字节数组
     * @throws IOException 图片压缩失败时抛出IO异常
     */
    private fun compressBitmapToBytes(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): ByteArray {
        // 用Long类型计算宽高乘积，防止超大分辨率图片int数值越界溢出.同时用Int.MAX_VALUE兜底,防止最大值报oom
        val estimatedSize = when (format) {
            // PNG无损格式，每个像素预估占用2字节
            Bitmap.CompressFormat.PNG -> (bitmap.width.toLong() * bitmap.height * 2).coerceIn(8192L, Int.MAX_VALUE.toLong()).toInt()
            // JPEG/WEBP有损格式，每个像素预估占用1字节
            else -> (bitmap.width.toLong() * bitmap.height).coerceIn(8192L, Int.MAX_VALUE.toLong()).toInt()
        }

        // 初始化字节输出流，保底最少分配8192字节（8KB） - ByteArrayOutputStream：内存里的字节盒子，用来临时装压缩后的图片数据
        val byteArrayOutputStream = ByteArrayOutputStream(estimatedSize.coerceAtLeast(8192))

        // 执行压缩，压缩失败直接抛出异常
        if (!bitmap.compress(format, quality, byteArrayOutputStream)) {
            throw IOException("Failed to compress bitmap to byte array")
        }

        // 转为字节数组返回
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * 给JPG图片字节数据写入拍摄EXIF信息
     * 实现方式：先在APP缓存文件夹生成一张临时jpg文件，写完EXIF再读回字节，最后删掉临时文件
     * @param jpegBytes 原始JPG图片二进制数据
     * @param metadata 视频里的拍摄信息（时间、定位、设备、旋转角度等）
     * @return Pair结果
     *      第一个值：处理后的图片字节数组（成功=带EXIF；失败=原样原图）
     *      第二个值：布尔标记，true代表写入元数据成功，false代表失败
     */
    private fun writeExifToJpegBytes(jpegBytes: ByteArray, metadata: VideoMetadata): Pair<ByteArray, Boolean> {
        // 在APP专属缓存目录创建临时文件，文件名用时间戳防止重名冲突
        val tmp = File.createTempFile("exif_", ".jpg", context.cacheDir)
        return try {
            // 1. 把原始图片字节写入临时文件
            tmp.writeBytes(jpegBytes)
            // 2. 打开临时文件，操作EXIF标签
            val exif = ExifInterface(tmp.absolutePath)
            // 3. 把视频的拍摄信息批量写入EXIF
            MetadataWriter.writeExifData(exif, metadata)
            // 4. 保存修改到文件
            exif.saveAttributes()
            // 读取修改完的图片字节，返回 新字节 + 成功标记true
            Pair(tmp.readBytes(), true)
        } catch (e: Exception) {
            LogUtil.e("FrameExporter", "Exif write error", e)
            // 写入失败，直接返回原始图片数据，标记false
            Pair(jpegBytes, false)
        } finally {
            // 不管成功还是失败，最后都删除这个临时文件，不占用手机存储空间
            tmp.delete()
        }
    }

    /**
     * 核心视频片段抽取入口：以关键帧截取前后MP4，自动对齐关键帧，不兼容音频转AAC
     * @param videoUri 原始视频Uri
     * @param centerTimestampUs 选中帧视频内绝对微秒时间戳
     * @param beforeDurationUs 向前截取时长
     * @param afterDurationUs 向后截取时长
     * @param muteAudio true静音不处理音频
     * @param videoDurationUs 视频总时长（复用上层减少Retriever）
     * @param rotation 视频旋转0/90/180/270
     * @return MP4剪辑结果
     */
    private suspend fun extractVideoClip(
        videoUri: Uri, centerTimestampUs: Long, beforeDurationUs: Long, afterDurationUs: Long, muteAudio: Boolean = false, videoDurationUs: Long = -1L, rotation: Int = 0
    ): VideoClipResult {
        // 最大单段时长限制5秒，转成微秒单位
        val maxDurationUs = (ExportConfig.MAX_MOTION_DURATION_S * 1_000_000).toLong()

        // 防止用户设置时长超标，强制限制前后截取都不超过5秒
        val safeBeforeDurationUs = minOf(beforeDurationUs, maxDurationUs)
        val safeAfterDurationUs = minOf(afterDurationUs, maxDurationUs)

        // 获取完整视频总时长，上层有传就直接用，没有就读取原视频
        val durationUs = if (videoDurationUs > 0L) {
            // 直接使用传递的值
            videoDurationUs
        } else {
            // 获取整个视频的总时长
            getVideoDurationUs(videoUri)
        }

        // 修正中心点时间：不能小于0，也不能超过视频最后一帧，避免越界报错
        val clampedCenterUs = if (durationUs > 0L) {
            centerTimestampUs.coerceIn(0L, (durationUs - 1L).coerceAtLeast(0L))
        } else {
            maxOf(0L, centerTimestampUs)
        }

        // 计算片段开始时间：中心点减去前置时长，最小不能小于0（视频开头）
        val startUs = maxOf(0L, clampedCenterUs - safeBeforeDurationUs)
        // 计算片段结束时间：中心点加后置时长，最大不能超过视频总长度
        val endUs = if (durationUs > 0L) {
            minOf(durationUs, clampedCenterUs + safeAfterDurationUs)
        } else {
            clampedCenterUs + safeAfterDurationUs
        }
        // 兜底：结束时间必须比开始时间至少多1微秒，防止起止时间相同生成空视频
        val safeEndUs = maxOf(startUs + 1L, endUs)

        // 第一次裁剪：优先用【往前找最近关键帧】模式，画面定位最精准
        val previousSyncResult = try {
            extractVideoClipOnce(videoUri = videoUri, startUs = startUs, endUs = safeEndUs, muteAudio = muteAudio, seekMode = MediaExtractor.SEEK_TO_PREVIOUS_SYNC, rotation = rotation)
        } catch (e: CancellationException) {
            // 用户手动取消导出，直接抛出终止
            throw e
        } catch (e: Exception) {
            // 第一次精准模式裁剪失败，打印日志，准备降级重试
            LogUtil.e("FrameExporter", "PREVIOUS_SYNC extraction failed, will retry", e)
            null
        }

        // 第一次裁剪成功并且有有效视频画面，直接返回结果
        if (previousSyncResult != null && previousSyncResult.videoSamplesWritten > 0) {
            return previousSyncResult
        }
        // 第一次失败就删掉残留的空临时文件
        previousSyncResult?.file?.delete()

        // 降级重试：改用【就近找关键帧】兼容模式，兼容性更强，几乎不会裁剪失败
        return extractVideoClipOnce(
            videoUri = videoUri, startUs = startUs, endUs = safeEndUs, muteAudio = muteAudio, seekMode = MediaExtractor.SEEK_TO_CLOSEST_SYNC, rotation = rotation
        )
    }

    /**
     * 获取整个视频的总时长，返回单位：微秒
     * 系统工具拿到的时长默认是毫秒，代码内部转成微秒给上层剪辑方法使用
     * 读取失败、文件损坏、无权限时统一返回 -1
     */
    private fun getVideoDurationUs(videoUri: Uri): Long {
        // 安卓系统自带的视频信息读取工具
        val ret = MediaMetadataRetriever()
        return try {
            // 绑定要读取的视频文件
            ret.setDataSource(context, videoUri)
            // 提取视频总时长，拿到的是 毫秒(ms) 字符串，转成数字
            val ms = ret.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: return -1
            ms * 1000
        } catch (e: Exception) {
            LogUtil.e("FrameExporter", "Failed to retrieve video duration", e)
            // 任意异常直接返回-1，上层会做判断
            -1
        } finally {
            try {
                // 必须释放工具资源，防止内存泄漏、占用视频文件
                ret.release()
            } catch (e: Exception) {
                LogUtil.e("FrameExporter", "Failed to release retriever", e)
            }
        }
    }

    /**
     * 单次执行视频截取函数（真正干活的底层裁剪逻辑）
     * 功能：从原视频指定起止微秒时间段，抠出一小段MP4短视频，用于拼接成Google动态照片
     * 核心难点兼容：
     * 1. 视频只提取一条视频轨、一条音频轨，避免多轨道错乱导致定位不准
     * 2. MP4容器只支持AAC音频，非AAC格式自动转码为AAC，转码失败直接丢弃声音
     * 3. 部分相机导出视频格式带厂商私有参数，封装器无法识别，自动清理格式重试
     * 4. 协程中途取消自动响应，大文件循环里定时检查任务是否被终止
     * @param videoUri 原视频文件Uri地址
     * @param startUs 截取开始时间（微秒）
     * @param endUs 截取结束时间（微秒）
     * @param muteAudio 是否静音 true=直接不处理音频
     * @param seekMode 视频定位查找关键帧模式（上一层传入精准/兼容两种模式）
     * @param rotation 视频旋转角度 0/90/180/270，保证裁剪后画面方向正确
     * @return VideoClipResult 裁剪结果：临时MP4文件、实际起始时间、写入视频帧数、是否带音频等状态
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun extractVideoClipOnce(
        videoUri: Uri, startUs: Long, endUs: Long, muteAudio: Boolean, seekMode: Int, rotation: Int = 0
    ): VideoClipResult {
        // 1. 在APP缓存文件夹创建临时空mp4文件，用来存放剪出来的短视频
        var tempFile = File.createTempFile("motion_clip_", ".mp4", context.cacheDir)
        // 视频实际真正开始的时间（定位关键帧后会修正这个值）
        var actualStartUs = startUs
        // 统计成功写入了多少帧画面、多少帧声音
        var videoSamplesWritten = 0
        var audioSamplesWritten = 0
        // 音频轨道下标，-1代表没找到音频
        var audioTrackIndex = -1
        // 安卓官方视频解析工具：MediaExtractor 读取原视频数据流
        val extractor = MediaExtractor()

        try {
            // 绑定要解析的原视频
            extractor.setDataSource(context, videoUri, null)

            // ====================== 第一步：遍历视频所有轨道，只挑1条视频轨 + 1条音频轨 ======================
            // 只单独提取视频轨，避免多轨道交织导致部分手机时间定位、戳点错乱
            var videoTrackIndex = -1
            var videoFormat: MediaFormat? = null
            var audioFormat: MediaFormat? = null
            // 单帧最大缓冲区大小，默认1MB
            var maxInputSize = 1024 * 1024

            // 循环遍历视频里面所有数据流轨道（视频、音频、字幕等）
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("video/") && videoTrackIndex == -1) {
                    // 找到第一条视频轨道，记录下标和编码格式
                    videoTrackIndex = i
                    videoFormat = format
                } else if (mime.startsWith("audio/") && !muteAudio && audioTrackIndex == -1) {
                    // 找到第一条音频轨道，并且用户没开静音，记录下来
                    audioTrackIndex = i
                    audioFormat = format
                }
                // 更新最大单帧占用大小，给后面读取数据分配缓冲区用
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    maxInputSize = maxOf(maxInputSize, format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE))
                }
            }

            // 异常兜底：连视频画面轨道都没找到，直接抛异常
            if (videoTrackIndex == -1 || videoFormat == null) {
                throw IOException("No video track found (total tracks: ${extractor.trackCount})")
            }

            // ====================== 第二步：音频预处理，非AAC格式强制转码 ======================
            // MP4文件封装器MediaMuxer只兼容AAC、少数AMR音频 索尼相机XAVC、AC3、MP3、OPUS、FLAC这些格式直接加轨道会崩溃，所以提前转成标准AAC
            val audioMime = audioFormat?.getString(MediaFormat.KEY_MIME)
            var encodedAudio: EncodedAudio? = null

            // 存在音频轨道、没静音、并且格式不兼容MP4 → 执行音频转码
            if (audioFormat != null && audioTrackIndex >= 0 && !isMuxerCompatibleAudioMime(audioMime)) {
                // 先定位视频关键帧，拿到真实起始时间，保证音频时间戳和视频对齐
                // 1. 选中我们找到的那条视频轨道（告诉工具：我现在只操作视频画面，别动音频）
                extractor.selectTrack(videoTrackIndex)
                // 2. 执行跳转：跳到计划的起始时间 startUs，用上层传过来的精准/兼容查找模式
                extractor.seekTo(startUs, seekMode)
                // 3. 获取【实际跳到的关键帧时间点】
                val seekedPosition = extractor.sampleTime
                // 4. 修正真正的片段起始时间
                // 如果跳转有效（>=0），就用真实关键帧时间覆盖原来的startUs
                // 后面裁剪视频、对齐音频全部以 actualStartUs 为准
                if (seekedPosition >= 0) {
                    actualStartUs = seekedPosition
                }
                // 5. 取消选中视频轨道，释放锁定，后面要切换去操作音频轨道
                extractor.unselectTrack(videoTrackIndex)

                // 选中音频轨道执行转码
                extractor.selectTrack(audioTrackIndex)
                encodedAudio = try {
                    // 把这段时间内的音频全部转为AAC二进制数据包
                    transcodeAudioToAac(extractor, audioFormat, actualStartUs, endUs)
                } catch (e: CancellationException) {
                    // 协程被手动取消，直接往上抛出终止任务
                    throw e
                } catch (e: Exception) {
                    // 音频转码失败，记录日志，最终视频不带声音
                    LogUtil.e("FrameExporter", "Audio transcode failed ($audioMime), exporting without audio", e)
                    null
                }
                extractor.unselectTrack(audioTrackIndex)
            }

            // ====================== 第三步：创建MP4封装器，兼容奇葩相机导出的私有格式 ======================
            // 部分相机（索尼ZV1 XAVC S）的视频格式带厂商自定义字段，原生Muxer无法添加轨道
            // 策略：先试原始格式，报错就清理格式、重建封装器、换新临时文件
            var muxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerVideoTrack: Int = try {
                // 尝试添加视频轨道
                muxer.addTrack(videoFormat)
            } catch (e: Exception) {
                LogUtil.e("FrameExporter", "addTrack failed with original format (${videoFormat.getString(MediaFormat.KEY_MIME)}), using clean format", e)
                // 释放坏掉的封装器，删除无效临时文件，重新创建干净格式
                try {
                    muxer.release()
                } catch (releaseException: Exception) {
                    LogUtil.e("FrameExporter", "Failed to release failed muxer", releaseException)
                }
                tempFile.delete()
                tempFile = File.createTempFile("motion_clip_", ".mp4", context.cacheDir)
                muxer = MediaMuxer(
                    tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                )
                // 清理掉厂商私有参数后的纯净格式再添加轨道
                muxer.addTrack(createCleanVideoFormat(videoFormat))
            }

            var muxerStarted: Boolean
            try {
                // 设置视频旋转角度，告诉 MediaMuxer MP4 封装器：最终生成的 MP4 文件要自带旋转标记，播放器打开自动把画面转正，不用手动旋转像素数据。
                if (rotation in arrayOf(0, 90, 180, 270)) {
                    muxer.setOrientationHint(rotation)
                }

                // 判断添加哪一条音频轨道：1.转码后的AAC / 2.原生兼容音频 / 3.不添加音频
                val muxerAudioTrack = when {
                    // 如果有转码后的 AAC 则直接添加它
                    encodedAudio != null -> try {
                        muxer.addTrack(encodedAudio.format)
                    } catch (e: Exception) {
                        LogUtil.e("FrameExporter", "addTrack failed for transcoded AAC, skipping audio", e)
                        -1
                    }
                    // 如果原始音频格式,MP4 兼容 则添加它
                    audioFormat != null && isMuxerCompatibleAudioMime(audioMime) -> try {
                        muxer.addTrack(audioFormat)
                    } catch (e: Exception) {
                        LogUtil.e("FrameExporter", "addTrack failed for audio, skipping audio", e)
                        -1
                    }
                    // 如果都没有,则不添加音频
                    else -> -1
                }

                // 正式启动MP4封装写入
                muxer.start()
                muxerStarted = true

                // ====================== 阶段1：循环读取并写入所有视频画面帧 ======================
                extractor.selectTrack(videoTrackIndex)
                extractor.seekTo(startUs, seekMode)

                // 修正真正的起始时间（定位到关键帧的实际时间）
                val seekedPosition = extractor.sampleTime
                if (seekedPosition >= 0) {
                    actualStartUs = seekedPosition
                }

                // 分配直接内存缓冲区存放帧数据
                var buffer = ByteBuffer.allocateDirect(maxInputSize)
                val bufferInfo = MediaCodec.BufferInfo()

                var sampleCount = 0
                while (true) {
                    // 每循环10次检查一次协程是否被取消，防止卡死
                    if (++sampleCount % 10 == 0) {
                        currentCoroutineContext().ensureActive()
                    }

                    // 获取当前数据包所属的轨道索引，返回-1代表文件所有轨道数据已读完，退出循环
                    val trackIndex = extractor.sampleTrackIndex
                    if (trackIndex < 0) break

                    // 获取当前视频帧原始PTS时间戳（单位：微秒）
                    val sampleTime = extractor.sampleTime
                    // 时间戳异常负数 或者 超过本次裁剪片段的结束时间，终止帧读取
                    if (sampleTime !in 0..endUs) {
                        break
                    }

                    // Android 9.0(P) 新增API：可以预先获取当前帧二进制数据占用字节大小
                    // 动态扩容Direct ByteBuffer缓冲区，避免缓冲区容量不足导致readSampleData截断数据、画面花屏损坏
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        // 获取每一帧的“预估的”字节Byte
                        val sampleSizeHint = extractor.sampleSize
                        // 合法性校验：帧大小有效、不越界Int范围、且大于当前buffer剩余容量
                        if (sampleSizeHint > 0 && sampleSizeHint <= Int.MAX_VALUE && sampleSizeHint.toInt() > buffer.capacity()) {
                            // 重新分配更大的堆外直接内存Buffer，提升IO读取效率
                            buffer = ByteBuffer.allocateDirect(sampleSizeHint.toInt())
                        }
                    }

                    // 重置ByteBuffer读写指针，准备写入新一帧视频编码数据
                    buffer.clear()
                    // 从MediaExtractor读取当前H264/H265裸流数据到Buffer，返回实际读取字节数
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    // 返回负数代表文件读取完毕或者读取异常，结束循环
                    if (sampleSize < 0) {
                        break
                    }

                    // ========== 填充Muxer写入所需的BufferInfo信息 ==========
                    // 数据在Buffer内起始偏移固定为0
                    bufferInfo.offset = 0
                    // 当前帧有效数据大小
                    bufferInfo.size = sampleSize
                    // 计算相对裁剪起点的偏移时间戳，最小不低于0，保证MP4时间轴从0开始规整
                    bufferInfo.presentationTimeUs = (sampleTime - actualStartUs).coerceAtLeast(0L)
                    // 将Extractor的原始帧标记（关键帧/非关键帧）转换成MediaMuxer可识别的Codec Flag
                    bufferInfo.flags = convertSampleToCodecFlags(extractor.sampleFlags)

                    // 把这一帧写入临时MP4文件
                    muxer.writeSampleData(muxerVideoTrack, buffer, bufferInfo)
                    // 已成功写入视频帧计数器自增（用于日志统计、导出进度展示）
                    videoSamplesWritten++

                    // 读取下一帧
                    extractor.advance()
                }

                // ====================== 阶段2：写入音频数据流（如果需要） ======================
                if (muxerAudioTrack >= 0 && videoSamplesWritten > 0) {
                    // 分支1：已经提前转码好AAC数据包，直接循环写入
                    if (encodedAudio != null) {
                        // 循环遍历所有转码好的AAC音频数据包
                        for (packet in encodedAudio.packets) {
                            // 每循环10个音频包检查一次协程是否被用户取消导出任务
                            // 不每一次都判断，减少CPU开销，性能优化
                            if (++sampleCount % 10 == 0) {
                                currentCoroutineContext().ensureActive()
                            }

                            // 给MP4封装器填写当前音频包的基础信息
                            // 音频数据在Buffer里从第0位开始读
                            bufferInfo.offset = 0
                            // 当前这一小段AAC音频字节长度
                            bufferInfo.size = packet.data.size
                            // 这个音频包该什么时候播放（微秒时间戳，用来和视频对齐不卡顿）
                            bufferInfo.presentationTimeUs = packet.presentationTimeUs
                            // 音频包标记（一般用来区分起始配置包）
                            bufferInfo.flags = packet.flags

                            // 把byte数组包装成ByteBuffer，调用Muxer写入MP4文件的音频轨道
                            muxer.writeSampleData(muxerAudioTrack, ByteBuffer.wrap(packet.data), bufferInfo)

                            // 统计成功写入了多少段音频，方便打日志看导出进度
                            audioSamplesWritten++
                        }
                    }

                    // 分支2：原生音频格式兼容，直接解析原视频音频写入
                    else if (audioTrackIndex >= 0) {
                        // MediaExtractor同一时间只能读取一条轨道，先取消选中视频轨道
                        extractor.unselectTrack(videoTrackIndex)
                        // 切换选中音频轨道，接下来只读取音频数据
                        extractor.selectTrack(audioTrackIndex)
                        // 跳转到裁剪的真实起始时间点，参数含义：跳到离目标时间最近的同步帧（关键帧），避免音频开头卡顿、杂音
                        extractor.seekTo(actualStartUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

                        // 循环逐条读取音频每一帧压缩数据包
                        while (true) {
                            // 每循环10个音频包，检查一次协程是否被用户取消导出，降低频繁判断带来的性能消耗
                            if (++sampleCount % 10 == 0) {
                                currentCoroutineContext().ensureActive()
                            }

                            // 获取当前数据包所属轨道，返回-1代表音频数据全部读完，退出循环
                            val trackIndex = extractor.sampleTrackIndex
                            if (trackIndex < 0) {
                                break
                            }

                            // 获取当前音频帧原始播放时间戳（微秒）
                            val sampleTime = extractor.sampleTime
                            // 时间戳非法负数 或者 超出裁剪结束时间，停止读取音频
                            if (sampleTime !in 0..endUs) {
                                break
                            }

                            // Android 9.0(P)及以上系统，提前获取当前音频帧的字节大小，动态扩容堆外缓冲区
                            // 防止缓冲区太小读不全音频数据，导致声音破损、杂音
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                // 获取每一帧的"预估的"字节Byte
                                val sampleSizeHint = extractor.sampleSize
                                // 合法性校验：帧大小有效、不越界Int范围、且大于当前buffer剩余容量
                                if (sampleSizeHint > 0 && sampleSizeHint <= Int.MAX_VALUE && sampleSizeHint.toInt() > buffer.capacity()) {
                                    // 分配足够大的堆外直接内存ByteBuffer，零拷贝读取效率更高
                                    buffer = ByteBuffer.allocateDirect(sampleSizeHint.toInt())
                                }
                            }

                            // 重置Buffer读写指针，准备接收下一帧音频数据
                            buffer.clear()
                            // 把当前音频帧二进制数据读取到Buffer中，返回实际读取字节长度
                            val sampleSize = extractor.readSampleData(buffer, 0)
                            // 返回负数代表读取完毕或异常，结束循环
                            if (sampleSize < 0) {
                                break
                            }

                            // 给MP4封装器填充当前音频帧的配置信息
                            // 数据从Buffer起始位置开始读取
                            bufferInfo.offset = 0
                            // 当前音频帧有效字节长度
                            bufferInfo.size = sampleSize
                            // 换算成裁剪片段内的相对时间戳，最小限制为0，保证音频时间轴从0开始，和视频对齐
                            bufferInfo.presentationTimeUs = (sampleTime - actualStartUs).coerceAtLeast(0L)
                            // 把Extractor原始帧标记转换成Muxer识别的编码标识（区分普通帧/起始配置帧）
                            bufferInfo.flags = convertSampleToCodecFlags(extractor.sampleFlags)

                            // 核心：将原生AAC音频帧写入MP4音频轨道
                            muxer.writeSampleData(muxerAudioTrack, buffer, bufferInfo)
                            // 已写入音频包计数+1，用于日志统计、导出进度展示
                            audioSamplesWritten++
                            // 游标下移，读取下一段音频数据包
                            extractor.advance()
                        }
                    }
                }
                // 画面写入成功就停止封装器生成完整MP4
                if (videoSamplesWritten > 0) {
                    muxer.stop()
                } else {
                    try {
                        muxer.stop()
                    } catch (_: Exception) {
                    }
                }
            } finally {
                // 无论成功失败，释放MP4封装器资源，防止文件占用、内存泄漏
                try {
                    muxer.release()
                } catch (e: Exception) {
                    LogUtil.e("FrameExporter", "Failed to release muxer", e)
                }
            }
        } finally {
            // 释放视频解析工具MediaExtractor，安卓强制要求回收
            try {
                extractor.release()
            } catch (e: Exception) {
                LogUtil.e("FrameExporter", "Failed to release extractor", e)
            }
        }
        // 打包所有结果返回给上层调用函数
        return VideoClipResult(
            file = tempFile,                          // 裁剪好的临时MP4文件
            actualStartUs = actualStartUs,            // 实际视频片段起始微秒时间
            videoSamplesWritten = videoSamplesWritten,// 一共写入了多少帧画面
            audioIncluded = audioSamplesWritten > 0,  // 最终MP4是否包含声音
            hasAudioTrack = audioTrackIndex >= 0      // 原视频本身有没有音频轨道
        )
    }

    /**
     * 判断某种音频编码格式，能不能直接塞进MP4文件里
     * 安卓自带的视频打包工具MediaMuxer对MP4容器支持很有限：
     * 仅允许 AAC、AMR-NB、AMR-WB 三种音频直接封装
     * 其他格式（索尼相机的LPCM、杜比AC3、MP3、Opus、FLAC无损音乐等）都不能直接打包，
     * 必须先转码转换成AAC格式，否则添加音频轨道时直接报错崩溃
     * @param mime 音频的MIME类型字符串
     * @return true=可以直接打包进MP4；false=不兼容，需要转码为AAC
     */
    internal fun isMuxerCompatibleAudioMime(mime: String?): Boolean = when (mime) {
        MediaFormat.MIMETYPE_AUDIO_AAC,
        MediaFormat.MIMETYPE_AUDIO_AMR_NB,
        MediaFormat.MIMETYPE_AUDIO_AMR_WB -> true

        else -> false
    }

    /**
     * 把不兼容的原始音频转成AAC格式，供MP4封装器使用
     * 整体两步流水线：
     * 1. 先把原编码音频解码成无损PCM原始音频流
     * 2. 再将PCM重新编码为标准AAC
     * @param extractor 视频解析器（已经选中音频轨道）
     * @param audioFormat 原音频的编码参数信息
     * @param clipStartUs 截取片段起始微秒时间
     * @param endUs 截取片段结束微秒时间
     * @return EncodedAudio 封装好的AAC二进制数据包 + 格式信息；解码失败/无音频数据返回null
     */
    private suspend fun transcodeAudioToAac(extractor: MediaExtractor, audioFormat: MediaFormat, clipStartUs: Long, endUs: Long): EncodedAudio? {
        // 第一步：解码原音频，输出PCM裸音频数据，失败直接返回null终止
        val pcm = decodeToPcm(extractor, audioFormat, clipStartUs, endUs) ?: return null
        if (pcm.bytes.isEmpty()) {
            LogUtil.e("FrameExporter", "No PCM data in clip range, skipping audio")
            return null
        }
        return encodePcmToAac(pcm)
    }

    /**
     * 将指定时间段的音频解码为 PCM 原始裸音频数据
     * PCM 是未经压缩的声音原始采样数据，作为转AAC的中间格式
     * 分两种情况处理：
     * 1. 音频本身已经是PCM无损格式：直接读取二进制数据即可，不用解码器
     * 2. 其余所有压缩格式（MP3/FLAC/AC3/OPUS等）：调用通用压缩音频解码器转PCM
     * @param extractor 视频轨道解析器（已选中音频轨）
     * @param audioFormat 音频轨道的编码配置信息
     * @param clipStartUs 截取片段开始微秒时间
     * @param endUs 截取片段结束微秒时间
     * @return PcmAudio 封装好的PCM字节流、采样率、声道数；解析失败返回null
     */
    private suspend fun decodeToPcm(extractor: MediaExtractor, audioFormat: MediaFormat, clipStartUs: Long, endUs: Long): PcmAudio? {
        // 获取音频MIME编码类型，拿不到直接返回null
        val mime = audioFormat.getString(MediaFormat.KEY_MIME) ?: return null

        // 判断：如果不是原生PCM无损格式，统一走压缩音频解码逻辑
        if (mime != MediaFormat.MIMETYPE_AUDIO_RAW) {
            return decodeCompressedToPcm(extractor, audioFormat, clipStartUs, endUs)
        }

        // 代码走到这里 = 当前音频本身就是PCM原始格式，只需读取数据
        // 安全获取采样率，异常则赋值-1
        val sampleRate = runCatching { audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) }.getOrNull() ?: -1
        // 安全获取声道数，异常则赋值-1
        val channelCount = runCatching { audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) }.getOrNull() ?: -1

        // 校验参数合法性：采样率必须大于0，声道只能是单声道(1)或双声道立体声(2)
        if (sampleRate <= 0 || channelCount !in 1..2) {
            LogUtil.e(
                "FrameExporter",
                "Unsupported PCM layout (rate=$sampleRate, ch=$channelCount), skipping audio"
            )
            return null
        }
        // 只处理 16位深度的PCM（安卓最通用标准）
        if (audioFormat.containsKey(MediaFormat.KEY_PCM_ENCODING) && audioFormat.getInteger(MediaFormat.KEY_PCM_ENCODING) == AudioFormat.ENCODING_PCM_16BIT) {
            // 定位到音频片段起始位置
            extractor.seekTo(clipStartUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            // 直接从解析器读取PCM二进制数据并封装返回
            return readPcmFromExtractor(extractor, clipStartUs, endUs, sampleRate, channelCount)
        }
        // 不是16位PCM格式，也走通用压缩解码流程兜底
        return decodeCompressedToPcm(extractor, audioFormat, clipStartUs, endUs)
    }

    /**
     * 通用压缩音频解码器：把 MP3/FLAC/AC3/Opus 等压缩音频统一解码成 16位PCM原始裸音频
     * 核心流程：
     * 1. 根据音频MIME类型创建系统硬/软解码器 MediaCodec
     * 2. MediaExtractor 循环读出压缩音频数据包喂给解码器
     * 3. 解码器输出PCM原始字节流，写入内存缓冲区
     * 4. 校验采样率、声道，最后封装成PcmAudio对象返回给上层做AAC编码
     * 兜底保护：解码器卡死超时直接抛异常终止，防止死循环
     * @param extractor 已选中音频轨道的视频解析器
     * @param audioFormat 原压缩音频的编码格式参数
     * @param clipStartUs 音频截取起始微秒
     * @param endUs 音频截取结束微秒
     * @return PcmAudio 解码后的PCM字节数组+采样率+声道+基准时间戳；失败返回null
     */
    private suspend fun decodeCompressedToPcm(extractor: MediaExtractor, audioFormat: MediaFormat, clipStartUs: Long, endUs: Long): PcmAudio? {
        // 1. 获取音频编码类型（比如audio/mpeg、audio/flac、audio/ac3）
        val mime = audioFormat.getString(MediaFormat.KEY_MIME) ?: return null

        // 2. 根据编码格式创建安卓硬件/软件解码器MediaCodec
        val decoder = try {
            MediaCodec.createDecoderByType(mime)
        } catch (e: Exception) {
            LogUtil.e("FrameExporter", "No decoder for $mime, skipping audio", e)
            // 创建解码器失败（系统不支持该编码）直接返回null
            return null
        }

        try {
            // 3. 配置解码器：绑定音频格式，输出到内存缓冲区
            decoder.configure(audioFormat, null, null, 0)
            decoder.start()
        } catch (e: Exception) {
            LogUtil.e("FrameExporter", "Failed to start decoder for $mime, skipping audio", e)
            try {
                // 配置/启动异常，释放解码器资源再退出
                decoder.release()
            } catch (releaseException: Exception) {
                LogUtil.e("FrameExporter", "Failed to release audio decoder", releaseException)
            }
            return null
        }

        try {

            // 4. 音频定位到截取起始点，就近找同步帧
            extractor.seekTo(clipStartUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            // 内存输出流：存放最终全部PCM二进制数据
            val out = ByteArrayOutputStream(PCM_READ_BUFFER_BYTES)
            // 单次拷贝数据的缓冲小块
            val chunk = ByteArray(AUDIO_COPY_CHUNK_BYTES)
            // 解码器输出帧信息载体（长度、偏移、时间戳、标志位）
            val info = MediaCodec.BufferInfo()

            // 采样率、声道数，从格式中安全读取，出错给默认-1
            var sr = runCatching { audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) }.getOrNull() ?: -1
            var ch = runCatching { audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) }.getOrNull() ?: -1

            var basePts = 0L          // PCM音频相对裁剪起点的基准时间戳
            var sawPcm = false        // 标记是否已经读到有效PCM输出数据
            var inputDone = false     // 标记是否已经喂完所有压缩音频输入数据给解码器
            var outputDone = false    // 标记解码器是否全部解码完成
            var idle = 0              // 解码器空闲轮次计数器，用来防卡死死循环
            var rounds = 0

            // 循环：只要解码器还没输出完毕就持续跑
            while (!outputDone) {
                // 每10轮循环检查协程是否被取消（用户退出导出）
                if (++rounds % 10 == 0) {
                    currentCoroutineContext().ensureActive()
                }

                var progress = false // 本轮循环是否有数据处理（输入解码/输出PCM）

                // ========== 第一部分：向解码器喂入压缩音频数据包 ==========
                if (!inputDone) {
                    // 取出解码器空闲的输入缓冲区下标，超时等待
                    val inIdx = decoder.dequeueInputBuffer(CODEC_DEQUEUE_TIMEOUT_US)
                    if (inIdx >= 0) {
                        progress = true
                        val buf = decoder.getInputBuffer(inIdx)
                        val ts = extractor.sampleTime // 当前音频帧原始时间戳

                        // 判断：超出截取结束时间 / 无有效轨道 → 给解码器发送「输入结束」标记
                        if (extractor.sampleTrackIndex < 0 || ts < 0 || ts > endUs) {
                            decoder.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else if (buf == null) {
                            throw IOException("Audio decoder returned a null input buffer")
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val sampleSizeHint = extractor.sampleSize
                                if (sampleSizeHint > Int.MAX_VALUE || sampleSizeHint > buf.capacity().toLong()) {
                                    throw IOException("Audio sample too large for decoder input buffer")
                                }
                            }
                            // 清空缓冲区，读取一段压缩音频数据
                            buf.clear()
                            val len = buf.let { extractor.readSampleData(it, 0) }
                            if (len < 0) {
                                // 读到文件末尾，标记输入完结
                                decoder.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                inputDone = true
                            } else {
                                // 把数据包推入解码器进行解码
                                decoder.queueInputBuffer(inIdx, 0, len, ts, 0)
                                // 解析器移动到下一帧音频
                                extractor.advance()
                            }
                        }
                    }
                }

                // ========== 第二部分：从解码器取出解码后的PCM输出数据 ==========
                when (val outIdx = decoder.dequeueOutputBuffer(info, CODEC_DEQUEUE_TIMEOUT_US)) {
                    // 分支1：解码器输出格式发生变化（比如中途声道/位深变更）
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        progress = true
                        val newFmt = decoder.outputFormat
                        // 强制校验：只允许16位PCM，其他位深直接作废
                        val enc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            runCatching { newFmt.getInteger(MediaFormat.KEY_PCM_ENCODING) }.getOrNull()
                        } else {
                            // 低于24版本默认当作 16bit PCM，不用校验
                            AudioFormat.ENCODING_PCM_16BIT
                        }
                        if (enc != null && enc != AudioFormat.ENCODING_PCM_16BIT) {
                            LogUtil.e("FrameExporter", "Decoder produced non-16-bit PCM ($enc), skipping audio")
                            return null
                        }
                        // 更新最新的采样率和声道数
                        sr = runCatching { newFmt.getInteger(MediaFormat.KEY_SAMPLE_RATE) }.getOrNull() ?: sr
                        ch = runCatching { newFmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT) }.getOrNull() ?: ch
                    }

                    @Suppress("DEPRECATION")
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                        progress = true
                    }

                    // 分支2：暂时没有可输出的数据，跳过本轮
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {}

                    // 分支3：拿到有效解码输出缓冲区下标，读取PCM原始数据
                    else -> {
                        progress = true
                        // 过滤掉解码器配置头数据，只读取真正的音频采样，且数据长度大于0
                        if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0 && info.size > 0) {
                            val buf = decoder.getOutputBuffer(outIdx)
                            if (buf != null) {
                                // 第一次读到有效PCM时，记录基准时间戳（用来对齐后面AAC编码）
                                if (!sawPcm) {
                                    sawPcm = true
                                    basePts = info.presentationTimeUs - clipStartUs
                                }
                                buf.position(info.offset)
                                buf.limit(info.offset + info.size)
                                // 循环小块拷贝PCM字节到内存输出流
                                while (buf.hasRemaining()) {
                                    val r = minOf(chunk.size, buf.remaining())
                                    buf.get(chunk, 0, r)
                                    out.write(chunk, 0, r)
                                    // 防止内存占用爆炸，超出最大PCM内存上限直接终止
                                    if (out.size() > MAX_PCM_BUFFER_BYTES) {
                                        LogUtil.e(
                                            "FrameExporter",
                                            "Decoded PCM buffer size exceeded limit (${out.size()} > ${MAX_PCM_BUFFER_BYTES}), skipping audio"
                                        )
                                        return null
                                    }
                                }
                            }
                        }

                        // 如果解码器标记输出流结束，修改循环终止条件
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            outputDone = true
                        }
                        // 释放已读完的输出缓冲区，交还解码器复用
                        decoder.releaseOutputBuffer(outIdx, false)
                    }
                }

                // ========== 解码器卡死保护逻辑 ==========
                // 本轮有处理动作就清空空闲计数，否则计数+1
                idle = if (progress) {
                    0
                } else {
                    idle + 1
                }
                // 连续多轮无任何数据进出，判定解码器卡死，抛出异常中断导出
                if (idle > MAX_CODEC_IDLE_ROUNDS) {
                    throw IOException("Audio decoder stalled")
                }
            }

            // 最终合法性校验：必须读到PCM、采样率有效、声道只能1或2
            if (!sawPcm || sr <= 0 || ch !in 1..2) {
                LogUtil.e("FrameExporter", "Decoded audio unusable (rate=$sr, ch=$ch), skipping audio")
                return null
            }

            // 封装全部PCM字节、采样率、声道、起始基准时间戳，返回给AAC编码器
            return PcmAudio(out.toByteArray(), sr, ch, basePts)
        } finally {
            try {
                decoder.stop()
            } catch (e: Exception) {
                LogUtil.e("FrameExporter", "Failed to stop audio decoder", e)
            }
            try {
                decoder.release()
            } catch (e: Exception) {
                LogUtil.e("FrameExporter", "Failed to release audio decoder", e)
            }
        }
    }

    /**
     * 直接读取原生PCM格式音频（无需解码器）
     * 适用场景：音频轨道本身就是RAW PCM无损数据，不需要MediaCodec解码，直接二进制拷贝即可
     * 功能：遍历读取指定时间区间内所有PCM采样数据，存入内存字节流，同时计算音频基准时间戳用于音画对齐
     * @param extractor 已选中PCM音频轨道的视频解析器
     * @param clipStartUs 音频截取起始微秒时间
     * @param endUs 音频截取结束微秒时间
     * @param sampleRate 音频采样率
     * @param channelCount 声道数量（1单声道 / 2立体声）
     * @return PcmAudio 完整PCM字节数组、采样率、声道、相对起始点的基准PTS时间戳
     */
    private suspend fun readPcmFromExtractor(extractor: MediaExtractor, clipStartUs: Long, endUs: Long, sampleRate: Int, channelCount: Int): PcmAudio {
        // 内存输出流，存放最终完整PCM二进制数据
        val pcmStream = ByteArrayOutputStream(PCM_READ_BUFFER_BYTES)
        // 直接内存缓冲区，用于读取Extractor原始PCM数据
        var buffer = ByteBuffer.allocateDirect(PCM_READ_BUFFER_BYTES)
        // 小块缓冲区，分批拷贝ByteBuffer数据
        val chunk = ByteArray(AUDIO_COPY_CHUNK_BYTES)
        // 音频相对裁剪起点的基准时间戳
        var basePtsUs = 0L
        // 记录读到的第一帧音频原始时间戳
        var firstSampleTimeUs = -1L
        var sampleCount = 0

        // 循环读取所有PCM音频帧
        while (true) {
            if (++sampleCount % 10 == 0) {
                // 协程安全检测：如果导出任务被手动取消，直接抛出异常终止循环
                currentCoroutineContext().ensureActive()
            }

            // 获取当前数据所属轨道下标，无有效轨道则退出循环
            val tid = extractor.sampleTrackIndex
            if (tid < 0) break

            // 获取当前音频帧原始时间戳
            val sampleTime = extractor.sampleTime
            // 时间超出截取范围，停止读取
            if (sampleTime !in 0..endUs) {
                break
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val sampleSizeHint = extractor.sampleSize
                if (sampleSizeHint > 0 && sampleSizeHint <= Int.MAX_VALUE && sampleSizeHint.toInt() > buffer.capacity()) {
                    buffer = ByteBuffer.allocateDirect(sampleSizeHint.toInt())
                }
            }

            // 清空缓冲区，准备读取下一帧数据
            buffer.clear()
            // 从解析器读取PCM二进制数据到Buffer中
            val len = extractor.readSampleData(buffer, 0)
            // 读取长度小于0代表读取完毕，退出
            if (len < 0) break

            if (firstSampleTimeUs < 0L) {
                firstSampleTimeUs = sampleTime
                basePtsUs = (sampleTime - clipStartUs).coerceAtLeast(0L)
            }

            // ByteBuffer切换为读模式
            buffer.flip()
            // 循环分批把Buffer里的PCM字节写入内存输出流
            while (buffer.hasRemaining()) {
                val read = minOf(chunk.size, buffer.remaining())
                buffer.get(chunk, 0, read)
                pcmStream.write(chunk, 0, read)

                // 内存上限保护：PCM数据体积巨大，超过阈值直接返回空音频，防止OOM内存溢出
                if (pcmStream.size() > MAX_PCM_BUFFER_BYTES) {
                    LogUtil.e("FrameExporter", "PCM buffer size exceeded limit (${pcmStream.size()} > ${MAX_PCM_BUFFER_BYTES}), aborting read")
                    return PcmAudio(ByteArray(0), sampleRate, channelCount, basePtsUs)
                }
            }
            // 移动解析器指针到下一帧音频
            extractor.advance()
        }

        // 循环结束，把内存流转为字节数组，封装返回PCM音频对象
        return PcmAudio(pcmStream.toByteArray(), sampleRate, channelCount, basePtsUs)
    }

    /**
     * 将统一格式的PCM原始音频编码为标准AAC格式
     * 整体流程：
     * 1. 构建AAC编码器参数：LC低复杂度规格、按声道数计算码率
     * 2. 创建并启动MediaCodec AAC硬件/软件编码器
     * 3. 分片循环喂入PCM字节流，自动对齐AAC帧字节边界
     * 4. 取出编码器输出的AAC数据包，记录每个包的时间戳与标记
     * 5. 编码器空闲卡死熔断保护、协程取消监听，最后封装返回AAC结果对象
     * @param pcm 解码完成的PCM裸音频数据（字节数组、采样率、声道、基准时间戳）
     * @return EncodedAudio 包含AAC输出格式 + 所有AAC数据包列表；编码异常/无数据返回null
     */
    private suspend fun encodePcmToAac(pcm: PcmAudio): EncodedAudio? {
        // 单个PCM采样帧占用字节：16bit=2字节 × 声道数，AAC编码必须按完整帧送入，不能截断
        val frameByte = 2 * pcm.channelCount

        // 1. 构造AAC编码配置参数
        val aacFmt = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC, pcm.sampleRate, pcm.channelCount
        ).apply {
            // AAC LC 低复杂度 Profile，兼容性最好，所有设备都支持
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            // 总码率 = 单声道码率 × 声道数量
            setInteger(MediaFormat.KEY_BIT_RATE, AAC_BIT_RATE_PER_CHANNEL * pcm.channelCount)
        }

        // 2. 创建AAC编码器
        val encoder = try {
            MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        } catch (e: Exception) {
            LogUtil.e("FrameExporter", "No AAC encoder available, skipping audio", e)
            // 当前设备不支持AAC编码直接返回null
            return null
        }

        try {
            // 配置编码器：第二个参数Surface传null（音频无需画面输出），标记为编码模式
            encoder.configure(aacFmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()
        } catch (e: Exception) {
            LogUtil.e("FrameExporter", "Failed to start AAC encoder, skipping audio", e)
            // 配置/启动失败，必须释放编码器避免系统资源泄漏
            try {
                encoder.release()
            } catch (releaseException: Exception) {
                LogUtil.e("FrameExporter", "Failed to release AAC encoder", releaseException)
            }
            return null
        }

        try {
            // 存放最终所有AAC编码数据包
            val packets = mutableListOf<EncodedAudioPacket>()
            // 编码器最终输出格式（用于后续封装进MP4）
            var outFmt: MediaFormat? = null
            // 输出帧信息载体：长度、偏移、PTS时间戳、标志位
            val info = MediaCodec.BufferInfo()

            var offset = 0                  // PCM原始字节数组读取偏移下标
            var frameCnt = 0L               // 已送入编码器的PCM总帧数，用于计算PTS时间
            var inputDone = false           // PCM数据是否全部喂入编码器
            var outputDone = false          // AAC编码是否全部输出完成
            var idleRounds = 0                    // 编码器空闲轮次计数器，防止死循环卡死
            var rounds = 0

            // 主循环：持续输入PCM、取出AAC输出，直到编码全部结束
            while (!outputDone) {
                // 协程中断检测：外部取消导出任务时直接抛出异常终止
                if (++rounds % 10 == 0) {
                    currentCoroutineContext().ensureActive()
                }
                // 本轮循环是否有有效读写操作
                var progressed = false

                // ========== 一、向AAC编码器写入PCM原始数据 ==========
                if (!inputDone) {
                    // 获取编码器空闲输入缓冲区
                    val inputIndex = encoder.dequeueInputBuffer(CODEC_DEQUEUE_TIMEOUT_US)
                    if (inputIndex >= 0) {
                        progressed = true
                        val inputBuffer = encoder.getInputBuffer(inputIndex)
                        // 根据已送入帧数计算当前这一批数据对应的PTS时间戳（保证音画同步）
                        val nextPts = pcm.basePtsUs + pcmFramesToUs(frameCnt, pcm.sampleRate)

                        // 缓冲区为空 或 PCM数据已经读完，送入结束流标记
                        if (inputBuffer == null || offset >= pcm.bytes.size) {
                            encoder.queueInputBuffer(
                                inputIndex, 0, 0, nextPts, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            inputDone = true
                        } else {
                            // 本轮最多读取的字节数
                            val avail = inputBuffer.capacity()
                            var chunk = minOf(avail, pcm.bytes.size - offset)
                            // 强制对齐AAC帧边界：剔除不足一帧的尾数，避免编码损坏
                            chunk -= chunk % frameByte
                            if (chunk <= 0) {
                                throw IOException("AAC buffer too small")
                            }
                            // 从PCM字节数组拷贝数据到编码器输入Buffer
                            inputBuffer.clear()
                            inputBuffer.put(pcm.bytes, offset, chunk)
                            // 将数据推入编码器开始编码
                            encoder.queueInputBuffer(inputIndex, 0, chunk, nextPts, 0)

                            // 累加已处理帧数、移动读取偏移
                            frameCnt += chunk / frameByte
                            offset += chunk
                        }
                    }
                }

                // ========== 二、从编码器取出AAC压缩数据包 ==========
                when (val outIdx = encoder.dequeueOutputBuffer(info, CODEC_DEQUEUE_TIMEOUT_US)) {
                    // 编码器输出格式发生变更，更新最终输出格式
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        progressed = true
                        outFmt = encoder.outputFormat
                    }

                    @Suppress("DEPRECATION")
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                        progressed = true
                    }

                    // 暂无可用输出数据，跳过本轮
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {}

                    // 拿到有效AAC输出Buffer
                    else -> {
                        progressed = true
                        // 过滤掉解码器配置头数据，只保存真正的AAC音帧
                        if ((info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && info.size > 0) {
                            val outputBuffer = encoder.getOutputBuffer(outIdx)
                            if (outputBuffer != null) {
                                // 将Buffer里的AAC二进制拷贝为字节数组存入列表
                                val arr = ByteArray(info.size)
                                outputBuffer.position(info.offset)
                                outputBuffer.limit(info.offset + info.size)

                                outputBuffer.get(arr)
                                packets.add(EncodedAudioPacket(arr, info.presentationTimeUs, info.flags))
                            }
                        }

                        // 收到结束标记，修改循环终止条件
                        if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            outputDone = true
                        }
                        // 释放输出缓冲区，交还编码器复用
                        encoder.releaseOutputBuffer(outIdx, false)
                    }
                }

                // ========== 三、编码器卡死保护机制 ==========
                idleRounds = if (progressed) 0 else idleRounds + 1
                // 连续多轮无任何数据进出，判定编码器阻塞，抛出异常中断
                if (idleRounds > MAX_CODEC_IDLE_ROUNDS) {
                    throw IOException("AAC encoder stalled")
                }
            }

            // 合法性校验：必须拿到输出格式 + 至少有一个AAC数据包
            val format = outFmt
            if (format == null || packets.isEmpty()) {
                LogUtil.e("FrameExporter", "AAC encoder produced no packets, skipping audio")
                return null
            }

            // 封装AAC格式信息和所有数据包，返回给上层MP4 muxer混流器
            return EncodedAudio(format, packets)
        } finally {
            try {
                encoder.stop()
            } catch (e: Exception) {
                LogUtil.e("FrameExporter", "Failed to stop AAC encoder", e)
            }
            try {
                encoder.release()
            } catch (e: Exception) {
                LogUtil.e("FrameExporter", "Failed to release AAC encoder", e)
            }
        }
    }

    /**
     * PCM采样帧数转微秒时间戳
     * */
    internal fun pcmFramesToUs(frames: Long, sampleRate: Int): Long {
        return if (sampleRate > 0) {
            frames * 1_000_000L / sampleRate
        } else {
            0L
        }
    }

    /**
     * 清理厂商私有扩展参数，生成可供MediaMuxer正常使用的纯净视频MediaFormat
     * 问题背景：
     * 部分手机厂商（高通、联发科、华为等）在解码器输出的原始MediaFormat中塞入大量私有扩展字段，
     * MediaMuxer混流器无法识别这些非标准参数，调用 addTrack 时直接抛出异常导致导出崩溃。
     * 处理逻辑：
     * 1. 只保留MP4封装强制需要的核心字段：MIME类型、分辨率、CSD编码头数据、帧率、最大帧尺寸、总时长
     * 2. 丢弃所有厂商私有Key与多余冗余参数
     * 3. CSD（Codec Specific Data）SPS/PPS编码配置信息必须重置Buffer游标再拷贝，否则Muxer读取为空
     * @param original 解码器输出的原始视频轨道MediaFormat
     * @return 过滤清理后、MediaMuxer可安全addTrack的标准MediaFormat
     */
    private fun createCleanVideoFormat(original: MediaFormat): MediaFormat {
        // 1. 取出视频编码MIME（如video/avc H.264、video/hevc H.265），不存在直接抛异常
        val mime = original.getString(MediaFormat.KEY_MIME) ?: throw IOException("Video track has no MIME type")

        // 安全读取视频宽度，读取异常兜底默认1920
        val width = try {
            original.getInteger(MediaFormat.KEY_WIDTH)
        } catch (e: Exception) {
            LogUtil.e("FrameExporter", "Failed to get KEY_WIDTH, falling back to 1920", e)
            1920
        }

        // 安全读取视频高度，读取异常兜底默认1080
        val height = try {
            original.getInteger(MediaFormat.KEY_HEIGHT)
        } catch (e: Exception) {
            LogUtil.e("FrameExporter", "Failed to get KEY_HEIGHT, falling back to 1080", e)
            1080
        }

        // 用核心基础参数创建全新干净的MediaFormat对象（无厂商私有垃圾参数）
        val clean = MediaFormat.createVideoFormat(mime, width, height)

        // 2. 拷贝CSD编码配置数据（csd-0/csd-1/csd-2）
        // CSD = SPS/PPS/VPS 解码器初始化解码头，MP4必须写入否则播放器无法解码画面
        // ByteBuffer读取后游标会偏移，必须执行rewind()重置到起始位置，否则setByteBuffer传入空数据
        for (csdKey in arrayOf("csd-0", "csd-1", "csd-2")) {
            try {
                if (original.containsKey(csdKey)) {
                    val csd = original.getByteBuffer(csdKey)
                    if (csd != null) {
                        csd.rewind()
                        clean.setByteBuffer(csdKey, csd)
                    }
                }
            } catch (e: Exception) {
                LogUtil.e("FrameExporter", "Failed to copy CSD key: $csdKey", e)
            }
        }

        // 3. 拷贝可选整型参数：帧率、最大输入帧大小
        val intKeys = arrayOf(MediaFormat.KEY_FRAME_RATE, MediaFormat.KEY_MAX_INPUT_SIZE)
        for (key in intKeys) {
            try {
                if (original.containsKey(key)) {
                    clean.setInteger(key, original.getInteger(key))
                }
            } catch (e: Exception) {
                LogUtil.e("FrameExporter", "Failed to copy optional int key: $key", e)
            }
        }

        // 4. 拷贝视频总时长long类型字段
        try {
            if (original.containsKey(MediaFormat.KEY_DURATION)) {
                clean.setLong(MediaFormat.KEY_DURATION, original.getLong(MediaFormat.KEY_DURATION))
            }
        } catch (e: Exception) {
            LogUtil.e("FrameExporter", "Failed to copy KEY_DURATION", e)
        }

        // 返回过滤完成的标准格式给MediaMuxer.addTrack()使用
        return clean
    }

    /**
     *  MediaExtractor采样标记转MediaCodec Buffer标记
     *  */
    private fun convertSampleToCodecFlags(flags: Int): Int {
        return if ((flags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
            MediaCodec.BUFFER_FLAG_KEY_FRAME
        } else {
            0
        }
    }

    /**
     * 给JPEG图片注入Google动态照片(Motion Photo)XMP元数据
     * 动态照片识别原理：相册读取JPEG的APP1‑XMP片段，识别GCamera标记，才知道图片尾部还挂了一段MP4视频
     * 注意：只写入XMP标签，**不会把MP4视频拼接到文件末尾**，这一步只负责打标记；拼接视频是后面另外的逻辑
     * @param jpegBytes 原始JPEG图片二进制字节数组
     * @param videoLength 追加在JPEG尾部的MP4视频总字节大小（XMP里要记录这个长度给相册读取）
     * @param presentationTimestampUs 动态照片的关键帧时间戳：视频里哪一帧作为静态封面（微秒）
     * @return 已经插入XMP元数据的全新JPEG字节数组
     */
    internal fun injectMotionPhotoXmp(jpegBytes: ByteArray, videoLength: Long, presentationTimestampUs: Long): ByteArray {
        // StringWriter：把生成的XMP‑XML文本临时存到内存字符串
        val sw = StringWriter()
        // Android系统XML序列化工具，用来构建XMP的XML内容
        val xml = Xml.newSerializer().apply { setOutput(sw) }

        // 一堆XMP需要用到的命名空间，Google MotionPhoto标准规定死，不能随便改
        val xNs = "adobe:ns:meta/"
        val rNs = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        val gCamNs = "http://ns.google.com/photos/1.0/camera/"
        val conNs = "http://ns.google.com/photos/1.0/container/"
        val itemNs = "http://ns.google.com/photos/1.0/container/item/"

        // XMP文件固定头部标记，固定字符串，Google标准写死
        xml.processingInstruction("xpacket begin='\uFEFF' id='W5M0MpCehiHzreSzNTczkc9d'")

        // 开始组装xml标签 <xmpmeta>
        xml.setPrefix("x", xNs)
        xml.startTag(xNs, "xmpmeta")

        // RDF 资源描述框架，XMP全部是套在RDF里面
        xml.setPrefix("rdf", rNs)
        xml.startTag(rNs, "RDF")

        // 设置各个命名空间别名
        xml.setPrefix("GCamera", gCamNs)
        xml.setPrefix("Container", conNs)
        xml.setPrefix("Item", itemNs)

        // <rdf:Description> 主体描述节点
        xml.startTag(rNs, "Description")
        xml.attribute(rNs, "about", "")

        // ========== 核心MotionPhoto标识，相册靠这几个字段识别是动态照片 ==========
        // 1=开启动态照片标记
        xml.attribute(gCamNs, "MotionPhoto", "1")
        // 版本号固定1
        xml.attribute(gCamNs, "MotionPhotoVersion", "1")
        // 告诉相册：MP4视频中，哪一个时间点是静态封面图片（微秒）
        xml.attribute(gCamNs, "MotionPhotoPresentationTimestampUs", presentationTimestampUs.toString())

        // Container目录：声明这个JPEG文件包含多个附件条目
        xml.startTag(conNs, "Directory")
        // Seq序列：列表，里面li存放2个条目
        xml.startTag(rNs, "Seq")

        // 第1个li条目：主条目，代表JPEG原图（静态封面）
        xml.startTag(rNs, "li")
        xml.attribute(rNs, "parseType", "Resource")
        xml.startTag(conNs, "Item")
        xml.attribute(itemNs, "Mime", "image/jpeg")
        xml.attribute(itemNs, "Semantic", "Primary")
        xml.attribute(itemNs, "Padding", "0")
        xml.endTag(conNs, "Item")
        xml.endTag(rNs, "li")

        // 第2个li条目：动态视频条目，就是追加在JPEG尾巴的MP4
        xml.startTag(rNs, "li")
        xml.attribute(rNs, "parseType", "Resource")
        xml.startTag(conNs, "Item")
        xml.attribute(itemNs, "Mime", "video/mp4")
        xml.attribute(itemNs, "Semantic", "MotionPhoto")
        xml.attribute(itemNs, "Length", videoLength.toString())
        xml.attribute(itemNs, "Padding", "0")
        xml.endTag(conNs, "Item")
        xml.endTag(rNs, "li")

        // 一层层闭合全部xml标签
        xml.endTag(rNs, "Seq")
        xml.endTag(conNs, "Directory")
        xml.endTag(rNs, "Description")
        xml.endTag(rNs, "RDF")
        xml.endTag(xNs, "xmpmeta")

        // XMP结束标记，固定写法
        xml.processingInstruction("xpacket end='w'")
        xml.flush()

        // 把组装完成的XML拿出来，转成UTF‑8字节数组
        val xmpStr = sw.toString()
        val xmpBin = xmpStr.toByteArray(Charsets.UTF_8)

        // XMP段头部固定标识（APP1段XMP的魔头）
        val headerBin = XMP_NAMESPACE_URI.toByteArray(Charsets.UTF_8)
        val segAll = headerBin + xmpBin

        // JPEG APP1段格式：2字节存段长度，最大不能超过 0xFFFF(65535字节)，XMP太大就报错
        val segLen = segAll.size + 2
        if (segLen > 0xFFFF) throw IllegalArgumentException("XMP segment too large")

        // ByteArrayOutputStream 用来拼接新的完整JPEG
        val outBaos = ByteArrayOutputStream(jpegBytes.size + segLen + 4)

        // 校验输入是不是合法JPEG：JPEG文件开头魔数 0xFF 0xD8 (SOI，图片开始标记)
        if (jpegBytes.size < 2 || jpegBytes[0] != 0xFF.toByte() || jpegBytes[1] != 0xD8.toByte()) {
            throw IllegalArgumentException("Invalid JPEG SOI")
        }

        // ========== 手动组装带APP1‑XMP段的JPEG文件 ==========
        // 1. 写入JPEG起始标记 0xFF 0xD8
        outBaos.write(0xFF)
        outBaos.write(0xD8)

        // 2. 写入APP1段标记：0xFF 0xE1，XMP数据就存放在APP1段
        outBaos.write(0xFF)
        outBaos.write(0xE1)

        // 3. 写入APP1段的总长度（大端高低字节拆分）
        outBaos.write((segLen shr 8) and 0xFF)
        outBaos.write(segLen and 0xFF)

        // 4. 写入XMP头部 + XMP‑XML全部二进制内容
        outBaos.write(segAll)

        // 5. 把原来JPEG剩下的全部字节追加进来，跳过原来最开头的0xFF 0xD8（已经手写过了）
        outBaos.write(jpegBytes, 2, jpegBytes.size - 2)

        // 返回：已经插入XMP元数据的JPEG字节
        return outBaos.toByteArray()
    }

    /**
     * 通过SAF框架，把生成好的动态照片保存到用户授权的自定义文件夹
     * 动态照片文件结构：前面是带XMP标记的JPEG图片字节，文件尾巴直接拼接MP4短视频二进制数据
     * @param jpegBytes 已经注入Motion‑Photo XMP元数据的JPEG图片字节数组
     * @param videoFile 临时生成的MP4短视频本地缓存File文件
     * @param fileName 最终保存的文件名
     * @param treeUri 用户SAF授权的目标文件夹Uri
     * @return 返回保存完成后的DocumentFile Uri
     */
    private fun saveMotionPhotoToCustomDirectory(jpegBytes: ByteArray, videoFile: File, fileName: String, treeUri: Uri): Uri {
        // 根据SAF授权的文件夹uri拿到目录对象，拿不到说明权限被用户撤销了，直接抛异常
        val tree = DocumentFile.fromTreeUri(context, treeUri) ?: throw IOException("Failed to access custom directory — permission may have been revoked")
        // 在这个SAF文件夹里面创建一个JPEG类型的空文件
        val docFile = tree.createFile(ExportFormat.JPEG.mimeType, fileName) ?: throw IOException("Failed to create file in custom directory")
        // 获取新建文件的Uri，后续通过这个Uri读写文件
        val uri = docFile.uri
        try {
            // 打开这个SAF文件的输出流，用来写二进制内容
            val outputStream = context.contentResolver.openOutputStream(uri) ?: throw IOException("Failed to open output stream for custom directory")
            outputStream.use { os ->
                // 第一步：先写入带XMP元数据的JPEG图片二进制
                os.write(jpegBytes)
                // 如果临时MP4片段文件存在并且大小大于0，就把MP4字节直接追加写到JPEG的文件末尾
                // Google Motion Photo标准：JPEG后面直接拼接完整MP4，不需要任何分隔符
                if (videoFile.exists() && videoFile.length() > 0) {
                    // 读取本地临时MP4文件，拷贝输出到SAF输出流；缓冲区65536字节=64KB，分块拷贝，防止一次性加载大文件占满内存
                    FileInputStream(videoFile).use { inputStream ->
                        inputStream.copyTo(os, 65536)
                    }
                }
            }
            // 写入全部数据成功，返回最终文件Uri
            return uri
        } catch (e: Exception) {
            // 导出发生任何异常，要把创建出来残缺损坏的文件删掉，避免残留垃圾文件
            try {
                docFile.delete()
            } catch (deleteException: Exception) {
                // 删除本身也可能失败（权限问题），只打日志，不要二次抛出异常，不覆盖原始导出错误
                LogUtil.e("FrameExporter", "Failed to delete file after export failure", deleteException)
            }
            // 把原始异常继续往上抛，上层捕获做导出失败处理
            throw e
        }
    }

    /**
     * 将动态照片保存到系统图库MediaStore，采用流拷贝方式，避免一次性加载全部大文件导致内存溢出OOM
     * 动态照片结构：JPEG图片字节(已经打好XMP动态照片标记) + 文件尾部直接拼接MP4短视频
     * @param jpegBytes 注入完Motion‑Photo XMP元数据的JPEG图片字节数组
     * @param videoFile 本地缓存的临时MP4短视频文件
     * @param fileName 导出文件显示名称
     * @param config 导出配置，包含保存目录等参数
     * @param dateTakenMs 图片拍摄时间，毫秒，可以为空
     * @return 保存成功后返回图库的Uri
     */
    internal fun saveMotionPhotoToMediaStore(jpegBytes: ByteArray, videoFile: File, fileName: String, config: ExportConfig, dateTakenMs: Long? = null): Uri {
        // ContentValues：准备要插入到图库数据库的字段信息
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, ExportFormat.JPEG.mimeType)
            dateTakenMs?.let { put(MediaStore.Images.Media.DATE_TAKEN, it) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android10及以上分区存储：设置保存到图库的相对目录，比如 DCIM/FrameEcho
                put(MediaStore.Images.Media.RELATIVE_PATH, config.exportDirectory.relativePath)
                // IS_PENDING=1：标记文件正在写入，图库暂时不扫描显示这个文件，写完再改成0对外可见
                put(MediaStore.Images.Media.IS_PENDING, 1)
            } else {
                // Android9及以下旧版本：直接填写完整文件绝对路径
                put(MediaStore.Images.Media.DATA, resolveLegacyOutputFile(fileName, config.exportDirectory).absolutePath)
            }
        }

        // 向图库数据库插入一条记录，相当于占一个文件位置
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        ) ?: throw IOException("Failed to create MediaStore entry — storage may be full or unavailable")

        try {
            // 获取输出流，往这个图库Uri里面写二进制数据
            val outputStream = context.contentResolver.openOutputStream(uri) ?: throw IOException("Failed to open output stream for MediaStore entry")

            outputStream.use { os ->
                // 先写入带XMP标记的JPEG图片二进制
                os.write(jpegBytes)

                // 如果临时MP4视频文件有效，直接把MP4拼接写在JPEG的末尾
                // Google动态照片标准：JPEG后面直接跟MP4，不需要分隔符
                if (videoFile.exists() && videoFile.length() > 0) {
                    FileInputStream(videoFile).use { inputStream ->
                        // 64KB缓冲区分块拷贝MP4，不会把整个视频全部读进内存，防止OOM
                        inputStream.copyTo(os, 65536)
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android10+：写入完成，把IS_PENDING改为0，告诉图库这个文件已经写完，可以展示出来了
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }

            // 全部写入成功，返回图库Uri
            return uri
        } catch (e: Exception) {
            // 导出过程出错，把图库里面这条残缺损坏的文件删掉，避免图库出现损坏图片
            try {
                context.contentResolver.delete(uri, null, null)
            } catch (deleteException: Exception) {
                // 删除也可能失败，只打印警告日志，不要把删除异常覆盖原本的导出错误
                LogUtil.e("FrameExporter", "Failed to delete URI after export failure", deleteException)
            }
            // 把原始异常继续往上抛，上层处理导出失败
            throw e
        }
    }

    internal fun toCompressFormat(quality: Int, sdkInt: Int = Build.VERSION.SDK_INT): Bitmap.CompressFormat {
        return Bitmap.CompressFormat.JPEG
    }

}