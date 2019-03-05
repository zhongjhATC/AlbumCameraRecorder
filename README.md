# AlbumCameraRecorder

[![MinSdk](https://img.shields.io/badge/MinSdk-16-blue.svg)](https://developer.android.com/about/versions/android-4.1)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/LICENSE)

## 目前已经投入到正式项目中使用，基本锥形功能已经形成，当然还需要更进一步完成相关测试。
## 有开了兼容云测试包月的小伙伴们，可以麻烦私聊下我吗谢谢！

## 中文
一个高效的多媒体支持操作库，可多方面的简单配置操作相册、录制、录音等功能。

本开源库的部分代码来自[Matisse](https://github.com/zhihu/Matisse).

非常感谢知乎提供的这么棒的开源项目！    

## 特性
 - 支持自定义样式.支持更换里面的相关按钮.
 - 支持相册、录制、录音等多个嵌套功能，并且也可以通过配置只设置显示一个.
 - 丰富的回调接口和调试信息,可利用现有API实现丰富的效果.
 
## 引入

#### Step 1. Add the JitPack repository to your build file

	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
#### Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.zhongjhATC.AlbumCameraRecorder:multilibrary:1.0.1'         // 核心lib，调用显示相册、录屏、录音等
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:progresslibrary:1.0.1' // 配套使用，主要用于获取数据后进行相关显示，相应的上传进度显示，如果你只需要获取照片录像录音等数据，自行写获取后呈现方式，可以不需要是用这个
	}

## 快照
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/Demonstration.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/DemonstrationShowImg.png)

## 使用   
#### 在代码中打开设置（这方面最近继续完善）
 
        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        cameraSetting.mimeTypeSet(MimeType.ofImage());// 支持的类型：图片，视频
        cameraSetting.captureStrategy(new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/camera")); // 保存目录

        // 相册
        AlbumSetting albumSetting = new AlbumSetting(true)
                .mimeTypeSet(MimeType.ofImage())// 支持的类型：图片，视频
                .captureStrategy(
                        new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/album"))// 设置路径和7.0保护路径等等
                .showSingleMediaType(true) // 仅仅显示一个多媒体类型
                .countable(true)// 是否显示多选图片的数字
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))// 查看的大小限制
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))// 九宫格大小
                .thumbnailScale(0.85f)
                .setOnSelectedListener((uriList, pathList) -> {
                    // 每次选择的事件
                    Log.e("onSelected", "onSelected: pathList=" + pathList);
                })
                .originalEnable(true)// 开启原图
                .maxOriginalSize(1) // 最大原图size,仅当originalEnable为true的时候才有效
                .setOnCheckedListener(isChecked -> {
                    // DO SOMETHING IMMEDIATELY HERE
                    Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                });

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();
        recorderSetting.captureStrategy(new CaptureStrategy(true,"com.zhongjh.cameraapp.fileprovider", "AA/recorder"));// 保存目录

        // 全局
        MultiMediaSetting.from(MainActivity.this)
                .choose(MimeType.ofAll())
                .albumSetting(albumSetting)
                .cameraSetting(cameraSetting)
                .recorderSetting(recorderSetting)
                .setOnMainListener(errorMessage -> Toast.makeText(MainActivity.this.getApplicationContext(), "自定义失败信息：录音已经达到上限", Toast.LENGTH_LONG).show())
                .captureStrategy(
                        new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/test"))// 设置路径和7.0保护路径等等
                //                                            .imageEngine(new GlideEngine())  // for glide-V3
                .imageEngine(new Glide4Engine())    // for glide-V4
                .maxSelectable(10 - (alreadyImageCount + alreadyVideoCount))// 全部最多选择几个
                .maxSelectablePerMediaType(10 - alreadyImageCount, 1 - alreadyVideoCount, 1 - alreadyAudioCount)// 最大10张图片或者最大1个视频
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .forResult(REQUEST_CODE_CHOOSE);

## 近期计划更新
#### 1.0.1
 - 进一步完善代码加强注释，方便阅读理解
 - 完善Theme