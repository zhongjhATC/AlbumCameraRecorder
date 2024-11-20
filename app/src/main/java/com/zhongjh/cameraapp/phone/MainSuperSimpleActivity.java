package com.zhongjh.cameraapp.phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;
import com.zhongjh.cameraapp.configuration.Glide4Engine;
import com.zhongjh.cameraapp.databinding.ActivityMainSuperSimpleBinding;
import com.zhongjh.combined.Combined;
import com.zhongjh.common.entity.SaveStrategy;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.displaymedia.apapter.AudioAdapter;
import com.zhongjh.displaymedia.apapter.ImagesAndVideoAdapter;
import com.zhongjh.displaymedia.entity.DisplayMedia;
import com.zhongjh.displaymedia.listener.AbstractDisplayMediaLayoutListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 这是一个超级简单代码就完成的示例
 * 主要优化了onActivityResult、setMaskProgressLayoutListener方面的代码
 *
 * @author zhongjh
 * @date 2021/9/6
 */
public class MainSuperSimpleActivity extends AppCompatActivity {

    protected static final int REQUEST_CODE_CHOOSE = 236;
    private final static int PROGRESS_MAX = 100;

    ActivityMainSuperSimpleBinding mBinding;
    GlobalSetting mGlobalSetting;
    AlbumSetting mAlbumSetting;
    Combined mCombined;

    /**
     * 模拟上传进度
     */
    protected HashMap<DisplayMedia, MyTask> timers = new HashMap<>();

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainSuperSimpleActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainSuperSimpleBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();
    }

    @Override
    protected void onDestroy() {
        // 停止所有的上传
        for (Map.Entry<DisplayMedia, MyTask> entry : timers.entrySet()) {
            entry.getValue().cancel();
        }
        mBinding.dmlImageList.onDestroy();

        if (mGlobalSetting != null) {
            mGlobalSetting.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        mCombined.onActivityResult(requestCode, data);
    }


    /**
     * 初始化
     */
    private void init() {
        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        // 支持的类型：图片，视频
        cameraSetting.mimeTypeSet(MimeType.ofAll());

        // 相册
        mAlbumSetting = new AlbumSetting(false);

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();

        // 全局
        mGlobalSetting = MultiMediaSetting.from(MainSuperSimpleActivity.this).choose(MimeType.ofAll());

        // 开启相册功能
        mGlobalSetting.albumSetting(mAlbumSetting);
        // 开启拍摄功能
        mGlobalSetting.cameraSetting(cameraSetting);
        // 开启录音功能
        mGlobalSetting.recorderSetting(recorderSetting);

        mGlobalSetting
                // 设置路径和7.0保护路径等等
                .allStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "aabb"))
                // for glide-V4
                .imageEngine(new Glide4Engine())
                // 最大5张图片、最大3个视频、最大1个音频
                .maxSelectablePerMediaType(null,
                        6,
                        3,
                        3,
                        0,
                        0,
                        0);

        // 这里是将AlbumCameraRecorder和Mask控件合并，需要放在初始化最后，alreadyImageCount才能以最新生效
        mCombined = new Combined(MainSuperSimpleActivity.this, REQUEST_CODE_CHOOSE,
                mGlobalSetting, mBinding.dmlImageList, new AbstractDisplayMediaLayoutListener() {

            @Override
            public void onItemStartUploading(@NonNull DisplayMedia displayMedia, @NonNull ImagesAndVideoAdapter.PhotoViewHolder viewHolder) {
                super.onItemStartUploading(displayMedia, viewHolder);
                // 开始模拟上传 - 指刚添加后的。这里可以使用你自己的上传事件
                MyTask timer = new MyTask(displayMedia, viewHolder, null);
                timers.put(displayMedia, timer);
                timer.schedule();
            }

            @Override
            public void onItemClose(@NotNull DisplayMedia displayMedia) {
                super.onItemClose(displayMedia);
                // 停止上传
                MyTask myTask = timers.get(displayMedia);
                if (myTask != null) {
                    myTask.cancel();
                    timers.remove(displayMedia);
                }
            }
        });
    }

    protected class MyTask extends Timer {

        // 百分比
        int percentage = 0;
        DisplayMedia multiMedia;
        ImagesAndVideoAdapter.PhotoViewHolder viewHolder;
        AudioAdapter.AudioHolder audioHolder;

        public MyTask(DisplayMedia multiMedia, ImagesAndVideoAdapter.PhotoViewHolder viewHolder, AudioAdapter.AudioHolder audioHolder) {
            this.multiMedia = multiMedia;
            this.viewHolder = viewHolder;
            this.audioHolder = audioHolder;
        }

        public void schedule() {
            this.schedule(new TimerTask() {
                @Override
                public void run() {
                    percentage++;
                    mBinding.dmlImageList.setPercentage(multiMedia, percentage);
                    if (percentage == PROGRESS_MAX) {
                        this.cancel();
                    }
                    // 真实场景的应用设置完成赋值url的时候可以这样写如下代码：
//                        // 赋值完成
//                        multiMedia.setUrl(url);
//                        multiMedia.setPercentage(100);
                }
            }, 1000, 100);
        }
    }

}
