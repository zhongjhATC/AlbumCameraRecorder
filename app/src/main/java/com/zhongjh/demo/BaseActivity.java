package com.zhongjh.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zhongjh.multimedia.settings.MultiMediaSetting;
import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.common.entity.MediaExtraInfo;
import com.zhongjh.common.utils.MediaUtils;
import com.zhongjh.common.entity.GridMedia;
import com.zhongjh.gridview.widget.GridView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
     * AlbumCameraRecorder的回调
     */
    protected final ActivityResultLauncher<Intent> requestLauncherACR = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) {
            return;
        }
        if (null == result.getData()) {
            return;
        }

        List<LocalMedia> data = MultiMediaSetting.obtainLocalMediaResult(result.getData());
        printProperty(data);
        getMaskProgressLayout().addLocalFileStartUpload(data);
    });
    /**
     * 九宫格的回调
     */
    protected final ActivityResultLauncher<Intent> requestLauncherGrid = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) {
            return;
        }
        if (null == result.getData()) {
            return;
        }
        // 获取选择的数据
        ArrayList<LocalMedia> selected = MultiMediaSetting.obtainLocalMediaResult(result.getData());
        // 循环判断，如果不存在，则删除
        for (int i = getMaskProgressLayout().getAllData().size() - 1; i >= 0; i--) {
            int k = 0;
            for (LocalMedia multiMedia : selected) {
                if (!getMaskProgressLayout().getAllData().get(i).equalsLocalMedia(multiMedia)) {
                    k++;
                }
            }
            if (k == selected.size()) {
                // 所有都不符合，则删除
                getMaskProgressLayout().removePosition(i);
            }
        }
    });

    protected final HashMap<GridMedia, MyTask> timers = new HashMap<>();

    /**
     * 返回九宫格
     *
     * @return MaskProgressLayout
     */
    protected abstract GridView getMaskProgressLayout();

    /**
     * 是否浏览
     */
    protected boolean isBrowse = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 公共的打开多媒体事件
     *
     * @param alreadyImageCount 已经存在的图片
     * @param alreadyVideoCount 已经存在的语音
     * @param alreadyAudioCount 已经存在的视频
     */
    protected abstract void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount);

    @Override
    protected void onDestroy() {
        // 停止所有的上传
        for (Map.Entry<GridMedia, MyTask> entry : timers.entrySet()) {
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

    /**
     * 公共的打印属性
     *
     * @param result 数据源
     */
    protected void printProperty(List<? extends LocalMedia> result) {
        for (LocalMedia localMedia : result) {
            // 绝对路径,AndroidQ如果存在不属于自己App下面的文件夹则无效
            Log.i(TAG, "onResult id:" + localMedia.getFileId());
            // 是必定可用的地址，如果对地址没有太苛刻的时候可以使用它，具体逻辑可以看该方法(比如支持压缩的话，该方法返回压缩路径)。
            Log.d(TAG, "onResult getAvailablePath:" + localMedia.getAvailablePath());
            // 压缩后的路径，如果开启压缩配置后，将最终原图或者编辑后的图片进行压缩，然后赋值该属性。
            Log.d(TAG, "onResult getCompressPath:" + localMedia.getCompressPath());
            // 如果该图片裁剪或者编辑过，那么该属性会有值。
            Log.d(TAG, "onResult getEditorPath:" + localMedia.getEditorPath());
            // 沙盒路径，是配合 FileProvider 后形成的路径，未压缩、未编辑前的，即是原图
            Log.d(TAG, "onResult getSandboxPath:" + localMedia.getSandboxPath());
            // 初始的uri路径，未压缩、未编辑前的，即是原图
            Log.d(TAG, "onResult getPath:" + localMedia.getPath());
            // 初始的真实路径，未压缩、未编辑前的，即是原图
            Log.d(TAG, "onResult getAbsolutePath:" + localMedia.getAbsolutePath());
            // 如果有不存在的文件,抛出错误
            if (null == localMedia.getCompressPath() || !new File(localMedia.getCompressPath()).exists()) {
                Toast.makeText(getApplicationContext(), "CompressPath不存在", Toast.LENGTH_SHORT).show();
                Log.e(TAG, localMedia.getCompressPath() + " CompressPath不存在");
            }
            if (null == localMedia.getEditorPath() || !new File(localMedia.getEditorPath()).exists()) {
                Toast.makeText(getApplicationContext(), "EditorPath不存在", Toast.LENGTH_SHORT).show();
                Log.e(TAG, localMedia.getEditorPath() + " EditorPath不存在");
            }
            if (null == localMedia.getSandboxPath() || isNoUri(localMedia.getSandboxPath())) {
                Toast.makeText(getApplicationContext(), "SandboxPath不存在", Toast.LENGTH_SHORT).show();
                Log.e(TAG, localMedia.getSandboxPath() + " SandboxPath不存在");
            }
            if (isNoUri(localMedia.getPath())) {
                Toast.makeText(getApplicationContext(), "Path不存在", Toast.LENGTH_SHORT).show();
                Log.e(TAG, localMedia.getPath() + " Path不存在");
            }
            if (!new File(localMedia.getAbsolutePath()).exists()) {
                Toast.makeText(getApplicationContext(), "AbsolutePath不存在", Toast.LENGTH_SHORT).show();
                Log.e(TAG, localMedia.getAbsolutePath() + " AbsolutePath不存在");
            }


            Log.i(TAG, "onResult 视频音频长度: " + localMedia.getDuration());
            Log.i(TAG, "onResult 角度: " + localMedia.getOrientation());
            Log.i(TAG, "onResult 是否选中: " + localMedia.isChecked());
            Log.i(TAG, "onResult 是否裁剪: " + localMedia.isCut());
            Log.i(TAG, "onResult 索引: " + localMedia.getPosition());
            Log.i(TAG, "onResult 媒体资源类型: " + localMedia.getMimeType());
            Log.i(TAG, "onResult 宽度: " + localMedia.getWidth());
            Log.i(TAG, "onResult 高度: " + localMedia.getHeight());
            Log.i(TAG, "onResult 裁剪图片的宽度: " + localMedia.getCropImageWidth());
            Log.i(TAG, "onResult 裁剪图片的高度: " + localMedia.getCropImageHeight());
            Log.i(TAG, "onResult 裁剪比例X: " + localMedia.getCropOffsetX());
            Log.i(TAG, "onResult 裁剪比例Y: " + localMedia.getCropOffsetY());
            Log.i(TAG, "onResult 裁剪纵横比: " + localMedia.getCropResultAspectRatio());
            Log.i(TAG, "onResult 文件大小: " + localMedia.getSize());
            Log.i(TAG, "onResult 文件名称: " + localMedia.getFileName());
            Log.i(TAG, "onResult 父文件夹名称: " + localMedia.getParentFolderName());
            Log.i(TAG, "onResult 专辑ID: " + localMedia.getBucketId());
            Log.i(TAG, "onResult 相册ID: " + localMedia.getFileId());
            Log.i(TAG, "onResult 文件创建时间: " + localMedia.getDateAddedTime());
            Log.i(TAG, "onResult 是否选择了原图: " + localMedia.isOriginal());
            if (localMedia.isImageOrGif()) {
                if (localMedia.isImage()) {
                    Log.d(TAG, "onResult 图片类型");
                } else if (localMedia.isImage()) {
                    Log.d(TAG, "onResult 图片类型");
                }
            } else if (localMedia.isVideo()) {
                Log.d(TAG, "onResult 视频类型");
            } else if (localMedia.isAudio()) {
                Log.d(TAG, "onResult 音频类型");
            }
            Log.i(TAG, "onResult 具体类型:" + localMedia.getMimeType());
            // 某些手机拍摄没有自带宽高，那么我们可以自己获取
            if (localMedia.getWidth() == 0 && localMedia.isVideo()) {
                MediaExtraInfo mediaExtraInfo = MediaUtils.getVideoSize(getApplication(), localMedia.getPath());
                localMedia.setWidth(mediaExtraInfo.getWidth());
                localMedia.setHeight(mediaExtraInfo.getHeight());
                localMedia.setDuration(mediaExtraInfo.getDuration());
            }
            Log.i(TAG, "onResult 宽高: " + localMedia.getWidth() + "x" + localMedia.getHeight());


            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "";
                }
            });
        }
    }

    protected class MyTask extends Timer {

        // 百分比
        int percentage = 0;
        final GridMedia gridMedia;

        public MyTask(GridMedia gridMedia) {
            this.gridMedia = gridMedia;
        }

        public void schedule() {
            this.schedule(new TimerTask() {
                @Override
                public void run() {
                    percentage++;
                    getMaskProgressLayout().setPercentage(gridMedia, percentage);
                    // 真实场景的应用设置完成赋值url的时候可以这样写如下代码：multiMedia.setUrl(url);multiMedia.setPercentage(100);
                    if (percentage == PROGRESS_MAX) {
                        this.cancel();
                    }
                }
            }, 1000, 100);
        }
    }

    private boolean isNoUri(String uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(Uri.parse(uri));
            if (inputStream != null) {
                // 文件存在
                Log.d(TAG, "文件存在");
                inputStream.close();
                return false;
            } else {
                // 文件不存在
                Log.d(TAG, "文件不存在");
                return true;
            }
        } catch (FileNotFoundException e) {
            // 文件不存在
            Log.d(TAG, "文件不存在");
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
