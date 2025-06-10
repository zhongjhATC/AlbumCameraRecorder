package com.zhongjh.demo.phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.zhongjh.multimedia.album.filter.BaseFilter;
import com.zhongjh.multimedia.settings.AlbumSetting;
import com.zhongjh.multimedia.settings.CameraSetting;
import com.zhongjh.multimedia.settings.GlobalSetting;
import com.zhongjh.multimedia.settings.MultiMediaSetting;
import com.zhongjh.multimedia.settings.RecorderSetting;
import com.zhongjh.demo.BaseActivity;
import com.zhongjh.demo.configuration.GifSizeFilter;
import com.zhongjh.demo.configuration.Glide4Engine;
import com.zhongjh.demo.databinding.ActivityMainSimpleBinding;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.gridview.apapter.GridAdapter;
import com.zhongjh.common.entity.GridMedia;
import com.zhongjh.gridview.listener.GridViewListener;
import com.zhongjh.gridview.widget.GridView;

import org.jetbrains.annotations.NotNull;

/**
 * 简单版
 *
 * @author zhongjh
 */
public class MainSimpleActivity extends BaseActivity {

    ActivityMainSimpleBinding mBinding;
    private final String TAG = MainSimpleActivity.this.getClass().getSimpleName();
    private final Integer MAX_SELECTABLE = null;
    private final Integer MAX_IMAGE_SELECTABLE = 5;
    private final Integer MAX_VIDEO_SELECTABLE = 3;
    private final Integer MAX_AUDIO_SELECTABLE = 5;

    GlobalSetting mGlobalSetting;

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainSimpleActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainSimpleBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.gridView.setMaxMediaCount(MAX_SELECTABLE, MAX_IMAGE_SELECTABLE, MAX_VIDEO_SELECTABLE, MAX_AUDIO_SELECTABLE);

        // 以下为点击事件
        mBinding.gridView.setGridViewListener(new GridViewListener() {

            @Override
            public void onItemAdd(@NotNull View view, @NotNull GridMedia gridMedia, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击添加
                openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            }

            @Override
            public void onItemClick(@NotNull View view, @NotNull GridMedia gridMedia) {
                // 点击详情
                mGlobalSetting.openPreviewData(MainSimpleActivity.this, requestLauncherGrid, mBinding.gridView.getAllData(), mBinding.gridView.getAllData().indexOf(gridMedia));
            }

            @Override
            public void onItemStartUploading(@NonNull GridMedia gridMedia, @NonNull GridAdapter.PhotoViewHolder viewHolder) {
                // 开始模拟上传 - 指刚添加后的。这里可以使用你自己的上传事件
                MyTask timer = new MyTask(gridMedia);
                timers.put(gridMedia, timer);
                timer.schedule();
            }

            @Override
            public void onItemClose(@NotNull GridMedia gridMedia) {
                // 停止上传
                MyTask myTask = timers.get(gridMedia);
                if (myTask != null) {
                    myTask.cancel();
                    timers.remove(gridMedia);
                }
            }

            @Override
            public boolean onItemStartDownload(@NotNull View view, @NotNull GridMedia gridMedia, int position) {
                return false;
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGlobalSetting != null) {
            mGlobalSetting.onDestroy();
        }
    }

    @Override
    protected GridView getMaskProgressLayout() {
        return mBinding.gridView;
    }

    @Override
    protected void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        // 支持的类型：图片，视频
        cameraSetting.mimeTypeSet(MimeType.ofAll());

        // 相册
        AlbumSetting albumSetting = new AlbumSetting(false)
                // 支持的类型：图片，视频
                .mimeTypeSet(MimeType.ofAll())
                // 是否显示多选图片的数字
                .countable(true)
                // 自定义过滤器
                .addFilter(new GifSizeFilter(320, 320, 5 * BaseFilter.K * BaseFilter.K))
                // 开启原图
                .originalEnable(true)
                // 最大原图size,仅当originalEnable为true的时候才有效
                .maxOriginalSize(10);

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();

        // 全局
        mGlobalSetting = MultiMediaSetting.from(MainSimpleActivity.this).choose(MimeType.ofAll());

        if (mBinding.cbAlbum.isChecked()) {
            // 开启相册功能
            mGlobalSetting.albumSetting(albumSetting);
        }
        if (mBinding.cbCamera.isChecked()) {
            // 开启拍摄功能
            mGlobalSetting.cameraSetting(cameraSetting);
        }
        if (mBinding.cbRecorder.isChecked()) {
            // 开启录音功能
            mGlobalSetting.recorderSetting(recorderSetting);
        }

        mGlobalSetting
                // for glide-V4
                .imageEngine(new Glide4Engine())
                // 最大5张图片、最大3个视频、最大1个音频。如果需要使用九宫格，请把九宫格MaskProgressLayout的maxCount也改动 mBinding.dmlImageList.setMaxMediaCount();
                .maxSelectablePerMediaType(MAX_SELECTABLE, MAX_IMAGE_SELECTABLE, MAX_VIDEO_SELECTABLE, MAX_AUDIO_SELECTABLE,
                        alreadyImageCount,
                        alreadyVideoCount,
                        alreadyAudioCount)
                .forResult(requestLauncherACR);
    }

}
