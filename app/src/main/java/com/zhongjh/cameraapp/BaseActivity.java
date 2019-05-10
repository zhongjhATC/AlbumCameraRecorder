package com.zhongjh.cameraapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.recorder.db.RecordingItem;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import gaode.zhongjh.com.common.entity.MultiMedia;
import gaode.zhongjh.com.common.enums.MultimediaTypes;

import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.REQUEST_CODE_PREVIEW;

/**
 * 父类，包含下面几部分操作：
 * 1.权限控制
 * 2.打开多媒体操作
 * 3.多媒体返回数据有关操作
 * Created by zhongjh on 2019/5/10.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected static final int REQUEST_CODE_CHOOSE = 236;

    protected final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码
    protected HashMap<MultiMediaView, MyTask> timers = new HashMap<>();

    protected abstract MaskProgressLayout getMaskProgressLayout();

    /**
     * 公共的打开多媒体事件
     * @param alreadyImageCount 已经存在的图片
     * @param alreadyVideoCount 已经存在的语音
     * @param alreadyAudioCount 已经存在的视频
     */
    protected abstract void openMain(int alreadyImageCount,int alreadyVideoCount,int alreadyAudioCount);

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
                    startActivityForResult(new Intent(BaseActivity.this, com.zhongjh.albumcamerarecorder.MainActivity.class), 100);
                } else {
                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                }

                // 看下see为什么这么弄
//                if (size == 0) {
//                    Toast.makeText(this, "你可以重新打开相关功能", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
//                }

            }
        }
    }

    /**
     * 获取权限
     */
    protected boolean getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager
                    .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager
                            .PERMISSION_GRANTED) {
                return true;
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(BaseActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA}, GET_PERMISSION_REQUEST);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case REQUEST_CODE_PREVIEW:
                // 如果在预览界面点击了确定
                if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                    // 请求的预览界面
                    Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
                    // 获取选择的数据
                    ArrayList<MultiMedia> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
                    if (selected == null)
                        return;
                    // 循环判断，如果不存在，则删除
                    for (int i = getMaskProgressLayout().getImages().size() - 1; i >= 0; i--) {
                        int k = 0;
                        for (MultiMedia multiMedia : selected) {
                            if (!getMaskProgressLayout().getImages().get(i).equals(multiMedia)) {
                                k++;
                            }
                        }
                        if (k == selected.size()) {
                            // 所有都不符合，则删除
                            getMaskProgressLayout().onRemoveItemImage(i);
                        }
                    }
                }
                break;
            case REQUEST_CODE_CHOOSE:
                // 获取类型，根据类型设置不同的事情
                switch (MultiMediaSetting.obtainMultimediaType(data)) {
                    case MultimediaTypes.PICTURE:
                        // 图片
                        List<String> path = MultiMediaSetting.obtainPathResult(data);
                        getMaskProgressLayout().addImagesStartUpload(path);
                        break;
                    case MultimediaTypes.VIDEO:
                        // 录像
                        List<String> videoPath = MultiMediaSetting.obtainPathResult(data);
                        getMaskProgressLayout().addVideoStartUpload(videoPath);
                        break;
                    case MultimediaTypes.AUDIO:
                        // 语音
                        RecordingItem recordingItem = MultiMediaSetting.obtainRecordingItemResult(data);
                        getMaskProgressLayout().addAudioStartUpload(recordingItem.getFilePath(), recordingItem.getLength());
                        break;
                    case MultimediaTypes.BLEND:
                        // 混合类型，意思是图片可能跟录像在一起.
                        getMaskProgressLayout().addImagesStartUpload(MultiMediaSetting.obtainPathResult(data));
                        break;
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        // 停止所有的上传
        for (Map.Entry<MultiMediaView, MyTask> entry : timers.entrySet()) {
            entry.getValue().cancel();
        }
        getMaskProgressLayout().destroy();
        super.onDestroy();
    }

    /**
     * dp转px
     */
    public int dip2px(int dp) {
        float density = this.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5);
    }

    class MyTask extends Timer {

        int percentage = 0;// 百分比
        MultiMediaView multiMedia;

        MyTask(MultiMediaView multiMedia) {
            this.multiMedia = multiMedia;
        }

        void schedule() {
            this.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        percentage++;
                        multiMedia.setPercentage(percentage);
                        // 现实应用设置完成赋值url的时候可以这样写如下代码：
//                        // 赋值完成
//                        multiMedia.setUrl(url);
//                        multiMedia.setPercentage(100);
                    });
                }
            }, 1000, 100);
        }

    }

}
