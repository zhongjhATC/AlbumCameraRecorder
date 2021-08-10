# AlbumCameraRecorderX

[![MinSdk](https://img.shields.io/badge/MinSdk-21-blue.svg)](https://developer.android.com/about/versions/android-5.0)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/LICENSE)

## This release is an offshoot of the AndroidX release.Any version number followed by an X is based on the AndroidX version.
## At present, it has been put into use in the formal project.
## If you have any suggestions or want to add functions, you can put forward on Issues

## [中文](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/androidx/README_CN.md)
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
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:albumCameraRecorderCommon:1.1.16X'
	     // Core lib, call display album, recording screen, recording, etc
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:multilibrary:1.1.16X'
         // Supporting use, mainly used to obtain data after the relevant display, the corresponding upload progress display, if you only need to obtain photos, video, audio and other data, their own code to obtain the data after the presentation, you can not need to use this
         implementation 'com.github.zhongjhATC.AlbumCameraRecorder:progresslibrary:1.1.16X'
         // Supporting the use of editing pictures
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:imageedit:1.1.16X'
	     // Supporting the use of editing video
	     implementation 'com.github.zhongjhATC.AlbumCameraRecorder:videoedit:1.1.16X'
	}

## snapshoot
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/Demonstration.gif)
![](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/DemonstrationShowImg.png)



## Compatibility testing of mobile phones is commonly used in the market
100% through[Compatibility Test Report](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/WeTest.md).
![](https://raw.githubusercontent.com/zhongjhATC/AlbumCameraRecorder/master/wetest/5.jpg)

## use
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
        mGlobalSetting = MultiMediaSetting.from(MainSimpleActivity.this).choose(MimeType.ofAll());

        if (mBinding.cbAlbum.isChecked()){
            // Open the album function
            mGlobalSetting.albumSetting(mAlbumSetting);
        }
        if (mBinding.cbCamera.isChecked()){
            // Turn on the shooting function
            mGlobalSetting.cameraSetting(cameraSetting);
        }
        if (mBinding.cbRecorder.isChecked()){
            // Enable recording
            mGlobalSetting.recorderSetting(recorderSetting);
        }

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
        switch (requestCode) {
            case REQUEST_CODE_PREVIEW:
                ```
            case REQUEST_CODE_CHOOSE:
                // Gets the type and sets different things according to the type
                switch (MultiMediaSetting.obtainMultimediaType(data)) {
                    case MultimediaTypes.PICTURE:
                        // picture
                        List<String> path = MultiMediaSetting.obtainResult(data);
                        mBinding.mplImageList.addImagesStartUpload(path);
                        break;
                    case MultimediaTypes.VIDEO:
                        // video
                        List<String> videoPath = MultiMediaSetting.obtainResult(data);
                        mBinding.mplImageList.addVideoStartUpload(videoPath);
                        break;
                    case MultimediaTypes.AUDIO:
                        // voice
                        RecordingItem recordingItem = MultiMediaSetting.obtainRecordingItemResult(data);
                        mBinding.mplImageList.addAudioStartUpload(recordingItem.getFilePath(), recordingItem.getLength());
                        break;
                    case MultimediaTypes.BLEND:
                        // Mixed type, which means the image may accompany the video.
                        List<Uri> blends = MultiMediaSetting.obtainResult(data);
                        List<Uri> images = new ArrayList<>();
                        List<Uri> videos = new ArrayList<>();
                        // Type of circular judgment
                        for (Uri uri : blends) {
                            DocumentFile documentFile = DocumentFile.fromSingleUri(getBaseContext(), uri);
                            if (documentFile.getType().startsWith("image")) {
                                images.add(uri);
                            } else if (documentFile.getType().startsWith("video")) {
                                videos.add(uri);
                            }
                        }
                        // Upload pictures and videos separately
                        mBinding.mplImageList.addUrisStartUpload(images);
                        mBinding.mplImageList.addVideoStartUpload(videos);
                        break;
                }
                break;
        }
    }

#### If you need to use the data of the nine-grid exhibition, you can see A [code](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/app/src/main/java/com/zhongjh/cameraapp/MainSeeActivity.java) for details.

#### Related APIs, more APIs and support for continuous enrichment added
 - [Call the multimedia public configuration API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/GlobalSettingApi.java).
 - [Call the multimedia album configuration API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/AlbumSettingApi.java).
 - [Call the recording configuration API for multimedia](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/CameraSettingApi.java).
 - [Call the multimedia recording configuration API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/multilibrary/src/main/java/com/zhongjh/albumcamerarecorder/settings/api/RecorderSettingApi.java).
 - [Multimedia UI related properties configuration](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/multilibrary/src/main/res/values/styles.xml)

If you use the shown library, the following APIs are also useful for you
 - [Nine-grid related API](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/progresslibrary/src/main/java/com/zhongjh/progresslibrary/api/MaskProgressApi.java).
 - [Nine-grid dependent Events](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/progresslibrary/src/main/java/com/zhongjh/progresslibrary/listener/MaskProgressLayoutListener.java).
 - [Nine-grid related properties, configuring the UI, etc](https://github.com/zhongjhATC/AlbumCameraRecorder/blob/master/progresslibrary/src/main/res/values/attrs.xml)



## History update
[History update](https://github.com/zhongjhATC/AlbumCameraRecorder/releases).

# Write in the last

1. If you think it works, give me a Star（[GitHub](https://github.com/zhongjhATC/AlbumCameraRecorder)）

2. If you find any BUG, please leave a comment or leave a Issues（[Issues](https://github.com/zhongjhATC/AlbumCameraRecorder/issues)）

3. Any reprint please indicate the source
