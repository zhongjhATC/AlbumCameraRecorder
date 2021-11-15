# AlbumCameraRecorderX

[![MinSdk](https://img.shields.io/badge/MinSdk-21-blue.svg)](https://developer.android.com/about/versions/android-5.0)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/LICENSE)

## 该版本是基于AndroidX版本开发的分支。版本号后面带着X的都是属于基于AndroidX版本开发。
## 目前已经投入到正式项目中使用。
## 有任何建议或者想添加的功能，都可提在Issues

## [English](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/README_EN.md)
一个高效的多媒体支持操作库，可多方面的简单配置操作拍照、相册、录制、录音等功能。

也支持配套使用的展示图片、视频、音频的九宫格功能。


 - 本开源库的部分相册代码来自[Matisse](https://github.com/zhihu/Matisse).
 - 本开源库的相机代码来自[CameraView](https://github.com/natario1/CameraView).

## 非X版本分支
非X库版本,已经停止维护(https://github.com/zhongjhATC/AlbumCameraRecorder/tree/master)

## 特性
 - 支持自定义样式.支持更换里面的相关按钮.
 - 支持相册、录制、录音等三合一功能（类似抖音等），并且也可以通过配置只独立出其中一个功能.
 - 支持可自定义权限请求也可以直接交由该库完成权限请求
 - 虽然功能很多，但是可以按照所需功能来引入某些库
 - 丰富的回调接口和调试信息,可利用现有API实现丰富的效果.
 - 兼容性强，不管是低版本的4.1还是目前最新版本的Android 11,都进行了相关兼容处理
 - 支持所有图片读取处理自定义，例如可自定义glide、Fresco等等都可以
 - 支持从相册选择图片
 - 支持相册按照手机文件区分不同的文件夹选择
 - 自定义性强，支持各种自定义最多选多少张图片、视频等等，也支持只显示自定义文件大小
 - 支持自定义相册的样式，颜色，大小等等
 - 录制拍照时支持闪光灯、前后摄像头切换
 - 录制拍照时支持双指触摸放大缩小，支持单指上下滑动控制亮度
 - 录制拍照长按按钮等所有UI可自定义，全是svg图片可以很好的处理动画细节
 - 录制支持分段录制，以后还会加入视频编辑
 - 图片编辑支持颜色涂鸦、输入文字、马赛克处理、旋转、裁剪等处理
 - 支持拍摄、录制时的水印功能
 - 自带权限功能，无需修改任意代码，权限功能包括权限检测、发送权限时告知为何请求、多次拒绝后会提示是否跳转到设置界面设置权限。也可以自定义权限
 - 所有录制拍照都可以选择深度压缩处理，图片可选择自定义压缩处理，也可以使用Demo中的LuBan压缩，视频压缩则是使用ffmpeg处理
 - 完善的缓存管理系统
 - 完善的动画效果，后续还会继续增加
 - 性能优化，内存泄漏这些都一一仔细处理过

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
	     // 如果想简化代码并且同时用到multilibrary和progresslibrary、albumCameraRecorderCommon,可以直接使用combined库
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:combined:1.1.32X'

	     // 公共库，如果不使用上面的combined库
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:albumCameraRecorderCommon:1.1.32X'
	     // 核心lib，调用显示相册、录屏、录音等
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:multilibrary:1.1.32X'
         // 配套使用，主要用于获取数据后进行相关显示，相应的上传进度显示，如果你只需要获取照片录像录音等数据，可以不需要使用这个
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:progresslibrary:1.1.32X'

         // 配套编辑图片使用
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:imageedit:1.1.32X'
	     // 配套编辑视频使用
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:videoedit:1.1.32X'
	}

## 快照
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/Demonstration.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/DemonstrationShowImg.png)



## 市场上常用手机兼容测试
100%通过[兼容测试报告](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/WeTest.md).
![](https://raw.githubusercontent.com/zhongjhATC/AlbumCameraRecorder/androidx/wetest/5.jpg)

## 使用
#### 启动多媒体相关功能

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

        if (mBinding.cbAlbum.isChecked()){
            // 开启相册功能
            mGlobalSetting.albumSetting(albumSetting);
        }
        if (mBinding.cbCamera.isChecked()){
            // 开启拍摄功能
            mGlobalSetting.cameraSetting(cameraSetting);
        }
        if (mBinding.cbRecorder.isChecked()){
            // 开启录音功能
            mGlobalSetting.recorderSetting(recorderSetting);
        }

        mGlobalSetting
                .setOnMainListener(errorMessage -> {
                    Log.d(TAG, errorMessage);
                    Toast.makeText(MainSimpleActivity.this.getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                })
                // 设置路径和7.0保护路径等等
                .allStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "aabb"))
                // for glide-V4
                .imageEngine(new Glide4Engine())
                // 最大5张图片、最大3个视频、最大1个音频
                .maxSelectablePerMediaType(null,
                        5,
                        3,
                        3,
                        alreadyImageCount,
                        alreadyVideoCount,
                        alreadyAudioCount)
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
                        List<String> path = MultiMediaSetting.obtainResult(data);
                        break;
                    case MultimediaTypes.VIDEO:
                        // 录像
                        List<String> videoPath = MultiMediaSetting.obtainResult(data);
                        break;
                    case MultimediaTypes.AUDIO:
                        // 语音
                        RecordingItem recordingItem = MultiMediaSetting.obtainRecordingItemResult(data);
                        break;
                    case MultimediaTypes.BLEND:
                        // 混合类型，意思是图片可能跟录像在一起.
                        List<Uri> blends = MultiMediaSetting.obtainResult(data);
                        break;
                }
                break;
        }
    }

#### 如果你需要用到九宫格展览数据，具体可以看[相关代码](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/app/src/main/java/com/zhongjh/cameraapp/MainSeeActivity.java).

#### 相关API,更多API和支持持续丰富加入
 - [调用多媒体的公共配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/GlobalSettingApi.java).
 - [调用多媒体的相册配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/AlbumSettingApi.java).
 - [调用多媒体的录制配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/CameraSettingApi.java).
 - [调用多媒体的录音配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/RecorderSettingApi.java).
 - [多媒体UI相关属性配置](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/res/values/styles.xml)
 - [多媒体相关静态方法](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/AlbumCameraRecorderApi.java)

如果你使用展示的九宫库，那么下面这些api对你也有用
 - [九宫格相关API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/progresslibrary/src/main/java/com/zhongjh/progresslibrary/api/MaskProgressApi.java).
 - [九宫格相关事件](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/progresslibrary/src/main/java/com/zhongjh/progresslibrary/listener/MaskProgressLayoutListener.java).
 - [九宫格相关属性，配置UI等等](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/progresslibrary/src/main/res/values/attrs.xml)



## 历史更新
从1.0.1版本开始总结的[历史更新](https://github.com/zhongjhATC/AlbumCameraRecorder/releases).

## apk直接体验下载
 - 1.1.29X版本，跟当前最新代码版本可能会有稍许不同
[gitee下载地址](https://gitee.com/zhongjh/AlbumCameraRecorder/raw/androidx/apk/app-release.apk)

# 写在最后

1. 觉得好用的欢迎给个Star（[GitHub](https://github.com/zhongjhATC/AlbumCameraRecorder)）

2. 发现任何BUG欢迎留言或者留个Issues（[Issues](https://github.com/zhongjhATC/AlbumCameraRecorder/issues)）

3. 任何转载请注明出处

# QQ群915053430 此群于2021.4.28新建。用于解决问题，确保当前库是可以兼容最新版本，当然也坚持打造最万能的多媒体操作库
