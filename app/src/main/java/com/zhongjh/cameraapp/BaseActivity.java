package com.zhongjh.cameraapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

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

import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.enums.MultimediaTypes;

/**
 * 父类，包含下面几部分操作：
 * 1.权限控制
 * 2.打开多媒体操作
 * 3.多媒体返回数据有关操作
 *
 * @author zhongjh
 * @date 2019/5/10
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected static final int REQUEST_CODE_CHOOSE = 236;

    /**
     * 权限申请自定义码
     */
    protected final int GET_PERMISSION_REQUEST = 100;
    protected HashMap<MultiMediaView, MyTask> timers = new HashMap<>();

    /**
     * 返回九宫格
     *
     * @return MaskProgressLayout
     */
    protected abstract MaskProgressLayout getMaskProgressLayout();

    /**
     * 是否浏览
     */
    protected boolean isBrowse = false;

    /**
     * 公共的打开多媒体事件
     *
     * @param alreadyImageCount 已经存在的图片
     * @param alreadyVideoCount 已经存在的语音
     * @param alreadyAudioCount 已经存在的视频
     */
    protected abstract void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount);

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_REQUEST) {
            int size = 0;
            if (grantResults.length >= 1) {
                int writeResult = grantResults[0];
                // 读写内存权限
                boolean writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;
                if (!writeGranted) {
                    size++;
                }
                // 录音权限
                int recordPermissionResult = grantResults[1];
                boolean recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!recordPermissionGranted) {
                    size++;
                }
                // 相机权限
                int cameraPermissionResult = grantResults[2];
                boolean cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }

                if (size == 0) {
                    if (isBrowse) {
                        Toast.makeText(this, getString(R.string.you_can_turn_it_back_on), Toast.LENGTH_SHORT).show();
                    } else {
                        openMain(0, 0, 0);
                    }
                } else {
                    Toast.makeText(this, getString(R.string.please_open_it_in_settings_permission_management), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 获取权限
     *
     * @param isBrowse 是否浏览
     */
    protected boolean getPermissions(boolean isBrowse) {
        this.isBrowse = isBrowse;
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
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_CHOOSE) {
            // 如果是在预览界面点击了确定
            if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                // 请求的预览界面
                Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
                // 获取选择的数据
                ArrayList<MultiMedia> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
                if (selected == null) {
                    return;
                }
                // 循环判断，如果不存在，则删除
                for (int i = getMaskProgressLayout().getImagesAndVideos().size() - 1; i >= 0; i--) {
                    int k = 0;
                    for (MultiMedia multiMedia : selected) {
                        if (!getMaskProgressLayout().getImagesAndVideos().get(i).equals(multiMedia)) {
                            k++;
                        }
                    }
                    if (k == selected.size()) {
                        // 所有都不符合，则删除
                        getMaskProgressLayout().removePosition(i);
                    }
                }
                return;
            }
            // 获取类型，根据类型设置不同的事情
            switch (MultiMediaSetting.obtainMultimediaType(data)) {
                case MultimediaTypes.PICTURE:
                    // 图片，自从AndroidQ版本以后，Path只能访问本身app的文件，所以只能用uri方式控制
                    List<Uri> path = MultiMediaSetting.obtainResult(data);
                    showToastUris(path);
                    getMaskProgressLayout().addImagesUriStartUpload(path);
                    break;
                case MultimediaTypes.VIDEO:
                    // 录像
                    List<Uri> videoUris = MultiMediaSetting.obtainResult(data);
                    showToastUris(videoUris);
                    getMaskProgressLayout().addVideoStartUpload(videoUris);
                    break;
                case MultimediaTypes.AUDIO:
                    // 语音
                    RecordingItem recordingItem = MultiMediaSetting.obtainRecordingItemResult(data);
                    Toast.makeText(getApplicationContext(), recordingItem.getFilePath(), Toast.LENGTH_LONG).show();
                    getMaskProgressLayout().addAudioStartUpload(recordingItem.getFilePath(), recordingItem.getLength());
                    break;
                case MultimediaTypes.BLEND:
                    // 混合类型，意思是图片可能跟录像在一起.
                    List<Uri> blends = MultiMediaSetting.obtainResult(data);
                    showToastUris(blends);
                    List<Uri> images = new ArrayList<>();
                    List<Uri> videos = new ArrayList<>();
                    // 循环判断类型
                    for (Uri uri : blends) {
                        DocumentFile documentFile = DocumentFile.fromSingleUri(getBaseContext(), uri);
                        if (documentFile.getType().startsWith("image")) {
                            images.add(uri);
                        } else if (documentFile.getType().startsWith("video")) {
                            videos.add(uri);
                        }
                    }
                    // 分别上传图片和视频
                    getMaskProgressLayout().addImagesUriStartUpload(images);
                    getMaskProgressLayout().addVideoStartUpload(videos);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 显示当前所有文件的地址
     */
    private void showToastUris(List<Uri> uris) {
        StringBuilder content = new StringBuilder();
        for (Uri item : uris) {
            content.append(item.toString());
            content.append("\n");
        }
        Toast.makeText(getApplicationContext(), content, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        // 停止所有的上传
        for (Map.Entry<MultiMediaView, MyTask> entry : timers.entrySet()) {
            entry.getValue().cancel();
        }
        getMaskProgressLayout().onDestroy();
        super.onDestroy();
    }

    /**
     * dp转px
     */
    public int dip2px(int dp) {
        float density = this.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5);
    }

    protected class MyTask extends Timer {

        int percentage = 0;// 百分比
        MultiMediaView multiMedia;

        public MyTask(MultiMediaView multiMedia) {
            this.multiMedia = multiMedia;
        }

        public void schedule() {
            this.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        percentage++;
                        multiMedia.setPercentage(percentage);
                        if (percentage == 100) {
                            this.cancel();
                        }
                        // 真实场景的应用设置完成赋值url的时候可以这样写如下代码：
//                        // 赋值完成
//                        multiMedia.setUrl(url);
//                        multiMedia.setPercentage(100);
                    });
                }
            }, 1000, 100);
        }

    }

}
