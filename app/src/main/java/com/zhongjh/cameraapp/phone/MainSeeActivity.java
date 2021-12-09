package com.zhongjh.cameraapp.phone;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.album.filter.BaseFilter;
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;
import com.zhongjh.cameraapp.BaseActivity;
import com.zhongjh.cameraapp.configuration.GifSizeFilter;
import com.zhongjh.cameraapp.configuration.Glide4Engine;
import com.zhongjh.cameraapp.R;
import com.zhongjh.cameraapp.databinding.ActivityMainSeeBinding;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;
import com.zhongjh.retrofitdownloadlib.http.DownloadHelper;
import com.zhongjh.retrofitdownloadlib.http.DownloadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.zhongjh.common.entity.SaveStrategy;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.common.enums.MultimediaTypes;

/**
 * 这是用于设置加载数据的
 * 因为这不是重点开发加上时间因素，目前不做在线播放音频和视频。
 * 大体逻辑是先下载文件到指定目录，然后再赋值，播放。
 *
 * @author zhongjh
 * @date 2019/2/21
 */
public class MainSeeActivity extends BaseActivity implements DownloadListener {

    ActivityMainSeeBinding mBinding;
    /**
     * 用于下载后记录的音频view
     */
    View mAudioView;
    /**
     * 用于下载后记录的视频view
     */
    MultiMediaView mVideoMultiMediaView;

    /**
     * 初始化下载
     */
    private DownloadHelper mDownloadHelper = new DownloadHelper(this);

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
        setContentView(R.layout.activity_main_see);
        progressDialog = new ProgressDialog(MainSeeActivity.this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_see);
        mBinding.mplImageList.setMaskProgressLayoutListener(new MaskProgressLayoutListener() {

            @Override
            public void onItemAdd(View view, MultiMediaView multiMediaView, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击添加
                boolean isOk = getPermissions(false);
                if (isOk) {
                    openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
                }
            }

            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public void onItemClick(View view, MultiMediaView multiMediaView) {
                // 点击详情
                if (multiMediaView.getType() == MultimediaTypes.PICTURE || multiMediaView.getType() == MultimediaTypes.VIDEO) {
                    MultiMediaSetting.openPreviewData(MainSeeActivity.this, REQUEST_CODE_CHOOSE,
                            mBinding.mplImageList.getImagesAndVideos(),
                            mBinding.mplImageList.getImagesAndVideos().indexOf(multiMediaView));
                }
            }

            @Override
            public void onItemStartUploading(MultiMediaView multiMediaView) {
                // 开始模拟上传 - 指刚添加后的。这里可以使用你自己的上传事件
                MyTask timer = new MyTask(multiMediaView);
                timers.put(multiMediaView, timer);
                timer.schedule();
            }

            @Override
            public void onItemClose(View view, MultiMediaView multiMediaView) {
                // 停止上传
                if (timers.get(multiMediaView) != null) {
                    timers.get(multiMediaView).cancel();
                    timers.remove(multiMediaView);
                }
            }

            @Override
            public void onItemAudioStartDownload(View view, String url) {
                boolean isOk = getPermissions(true);
                if (isOk) {
                    // 判断是否存在文件
                    String[] fileFullPath = getFileFullPath(url, 0);
                    boolean isExists = fileIsExists(fileFullPath[0] + File.separator + fileFullPath[1]);
                    if (!isExists) {
                        // 调用方法
                        mAudioView = view;
                        mDownloadHelper.downloadFile(url, fileFullPath[0], fileFullPath[1]);
                    } else {
                        // 直接赋值
                        mBinding.mplImageList.setAudioCover(view, fileFullPath[0] + File.separator + fileFullPath[1]);
                        mBinding.mplImageList.onAudioClick(view);
                    }
                }
            }

            @Override
            public boolean onItemVideoStartDownload(View view, MultiMediaView multiMediaView) {
                boolean isOk = getPermissions(true);
                if (isOk) {
                    String[] fileFullPath = getFileFullPath(multiMediaView.getUrl(), 1);
                    boolean isExists = fileIsExists(fileFullPath[0] + File.separator + fileFullPath[1]);
                    if (!isExists) {
                        // 调用方法
                        mVideoMultiMediaView = multiMediaView;
                        mDownloadHelper.downloadFile(multiMediaView.getUrl(), fileFullPath[0], fileFullPath[1]);
                        // 返回false是中断后面的操作，先让目前视频文件下载完
                        return false;
                    } else {
                        // 直接赋值
                        mBinding.mplImageList.setVideoCover(multiMediaView, fileFullPath[0] + File.separator + fileFullPath[1]);
                        // 赋值本地播放地址后,返回true是可以继续播放的播放事件
                        return true;
                    }
                }
                return false;
            }

        });
        initConfig();
        initData();
        findViewById(R.id.btnSetValue).setOnClickListener(view -> initData());
        findViewById(R.id.btnReset).setOnClickListener(view -> mBinding.mplImageList.reset());
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
                .thumbnailScale(0.85f)
                .setOnSelectedListener((uriList, pathList) -> {
                    // 每次选择的事件
                    Log.d("onSelected", "onSelected: pathList=" + pathList);
                })
                // 开启原图
                .originalEnable(true)
                // 最大原图size,仅当originalEnable为true的时候才有效
                .maxOriginalSize(1)
                .setOnCheckedListener(isChecked -> {
                    // 是否勾选了原图
                    Log.d("isChecked", "onCheck: isChecked=" + isChecked);
                });

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();

        // 全局
        mGlobalSetting = MultiMediaSetting.from(MainSeeActivity.this)
                .choose(MimeType.ofAll())
                .albumSetting(albumSetting)
                .cameraSetting(cameraSetting)
                .recorderSetting(recorderSetting)
                .setOnMainListener(errorMessage -> Toast.makeText(getApplication(), errorMessage, Toast.LENGTH_LONG).show())
                // 设置路径和7.0保护路径等等
                .allStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/test"))
                // 如果设置这个，有关图片的优先权比allStrategy高
                .pictureStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/picture"))
                // 如果设置这个，有关音频的优先权比allStrategy高
                .audioStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/audio"))
                // 如果设置这个，有关视频的优先权比allStrategy高
                .videoStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/video"))
                //  .imageEngine(new GlideEngine())  // for glide-V3     // for glide-V4
                .imageEngine(new Glide4Engine());
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mBinding.mplImageList.setOperation(true);

        // 音频数据
        List<String> audioUrls = new ArrayList<>();
        audioUrls.add("https://img.huoyunji.com/audio_20190221105823_Android_28360");
        audioUrls.add("https://img.huoyunji.com/audio_20190221105823_Android_28360");
        mBinding.mplImageList.setAudioUrls(audioUrls);

        // 视频数据
        List<String> videoUrls = new ArrayList<>();
        videoUrls.add("https://img.huoyunji.com/video_20190221105749_Android_31228");
        videoUrls.add("https://www.w3school.com.cn/example/html5/mov_bbb.mp4");
        mBinding.mplImageList.setVideoUrls(videoUrls);

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
        mBinding.mplImageList.setImageUrls(imageUrls);
    }

    /**
     * 重置
     */
    private void reset() {
        mBinding.mplImageList.reset();
    }

    @Override
    protected MaskProgressLayout getMaskProgressLayout() {
        return mBinding.mplImageList;
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
        mGlobalSetting.maxSelectablePerMediaType(12,
                null,
                null,
                null,
                alreadyImageCount,
                alreadyVideoCount,
                alreadyAudioCount)
                .forResult(REQUEST_CODE_CHOOSE);
    }

    @Override
    public void onStartDownload() {
        // 加载前
        progressDialog.show();
    }

    @Override
    public void onProgress(int i) {

    }

    @Override
    public void onFinishDownload(File file) {
        // 下载完成，判断后缀名进行相应的处理
        String suffix = file.getPath().substring(file.getPath().lastIndexOf(".") + 1);
        switch (suffix) {
            case "mp3":
                mBinding.mplImageList.setAudioCover(mAudioView, file.getPath());
                break;
            case "mp4":
                mBinding.mplImageList.setVideoCover(mVideoMultiMediaView, file.getPath());
                break;
            default:
                break;
        }
        progressDialog.hide();
    }

    @Override
    public void onFail(Throwable throwable) {
        progressDialog.hide();
        Toast.makeText(this, getResources().getString(R.string.download_failed) + ":" + throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
