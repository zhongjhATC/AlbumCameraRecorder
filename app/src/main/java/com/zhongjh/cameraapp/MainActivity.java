package com.zhongjh.cameraapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.album.enums.MimeType;
import com.zhongjh.albumcamerarecorder.album.filter.Filter;
import com.zhongjh.albumcamerarecorder.preview.entity.PreviewItem;
import com.zhongjh.albumcamerarecorder.recorder.db.RecordingItem;
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.CaptureStrategy;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;
import com.zhongjh.albumcamerarecorder.utils.constants.MultimediaTypes;
import com.zhongjh.cameraapp.databinding.ActivityMainBinding;
import com.zhongjh.progresslibrary.entity.MultiMedia;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.retrofitdownloadlib.http.DownloadHelper;
import com.zhongjh.retrofitdownloadlib.http.DownloadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 23;

    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码
    private HashMap<MultiMedia, MyTask> timers = new HashMap<>();
    ActivityMainBinding mBinding;


    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.mplImageList.setMaskProgressLayoutListener(new MaskProgressLayoutListener() {

            @Override
            public void onItemAdd(View view, MultiMedia multiMedia, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击添加
                getPermissions(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            }

            @Override
            public void onItemImage(View view, MultiMedia multiMedia) {
                // 点击详情
                if (multiMedia.getType() == MultimediaTypes.PICTURE) {
                    // 判断如果是图片类型就预览当前所有图片
                    List<PreviewItem> previewItems = new ArrayList<>();
                    for (MultiMedia item : mBinding.mplImageList.getImages()) {
                        PreviewItem previewItem = new PreviewItem(item.getUri(),item.getUrl());
                        previewItems.add(previewItem);
                    }
                    MultiMediaSetting.openPreviewImage(MainActivity.this, previewItems);
                } else if (multiMedia.getType() == MultimediaTypes.VIDEO) {
                    // 判断如果是视频类型就预览视频
                    List<Uri> uris = new ArrayList<>();
                    uris.add(multiMedia.getUri());
                    MultiMediaSetting.openPreviewVideo(MainActivity.this, uris);
                }
            }

            @Override
            public void onItemStartUploading(MultiMedia multiMedia) {
                // 开始模拟上传 - 指刚添加后的。这里可以使用你自己的上传事件
                MyTask timer = new MyTask(multiMedia);
                timers.put(multiMedia, timer);
                timer.schedule();
            }

            @Override
            public void onItemClose(View view, MultiMedia multiMedia) {
                // 停止上传
                timers.get(multiMedia).cancel();
            }

            @Override
            public void onItemAudioStartDownload(String url) {

            }

            @Override
            public void onItemVideoStartDownload(String url) {

            }

        });
        mBinding.btnOpenSee.setOnClickListener(v -> MainSeeActivity.newInstance(MainActivity.this));
    }

    /**
     * 获取权限
     */
    private void getPermissions(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager
                    .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager
                            .PERMISSION_GRANTED) {
                openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA}, GET_PERMISSION_REQUEST);
            }
        } else {
            openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            // 获取类型，根据类型设置不同的事情
            switch (MultiMediaSetting.obtainMultimediaType(data)) {
                case MultimediaTypes.PICTURE:
                    // 图片
                    List<String> path = MultiMediaSetting.obtainPathResult(data);
                    mBinding.mplImageList.addImages(path);
                    break;
                case MultimediaTypes.VIDEO:
                    // 录像
                    List<String> videoPath = MultiMediaSetting.obtainPathResult(data);
                    mBinding.mplImageList.addVideo(videoPath);
                    break;
                case MultimediaTypes.AUDIO:
                    // 语音
                    RecordingItem recordingItem = MultiMediaSetting.obtainRecordingItemResult(data);
                    mBinding.mplImageList.addAudio(recordingItem.getFilePath(), recordingItem.getLength());
                    break;
                case MultimediaTypes.BLEND:
                    // 混合类型，意思是图片可能跟录像在一起.
                    mBinding.mplImageList.addImages(MultiMediaSetting.obtainPathResult(data));
                    break;
            }

        }
    }

    @Override
    protected void onDestroy() {
        // 停止所有的上传
        for (Map.Entry<MultiMedia, MyTask> entry : timers.entrySet()) {
            entry.getValue().cancel();
        }
        super.onDestroy();
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_REQUEST) {
            int size = 0;
            if (grantResults.length >= 1) {
                int writeResult = grantResults[0];
                //读写内存权限
                boolean writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;//读写内存权限
                if (!writeGranted) {
                    size++;
                }
                //录音权限
                int recordPermissionResult = grantResults[1];
                boolean recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!recordPermissionGranted) {
                    size++;
                }
                //相机权限
                int cameraPermissionResult = grantResults[2];
                boolean cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }
                if (size == 0) {
                    startActivityForResult(new Intent(MainActivity.this, com.zhongjh.albumcamerarecorder.MainActivity.class), 100);
                } else {
                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * @param alreadyImageCount 已经存在显示的几张图片
     * @param alreadyVideoCount 已经存在显示的几个视频
     * @param alreadyAudioCount 已经存在显示的几个音频
     *                          打开窗体
     */
    private void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {

        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        cameraSetting.mimeTypeSet(MimeType.ofAll());// 支持的类型：图片，视频
        cameraSetting.captureStrategy(new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/camera")); // 保存目录

        // 相册
        AlbumSetting albumSetting = new AlbumSetting(true)
                .mimeTypeSet(MimeType.ofAll())// 支持的类型：图片，视频
                .captureStrategy(
                        new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/album"))// 设置路径和7.0保护路径等等
                .showSingleMediaType(true) // 仅仅显示一个多媒体类型
                .countable(true)// 是否显示多选图片的数字
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))// 自定义过滤器
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))// 九宫格大小
                .thumbnailScale(0.85f)// 图片缩放比例
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
        recorderSetting.captureStrategy(new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/recorder"));// 保存目录

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
            globalSetting.albumSetting(albumSetting);
        if (mBinding.cbCamera.isChecked())
            globalSetting.cameraSetting(cameraSetting);
        if (mBinding.cbRecorder.isChecked())
            globalSetting.recorderSetting(recorderSetting);

        globalSetting
                .setOnMainListener(errorMessage -> Toast.makeText(MainActivity.this.getApplicationContext(), "自定义失败信息：录音已经达到上限", Toast.LENGTH_LONG).show())
                .captureStrategy(
                        new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/test"))// 设置路径和7.0保护路径等等
                //                                            .imageEngine(new GlideEngine())  // for glide-V3
                .imageEngine(new Glide4Engine())    // for glide-V4
                .maxSelectable(Integer.valueOf(mBinding.etAllCount.getText().toString()) - (alreadyImageCount + alreadyVideoCount))// 全部最多选择几个
                .maxSelectablePerMediaType(Integer.valueOf(mBinding.etAllCount.getText().toString()) - alreadyImageCount, 1 - alreadyVideoCount, 1 - alreadyAudioCount)// 最大10张图片或者最大1个视频
                .forResult(REQUEST_CODE_CHOOSE);


    }

    private void openMain2(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        cameraSetting.mimeTypeSet(MimeType.ofAll());// 支持的类型：图片，视频
        cameraSetting.captureStrategy(new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/camera")); // 保存目录

        // 相册
        AlbumSetting albumSetting = new AlbumSetting(true)
                .mimeTypeSet(MimeType.ofAll())// 支持的类型：图片，视频
                .captureStrategy(
                        new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/album"))// 设置路径和7.0保护路径等等
                .showSingleMediaType(true) // 仅仅显示一个多媒体类型
                .countable(true)// 是否显示多选图片的数字
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))// 自定义过滤器
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))// 九宫格大小
                .thumbnailScale(0.85f)// 图片缩放比例
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
        recorderSetting.captureStrategy(new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/recorder"));// 保存目录

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
                .maxSelectable(Integer.valueOf(mBinding.etAllCount.getText().toString()) - (alreadyImageCount + alreadyVideoCount))// 全部最多选择几个
                .maxSelectablePerMediaType(Integer.valueOf(mBinding.etAllCount.getText().toString()) - alreadyImageCount, 1 - alreadyVideoCount, 1 - alreadyAudioCount)// 最大10张图片或者最大1个视频
                .forResult(REQUEST_CODE_CHOOSE);
    }

    class MyTask extends Timer {

        int percentage = 0;// 百分比
        MultiMedia multiMedia;

        MyTask(MultiMedia multiMedia) {
            this.multiMedia = multiMedia;
        }

        void schedule() {
            this.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        percentage++;
                        multiMedia.setPercentage(percentage);
                    });
                }
            }, 1000, 100);
        }

    }

}
