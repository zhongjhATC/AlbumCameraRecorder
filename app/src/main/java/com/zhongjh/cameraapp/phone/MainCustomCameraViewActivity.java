package com.zhongjh.cameraapp.phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.databinding.DataBindingUtil;

import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.controls.Preview;
import com.zhongjh.albumcamerarecorder.album.filter.BaseFilter;
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;
import com.zhongjh.cameraapp.BaseActivity;
import com.zhongjh.cameraapp.R;
import com.zhongjh.cameraapp.configuration.GifSizeFilter;
import com.zhongjh.cameraapp.configuration.Glide4Engine;
import com.zhongjh.cameraapp.databinding.ActivityMainCustomCameraviewBinding;
import com.zhongjh.common.entity.SaveStrategy;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 自定义CameraView
 * 如无什么特殊要求，不要自定义该CameraView
 *
 * @author zhongjh
 * @date 2021/8/23
 */
public class MainCustomCameraViewActivity extends BaseActivity {

    ActivityMainCustomCameraviewBinding mBinding;
    private final String TAG = MainCustomCameraViewActivity.this.getClass().getSimpleName();

    GlobalSetting mGlobalSetting;
    CameraSetting cameraSetting;

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainCustomCameraViewActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_custom_cameraview);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_custom_cameraview);

        // 以下为点击事件
        mBinding.mplImageList.setMaskProgressLayoutListener(new MaskProgressLayoutListener() {

            @Override
            public void onAddDataSuccess(@NotNull List<MultiMediaView> multiMediaViews) {
                //                // 如果需要其他参数的话，循环数据初始化相关数值，这个读取时间会较长，建议异步线程执行
//                for (MultiMediaView item : multiMediaViews) {
//                    item.initDataByPath();
//                }
            }

            @Override
            public void onItemAdd(@NotNull View view, @NotNull MultiMediaView multiMediaView, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击添加
                boolean isOk = getPermissions(false);
                if (isOk) {
                    openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
                }
            }

            @Override
            public void onItemClick(@NotNull View view, @NotNull MultiMediaView multiMediaView) {
                // 点击详情
                if (multiMediaView.isImageOrGif() || multiMediaView.isVideo()) {
                    mGlobalSetting.openPreviewData(MainCustomCameraViewActivity.this, REQUEST_CODE_CHOOSE,
                            mBinding.mplImageList.getImagesAndVideos(),
                            mBinding.mplImageList.getImagesAndVideos().indexOf(multiMediaView));
                }
            }

            @Override
            public void onItemStartUploading(@NotNull MultiMediaView multiMediaView) {
                // 开始模拟上传 - 指刚添加后的。这里可以使用你自己的上传事件
                MyTask timer = new MyTask(multiMediaView);
                timers.put(multiMediaView, timer);
                timer.schedule();
            }

            @Override
            public void onItemClose(@NotNull View view, @NotNull MultiMediaView multiMediaView) {
                // 停止上传
                timers.get(multiMediaView).cancel();
                timers.remove(multiMediaView);
            }

            @Override
            public void onItemAudioStartDownload(@NotNull View view, @NotNull String url) {

            }

            @Override
            public boolean onItemVideoStartDownload(@NotNull View view, @NotNull MultiMediaView multiMediaView) {
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
    protected MaskProgressLayout getMaskProgressLayout() {
        return mBinding.mplImageList;
    }

    @Override
    protected void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        // 支持的类型：图片，视频
        cameraSetting.mimeTypeSet(MimeType.ofAll());
//        cameraSetting.enableImageHighDefinition(true);
        // 自定义cameraView的宽高，更多设置参考 https://github.com/natario1/CameraView 源码
        cameraSetting.setOnCameraViewListener(cameraView -> {
            // 可以自定义cameraView预览时候的宽高,如果定义的不是高清拍照录制模式，那么出来的成品也是跟预览一样大小
            // 如果想做成比例方式也可以，那么计算屏幕宽度，高度这些就不用我说了吧？
            updateSize(cameraView);

            // 如果想跟系统相机一样拍摄范围更广，需要设置cameraSetting.enableImageHighDefinition(true)，同时要修改cameraPreview
//             updateCameraPreview(cameraView);
        });

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
        mGlobalSetting = MultiMediaSetting.from(MainCustomCameraViewActivity.this).choose(MimeType.ofAll());

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
                        5,
                        3,
                        3,
                        alreadyImageCount,
                        alreadyVideoCount,
                        alreadyAudioCount)
                .forResult(REQUEST_CODE_CHOOSE);
    }

    /**
     * 修改宽高
     */
    private void updateSize(CameraView cameraView) {
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(400, 100);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        cameraView.setLayoutParams(layoutParams);
    }

    /**
     * 修改CameraPreview
     * <p>
     * 之前有小伙伴想做出跟系统相机预览范围一样广，但是这基本不可能的，这个是根据硬件配置而定
     * 因为想预览界面铺满又想拍照反应快又想跟系统相机一摸一样的话，微信早就这样做出来了
     */
    private void updateCameraPreview(CameraView cameraView) {
        cameraView.setPreview(Preview.SURFACE);
    }

}
