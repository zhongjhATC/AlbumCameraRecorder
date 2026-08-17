package com.zhongjh.multimedia.camera.ui.camera.motion.model

/**
 * 图片/动态照片导出的全部配置参数实体类
 * 控制导出格式、画质、是否生成动态照片、保存文件夹、视频片段时长等所有规则
 */
data class ExportConfig(
    /**
     * 导出图片格式：JPEG / PNG / WEBP
     * */
    val format: ExportFormat = ExportFormat.JPEG,

    /**
     * 压缩画质 取值1~100，数字越大清晰度越高，文件体积越大；100为无损画质（支持无损的格式生效）
     * */
    val quality: Int = 100,

    /**
     * 是否保留原视频的拍摄信息（时间、定位、设备型号等EXIF元数据）
     * */
    val preserveMetadata: Boolean = true,

    /**
     * 是否导出为Google动态照片Motion Photo（带长按播放短视频效果）
     * */
    val motionPhoto: Boolean = false,

    /**
     * 动态照片截取短视频的时长，单位：秒
     * 当前选中关键帧 往前截取多少秒 + 往后截取多少秒
     * 默认各1.5秒，总共3秒，符合谷歌动态照片官方规范，可由用户自定义修改
     */
    val motionDurationBeforeS: Float = 1.5f,
    val motionDurationAfterS: Float = 1.5f,

    /**
     * 动态照片里的短视频是否静音（true=去掉声音）
     * */
    val muteAudio: Boolean = false,

    /**
     *  限制导出图片最大边长分辨率，null代表使用原图原始分辨率，不做缩放
     *  */
    val maxResolution: Int? = null,

    /**
     * 自定义导出文件名（不带后缀 .jpg/.png），传空就用系统自动生成的命名规则
     * */
    val customFileName: String? = null,

    /**
     * 图片默认保存到系统哪个公共文件夹
     * */
    val exportDirectory: ExportDirectory = ExportDirectory.PICTURES_FRAMEECHO
) {
    /**
     * 动态照片短视频总时长（前时长+后时长），只读计算属性
     * */
    val totalMotionDurationS: Float
        get() = motionDurationBeforeS + motionDurationAfterS

    companion object {
        /**
         * 最大单段时长限制5秒
         */
        const val MAX_MOTION_DURATION_S = 5.0f
    }

    /**
     * 初始化构造时自动校验所有参数合法性，非法参数直接抛异常，防止导出逻辑出错
     */
    init {
        require(quality in 1..100) { "画质数值必须在1到100之间" }
        require(motionDurationBeforeS >= 0f) { "关键帧之前的视频时长不能为负数" }
        require(motionDurationAfterS >= 0f) { "关键帧之后的视频时长不能为负数" }
        require(motionDurationBeforeS <= MAX_MOTION_DURATION_S) { "前置视频时长不能超过${MAX_MOTION_DURATION_S}秒" }
        require(motionDurationAfterS <= MAX_MOTION_DURATION_S) { "后置视频时长不能超过${MAX_MOTION_DURATION_S}秒" }
        require(maxResolution == null || maxResolution > 0) { "分辨率必须填写大于0的正整数" }
        require(customFileName == null || customFileName.length <= 80) { "自定义文件名过长，最多支持80个字符" }
    }
}

/**
 * 枚举：图片要保存到系统哪个文件夹，relativePath是相册识别的相对路径
 */
enum class ExportDirectory(val relativePath: String) {
    /**
     * 图片目录下FrameEcho文件夹
     */
    PICTURES_FRAMEECHO("Pictures/AlbumCameraRecorderMotion"),

    /**
     * 相机相册DCIM下FrameEcho文件夹
     */
    DCIM_FRAMEECHO("DCIM/AlbumCameraRecorderMotion"),

    /**
     * 视频电影目录下FrameEcho文件夹
     */
    MOVIES_FRAMEECHO("Movies/AlbumCameraRecorderMotion"),

    /**
     * 系统默认Pictures图片根目录
     */
    PICTURES("Pictures"),

    /**
     * 系统默认相机相册根目录
     */
    DCIM("DCIM"),

    /**
     * 系统默认电影视频根目录
     */
    MOVIES("Movies")
}
