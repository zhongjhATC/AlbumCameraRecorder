package com.zhongjh.cameraapp.phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;
import com.zhongjh.cameraapp.R;
import com.zhongjh.cameraapp.configuration.Glide4Engine;
import com.zhongjh.cameraapp.databinding.ActivityMainSuperSimpleBinding;
import com.zhongjh.combined.Combined;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.AbstractMaskProgressLayoutListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.zhongjh.common.entity.SaveStrategy;
import com.zhongjh.common.enums.MimeType;

/**
 * 这是一个超级简单代码就完成的示例
 * 主要优化了onActivityResult、setMaskProgressLayoutListener方面的代码
 *
 * @author zhongjh
 * @date 2021/9/6
 */
public class MainSuperSimpleActivity extends AppCompatActivity {

    protected static final int REQUEST_CODE_CHOOSE = 236;

    ActivityMainSuperSimpleBinding mBinding;
    GlobalSetting mGlobalSetting;
    Combined mCombined;

    /**
     * 模拟上传进度
     */
    protected HashMap<MultiMediaView, MyTask> timers = new HashMap<>();

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainSuperSimpleActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_super_simple);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_super_simple);
        init();
    }

    @Override
    protected void onDestroy() {
        // 停止所有的上传
        for (Map.Entry<MultiMediaView, MyTask> entry : timers.entrySet()) {
            entry.getValue().cancel();
        }
        mBinding.mplImageList.onDestroy();

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
        AlbumSetting albumSetting = new AlbumSetting(false);

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();

        // 全局
        mGlobalSetting = MultiMediaSetting.from(MainSuperSimpleActivity.this).choose(MimeType.ofAll());

        // 开启相册功能
        mGlobalSetting.albumSetting(albumSetting);
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
                mGlobalSetting, mBinding.mplImageList, new AbstractMaskProgressLayoutListener() {
            @Override
            public void onItemStartUploading(MultiMediaView multiMediaView) {
                super.onItemStartUploading(multiMediaView);
                Log.d("onItemStartUploading","onItemStartUploading");
                // 开始模拟上传 - 指刚添加后的。这里可以使用你自己的上传事件
                MyTask timer = new MyTask(multiMediaView);
                timers.put(multiMediaView, timer);
                timer.schedule();
            }

            @Override
            public void onItemClose(View view, MultiMediaView multiMediaView) {
                super.onItemClose(view, multiMediaView);
                // 停止上传
                MyTask myTask = timers.get(multiMediaView);
                if (myTask != null) {
                    Log.d("onItemClose","取消");
                    myTask.cancel();
                    timers.remove(multiMediaView);
                }
            }
        });
    }

    protected class MyTask extends Timer {

        // 百分比
        int percentage = 0;
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
                        Log.d("MyTask",multiMedia.getUri().toString() + "进度： " + percentage);
                        if (percentage == 100) {
                            this.cancel();
                        }
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
