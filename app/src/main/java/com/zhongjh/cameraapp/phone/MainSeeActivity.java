package com.zhongjh.cameraapp.phone;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
import com.zhongjh.cameraapp.databinding.ActivityMainSeeBinding;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.gridview.apapter.GridAdapter;
import com.zhongjh.common.entity.GridMedia;
import com.zhongjh.gridview.listener.GridViewListener;
import com.zhongjh.gridview.widget.GridView;
import com.zhongjh.retrofitdownloadlib.http.DownloadInfo;
import com.zhongjh.retrofitdownloadlib.http.DownloadProgressHandler;
import com.zhongjh.retrofitdownloadlib.http.FileDownloader;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 这是用于设置加载数据的
 * 因为这不是重点开发加上时间因素，目前不做在线播放音频和视频。
 * 大体逻辑是先下载文件到指定目录，然后再赋值，播放。
 *
 * @author zhongjh
 * @date 2019/2/21
 */
public class MainSeeActivity extends BaseActivity {

    private static final String TAG = MainSeeActivity.class.getSimpleName();

    ActivityMainSeeBinding mBinding;

    ProgressDialog progressDialog;

    GlobalSetting mGlobalSetting;

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainSeeActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainSeeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        progressDialog = new ProgressDialog(MainSeeActivity.this);
        progressDialog.setTitle("下载中");
        mBinding.gridView.setGridViewListener(new GridViewListener() {

            @Override
            public boolean onItemStartDownload(@NotNull View view, @NotNull GridMedia gridMedia, int position) {
                boolean isOk = getPermissions(true);
                if (isOk) {
                    String[] fileFullPath = getFileFullPath(gridMedia.getUrl(), 1);
                    String path = fileFullPath[0] + File.separator + fileFullPath[1];
                    boolean isExists = fileIsExists(path);
                    if (!isExists) {
                        // 下载
                        progressDialog.show();
                        FileDownloader.downloadFile(gridMedia.getUrl(), fileFullPath[0], fileFullPath[1], new DownloadProgressHandler() {

                            @Override
                            public void onProgress(DownloadInfo downloadInfo) {

                            }

                            @Override
                            public void onCompleted(File file) {
                                mBinding.gridView.setDataCover(gridMedia, file.getPath());
                                progressDialog.hide();
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                progressDialog.hide();
                                Log.e("MainSeeActivity", "onFail", throwable);
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.download_failed) + ":" + throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        // 返回false是中断后面的操作，先让目前视频文件下载完
                        return false;
                    } else {
                        // 获取时间,直接赋值
                        mBinding.gridView.setDataCover(gridMedia, path);
                        // 赋值本地播放地址后,返回true是可以继续播放的播放事件
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onItemAdd(@NotNull View view, @NotNull GridMedia gridMedia, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击添加
                boolean isOk = getPermissions(false);
                if (isOk) {
                    openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
                }
            }

            @Override
            public void onItemClick(@NotNull View view, @NotNull GridMedia gridMedia) {
                // 点击详情,通过网页形式加载的数据，是加载不了详情数据的
                Log.i(TAG, "onResult id:" + gridMedia.getFileId());
                Log.i(TAG, "onResult url:" + gridMedia.getUrl());
                Log.d(TAG, "onResult 绝对路径:" + gridMedia.getPath());
                Log.d(TAG, "onResult Uri:" + gridMedia.getPath());
                Log.d(TAG, "onResult 文件大小: " + gridMedia.getSize());
                Log.d(TAG, "onResult 视频音频长度: " + gridMedia.getDuration());
                if (gridMedia.isImageOrGif()) {
                    if (gridMedia.isImage()) {
                        Log.d(TAG, "onResult 图片类型");
                    } else if (gridMedia.isImage()) {
                        Log.d(TAG, "onResult 图片类型");
                    }
                } else if (gridMedia.isVideo()) {
                    Log.d(TAG, "onResult 视频类型");
                } else if (gridMedia.isAudio()) {
                    Log.d(TAG, "onResult 音频类型");
                }
                Log.d(TAG, "onResult 具体类型:" + gridMedia.getMimeType());
                Log.d(TAG, "onResult 宽高: " + gridMedia.getWidth() + "x" + gridMedia.getHeight());
                if (gridMedia.isImageOrGif() || gridMedia.isVideo()) {
//                    mGlobalSetting.openPreviewData(MainSeeActivity.this, REQUEST_CODE_CHOOSE,
//                            mBinding.dmlImageList.getImagesAndVideos(),
//                            mBinding.dmlImageList.getImagesAndVideos().indexOf(multiMediaView));
                }
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
        initConfig();
        initData();
        initListener();
        findViewById(R.id.btnSetValue).setOnClickListener(view -> initData());
        findViewById(R.id.btnReset).setOnClickListener(view -> mBinding.gridView.reset());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGlobalSetting != null) {
            mGlobalSetting.onDestroy();
        }
    }

    /**
     * 初始化相关配置
     */
    private void initConfig() {
        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        // 支持的类型：图片，视频
        cameraSetting.mimeTypeSet(MimeType.ofAll());

        // 相册
        AlbumSetting albumSetting = new AlbumSetting(true)
                // 支持的类型：图片，视频
                .mimeTypeSet(MimeType.ofAll())
                // 仅仅显示一个多媒体类型
                .showSingleMediaType(true)
                // 是否显示多选图片的数字
                .countable(true)
                // 自定义过滤器
                .addFilter(new GifSizeFilter(320, 320, 5 * BaseFilter.K * BaseFilter.K))
                // 九宫格大小
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                // 图片缩放比例
                .thumbnailScale(0.85f).setOnSelectedListener(localFiles -> {
                    // 每次选择的事件
                    Log.d("onSelected", "onSelected: localFiles.size()=" + localFiles.size());
                })
                // 开启原图
                .originalEnable(true)
                // 最大原图size,仅当originalEnable为true的时候才有效
                .maxOriginalSize(1).setOnCheckedListener(isChecked -> {
                    // 是否勾选了原图
                    Log.d("isChecked", "onCheck: isChecked=" + isChecked);
                });

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();

        // 全局
        mGlobalSetting = MultiMediaSetting.from(MainSeeActivity.this).choose(MimeType.ofAll()).albumSetting(albumSetting).cameraSetting(cameraSetting).recorderSetting(recorderSetting)
                //  .imageEngine(new GlideEngine())  // for glide-V3     // for glide-V4
                .imageEngine(new Glide4Engine());
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mBinding.gridView.setOperation(true);

        // 音频数据
        List<String> audioUrls = new ArrayList<>();
        audioUrls.add("https://img.huoyunji.com/audio_20190221105823_Android_28360");
        audioUrls.add("https://img.huoyunji.com/audio_20190221105823_Android_28360");
        mBinding.gridView.setAudioUrls(audioUrls, false);

        // 视频数据
        List<String> videoUrls = new ArrayList<>();
        videoUrls.add("https://img.huoyunji.com/video_20190221105749_Android_31228");
        videoUrls.add("https://www.w3school.com.cn/example/html5/mov_bbb.mp4");
        mBinding.gridView.setVideoUrls(videoUrls, false);

        // 图片数据
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("https://img.huoyunji.com/photo_20190221105726_Android_15181?imageMogr2/auto-orient/thumbnail/!280x280r/gravity/Center/crop/280x280/format/jpg/interlace/1/blur/1x0/quality/90");
        imageUrls.add("https://img.huoyunji.com/photo_20190221105418_Android_47466?imageMogr2/auto-orient/thumbnail/!280x280r/gravity/Center/crop/280x280/format/jpg/interlace/1/blur/1x0/quality/90");
        imageUrls.add("https://img.huoyunji.com/photo_20190221105418_Android_47466?imageMogr2/auto-orient/thumbnail/!280x280r/gravity/Center/crop/280x280/format/jpg/interlace/1/blur/1x0/quality/90");
        imageUrls.add("https://img.huoyunji.com/photo_20190221105418_Android_47466?imageMogr2/auto-orient/thumbnail/!280x280r/gravity/Center/crop/280x280/format/jpg/interlace/1/blur/1x0/quality/90");
        imageUrls.add("https://img.huoyunji.com/photo_20190221105418_Android_47466?imageMogr2/auto-orient/thumbnail/!280x280r/gravity/Center/crop/280x280/format/jpg/interlace/1/blur/1x0/quality/90");
        imageUrls.add("https://img.huoyunji.com/photo_20190221105418_Android_47466?imageMogr2/auto-orient/thumbnail/!280x280r/gravity/Center/crop/280x280/format/jpg/interlace/1/blur/1x0/quality/90");
        imageUrls.add("https://img.huoyunji.com/photo_20190221105418_Android_47466?imageMogr2/auto-orient/thumbnail/!280x280r/gravity/Center/crop/280x280/format/jpg/interlace/1/blur/1x0/quality/90");
        imageUrls.add("https://img.huoyunji.com/photo_20190221105418_Android_47466?imageMogr2/auto-orient/thumbnail/!280x280r/gravity/Center/crop/280x280/format/jpg/interlace/1/blur/1x0/quality/90");
        mBinding.gridView.setImageUrls(imageUrls, false);

        // 刷新
        mBinding.gridView.notifyDataSetChanged();
    }

    /**
     * 通过url加入的
     */
    private void initListener() {
        findViewById(R.id.btnSetValue).setOnClickListener(view -> initData());
        findViewById(R.id.btnReset).setOnClickListener(view -> mBinding.gridView.reset());
    }

    @Override
    protected GridView getMaskProgressLayout() {
        return mBinding.gridView;
    }

    /**
     * @param alreadyImageCount 已经存在显示的几张图片
     * @param alreadyVideoCount 已经存在显示的几个视频
     * @param alreadyAudioCount 已经存在显示的几个音频
     *                          打开窗体
     */
    @Override
    protected void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        // 最大10张图片或者最大1个视频
        mGlobalSetting.maxSelectablePerMediaType(12, null, null, null, alreadyImageCount, alreadyVideoCount, alreadyAudioCount).forResult(requestLauncherACR);
    }

    /**
     * 返回文件路径
     *
     * @param url  网址
     * @param type 0是mp3,1是mp4
     */
    private String[] getFileFullPath(String url, int type) {
        // 获取后缀名
        String suffixName = null;
        switch (type) {
            case 0:
                suffixName = ".mp3";
                break;
            case 1:
                // 获取后缀名
                suffixName = ".mp4";
                break;
            default:
                break;
        }
        // 获取文件名
        String fileName = url.substring(url.lastIndexOf("/") + 1) + suffixName;

        return new String[]{MainSeeActivity.this.getCacheDir().getPath(), fileName};
    }

    /**
     * 判断是否存在文件
     *
     * @param strFile 文件路径
     */
    public boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}
