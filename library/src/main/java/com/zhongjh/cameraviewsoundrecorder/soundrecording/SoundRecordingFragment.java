package com.zhongjh.cameraviewsoundrecorder.soundrecording;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.PhotoVideoListener;
import com.zhongjh.cameraviewsoundrecorder.soundrecording.db.RecordingItem;
import com.zhongjh.cameraviewsoundrecorder.soundrecording.service.RecordingService;
import com.zhongjh.cameraviewsoundrecorder.soundrecording.widget.PhotoVideoLayout;

import java.io.File;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;
import static it.sephiroth.android.library.imagezoom.ImageViewTouchBase.LOG_TAG;

/**
 * 录音
 * Created by zhongjh on 2018/8/22.
 */
public class SoundRecordingFragment extends Fragment {

    private String title;
    private int page;

    // 是否正在录音中
    private boolean mRecording = false;
    // 是否正在播放中
    private boolean isPlaying = false;
    private boolean isChecked;
    private ViewHolder mViewHolder;

    long timeWhenPaused = 0; //存储用户单击暂停按钮的时间

    private MediaPlayer mMediaPlayer = null;
    RecordingItem recordingItem; // 存储这首歌

    public static SoundRecordingFragment newInstance(int page, String title) {
        SoundRecordingFragment soundRecordingFragment = new SoundRecordingFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        soundRecordingFragment.setArguments(args);
        return soundRecordingFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_soundrecording_zjh, container, false);
        mViewHolder = new ViewHolder(view);
        initListener();
        return view;
    }

    /**
     * 事件
     */
    private void initListener() {
        // 录音等事件
        mViewHolder.pvLayout.setPhotoVideoListener(new PhotoVideoListener() {
            @Override
            public void actionDown() {

            }

            @Override
            public void takePictures() {

            }

            @Override
            public void recordShort(long time) {

            }

            @Override
            public void recordStart() {
                // 录音开启
                mRecording = true;
                onRecord(mRecording);
            }

            @Override
            public void recordEnd(long time) {
                // 录音结束
                mRecording = false;
                onRecord(mRecording);
            }

            @Override
            public void recordZoom(float zoom) {

            }

            @Override
            public void recordError() {

            }
        });

        // 播放事件
        mViewHolder.pvLayout.mViewHolder.rlSoundRecording.setOnClickListener(view -> {
            // 获取service存储的数据
            recordingItem = new RecordingItem();
            SharedPreferences sharePreferences = getActivity().getSharedPreferences("sp_name_audio", MODE_PRIVATE);
            final String filePath = sharePreferences.getString("audio_path", "");
            long elpased = sharePreferences.getLong("elpased", 0);
            recordingItem.setFilePath(filePath);
            recordingItem.setLength((int) elpased);

            // 播放
            onPlay(isPlaying);
            isPlaying = !isPlaying;
        });

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
            // 录音
            mViewHolder.pvLayout.mViewHolder.iv_record.setImageResource(R.drawable.ic_stop_black_24dp);
            Toast.makeText(getActivity(), "开始录音", Toast.LENGTH_SHORT).show();
            // 创建文件
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                //folder /SoundRecorder doesn't exist, create the folder
                folder.mkdir();
            }
            // 开始计时
            mViewHolder.chronometer.setBase(SystemClock.elapsedRealtime());
            mViewHolder.chronometer.start();
            // 这个暂时不需要
//            mViewHolder.chronometer.setOnChronometerTickListener(chronometer -> {
//            });

            //start RecordingService
            getActivity().startService(intent);
            //keep screen on while recording
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            // 停止录音
//            mViewHolder.btnPhotoVideo.setImageResource(R.drawable.ic_mic_white_36dp);
            mViewHolder.chronometer.stop();
            mViewHolder.chronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;

            getActivity().stopService(intent);
            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
//        mPlayButton.setImageResource(R.drawable.ic_media_pause);
        mMediaPlayer = new MediaPlayer();

        try {
            // 文件地址
            mMediaPlayer.setDataSource(recordingItem.getFilePath());
            mMediaPlayer.prepare();

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });

//        updateSeekBar(); // 进度更新

        //keep screen on while playing audio
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 恢复播放
     */
    private void resumePlaying() {
//        mPlayButton.setImageResource(R.drawable.ic_media_pause); 暂停图
//        mHandler.removeCallbacks(mRunnable);  进度线程
        mMediaPlayer.start();
//        updateSeekBar(); 更新
    }

    /**
     * 暂停播放
     */
    private void pausePlaying() {
//        mPlayButton.setImageResource(R.drawable.ic_media_play);  设置播放图
//        mHandler.removeCallbacks(mRunnable);                     线程停止
        mMediaPlayer.pause();
    }

    /**
     * 停止播放
     */
    private void stopPlaying() {
//        mPlayButton.setImageResource(R.drawable.ic_media_play); 设置成播放的图片
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
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 显示录音后的界面
     */
    private void showRecordEndView() {
        mViewHolder.recordProgressBar.setVisibility(View.VISIBLE);
        // 录音按钮转变成播放按钮，播放录音

    }


    public static class ViewHolder {
        public View rootView;
        public Chronometer chronometer;
        public ProgressBar recordProgressBar;
        public PhotoVideoLayout pvLayout;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.chronometer = rootView.findViewById(R.id.chronometer);
            this.recordProgressBar = rootView.findViewById(R.id.recordProgressBar);
            this.pvLayout = rootView.findViewById(R.id.pvLayout);
        }

    }
}
