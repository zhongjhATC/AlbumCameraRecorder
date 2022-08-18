package com.zhongjh.albumcamerarecorder.recorder;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION_LOCAL_FILE;
import static com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_ONLY_LONG_CLICK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.zhongjh.albumcamerarecorder.BaseFragment;
import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;
import com.zhongjh.albumcamerarecorder.recorder.widget.SoundRecordingLayout;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.settings.RecordeSpec;
import com.zhongjh.albumcamerarecorder.widget.BaseOperationLayout;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.StatusBarUtils;
import com.zhongjh.common.utils.ThreadUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 录音
 *
 * @author zhongjh
 * @date 2018/8/22
 */
public class SoundRecordingFragment extends BaseFragment {

    private static final String TAG = SoundRecordingFragment.class.getSimpleName();
    /**
     * 再次确定的2秒时间
     */
    private final static int AGAIN_TIME = 2000;
    /**
     * 满进度
     */
    private final static int FULL = 100;
    protected Activity mActivity;
    private Context mContext;

    /**
     * 公共配置
     */
    private GlobalSpec mGlobalSpec;
    private RecordeSpec mRecordSpec;
    private MediaStoreCompat mAudioMediaStoreCompat;

    /**
     * 是否正在播放中
     */
    private boolean isPlaying = false;
    private ViewHolder mViewHolder;

    /**
     * 存储用户单击暂停按钮的时间
     */
    long timeWhenPaused = 0;

    private MediaPlayer mMediaPlayer = null;
    /**
     * 存储的数据
     */
    LocalFile localFile;

    /**
     * 声明一个long类型变量：用于存放上一点击“返回键”的时刻
     */
    private long mExitTime;

    // region 有关录音配置

    private File mFile = null;

    private MediaRecorder mRecorder = null;

    private long mStartingTimeMillis = 0;
    // endregion

    /**
     * 停止录音时的异步线程
     */
    ThreadUtils.SimpleTask<Boolean> mStopRecordingTask;

    public static SoundRecordingFragment newInstance() {
        return new SoundRecordingFragment();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewHolder = new ViewHolder(inflater.inflate(R.layout.fragment_soundrecording_zjh, container, false));

        // 处理图片、视频等需要进度显示
        mViewHolder.pvLayout.getViewHolder().btnConfirm.setProgressMode(true);

        // 初始化设置
        mGlobalSpec = GlobalSpec.INSTANCE;
        mRecordSpec = RecordeSpec.INSTANCE;
        // 提示文本
        mViewHolder.pvLayout.setTip(getResources().getString(R.string.z_multi_library_long_press_sound_recording));
        // 设置录制时间
        mViewHolder.pvLayout.setDuration(mRecordSpec.getDuration() * 1000);
        // 最短录制时间
        mViewHolder.pvLayout.setMinDuration(mRecordSpec.getMinDuration());
        // 设置只能长按
        mViewHolder.pvLayout.setButtonFeatures(BUTTON_STATE_ONLY_LONG_CLICK);

        // 兼容沉倾状态栏
        int statusBarHeight = StatusBarUtils.getStatusBarHeight(mContext);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mViewHolder.chronometer.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + statusBarHeight, layoutParams.rightMargin, layoutParams.bottomMargin);

        initListener();
        return mViewHolder.rootView;
    }

    @Override
    public boolean onBackPressed() {
        // 判断当前状态是否休闲
        if (mViewHolder.pvLayout.mState == SoundRecordingLayout.STATE_PREVIEW) {
            return false;
        } else {
            // 与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > AGAIN_TIME) {
                // 大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(mActivity.getApplicationContext(), getResources().getString(R.string.z_multi_library_press_confirm_again_to_close), Toast.LENGTH_SHORT).show();
                // 并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 事件
     */
    private void initListener() {
        // 录音等事件
        initPvLayoutPhotoVideoListener();
        // 播放事件
        initRlSoundRecordingClickListener();

        // 确认和取消
        initPvLayoutOperateListener();
    }

    /**
     * 录音等事件
     */
    private void initPvLayoutPhotoVideoListener() {
        mViewHolder.pvLayout.setPhotoVideoListener(new ClickOrLongListener() {
            @Override
            public void actionDown() {
                // 母窗体禁止滑动
                ((MainActivity) mActivity).showHideTableLayout(false);
            }

            @Override
            public void onClick() {

            }

            @Override
            public void onLongClickShort(long time) {
                Log.d(TAG, "onLongClickShort" + time);
                mViewHolder.pvLayout.setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_the_recording_time_is_too_short));  // 提示过短
                // 停止录音
                new Handler(Looper.getMainLooper()).postDelayed(() -> onRecord(false, true), mRecordSpec.getMinDuration() - time);
                mViewHolder.chronometer.setBase(SystemClock.elapsedRealtime());
                // 母窗体启动滑动
                ((MainActivity) mActivity).showHideTableLayout(true);
            }

            @Override
            public void onLongClick() {
                Log.d(TAG, "onLongClick");
                // 录音开启
                onRecord(true, false);
            }

            @Override
            public void onLongClickEnd(long time) {
                mViewHolder.pvLayout.hideBtnClickOrLong();
                mViewHolder.pvLayout.startShowLeftRightButtonsAnimator();
                Log.d(TAG, "onLongClickEnd");
                // 录音结束
                onRecord(false, false);
                showRecordEndView();
            }

            @Override
            public void onLongClickError() {

            }

            @Override
            public void onBanClickTips() {

            }

            @Override
            public void onClickStopTips() {

            }
        });
    }

    /**
     * 播放事件
     */
    private void initRlSoundRecordingClickListener() {
        mViewHolder.pvLayout.getViewHolder().rlSoundRecording.setOnClickListener(view -> {
            initAudio();
            // 播放
            onPlay(isPlaying);
            isPlaying = !isPlaying;
        });
    }

    /**
     * 确认和取消
     */
    private void initPvLayoutOperateListener() {
        mViewHolder.pvLayout.setOperateListener(new BaseOperationLayout.OperateListener() {
            @Override
            public void cancel() {
                // 母窗体启动滑动
                ((MainActivity) mActivity).showHideTableLayout(true);
                // 重置取消确认按钮
                mViewHolder.pvLayout.reset();
                // 重置时间
                mViewHolder.chronometer.setBase(SystemClock.elapsedRealtime());
            }

            @Override
            public void confirm() {
            }

            @Override
            public void startProgress() {
                moveRecordFile();
            }

            @Override
            public void stopProgress() {

            }

            @Override
            public void doneProgress() {

            }
        });
    }

    /**
     * 初始化音频的数据
     */
    private void initAudio() {
        // 获取service存储的数据
        localFile = new LocalFile();
        SharedPreferences sharePreferences = mActivity.getSharedPreferences("sp_name_audio", MODE_PRIVATE);
        final String filePath = sharePreferences.getString("audio_path", "");
        long elapsed = sharePreferences.getLong("elapsed", 0);
        localFile.setPath(filePath);
        localFile.setDuration(elapsed);
        localFile.setSize(new File(filePath).length());
        localFile.setMimeType(MimeType.AAC.getMimeTypeName());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            stopPlaying();
        }
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            stopPlaying();
        }
        mMoveRecordFileTask.cancel();
        if (mStopRecordingTask != null) {
            mStopRecordingTask.cancel();
        }
        super.onDestroy();
    }


    /**
     * 录音开始或者停止
     * // recording pause
     *
     * @param start   录音开始或者停止
     * @param isShort 短时结束不算
     */
    private void onRecord(boolean start, boolean isShort) {
        if (start) {
            // 创建文件
            File folder = new File(mActivity.getExternalFilesDir(null) + "/SoundRecorder");
            if (!folder.exists()) {
                // folder /SoundRecorder doesn't exist, create the folder
                boolean wasSuccessful = folder.mkdir();
                if (!wasSuccessful) {
                    System.out.println("was not successful.");
                }
            }
            // 开始计时,从1秒开始算起
            mViewHolder.chronometer.setBase(SystemClock.elapsedRealtime() - 1000);
            mViewHolder.chronometer.start();

            // start RecordingService
            startRecording();
            // keep screen on while recording
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            mViewHolder.chronometer.stop();
            timeWhenPaused = 0;

            stopRecording(isShort);
            // allow the screen to turn off again once recording is finished
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * 播放开始或者停止
     * // Play start/stop
     *
     * @param isPlaying 播放或者停止
     */
    private void onPlay(boolean isPlaying) {
        if (!isPlaying) {
            // currently MediaPlayer is not playing audio
            if (mMediaPlayer == null) {
                startPlaying(); // 第一次播放
            } else {
                resumePlaying(); // 恢复当前暂停的媒体播放器
            }

        } else {
            // 暂停播放
            pausePlaying();
        }
    }

    /**
     * 播放MediaPlayer
     */
    private void startPlaying() {
        // 变成等待的图标
        mViewHolder.pvLayout.getViewHolder().ivRecord.setImageResource(R.drawable.ic_pause_white_24dp);
        mMediaPlayer = new MediaPlayer();

        try {
            // 文件地址
            mMediaPlayer.setDataSource(localFile.getPath());
            mMediaPlayer.prepare();

            mMediaPlayer.setOnPreparedListener(mp -> mMediaPlayer.start());
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mMediaPlayer.setOnCompletionListener(mp -> stopPlaying());

        //keep screen on while playing audio
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 恢复播放
     */
    private void resumePlaying() {
        // 暂停图
        mViewHolder.pvLayout.getViewHolder().ivRecord.setImageResource(R.drawable.ic_pause_white_24dp);
        mMediaPlayer.start();
    }

    /**
     * 暂停播放
     */
    private void pausePlaying() {
        // 设置成播放的图片
        mViewHolder.pvLayout.getViewHolder().ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        mMediaPlayer.pause();
    }

    /**
     * 停止播放
     */
    private void stopPlaying() {
        // 设置成播放的图片
        mViewHolder.pvLayout.getViewHolder().ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        // 停止mediaPlayer
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        isPlaying = !isPlaying;

        // 一旦音频播放完毕，保持屏幕常亮 这个设置关闭
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 显示录音后的界面
     */
    private void showRecordEndView() {
        // 录音按钮转变成播放按钮，播放录音
        mViewHolder.pvLayout.getViewHolder().ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp);
    }

    /**
     * 迁移语音文件
     */
    private void moveRecordFile() {
        // 执行等待动画
        mViewHolder.pvLayout.getViewHolder().btnConfirm.setProgress(1);
        // 开始迁移文件
        ThreadUtils.executeByIo(mMoveRecordFileTask);
    }

    /**
     * 迁移语音的异步线程
     */
    private final ThreadUtils.SimpleTask<Void> mMoveRecordFileTask = new ThreadUtils.SimpleTask<Void>() {
        @Override
        public Void doInBackground() {
            if (localFile == null) {
                initAudio();
            }
            if (localFile.getPath() != null) {
                // 初始化保存好的音频文件
                initAudio();
                // 获取文件名称
                String newFileName = localFile.getPath().substring(localFile.getPath().lastIndexOf(File.separator));
                File newFile = mAudioMediaStoreCompat.createFile(newFileName, 2, false);
                Log.d(TAG, "newFile" + newFile.getAbsolutePath());
                FileUtil.copy(new File(localFile.getPath()), newFile, null, (ioProgress, file) -> {
                    int progress = (int) (ioProgress * FULL);
                    ThreadUtils.runOnUiThread(() -> {
                        mViewHolder.pvLayout.getViewHolder().btnConfirm.addProgress(progress);
                        localFile.setPath(newFile.getPath());
                        localFile.setUri(mAudioMediaStoreCompat.getUri(newFile.getPath()));
                        if (progress >= FULL) {
                            if (mGlobalSpec.getOnResultCallbackListener() == null) {
                                Intent result = new Intent();
                                ArrayList<LocalFile> localFiles = new ArrayList<>();
                                localFiles.add(localFile);
                                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION_LOCAL_FILE, localFiles);
                                mActivity.setResult(RESULT_OK, result);
                            } else {
                                ArrayList<LocalFile> localFiles = new ArrayList<>();
                                localFiles.add(localFile);
                                mGlobalSpec.getOnResultCallbackListener().onResult(localFiles);
                            }
                            mActivity.finish();
                        }
                    });
                });
            }
            return null;
        }

        @Override
        public void onSuccess(Void result) {

        }
    };

    // region 有关录音相关方法

    /**
     * 开始录音
     */
    private void startRecording() {
        // 设置音频路径
        if (mGlobalSpec.getAudioStrategy() != null) {
            // 如果设置了音频的文件夹路径，就使用它的
            mAudioMediaStoreCompat = new MediaStoreCompat(mContext, mGlobalSpec.getAudioStrategy());
        } else {
            // 否则使用全局的
            if (mGlobalSpec.getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mAudioMediaStoreCompat = new MediaStoreCompat(mContext, mGlobalSpec.getSaveStrategy());
            }
        }
        mFile = mAudioMediaStoreCompat.createFile(2, true, "aac");

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mRecorder.setOutputFile(mFile.getPath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

            //startTimer();
            //startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    /**
     * 停止录音
     *
     * @param isShort 短时结束不算
     */
    private void stopRecording(boolean isShort) {
        mViewHolder.pvLayout.setEnabled(false);
        ThreadUtils.executeByIo(getStopRecordingTask(isShort));
    }

    /**
     * 停止录音的异步线程
     *
     * @param isShort 短时结束不算
     */
    private ThreadUtils.SimpleTask<Boolean> getStopRecordingTask(boolean isShort) {
        mStopRecordingTask = new ThreadUtils.SimpleTask<Boolean>() {
            @Override
            public Boolean doInBackground() {
                if (isShort) {
                    // 如果是短时间的，删除该文件
                    if (mFile.exists()) {
                        boolean delete = mFile.delete();
                        if (!delete) {
                            System.out.println("file not delete.");
                        }
                    }
                } else {
                    long mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
                    // 存储到缓存的文件地址
                    mActivity.getSharedPreferences("sp_name_audio", MODE_PRIVATE)
                            .edit()
                            .putString("audio_path", mFile.getPath())
                            .putLong("elapsed", mElapsedMillis)
                            .apply();
                }
                if (mRecorder != null) {
                    try {
                        mRecorder.stop();
                    } catch (RuntimeException ignored) {
                        // 防止立即录音完成
                    }
                    mRecorder.release();
                    mRecorder = null;
                }
                return true;
            }

            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    mViewHolder.pvLayout.setEnabled(true);
                }
            }
        };
        return mStopRecordingTask;
    }


    // endregion

    public static class ViewHolder {
        View rootView;
        public Chronometer chronometer;
        public SoundRecordingLayout pvLayout;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.chronometer = rootView.findViewById(R.id.chronometer);
            this.pvLayout = rootView.findViewById(R.id.pvLayout);
        }

    }
}
