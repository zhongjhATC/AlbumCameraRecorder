package com.zhongjh.demo.phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.zhongjh.multimedia.settings.AlbumSetting;
import com.zhongjh.multimedia.settings.CameraSetting;
import com.zhongjh.multimedia.settings.GlobalSetting;
import com.zhongjh.multimedia.settings.MultiMediaSetting;
import com.zhongjh.multimedia.settings.RecorderSetting;
import com.zhongjh.demo.configuration.Glide4Engine;
import com.zhongjh.demo.databinding.ActivityMainSuperSimpleBinding;
import com.zhongjh.combined.Combined;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.gridview.apapter.GridAdapter;
import com.zhongjh.common.entity.GridMedia;
import com.zhongjh.gridview.listener.AbstractGridViewListener;

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

    private final static int PROGRESS_MAX = 100;

    ActivityMainSuperSimpleBinding mBinding;
    GlobalSetting mGlobalSetting;
    AlbumSetting mAlbumSetting;
    Combined mCombined;

    /**
     * 模拟上传进度
     */
    protected final HashMap<GridMedia, MyTask> timers = new HashMap<>();

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
        for (Map.Entry<GridMedia, MyTask> entry : timers.entrySet()) {
            entry.getValue().cancel();
        }
        mBinding.gridView.onDestroy();

        if (mGlobalSetting != null) {
            mGlobalSetting.onDestroy();
        }
        super.onDestroy();
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
        mCombined = new Combined(MainSuperSimpleActivity.this,
                mGlobalSetting, mBinding.gridView, new AbstractGridViewListener() {

            @Override
            public void onItemStartUploading(@NonNull GridMedia gridMedia, @NonNull GridAdapter.PhotoViewHolder viewHolder) {
                super.onItemStartUploading(gridMedia, viewHolder);
                // 开始模拟上传 - 指刚添加后的。这里可以使用你自己的上传事件
                MyTask timer = new MyTask(gridMedia, viewHolder);
                timers.put(gridMedia, timer);
                timer.schedule();
            }

            @Override
            public void onItemClose(@NotNull GridMedia gridMedia) {
                super.onItemClose(gridMedia);
                // 停止上传
                MyTask myTask = timers.get(gridMedia);
                if (myTask != null) {
                    myTask.cancel();
                    timers.remove(gridMedia);
                }
            }
        });
    }

    protected class MyTask extends Timer {

        // 百分比
        int percentage = 0;
        final GridMedia multiMedia;
        final GridAdapter.PhotoViewHolder viewHolder;

        public MyTask(GridMedia multiMedia, GridAdapter.PhotoViewHolder viewHolder) {
            this.multiMedia = multiMedia;
            this.viewHolder = viewHolder;
        }

        public void schedule() {
            this.schedule(new TimerTask() {
                @Override
                public void run() {
                    percentage++;
                    mBinding.gridView.setPercentage(multiMedia, percentage);
                    // 真实场景的应用设置完成赋值url的时候可以这样写如下代码：multiMedia.setUrl(url);multiMedia.setPercentage(100);
                    if (percentage == PROGRESS_MAX) {
                        this.cancel();
                    }

                }
            }, 1000, 100);
        }
    }

}
