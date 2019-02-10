# AlbumCameraRecorder

![Methods](https://img.shields.io/badge/Methods%20%7C%20Size-723%20%7C%2076%20KB-e91e63.svg)
 
 
 // 拍摄
        CameraSetting cameraSetting = new CameraSetting();
        cameraSetting.mimeTypeSet(MimeType.ofAll());
        cameraSetting.supportSingleMediaType(false);
        cameraSetting.captureStrategy(new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/camera"));

        // 相册
        AlbumSetting albumSetting = new AlbumSetting(true)
                .captureStrategy(
                        new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/album"))// 设置路径和7.0保护路径等等
                .showSingleMediaType(true) // 仅仅显示一个多媒体类型
                .countable(true)// 是否显示多选图片的数字
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
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
        recorderSetting.captureStrategy(new CaptureStrategy(true,"com.zhongjh.cameraapp.fileprovider", "AA/recorder"));

        // 全局
        MultiMediaSetting.from(MainActivity.this)
                .choose(MimeType.ofImage())
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
