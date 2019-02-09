package com.zhongjh.cameraapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhongjh.cameraviewsoundrecorder.settings.AlbumSetting;
import com.zhongjh.cameraviewsoundrecorder.settings.CameraSetting;
import com.zhongjh.cameraviewsoundrecorder.settings.MultiMediaSetting;
import com.zhongjh.cameraviewsoundrecorder.settings.CaptureStrategy;
import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;
import com.zhongjh.cameraviewsoundrecorder.album.filter.Filter;
import com.zhongjh.cameraviewsoundrecorder.camera.util.DeviceUtil;
import com.zhongjh.cameraviewsoundrecorder.utils.constants.MultimediaTypes;
import com.zhongjh.progresslibrary.adapter.ImageAdapter;
import com.zhongjh.progresslibrary.entity.MultiMedia;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;
import com.zhongjh.progresslibrary.widget.MaskProgressView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 23;

    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码
    private ImageView photo;
    private TextView device;
    public MaskProgressLayout mplImageList;

    private DoActionHandler mDoActionHandler = new DoActionHandler(this);
    private ArrayList<MyTask> timers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photo = findViewById(R.id.image_photo);
        device = findViewById(R.id.device);
        device.setText(DeviceUtil.getDeviceInfo());
        mplImageList = findViewById(R.id.mplImageList);
        mplImageList.setMaskProgressLayoutListener(new MaskProgressLayoutListener() {

            @Override
            public void onItemAdd(View view, int position, int alreadyImageCount, int alreadyVideoCount) {
                getPermissions(alreadyImageCount, alreadyVideoCount);
            }

            @Override
            public void onItemImage(View view, int position) {

            }

            @Override
            public void onItemClose(View view, int position) {

            }
        });
    }

    /**
     * 获取权限
     */
    private void getPermissions(int alreadyImageCount, int alreadyVideoCount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager
                    .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager
                            .PERMISSION_GRANTED) {
                openMain(alreadyImageCount, alreadyVideoCount);
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA}, GET_PERMISSION_REQUEST);
            }
        } else {
            openMain(alreadyImageCount, alreadyVideoCount);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        MultiMediaSetting.obtainResult(data), MultiMediaSetting.obtainPathResult(data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            // 获取类型，根据类型设置不同的事情
            switch (MultiMediaSetting.obtainMultimediaType(data)) {
                case MultimediaTypes.PICTURE:
                    // 图片
                    List<String> path = MultiMediaSetting.obtainPathResult(data);
                    List<MultiMedia> multiMedias = mplImageList.addImages(path, null);

                    // 修改数据前，其他线程全部暂停
                    for (MyTask timer : timers) {
                        timer.cancel();
                    }
                    for (int position = 0; position < multiMedias.size(); position++) {
                        MyTask timer = new MyTask(multiMedias.get(position).getPosition(), false);
                        timers.add(timer);
                    }
                    // 重新开启
                    for (MyTask timer : timers) {
                        timer.schedule();
                    }
                    break;
                case MultimediaTypes.VIDEO:
                    // 录像
                    List<String> videoPath = MultiMediaSetting.obtainPathResult(data);
                    MultiMedia multiMedia = mplImageList.setVideo(videoPath);
                    MyTask timer = new MyTask(multiMedia.getPosition(), true);
                    timers.add(timer);
                    timer.schedule();
                    break;
                case MultimediaTypes.AUDIO:
                    // 语音
                    break;
                case MultimediaTypes.BLEND:
                    // 混合类型，意思是图片可能跟录像在一起.
                    mplImageList.addImages(MultiMediaSetting.obtainPathResult(data), null);
                    break;
            }

        }
        if (resultCode == 101) {
            Log.i("CJT", "picture");
            String path = data.getStringExtra("path");
            photo.setImageBitmap(BitmapFactory.decodeFile(path));
        }
        if (resultCode == 102) {
            Log.i("CJT", "video");
            String path = data.getStringExtra("path");
        }
        if (resultCode == 103) {
            Toast.makeText(this, "请检查相机权限~", Toast.LENGTH_SHORT).show();
        }
    }

    class MyTask extends Timer {

        public int position = 0;//需求
        public int percentage = 0;// 百分比
        public boolean isVideo = false;// 是否视频

        public MyTask(int position, boolean isVideo) {
            this.position = position;
            this.isVideo = isVideo;
        }

        public void schedule() {
            this.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        percentage++;
                        ImageAdapter.ViewHolder viewHolder;
                        if (isVideo)
                            viewHolder = mplImageList.setVideoProgress(position, percentage);
                        else
                            viewHolder = mplImageList.setImageProgress(position, percentage);
                        if (viewHolder == null || percentage == 100) {
                            cancel();
                        }
                    });
                }
            }, 1000, 100);
        }

    }

    /**
     * do some action
     */
    static class DoActionHandler extends Handler {
        private final WeakReference<Activity> mActivity;

        DoActionHandler(Activity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = (MainActivity) mActivity.get();
            activity.mplImageList.setImageProgress(msg.what, 1);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Timer timer : timers) {
            timer.cancel();
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
                    startActivityForResult(new Intent(MainActivity.this, com.zhongjh.cameraviewsoundrecorder.MainActivity.class), 100);
                } else {
                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * @param alreadyImageCount 已经存在显示的几张图片
     * @param alreadyVideoCount 已经存在显示的几个视频
     *                          打开窗体
     */
    private void openMain(int alreadyImageCount, int alreadyVideoCount) {

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

        // 全局
        MultiMediaSetting.from(MainActivity.this)
                .choose(MimeType.ofImage())
                .albumSetting(albumSetting)
                .cameraSetting(cameraSetting)
                .captureStrategy(
                        new CaptureStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/test"))// 设置路径和7.0保护路径等等
                //                                            .imageEngine(new GlideEngine())  // for glide-V3
                .imageEngine(new Glide4Engine())    // for glide-V4
                .maxSelectable(10 - (alreadyImageCount + alreadyVideoCount))// 全部最多选择几个
                .maxSelectablePerMediaType(10 - alreadyImageCount, 1 - alreadyVideoCount)// 最大10张图片或者最大1个视频
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .forResult(REQUEST_CODE_CHOOSE);
    }

}
