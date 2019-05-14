package com.zhongjh.cameraapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.album.filter.Filter;
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;
import com.zhongjh.cameraapp.databinding.ActivityMainSeeBinding;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;
import com.zhongjh.retrofitdownloadlib.http.DownloadHelper;
import com.zhongjh.retrofitdownloadlib.http.DownloadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gaode.zhongjh.com.common.entity.SaveStrategy;
import gaode.zhongjh.com.common.enums.MimeType;
import gaode.zhongjh.com.common.enums.MultimediaTypes;

/**
 * 这是用于设置加载数据的
 * 因为这不是重点开发加上时间因素，目前不做在线播放音频和视频。
 * 大体逻辑是先下载文件到指定目录，然后再赋值，播放。
 * Created by zhongjh on 2019/2/21.
 */
public class MainSeeActivity extends BaseActivity implements DownloadListener {

    ActivityMainSeeBinding mBinding;

    // 初始化
    private DownloadHelper mDownloadHelper = new DownloadHelper("http://www.baseurl.com", this);

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
                if (isOk)
                    openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            }

            @Override
            public void onItemImage(View view, MultiMediaView multiMediaView) {
                // 点击详情
                if (multiMediaView.getType() == MultimediaTypes.PICTURE) {
                    // 判断如果是图片类型就预览当前所有图片
                    MultiMediaSetting.openPreviewImage(MainSeeActivity.this, (ArrayList) mBinding.mplImageList.getImages(), multiMediaView.getPosition());
                } else if (multiMediaView.getType() == MultimediaTypes.VIDEO) {
                    // 判断如果是视频类型就预览视频
                    MultiMediaSetting.openPreviewVideo(MainSeeActivity.this, (ArrayList) mBinding.mplImageList.getVideos());
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
            public void onItemAudioStartDownload(String url) {
                boolean isOk = getPermissions(true);
                if (isOk) {
                    // 判断是否存在文件
                    String[] fileFullPath = getFileFullPath(url, 0);
                    boolean isExists = fileIsExists(fileFullPath[0] + File.separator + fileFullPath[1]);
                    if (!isExists) {
                        // 调用方法
                        mDownloadHelper.downloadFile(url, fileFullPath[0], fileFullPath[1]);
                    } else {
                        // 直接赋值
                        mBinding.mplImageList.addAudioCover(fileFullPath[0] + File.separator + fileFullPath[1]);
                        mBinding.mplImageList.onAudioClick();
                    }
                }
            }

            @Override
            public void onItemVideoStartDownload(String url) {
                boolean isOk = getPermissions(true);
                if (isOk) {
                    String[] fileFullPath = getFileFullPath(url, 1);
                    boolean isExists = fileIsExists(fileFullPath[0] + File.separator + fileFullPath[1]);
                    if (!isExists) {
                        // 调用方法
                        mDownloadHelper.downloadFile(url, fileFullPath[0], fileFullPath[1]);
                    } else {
                        // 直接赋值
                        List<String> videoPath = new ArrayList<>();
                        videoPath.add(fileFullPath[0] + File.separator + fileFullPath[1]);
                        mBinding.mplImageList.addVideoCover(videoPath);
                        mBinding.mplImageList.onVideoClick();
                    }
                }
            }

        });

        initConfig();

        initData();

    }

    /**
     * 初始化相关配置
     */
    private void initConfig() {
        // 拍摄有关设置
        CameraSetting cameraSetting = new CameraSetting();
        cameraSetting.mimeTypeSet(MimeType.ofAll());// 支持的类型：图片，视频

        // 相册
        AlbumSetting albumSetting = new AlbumSetting(true)
                .mimeTypeSet(MimeType.ofAll())// 支持的类型：图片，视频
                .showSingleMediaType(true) // 仅仅显示一个多媒体类型
                .countable(true)// 是否显示多选图片的数字
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))// 自定义过滤器
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))// 九宫格大小
                .thumbnailScale(0.85f)// 图片缩放比例
                .setOnSelectedListener((uriList, pathList) -> {
                    // 每次选择的事件
                    Log.e("onSelected", "onSelected: pathList=" + pathList);
                })
                .originalEnable(true)// 开启原图
                .maxOriginalSize(1) // 最大原图size,仅当originalEnable为true的时候才有效
                .setOnCheckedListener(isChecked -> {
                    // DO SOMETHING IMMEDIATELY HERE
                    Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                });

        // 录音机
        RecorderSetting recorderSetting = new RecorderSetting();

        // 全局
        mGlobalSetting = MultiMediaSetting.from(MainSeeActivity.this)
                .choose(MimeType.ofAll())
                .albumSetting(albumSetting)
                .cameraSetting(cameraSetting)
                .recorderSetting(recorderSetting)
                .setOnMainListener(errorMessage -> Toast.makeText(MainSeeActivity.this.getApplicationContext(), "自定义失败信息：录音已经达到上限", Toast.LENGTH_LONG).show())
                .allStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/test"))// 设置路径和7.0保护路径等等
                .pictureStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/picture")) // 如果设置这个，有关图片的优先权比allStrategy高
                .audioStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/audio")) // 如果设置这个，有关音频的优先权比allStrategy高
                .videoStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "AA/video")) // 如果设置这个，有关视频的优先权比allStrategy高
                //                                            .imageEngine(new GlideEngine())  // for glide-V3
                .imageEngine(new Glide4Engine());    // for glide-V4
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mBinding.mplImageList.setOperation(true);
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("http://img.huoyunji.com/photo_20190221105726_Android_15181?imageMogr2/auto-orient/thumbnail/!280x280r/gravity/Center/crop/280x280/format/jpg/interlace/1/blur/1x0/quality/90");
        imageUrls.add("http://img.huoyunji.com/photo_20190221105418_Android_47466?imageMogr2/auto-orient/thumbnail/!280x280r/gravity/Center/crop/280x280/format/jpg/interlace/1/blur/1x0/quality/90");
        mBinding.mplImageList.addImageUrls(imageUrls);
        mBinding.mplImageList.addAudioUrl("http://img.huoyunji.com/audio_20190221105823_Android_28360");
        mBinding.mplImageList.addVideoUrl("http://img.huoyunji.com/video_20190221105749_Android_31228");
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
    protected void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        mGlobalSetting.maxSelectablePerMediaType(5 - alreadyImageCount, 1 - alreadyVideoCount, 1 - alreadyAudioCount)// 最大10张图片或者最大1个视频
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
                mBinding.mplImageList.addAudioCover(file.getPath());
                break;
            case "mp4":
                List<String> videoPath = new ArrayList<>();
                videoPath.add(file.getPath());
                mBinding.mplImageList.addVideoCover(videoPath);
                break;
        }
        progressDialog.hide();
    }

    @Override
    public void onFail(Throwable throwable) {
        progressDialog.hide();
        Toast.makeText(this, "下载失败：" + throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
        }
        // 获取文件名
        String fileName = url.substring(url.lastIndexOf("/") + 1) + suffixName;
        return new String[]{Environment.getExternalStorageDirectory() + File.separator + "AA" + File.separator + "audioCache", fileName};
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
