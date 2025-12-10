package com.zhongjh.demo.phone;

import static androidx.camera.core.CameraEffect.IMAGE_CAPTURE;
import static androidx.camera.core.CameraEffect.PREVIEW;
import static androidx.camera.core.CameraEffect.VIDEO_CAPTURE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
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

import com.zhongjh.common.entity.GridMedia;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.demo.BaseActivity;
import com.zhongjh.demo.R;
import com.zhongjh.demo.configuration.GifSizeFilter;
import com.zhongjh.demo.configuration.Glide4Engine;
import com.zhongjh.demo.configuration.OnImageCompressionLuBan;
import com.zhongjh.demo.databinding.ActivityMainBinding;
import com.zhongjh.gridview.apapter.GridAdapter;
import com.zhongjh.gridview.listener.GridViewListener;
import com.zhongjh.gridview.widget.GridView;
import com.zhongjh.multimedia.AlbumCameraRecorderApi;
import com.zhongjh.multimedia.album.filter.BaseFilter;
import com.zhongjh.multimedia.camera.entity.BitmapData;
import com.zhongjh.multimedia.camera.listener.OnCaptureListener;
import com.zhongjh.multimedia.camera.listener.OnInitCameraManager;
import com.zhongjh.multimedia.settings.AlbumSetting;
import com.zhongjh.multimedia.settings.CameraSetting;
import com.zhongjh.multimedia.settings.GlobalSetting;
import com.zhongjh.multimedia.settings.MultiMediaSetting;
import com.zhongjh.multimedia.settings.RecorderSetting;
import com.zhongjh.videoedit.VideoCompressManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 配置版
 *
 * @author zhongjh
 */
@SuppressLint("WrongConstant")
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivityTEST";

    ActivityMainBinding mBinding;

    GlobalSetting mGlobalSetting;
    AlbumSetting mAlbumSetting;

    @GlobalSetting.ScreenOrientation
    int requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    int flashMode = ImageCapture.FLASH_MODE_OFF;

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // 提交进行上传
        mBinding.btnSubmitUpload.setOnClickListener(v -> {
            // 开始模拟上传 - 指所有
            for (GridMedia gridMedia : mBinding.gridView.getAllData()) {
                MyTask timer = new MyTask(gridMedia);
                timers.put(gridMedia, timer);
                timer.schedule();
            }
        });

        mBinding.llScreenOrientation.setOnClickListener(v -> showPopupMenu());

        mBinding.llFlashMode.setOnClickListener(v -> showFlashPopupMenu());

        // 设置九宫格的最大呈现数据
        mBinding.gridView.setMaxMediaCount(getMaxCount(), getImageCount(), getVideoCount(), getAudioCount());

        // 以下为点击事件
        mBinding.gridView.setGridViewListener(new GridViewListener() {

            /** @noinspection unused*/
            @Override
            public boolean onItemStartDownload(@NonNull View view, @NonNull GridMedia gridMedia, int position) {
                return false;
            }

            /** @noinspection unused*/
            @Override
            public void onItemAdd(@NotNull View view, @NotNull GridMedia gridMedia, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            }

            /** @noinspection unused*/
            @Override
            public void onItemClick(@NotNull View view, @NotNull GridMedia gridMedia) {
                // 点击详情
                mGlobalSetting.openPreviewData(MainActivity.this, requestLauncherGrid, mBinding.gridView.getAllData(), mBinding.gridView.getAllData().indexOf(gridMedia), mBinding.gridView.isOperation());
            }

            /** @noinspection unused*/
            @Override
            public void onItemStartUploading(@NotNull GridMedia gridMedia, @NotNull GridAdapter.PhotoViewHolder viewHolder) {
                // 开始模拟上传 - 指刚添加后的。这里可以使用你自己的上传事件
                MyTask timer = new MyTask(gridMedia);
                timers.put(gridMedia, timer);
                timer.schedule();
            }

            /** @noinspection unused*/
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

        // 获取文件大小 文件目录：context.getExternalCacheDir()
        mBinding.btnFileSize.setOnClickListener(v -> mBinding.tvFileSize.setText(AlbumCameraRecorderApi.getFileSize(getApplication())));

        // 删除文件缓存 文件目录：context.getExternalCacheDir()
        mBinding.btnDeleteFileCache.setOnClickListener(v -> AlbumCameraRecorderApi.deleteCacheDirFile(getApplication()));

        // 当选择了默认分辨率、比例
        mBinding.rbDefaultResolutionScale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mBinding.cbDefaultWatermark.setChecked(true);
                mBinding.cbDefaultWatermark.setEnabled(true);
                mBinding.cbCustomWatermarkAll.setEnabled(true);
                mBinding.cbCustomWatermarkVideo.setChecked(false);
                mBinding.cbCustomWatermarkVideo.setEnabled(false);
                mBinding.cbCustomWatermarkImage.setChecked(false);
                mBinding.cbCustomWatermarkImage.setEnabled(false);
                mBinding.cbNoWatermark.setChecked(false);
            }
        });

        // 当选择了自定义分辨率、比例
        mBinding.rbHDResolutionScale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mBinding.cbDefaultWatermark.setChecked(false);
                mBinding.cbCustomWatermarkAll.setChecked(false);
                mBinding.cbDefaultWatermark.setEnabled(false);
                mBinding.cbCustomWatermarkAll.setEnabled(false);
                mBinding.cbCustomWatermarkVideo.setChecked(true);
                mBinding.cbCustomWatermarkVideo.setEnabled(true);
                mBinding.cbCustomWatermarkImage.setChecked(true);
                mBinding.cbCustomWatermarkImage.setEnabled(true);
                mBinding.cbNoWatermark.setChecked(false);
            }
        });

        // 清空所有水印
        mBinding.cbNoWatermark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mBinding.cbDefaultWatermark.setChecked(false);
                mBinding.cbCustomWatermarkAll.setChecked(false);
                mBinding.cbCustomWatermarkVideo.setChecked(false);
                mBinding.cbCustomWatermarkImage.setChecked(false);
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

    /**
     * @param alreadyImageCount 已经存在显示的几张图片
     * @param alreadyVideoCount 已经存在显示的几个视频
     * @param alreadyAudioCount 已经存在显示的几个音频
     *                          打开窗体
     */
    @SuppressLint("AppBundleLocaleChanges")
    @Override
    protected void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        if (!check()) {
            return;
        }

        // 刷新九宫格的最大呈现数据
        mBinding.gridView.setMaxMediaCount(getMaxCount(), getImageCount(), getVideoCount(), getAudioCount());

        // 拍摄有关设置
        CameraSetting cameraSetting = initCameraSetting();

        // 相册设置
        mAlbumSetting = initAlbumSetting();

        // 录音机设置
        RecorderSetting recorderSetting = new RecorderSetting();

        //  全局
        Set<MimeType> mimeTypes = MimeType.ofAll();
        if (mBinding.rbAllAll.isChecked()) {
            mimeTypes = MimeType.ofAll();
        } else if (mBinding.rbAllVideo.isChecked()) {
            mimeTypes = MimeType.ofVideo();
        } else if (mBinding.rbAllImage.isChecked()) {
            mimeTypes = MimeType.ofImage();
        }

        mGlobalSetting = MultiMediaSetting.from(MainActivity.this).choose(mimeTypes);
        // 默认从第二个开始
        mGlobalSetting.defaultPosition(1);
        // 这种是App内自己修改成强制英文
        if (mBinding.cbEnglish.isChecked()) {
            Resources resources = getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            Configuration config = resources.getConfiguration();
            // 应用用户选择语言
            config.setLocale(Locale.ENGLISH);
            resources.updateConfiguration(config, dm);
        } else {
            Resources resources = getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            Configuration config = resources.getConfiguration();
            // 应用用户选择语言
            config.setLocale(Locale.getDefault());
            resources.updateConfiguration(config, dm);
        }

        // 启动过场动画，从下往上动画
        mGlobalSetting.isCutscenes(mBinding.cbIsCutscenes.isChecked());
        // 设置图片编辑、拍照、录像后是否加入相册功能，默认加入
        mGlobalSetting.isAddAlbum(mBinding.cbIsAddAlbumByEdit.isChecked());
        // 是否支持编辑图片，预览相册、拍照处拥有编辑功能
        mGlobalSetting.isImageEdit(mBinding.cbIsEdit.isChecked());
        if (mBinding.cbAlbum.isChecked())
        // 开启相册功能
        {
            mGlobalSetting.albumSetting(mAlbumSetting);
        }
        if (mBinding.cbCamera.isChecked())
        // 开启拍摄功能
        {
            mGlobalSetting.cameraSetting(cameraSetting);
        }
        if (mBinding.cbRecorder.isChecked())
        // 开启录音功能
        {
            mGlobalSetting.recorderSetting(recorderSetting);
        }

        // 设置横竖屏
        mGlobalSetting.setRequestedOrientation(requestedOrientation);

        // 是否压缩图片
        if (mBinding.cbIsCompressImage.isChecked()) {
            mGlobalSetting.setOnImageCompressionListener(new OnImageCompressionLuBan());
        }

        // 用于记录日志
        mGlobalSetting.setOnLogListener(throwable -> {
            // 打印堆栈日志
            String message = throwable.getMessage();
            if (message != null) {
                Log.e(TAG, message);
                StackTraceElement[] stackTraceElements = throwable.getStackTrace();
                for (StackTraceElement stackTraceElement : stackTraceElements) {
                    Log.e(TAG, stackTraceElement.toString());
                }
                Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // 是否压缩视频
        if (mBinding.cbIsCompressVideo.isChecked()) {
            mGlobalSetting.videoCompress(new VideoCompressManager());
        }

        // 加载图片框架，具体注释看maxSelectablePerMediaType方法注释
        mGlobalSetting.imageEngine(new Glide4Engine()).maxSelectablePerMediaType(getMaxCount(), getImageCount(), getVideoCount(), getAudioCount(), alreadyImageCount, alreadyVideoCount, alreadyAudioCount);

        // 启动
        mGlobalSetting.forResult(requestLauncherACR);
    }

    /**
     * @return 拍摄设置
     */
    private CameraSetting initCameraSetting() {
        CameraSetting cameraSetting = new CameraSetting();
        Set<MimeType> mimeTypeCameras;
        if (mBinding.cbCameraImage.isChecked() && mBinding.cbCameraVideo.isChecked()) {
            mimeTypeCameras = MimeType.ofAll();
            // 支持的类型：图片，视频
            cameraSetting.mimeTypeSet(mimeTypeCameras);
        } else if (mBinding.cbCameraImage.isChecked()) {
            mimeTypeCameras = MimeType.ofImage();
            // 支持的类型：图片
            cameraSetting.mimeTypeSet(mimeTypeCameras);
        } else if (mBinding.cbCameraVideo.isChecked()) {
            mimeTypeCameras = MimeType.ofVideo();
            // 支持的类型：视频
            cameraSetting.mimeTypeSet(mimeTypeCameras);
        }
        // 最长录制时间
        cameraSetting.maxDuration(Integer.parseInt(mBinding.etCameraDuration.getText().toString()));
        // 最短录制时间限制，单位为毫秒，如果录制期间低于2000毫秒，均不算录制。值不能低于2000，如果低于2000还是以2000为准
        cameraSetting.minDuration(Integer.parseInt(mBinding.etMinCameraDuration.getText().toString()));
        // 长按准备时间，单位为毫秒，即是如果长按在1000毫秒内，都暂时不开启录制
        cameraSetting.readinessDuration(Integer.parseInt(mBinding.etCameraReadinessDuration.getText().toString()));
        cameraSetting.setOnInitCameraManager(new OnInitCameraManager() {

            @Override
            public void initPreview(@NonNull Preview.Builder previewBuilder, int screenAspectRatio, int rotation) {
                if (mBinding.rbHDResolutionScale.isChecked()) {
                    previewBuilder.setResolutionSelector(new ResolutionSelector.Builder()
                            // 设置比例 16：9
                            .setAspectRatioStrategy(new AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                            // 设置高分辨率
                            .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                            .build());
                    previewBuilder.setTargetRotation(rotation);
                }
            }

            @Override
            public void initImageCapture(@NonNull ImageCapture.Builder imageBuilder, int screenAspectRatio, int rotation) {
                if (mBinding.rbHDResolutionScale.isChecked()) {
                    imageBuilder.setResolutionSelector(new ResolutionSelector.Builder()
                            // 设置比例 16：9
                            .setAspectRatioStrategy(new AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                            // 设置高分辨率
                            .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                            .build());
                    imageBuilder.setTargetRotation(rotation);
                }
            }

            @Override
            public void initImageAnalyzer(@NonNull ImageAnalysis.Builder imageAnalyzerBuilder, int screenAspectRatio, int rotation) {
                if (mBinding.rbHDResolutionScale.isChecked()) {
                    imageAnalyzerBuilder.setResolutionSelector(new ResolutionSelector.Builder()
                            // 设置比例 16：9
                            .setAspectRatioStrategy(new AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                            // 设置高分辨率
                            .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                            .build());
                    imageAnalyzerBuilder.setTargetRotation(rotation);
                }
            }

            @Override
            public void initVideoRecorder(@NonNull Recorder.Builder recorder, int screenAspectRatio) {
                if (mBinding.rbHDResolutionScale.isChecked()) {
                    // 设置高分辨率,设置比例 16：9
                    QualitySelector qualitySelector = QualitySelector.from(Quality.HIGHEST);
                    recorder.setQualitySelector(qualitySelector)
                            .setAspectRatio(AspectRatio.RATIO_16_9);
                }
            }

            @Override
            public void initVideoCapture(@NonNull VideoCapture.Builder<Recorder> videoCaptureBuilder, int rotation) {
                if (mBinding.rbHDResolutionScale.isChecked()) {
                    videoCaptureBuilder.setTargetRotation(rotation);
                }
            }

            @Override
            public boolean isDefaultOverlayEffect() {
                // 是否启用默认水印
                return mBinding.cbDefaultWatermark.isChecked();
            }

            @Override
            public OverlayEffect initOverlayEffect(@NonNull PreviewView previewView) {
                if (mBinding.cbCustomWatermarkAll.isChecked()) {
                    return MainActivity.this.initOverlayEffect(previewView, PREVIEW | VIDEO_CAPTURE | IMAGE_CAPTURE);
                } else if (mBinding.cbCustomWatermarkVideo.isChecked()) {
                    return MainActivity.this.initOverlayEffect(previewView, PREVIEW | VIDEO_CAPTURE);
                } else if (mBinding.cbNoWatermark.isChecked()) {
                    return null;
                } else {
                    return null;
                }
            }

            @Override
            public void initWatermarkedImage(@NonNull Uri uri, @NonNull String path) {
                if (mBinding.cbCustomWatermarkImage.isChecked()) {
                    addWatermarkToImage(path);
                }
            }
        });

        // 是否启用闪光灯记忆模式
        cameraSetting.enableFlashMemoryModel(mBinding.cbFlashMemoryModel.isChecked());

        // 闪光灯默认模式
        cameraSetting.flashMode(flashMode);

        // 开启点击即开启录制(失去点击拍照功能)
        cameraSetting.isClickRecord(mBinding.cbClickRecord.isChecked());

        // 拍照时添加图片事件以及删除图片事件
        cameraSetting.setOnCaptureListener(new OnCaptureListener() {
            @Override
            public void add(@NonNull List<BitmapData> captureData, int position) {
                Log.d(TAG, "添加索引 " + position);
            }

            @Override
            public void remove(@NonNull List<BitmapData> captureData, int position) {
                Log.d(TAG, "删除索引 " + position);
            }
        });

        return cameraSetting;
    }

    /**
     * @return 相册设置
     */
    private AlbumSetting initAlbumSetting() {
        AlbumSetting albumSetting = new AlbumSetting(!mBinding.cbMediaTypeExclusive.isChecked());
        Set<MimeType> mimeTypeAlbum;
        if (mBinding.cbAlbumImage.isChecked() && mBinding.cbAlbumVideo.isChecked()) {
            mimeTypeAlbum = MimeType.ofAll();
            // 支持的类型：图片，视频
            albumSetting.mimeTypeSet(mimeTypeAlbum);
        } else if (mBinding.cbAlbumImage.isChecked()) {
            mimeTypeAlbum = MimeType.ofImage();
            // 支持的类型：图片，视频
            albumSetting.mimeTypeSet(mimeTypeAlbum);
        } else if (mBinding.cbAlbumVideo.isChecked()) {
            mimeTypeAlbum = MimeType.ofVideo();
            // 支持的类型：图片，视频
            albumSetting.mimeTypeSet(mimeTypeAlbum);
        }

        albumSetting
                // 是否显示多选图片的数字
                .countable(mBinding.cbCountableTrue.isChecked())
                // 自定义过滤器
                .addFilter(new GifSizeFilter(Integer.parseInt(mBinding.etAddFilterMinWidth.getText().toString()), Integer.parseInt(mBinding.etAddFilterMinHeight.getText().toString()), Integer.parseInt(mBinding.etMaxSizeInBytes.getText().toString()) * BaseFilter.K * BaseFilter.K))
                // 九宫格大小 ,建议这样使用getResources().getDimensionPixelSize(R.dimen.grid_expected_size)
                .gridExpectedSize(dip2px(Integer.parseInt(mBinding.etGridExpectedSize.getText().toString())))
                // 图片缩放比例
                .thumbnailScale(0.85f).setOnSelectedListener(localFiles -> {
                    // 每次选择的事件
                    Log.d("onSelected", "onSelected: localFiles.size()=" + localFiles.size());
                })
                // 开启原图
                .originalEnable(mBinding.cbOriginalEnableTrue.isChecked())
                // 是否启动相册列表滑动隐藏顶部和底部控件，上滑隐藏、下滑显示
                .slidingHiddenEnable(mBinding.cbSlideHideEnable.isChecked())
                // 最大原图size,仅当originalEnable为true的时候才有效
                .maxOriginalSize(Integer.parseInt(mBinding.etMaxOriginalSize.getText().toString())).setOnCheckedListener(isChecked -> {
                    // 是否勾选了原图
                    Log.d("isChecked", "onCheck: isChecked=" + isChecked);
                });
        return albumSetting;
    }

    /**
     * @return 返回 图片、视频、音频能选择的上限
     */
    private Integer getMaxCount() {
        if (!mBinding.etMaxCount.getText().toString().isEmpty()) {
            return Integer.parseInt(mBinding.etMaxCount.getText().toString());
        }
        return null;
    }

    /**
     * @return 返回图片能选择的上限
     */
    private Integer getImageCount() {
        if (!mBinding.etAlbumCount.getText().toString().isEmpty()) {
            return Integer.parseInt(mBinding.etAlbumCount.getText().toString());
        }
        return null;
    }

    /**
     * @return 返回视频能选择的上限
     */
    private Integer getVideoCount() {
        if (!mBinding.etVideoCount.getText().toString().isEmpty()) {
            return Integer.parseInt(mBinding.etVideoCount.getText().toString());
        }
        return null;
    }

    /**
     * @return 返回音频能选择的上限
     */
    private Integer getAudioCount() {
        if (!mBinding.etAudioCount.getText().toString().isEmpty()) {
            return Integer.parseInt(mBinding.etAudioCount.getText().toString());
        }
        return null;
    }

    /**
     * 检测正确性
     *
     * @return 是否正确
     */
    private boolean check() {
        if (getMaxCount() == null && getImageCount() == null) {
            Toast.makeText(getApplicationContext(), "maxSelectablePerMediaType 方法中如果 maxSelectable 为null，那么 maxImageSelectable 必须是0或者0以上数值", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (getMaxCount() == null && getVideoCount() == null) {
            Toast.makeText(getApplicationContext(), "maxSelectablePerMediaType 方法中如果 maxSelectable 为null，那么 maxVideoSelectable 必须是0或者0以上数值", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (getMaxCount() == null && getAudioCount() == null) {
            Toast.makeText(getApplicationContext(), "maxSelectablePerMediaType 方法中如果 maxSelectable 为null，那么 maxAudioSelectable 必须是0或者0以上数值", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (getMaxCount() != null && getImageCount() != null && getImageCount() > getMaxCount()) {
            Toast.makeText(getApplicationContext(), "maxSelectable 必须比 maxImageSelectable 大", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (getMaxCount() != null && getVideoCount() != null && getVideoCount() > getMaxCount()) {
            Toast.makeText(getApplicationContext(), "maxSelectable 必须比 maxVideoSelectable 大", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (getMaxCount() != null && getAudioCount() != null && getAudioCount() > getMaxCount()) {
            Toast.makeText(getApplicationContext(), "maxSelectable 必须比 maxAudioSelectable 大", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, mBinding.llScreenOrientation);
        popupMenu.inflate(R.menu.menu_screenorientation);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.actionUnspecified) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_UNSPECIFIED");
            } else if (menuItem.getItemId() == R.id.actionLandscape) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_LANDSCAPE");
            } else if (menuItem.getItemId() == R.id.actionPortrait) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_PORTRAIT");
            } else if (menuItem.getItemId() == R.id.actionUser) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_USER");
            } else if (menuItem.getItemId() == R.id.actionBehind) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_BEHIND;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_BEHIND");
            } else if (menuItem.getItemId() == R.id.actionSensor) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_SENSOR");
            } else if (menuItem.getItemId() == R.id.actionNoSensor) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_NOSENSOR");
            } else if (menuItem.getItemId() == R.id.actionSensorLandscape) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_SENSOR_LANDSCAPE");
            } else if (menuItem.getItemId() == R.id.actionReverseLandscape) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_REVERSE_LANDSCAPE");
            } else if (menuItem.getItemId() == R.id.actionReversePortrait) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_REVERSE_PORTRAIT");
            } else if (menuItem.getItemId() == R.id.actionFullSensor) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_FULL_SENSOR");
            } else if (menuItem.getItemId() == R.id.actionUserLandscape) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_USER_LANDSCAPE");
            } else if (menuItem.getItemId() == R.id.actionUserPortrait) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_USER_PORTRAIT");
            } else if (menuItem.getItemId() == R.id.actionFullUser) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_FULL_USER");
            } else if (menuItem.getItemId() == R.id.actionLocked) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED;
                mBinding.tvScreenOrientation.setText("SCREEN_ORIENTATION_LOCKED");
            }
            return false;
        });
        popupMenu.show();
    }

    /**
     * 弹窗闪光灯选项
     */
    @SuppressLint("NonConstantResourceId")
    private void showFlashPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, mBinding.llFlashMode);
        popupMenu.inflate(R.menu.menu_flash);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.actionFlashOff) {
                flashMode = ImageCapture.FLASH_MODE_OFF;
                mBinding.tvFlashMode.setText(getResources().getString(R.string.flash_off));
            } else if (menuItem.getItemId() == R.id.actionFlashOn) {
                flashMode = ImageCapture.FLASH_MODE_ON;
                mBinding.tvFlashMode.setText(getResources().getString(R.string.flash_on));
            } else if (menuItem.getItemId() == R.id.actionFlashAuto) {
                flashMode = ImageCapture.FLASH_MODE_AUTO;
                mBinding.tvFlashMode.setText(getResources().getString(R.string.flash_auto));
            }
            return false;
        });
        popupMenu.show();
    }

    /**
     * 自定义叠加效果
     *
     * @param previewView 预览view
     * @param targets     针对哪些模块进行水印设置
     * @return OverlayEffect
     */
    private OverlayEffect initOverlayEffect(PreviewView previewView, int targets) {
        OverlayEffect overlayEffect = new OverlayEffect(targets, 0, new Handler(Looper.getMainLooper()), throwable -> Log.e(TAG, "initOverlayEffect errorListener " + throwable.getMessage()));
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

    /**
     * 添加水印
     */
    private void addWatermarkToImage(String path) {
        File file = new File(path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = false;
        if (file.exists()) {
            Bitmap sourceBitmap = BitmapFactory.decodeFile(path, options);
            // 创建带水印的新 Bitmap
            Bitmap watermarkedBitmap = addWatermark(sourceBitmap);
            // 释放源图bitmap
            sourceBitmap.recycle();
            // 保存带水印的图像
            saveWatermarkedImage(watermarkedBitmap, path);
            // 释放带水印的新 Bitmap
            watermarkedBitmap.recycle();
        }
    }

    /**
     * 创建带水印的新 Bitmap
     *
     * @param source 源图
     * @return 新Bitmap
     */
    private Bitmap addWatermark(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, source.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(source, 0f, 0f, null);

        // 设置水印画笔
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(36f);
        paint.setAntiAlias(true);
        paint.setShadowLayer(5f, 0f, 0f, Color.BLACK);

        // 准备水印文本
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String watermarkText = "自定义 - " + dateFormat.format(new Date());
        float textWidth = paint.measureText(watermarkText);

        // 计算水印位置（底部居中）
        float x = (width - textWidth) / 2;
        // 底部边距
        float y = height - 50F;

        // 绘制水印
        canvas.drawText(watermarkText, x, y, paint);
        return result;
    }

    /**
     * 保存带水印的图像
     *
     * @param bitmap 新的bitmap
     * @param path   路径
     */
    private void saveWatermarkedImage(Bitmap bitmap, String path) {
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "saveWatermarkedImage IOException:" + e.getMessage());
        }
    }

}
