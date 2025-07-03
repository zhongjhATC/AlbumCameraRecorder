package com.zhongjh.demo.phone;

import static androidx.camera.core.CameraEffect.PREVIEW;
import static androidx.camera.core.CameraEffect.VIDEO_CAPTURE;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.effects.OverlayEffect;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.VideoCapture;
import androidx.camera.view.PreviewView;

import com.zhongjh.multimedia.album.filter.BaseFilter;
import com.zhongjh.multimedia.camera.listener.OnInitCameraManager;
import com.zhongjh.multimedia.settings.AlbumSetting;
import com.zhongjh.multimedia.settings.CameraSetting;
import com.zhongjh.multimedia.settings.GlobalSetting;
import com.zhongjh.multimedia.settings.MultiMediaSetting;
import com.zhongjh.multimedia.settings.RecorderSetting;
import com.zhongjh.demo.BaseActivity;
import com.zhongjh.demo.configuration.GifSizeFilter;
import com.zhongjh.demo.configuration.Glide4Engine;
import com.zhongjh.demo.databinding.ActivityMainCustomCameraviewBinding;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.gridview.apapter.GridAdapter;
import com.zhongjh.common.entity.GridMedia;
import com.zhongjh.gridview.listener.GridViewListener;
import com.zhongjh.gridview.widget.GridView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 自定义CameraView
 *
 * @author zhongjh
 * @date 2021/8/23
 */
public class MainCustomCameraViewActivity extends BaseActivity {

    ActivityMainCustomCameraviewBinding mBinding;

    GlobalSetting mGlobalSetting;

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainCustomCameraViewActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainCustomCameraviewBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        // 以下为点击事件
        mBinding.gridView.setGridViewListener(new GridViewListener() {

            @Override
            public boolean onItemStartDownload(@NonNull View view, @NonNull GridMedia gridMedia, int position) {
                return false;
            }

            @Override
            public void onItemAdd(@NotNull View view, @NotNull GridMedia gridMedia, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击添加
                openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            }

            @Override
            public void onItemClick(@NotNull View view, @NotNull GridMedia gridMedia) {
                // 点击详情
                mGlobalSetting.openPreviewData(MainCustomCameraViewActivity.this, requestLauncherGrid, mBinding.gridView.getAllData(), mBinding.gridView.getAllData().indexOf(gridMedia), mBinding.gridView.isOperation());
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
    protected GridView getGridView() {
        return mBinding.gridView;
    }

    @Override
    protected void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        // 支持的类型：图片，视频
        cameraSetting.mimeTypeSet(MimeType.ofAll());
        // 如果想自己自定义分辨率、比例等等,可参考camerax API
        cameraSetting.setOnInitCameraManager(new OnInitCameraManager() {

            @Override
            public void initPreview(@NonNull Preview.Builder previewBuilder, int screenAspectRatio, int rotation) {
                previewBuilder.setResolutionSelector(new ResolutionSelector.Builder()
                        // 设置比例 16：9
                        .setAspectRatioStrategy(new AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                        // 设置高分辨率
                        .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                        .build());
                previewBuilder.setTargetRotation(rotation);
            }

            @Override
            public void initImageCapture(@NonNull ImageCapture.Builder imageBuilder, int screenAspectRatio, int rotation) {
                imageBuilder.setResolutionSelector(new ResolutionSelector.Builder()
                        // 设置比例 16：9
                        .setAspectRatioStrategy(new AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                        // 设置高分辨率
                        .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                        .build());
                imageBuilder.setTargetRotation(rotation);
            }

            @Override
            public void initImageAnalyzer(@NonNull ImageAnalysis.Builder imageAnalyzerBuilder, int screenAspectRatio, int rotation) {
                imageAnalyzerBuilder.setResolutionSelector(new ResolutionSelector.Builder()
                        // 设置比例 16：9
                        .setAspectRatioStrategy(new AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                        // 设置高分辨率
                        .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                        .build());
                imageAnalyzerBuilder.setTargetRotation(rotation);
            }

            @Override
            public void initVideoRecorder(@NonNull Recorder.Builder recorder, int screenAspectRatio) {
                // 设置高分辨率,设置比例 16：9
                QualitySelector qualitySelector = QualitySelector.from(Quality.HIGHEST);
                recorder.setQualitySelector(qualitySelector)
                        .setAspectRatio(AspectRatio.RATIO_16_9);
            }

            @Override
            public void initVideoCapture(@NonNull VideoCapture.Builder<Recorder> videoCaptureBuilder, int rotation) {
                videoCaptureBuilder.setTargetRotation(rotation);
            }

            @Override
            public boolean isDefaultOverlayEffect() {
                // 如果自定义高分辨率,请不要用默认水印,会影响性能,最好方式是自定义水印,分别是视频水印在这个方法initOverlayEffect处理、图片水印在这个方法处理
                return false;
            }

            @Override
            public OverlayEffect initOverlayEffect(@NonNull PreviewView previewView) {
                return MainCustomCameraViewActivity.this.initOverlayEffect(previewView);
            }
            
            @Override
            public void initWatermarkedImage(@NonNull String path) {

            }
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
//        mGlobalSetting.defaultPosition(1);

        // 开启相册功能
        mGlobalSetting.albumSetting(albumSetting);
        // 开启拍摄功能
        mGlobalSetting.cameraSetting(cameraSetting);
        // 开启录音功能
        mGlobalSetting.recorderSetting(recorderSetting);

        mGlobalSetting
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
                .forResult(requestLauncherACR);
    }

    /**
     * 自定义叠加效果
     * 由于分辨率改成高清,所以水印只支持
     *
     * @return OverlayEffect
     */
    private OverlayEffect initOverlayEffect(PreviewView previewView) {
        OverlayEffect overlayEffect = new OverlayEffect(PREVIEW | VIDEO_CAPTURE, 0, new Handler(Looper.getMainLooper()), throwable -> Log.e("initOverlayEffect", "initOverlayEffect errorListener " + throwable.getMessage()));
        Paint textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(36F);
        textPaint.setAntiAlias(true);
        // 左右底部间距
        float marginSize = 50F;
        // 文字宽度
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        float textWidthSize = textPaint.measureText("自定义 - " + dataFormat.format(new Date())) + marginSize;
        // 在每一帧绘制水印
        overlayEffect.clearOnDrawListener();
        overlayEffect.setOnDrawListener(frame -> {
            Matrix sensorToUi = previewView.getSensorToViewTransform();
            if (sensorToUi != null) {
                Matrix sensorToEffect = frame.getSensorToBufferTransform();
                Matrix uiToSensor = new Matrix();
                sensorToUi.invert(uiToSensor);
                uiToSensor.postConcat(sensorToEffect);
                Canvas canvas = frame.getOverlayCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.setMatrix(uiToSensor);
                // 绘制文字
                switch (Integer.parseInt(previewView.getTag(com.zhongjh.multimedia.R.id.target_rotation).toString())) {
                    case Surface.ROTATION_0:
                        // 底部绘制,以手机底部为标准xy正常计算
                        canvas.drawText("自定义 - " + dataFormat.format(new Date()), previewView.getWidth() - textWidthSize, previewView.getHeight() - marginSize, textPaint);
                        break;
                    case Surface.ROTATION_90:
                        // 以左边为底部,依然以手机底部为标准xy正常计算
                        canvas.rotate(90F, marginSize, previewView.getHeight() - textWidthSize);
                        canvas.drawText("自定义 - " + dataFormat.format(new Date()), marginSize, previewView.getHeight() - textWidthSize, textPaint);
                        break;
                    case Surface.ROTATION_270:
                        // 以右边为底部,依然以手机底部为标准xy正常计算
                        canvas.rotate(-90F, previewView.getWidth() - marginSize, textWidthSize);
                        canvas.drawText("自定义 - " + dataFormat.format(new Date()), previewView.getWidth() - marginSize, textWidthSize, textPaint);
                        break;
                }
            }
            return true;
        });
        return overlayEffect;
    }

}
