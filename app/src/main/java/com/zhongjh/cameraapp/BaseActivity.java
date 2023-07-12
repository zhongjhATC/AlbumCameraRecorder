package com.zhongjh.cameraapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MediaExtraInfo;
import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.utils.MediaUtils;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 父类，包含下面几部分操作：
 * 1.权限控制
 * 2.打开多媒体操作
 * 3.多媒体返回数据有关操作\
 *
 * @author zhongjh
 * @date 2019/5/10
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    protected static final int REQUEST_CODE_CHOOSE = 236;
    private final static int PROGRESS_MAX = 100;

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
                        Toast.makeText(getApplicationContext(), getString(R.string.you_can_turn_it_back_on), Toast.LENGTH_SHORT).show();
                    } else {
                        openMain(0, 0, 0);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_open_it_in_settings_permission_management), Toast.LENGTH_SHORT).show();
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
            // 存储功能必须验证,兼容Android SDK 33
            ArrayList<String> permissions = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
            if (permissions.size() > 0) {
                // 请求权限
                ActivityCompat.requestPermissions(BaseActivity.this, permissions.toArray(new String[0]), GET_PERMISSION_REQUEST);
                return false;
            } else {
                // 没有所需要请求的权限，就进行初始化
                return true;
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
                // 获取选择的数据
                ArrayList<MultiMedia> selected = MultiMediaSetting.obtainMultiMediaResult(data);
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
            } else {
                List<LocalFile> result = MultiMediaSetting.obtainLocalFileResult(data);
                for (LocalFile localFile : result) {
                    // 绝对路径,AndroidQ如果存在不属于自己App下面的文件夹则无效
                    Log.i(TAG, "onResult id:" + localFile.getId());
                    Log.i(TAG, "onResult 绝对路径:" + localFile.getPath());
                    Log.d(TAG, "onResult 旧图路径:" + localFile.getOldPath());
                    Log.d(TAG, "onResult 原图路径:" + localFile.getOriginalPath());
                    Log.i(TAG, "onResult Uri:" + localFile.getUri());
                    Log.d(TAG, "onResult 旧图Uri:" + localFile.getOldUri());
                    Log.d(TAG, "onResult 原图Uri:" + localFile.getOriginalUri());
                    Log.i(TAG, "onResult 文件大小: " + localFile.getSize());
                    Log.i(TAG, "onResult 视频音频长度: " + localFile.getDuration());
                    Log.i(TAG, "onResult 是否选择了原图: " + localFile.isOriginal());
                    if (localFile.isImageOrGif()) {
                        if (localFile.isImage()) {
                            Log.d(TAG, "onResult 图片类型");
                        } else if (localFile.isImage()) {
                            Log.d(TAG, "onResult 图片类型");
                        }
                    } else if (localFile.isVideo()) {
                        Log.d(TAG, "onResult 视频类型");
                    } else if (localFile.isAudio()) {
                        Log.d(TAG, "onResult 音频类型");
                    }
                    Log.i(TAG, "onResult 具体类型:" + localFile.getMimeType());
                    // 某些手机拍摄没有自带宽高，那么我们可以自己获取
                    if (localFile.getWidth() == 0 && localFile.isVideo()) {
                        MediaExtraInfo mediaExtraInfo = MediaUtils.getVideoSize(getApplication(), localFile.getPath());
                        localFile.setWidth(mediaExtraInfo.getWidth());
                        localFile.setHeight(mediaExtraInfo.getHeight());
                        localFile.setDuration(mediaExtraInfo.getDuration());
                    }
                    Log.i(TAG, "onResult 宽高: " + localFile.getWidth() + "x" + localFile.getHeight());
                }
                getMaskProgressLayout().addLocalFileStartUpload(result);
            }
        }
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
                        if (percentage == PROGRESS_MAX) {
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
