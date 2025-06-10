package com.zhongjh.demo.phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhongjh.multimedia.album.filter.BaseFilter;
import com.zhongjh.multimedia.settings.AlbumSetting;
import com.zhongjh.multimedia.settings.CameraSetting;
import com.zhongjh.multimedia.settings.GlobalSetting;
import com.zhongjh.multimedia.settings.MultiMediaSetting;
import com.zhongjh.multimedia.settings.RecorderSetting;
import com.zhongjh.demo.BaseActivity;
import com.zhongjh.demo.configuration.GifSizeFilter;
import com.zhongjh.demo.configuration.Glide4Engine;
import com.zhongjh.demo.databinding.ActivityMainUpperLimitBinding;
import com.zhongjh.demo.model.LimitModel;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.gridview.apapter.GridAdapter;
import com.zhongjh.common.entity.GridMedia;
import com.zhongjh.gridview.listener.GridViewListener;
import com.zhongjh.gridview.widget.GridView;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author zhongjh
 * @date 2021/7/16
 */
public class MainUpperLimitActivity extends BaseActivity {

    private final String TAG = MainUpperLimitActivity.this.getClass().getSimpleName();

    ActivityMainUpperLimitBinding mBinding;
    GlobalSetting mGlobalSetting;


    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainUpperLimitActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainUpperLimitBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.tvMessage.append("1. 这个配置十分灵活，总有一种适合你使用，可以配置 总共选择上限、图片选择上限、视频选择上限、音频选择上限。");
        mBinding.tvMessage.append("\n");
        mBinding.tvMessage.append("2. 如果图片、视频、音频不需要选择填0即可。");
        mBinding.tvMessage.append("\n");
        mBinding.tvMessage.append("3. 也支持各自没有上限，但是需要设置一个总共选择上限，最终可选择的数量以该值为准。");
        mBinding.tvMessage.append("\n");
        mBinding.tvMessage.append("4. 如果图片视频音频有各自的选择上限，那么以他们的上限为准。");
        mBinding.tvMessage.append("\n");
        mBinding.tvMessage.append("5. 如果其中一个例如图片为null，那么图片可选择无限，但是受限于总上限。");

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
                mGlobalSetting.openPreviewData(MainUpperLimitActivity.this, requestLauncherGrid, mBinding.gridView.getAllData(), mBinding.gridView.getAllData().indexOf(gridMedia));
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

        // 清空重置
        mBinding.btnReset.setOnClickListener(v -> {
            mBinding.gridView.reset();
            // 停止所有的上传
            for (Map.Entry<GridMedia, MyTask> entry : timers.entrySet()) {
                entry.getValue().cancel();
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
        mGlobalSetting = MultiMediaSetting.from(MainUpperLimitActivity.this).choose(MimeType.ofAll());
        // 开启相册功能
        mGlobalSetting.albumSetting(albumSetting);
        // 开启拍摄功能
        mGlobalSetting.cameraSetting(cameraSetting);
        // 开启录音功能
        mGlobalSetting.recorderSetting(recorderSetting);


        // 最大5张图片、最大3个视频、最大1个音频
        LimitModel limitModel = getLimitModel();
        mBinding.gridView.setMaxMediaCount(limitModel.getMaxSelectable(),
                limitModel.getMaxImageSelectable(),
                limitModel.getMaxVideoSelectable(),
                limitModel.getMaxAudioSelectable());

        mGlobalSetting
                // for glide-V4
                .imageEngine(new Glide4Engine())
                .maxSelectablePerMediaType(limitModel.getMaxSelectable(),
                        limitModel.getMaxImageSelectable(),
                        limitModel.getMaxVideoSelectable(),
                        limitModel.getMaxAudioSelectable(),
                        alreadyImageCount,
                        alreadyVideoCount,
                        alreadyAudioCount)
                .forResult(requestLauncherACR);
    }

    private LimitModel getLimitModel() {
        LimitModel limitModel = new LimitModel();
        // 根据选择类型返回相关上限设置
        if (mBinding.rbEachLimit.isChecked()) {
            limitModel.setMaxSelectable(null);
            limitModel.setMaxImageSelectable(2);
            limitModel.setMaxVideoSelectable(1);
            limitModel.setMaxAudioSelectable(1);
        } else if (mBinding.rbSumLimit.isChecked()) {
            limitModel.setMaxSelectable(20);
            limitModel.setMaxImageSelectable(null);
            limitModel.setMaxVideoSelectable(null);
            limitModel.setMaxAudioSelectable(null);
        } else if (mBinding.rbImageLimit.isChecked()) {
            limitModel.setMaxSelectable(5);
            limitModel.setMaxImageSelectable(null);
            limitModel.setMaxVideoSelectable(1);
            limitModel.setMaxAudioSelectable(1);
        } else if (mBinding.rbVideoLimit.isChecked()) {
            limitModel.setMaxSelectable(5);
            limitModel.setMaxImageSelectable(3);
            limitModel.setMaxVideoSelectable(null);
            limitModel.setMaxAudioSelectable(1);
        } else if (mBinding.rbAudioLimit.isChecked()) {
            limitModel.setMaxSelectable(5);
            limitModel.setMaxImageSelectable(3);
            limitModel.setMaxVideoSelectable(1);
            limitModel.setMaxAudioSelectable(null);
        } else if (mBinding.rbOne.isChecked()) {
            limitModel.setMaxSelectable(5);
            limitModel.setMaxImageSelectable(3);
            limitModel.setMaxVideoSelectable(null);
            limitModel.setMaxAudioSelectable(null);
        } else if (mBinding.rbTwo.isChecked()) {
            limitModel.setMaxSelectable(5);
            limitModel.setMaxImageSelectable(null);
            limitModel.setMaxVideoSelectable(2);
            limitModel.setMaxAudioSelectable(null);
        } else if (mBinding.rbThree.isChecked()) {
            limitModel.setMaxSelectable(5);
            limitModel.setMaxImageSelectable(null);
            limitModel.setMaxVideoSelectable(null);
            limitModel.setMaxAudioSelectable(2);
        }
        return limitModel;
    }

}
