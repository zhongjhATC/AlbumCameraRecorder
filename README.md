# AlbumCameraRecorderX

[![MinSdk](https://img.shields.io/badge/MinSdk-21-blue.svg)](https://developer.android.com/about/versions/android-5.0)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/LICENSE)

## This release is an offshoot of the AndroidX release.Any version number followed by an X is based on the AndroidX version.
## At present, it has been put into use in the formal project.
## If you have any suggestions or want to add functions, you can put forward on Issues

## [中文](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/README_CN.md)
An efficient multimedia support operation library, can be a variety of simple configuration operation photo, album, recording, recording and other functions.

Also support supporting the use of the display of pictures, video, audio 9 grid function.

 - Part of the code for this open source library comes from [Matisse](https://github.com/zhihu/Matisse).
 - This open source library camera code from [CameraView](https://github.com/natario1/CameraView).

## Non-X version branching
A non-X library version, no longer maintained(https://github.com/zhongjhATC/AlbumCameraRecorder/tree/master)

## peculiarity
 - Record photo fully support your own custom! See how to use Demo for more details, if you need to go deeper, you need to know more code [Introduction to the CameraFragment architecture](https://juejin.cn/post/7136108758010167304/)
 - Support album, recording, recording and other functions in one (similar to Douyin, etc.), and you can configure only one of the functions independently.
 - Customizable permission requests are supported and can be directed to the library to complete permission requests
 - While there are many features, some libraries can be introduced as required
 - Rich callback interface and debugging information, using the existing API to achieve a rich effect.
 - Strong compatibility, whether the lower version of 4.1 or the current latest version of Android 11, has been carried out related compatibility processing(I also tested Android 13 and found no problem)
 - Support all image reading and processing customization, such as custom Glide, Fresco, etc
 - Support to select pictures from albums
 - Supports photo albums to select different folders according to mobile phone files
 - Strong customization, support a variety of maximum selection of how many pictures, videos, and so on, also support only display custom file size
 - Support custom album style, color, size and so on
 - Support flash and front and rear camera switching when taking photos
 - Support double finger touch to zoom in and out when recording and photographing, and single finger sliding up and down to control brightness
 - recording, taking photos, pressing buttons, all of the UI is customizable, all SVG images are good for handling animation details
 - Recording support for segment recording, video editing will be added in the future
 - Image editing supports color graffiti, input text, Mosaic processing, rotation, cropping and other processing
 - Support recording processing
 - Deep compression is available for all recorded photos, custom compression is available for pictures, LuBan compression is available for Demo, and FFMPEG compression is available for videos
 - Perfect cache management system
 - Improved animation effects, which will be added later
 - Performance optimizations, memory leaks -- all carefully addressed

## import

#### Step 1. Add the JitPack repository to your build file

	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
#### Step 2. Add the dependency

	dependencies {
	     // if you want to simplify the code and at the same time use multilibrary and progresslibrary, albumCameraRecorderCommon, can directly use the combined library
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:combined:1.1.80X'

	     // Public library, if not using the combined library above
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:common:1.1.80X'
	     // core lib, call display album, screen recording, recording, etc
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:multilibrary:1.1.80X'
         // It is mainly used to display the relevant upload progress after obtaining data. If you only need to obtain photos, videos and recordings, you don't need to use this
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:progresslibrary:1.1.80X'

         // use it with editing pictures
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:imageedit:1.1.80X'
	     // For editing video
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:videoedit:1.1.80X'
	}

## snapshoot
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/Demonstration.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/Demonstration1.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/Demonstration2.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/DemonstrationShowImg.png)



## Compatibility testing of mobile phones is commonly used in the market
100% through[Compatibility Test Report](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/WeTest.md).
![](https://raw.githubusercontent.com/zhongjhATC/AlbumCameraRecorder/androidx/wetest/5.jpg)

## use(You are advised to download Demo for more functions)
#### Enable multimedia related functions

        // Shooting Related Settings
        CameraSetting cameraSetting = new CameraSetting();
        // Types supported: picture, video
        cameraSetting.mimeTypeSet(MimeType.ofAll());

        // album
        mAlbumSetting = new AlbumSetting(false)
                // Types supported: picture, video
                .mimeTypeSet(MimeType.ofAll())
                // Whether to display the number of multiple selected images
                .countable(true)
                // Custom filter
                .addFilter(new GifSizeFilter(320, 320, 5 * BaseFilter.K * BaseFilter.K))
                // Open the original
                .originalEnable(true)
                // Maximum original size, valid only if originalEnable is true
                .maxOriginalSize(10);

        // recorder
        RecorderSetting recorderSetting = new RecorderSetting();

        // globalSetting
        GlobalSetting globalSetting = MultiMediaSetting.from(MainActivity.this).choose(MimeType.ofAll());
        globalSetting.cameraSetting(cameraSetting);
        globalSetting.albumSetting(albumSetting);
        globalSetting.recorderSetting(recorderSetting);

        mGlobalSetting
                .setOnMainListener(errorMessage -> {
                    Log.d(TAG, errorMessage);
                    Toast.makeText(MainSimpleActivity.this.getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                })
                // Set paths and 7.0 protection paths, and so on
                .allStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "aabb"))
                // for glide-V4
                .imageEngine(new Glide4Engine())
                // Maximum 5 images or maximum 3 video
                .maxSelectablePerMediaType(null,
                                        5,
                                        3,
                                        3,
                                        alreadyImageCount,
                                        alreadyVideoCount,
                                        alreadyAudioCount)
                                .forResult(REQUEST_CODE_CHOOSE);

#### Gets the relevant returned data

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        List<LocalFile> result = MultiMediaSetting.obtainLocalFileResult(data);
    }

#### Do not forget this configuration. Otherwise, an error will be reported when recording and saving files、Uri is cleared when album selection image preview is confirmed

    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.fileProvider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths_public" />
    </provider>

#### If you need to modify the shooting and recording interface and logic
Please refer to Demo for simple extensions. If you need to go deeper to learn more about extensions [simple architecture of CameraFragment](https://juejin.cn/post/7136108758010167304/)

#### If there are other common problems, you can see the [website](https://github.com/zhongjhATC/AlbumCameraRecorder/issues) search error keywords

#### If you need to use the data of the nine-grid exhibition, you can see A [code](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/app/src/main/java/com/zhongjh/cameraapp/MainSeeActivity.java) for details.

#### Related APIs, more APIs and support for continuous enrichment added
 - [Call the multimedia public configuration API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/GlobalSettingApi.java).
 - [Call the multimedia album configuration API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/AlbumSettingApi.java).
 - [Call the recording configuration API for multimedia](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/CameraSettingApi.java).
 - [Call the multimedia recording configuration API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/RecorderSettingApi.java).
 - [Multimedia UI related properties configuration](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/res/values/styles.xml)
 - [Multimedia related static methods](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/AlbumCameraRecorderApi.java)


If you use the shown library, the following APIs are also useful for you
 - [Nine-grid related API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/progresslibrary/src/main/java/com/zhongjh/progresslibrary/api/MaskProgressApi.java).
 - [Nine-grid dependent Events](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/progresslibrary/src/main/java/com/zhongjh/progresslibrary/listener/MaskProgressLayoutListener.java).
 - [Nine-grid related properties, configuring the UI, etc](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/progresslibrary/src/main/res/values/attrs.xml)



## History update
[History update](https://github.com/zhongjhATC/AlbumCameraRecorder/releases).

## Apk direct experience download
 - The 1.1.29x version may be slightly different from the current latest code version
[Gitee download address](https://gitee.com/zhongjh/AlbumCameraRecorder/raw/androidx/apk/app-release.apk)

# Write in the last

1. If you think it works, give me a Star（[GitHub](https://github.com/zhongjhATC/AlbumCameraRecorder)）

2. If you find any BUG, please leave a comment or leave a Issues（[Issues](https://github.com/zhongjhATC/AlbumCameraRecorder/issues)）

3. Any reprint please indicate the source
