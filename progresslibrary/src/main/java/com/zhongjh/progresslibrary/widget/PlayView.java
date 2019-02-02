package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.entity.RecordingItem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 一个播放的view
 * Created by zhongjh on 2019/2/1.
 */
public class PlayView extends FrameLayout {

    private static final String LOG_TAG = "PlayView";
    private static final String ARG_ITEM = "recording_item";
    private RecordingItem mRecordingItem;   // 当前音频数据

    public ViewHolder mViewHolder;          // 控件集合

    private boolean isPlaying = false;      // 标记当前播放状态

    // stores minutes and seconds of the length of the file.
    long minutes = 0;
    long seconds = 0;

    String mFileLength;// 该音频文件的总时

    // region 有关音频

    private MediaPlayer mMediaPlayer = null;

    private Handler mHandler = new Handler();

    // endreigon 有关音频

    public PlayView(@NonNull Context context) {
        this(context, null);
    }

    public PlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    /**
     * 初始化view
     */
    private void initView(AttributeSet attrs) {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false);

        mViewHolder = new ViewHolder(View.inflate(getContext(), R.layout.layout_play, this));

        initListener();
    }

    /**
     * 初始化相关数据
     *
     * @param recordingItem      音频数据源
     * @param audioProgressColor 进度条颜色
     */
    public void setData(RecordingItem recordingItem, int audioProgressColor){
        this.mRecordingItem = recordingItem;
        long itemDuration = mRecordingItem.getLength();
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);

        // 设置进度条颜色
        ColorFilter filter = new LightingColorFilter
                (audioProgressColor, audioProgressColor);
        mViewHolder.seekbar.getProgressDrawable().setColorFilter(filter);
        mViewHolder.seekbar.getThumb().setColorFilter(filter);
    }

    private void initListener() {

        // 进度条
        mViewHolder.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMediaPlayer != null && fromUser) {
                    // 如果处于播放中
                    mMediaPlayer.seekTo(progress);
                    mHandler.removeCallbacks(mRunnable);

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    mViewHolder.tvCurrentProgress.setText(String.format("%02d:%02d", minutes, seconds));

                    updateSeekBar();

                } else if (mMediaPlayer == null && fromUser) {
                    // 如果还未播放
                    prepareMediaPlayerFromPoint(progress);
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    // remove message Handler from updating progress bar
                    mHandler.removeCallbacks(mRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mRunnable);
                    mMediaPlayer.seekTo(seekBar.getProgress());

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    mViewHolder.tvCurrentProgress.setText(String.format("%02d:%02d", minutes, seconds));
                    updateSeekBar();
                }
            }
        });

        // 播放按钮
        mViewHolder.imgPlay.setOnClickListener(v -> {
            onPlay(isPlaying);
            isPlaying = !isPlaying;
        });
    }

    // region 有关音频的方法

    /**
     * 播放或者暂停
     * @param isPlaying 播放或者暂停
     */
    private void onPlay(boolean isPlaying){
        if (!isPlaying) {
            // 当前MediaPlayer未播放音频
            if(mMediaPlayer == null) {
                startPlaying(); // 从头开始
            } else {
                resumePlaying(); // 恢复当前暂停的MediaPlayer
            }

        } else {
            // 暂停MediaPlayer
            pausePlaying();
        }
    }

    /**
     * 从头开始播放
     */
    private void startPlaying() {
        mViewHolder.imgPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(mRecordingItem.getFilePath());
            mMediaPlayer.prepare();
            mViewHolder.seekbar.setMax(mMediaPlayer.getDuration());

            mMediaPlayer.setOnPreparedListener(mp -> mMediaPlayer.start());
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mMediaPlayer.setOnCompletionListener(mp -> stopPlaying());

        updateSeekBar();

//        // 播放音频时保持屏幕打开
//        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 将MediaPlayer设置为从音频文件中间直接开始（意思即为拉到一半立即播放）
     * @param progress 进度
     */
    private void prepareMediaPlayerFromPoint(int progress) {
        //set mediaPlayer to start from middle of the audio file

        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(mRecordingItem.getFilePath());
            mMediaPlayer.prepare();
            mViewHolder.seekbar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.seekTo(progress);

            mMediaPlayer.setOnCompletionListener(mp -> stopPlaying());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

//        // 播放完音频后允许再次关闭屏幕
//        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 暂停MediaPlayer
     */
    private void pausePlaying() {
        mViewHolder.imgPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.pause();
    }

    /**
     * 恢复当前暂停的MediaPlayer
     */
    private void resumePlaying() {
        mViewHolder.imgPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.start();
        updateSeekBar();
    }

    /**
     * 播放停止
     */
    private void stopPlaying() {
        mViewHolder.imgPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        mViewHolder.seekbar.setProgress(mViewHolder.seekbar.getMax());
        isPlaying = !isPlaying;

        mViewHolder.tvCurrentProgress.setText(mFileLength);
        mViewHolder.seekbar.setProgress(mViewHolder.seekbar.getMax());

//        // 播放完音频后允许再次关闭屏幕
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 更新 进度条
     */
    private Runnable mRunnable = () -> {
        if(mMediaPlayer != null){
            int mCurrentPosition = mMediaPlayer.getCurrentPosition();   // 获取当前进度
            mViewHolder.seekbar.setProgress(mCurrentPosition); // 赋值

            long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition)
                    - TimeUnit.MINUTES.toSeconds(minutes);
            mViewHolder.tvCurrentProgress.setText(String.format("%02d:%02d", minutes, seconds));

            updateSeekBar();
        }
    };

    /**
     * 每隔一秒更新
     */
    private void updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1000);
    }

    // endregion

    public static class ViewHolder {

        public View rootView;
        public ImageView imgPlay;
        public SeekBar seekbar;
        public TextView tvCurrentProgress;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.imgPlay = rootView.findViewById(R.id.imgPlay);
            this.seekbar = rootView.findViewById(R.id.seekbar);
            this.tvCurrentProgress = rootView.findViewById(R.id.tvCurrentProgress);
        }

    }
}
