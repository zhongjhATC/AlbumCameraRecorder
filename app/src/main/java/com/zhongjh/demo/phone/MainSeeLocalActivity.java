package com.zhongjh.demo.phone;

import static com.zhongjh.common.utils.MediaUtils.getVideoSize;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.zhongjh.common.entity.MediaExtraInfo;
import com.zhongjh.multimedia.album.filter.BaseFilter;
import com.zhongjh.multimedia.settings.AlbumSetting;
import com.zhongjh.multimedia.settings.CameraSetting;
import com.zhongjh.multimedia.settings.GlobalSetting;
import com.zhongjh.multimedia.settings.MultiMediaSetting;
import com.zhongjh.multimedia.settings.RecorderSetting;
import com.zhongjh.common.entity.GridMedia;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.demo.BaseActivity;
import com.zhongjh.demo.R;
import com.zhongjh.demo.configuration.GifSizeFilter;
import com.zhongjh.demo.configuration.Glide4Engine;
import com.zhongjh.demo.databinding.ActivityMainSeeBinding;
import com.zhongjh.gridview.apapter.GridAdapter;
import com.zhongjh.gridview.listener.GridViewListener;
import com.zhongjh.gridview.widget.GridView;
import com.zhongjh.retrofitdownloadlib.http.DownloadInfo;
import com.zhongjh.retrofitdownloadlib.http.DownloadProgressHandler;
import com.zhongjh.retrofitdownloadlib.http.FileDownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 这是用于设置加载数据的,演示本地的Demo
 *
 * @author zhongjh
 * @date 2024/11/07
 */
public class MainSeeLocalActivity extends BaseActivity {

    private static final String TAG = MainSeeLocalActivity.class.getSimpleName();

    ActivityMainSeeBinding mBinding;

    ProgressDialog progressDialog;

    GlobalSetting mGlobalSetting;

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainSeeLocalActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_see);
        // noinspection deprecation
        progressDialog = new ProgressDialog(MainSeeLocalActivity.this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_see);
        mBinding.gridView.setGridViewListener((new GridViewListener() {

            /** @noinspection unused*/
            @Override
            public boolean onItemStartDownload(@NonNull View view, @NonNull GridMedia gridMedia, int position) {
                String[] fileFullPath = getFileFullPath(gridMedia.getUrl(), 1);
                String path = fileFullPath[0] + File.separator + fileFullPath[1];
                boolean isExists = fileIsExists(new File(path));
                if (!isExists) {
                    // 下载
                    progressDialog.show();
                    FileDownloader.downloadFile(gridMedia.getUrl(), fileFullPath[0], fileFullPath[1], new DownloadProgressHandler() {

                        @Override
                        public void onProgress(DownloadInfo downloadInfo) {

                        }

                        @Override
                        public void onCompleted(File file) {
                            mBinding.gridView.setItemCover(gridMedia, file.getPath());
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
                    mBinding.gridView.setItemCover(gridMedia, path);
                    // 赋值本地播放地址后,返回true是可以继续播放的播放事件
                    return true;
                }
            }

            /** @noinspection unused*/
            @Override
            public void onItemStartUploading(@NonNull GridMedia gridMedia, @NonNull GridAdapter.PhotoViewHolder viewHolder) {
                // 开始模拟上传 - 指刚添加后的。这里可以使用你自己的上传事件
                MyTask timer = new MyTask(gridMedia);
                timers.put(gridMedia, timer);
                timer.schedule();
            }

            /** @noinspection unused*/
            @Override
            public void onItemClose(@NonNull GridMedia gridMedia) {
                // 停止上传
                MyTask myTask = timers.get(gridMedia);
                if (myTask != null) {
                    myTask.cancel();
                    timers.remove(gridMedia);
                }
            }

            /** @noinspection unused*/
            @Override
            public void onItemClick(@NonNull View view, @NonNull GridMedia gridMedia) {
                // 点击详情,通过网页形式加载的数据，是加载不了详情数据的
                Log.i(TAG, "onResult id:" + gridMedia.getId());
                Log.i(TAG, "onResult url:" + gridMedia.getUrl());
                Log.d(TAG, "onResult 绝对路径:" + gridMedia.getAbsolutePath());
                Log.d(TAG, "onResult Uri:" + gridMedia.getUri());
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
                // 点击详情
                mGlobalSetting.openPreviewData(MainSeeLocalActivity.this, requestLauncherGrid, mBinding.gridView.getAllData(), mBinding.gridView.getAllData().indexOf(gridMedia), mBinding.gridView.isOperation());
            }

            /** @noinspection unused*/
            @Override
            public void onItemAdd(@NonNull View view, @NonNull GridMedia gridMedia, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击添加
                openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
            }

        }));
        initConfig();
        initData();
        initListener();
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
                // 是否显示多选图片的数字
                .countable(true)
                // 自定义过滤器
                .addFilter(new GifSizeFilter(320, 320, 5 * BaseFilter.K * BaseFilter.K))
                // 九宫格大小
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                // 图片缩放比例
                .thumbnailScale(0.85f)
                .setOnSelectedListener(localFiles -> {
                    // 每次选择的事件
                    Log.d("onSelected", "onSelected: localFiles.size()=" + localFiles.size());
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
        mGlobalSetting = MultiMediaSetting.from(MainSeeLocalActivity.this)
                .choose(MimeType.ofAll())
                .albumSetting(albumSetting)
                .cameraSetting(cameraSetting)
                .recorderSetting(recorderSetting)
                //  .imageEngine(new GlideEngine())  // for glide-V3     // for glide-V4
                .imageEngine(new Glide4Engine());
    }

    /**
     * 初始化数据 - 这些初始化数据建议加入异步
     * 添加的这个本地地址自行修改,如果本地手机不存在该文件,app是不会添加的
     */
    private void initData() {
        // 这是演示是否可操作九宫格控件
        mBinding.gridView.setOperation(false);
        // 先把assets的文件拷贝到手机
        copyAssetsFileToInternalStorage(getApplicationContext(), "test_image.jpeg", "a.jpg", getApplicationContext().getCacheDir().getPath());
        copyAssetsFileToInternalStorage(getApplicationContext(), "test_image.jpeg", "b.jpg", getApplicationContext().getCacheDir().getPath());
        copyAssetsFileToInternalStorage(getApplicationContext(), "test_video.mp4", "c.mp4", getApplicationContext().getCacheDir().getPath());
        copyAssetsFileToInternalStorage(getApplicationContext(), "test_video.mp4", "d.mp4", getApplicationContext().getCacheDir().getPath());
        copyAssetsFileToInternalStorage(getApplicationContext(), "test_audio.aac", "e.aac", getApplicationContext().getCacheDir().getPath());
        copyAssetsFileToInternalStorage(getApplicationContext(), "test_audio.aac", "f.aac", getApplicationContext().getCacheDir().getPath());

        List<GridMedia> data = new ArrayList<>();

        // 图片数据
        List<String> imagePaths = new ArrayList<>();
        imagePaths.add(getApplicationContext().getCacheDir().getPath() + "/a.jpg");
        imagePaths.add(getApplicationContext().getCacheDir().getPath() + "/b.jpg");
        for (String path : imagePaths) {
            File file = new File(path);
            boolean isExists = fileIsExists(file);
            if (isExists) {
                GridMedia gridMedia = new GridMedia(MimeType.JPEG.getMimeTypeName());
                gridMedia.setAbsolutePath(path);
                MediaExtraInfo mediaExtraInfo = getVideoSize(getApplicationContext(), path);
                gridMedia.setWidth(mediaExtraInfo.getWidth());
                gridMedia.setHeight(mediaExtraInfo.getHeight());
                gridMedia.setSize(file.length());
                gridMedia.setUri(Uri.fromFile(file).toString());
                gridMedia.setAbsolutePath(path);
                gridMedia.setUploading(false);
                data.add(gridMedia);
            }
        }

        // 视频数据
        List<String> videoPaths = new ArrayList<>();
        videoPaths.add(getApplicationContext().getCacheDir().getPath() + "/c.mp4");
        videoPaths.add(getApplicationContext().getCacheDir().getPath() + "/d.mp4");
        for (String path : videoPaths) {
            File file = new File(path);
            boolean isExists = fileIsExists(file);
            if (isExists) {
                GridMedia gridMedia = new GridMedia(MimeType.MP4.getMimeTypeName());
                gridMedia.setAbsolutePath(path);
                MediaExtraInfo mediaExtraInfo = getVideoSize(getApplicationContext(), path);
                gridMedia.setWidth(mediaExtraInfo.getWidth());
                gridMedia.setHeight(mediaExtraInfo.getHeight());
                gridMedia.setDuration(mediaExtraInfo.getDuration());
                gridMedia.setSize(file.length());
                gridMedia.setUri(Uri.fromFile(file).toString());
                gridMedia.setAbsolutePath(path);
                gridMedia.setUploading(false);
                data.add(gridMedia);
            }
        }

        // 音频数据
        List<String> audioPaths = new ArrayList<>();
        audioPaths.add(getApplicationContext().getCacheDir().getPath() + "/e.aac");
        audioPaths.add(getApplicationContext().getCacheDir().getPath() + "/f.aac");
        for (String path : audioPaths) {
            File file = new File(path);
            boolean isExists = fileIsExists(file);
            if (isExists) {
                GridMedia gridMedia = new GridMedia(MimeType.AAC.getMimeTypeName());
                gridMedia.setAbsolutePath(path);
                MediaExtraInfo mediaExtraInfo = getVideoSize(getApplicationContext(), path);
                gridMedia.setWidth(mediaExtraInfo.getWidth());
                gridMedia.setHeight(mediaExtraInfo.getHeight());
                gridMedia.setDuration(mediaExtraInfo.getDuration());
                gridMedia.setSize(file.length());
                gridMedia.setUri(Uri.fromFile(file).toString());
                gridMedia.setAbsolutePath(path);
                gridMedia.setUploading(false);
                data.add(gridMedia);
            }
        }

        // 添加数据
        mBinding.gridView.setData(data);
    }

    /**
     * 通过url加入的
     */
    private void initListener() {
        findViewById(R.id.btnSetValue).setOnClickListener(view -> initData());
        findViewById(R.id.btnReset).setOnClickListener(view -> mBinding.gridView.reset());
        findViewById(R.id.btnGetValue).setOnClickListener(v -> {
            ArrayList<GridMedia> value = mBinding.gridView.getAllData();
            for (GridMedia item : value) {
                Log.i(TAG, "onResult id:" + item.getId());
                Log.i(TAG, "onResult url:" + item.getUrl());
                Log.d(TAG, "onResult 绝对路径:" + item.getAbsolutePath());
                Log.d(TAG, "onResult Uri:" + item.getUri());
                Log.d(TAG, "onResult 文件大小: " + item.getSize());
                Log.d(TAG, "onResult 视频音频长度: " + item.getDuration());
            }
        });
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
                .forResult(requestLauncherACR);
    }

    /**
     * 返回文件路径
     *
     * @param url  网址
     * @param type 0是mp3,1是mp4
     * @noinspection SameParameterValue
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

        return new String[]{MainSeeLocalActivity.this.getCacheDir().getPath(), fileName};
    }

    /**
     * 判断是否存在文件
     *
     * @param file 文件
     */
    public boolean fileIsExists(File file) {
        try {
            if (!file.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * @noinspection ResultOfMethodCallIgnored
     */
    public static void copyAssetsFileToInternalStorage(Context context, String fileName, String newFileName, String targetDir) {
        try {
            // 打开assets目录中的文件
            InputStream inputStream = context.getAssets().open(fileName);
            // 创建目标文件路径
            File targetFile = new File(targetDir, newFileName);
            if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs(); // 创建目标目录
            }
            // 写入文件到目标路径
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            // 关闭流
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            Log.i("CopyAssets", "文件复制成功: " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e("CopyAssets", "文件复制失败: " + e.getMessage());
        }
    }

}
