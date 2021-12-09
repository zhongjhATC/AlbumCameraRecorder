package com.zhongjh.albumcamerarecorder.recorder;

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
import com.zhongjh.albumcamerarecorder.recorder.db.RecordingItem;
import com.zhongjh.albumcamerarecorder.recorder.widget.SoundRecordingLayout;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.settings.RecordeSpec;
import com.zhongjh.albumcamerarecorder.utils.ViewBusinessUtils;
import com.zhongjh.albumcamerarecorder.widget.BaseOperationLayout;

import java.io.File;
import java.io.IOException;

import com.zhongjh.common.enums.MultimediaTypes;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.StatusBarUtils;
import com.zhongjh.common.utils.ThreadUtils;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_MULTIMEDIA_CHOICE;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_MULTIMEDIA_TYPES;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_RECORDING_ITEM;
import static com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_ONLY_LONG_CLICK;

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
    protected Activity mActivity;
    private Context mContext;

    RecordeSpec mRecordSpec;
    MediaStoreCompat mAudioMediaStoreCompat;

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
    RecordingItem recordingItem;

    /**
     * 声明一个long类型变量：用于存放上一点击“返回键”的时刻
     */
    private long mExitTime;

    // region 有关录音配置

    private File mFile = null;

    private MediaRecorder mRecorder = null;

    private long mStartingTimeMillis = 0;
    // endregion

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
        mRecordSpec = RecordeSpec.getInstance();
        // 提示文本
        mViewHolder.pvLayout.setTip(getResources().getString(R.string.z_multi_library_long_press_sound_recording));
        // 设置录制时间
        mViewHolder.pvLayout.setDuration(mRecordSpec.duration * 1000);
        // 最短录制时间
        mViewHolder.pvLayout.setMinDuration(mRecordSpec.minDuration);
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
                ViewBusinessUtils.setTabLayoutScroll(false, ((MainActivity) mActivity), mViewHolder.pvLayout);
            }

            @Override
            public void onClick() {

            }

            @Override
            public void onLongClickShort(long time) {
                Log.d(TAG, "onLongClickShort" + time);
                mViewHolder.pvLayout.setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_the_recording_time_is_too_short));  // 提示过短
                // 停止录音
                new Handler(Looper.getMainLooper()).postDelayed(() -> onRecord(false, true), mRecordSpec.minDuration - time);
                mViewHolder.chronometer.setBase(SystemClock.elapsedRealtime());
                // 母窗体启动滑动
                ViewBusinessUtils.setTabLayoutScroll(true, ((MainActivity) mActivity), mViewHolder.pvLayout);
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
        ((SoundRecordingLayout.ViewHolder) mViewHolder.pvLayout.viewHolder).rlSoundRecording.setOnClickListener(view -> {
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
                ViewBusinessUtils.setTabLayoutScroll(true, ((MainActivity) mActivity), mViewHolder.pvLayout);
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
        recordingItem = new RecordingItem();
        SharedPreferences sharePreferences = mActivity.getSharedPreferences("sp_name_audio", MODE_PRIVATE);
        final String filePath = sharePreferences.getString("audio_path", "");
        long elapsed = sharePreferences.getLong("elapsed", 0);
        recordingItem.setFilePath(filePath);
        recordingItem.setLength((int) elapsed);
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
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopPlaying();
        }
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
        ((SoundRecordingLayout.ViewHolder) mViewHolder.pvLayout.viewHolder).ivRecord.setImageResource(R.drawable.ic_pause_white_24dp);
        mMediaPlayer = new MediaPlayer();

        try {
            // 文件地址
            mMediaPlayer.setDataSource(recordingItem.getFilePath());
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
        ((SoundRecordingLayout.ViewHolder) mViewHolder.pvLayout.viewHolder).ivRecord.setImageResource(R.drawable.ic_pause_white_24dp);
        mMediaPlayer.start();
    }

    /**
     * 暂停播放
     */
    private void pausePlaying() {
        // 设置成播放的图片
        ((SoundRecordingLayout.ViewHolder) mViewHolder.pvLayout.viewHolder).ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        mMediaPlayer.pause();
    }

    /**
     * 停止播放
     */
    private void stopPlaying() {
        // 设置成播放的图片
        ((SoundRecordingLayout.ViewHolder) mViewHolder.pvLayout.viewHolder).ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp);
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
        ((SoundRecordingLayout.ViewHolder) mViewHolder.pvLayout.viewHolder).ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp);
    }

    /**
     * 迁移语音文件
     */
    private void moveRecordFile() {
        // 执行等待动画
        mViewHolder.pvLayout.getViewHolder().btnConfirm.setProgress(1);
        // 开始迁移文件
        ThreadUtils.executeByIo(new ThreadUtils.BaseSimpleBaseTask<Void>() {
            @Override
            public Void doInBackground() {
                // 初始化保存好的音频文件
                initAudio();
                // 获取文件名称
                String newFileName = recordingItem.getFilePath().substring(recordingItem.getFilePath().lastIndexOf(File.separator));
                File newFile = mAudioMediaStoreCompat.createFile(newFileName, 2, false);
                Log.d(TAG, "newFile" + newFile.getAbsolutePath());
                FileUtil.copy(new File(recordingItem.getFilePath()), newFile, null, (ioProgress, file) -> {
                    int progress = (int) (ioProgress * 100);
                    ThreadUtils.runOnUiThread(() -> {
                        mViewHolder.pvLayout.getViewHolder().btnConfirm.addProgress(progress);
                        recordingItem.setFilePath(newFile.getPath());
                        if (progress >= 100) {
                            // 完成 获取音频路径
                            Intent result = new Intent();
                            result.putExtra(EXTRA_RESULT_RECORDING_ITEM, recordingItem);
                            result.putExtra(EXTRA_MULTIMEDIA_TYPES, MultimediaTypes.AUDIO);
                            result.putExtra(EXTRA_MULTIMEDIA_CHOICE, false);
                            mActivity.setResult(RESULT_OK, result);
                            mActivity.finish();
                        }
                    });
                });
                return null;
            }

            @Override
            public void onSuccess(Void result) {

            }
        });
    }

    // region 有关录音相关方法

    /**
     * 开始录音
     */
    private void startRecording() {

        // 根据配置创建文件配置
        GlobalSpec globalSpec = GlobalSpec.getInstance();
        // 音频文件配置路径
        mAudioMediaStoreCompat = new MediaStoreCompat(mContext,
                globalSpec.audioStrategy == null ? globalSpec.saveStrategy : globalSpec.audioStrategy);
        mFile = mAudioMediaStoreCompat.createFile(2, true);

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
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

        ThreadUtils.executeByIo(new ThreadUtils.BaseTask<Boolean>() {
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

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {

            }
        });
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
