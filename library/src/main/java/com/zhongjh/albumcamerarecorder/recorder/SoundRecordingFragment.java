package com.zhongjh.albumcamerarecorder.recorder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.albumcamerarecorder.recorder.db.RecordingItem;
import com.zhongjh.albumcamerarecorder.recorder.service.RecordingService;
import com.zhongjh.albumcamerarecorder.recorder.widget.SoundrecordingLayout;
import com.zhongjh.albumcamerarecorder.utils.ViewBusinessUtils;
import com.zhongjh.albumcamerarecorder.utils.constants.MultimediaTypes;
import com.zhongjh.albumcamerarecorder.widget.OperationLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_MULTIMEDIA_TYPES;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_RECORDING_ITEM;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_SELECTION_PATH;
import static it.sephiroth.android.library.imagezoom.ImageViewTouchBase.LOG_TAG;

/**
 * 录音
 * Created by zhongjh on 2018/8/22.
 */
public class SoundRecordingFragment extends Fragment {

    protected Activity mActivity;
    // 是否正在播放中
    private boolean isPlaying = false;
    private ViewHolder mViewHolder;

    long timeWhenPaused = 0; //存储用户单击暂停按钮的时间

    private MediaPlayer mMediaPlayer = null;
    RecordingItem recordingItem; // 存储的数据

    public static SoundRecordingFragment newInstance() {
        return  new SoundRecordingFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_soundrecording_zjh, container, false);
        mViewHolder = new ViewHolder(view);
        // 设置录音最长录制时间30秒
        mViewHolder.pvLayout.setDuration(30000);
        initListener();
        return view;
    }

    /**
     * 事件
     */
    private void initListener() {
        // 录音等事件
        mViewHolder.pvLayout.setPhotoVideoListener(new ClickOrLongListener() {
            @Override
            public void actionDown() {
                // 母窗体禁止滑动
                ViewBusinessUtils.setTablayoutScroll(false,((MainActivity) mActivity),mViewHolder.pvLayout);
            }

            @Override
            public void onClick() {

            }

            @Override
            public void onLongClickShort(long time) {
                // 停止录音
                onRecord(false);
                mViewHolder.chronometer.setBase(SystemClock.elapsedRealtime());
                // 母窗体启动滑动
                ViewBusinessUtils.setTablayoutScroll(true,((MainActivity) mActivity),mViewHolder.pvLayout);
            }

            @Override
            public void onLongClick() {
                // 录音开启
                onRecord(true);
            }

            @Override
            public void onLongClickEnd(long time) {
                // 录音结束
                onRecord(false);
                showRecordEndView();
            }

            @Override
            public void onLongClickZoom(float zoom) {

            }

            @Override
            public void onLongClickError() {

            }
        });

        // 播放事件
        ((SoundrecordingLayout.ViewHolder)mViewHolder.pvLayout.mViewHolder).rlSoundRecording.setOnClickListener(view -> {
            // 获取service存储的数据
            recordingItem = new RecordingItem();
            SharedPreferences sharePreferences = mActivity.getSharedPreferences("sp_name_audio", MODE_PRIVATE);
            final String filePath = sharePreferences.getString("audio_path", "");
            long elpased = sharePreferences.getLong("elpased", 0);
            recordingItem.setFilePath(filePath);
            recordingItem.setLength((int) elpased);

            // 播放
            onPlay(isPlaying);
            isPlaying = !isPlaying;
        });

        // 确认和取消
        mViewHolder.pvLayout.setOperaeListener(new OperationLayout.OperaeListener() {
            @Override
            public void cancel() {
                // 母窗体启动滑动
                ViewBusinessUtils.setTablayoutScroll(true,((MainActivity) mActivity),mViewHolder.pvLayout);
            }

            @Override
            public void confirm() {
                //获取视频路径
                Intent result = new Intent();
                result.putExtra(EXTRA_RESULT_RECORDING_ITEM, recordingItem);
                result.putExtra(EXTRA_MULTIMEDIA_TYPES, MultimediaTypes.AUDIO);
                mActivity.setResult(RESULT_OK, result);
                mActivity.finish();
            }
        });
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
     * @param start 录音开始或者停止
     */
    private void onRecord(boolean start) {
        Intent intent = new Intent(getActivity(), RecordingService.class);
        if (start) {
//            Toast.makeText(BaseApplication.getInstance(), "开始录音", Toast.LENGTH_SHORT).show();
            // 创建文件
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                //folder /SoundRecorder doesn't exist, create the folder
                folder.mkdir();
            }
            // 开始计时,从1秒开始算起
            mViewHolder.chronometer.setBase(SystemClock.elapsedRealtime() - 1000);
            mViewHolder.chronometer.start();
            // 这个暂时不需要
//            mViewHolder.chronometer.setOnChronometerTickListener(chronometer -> {
//            });

            //start RecordingService
            mActivity.startService(intent);
            //keep screen on while recording
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            mViewHolder.chronometer.stop();
            timeWhenPaused = 0;

            mActivity.stopService(intent);
            //allow the screen to turn off again once recording is finished
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
            //currently MediaPlayer is not playing audio
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
        ((SoundrecordingLayout.ViewHolder)mViewHolder.pvLayout.mViewHolder).iv_record.setImageResource(R.drawable.ic_pause_white_24dp);
        mMediaPlayer = new MediaPlayer();

        try {
            // 文件地址
            mMediaPlayer.setDataSource(recordingItem.getFilePath());
            mMediaPlayer.prepare();

            mMediaPlayer.setOnPreparedListener(mp -> mMediaPlayer.start());
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mMediaPlayer.setOnCompletionListener(mp -> stopPlaying());

//        updateSeekBar(); // 进度更新

        //keep screen on while playing audio
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 恢复播放
     */
    private void resumePlaying() {
        // 暂停图
        ((SoundrecordingLayout.ViewHolder)mViewHolder.pvLayout.mViewHolder).iv_record.setImageResource(R.drawable.ic_pause_white_24dp);
//        mHandler.removeCallbacks(mRunnable);  进度线程
        mMediaPlayer.start();
//        updateSeekBar(); 更新
    }

    /**
     * 暂停播放
     */
    private void pausePlaying() {
        // 设置成播放的图片
        ((SoundrecordingLayout.ViewHolder)mViewHolder.pvLayout.mViewHolder).iv_record.setImageResource(R.drawable.ic_play_arrow_white_24dp);
//        mHandler.removeCallbacks(mRunnable);                     线程停止
        mMediaPlayer.pause();
    }

    /**
     * 停止播放
     */
    private void stopPlaying() {
        // 设置成播放的图片
        ((SoundrecordingLayout.ViewHolder)mViewHolder.pvLayout.mViewHolder).iv_record.setImageResource(R.drawable.ic_play_arrow_white_24dp);
//        mHandler.removeCallbacks(mRunnable); // 有关进度的
        // 停止mediaPlayer
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

//        mSeekBar.setProgress(mSeekBar.getMax()); 进度
        isPlaying = !isPlaying;

//        mCurrentProgressTextView.setText(mFileLengthTextView.getText()); 进度文本
//        mSeekBar.setProgress(mSeekBar.getMax()); // 进度

        // 一旦音频播放完毕，保持屏幕常亮 这个设置关闭
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 显示录音后的界面
     */
    private void showRecordEndView() {
        // 录音按钮转变成播放按钮，播放录音
        ((SoundrecordingLayout.ViewHolder)mViewHolder.pvLayout.mViewHolder).iv_record.setImageResource(R.drawable.ic_play_arrow_white_24dp);
    }

    public static class ViewHolder {
        View rootView;
        public Chronometer chronometer;
        public SoundrecordingLayout pvLayout;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.chronometer = rootView.findViewById(R.id.chronometer);
            this.pvLayout = rootView.findViewById(R.id.pvLayout);
        }

    }
}
