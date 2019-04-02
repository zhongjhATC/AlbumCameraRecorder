# AlbumCameraRecorder

[![MinSdk](https://img.shields.io/badge/MinSdk-16-blue.svg)](https://developer.android.com/about/versions/android-4.1)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/LICENSE)

## 目前已经投入到正式项目中使用。
## 有开了兼容云测试包月的小伙伴们，可以麻烦私聊下我吗谢谢！

## 中文
一个高效的多媒体支持操作库，可多方面的简单配置操作拍照、相册、录制、录音等功能。

也支持配套使用的展示图片、视频、音频的九宫格功能。


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
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:albumCameraRecorderCommon:1.0.6'        // 公共库，必须使用此库
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:multilibrary:1.0.6'      // 核心lib，调用显示相册、录屏、录音等
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:progresslibrary:1.0.6' // 配套使用，主要用于获取数据后进行相关显示，相应的上传进度显示，如果你只需要获取照片录像录音等数据，自行写获取后呈现方式，可以不需要是用这个
	}

## 快照
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/Demonstration.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/DemonstrationShowImg.png)



## 市场上常用手机兼容测试
100%通过[兼容测试报告](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/WeTest.md).
![](https://raw.githubusercontent.com/zhongjhATC/AlbumCameraRecorder/master/wetest/5.jpg)

## 使用   
#### 启动多媒体相关功能
 
        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        Set<MimeType> mimeTypeCameras;
        if (mBinding.cbCameraImage.isChecked() && mBinding.cbCameraVideo.isChecked()) {
            mimeTypeCameras = MimeType.ofAll();
            cameraSetting.mimeTypeSet(mimeTypeCameras);// 支持的类型：图片，视频
        } else if (mBinding.cbCameraImage.isChecked()) {
            mimeTypeCameras = MimeType.ofVideo();
            cameraSetting.mimeTypeSet(mimeTypeCameras);// 支持的类型：图片，视频
        } else if (mBinding.cbCameraVideo.isChecked()) {
            mimeTypeCameras = MimeType.ofImage();
            cameraSetting.mimeTypeSet(mimeTypeCameras);// 支持的类型：图片，视频
        }
        cameraSetting.duration(Integer.parseInt(mBinding.etCameraDuration.getText().toString()));// 最长录制时间
        cameraSetting.minDuration(Integer.parseInt(mBinding.etMinCameraDuration.getText().toString()) * 1000);// 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制

        // 相册
        AlbumSetting albumSetting = new AlbumSetting(true);
        Set<MimeType> mimeTypeAlbum;
        if (mBinding.cbAlbumImage.isChecked() && mBinding.cbAlbumVideo.isChecked()) {
            mimeTypeAlbum = MimeType.ofAll();
            cameraSetting.mimeTypeSet(mimeTypeAlbum);// 支持的类型：图片，视频
        } else if (mBinding.cbAlbumImage.isChecked()) {
            mimeTypeAlbum = MimeType.ofVideo();
            cameraSetting.mimeTypeSet(mimeTypeAlbum);// 支持的类型：图片，视频
        } else if (mBinding.cbAlbumVideo.isChecked()) {
            mimeTypeAlbum = MimeType.ofImage();
            cameraSetting.mimeTypeSet(mimeTypeAlbum);// 支持的类型：图片，视频
        }

        albumSetting
                .showSingleMediaType(mBinding.cbShowSingleMediaTypeTrue.isChecked()) // 仅仅显示一个多媒体类型
                .countable(mBinding.cbCountableTrue.isChecked())// 是否显示多选图片的数字
                .addFilter(new GifSizeFilter(Integer.parseInt(mBinding.etAddFilterMinWidth.getText().toString()), Integer.parseInt(mBinding.etAddFilterMinHeight.getText().toString()), Integer.parseInt(mBinding.etMaxSizeInBytes.getText().toString()) * Filter.K * Filter.K))// 自定义过滤器
                .gridExpectedSize(dip2px(Integer.parseInt(mBinding.etGridExpectedSize.getText().toString())))// 九宫格大小 ,建议这样使用getResources().getDimensionPixelSize(R.dimen.grid_expected_size)
                .thumbnailScale(0.85f)// 图片缩放比例
                .setOnSelectedListener((uriList, pathList) -> {
                    // 每次选择的事件
                    Log.e("onSelected", "onSelected: pathList=" + pathList);
                })
                .originalEnable(mBinding.cbOriginalEnableTrue.isChecked())// 开启原图
                .maxOriginalSize(Integer.parseInt(mBinding.etMaxOriginalSize.getText().toString())) // 最大原图size,仅当originalEnable为true的时候才有效
                .setOnCheckedListener(isChecked -> {
                    // DO SOMETHING IMMEDIATELY HERE
                    Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                });

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();

        // 全局,确定类型
        Set<MimeType> mimeTypes = null;
        if (mBinding.rbAllAll.isChecked())
            mimeTypes = MimeType.ofAll();
        else if (mBinding.rbAllVideo.isChecked())
            mimeTypes = MimeType.ofVideo();
        else if (mBinding.rbAllImage.isChecked())
            mimeTypes = MimeType.ofImage();

        GlobalSetting globalSetting = MultiMediaSetting.from(MainActivity.this).choose(mimeTypes);

        if (mBinding.cbAlbum.isChecked())
            // 开启相册功能
            globalSetting.albumSetting(albumSetting);
        if (mBinding.cbCamera.isChecked())
            // 开启拍摄功能
            globalSetting.cameraSetting(cameraSetting);
        if (mBinding.cbRecorder.isChecked())
            // 开启录音功能
            globalSetting.recorderSetting(recorderSetting);

        // 自定义失败信息
        globalSetting.setOnMainListener(errorMessage -> Toast.makeText(MainActivity.this.getApplicationContext(), "自定义失败信息：录音已经达到上限", Toast.LENGTH_LONG).show());

        // 自定义路径，如果其他子权限设置了路径，那么以子权限为准
        if (!TextUtils.isEmpty(mBinding.etAllFile.getText().toString()))
            globalSetting.allStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etAllFile.getText().toString()));// 设置路径和7.0保护路径等等
        if (!TextUtils.isEmpty(mBinding.etPictureFile.getText().toString()))
            globalSetting.pictureStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etPictureFile.getText().toString()));// 设置路径和7.0保护路径等等
        if (!TextUtils.isEmpty(mBinding.etAudioFile.getText().toString()))
            globalSetting.audioStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etAudioFile.getText().toString()));// 设置路径和7.0保护路径等等
        if (!TextUtils.isEmpty(mBinding.etVideoFile.getText().toString()))
            globalSetting.videoStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etVideoFile.getText().toString()));// 设置路径和7.0保护路径等等

        //                                            .imageEngine(new GlideEngine())  // for glide-V3
        globalSetting.imageEngine(new Glide4Engine())    // for glide-V4
                .maxSelectablePerMediaType(Integer.valueOf(mBinding.etAlbumCount.getText().toString()) - alreadyImageCount,
                        Integer.valueOf(mBinding.etVideoCount.getText().toString()) - alreadyVideoCount,
                        Integer.valueOf(mBinding.etAudioCount.getText().toString()) - alreadyAudioCount)// 最大10张图片或者最大1个视频
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
从1.0.1版本开始总结的[历史更新](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/UPDATE.md).

## apk直接体验下载
 - 1.0.0版本，跟当前最新代码版本可能会有稍许不同
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/qrcode.png)

 - 链接下载地址：https://fir.im/s9b6?release_id=5c84dcd3ca87a807f7ef5181&fir_source=%E7%89%88%E6%9C%AC1&fir_campaign=%E7%89%88%E6%9C%AC1

## 喜欢的麻烦在顶部点个star
