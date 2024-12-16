# 这是一份逻辑文件，展示当前文件保存的逻辑
如果启动了压缩功能，下面的操作都会包含（压缩到LuBanCache文件夹）该逻辑

录制界面： Pictures/AlbumCameraRecorderTemp
1. 拍照图片时，会保存当前临时文件到Cache文件夹
1.1 编辑后并且保存图片：编辑后的图片临时保存到Cache文件夹 - 压缩到LuBanCache文件夹 - 复制LuBanCache文件到设置目录下 - 加入相册 - 返回设置目录文件
1.2 保存图片：压缩到LuBanCache文件夹 - 复制LuBanCache文件到设置目录下 - 加入相册 - 返回设置目录文件
1.3 不保存图片，关闭窗口： 会保留中途操作过的Cache文件，所以如果有必要可以选择清楚缓存

1.1:    拍照 - AlbumCameraRecorderCache 文件名字：IMAGE_yyyyMMdd_HHmmssSSS.jpg
        加入相册 - 迁移到DCIM/Camera 文件名字：IMAGE_yyyyMMdd_HHmmssSSS.jpg
        压缩 - AlbumCameraRecorderCompress 文件名字和路径：根据用户的压缩库来决定，不一定是这个路径
        编辑 - AlbumCameraRecorderTemp - 加入相册 - 压缩

1.2:    录像 - 迁移到DCIM/Camera 文件名字：VIDEO_yyyyMMdd_HHmmssSSS.jpg
        压缩 - AlbumCameraRecorderCompress 文件名字和路径：根据用户的压缩库来决定，不一定是这个路径

相册界面：
1. 相册选择图片
1.1 相册选择图片并且在预览中编辑图片：
    编辑后的图片临时保存到Cache文件夹 - 如果是编辑并且确认后，最终会压缩到LuBanCache文件夹 - 复制LuBanCache文件到设置目录下 - 加入相册 - 返回设置目录文件
1.2 相册选择图片：
    path - 原图uri
    absolutePath - 原图真实路径
    sandboxPath - 沙盒路径 改
    editorPath - 编辑路径
    compressPath - 压缩路径 - com.zhongjh.cameraapp/files/Pictures/AA/picture
1.3 不选择图片，关闭窗口： 不产生任何Cache文件


报错汇总：
2. 录制时间过短，上一段录制时间看看要不要删掉无用代码
3. 打开视频界面后，顶部显示问题，去掉fit属性就正常
4. 整理状态属性 - 视频、多视频、录制中
5. 长按按钮后，动画会刷一下体验不太好
6. 编辑报错
7. 视频代码看看为什么用不上 stopProgress
