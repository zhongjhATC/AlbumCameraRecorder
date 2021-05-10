# AlbumCameraRecorderX

[![MinSdk](https://img.shields.io/badge/MinSdk-21-blue.svg)](https://developer.android.com/about/versions/android-5.0)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/LICENSE)

## This release is an offshoot of the AndroidX release.Any version number followed by an X is based on the AndroidX version.
## At present, it has been put into use in the formal project.
## If you have any suggestions or want to add functions, you can put forward on Issues

## 中文
An efficient multimedia support operation library, can be a variety of simple configuration operation photo, album, recording, recording and other functions.

Also support supporting the use of the display of pictures, video, audio 9 grid function.


Part of the code for this open source library comes from [Matisse](https://github.com/zhihu/Matisse).

Thank you very much Zhihu for providing such a great open source project!

## Non-X version branching
A non-X library version, no longer maintained(https://github.com/zhongjhATC/AlbumCameraRecorder/tree/master)

## peculiarity
 - Support for custom styles. Support to change the relevant buttons inside.
 - Support album, recording, recording and other functions in one (similar to Douyin, etc.), and you can configure only one of the functions independently.
 - While there are many features, some libraries can be introduced as required
 - Rich callback interface and debugging information, using the existing API to achieve a rich effect.
 - Strong compatibility, whether the lower version of 4.1 or the current latest version of Android 11, has been carried out related compatibility processing
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
 - All recorded photos are compressed
 - Version after 1.0.19x is compatible with Android Q version, if you want to keep your project SDK28, you can only keep 1.0.18x

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
	     // Public library, which must be used
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:albumCameraRecorderCommon:1.0.31X'
	     // Core lib, call display album, recording screen, recording, etc
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:multilibrary:1.0.31X'
         // Supporting use, mainly used to obtain data after the relevant display, the corresponding upload progress display, if you only need to obtain photos, video, audio and other data, their own code to obtain the data after the presentation, you can not need to use this
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:progresslibrary:1.0.31X'
         // Supporting the use of editing pictures
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:imageedit:1.0.31X'
	     // Supporting the use of editing video
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:videoedit:1.0.31X'
	}

## snapshoot
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/Demonstration.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/DemonstrationShowImg.png)



## Compatibility testing of mobile phones is commonly used in the market
100% through[Compatibility Test Report](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/WeTest.md).
![](https://raw.githubusercontent.com/zhongjhATC/AlbumCameraRecorder/master/wetest/5.jpg)

## use
#### Enable multimedia related functions

        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        cameraSetting.mimeTypeSet(MimeType.ofAll());// 支持的类型：图片，视频

        // 相册
        AlbumSetting albumSetting = new AlbumSetting(true)
                .mimeTypeSet(MimeType.ofAll())// 支持的类型：图片，视频
                .countable(true)// 是否显示多选图片的数字
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))// 自定义过滤器
                .originalEnable(true)// 开启原图
                .maxOriginalSize(10); // 最大原图size,仅当originalEnable为true的时候才有效

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();

        // 全局
        GlobalSetting globalSetting = MultiMediaSetting.from(MainSimpleActivity.this).choose(MimeType.ofAll());

        if (mBinding.cbAlbum.isChecked())
            // 开启相册功能
            globalSetting.albumSetting(albumSetting);
        if (mBinding.cbCamera.isChecked())
            // 开启拍摄功能
            globalSetting.cameraSetting(cameraSetting);
        if (mBinding.cbRecorder.isChecked())
            // 开启录音功能
            globalSetting.recorderSetting(recorderSetting);

        globalSetting
                .setOnMainListener(errorMessage -> Toast.makeText(MainSimpleActivity.this.getApplicationContext(), "自定义失败信息：录音已经达到上限", Toast.LENGTH_LONG).show())
                .allStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/test"))// 设置路径和7.0保护路径等等
                .imageEngine(new Glide4Engine())    // for glide-V4
                .maxSelectablePerMediaType(5 - alreadyImageCount, 1 - alreadyVideoCount, 1 - alreadyAudioCount)// 最大10张图片或者最大1个视频
                .forResult(REQUEST_CODE_CHOOSE);

#### 获取相关返回的数据

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case REQUEST_CODE_PREVIEW:
                ```
            case REQUEST_CODE_CHOOSE:
                // 获取类型，根据类型设置不同的事情
                switch (MultiMediaSetting.obtainMultimediaType(data)) {
                    case MultimediaTypes.PICTURE:
                        // 图片
                        List<String> path = MultiMediaSetting.obtainPathResult(data);
                        mBinding.mplImageList.addImagesStartUpload(path);
                        break;
                    case MultimediaTypes.VIDEO:
                        // 录像
                        List<String> videoPath = MultiMediaSetting.obtainPathResult(data);
                        mBinding.mplImageList.addVideoStartUpload(videoPath);
                        break;
                    case MultimediaTypes.AUDIO:
                        // 语音
                        RecordingItem recordingItem = MultiMediaSetting.obtainRecordingItemResult(data);
                        mBinding.mplImageList.addAudioStartUpload(recordingItem.getFilePath(), recordingItem.getLength());
                        break;
                    case MultimediaTypes.BLEND:
                        // 混合类型，意思是图片可能跟录像在一起.
                        mBinding.mplImageList.addImagesStartUpload(MultiMediaSetting.obtainPathResult(data));
                        break;
                }
                break;
        }
    }

#### 如果你需要用到九宫格展览数据，具体可以看[相关代码](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/app/src/main/java/com/zhongjh/cameraapp/MainSeeActivity.java).

#### 相关API,更多API和支持持续丰富加入
 - [调用多媒体的公共配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/GlobalSettingApi.java).
 - [调用多媒体的相册配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/AlbumSettingApi.java).
 - [调用多媒体的录制配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/CameraSettingApi.java).
 - [调用多媒体的录音配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/RecorderSettingApi.java).
 - [多媒体UI相关属性配置](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/multilibrary/src/main/res/values/styles.xml)

如果你使用展示的九宫库，那么下面这些api对你也有用
 - [九宫格相关API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/progresslibrary/src/main/java/com/zhongjh/progresslibrary/api/MaskProgressApi.java).
 - [九宫格相关事件](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/progresslibrary/src/main/java/com/zhongjh/progresslibrary/listener/MaskProgressLayoutListener.java).
 - [九宫格相关属性，配置UI等等](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/progresslibrary/src/main/res/values/attrs.xml)



## 历史更新
从1.0.1版本开始总结的[历史更新](https://github.com/zhongjhATC/AlbumCameraRecorder/releases).

## apk直接体验下载
 - 1.0.0版本，跟当前最新代码版本可能会有稍许不同
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/qrcode.png)

 - 链接下载地址：https://fir.im/s9b6?release_id=5c84dcd3ca87a807f7ef5181&fir_source=%E7%89%88%E6%9C%AC1&fir_campaign=%E7%89%88%E6%9C%AC1

# 写在最后

1. 觉得好用的欢迎给个Star（[GitHub](https://github.com/zhongjhATC/AlbumCameraRecorder)）

2. 发现任何BUG欢迎留言或者留个Issues（[Issues](https://github.com/zhongjhATC/AlbumCameraRecorder/issues)）

3. 任何转载请注明出处

# QQ群915053430 此群于2021.4.28新建。用于解决问题，确保当前库是可以兼容最新版本，当然也坚持打造最万能的多媒体操作库
