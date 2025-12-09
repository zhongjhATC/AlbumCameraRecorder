# AlbumCameraRecorderX

[![MinSdk](https://img.shields.io/badge/MinSdk-21-blue.svg)](https://developer.android.com/about/versions/android-5.0)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/LICENSE)

## This version has just been released. If you want a more stable version, you can choose the androidx branch with version number 1.-.--X. Please note that the code in the androidx branch will no longer be maintained unless there are serious bugs.
## This version uses CameraX for shooting, making it easier to customize shooting parameters and simplify dynamic watermarks. The global animations are more delicate. At the same time, redundant configurations and attributes have been sorted out.
## Any suggestions or features you want to add can be submitted in Issues.

## [中文](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/README_CN.md)
An efficient multimedia support operation library that allows simple configuration of various functions such as taking photos, accessing albums, recording videos, and recording audio.

It also supports a matching nine-grid function for displaying pictures, videos, and audios.

## androidx version branch
No longer maintained(https://github.com/zhongjhATC/AlbumCameraRecorder/tree/androidx)

## 特性
- Recording and photography fully support customization! For details on how to use it, please refer to the Demo. For in-depth expansion, you need to learn more about the code[CameraFragment架构简介](https://juejin.cn/post/7136108758010167304/)
- Supports three-in-one functions such as album, recording, and audio recording (similar to Douyin, etc.), and can also be configured to independently use one of the functions.
- Supports custom permission requests or allows the library to handle permission requests directly.
- Although there are many functions, you can import certain libraries according to the required functions.
- Rich callback interfaces and debugging information, allowing you to achieve rich effects using existing APIs.
- Strong compatibility, with relevant compatibility handling for both low-version 4.1 and the latest Android 16.
- Supports customization of all image reading and processing, such as customizing Glide, Fresco, etc.
- Supports selecting pictures from the album.
- Supports album selection by distinguishing different folders according to mobile phone files.
- High customizability, supporting various custom settings such as the maximum number of selectable pictures and videos, and also supporting displaying only custom file sizes.
- Supports customizing the style, color, size, etc. of the album.
- Compatible with horizontal and vertical screen adjustments.
- Supports flash and front/back camera switching during recording and photography.
- Supports two-finger touch to zoom in/out and single-finger sliding to adjust brightness during recording and photography.
- All UIs such as long-press buttons for recording and photography can be customized, all using SVG images to handle animation details well.
- Recording supports segmented recording, and video editing will be added in the future.
- Image editing supports color graffiti, text input, mosaic processing, rotation, cropping, etc.
- Supports watermark function during shooting and recording, even dynamic watermarks.
- Built-in permission function, no need to modify any code. The permission function includes permission detection, informing why the permission is requested when sending the permission request, and prompting to jump to the settings interface to set permissions after repeated denials. Custom permissions are also supported.
- All recording and photography can choose deep compression processing. Image compression can be customized or use LuBan compression in the Demo. Video compression uses ffmpeg.
- Complete cache management system.
- Complete animation effects, shared element animations, and button detail animations.
- Performance optimization, with careful handling of memory leaks.

## Import

#### Step 1. Add the JitPack repository to your build file

	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
#### Step 2. Add the dependency

	dependencies {
	     // If you want to simplify the code and use multilibrary, grid, and albumCameraRecorderCommon at the same
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:combined:2.0.07'

	     // Common library, if you don't use the combined library above 
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:common:2.0.07'
	     // Core library for calling and displaying album, screen recording, audio recording, etc.
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:multilibrary:2.0.07'
         // Used as a supplement, mainly for displaying relevant content after obtaining data and showing corresponding upload progress. If you only need to obtain data such as photos, videos, and audios, you don't need to use this.
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:grid:2.0.07'

         // Used for image editing
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:imageedit:2.0.07'
	     // Used for video editing, currently only has compression function. More functions will be added later. Since this library uses ffmpeg, it takes up 25M of file size. Choose whether to use it according to the actual situation.
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:videoedit:2.0.07'
        
	}

#### Step 3. gradle file to add configuration

    android.enableJetifier=true
    android.useAndroidX=true

## Screenshots
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/Demonstration.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/Demonstration1.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/Demonstration2.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/DemonstrationShowImg.png)

## Compatibility test on commonly used mobile phones in the market
100% passed the[compatibility test report.](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/WeTest.md).
![](https://raw.githubusercontent.com/zhongjhATC/AlbumCameraRecorder/kotlin/wetest/5.jpg)

## Usage (it is recommended to download the Demo for more functions)
#### Launch multimedia-related functions

        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        // 支持的类型：图片，视频
        cameraSetting.mimeTypeSet(MimeType.ofAll());

        // 相册
        AlbumSetting albumSetting = new AlbumSetting(false)
                // 支持的类型：图片，视频
                .mimeTypeSet(MimeType.ofAll())
                // 是否显示多选图片的数字
                .countable(true)
                // 自定义过滤器
                .addFilter(new GifSizeFilter(320, 320, 5 * BaseFilter.K * BaseFilter.K))
                // 开启原图
                .originalEnable(true)
                // 最大原图size,仅当originalEnable为true的时候才有效
                .maxOriginalSize(10);

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();

        // 全局
        mGlobalSetting = MultiMediaSetting.from(MainSimpleActivity.this).choose(MimeType.ofAll());
        // 开启相册功能
        mGlobalSetting.albumSetting(albumSetting);
        // 开启拍摄功能
        mGlobalSetting.cameraSetting(cameraSetting);
        // 开启录音功能
        mGlobalSetting.recorderSetting(recorderSetting);

        mGlobalSetting
                // for glide-V4
                .imageEngine(new Glide4Engine())
                // 最大5张图片、最大3个视频、最大1个音频。如果需要使用九宫格，请把九宫格GridView的maxCount也改动 mBinding.dmlImageList.setMaxMediaCount();
                .maxSelectablePerMediaType(null, MAX_IMAGE_SELECTABLE, MAX_VIDEO_SELECTABLE, MAX_AUDIO_SELECTABLE,
                        alreadyImageCount,
                        alreadyVideoCount,
                        alreadyAudioCount)
                .forResult(requestLauncherACR);

#### Get the returned data

    protected final ActivityResultLauncher<Intent> requestLauncherACR = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) {
            return;
        }
        if (null == result.getData()) {
            return;
        }

        List<LocalMedia> data = MultiMediaSetting.obtainLocalMediaResult(result.getData());
        printProperty(data);
    });

#### For other common issues, you can search for error keywords in[Possible Issues](https://github.com/zhongjhATC/AlbumCameraRecorder/issues)

#### If you need to modify the interface and logic of shooting and recording
For simple expansion, please refer to the Demo. For in-depth expansion, learn more about[Brief Introduction to CameraFragment Architecture](https://juejin.cn/post/7136108758010167304/)
However, please note that if configuration is supported, configuration takes priority. Only when there is no configuration, custom expansion can be done here.

#### If you need to use the nine-grid to display data, you can refer to the[relevant code](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/app/src/main/java/com/zhongjh/demo/phone/MainSeeActivity.java).

#### Relevant APIs, more APIs and support are continuously added
- [Public configuration API for calling multimedia.](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/java/com/zhongjh/multimedia/settings/api/GlobalSettingApi.kt).
- [Album configuration API for calling multimedia.](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/java/com/zhongjh/multimedia/settings/api/AlbumSettingApi.kt).
- [Recording configuration API for calling multimedia.](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/java/com/zhongjh/multimedia/settings/api/CameraSettingApi.kt).
- [Audio recording configuration API for calling multimedia.](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/java/com/zhongjh/multimedia/settings/api/RecorderSettingApi.kt).
- [Multimedia-related static methods](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/java/com/zhongjh/multimedia/AlbumCameraRecorderApi.kt)
- [Multimedia UI-related attribute configurations](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/res/values/styles.xml)

If you use the displayed nine-grid library, the following APIs will be useful for you:
- [Nine-grid related APIs.](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/gridview/src/main/java/com/zhongjh/gridview/api/GridViewApi.kt).
- [Nine-grid related events.](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/gridview/src/main/java/com/zhongjh/gridview/listener/GridViewListener.kt).
- [Nine-grid related attributes, UI configurations, etc.](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/gridview/src/main/res/values/attrs.xml)



Historical updates
[Historical updates](https://github.com/zhongjhATC/AlbumCameraRecorder/releases)summarized from version 1.0.1.

## APK direct experience download
- Version 2.0.00, which may differ slightly from the current latest code version(uploaded via https://apponthego.com/上传)
  [Download link](https://i.apponthego.com/9c655)

# Finally

1. If you find it useful, please give a Star（[GitHub](https://github.com/zhongjhATC/AlbumCameraRecorder)）

2. If you find any bugs, please leave a message or an Issue（[Issues](https://github.com/zhongjhATC/AlbumCameraRecorder/issues)）

3. Any reproduction must indicate the source.

# QQ Group 915053430 This group was established on April 28, 2021. It is used to solve problems in a timely manner. It is recommended to submit the problem in issues before asking, so that it can be referenced by later users.
