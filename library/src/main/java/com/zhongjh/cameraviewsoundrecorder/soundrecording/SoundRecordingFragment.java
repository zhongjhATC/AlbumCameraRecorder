package com.zhongjh.cameraviewsoundrecorder.soundrecording;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
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
import com.zhongjh.cameraviewsoundrecorder.soundrecording.service.RecordingService;
import com.zhongjh.cameraviewsoundrecorder.widget.photovieobutton.RecordButton;

import java.io.File;
import java.io.IOException;

/**
 * 录音
 * Created by zhongjh on 2018/8/22.
 */
public class SoundRecordingFragment extends Fragment implements ServiceConnection {

    private String title;
    private int page;

    // 是否正在录音中
    private boolean mRecording = false;
    private boolean isChecked;
    private ViewHolder mViewHolder;

    long timeWhenPaused = 0; //存储用户单击暂停按钮的时间

    private MediaPlayer mMediaPlayer = null;

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

//        iv_record.setOnClickListener(v -> {
//            isChecked = !isChecked;
//            final int[] stateSet = {android.R.attr.state_checked * (isChecked ? 1 : -1)};
//            iv_record.setImageState(stateSet,true);
//            if(isChecked){
//                iv_ring.setImageResource(R.drawable.shape_ring_red);
//                tv_hint.setText("录音");
//                tv_hint.setTextColor(getResources().getColor(R.color.app_color));
//                // TODO: 2018/3/21  录音
//            }else {
//                iv_ring.setImageResource(R.drawable.shape_ring_white);
//                tv_hint.setText("录制会议");
//                tv_hint.setTextColor(getResources().getColor(R.color.white));
//            }
//        });

        return view;
    }

    /**
     * 事件
     */
    private void initListener() {
        mViewHolder.btnPhotoVideo.setRecordingListener(new PhotoVideoListener() {
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

                // 显示录音后的布局

            }

            @Override
            public void recordZoom(float zoom) {

            }

            @Override
            public void recordError() {

            }
        });

        mViewHolder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
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
//            mViewHolder.btnPhotoVideo.setImageResource(R.drawable.ic_media_stop);
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
     * @param isPlaying 播放或者停止
     */
    private void onPlay(boolean isPlaying){
        if (!isPlaying) {
            //currently MediaPlayer is not playing audio
            if(mMediaPlayer == null) {
                startPlaying(); //start from beginning
            } else {
                resumePlaying(); //resume the currently paused MediaPlayer
            }

        } else {
            //pause the MediaPlayer
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
            mMediaPlayer.setDataSource(item.getFilePath());
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());

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

        updateSeekBar();

        //keep screen on while playing audio
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 显示录音后的界面
     */
    private void showRecordEndView() {
        mViewHolder.recordProgressBar.setVisibility(View.VISIBLE);
        // 录音按钮转变成播放按钮，播放录音

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onBindingDied(ComponentName name) {

    }

    public static class ViewHolder {
        public View rootView;
        public Chronometer chronometer;
        public RecordButton btnPhotoVideo;
        public ProgressBar recordProgressBar;
        public Button btnPlay;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.chronometer = rootView.findViewById(R.id.chronometer);
            this.btnPhotoVideo = rootView.findViewById(R.id.btnPhotoVideo);
            this.btnPlay = rootView.findViewById(R.id.btnPlay);
        }

    }
}
