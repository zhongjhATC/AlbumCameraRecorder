# AlbumCameraRecorderX

[![MinSdk](https://img.shields.io/badge/MinSdk-21-blue.svg)](https://developer.android.com/about/versions/android-5.0)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/LICENSE)

## 该版本刚发版不久，如果你想更加稳定的版本可以选择androidx分支,版本号1.-.--X,请注意,androidx分支代码除非严重bug,否则不会再进行维护。
## 该版本使用camerax完成的拍摄,在自定义拍摄参数等会更加容易、动态水印更加简单。全局动画会更加细腻。同时整理了冗余的配置、属性等
## 有任何建议或者想添加的功能，都可提在Issues

## 依然支持java调用

## [English](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/README_EN.md)
一个高效的多媒体支持操作库，可多方面的简单配置操作拍照、相册、录制、录音等功能。

也支持配套使用的展示图片、视频、音频的九宫格功能。

## androidx稳定版本分支
已经停止维护(https://github.com/zhongjhATC/AlbumCameraRecorder/tree/androidx)

## 特性
 - 录制拍照完全支持自己自定义！详情请看Demo如何使用，如果需要深入扩展需要了解更多代码[CameraFragment架构简介](https://juejin.cn/post/7136108758010167304/)
 - 支持相册、录制、录音等三合一功能（类似抖音等），并且也可以通过配置只独立出其中一个功能.
 - 支持可自定义权限请求也可以直接交由该库完成权限请求
 - 虽然功能很多，但是可以按照所需功能来引入某些库
 - 丰富的回调接口和调试信息,可利用现有API实现丰富的效果.
 - 兼容性强，不管是低版本的4.1还是目前最新版本的Android 16,都进行了相关兼容处理
 - 支持所有图片读取处理自定义，例如可自定义glide、Fresco等等都可以
 - 支持从相册选择图片
 - 支持相册按照手机文件区分不同的文件夹选择
 - 自定义性强，支持各种自定义最多选多少张图片、视频等等，也支持只显示自定义文件大小
 - 支持自定义相册的样式，颜色，大小等等
 - 兼容横竖版调整
 - 录制拍照时支持闪光灯、前后摄像头切换
 - 录制拍照时支持双指触摸放大缩小，支持单指上下滑动控制亮度
 - 录制拍照长按按钮等所有UI可自定义，全是svg图片可以很好的处理动画细节
 - 录制支持分段录制，以后还会加入视频编辑
 - 图片编辑支持颜色涂鸦、输入文字、马赛克处理、旋转、裁剪等处理
 - 支持拍摄、录制时的水印功能,甚至支持动态水印
 - 自带权限功能，无需修改任意代码，权限功能包括权限检测、发送权限时告知为何请求、多次拒绝后会提示是否跳转到设置界面设置权限。也可以自定义权限
 - 所有录制拍照都可以选择深度压缩处理，图片可选择自定义压缩处理，也可以使用Demo中的LuBan压缩，视频压缩则是使用ffmpeg处理
 - 完善的缓存管理系统
 - 完善的动画效果、共享元素动画、按钮细节动画
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
	     // 如果想简化代码并且同时用到multilibrary和grid、albumCameraRecorderCommon,可以直接使用combined库
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:combined:2.0.10'

	     // 公共库，如果不使用上面的combined库
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:common:2.0.10'
	     // 核心lib，调用显示相册、录屏、录音等
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:multilibrary:2.0.10'
         // 配套使用，主要用于获取数据后进行相关显示，相应的上传进度显示，如果你只需要获取照片录像录音等数据，可以不需要使用这个
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:grid:2.0.10'

         // 配套编辑图片使用
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:imageedit:2.0.10'
	     // 配套编辑视频使用,目前只有压缩功能,后续持续添加相关功能,因为该库使用了ffmpeg,占用了文件大小25M,根据实际情况选择是否使用
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:videoedit:2.0.10'
        
	}

#### Step 3. gradle file to add configuration

    android.enableJetifier=true
    android.useAndroidX=true

## 快照
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/Demonstration.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/Demonstration1.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/Demonstration2.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/DemonstrationShowImg.png)

## 市场上常用手机兼容测试
100%通过[兼容测试报告](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/WeTest.md).
![](https://raw.githubusercontent.com/zhongjhATC/AlbumCameraRecorder/kotlin/wetest/5.jpg)

## 使用(更多功能建议下载Demo了解)
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

#### 获取相关返回的数据

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
    
#### 如果还有别的常见问题，具体可以看[可能会发生的问题](https://github.com/zhongjhATC/AlbumCameraRecorder/issues)搜索报错关键字    

#### 如果你需要修改拍摄录制的界面和逻辑 
简单扩展请参考Demo，如果需要深入扩展了解更多[CameraFragment架构简洁](https://juejin.cn/post/7136108758010167304/)
但是请注意,如果有支持配置的情况下,是配置优先.只有在没有配置的情况下,才在这里进行自定义扩展

#### 如果你需要用到九宫格展览数据，具体可以看[相关代码](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/app/src/main/java/com/zhongjh/demo/phone/MainSeeActivity.java).

#### 相关API,更多API和支持持续丰富加入
 - [调用多媒体的公共配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/java/com/zhongjh/multimedia/settings/api/GlobalSettingApi.kt).
 - [调用多媒体的相册配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/java/com/zhongjh/multimedia/settings/api/AlbumSettingApi.kt).
 - [调用多媒体的录制配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/java/com/zhongjh/multimedia/settings/api/CameraSettingApi.kt).
 - [调用多媒体的录音配置API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/java/com/zhongjh/multimedia/settings/api/RecorderSettingApi.kt).
 - [多媒体相关静态方法](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/java/com/zhongjh/multimedia/AlbumCameraRecorderApi.kt)
 - [多媒体UI相关属性配置](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/multilibrary/src/main/res/values/styles.xml)

如果你使用展示的九宫库，那么下面这些api对你也有用
 - [九宫格相关API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/gridview/src/main/java/com/zhongjh/gridview/api/GridViewApi.kt).
 - [九宫格相关事件](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/gridview/src/main/java/com/zhongjh/gridview/listener/GridViewListener.kt).
 - [九宫格相关属性，配置UI等等](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/kotlin/gridview/src/main/res/values/attrs.xml)



## 历史更新
从1.0.1版本开始总结的[历史更新](https://github.com/zhongjhATC/AlbumCameraRecorder/releases).

# 写在最后

1. 觉得好用的欢迎给个Star（[GitHub](https://github.com/zhongjhATC/AlbumCameraRecorder)）

2. 发现任何BUG欢迎留言或者留个Issues（[Issues](https://github.com/zhongjhATC/AlbumCameraRecorder/issues)）

3. 任何转载请注明出处

# QQ群915053430 此群于2021.4.28新建。用于及时解决问题，加入群之前建议先把问题提在issues再询问，这样可以方便后来者参考
