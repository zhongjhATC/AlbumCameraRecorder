package com.zhongjh.albumcamerarecorder.camera.ui.previewvideo;

import static com.zhongjh.albumcamerarecorder.utils.MediaStoreUtils.MediaTypes.TYPE_VIDEO;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.MediaStoreUtils;
import com.zhongjh.albumcamerarecorder.widget.progressbutton.CircularProgressButton;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MediaExtraInfo;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.common.listener.VideoEditListener;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.MediaUtils;
import com.zhongjh.common.utils.StatusBarUtils;
import com.zhongjh.common.utils.ThreadUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;


/**
 * 一个预览合成分段录制的视频
 *
 * @author zhongjh
 */
public class PreviewVideoActivity extends AppCompatActivity {

    private static final String TAG = PreviewVideoActivity.class.getSimpleName();

    public static final String LOCAL_FILE = "LOCAL_FILE";
    static final String PATH = "PATH";

    VideoView mVideoViewPreview;
    ImageView mImgClose;
    CircularProgressButton mBtnConfirm;
    /**
     * 该视频的相关参数
     */
    LocalFile mLocalFile = new LocalFile();
    /**
     * 按钮事件运行中，因为该自定义控件如果通过setEnabled控制会导致动画不起效果，所以需要该变量控制按钮事件是否生效
     */
    boolean mIsRun;

    /**
     * 录像文件配置路径
     */
    private MediaStoreCompat mVideoMediaStoreCompat;
    /**
     * 拍摄配置
     */
    GlobalSpec mGlobalSpec = GlobalSpec.INSTANCE;
    /**
     * 迁移视频的异步线程
     */
    private ThreadUtils.SimpleTask<File> mMoveVideoFileTask;

    /**
     * 打开activity
     *
     * @param fragment 打开者
     * @param path     视频地址
     */
    public static void startActivity(Fragment fragment, ActivityResultLauncher<Intent> previewVideoActivityResult, String path) {
        Intent intent = new Intent();
        intent.putExtra(PATH, path);
        intent.setClass(fragment.getContext(), PreviewVideoActivity.class);
        previewVideoActivityResult.launch(intent);
        if (fragment.getActivity() != null) {
            fragment.getActivity().overridePendingTransition(R.anim.activity_open_zjh, 0);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(GlobalSpec.INSTANCE.getOrientation());
        setTheme(mGlobalSpec.getThemeId());
        StatusBarUtils.initStatusBar(PreviewVideoActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_video_zjh);
        mLocalFile.setPath(getIntent().getStringExtra(PATH));
        initView();
        initListener();
        initData();
    }

    @Override
    public void finish() {
        //关闭窗体动画显示
        this.overridePendingTransition(0, R.anim.activity_close_zjh);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        if (mGlobalSpec.isCompressEnable() && mGlobalSpec.getVideoCompressCoordinator() != null) {
            mGlobalSpec.getVideoCompressCoordinator().onCompressDestroy(PreviewVideoActivity.this.getClass());
            mGlobalSpec.setVideoCompressCoordinator(null);
        }
        if (mMoveVideoFileTask != null) {
            mMoveVideoFileTask.cancel();
        }
        super.onDestroy();
    }

    /**
     * 初始化View
     */
    private void initView() {
        mVideoViewPreview = findViewById(R.id.vvPreview);
        mImgClose = findViewById(R.id.imgClose);
        mBtnConfirm = findViewById(R.id.btnConfirm);
        mBtnConfirm.setIndeterminateProgressMode(true);
    }

    private void initListener() {
        mBtnConfirm.setOnClickListener(v -> {
            if (mIsRun) {
                return;
            }
            mIsRun = true;
            confirm();
        });
        mImgClose.setOnClickListener(v -> PreviewVideoActivity.this.finish());
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 设置视频路径
        if (mGlobalSpec.getVideoStrategy() != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mVideoMediaStoreCompat = new MediaStoreCompat(PreviewVideoActivity.this, mGlobalSpec.getVideoStrategy());
        } else {
            // 否则使用全局的
            if (mGlobalSpec.getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mVideoMediaStoreCompat = new MediaStoreCompat(PreviewVideoActivity.this, mGlobalSpec.getSaveStrategy());
            }
        }

        if (mLocalFile.getPath() != null) {
            File file = new File(mLocalFile.getPath());
            Log.d(TAG, "exists:" + file.exists() + " length:" + file.length());
            playVideo(file);
        }
    }

    /**
     * 播放视频,用于录制后，在是否确认的界面中，播放视频
     */
    private void playVideo(File file) {
        mVideoViewPreview.pause();
        // mediaController 是底部控制条
        MediaController mediaController = new MediaController(PreviewVideoActivity.this);
        mediaController.setAnchorView(mVideoViewPreview);
        mediaController.setMediaPlayer(mVideoViewPreview);
        mediaController.setVisibility(View.GONE);
        mVideoViewPreview.setMediaController(mediaController);
        Uri uri = Uri.fromFile(file);
        mVideoViewPreview.setVideoURI(uri);
        // 这段代码需要放在更新视频文件后播放，不然会找不到文件。
        mVideoViewPreview.setVisibility(View.VISIBLE);
        if (!mVideoViewPreview.isPlaying()) {
            mVideoViewPreview.start();
        }
        mVideoViewPreview.setOnPreparedListener(mp -> {
            // 获取相关参数
            mLocalFile.setDuration(mVideoViewPreview.getDuration());
        });
        mVideoViewPreview.setOnCompletionListener(mediaPlayer -> {
            // 循环播放
            if (!mVideoViewPreview.isPlaying()) {
                mVideoViewPreview.start();
            }
        });
    }

    /**
     * 提交
     */
    private void confirm() {
        // 判断是否开启了视频压缩功能
        if (mGlobalSpec.isCompressEnable()) {
            // 如果开启了直接压缩
            compress();
        } else {
            // 否则直接转移
            moveVideoFile();
        }
    }

    /**
     * 压缩视频
     */
    private void compress() {
        if (mLocalFile.getPath() != null && mGlobalSpec.getVideoCompressCoordinator() != null) {
            // 获取文件名称
            String newFileName = mLocalFile.getPath().substring(mLocalFile.getPath().lastIndexOf(File.separator));
            File newFile = mVideoMediaStoreCompat.createFile(newFileName, 1, false);
            mGlobalSpec.getVideoCompressCoordinator().setVideoCompressListener(PreviewVideoActivity.this.getClass(), new VideoEditListener() {
                @Override
                public void onFinish() {
                    confirm(newFile);
                }

                @Override
                public void onProgress(int progress, long progressTime) {
                    mBtnConfirm.setProgress(progress);
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(@NotNull String message) {
                    mIsRun = false;
                }
            });
            if (mLocalFile.getPath() != null && mGlobalSpec.getVideoCompressCoordinator() != null) {
                mGlobalSpec.getVideoCompressCoordinator().compressRxJava(PreviewVideoActivity.this.getClass(), mLocalFile.getPath(), newFile.getPath());
            }
        }
    }

    /**
     * 迁移视频文件，缓存文件迁移到配置目录
     */
    private void moveVideoFile() {
        Log.d(TAG, "moveVideoFile");
        // 执行等待动画
        mBtnConfirm.setProgress(50);
        // 开始迁移文件，将 缓存文件 拷贝到 配置目录
        ThreadUtils.executeByIo(getMoveVideoFileTask());
    }

    /**
     * 迁移视频的异步线程
     */
    private ThreadUtils.SimpleTask<File> getMoveVideoFileTask() {
        mMoveVideoFileTask = new ThreadUtils.SimpleTask<File>() {
            @Override
            public File doInBackground() {
                if (mLocalFile.getPath() == null) {
                    return null;
                }
                // 获取文件名称
                String newFileName = mLocalFile.getPath().substring(mLocalFile.getPath().lastIndexOf(File.separator));
                File newFile = mVideoMediaStoreCompat.createFile(newFileName, 1, false);
                FileUtil.move(new File(mLocalFile.getPath()), newFile);
                return newFile;
            }

            @Override
            public void onSuccess(File newFile) {
                if (newFile.exists()) {
                    mBtnConfirm.setProgress(100);
                    confirm(newFile);
                } else {
                    mBtnConfirm.setProgress(0);
                }
                mIsRun = false;
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                mIsRun = false;
            }
        };
        return mMoveVideoFileTask;
    }

    /**
     * 确定该视频
     */
    private void confirm(File newFile) {
        Intent intent = new Intent();
        MediaExtraInfo mediaExtraInfo = MediaUtils.getVideoSize(getApplicationContext(), newFile.getPath());
        mLocalFile.setWidth(mediaExtraInfo.getWidth());
        mLocalFile.setHeight(mediaExtraInfo.getHeight());
        Uri uri = MediaStoreUtils.displayToGallery(getApplicationContext(), newFile, TYPE_VIDEO, mLocalFile.getDuration(),
                mLocalFile.getWidth(), mLocalFile.getHeight(),
                mVideoMediaStoreCompat.getSaveStrategy().getDirectory(), mVideoMediaStoreCompat);
        // 加入相册后的最后是id，直接使用该id
        mLocalFile.setId(MediaStoreUtils.getId(uri));
        mLocalFile.setPath(newFile.getPath());
        mLocalFile.setUri(mVideoMediaStoreCompat.getUri(newFile.getPath()));
        mLocalFile.setSize(newFile.length());
        mLocalFile.setMimeType(MimeType.MP4.getMimeTypeName());
        intent.putExtra(LOCAL_FILE, mLocalFile);
        setResult(RESULT_OK, intent);
        PreviewVideoActivity.this.finish();
    }

}
